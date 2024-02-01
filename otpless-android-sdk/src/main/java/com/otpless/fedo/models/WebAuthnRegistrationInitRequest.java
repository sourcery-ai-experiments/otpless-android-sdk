package com.otpless.fedo.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WebAuthnRegistrationInitRequest implements Serializable {

    @NonNull
    @SerializedName("userName")
    private final String userName;

    @NonNull
    @SerializedName("displayName")
    private final String displayName;

    @NonNull
    @SerializedName("userId")
    private final String userId;

    @NonNull
    @SerializedName("domainName")
    private final String domainName;

    @NonNull
    @SerializedName("companyName")
    private final String companyName;


    public WebAuthnRegistrationInitRequest(@NonNull String userName, @NonNull String displayName,
                                           @NonNull String uuid, @NonNull String domainName, @NonNull String companyName) {
        this.userName = userName;
        this.displayName = displayName;
        this.userId = uuid;
        this.domainName = domainName;
        this.companyName = companyName;
    }

    @NonNull
    public String getUserName() {
        return userName;
    }

    @NonNull
    public String getDisplayName() {
        return displayName;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    @NonNull
    public String getDomainName() {
        return domainName;
    }

    @NonNull
    public String getCompanyName() {
        return companyName;
    }
}
