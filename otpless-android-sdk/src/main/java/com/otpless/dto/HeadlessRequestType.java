package com.otpless.dto;

public enum HeadlessRequestType {
    OTPLINK("OTPLINK"),
    SSO("SSO"),
    REQUEST_OTP("REQUEST_OTP"),
    RESEND_OTP("RESEND_OTP"),
    VERIFY_OTP("VERIFY_OTP"),
    VERIFY_CODE("VERIFY_CODE");

    private final String requestName;
    HeadlessRequestType(String requestName) {
        this.requestName = requestName;
    }

    public String getRequestName() {
        return requestName;
    }
}
