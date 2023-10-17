package com.otpless.otplesssample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.otpless.main.OtplessManager;
import com.otpless.main.OtplessView;

public class HomeActivity extends AppCompatActivity {

    private FrameLayout homeContainer;

    private OtplessView otplessView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        otplessView = OtplessManager.getInstance().getOtplessView(this);
        homeContainer = findViewById(R.id.home_container_fl);
        final Fragment fragment = HomeFirstFragment.newInstance("p1", "p2");
        getSupportFragmentManager()
                .beginTransaction()
                .add(homeContainer.getId(), fragment)
                .commit();

    }

    public void openSecondFragment() {
        final Fragment secondFragment = HomeSecondFragment.newInstance("p1", "p2");
        getSupportFragmentManager()
                .beginTransaction()
                .add(homeContainer.getId(), secondFragment)
                .commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (otplessView.verifyIntent(intent)) return;
    }

    @Override
    public void onBackPressed() {
        if (otplessView.onBackPressed()) return;
        super.onBackPressed();
    }
}