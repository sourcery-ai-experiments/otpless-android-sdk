package com.otpless.main;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;

public class OtplessLoginRequest implements Serializable {

    private final @NonNull
    @SerializedName("url")
    Uri openUrl;

    public OtplessLoginRequest(@NonNull Uri url, @Nullable HashMap<String, String> additionalMap) {
        openUrl = url;
    }

    @NonNull
    public Uri getOpenUrl() {
        return openUrl;
    }
}
