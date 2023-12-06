package com.otpless.views;

@FunctionalInterface
public interface OtplessLoaderCallback {
    void onOtplessLoaderEvent(final OtplessLoaderEvent event);
}
