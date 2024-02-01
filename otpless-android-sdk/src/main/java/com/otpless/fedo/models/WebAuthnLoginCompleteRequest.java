package com.otpless.fedo.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WebAuthnLoginCompleteRequest implements Serializable {
    @SerializedName("id")
    private final String id;

    @SerializedName("rawId")
    private final String rawId;

    @SerializedName("type")
    private final String type;

    @SerializedName("response")
    private final WebAuthnAuthenticatorAttestationLoginData attestationLoginData;

    public WebAuthnLoginCompleteRequest(String id, String rawId, String type, WebAuthnAuthenticatorAttestationLoginData attestationLoginData) {
        this.id = id;
        this.rawId = rawId;
        this.type = type;
        this.attestationLoginData = attestationLoginData;
    }
}
