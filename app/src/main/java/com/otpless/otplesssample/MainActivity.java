package com.otpless.otplesssample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.otpless.dto.OtplessResponse;
import com.otpless.main.OtplessEventCallback;
import com.otpless.main.OtplessEventCode;
import com.otpless.main.OtplessEventData;
import com.otpless.main.OtplessManager;
import com.otpless.main.OtplessView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    OtplessView otplessView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // copy this code in onCreate of your Login Activity

        final JSONObject extra = new JSONObject();
        try {
            extra.put("method", "get");
            final JSONObject params = new JSONObject();
            params.put("primaryColor", "#030ffc");
            params.put("closeButtonColor", "#03fc24");
            params.put("loaderColor", "#fc03ec");
            params.put("textColor", "#03fcf4");
            params.put("loadingText", "I am Loading");
            params.put("loaderAlpha", "0.5");
            extra.put("params", params);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        otplessView = OtplessManager.getInstance().getOtplessView(this);
//        otplessView.showOtplessLoginPage(this::onOtplessCallback);
        otplessView.setCallback(this::onOtplessCallback, extra, true);
        findViewById(R.id.otpless_btn).setOnClickListener(v -> {
            otplessView.showOtplessLoginPage(extra, this::onOtplessCallback);
        });
        final HashMap<String, String> noInternet = new HashMap<>();
        noInternet.put("backgroundColor", "#00FF00");
        noInternet.put("textColor", "#FF00FF");
        otplessView.setNoInternetViewConfig(noInternet);

        findViewById(R.id.sign_in_complete).setOnClickListener(v -> {
            otplessView.onSignInCompleted();
        });
        otplessView.verifyIntent(getIntent());
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
        otplessView.onActivityResult(requestCode, resultCode, data);
    }
}