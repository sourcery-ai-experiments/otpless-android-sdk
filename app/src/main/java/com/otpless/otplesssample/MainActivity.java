package com.otpless.otplesssample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.otpless.dto.OtplessResponse;
import com.otpless.utils.Utility;
import com.otpless.views.OtplessManager;
import com.otpless.views.OtplessWhatsappButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OtplessManager.registerCallback(this, this::onOtplessResult);
        OtplessWhatsappButton button = (OtplessWhatsappButton) findViewById(R.id.whatsapp_login);
        button.setOnClickListener(v -> {
            OtplessManager.openOtpless(this, Uri.parse("https://anubhav.authlink.me"));
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        OtplessManager.verify(this, intent, null);
    }

    @Override
    public void onBackPressed() {
        OtplessManager.onBackPressed(this);
        super.onBackPressed();
    }

    private void afterSessionId() {
        final Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void onOtplessResult(@Nullable OtplessResponse userDetail) {
        if (userDetail == null) return;
        String message = userDetail.toString();
        message = userDetail.getWaId() + "\n" + message;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.d("MainActivity", message);
    }
}