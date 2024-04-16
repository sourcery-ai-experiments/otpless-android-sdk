package com.otpless.network

import android.net.Uri
import org.json.JSONObject

interface OtplessCellularNetwork {
    fun openWithDataCellular(url: Uri, callback: OtplessCellularDataListener)
}

interface OtplessCellularDataListener {
    fun onCellularDataResult(data: JSONObject)
}