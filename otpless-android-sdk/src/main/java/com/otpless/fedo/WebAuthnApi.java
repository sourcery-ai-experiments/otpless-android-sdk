package com.otpless.fedo;

import com.otpless.fedo.models.WebAuthnLoginInitData;
import com.otpless.fedo.models.WebAuthnLoginInitRequest;
import com.otpless.fedo.models.WebAuthnRegistrationCompleteData;
import com.otpless.fedo.models.WebAuthnRegistrationCompleteRequest;
import com.otpless.fedo.models.WebAuthnRegistrationInitData;
import com.otpless.fedo.models.WebAuthnRegistrationInitRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface WebAuthnApi {

    @POST("registration/initiate")
    Call<WebAuthnBaseResponse<WebAuthnRegistrationInitData>> initRegistration(
            final @Body WebAuthnRegistrationInitRequest request
            );

    @POST("registration/complete")
    Call<WebAuthnBaseResponse<WebAuthnRegistrationCompleteData>> completeRegistration(
            final @Body WebAuthnRegistrationCompleteRequest request
    );

    @POST("login/initiate")
    Call<WebAuthnBaseResponse<WebAuthnLoginInitData>> initLogin(
            final @Body WebAuthnLoginInitRequest request
            );

    @POST("login/complete")
    Call<WebAuthnBaseResponse<WebAuthnRegistrationCompleteData>> completeLogin(
            final @Body WebAuthnRegistrationCompleteRequest request
    );
}
