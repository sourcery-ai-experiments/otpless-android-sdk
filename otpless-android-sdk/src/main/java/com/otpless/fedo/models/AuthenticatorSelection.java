package com.otpless.fedo.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AuthenticatorSelection implements Serializable {

    @SerializedName("authenticatorAttachment")
    private String authenticatorAttachment;

    @SerializedName("residentKeyRequirement")
    private String residentKeyRequirement;

    @SerializedName("requireResidentKey")
    private boolean requireResidentKey;

    @SerializedName("userVerification")
    private String userVerification;

    public String getAuthenticatorAttachment() {
        return authenticatorAttachment;
    }

    public String getResidentKeyRequirement() {
        return residentKeyRequirement;
    }

    public boolean isRequireResidentKey() {
        return requireResidentKey;
    }

    public String getUserVerification() {
        return userVerification;
    }
}
