package com.otpless.otplesssample;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.otpless.dto.OtplessResponse;
import com.otpless.views.WhatsappLoginButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WhatsappLoginButton button = (WhatsappLoginButton) findViewById(R.id.whatsapp_login);
        button.setResultCallback(this::onOtplessResult);
    }

    private void onOtplessResult(@Nullable OtplessResponse userDetail) {
        if (userDetail == null) return;
        String message = userDetail.toString();
        message = userDetail.getWaId() + "\n" +message;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.d("MainActivity", message);
    }


}