package com.otpless.fedo.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class WebAuthnAllowedCredential implements Serializable {

    @SerializedName("type")
    private String type;

    @SerializedName("id")
    private String id;

    @SerializedName("transports")
    private List<String> transports;

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public List<String> getTransports() {
        return transports;
    }
}
