package com.otpless.views;

import android.graphics.Color;

import androidx.annotation.NonNull;

class ColorUtils {
    static void parseColor(String hexColor, @NonNull final OnColorParseCallback callback) {
        if (hexColor == null || hexColor.isEmpty()) return;
        try {
            if (hexColor.charAt(0) != '#') {
                // add the # before color code
                hexColor = "#" + hexColor;
            }
            final int color = Color.parseColor(hexColor);
            callback.onColorParsed(color);
        } catch (Exception ignore) {
        }
    }
}

@FunctionalInterface
interface OnColorParseCallback {
    void onColorParsed(final int colorCode);
}
