package com.otpless.main;

import android.app.Activity;
import static android.content.Context.MODE_PRIVATE;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.identity.Identity;
import com.otpless.dto.HeadlessRequest;
import com.otpless.dto.HeadlessResponse;
import com.otpless.dto.OtplessRequest;
import com.otpless.dto.OtplessResponse;
import com.otpless.dto.Triple;
import com.otpless.dto.Tuple;
import com.otpless.network.NetworkStatusData;
import com.otpless.network.OnConnectionChangeListener;
import com.otpless.network.OtplessNetworkManager;
import com.otpless.utils.OtpReaderManager;
import com.otpless.utils.Utility;
import com.otpless.views.OtplessContainerView;
import com.otpless.views.OtplessUserDetailCallback;
import com.otpless.web.NativeWebManager;
import com.otpless.web.OtplessWebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.UUID;

final class OtplessViewImpl implements OtplessView, OtplessViewContract, OnConnectionChangeListener, NativeWebListener {

    private static final String VIEW_TAG_NAME = "OtplessView";

    private final Activity activity;
    private OtplessRequest mOtplessRequest;

    private WeakReference<OtplessContainerView> wContainer = new WeakReference<>(null);
    private OtplessUserDetailCallback detailCallback;
    private OtplessEventCallback eventCallback;

    private boolean backSubscription = false;
    private boolean isLoaderVisible = true;
    private boolean isRetryVisible = true;
    private boolean isContainerViewInvisible = false;
    private static final String BASE_LOADING_URL = "https://otpless.com";

    @NonNull String installId = "";
    @NonNull String trackingSessionId = "";
    private static final String INSTALL_ID_KEY = "otpless_inid";
    private boolean isHeadless = false;
    private boolean isOneTapEnabled = true;
    private HeadlessRequest headlessRequest;

    private HeadlessResponseCallback headlessResponseCallback;
    private final Queue<ViewGroup> helpQueue = new LinkedList<>();
    OtplessViewRemovalNotifier viewRemovalNotifier = null;
    private ActivityResultLauncher<IntentSenderRequest> phoneNumberHintIntentResultLauncher = null;

    OtplessViewImpl(final Activity activity) {
        this.activity = activity;
        final SharedPreferences preferences = activity.getPreferences(MODE_PRIVATE);
        final String inid = preferences.getString(INSTALL_ID_KEY, "");
        if (Utility.isValid(inid)) {
            this.installId = inid;
        } else {
            this.installId = UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(INSTALL_ID_KEY, this.installId);
            editor.apply();
        }
        trackingSessionId = UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }
    Activity getActivity() {
        return this.activity;
    }

    @Override
    public void startHeadless(@NonNull final HeadlessRequest request, final HeadlessResponseCallback callback) {
        // configuration setting
        this.isHeadless = true;
        // request and callback setting
        this.headlessRequest = request;
        this.headlessResponseCallback = callback;
        // check if view is all ready added then call the web js method
        final OtplessContainerView containerView = wContainer.get();
        if (containerView.getWebView() != null && containerView.getWebView().getLoadedUrl() != null) {
            containerView.getWebManager().sendHeadlessRequest();
            return;
        }
        // add view and load url
        addViewIfNotAdded();
        loadWebView(null, null);
    }

