package com.otpless.fedo.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class WebAuthnRegistrationInitData implements Serializable {

    @SerializedName("rp")
    private WebAuthnUser rp;

    @SerializedName("user")
    private WebAuthnUser user;

    @SerializedName("challenge")
    @NonNull
    private String challenge;

    @SerializedName("timeout")
    private long timeout;

    @SerializedName("pubKeyCredParams")
    private List<OtplessPublicKeyCredential> pubKeyCredParams;

    @SerializedName("authenticatorSelection")
    private AuthenticatorSelection authenticatorSelection;

    public long getTimeout() {
        return timeout;
    }

    @NonNull
    public String getChallenge() {
        return challenge;
    }

    public List<OtplessPublicKeyCredential> getPubKeyCredParams() {
        return pubKeyCredParams;
    }

    public AuthenticatorSelection getAuthenticatorSelection() {
        return authenticatorSelection;
    }

    public WebAuthnUser getRp() {
        return rp;
    }

    public WebAuthnUser getUser() {
        return user;
    }
}
