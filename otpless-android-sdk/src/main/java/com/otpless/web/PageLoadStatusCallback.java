package com.otpless.web;

@FunctionalInterface
public interface PageLoadStatusCallback {
    void onPageStatusChange(LoadingStatusData loadingStatusData);
}
