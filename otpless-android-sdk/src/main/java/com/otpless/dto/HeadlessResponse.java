package com.otpless.dto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public class HeadlessResponse {

    @NonNull
    private final String responseType;
    @Nullable
    private final JSONObject response;
    @Nullable
    private final int statusCode;

    public HeadlessResponse(@NonNull String responseType, @Nullable JSONObject response, int statusCode) {
        this.responseType = responseType;
        this.response = response;
        this.statusCode = statusCode;
    }

    @NonNull
    public String getResponseType() {
        return responseType;
    }

    @Nullable
    public JSONObject getResponse() {
        return response;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public static HeadlessResponse getBackPressedResponse(final String channelType) {
        final JSONObject response = new JSONObject();
        try {
            response.put("errorMessage", "User cancelled");
            final JSONObject detail = new JSONObject();
            detail.put("channelType", channelType);
            response.put("detail", detail);
        } catch (JSONException ignore) {
        }
        return new HeadlessResponse("BACKPRESS", response, 0);
    }

    @NonNull
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("responseType: ")
                .append(responseType)
                .append("\n")
                .append("statusCode: ")
                .append(statusCode);
        if (response != null) {
            builder.append("\n");
            builder.append("response: ");
            builder.append(response);
        }
        return builder.toString();
    }
}