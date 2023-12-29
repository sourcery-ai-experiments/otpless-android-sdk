package com.otpless.dto;

import androidx.annotation.NonNull;

public enum OtplessChannelType {
    WHATSAPP("WHATSAPP"),
    GMAIL("GMAIL"),
    APPLE("APPLE"),
    TWITTER("TWITTER"),
    DISCORD("DISCORD"),
    SLACK("SLACK"),
    FACEBOOK("FACEBOOK"),
    LINKEDIN("LINKEDIN"),
    MICROSOFT("MICROSOFT");

    @NonNull
    private final String channelName;

    private OtplessChannelType(@NonNull String channelName) {
        this.channelName = channelName;
    }

    @NonNull
    public String getChannelName() {
        return channelName;
    }
}
