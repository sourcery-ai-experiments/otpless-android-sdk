package com.otpless.otplesssample;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.otpless.dto.OtplessRequest;
import com.otpless.dto.OtplessResponse;
import com.otpless.fedo.OtplessWebAuthnManager;
import com.otpless.fedo.models.WebAuthnLoginInitRequest;
import com.otpless.fedo.models.WebAuthnRegistrationInitRequest;
import com.otpless.main.OtplessManager;
import com.otpless.main.OtplessView;
import com.otpless.network.ApiCallback;
import com.otpless.utils.Utility;

import org.json.JSONObject;


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
        final OtplessRequest request = new OtplessRequest()
                .setUxmode("anf")
                        .addExtras("loaderAlpha", "0.1");
        otplessView.setCallback(this::onOtplessCallback, null, false);
        findViewById(R.id.otpless_btn).setOnClickListener(v -> {
            otplessView.startOtpless(request, this::onOtplessCallback);
        });
        findViewById(R.id.sign_in_complete).setOnClickListener(v -> {
            otplessView.onSignInCompleted();
        });
        otplessView.verifyIntent(getIntent());
        findViewById(R.id.register_fedo_btn).setOnClickListener(v -> registerWithFedo());
        findViewById(R.id.authenticate_with_fedo_btn).setOnClickListener(v -> authenticateWithFedo());
        fedoUsernameEt = findViewById(R.id.fedo_username_et);

        manager = new OtplessWebAuthnManager(this);
    }

    private void registerWithFedo() {
        final WebAuthnRegistrationInitRequest request = new WebAuthnRegistrationInitRequest(
                "ashwin", "ashwin", "917042507646", "otpless.com", "otpless"
        );
        manager.initRegistration(request, callback);
    }

    private final ApiCallback<JSONObject> callback = new ApiCallback<JSONObject>() {
        @Override
        public void onSuccess(JSONObject data) {
            Toast.makeText(MainActivity.this, data.toString(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onError(Throwable exception) {
            Utility.debugLog(exception);
            Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    private void authenticateWithFedo() {
        final WebAuthnLoginInitRequest request = new WebAuthnLoginInitRequest("917042507646", "otpless.com");
        manager.initLogin(request, new ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {
                Toast.makeText(MainActivity.this, data.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(Throwable exception) {
                Utility.debugLog(exception);
                Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
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