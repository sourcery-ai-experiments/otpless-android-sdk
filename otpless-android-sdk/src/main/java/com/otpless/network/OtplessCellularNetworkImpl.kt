package com.otpless.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.TelephonyNetworkSpecifier
import android.net.Uri
import android.net.wifi.WifiNetworkSpecifier
import android.net.wifi.aware.WifiAwareNetworkSpecifier
import android.os.Build
import android.os.Looper
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import com.otpless.BuildConfig
import org.json.JSONObject
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule

/**
 * CellularNetworkManager requests Cellular Network from the system to be available to
 * current process. On some devices (such as Samsung and Huawei), when WiFi is on
 * it is possible that the devices default to the WiFi and set it as the active, and hide
 * the cellular (despite being available). This class (for API Level 26+) forces the system to
 * make the cellular network visible to the process.
 *
 */

internal class OtplessCellularNetworkImpl(private val context: Context) : OtplessCellularNetwork {


    private val cellularInfo by lazy {
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    private val connectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private var cellularNetworkCallBack: ConnectivityManager.NetworkCallback? = null

    private var timeoutTask: TimerTask? = null


    override fun openWithDataCellular(url: Uri, callback: OtplessCellularDataListener) {
        if (BuildConfig.DEBUG)
            checkNetworks()
        execute {
            if (it) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "-> After forcing isAvailable? ${isCellularAvailable()}")
                    Log.d(TAG, "-> After forcing isBound? ${isCellularBoundToProcess()}")
                }
                ApiManager.getInstance().get(url, null, object : ApiCallback<JSONObject> {
                    override fun onError(exception: Exception) {
                        val response = makeErrorJson("sdk_api_error", exception.message ?: "Data connectivity error")
                        callback.onCellularDataResult(response)
                    }

                    override fun onSuccess(data: JSONObject) {
                        callback.onCellularDataResult(data)
                    }
                })

            } else {
                Log.d(TAG, "We do not have a path")
                val response = makeErrorJson("sdk_no_data_connectivity", "Data connectivity not available")
                callback.onCellularDataResult(response)
            }
        }
    }

    private fun makeErrorJson(code: String, description: String): JSONObject {
        val json = JSONObject()
        json.put("error", code)
        json.put("error_description", description)
        return json
    }

    private fun execute(onCompletion: (isSuccess: Boolean) -> Unit) {
        val capabilities = intArrayOf(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val transportTypes = intArrayOf(NetworkCapabilities.TRANSPORT_CELLULAR)

        forceCellular(capabilities, transportTypes) { isOnCellular ->
            onCompletion(isOnCellular)
        }
    }

    private fun forceCellular(
        capabilities: IntArray,
        transportTypes: IntArray,
        onCompletion: (isSuccess: Boolean) -> Unit
    ) {
        Log.d(TAG, "------ Forcing Cellular ------")
        try {
            if (!cellularInfo.isDataEnabled) {
                Log.d(TAG, "Mobile Data is NOT enabled, we can not force cellular!")
                onCompletion(false)
                return
            } else {
                Log.d(TAG, "-> Mobile Data is Enabled!")
            }
        } catch (ex: Exception) {
            Log.d(TAG, "-> error: ${ex.message}")
        }


        if (cellularNetworkCallBack == null) {
            cellularNetworkCallBack = object : ConnectivityManager.NetworkCallback() {

                override fun onAvailable(network: Network) {
                    Log.d(TAG, "Cellular OnAvailable:")
                    networkInfo(network)
                    try {
                        // Binds the current process to network.  All Sockets created in the future
                        // (and not explicitly bound via a bound SocketFactory from {@link Network#getSocketFactory() Network.getSocketFactory()})
                        // will be bound to network.
                        Log.d(TAG, "  Binding to process:")
                        bind(network)
                        Log.d(TAG, "  Binding finished. Is Main thread? ${isMainThread()}")
                        cancelTimeout()
                        onCompletion(true) // Network request needs to be done in this lambda
                    } catch (e: IllegalStateException) {
                        Log.d(TAG, "ConnectivityManager.NetworkCallback.onAvailable: $e")
                        cancelTimeout()
                        onCompletion(false)
                    } finally {
                        // Release the request when done.
                        unregisterCellularNetworkListener()
                        bind(null)
                    }
                }

                override fun onLost(network: Network) {
                    Log.d(TAG, "Cellular OnLost:")
                    networkInfo(network)
                    super.onLost(network)
                }

                override fun onUnavailable() {
                    Log.d(TAG, "Cellular onUnavailable")
                    // When this method gets called due to timeout, the callback will automatically be unregistered
                    // So no need to call unregisterCellularNetworkListener()
                    // But we should null it
                    cellularNetworkCallBack = null
                    onCompletion(false)
                    super.onUnavailable()
                }
            }
            Log.d(TAG, "Creating a network builder on Main thread? ${isMainThread()}")

            val request = NetworkRequest.Builder()
            // Just in case as per Documentation
            request.removeTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            request.removeTransportType(NetworkCapabilities.TRANSPORT_BLUETOOTH)

            for (capability in capabilities) {
                request.addCapability(capability)
            }
            for (transportType in transportTypes) {
                request.addTransportType(transportType)
            }
            Log.d(TAG, "Cellular requested")

            requestNetwork(request.build(), onCompletion)
            Log.d(TAG, "Forcing Cellular - Requesting to registered...")
        } else {
            // Perhaps there is already one registered, and in progress or waiting to be timed out
            Log.d(TAG, "There is already a Listener registered.")
        }
    }

    /**
     * Return the Country Code of the Carrier + MCC + MNC
     * in uppercase
     */
    private fun getOperator(): String? {
        if (cellularInfo.phoneType == TelephonyManager.PHONE_TYPE_GSM) {
            val op: String = cellularInfo.simOperator
            Log.d(TAG, "-> getOperator $op")
            return op
        } else {
            Log.d(TAG, "-> getOperator not PHONE_TYPE_GSM!")
        }

        return null
    }

    private fun bind(network: Network?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            ConnectivityManager.setProcessDefaultNetwork(network)
        } else {
            connectivityManager.bindProcessToNetwork(network) // API Level 23, 6.0 Marsh
        }
    }

    private fun requestNetwork(request: NetworkRequest, onCompletion: (isSuccess: Boolean) -> Unit) {
        // The network request will live, until unregisterNetworkCallback is called or app exit.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) { // API Level 26
            timeoutTask = Timer("Setting Up", true).schedule(TIME_OUT) {
                Log.d(TAG, "Timeout...")
                Thread { onCompletion(false) }.start()
            }
            connectivityManager.requestNetwork(
                request,
                cellularNetworkCallBack as ConnectivityManager.NetworkCallback
            )
        } else {
            connectivityManager.requestNetwork(
                request,
                cellularNetworkCallBack as ConnectivityManager.NetworkCallback,
                TIME_OUT.toInt()
            )
        }
    }

    private fun cancelTimeout() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Log.d(TAG, "Cancelling timeout")
            timeoutTask?.cancel()
        }
    }

    private fun unregisterCellularNetworkListener() {
        Log.d(TAG, "UnregisteringCellularNetworkListener")
        cellularNetworkCallBack?.let {
            Log.d(TAG, "CallBack available, unregistering it.")
            connectivityManager.unregisterNetworkCallback(cellularNetworkCallBack as ConnectivityManager.NetworkCallback)
            cellularNetworkCallBack = null
        }
    }

    private fun isMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }

    private fun checkNetworks() {
        Log.d(TAG, "----- Check Network ------")
        Log.d(TAG, "Is Default Network Active? " + connectivityManager.isDefaultNetworkActive.toString())
        boundNetwork()
        activeNetworkInfo()
        availableNetworks()
    }

    private fun isCellular(network: Network): Boolean {
        val caps = connectivityManager.getNetworkCapabilities(network)
        caps?.let {
            if (it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            ) {
                return true
            }
        }
        return false
    }

    private fun isCellularAvailable(): Boolean {
        val networks = connectivityManager.allNetworks
        var available = false
        for (network in networks) {
            available = isCellular(network)
            if (available) break
        }
        return available
    }

    private fun isCellularBoundToProcess(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // API 23
            connectivityManager.boundNetworkForProcess?.let {
                return isCellular(it)
            }
        }
        return false
    }

    companion object {
        private const val TAG = "OtplessCellularNetwork"
        private const val TIME_OUT: Long = 5000
    }

    private fun boundNetwork() { // API 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "----- Bound network ----")
            connectivityManager.boundNetworkForProcess?.let { networkInfo(it) }
        }
    }

    private fun activeNetworkInfo() { // API 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "----- Active network ----")
            connectivityManager.activeNetwork?.let { networkInfo(it) }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R) // 30
    private fun networkType(capability: NetworkCapabilities) {
        when (capability.networkSpecifier) {
            is TelephonyNetworkSpecifier -> Log.d(TAG, "Cellular network")
            is WifiNetworkSpecifier -> Log.d(TAG, "Wifi network")
            is WifiAwareNetworkSpecifier -> Log.d(TAG, "Wifi Aware network")
        }
    }

    private fun availableNetworks() {
        Log.d(TAG, "----------Available Networks----------")
        val networks = connectivityManager.allNetworks
        for (network in networks) {
            networkInfo(network)
            val caps = connectivityManager.getNetworkCapabilities(network)
            caps?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // IF >= API Level 30
                    networkType(it)
                    Log.d(TAG, "Signal Strength : " + it.signalStrength)

                    if (it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                        Log.d(TAG, "Cap: Internet Capability")
                    }
                    if (it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        Log.d(TAG, "Cap: Cellular")
                    }
                    if (it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)) {
                        Log.d(TAG, "Cap: Wifi Aware")
                    }
                    if (it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        Log.d(TAG, "Cap: Wifi")
                    }
                    if (it.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)) {
                        Log.d(TAG, "Cap: Bluetooth")
                    }
                }
            }
        }
    }

    private fun networkInfo(network: Network) {
        fun linkName(network: Network): String {
            val networkLinkProperties = connectivityManager.getLinkProperties(network)
            return networkLinkProperties?.interfaceName ?: "None"
        }
        Log.d(TAG, "Name:" + linkName(network))
        linkAddresses(network)
    }

    private fun linkAddresses(network: Network) {
        val activeNetworkLinkProperties = connectivityManager.getLinkProperties(network)
        activeNetworkLinkProperties?.linkAddresses?.let {
            for (address in it) {
                Log.d(TAG, "Address: $address")
            }
        }
    }
}