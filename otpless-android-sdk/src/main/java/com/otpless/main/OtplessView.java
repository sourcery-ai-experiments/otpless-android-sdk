package com.otpless.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.otpless.dto.HeadlessRequest;
import com.otpless.dto.OtplessRequest;
import com.otpless.views.OtplessUserDetailCallback;

public interface OtplessView {

    /// explicitly setting the callback
    void setCallback(@NonNull final OtplessRequest request, final OtplessUserDetailCallback callback);

    /// explicitly closing the view
    void closeView();

    /// send the onBackPressed call from activity to here it returns false if it do not handles call
    /// and it returns true if it handles call
    boolean onBackPressed();

    /// intent to verify mainly from onNewIntent and also can be from onCreate
    boolean verifyIntent(Intent intent);

    /// to receive the events in apps
    void setEventCallback(final OtplessEventCallback callback);

    void setBackBackButtonSubscription(final boolean backSubscription);

    /// to show otpless login page
    void showOtplessLoginPage(@NonNull final OtplessRequest request, OtplessUserDetailCallback callback);

    default void setLoaderVisibility(final boolean isVisible) {}

    default void setRetryVisibility(final boolean isVisible) {}

    void hideContainerView();

    default void onActivityResult(final int requestCode, final int resultCode, final Intent data) {}

    void startHeadless(@NonNull final HeadlessRequest request, final HeadlessResponseCallback callback);

    void setHeadlessCallback(final HeadlessResponseCallback callback);

    void initHeadless(@NonNull String appId, @Nullable Bundle savedInstanceState);

    default void enableOneTap(final boolean isEnable) {
    }
}