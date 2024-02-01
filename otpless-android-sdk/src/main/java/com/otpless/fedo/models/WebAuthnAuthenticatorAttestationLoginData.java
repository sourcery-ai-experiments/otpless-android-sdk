package com.otpless.fedo.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WebAuthnAuthenticatorAttestationLoginData implements Serializable {

    @SerializedName("clientDataJSON")
    private final String clientDataJSON;

    @SerializedName("authenticatorData")
    private final String authenticatorData;

    @SerializedName("signature")
    private final String signature;

    @SerializedName("userHandle")
    private final String userHandle;

    public WebAuthnAuthenticatorAttestationLoginData(String clientDataJSON, String authenticatorData, String signature, String userHandle) {
        this.clientDataJSON = clientDataJSON;
        this.authenticatorData = authenticatorData;
        this.signature = signature;
        this.userHandle = userHandle;
    }
}
