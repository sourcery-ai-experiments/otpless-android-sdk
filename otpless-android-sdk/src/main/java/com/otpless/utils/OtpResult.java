package com.otpless.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

public class OtpResult {

    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_TIMEOUT = 2;
    public static final int STATUS_ERROR = 0;

    private final boolean isSuccess;
    private final String otp;
    private final String errorMessage;

    private int statusCode = STATUS_SUCCESS;

    public OtpResult(boolean isSuccess, String otp, String errorMessage) {
        this.isSuccess = isSuccess;
        this.otp = otp;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    @Nullable
    public String getOtp() {
        return otp;
    }

    @Nullable
    public String getErrorMessage() {
        return errorMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public OtpResult addStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.US, "isSuccess: %s, otp: %s, errorMessage: %s, status: %d ", isSuccess, otp, errorMessage, statusCode);
    }
}
