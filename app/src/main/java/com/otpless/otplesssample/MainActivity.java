package com.otpless.otplesssample;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.otpless.dto.OtplessResponse;
import com.otpless.fedo.OtplessWebAuthnManager;
import com.otpless.fedo.models.WebAuthnRegistrationInitRequest;
import com.otpless.main.OtplessManager;
import com.otpless.main.OtplessView;
import com.otpless.network.ApiCallback;


public class MainActivity extends AppCompatActivity {

    OtplessView otplessView;

    private EditText fedoUsernameEt;

    private OtplessWebAuthnManager manager;

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
        otplessView.verifyIntent(getIntent());
        findViewById(R.id.register_fedo_btn).setOnClickListener(v -> registerWithFedo());
        findViewById(R.id.fedo_username_et).setOnClickListener(v -> authenticateWithFedo());
        fedoUsernameEt = findViewById(R.id.fedo_username_et);

        manager = new OtplessWebAuthnManager(this);
    }

    private void registerWithFedo() {
        final WebAuthnRegistrationInitRequest request = new WebAuthnRegistrationInitRequest(
                "ashwin", "ashwin", "917042507646", "otpless.com", "otpless"
        );
        manager.initRegistration(request, callback);
    }

    private final ApiCallback<PendingIntent> callback = new ApiCallback<PendingIntent>() {
        @Override
        public void onSuccess(PendingIntent data) {

        }

        @Override
        public void onError(Throwable exception) {
            Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    private void authenticateWithFedo() {

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        otplessView.verifyIntent(intent);
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
        manager.onActivityResult(requestCode, resultCode, data);
    }
}