package com.otpless.fedo.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WebAuthnRegistrationCompleteRequest implements Serializable {

    @SerializedName("requestId")
    private final String requestId;

    @SerializedName("data")
    private final WebAuthnPublicCredential publicCredential;

    public WebAuthnRegistrationCompleteRequest(String requestId, WebAuthnPublicCredential publicCredential) {
        this.requestId = requestId;
        this.publicCredential = publicCredential;
    }
}
