package com.otpless.main;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.otpless.R;
import com.otpless.dto.HeadlessRequestBuilder;
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
import com.otpless.views.FabButtonAlignment;
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
import java.util.Queue;

final class OtplessViewImpl implements OtplessView, OtplessViewContract, OnConnectionChangeListener, NativeWebListener {

    private static final String VIEW_TAG_NAME = "OtplessView";

    private final Activity activity;
    private JSONObject extras;

    private WeakReference<OtplessContainerView> wContainer = new WeakReference<>(null);
    private OtplessUserDetailCallback detailCallback;
    private OtplessEventCallback eventCallback;
    private FabButtonAlignment mAlignment = FabButtonAlignment.BottomRight;
    private int mBottomMargin = 24;
    private int mSideMargin = 16;
    private String mFabText = "Sign in";
    private boolean mShowOtplessFab = true;
    private WeakReference<Button> wFabButton = new WeakReference<>(null);
    private static final int ButtonWidth = 120;
    private static final int ButtonHeight = 40;

    private boolean isLoginPageEnabled = false;
    private boolean backSubscription = false;
    private boolean isLoaderVisible = true;
    private boolean isRetryVisible = true;
    private boolean isHeadless = false;
    private HeadlessRequestBuilder headlessRequestBuilder;

    private HeadlessResponseCallback headlessResponseCallback;

    private final Queue<ViewGroup> helpQueue = new LinkedList<>();

    OtplessViewRemovalNotifier viewRemovalNotifier = null;

    OtplessViewImpl(final Activity activity) {
        this.activity = activity;
    }

    Activity getActivity() {
        return this.activity;
    }

    @Override
    public void startOtpless(JSONObject params) {
        this.extras = params;
        this.isLoginPageEnabled = false;
        addViewIfNotAdded();
        loadWebView(null, null);
    }

    @Override
    public void startOtpless(OtplessUserDetailCallback callback) {
        this.detailCallback = callback;
        this.startOtpless();
    }

    @Override
    public void startOtpless(JSONObject params, OtplessUserDetailCallback callback) {
        this.detailCallback = callback;
        this.extras = params;
        this.isLoginPageEnabled = false;
        addViewIfNotAdded();
        loadWebView(null, null);
    }

    @Override
    public void startOtpless(@NonNull final OtplessRequest request, final OtplessUserDetailCallback callback) {
        this.extras = request.toJsonObj();
        this.detailCallback = callback;
        this.startOtpless();
    }

    @Override
    public void startHeadless(@NonNull final HeadlessRequestBuilder request, final HeadlessResponseCallback callback) {
        this.isHeadless = true;
        this.headlessRequestBuilder = request;
        this.headlessResponseCallback = callback;
        this.startOtpless();
    }

    @Override
    public void startOtpless() {
        this.isLoginPageEnabled = false;
        addViewIfNotAdded();
        loadWebView(null, null);
    }