    private void loadWebView(final String baseUrl, Uri uri) {
        final OtplessContainerView containerView = wContainer.get();
        if (containerView == null || containerView.getWebView() == null) return;
        if (this.isHeadless) {
            final Uri.Builder builder = Uri.parse(getFirstLoadingUrl()).buildUpon();
            builder.appendQueryParameter("isHeadless", String.valueOf(true));
            if (uri != null) {
                String code = uri.getQueryParameter("code");
                this.headlessRequest.setCode(code);
                // check if url is already loaded call the javascript directly
                if (containerView.getWebView().getLoadedUrl() != null) {
                    containerView.getWebManager().sendHeadlessRequest();
                    return;
                }
            }
            containerView.getWebView().loadWebUrl(builder.build().toString());
            return;
        }
        if (baseUrl == null) {
            String firstLoadingUrl = getFirstLoadingUrl();
            if (uri == null) {
                containerView.getWebView().loadWebUrl(firstLoadingUrl);
            } else {
                reloadToVerifyCode(containerView.getWebView(), uri, firstLoadingUrl);
            }
        } else if (uri == null) {
            containerView.getWebView().loadWebUrl(baseUrl);
        } else {
            reloadToVerifyCode(containerView.getWebView(), uri, baseUrl);
        }
    }

    private String getFirstLoadingUrl() {
        final Uri.Builder urlToLoad = Uri.parse(OtplessViewImpl.BASE_LOADING_URL).buildUpon();
        urlToLoad.appendPath("appid");
        final String loginUrl;
        if (this.isHeadless) {
            urlToLoad.appendPath(this.headlessRequest.getAppId());
            loginUrl = "otpless." + this.headlessRequest.getAppId().toLowerCase(Locale.US) + "://otpless";
        } else {
            urlToLoad.appendPath(this.mOtplessRequest.getAppId());
            // check for additional json params while loading
            final JSONObject extraParams = mOtplessRequest.toJsonObj();
            try {
                String methodName = extraParams.optString("method").toLowerCase();
                if (methodName.equals("get")) {
                    // add the params in url
                    final JSONObject params = extraParams.getJSONObject("params");
                    for (Iterator<String> it = params.keys(); it.hasNext(); ) {
                        String key = it.next();
                        final String value = params.optString(key);
                        if (value.isEmpty()) continue;
                        urlToLoad.appendQueryParameter(key, value);
                    }
                }
            } catch (JSONException ignore) {
            }
            loginUrl = "otpless." + this.mOtplessRequest.getAppId().toLowerCase(Locale.US) + "://otpless";
        }
        // adding package name
        final String packageName = this.activity.getPackageName();
        urlToLoad.appendQueryParameter("package", packageName);
        urlToLoad.appendQueryParameter("hasWhatsapp", String.valueOf(Utility.isWhatsAppInstalled(activity)));
        urlToLoad.appendQueryParameter("hasOtplessApp", String.valueOf(Utility.isOtplessAppInstalled(activity)));
        //check other chatting apps
        final PackageManager pm = activity.getPackageManager();
        final List<Triple<String, String, Boolean>> messagingApps = Utility.getMessagingInstalledAppStatus(pm);
        for (final Triple<String, String, Boolean> installStatus : messagingApps) {
            urlToLoad.appendQueryParameter("has" + installStatus.getFirst(), String.valueOf(installStatus.getThird()));
        }
        urlToLoad.appendQueryParameter("login_uri", loginUrl);
        urlToLoad.appendQueryParameter("nbbs", String.valueOf(this.backSubscription));
        urlToLoad.appendQueryParameter("inid", this.installId);
        urlToLoad.appendQueryParameter("tsid", this.trackingSessionId);
        return urlToLoad.build().toString();
    }

    @Override
    public void setCallback(@NonNull final OtplessRequest request, final OtplessUserDetailCallback callback) {
        this.mOtplessRequest = request;
        this.detailCallback = callback;
        this.isHeadless = false;
    }

    @Override
    public void setHeadlessCallback(@NonNull final HeadlessRequest request, final HeadlessResponseCallback callback) {
        // setting configuration
        this.isHeadless = true;
        // setting request and callback
        this.headlessRequest = request;
        this.headlessResponseCallback = callback;
        if (activity.getIntent() == null) return;
        Uri uri = activity.getIntent().getData();
        // check for headless and onetap special case
        if (uri == null && this.isOneTapEnabled) {
            handleHeadlessAndOnetapSpecialCase();
        }
    }

