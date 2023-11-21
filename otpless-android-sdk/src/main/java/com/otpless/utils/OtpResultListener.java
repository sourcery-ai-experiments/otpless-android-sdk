package com.otpless.utils;

@FunctionalInterface
public interface OtpResultListener {
    void onOtpReadResult(final OtpResult otpResult);
}