    private void loadWebView(final String baseUrl, Uri uri) {
        final OtplessContainerView containerView = wContainer.get();
        if (containerView == null || containerView.getWebView() == null) return;
        if (this.isHeadless) {
            final Uri.Builder builder = Uri.parse(
                    getFirstLoadingUrl("https://otpless.tech/aid/870OD5RME1UBYVDJPKL3", null)
            ).buildUpon();
            builder.appendQueryParameter("isHeadless", String.valueOf(true));
            if (uri != null) {
                String code = uri.getQueryParameter("code");
                this.headlessRequestBuilder.setCode(code);
            }
            containerView.getWebView().loadWebUrl(builder.build().toString());
            return;
        }
        if (baseUrl == null) {
            String firstLoadingUrl = getFirstLoadingUrl("https://otpless.tech/aid/870OD5RME1UBYVDJPKL3", extras);
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

    private String getFirstLoadingUrl(final String url, final JSONObject extraParams) {
        final String packageName = this.activity.getPackageName();
        String loginUrl = packageName + ".otpless://otpless";
        final Uri.Builder urlToLoad = Uri.parse(url).buildUpon();
        // check for additional json params while loading
        if (extraParams != null) {
            try {
                String methodName = extraParams.optString("method").toLowerCase();
                if (methodName.equals("get")) {
                    // add the params in url
                    final JSONObject params = extraParams.getJSONObject("params");
                    for (Iterator<String> it = params.keys(); it.hasNext(); ) {
                        String key = it.next();
                        final String value = params.optString(key);
                        if (value.isEmpty()) continue;
                        if ("login_uri".equals(key)) {
                            loginUrl = value + ".otpless://otpless";
                            continue;
                        }
                        urlToLoad.appendQueryParameter(key, value);
                    }
                }
            } catch (JSONException ignore) {
            }
        }
        // adding loading url and package name, add login uri at last
        urlToLoad.appendQueryParameter("package", packageName);
        urlToLoad.appendQueryParameter("hasWhatsapp", String.valueOf(Utility.isWhatsAppInstalled(activity)));
        urlToLoad.appendQueryParameter("hasOtplessApp", String.valueOf(Utility.isOtplessAppInstalled(activity)));
        //check other chatting apps
        final PackageManager pm = activity.getPackageManager();
        final List<Triple<String, String, Boolean>> messagingApps = Utility.getMessagingInstalledAppStatus(pm);
        for (final Triple<String, String, Boolean> installStatus: messagingApps) {
            urlToLoad.appendQueryParameter("has" + installStatus.getFirst(), String.valueOf(installStatus.getThird()));
        }
        if (isLoginPageEnabled) {
            urlToLoad.appendQueryParameter("lp", String.valueOf(true));
        }
        urlToLoad.appendQueryParameter("login_uri", loginUrl);
        urlToLoad.appendQueryParameter("nbbs", String.valueOf(this.backSubscription));
        return urlToLoad.build().toString();
    }

    @Override
    public void setCallback(final OtplessUserDetailCallback callback, final JSONObject extra) {
        this.setCallback(callback, extra, false);
    }

    @Override
    public void setCallback(OtplessUserDetailCallback callback, JSONObject extra, boolean isLoginPage) {
        this.detailCallback = callback;
        this.extras = extra;
        this.isLoginPageEnabled = isLoginPage;
    }

    @Override
    public void setCallback(final OtplessUserDetailCallback callback, final boolean isLoginPage) {
        this.setCallback(callback, null, isLoginPage);
    }

    @Override
    public void setCallback(@NonNull final OtplessRequest request, final OtplessUserDetailCallback callback ,final boolean isLoginPage) {
        this.extras = request.toJsonObj();
        this.detailCallback = callback;
        this.isLoginPageEnabled = isLoginPage;
    }

    @Override
    public void setHeadlessCallback(@NonNull final HeadlessRequestBuilder request, final HeadlessResponseCallback callback) {
        this.isHeadless = true;
        this.headlessRequestBuilder = request;
        this.headlessResponseCallback = callback;
    }

    @Override
    public void closeView() {
        removeView();
    }

    @Override
    public void onVerificationResult(int resultCode, JSONObject jsonObject) {
        // if login page is not enable and show button is enabled
        // show sign in button
        if (!isLoginPageEnabled && mShowOtplessFab) {
            // check if button is already added
            final Button btn = wFabButton.get();
            if (btn == null) {
                addFabOnDecor();
            } else {
                btn.setVisibility(View.VISIBLE);
            }
        }
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
        return this.headlessRequestBuilder.build();
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
                    this.headlessRequestBuilder.getRequestType().getRequestName(), null, "User cancelled");
            this.headlessResponseCallback.onHeadlessResponse(response);
            removeView();
            return true;
        }
        if (this.eventCallback != null && this.backSubscription && this.isLoginPageEnabled) {
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

    @Override
    public boolean verifyIntent(Intent intent) {
        Uri uri = intent.getData();
        if (uri == null) return false;
        if (!"otpless".equals(uri.getHost())) return false;
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
        if (_container != null) return;
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
        containerView.setUiConfiguration(extras);
        if (this.isHeadless) {
            containerView.enableHeadlessConfig();
        }
        parent.addView(containerView);
        wContainer = new WeakReference<>(containerView);
        OtplessNetworkManager.getInstance().addListeners(activity, this);
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

    @Override
    public void setFabConfig(final FabButtonAlignment alignment, final int sideMargin, final int bottomMargin) {
        mAlignment = alignment;
        switch (alignment) {
            case BottomLeft:
            case BottomRight: {
                if (sideMargin > 0) {
                    mSideMargin = sideMargin;
                }
                if (bottomMargin > 0) {
                    mBottomMargin = bottomMargin;
                }
            }
            break;
            case BottomCenter:
                if (bottomMargin > 0) {
                    mBottomMargin = bottomMargin;
                }
        }
    }

    @Override
    public void showOtplessFab(boolean isToShow) {
        this.mShowOtplessFab = isToShow;
    }

    private void addFabOnDecor() {
        if (wFabButton.get() != null) return;
        final ViewGroup parentView = activity.findViewById(android.R.id.content);
        if (parentView == null) return;
        final Button button = (Button) activity.getLayoutInflater().inflate(R.layout.otpless_fab_button, parentView, false);
        button.setOnClickListener(v -> onFabButtonClicked());
        button.setText(mFabText);
        final ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) button.getLayoutParams();
        // region add the margin
        final int screenWidth = parentView.getWidth();
        final int screenHeight = parentView.getHeight();
        final int buttonWidth = dpToPixel(ButtonWidth);
        final int buttonHeight = dpToPixel(ButtonHeight);
        switch (mAlignment) {
            case Center: {
                // in center case draw of button will be
                int x = (screenWidth - buttonWidth) / 2;
                int y = ((screenHeight - buttonHeight) / 2);
                params.setMargins(x, y, 0, 0);
            }
            break;
            // margin calculation excludes the height of status bar while setting and we are calculating
            // the margin with reference to full screen that's way status bar height is added
            case BottomRight: {
                int marginEnd = dpToPixel(mSideMargin);
                int marginBottom = dpToPixel(mBottomMargin);
                int x = screenWidth - (buttonWidth + marginEnd);
                int y = screenHeight - (buttonHeight + marginBottom);
                params.setMargins(x, y, 0, 0);
            }
            break;
            case BottomLeft: {
                int marginStart = dpToPixel(mSideMargin);
                int marginBottom = dpToPixel(mBottomMargin);
                int y = screenHeight - (buttonHeight + marginBottom);
                params.setMargins(marginStart, y, 0, 0);
            }
            break;
            case BottomCenter: {
                int x = (screenWidth - buttonWidth) / 2;
                int marginBottom = dpToPixel(mBottomMargin);
                int y = screenHeight - (buttonHeight + marginBottom);
                params.setMargins(x, y, 0, 0);
            }
            break;
        }
        // endregion
        parentView.addView(button);
        wFabButton = new WeakReference<>(button);
    }

    private int dpToPixel(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) dp, activity.getResources().getDisplayMetrics());
    }

