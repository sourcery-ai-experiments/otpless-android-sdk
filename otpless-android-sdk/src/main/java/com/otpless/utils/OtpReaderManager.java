package com.otpless.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.Task;

public class OtpReaderManager {

    private OtpReaderReceiver otpReader = null;
    private Context callerContext;
    private OtpResultListener resultListener;

    @SuppressLint("StaticFieldLeak")
    private static OtpReaderManager INSTANCE = null;

    public static OtpReaderManager getInstance() {
        if (INSTANCE != null) return INSTANCE;
        synchronized (OtpReaderManager.class) {
            if (INSTANCE != null) return INSTANCE;
            INSTANCE = new OtpReaderManager();
        }
        return INSTANCE;
    }

    private OtpReaderManager() {
    }

    private void registerReceiver(final Context context) {
        if (otpReader != null) return;
        otpReader = new OtpReaderReceiver(this::onOtpReadResult);
        try {
            context.registerReceiver(otpReader, OtpReaderReceiver.newIntentFilter(), SmsRetriever.SEND_PERMISSION, null);
        } catch (Exception ignore) {
            otpReader = null;
        }
    }

    private void unregisterReceiver(final Context context) {
        if (otpReader == null) return;
        try {
            context.unregisterReceiver(otpReader);
        } catch (Exception ignore) {
        } finally {
            otpReader = null;
        }
    }

    private void onOtpReadResult(OtpResult otpResult) {
        if (this.resultListener != null) {
            this.resultListener.onOtpReadResult(otpResult);
        }
        stopOtpReader();
    }

    public void startOtpReader(@NonNull final Context context, @NonNull final OtpResultListener listener) {
        this.callerContext = context;
        this.resultListener = listener;
        registerReceiver(context);
        // staring sms retriever client
        final SmsRetrieverClient client = SmsRetriever.getClient(context);
        final Task<Void> task = client.startSmsRetriever();
        task.addOnFailureListener(e -> {
            String errorMessage = e.getMessage();
            if (errorMessage == null) {
                errorMessage = "Runtime Error: Failed to initialized sms retriever";
                if (this.resultListener != null) {
                    this.resultListener.onOtpReadResult(
                            new OtpResult(false, null, errorMessage)
                                    .addStatusCode(OtpResult.STATUS_ERROR)
                    );
                }
            }
            stopOtpReader();
        });
    }

    public void stopOtpReader() {
        if (this.callerContext != null) {
            unregisterReceiver(this.callerContext);
            this.callerContext = null;
        }
        this.resultListener = null;
    }
}
