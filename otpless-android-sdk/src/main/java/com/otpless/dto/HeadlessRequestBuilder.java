package com.otpless.dto;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HeadlessRequestBuilder {

    @NonNull
    private HeadlessRequestType requestType = HeadlessRequestType.OTPLINK;
    private String phoneNumber;
    private String email;
    private String otp;
    private String code;
    private OtplessChannelType channel;

    private HashMap<String, String> extraChannelType = new HashMap<>();

    public HeadlessRequestBuilder setRequestType(@NonNull HeadlessRequestType requestType) {
        this.requestType = requestType;
        return this;
    }

    public HeadlessRequestBuilder setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public HeadlessRequestBuilder setEmail(String email) {
        this.email = email;
        return this;
    }

    public HeadlessRequestBuilder setOtp(String otp) {
        this.otp = otp;
        return this;
    }

    public HeadlessRequestBuilder setCode(String code) {
        this.requestType = HeadlessRequestType.VERIFY_CODE;
        this.code = code;
        return this;
    }

    public HeadlessRequestBuilder setChannel(@NonNull OtplessChannelType channel) {
        this.channel = channel;
        return this;
    }

    public HeadlessRequestBuilder addExtraChannel(@NonNull String key, @NonNull String value) {
        this.extraChannelType.put(key, value);
        return this;
    }

    @NonNull
    public HeadlessRequestType getRequestType() {
        return requestType;
    }

    public JSONObject build() {
        final JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("request", this.requestType.getRequestName());
            final JSONObject params = new JSONObject();
            switch (requestType) {
                case VERIFY_CODE:
                    params.put("code", this.code);
                    break;
                default:
                    if (this.phoneNumber != null) params.put("pn", this.phoneNumber);
                    if (this.email != null) params.put("eml", this.email);
                    if (this.otp != null) params.put("otp", this.otp);
                    for (Map.Entry<String, String> entry : this.extraChannelType.entrySet()) {
                        params.put(entry.getKey(), entry.getValue());
                    }
                    if (this.channel != null) params.put("ch", this.channel.getChannelName());
            }

            requestJson.put("params", params);
        } catch (JSONException ignore) {
        }
        return requestJson;
    }
}
