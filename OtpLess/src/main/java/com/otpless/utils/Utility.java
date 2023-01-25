package com.otpless.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import org.json.JSONObject;

public class Utility {

    public static boolean isAppInstalled(final PackageManager packageManager, final String packageName) {
        try {
            return packageManager.getApplicationInfo(packageName, 0).enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static String parseWaNumber(final JSONObject jsonObject) {
        JSONObject user = jsonObject.optJSONObject("user");
        if (user != null) {
            return user.optString("waNumber");
        }
        return null;
    }

    public static boolean isNotEmpty(final String str) {
        return str != null && str.length() > 0;
    }

    public static void deleteWaId(final Context context) {
        SharedPreferences sp = context.getSharedPreferences("otpless_storage_manager", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("otpless_waid");
        editor.apply();
    }
}
