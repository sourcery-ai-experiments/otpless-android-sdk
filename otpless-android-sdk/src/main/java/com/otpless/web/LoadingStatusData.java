package com.otpless.web;

import androidx.annotation.Nullable;

public class LoadingStatusData {

    private final LoadingStatus loadingStatus;
    @Nullable
    private final String message;

    public LoadingStatusData(final LoadingStatus status, @Nullable final String message) {
        this.loadingStatus = status;
        this.message = message;
    }

    public LoadingStatusData(final LoadingStatus status) {
        this(status, null);
    }

    public LoadingStatus getLoadingStatus() {
        return loadingStatus;
    }

    @Nullable
    public String getMessage() {
        return message;
    }
}
