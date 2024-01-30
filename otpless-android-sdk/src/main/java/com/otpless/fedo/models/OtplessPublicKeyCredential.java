package com.otpless.fedo.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class OtplessPublicKeyCredential implements Serializable {

    @SerializedName("alg")
    private int algorithm;

    @SerializedName("type")
    private String type;

    public int getAlgorithm() {
        return algorithm;
    }

    public String getType() {
        return type;
    }
}
