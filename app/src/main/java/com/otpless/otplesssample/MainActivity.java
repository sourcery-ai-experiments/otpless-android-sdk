package com.otpless.otplesssample;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.otpless.views.OtplessManager;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        // copy this code in onCreate of your Login Activity
        OtplessManager.getInstance().start(this, data -> {
            if (data.getData() == null) {
                Log.e("OTP-less", data.getErrorMessage());
            } else {
                final JSONObject json = data.getData();
                final String token = json.optString("token");
                if (!token.isEmpty()) {
                    Log.d("OTP-less", String.format("token: %s", token));
                    // todo pass this token to backend to fetch user detail
                }
            }
        });
    }

    private void initView() {
        final EditText editText = findViewById(R.id.custom_url_et);
        final Button button = findViewById(R.id.save_and_start_btn);
        final SharedPreferences preferences = getSharedPreferences("otpless_mobile_sdk", Context.MODE_PRIVATE);
        final String url = preferences.getString(
                "custom_temp_url", ""
        );
        if (!url.isEmpty()) {
            editText.setText(url);
        }
        button.setOnClickListener(v -> {
            final String newUrl = editText.getText().toString();
            if (newUrl.isEmpty()) {
                Toast.makeText(this, "Empty url", Toast.LENGTH_SHORT).show();
                return;
            }
            final SharedPreferences.Editor editor = preferences.edit();
            editor.putString("custom_temp_url", newUrl);
            editor.apply();
            OtplessManager.getInstance().start(data -> {
                if (data.getData() == null) {
                    Log.e("OTP-less", data.getErrorMessage());
                } else {
                    final JSONObject json = data.getData();
                    final String token = json.optString("token");
                    if (!token.isEmpty()) {
                        Log.d("OTP-less", String.format("token: %s", token));
                        // todo pass this token to backend to fetch user detail
                    }
                }
            });
        });
    }
}