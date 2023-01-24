package com.otpless.network;

import android.app.Application;
import android.content.Context;

public class AppApplication extends Application {

    private static AppApplication sInstance;

    private static Context mContext;

    public static synchronized AppApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppApplication.mContext = getApplicationContext();
        sInstance = this;
    }

    public static Context getContext() {
        return mContext;
    }

}
