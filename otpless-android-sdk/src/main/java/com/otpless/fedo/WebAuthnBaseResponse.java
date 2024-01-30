package com.otpless.fedo;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WebAuthnBaseResponse<T> implements Serializable {

    @SerializedName("requestId")
    @NonNull
    private String requestId;

    @SerializedName("data")
    @NonNull
    private T data;

    @NonNull
    public String getRequestId() {
        return requestId;
    }

    @NonNull
    public T getData() {
        return data;
    }
}
