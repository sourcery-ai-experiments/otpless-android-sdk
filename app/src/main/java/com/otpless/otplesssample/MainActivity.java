package com.otpless.otplesssample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioGroup;
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

    private HeadlessRequestType headlessRequestType = HeadlessRequestType.OTPLINK;
    private OtplessChannelType channelType = OtplessChannelType.WHATSAPP;
    private EditText inputEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initTestingView();
        // copy this code in onCreate of your Login Activity
        otplessView = OtplessManager.getInstance().getOtplessView(this);

        findViewById(R.id.headless_sdk_btn).setOnClickListener(v -> {
            otplessView.startHeadless(getHeadlessRequest(), this::onHeadlessCallback);
        });
        otplessView.verifyIntent(getIntent());
    }

    private HeadlessRequestBuilder getHeadlessRequest() {
        final HeadlessRequestBuilder request = new HeadlessRequestBuilder()
                .setRequestType(headlessRequestType)
                .setChannel(channelType);
        final String input = inputEditText.getText().toString();
        try {
            // parse phone number
            Long.parseLong(input);
            request.setPhoneNumber(input);
        } catch (Exception ex) {
            request.setEmail(input);
        }
        return request;
    }

    private void initTestingView() {
        RadioGroup requestRadioGroup = findViewById(R.id.request_type_rg);
        requestRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.otplink_rb:
                    headlessRequestType = HeadlessRequestType.OTPLINK;
                    break;
                case R.id.sso_rb:
                    headlessRequestType = HeadlessRequestType.SSO;
                    break;
                case R.id.request_otp_rb:
                    headlessRequestType = HeadlessRequestType.REQUEST_OTP;
                    break;
            }
        });
        RadioGroup channelRadioGroup = findViewById(R.id.channel_type_rg);
        channelRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.whatsapp_rb:
                    channelType = OtplessChannelType.WHATSAPP;
                    break;
                case R.id.gmail_rb:
                    channelType = OtplessChannelType.GMAIL;
                    break;
            }
        });
        inputEditText = findViewById(R.id.input_text_layout);
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