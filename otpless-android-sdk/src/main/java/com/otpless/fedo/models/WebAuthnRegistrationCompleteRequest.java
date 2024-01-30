package com.otpless.fedo.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WebAuthnRegistrationCompleteRequest implements Serializable {

    @SerializedName("requestId")
    private final String requestId;

    @SerializedName("publicKeyCredentialCreationResponse")
    private final String publicKeyCredentialCreationResponse;

    public WebAuthnRegistrationCompleteRequest(String requestId, String publicKeyCredentialCreationResponse) {
        this.requestId = requestId;
        this.publicKeyCredentialCreationResponse = publicKeyCredentialCreationResponse;
    }
}