    private void onFabButtonClicked() {
        final View fBtn = wFabButton.get();
        if (fBtn != null) {
            // make button invisible after first callback
            fBtn.setVisibility(View.INVISIBLE);
        }
        startOtpless();
    }

    private void removeFabFromDecor() {
        final Button fab = wFabButton.get();
        if (fab == null) return;
        final ViewGroup parentView = activity.findViewById(android.R.id.content);
        if (parentView == null) return;
        parentView.removeView(fab);
        wFabButton = new WeakReference<>(null);
    }

    @Override
    public void onSignInCompleted() {
        removeFabFromDecor();
    }

    @Override
    public void setFabText(final String text) {
        if (text == null || text.length() == 0) return;
        this.mFabText = text;
    }

    @Override
    public void showOtplessLoginPage(JSONObject extra, OtplessUserDetailCallback callback) {
        this.setCallback(callback, extra, true);
        addViewIfNotAdded();
        loadWebView(null, null);
    }

    @Override
    public void showOtplessLoginPage(OtplessUserDetailCallback callback) {
        this.setCallback(callback, null, true);
        addViewIfNotAdded();
        loadWebView(null, null);
    }

    @Override
    public void showOtplessLoginPage(@NonNull final OtplessRequest request, OtplessUserDetailCallback callback) {
        this.isLoginPageEnabled = true;
        this.detailCallback = callback;
        this.extras = request.toJsonObj();
        addViewIfNotAdded();
        loadWebView(null, null);
    }

    @Override
    public void showOtplessLoginPage() {
        this.isLoginPageEnabled = true;
        addViewIfNotAdded();
        loadWebView(null, null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent intent) {
        final OtplessContainerView containerView = wContainer.get();
        if (containerView == null || containerView.getWebManager() == null || containerView.getWebView() == null) return;
        if (requestCode != Utility.PHONE_SELECTION_REQUEST_CODE || resultCode != Activity.RESULT_OK || intent == null) return;
        final Tuple<String, Exception> parseData = Utility.parsePhoneNumberSelectionIntent(intent);
        containerView.getWebManager().onPhoneNumberSelectionResult(parseData);
    }

    @Override
    public void setLoaderVisibility(boolean isVisible) {
        this.isLoaderVisible = isVisible;
    }

    @Override
    public void setRetryVisibility(boolean isVisible) {
        this.isRetryVisible = isVisible;
    }

    @Override
    public void onHeadlessResult(HeadlessResponse response) {
        if (this.headlessResponseCallback != null)
            this.headlessResponseCallback.onHeadlessResponse(response);
    }
}
