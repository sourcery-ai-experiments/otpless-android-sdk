package com.otpless.fedo.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WebAuthnPublicCredential implements Serializable {

    @SerializedName("id")
    private final String id;

    @SerializedName("rawId")
    private final String rawId;

    @SerializedName("type")
    private final String type;

    @SerializedName("response")
    private final WebAuthnAuthenticatorAttestationData authenticatorAttestationData;

    public WebAuthnPublicCredential(String id, String rawId, String type,
                                    WebAuthnAuthenticatorAttestationData authenticatorAttestationData) {
        this.id = id;
        this.rawId = rawId;
        this.type = type;
        this.authenticatorAttestationData = authenticatorAttestationData;
    }
}
