package com.otpless.dto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.otpless.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

public class HeadlessRequest {

    @NonNull
    private HeadlessChannel channel = HeadlessChannel.PHONE;
    private String phoneNumber;
    private String email;
    private String otp;
    private String code;

    @Nullable
    private HeadlessChannelType channelType;

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.channel = HeadlessChannel.PHONE;
        channelType = null;
        email = null;
    }

    public void setEmail(String email) {
        this.email = email;
        channel = HeadlessChannel.EMAIL;
        phoneNumber = null;
        channelType = null;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setChannelType(@NonNull HeadlessChannelType channelType) {
        this.channelType = channelType;
        channel = HeadlessChannel.OAUTH;
        phoneNumber = null;
        email = null;
    }

    @NonNull
    public HeadlessChannel getChannel() {
        return channel;
    }

    public boolean hasCodeOrOtp() {
        return Utility.isValid(this.code, this.otp);
    }

    public JSONObject makeJson() {
        final JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("channel", this.channel.getChannelName());
            switch (this.channel) {
                case PHONE:
                    requestJson.put("phone", this.phoneNumber);
                    break;
                case EMAIL:
                    requestJson.put("email", this.email);
                    break;
                case OAUTH:
                    assert this.channelType != null;
                    requestJson.put("channelType", this.channelType.getChannelTypeName());
                    break;
            }
            if (this.otp != null) requestJson.put("otp", this.otp);
            if (this.code != null) requestJson.put("code", this.code);

        } catch (JSONException ignore) {
        }
        return requestJson;
    }
}
