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

    public static String Tag = "OTPless-main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OtplessManager.registerCallback(this, this::onOtplessResult);
        OtplessWhatsappButton button = (OtplessWhatsappButton) findViewById(R.id.whatsapp_login);
        Log.d(Tag, "onCreate in otpless");
        button.setOnClickListener(v -> {
            OtplessManager.openOtpless(this, Uri.parse("https://anubhav.authlink.me"));
        });
        if (savedInstanceState != null) {
            Log.d(Tag, "verifying in onCreate after recreation");
            OtplessManager.verify(this, getIntent(), null);
        } else {
            Log.d(Tag, "verifying in onCreate for every case");
            OtplessManager.verify(this, getIntent(), null);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(Tag, "onNewIntent in otpless");
        if (intent != null) {
            final Uri uri = intent.getData();
            Log.d(Tag, "Data uri in otpless on new intent is "+ uri.toString());
        } else {
            Log.d(Tag, "Data uri is not found !!!! ");
        }
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
        if (userDetail == null) {
            Log.d(Tag, "Otpless resultcallback no user data");
            return;
        }
        String message = userDetail.toString();
        message = userDetail.getWaId() + "\n" + message;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.d(Tag, message);
    }
}