    @Override
    public void closeView() {
        removeView();
    }

    @Override
    public void onVerificationResult(int resultCode, JSONObject jsonObject) {
        if (this.detailCallback != null) {
            final OtplessResponse response = new OtplessResponse();
            if (resultCode == Activity.RESULT_CANCELED) {
                response.setErrorMessage("user cancelled");
                this.detailCallback.onOtplessUserDetail(response);
            } else {
                // check for error on jsonObject
                final String possibleError = jsonObject.optString("error");
                if (possibleError.isEmpty()) {
                    response.setData(jsonObject);
                } else {
                    response.setErrorMessage(possibleError);
                }
                this.detailCallback.onOtplessUserDetail(response);
            }
            // if adding fab on decor has been added
        }
        removeView();
    }

    @Override
    public JSONObject getExtraParams() {
        return this.isHeadless ? this.headlessRequest.makeJson() : this.mOtplessRequest.toJsonObj();
    }

    @Override
    public boolean onBackPressed() {
        if (wContainer.get() == null) return false;
        final NativeWebManager manager = wContainer.get().getWebManager();
        if (manager == null) return false;
        final OtplessWebView webView = wContainer.get().getWebView();
        if (webView == null) return false;
        if (this.isHeadless) {
            final HeadlessResponse response = new HeadlessResponse(
                    this.headlessRequest.getChannel().getChannelName(), null, "User cancelled");
            this.headlessResponseCallback.onHeadlessResponse(response);
            removeView();
            return true;
        }
        if (this.eventCallback != null && this.backSubscription) {
            if (manager.getBackSubscription()) {
                webView.callWebJs("onHardBackPressed");
            }
            final OtplessEventData eventData = new OtplessEventData(OtplessEventCode.BACK_PRESSED, null);
            this.eventCallback.onOtplessEvent(eventData);
            return true;
        }
        if (manager.getBackSubscription()) {
            // back-press has been consumed
            webView.callWebJs("onHardBackPressed");
        } else {
            // remove the view
            onVerificationResult(Activity.RESULT_CANCELED, null);
        }
        return true;
    }

    private void handleHeadlessAndOnetapSpecialCase() {
        // if one tap is enabled load the url here
        addViewIfNotAdded();
        final OtplessContainerView containerView = wContainer.get();
        if (containerView == null || containerView.getWebView() == null) return;
        final Uri.Builder builder = Uri.parse(getFirstLoadingUrl()).buildUpon();
        builder.appendQueryParameter("isHeadless", String.valueOf(true));
        final SharedPreferences pref = activity.getPreferences(MODE_PRIVATE);
        final String plov = pref.getString("plov", "");
        if (!plov.isEmpty()) {
            builder.appendQueryParameter("plov", plov);
        }
        containerView.getWebView().loadWebUrl(builder.build().toString());
    }

    @Override
    public boolean verifyIntent(Intent intent) {
        Uri uri = intent.getData();
        if (uri == null) return false;
        if (!"otpless".equals(uri.getHost())) return false;
        // request type solution for future use
        final String requestType = uri.getQueryParameter("requestType");
        if ("headless".equals(requestType)) {
            this.isHeadless = true;
        }
        // check if passed deeplink is having uri query param then open that is chrome custom tab
        final String otplessCode = uri.getQueryParameter("uri");
        if (Utility.isValid(otplessCode)) {
            // checkout the host for provided otpless uri
            Uri queryUri;
            try {
                queryUri = Uri.parse(otplessCode);
                if (queryUri.getHost() == null || !queryUri.getHost().contains("otpless")) {
                    return false;
                }
            } catch (Exception ignore) {
                return false;
            }
            Utility.openChromeCustomTab(activity, queryUri);
            return true;
        }
        // check if web view is already loaded or not if webview is loaded then reload the url
        final OtplessContainerView otplessContainerView = wContainer.get();
        if (otplessContainerView != null && otplessContainerView.getWebView() != null) {
            final OtplessWebView webView = wContainer.get().getWebView();
            final String loadedUrl = webView.getLoadedUrl();
            loadWebView(loadedUrl, uri);
        } else {
            // add view if not added
            Log.d(VIEW_TAG_NAME, "adding the view in low memory case.");
            addViewIfNotAdded();
            loadWebView(null, uri);
        }
        return true;
    }

