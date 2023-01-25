package com.otpless.main;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.otpless.dto.OtplessResponse;

public class OtplessResultContract extends ActivityResultContract<Uri, OtplessResponse> {

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Uri input) {
        Intent intent = new Intent(context, OtplessLoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("otpless_request", input);
        return intent;
    }

    @Override
    public OtplessResponse parseResult(int resultCode, @Nullable Intent intent) {
        if (intent == null) return null;
        if (resultCode == Activity.RESULT_CANCELED) {
            String error = intent.getStringExtra("error_message");
            if (error == null) return null;
            OtplessResponse detail = new OtplessResponse();
            detail.setMessage(error);
            detail.setStatus("failed");
            return detail;
        }
        String waid = intent.getStringExtra("waId");
        if (waid != null) {
            final OtplessResponse userDetail =  new OtplessResponse();
            userDetail.setWaId(waid);
            userDetail.setStatus("success");
            final String userNumber = intent.getStringExtra("userNumber");
            userDetail.setUserNumber(userNumber);
            return userDetail;
        }
        return null;
    }
}
