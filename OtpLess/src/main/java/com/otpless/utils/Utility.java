package com.otpless.utils;

import android.content.pm.PackageManager;

public class Utility {

    public static boolean isAppInstalled(final PackageManager packageManager, final String packageName) {
        try {
            return packageManager.getApplicationInfo(packageName, 0).enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
