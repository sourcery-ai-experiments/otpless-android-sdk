package com.otpless.dto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

public class HeadlessResponse {

    @NonNull
    private final String channel;
    @Nullable
    private final JSONObject data;
    @Nullable
    private final String error;

    public HeadlessResponse(
            @NonNull String request, @Nullable JSONObject data, @Nullable String error
    ) {
        this.channel = request;
        this.data = data;
        this.error = error;
    }

    @NonNull
    public String getChannel() {
        return channel;
    }

    @Nullable
    public JSONObject getData() {
        return data;
    }

    @Nullable
    public String getError() {
        return error;
    }
}