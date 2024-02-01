package com.otpless.fedo.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WebAuthnLoginInitRequest implements Serializable {

    @SerializedName("userId")
    private final String userId;

    @SerializedName("domainName")
    private final String domainName;

    public WebAuthnLoginInitRequest(String userId, String domainName) {
        this.userId = userId;
        this.domainName = domainName;
    }

    public String getUserId() {
        return userId;
    }

    public String getDomainName() {
        return domainName;
    }
}
