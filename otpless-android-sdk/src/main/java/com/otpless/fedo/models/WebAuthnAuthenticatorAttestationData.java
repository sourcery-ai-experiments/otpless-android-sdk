package com.otpless.fedo.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WebAuthnAuthenticatorAttestationData implements Serializable {

    @SerializedName("clientDataJSON")
    private final String clientDataJSON;

    @SerializedName("attestationObject")
    private final String attestationObject;

    public WebAuthnAuthenticatorAttestationData(String clientDataJSON, String attestationObject) {
        this.clientDataJSON = clientDataJSON;
        this.attestationObject = attestationObject;
    }
}
