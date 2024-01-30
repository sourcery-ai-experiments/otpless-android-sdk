package com.otpless.fedo.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WebAuthnLoginInitRequest implements Serializable {

    @SerializedName("uuid")
    private final String uuid;

    @SerializedName("domainName")
    private final String domainName;

    public WebAuthnLoginInitRequest(String uuid, String domainName) {
        this.uuid = uuid;
        this.domainName = domainName;
    }

    public String getUuid() {
        return uuid;
    }

    public String getDomainName() {
        return domainName;
    }
}
