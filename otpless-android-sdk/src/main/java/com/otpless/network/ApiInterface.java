package com.otpless.network;

import com.otpless.dto.OTPLessSignUpResponse;
import com.otpless.dto.OtplessResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface ApiInterface {

    String BasePath = "/api/v1/user/";

    @GET(BasePath + "getSignupUrl")
    Call<OTPLessSignUpResponse> signUp(@HeaderMap Map<String, String> headers, @QueryMap Map<String, String> query);

    @POST(BasePath + "getUserDetails")
    Call<OtplessResponse> userDetails(@HeaderMap Map<String, String> headers, @Body Map<String, String> body);

}