    private void addViewIfNotAdded() {
        // safety checks
        final Window window = activity.getWindow();
        if (window == null) {
            Utility.pushEvent("window_null");
            return;
        }
        final View decorView = window.getDecorView();
        if (decorView == null) {
            Utility.pushEvent("decorview_null");
            return;
        }
        final ViewGroup parent = findParentView();
        if (parent == null) {
            Utility.pushEvent("parent_null");
            return;
        }
        // check if view inflated is already present or not
        View _container = parent.findViewWithTag(VIEW_TAG_NAME);
        if (_container != null) {
            if (this.isHeadless) {
                ((OtplessContainerView)_container).enableHeadlessConfig();
            } else {
                ((OtplessContainerView)_container).disableHeadlessConfig();
                ((OtplessContainerView)_container).setUiConfiguration(mOtplessRequest.toJsonObj());
            }
            return;
        }
        // add the view
        final OtplessContainerView containerView = new OtplessContainerView(activity);
        containerView.setTag(VIEW_TAG_NAME);
        containerView.setId(View.generateViewId());
        containerView.setViewContract(this);
        // adding listener to the data components from this class
        if (containerView.getWebManager() != null) {
            containerView.getWebManager().setNativeWebListener(OtplessViewImpl.this);
        }
        containerView.isToShowLoader = this.isLoaderVisible;
        containerView.isToShowRetry = this.isRetryVisible;
        if (this.isHeadless) {
            containerView.enableHeadlessConfig();
        } else {
            containerView.setUiConfiguration(mOtplessRequest.toJsonObj());
        }
        parent.addView(containerView);
        wContainer = new WeakReference<>(containerView);
        OtplessNetworkManager.getInstance().addListeners(activity, this);
        if (isContainerViewInvisible) {
            containerView.setVisibility(View.INVISIBLE);
        }
    }

    private void removeView() {
        // safety checks
        final Window window = activity.getWindow();
        if (window == null) return;
        final View decorView = window.getDecorView();
        if (decorView == null) return;
        final ViewGroup parent = findParentView();
        if (parent == null) return;
        // search and remove the view
        View container = parent.findViewWithTag(VIEW_TAG_NAME);
        if (container != null) {
            parent.removeView(container);
            if (viewRemovalNotifier != null) {
                viewRemovalNotifier.onOtplessViewRemoved(this);
            }
            OtplessNetworkManager.getInstance().removeListener(activity, this);
            wContainer.clear();
        }
        // unregister the otp autoreader
        OtpReaderManager.getInstance().stopOtpReader();
    }

    private ViewGroup findParentView() {
        final Window window = activity.getWindow();
        if (window == null) return null;
        final View decorView = window.getDecorView();
        if (!(decorView instanceof ViewGroup)) return null;
        helpQueue.clear();
        helpQueue.add((ViewGroup) decorView);
        return findFrameLayout();
    }

    // using bfs to find the view from decor
    private ViewGroup findFrameLayout() {
        final ViewGroup group = helpQueue.poll();
        if (group == null) return null;
        final int childCount = group.getChildCount();
        int index = 0;
        while (index < childCount) {
            final View v = group.getChildAt(index);
            if (v.getId() == android.R.id.content) {
                return (ViewGroup) (v);
            }
            if (v instanceof FrameLayout) {
                return (FrameLayout)(v);
            } else if (v instanceof ViewGroup) {
                helpQueue.add((ViewGroup) v);
            }
            index++;
        }
        return findFrameLayout();
    }

