package com.otpless.fedo.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WebAuthnUser implements Serializable {

    @SerializedName("name")
    private String name;

    @SerializedName("id")
    private String id;

    @SerializedName("displayName")
    private String displayName;


    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : name;
    }
}
