package com.otpless.otplesssample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.otpless.dto.HeadlessRequestBuilder;
import com.otpless.dto.HeadlessRequestType;
import com.otpless.dto.HeadlessResponse;
import com.otpless.dto.OtplessChannelType;
import com.otpless.dto.OtplessResponse;
import com.otpless.main.OtplessManager;
import com.otpless.main.OtplessView;


public class MainActivity extends AppCompatActivity {

    OtplessView otplessView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // copy this code in onCreate of your Login Activity
        otplessView = OtplessManager.getInstance().getOtplessView(this);
//        otplessView.showOtplessLoginPage(this::onOtplessCallback);
        otplessView.setCallback(this::onOtplessCallback, null, true);
        findViewById(R.id.otpless_btn).setOnClickListener(v -> {
            otplessView.showOtplessLoginPage(this::onOtplessCallback);
        });
        findViewById(R.id.sign_in_complete).setOnClickListener(v -> {
            otplessView.onSignInCompleted();
        });

        final HeadlessRequestBuilder request = new HeadlessRequestBuilder()
                .setRequestType(HeadlessRequestType.SSO)
                .setPhoneNumber("917982893748")
                .setChannel(OtplessChannelType.WHATSAPP);
        otplessView.setHeadlessCallback(request, this::onHeadlessCallback);
        findViewById(R.id.headless_sdk_btn).setOnClickListener(v -> {
            otplessView.startHeadless(request, this::onHeadlessCallback);
        });
        otplessView.verifyIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        otplessView.verifyIntent(intent);
    }

    private void onHeadlessCallback(@NonNull final HeadlessResponse response) {
        String message;
        if (response.getError() == null) {
            message = response.getData().toString();
        } else {
            message = response.getError();
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void onOtplessCallback(OtplessResponse response) {
        if (response.getErrorMessage() != null) {
            Toast.makeText(this, response.getErrorMessage(), Toast.LENGTH_LONG).show();
        } else {
            final String token = response.getData().optString("token");
            Toast.makeText(this, token, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (otplessView.onBackPressed()) return;
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        otplessView.onActivityResult(requestCode, resultCode, data);
    }
}