    private void reloadToVerifyCode(final OtplessWebView webView, @NonNull final Uri uri, @NonNull final String loadedUrl) {
        final boolean hasCode;
        final String code = uri.getQueryParameter("code");
        hasCode = code != null && code.length() != 0;
        final Uri newUrl = Utility.combineQueries(
                Uri.parse(loadedUrl), uri
        );
        webView.loadWebUrl(newUrl.toString());
        sendIntentInEvent(hasCode);
    }

    private void sendIntentInEvent(final boolean isSuccess) {
        final JSONObject params = new JSONObject();
        final String type;
        if (isSuccess) {
            type = "success";
        } else {
            type = "error";
        }
        try {
            params.put("type", type);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        Utility.pushEvent("intent_redirect_in", params);
    }

    @Override
    public void onConnectionChange(NetworkStatusData statusData) {
    }

    @Override
    public void setEventCallback(final OtplessEventCallback callback) {
        this.eventCallback = callback;
    }

    @Override
    public void setBackBackButtonSubscription(final boolean backSubscription) {
        this.backSubscription = backSubscription;
    }

    @Override
    public void onOtplessEvent(OtplessEventData event) {
        if (this.eventCallback == null) return;
        this.eventCallback.onOtplessEvent(event);
    }

    @NonNull
    @Override
    public String getInstallationId() {
        return this.installId;
    }

    @Override
    @NonNull
    public String getTrackingSessionId() {
        return this.trackingSessionId;
    }

    @Override
    public @Nullable ActivityResultLauncher<IntentSenderRequest> getPhoneNumberHintLauncher() {
        return this.phoneNumberHintIntentResultLauncher;
    }

    @Override
    public void hideContainerView() {
        isContainerViewInvisible = true;
    }

    @Override
    public void showOtplessLoginPage(@NonNull final OtplessRequest request, OtplessUserDetailCallback callback) {
        this.detailCallback = callback;
        this.mOtplessRequest = request;
        this.isHeadless = false;
        addViewIfNotAdded();
        loadWebView(null, null);
    }

    @Override
    public void setLoaderVisibility(boolean isVisible) {
        this.isLoaderVisible = isVisible;
    }

    @Override
    public void setRetryVisibility(boolean isVisible) {
        this.isRetryVisible = isVisible;
    }

    void registerPhoneHintForResult() {
        if (phoneNumberHintIntentResultLauncher != null) return;
        if (activity instanceof ComponentActivity) {
            try {
                phoneNumberHintIntentResultLauncher =
                        ((ComponentActivity) activity).registerForActivityResult(
                                new ActivityResultContracts.StartIntentSenderForResult(), result -> {
                                    if (wContainer.get() != null && wContainer.get().getWebManager() != null) {
                                        final NativeWebManager manager = wContainer.get().getWebManager();
                                        try {
                                            String phoneNumber = Identity.getSignInClient(activity).getPhoneNumberFromIntent(result.getData());
                                            manager.onPhoneNumberSelectionResult(
                                                    new Tuple<>(phoneNumber, null)
                                            );
                                        } catch (Exception exception) {
                                            manager.onPhoneNumberSelectionResult(
                                                    new Tuple<>(null, exception)
                                            );
                                        }
                                    }
                                });
            } catch (Throwable ignore) {
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final OtplessContainerView otplessContainerView = wContainer.get();
        if (otplessContainerView != null && otplessContainerView.getWebView() != null && otplessContainerView.getWebManager() != null) {
            otplessContainerView.getWebManager()
                    .onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onHeadlessResult(HeadlessResponse response, boolean closeView) {
        if (this.headlessResponseCallback != null) {
            this.headlessResponseCallback.onHeadlessResponse(response);
        }
        if (closeView) {
            removeView();
        }
    }

    @Override
    public void enableOneTap(final boolean isEnable) {
        this.isOneTapEnabled = isEnable;
    }
}
