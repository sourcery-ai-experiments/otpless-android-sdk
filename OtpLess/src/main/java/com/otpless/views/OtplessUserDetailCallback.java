package com.otpless.views;

import com.otpless.dto.OtplessResponse;

@FunctionalInterface
public interface OtplessUserDetailCallback {
    void onOtplessUserDetail(final OtplessResponse otplessUserDetail);
}
