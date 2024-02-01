package com.otpless.fedo.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class WebAuthnLoginInitData implements Serializable {

    @SerializedName("challenge")
    @NonNull
    private String challenge;

    @SerializedName("timeout")
    private long timeout;

    @SerializedName("allowCredentials")
    private List<WebAuthnAllowedCredential> allowCredentials;

    @SerializedName("rpId")
    private String rpId;

    @SerializedName("userVerification")
    private String userVerification;

    @NonNull
    public String getChallenge() {
        return challenge;
    }

    public long getTimeout() {
        return timeout;
    }

    public List<WebAuthnAllowedCredential> getAllowCredentials() {
        return allowCredentials;
    }

    public String getRpId() {
        return rpId;
    }

    public String getUserVerification() {
        return userVerification;
    }
}
