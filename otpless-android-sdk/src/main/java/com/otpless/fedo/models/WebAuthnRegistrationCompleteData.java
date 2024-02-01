package com.otpless.fedo.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WebAuthnRegistrationCompleteData implements Serializable {

    @SerializedName("userId")
    private String userId;

    public String getUserId() {
        return userId;
    }
}
