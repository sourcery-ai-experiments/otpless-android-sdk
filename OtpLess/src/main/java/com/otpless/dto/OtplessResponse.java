package com.otpless.dto;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class OtplessResponse implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("waId")
    private String waId;

    @SerializedName("userNumber")
    private String userNumber;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getWaId() {
        return waId;
    }

    public void setWaId(String waId) {
        this.waId = waId;
    }

    public String getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(String userNumber) {
        this.userNumber = userNumber;
    }

    @Override
    public String toString() {
        return "OtplessUserDetail{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", waId='" + waId + '\'' +
                '}';
    }
}
