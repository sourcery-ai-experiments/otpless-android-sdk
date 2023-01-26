package com.otpless.main;

import com.otpless.dto.OTPLessSignUpResponse;
import com.otpless.dto.OtplessResponse;

public interface OtplessApiView {
    void onSignupSuccess(OTPLessSignUpResponse response, String url);

    void onUserDetailSuccess(OtplessResponse response);

    void onApiError(String error);
}
