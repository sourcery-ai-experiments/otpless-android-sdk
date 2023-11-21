package com.otpless.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OtpReaderReceiver extends BroadcastReceiver {

    static IntentFilter newIntentFilter() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION);
        return filter;
    }

    private static final String OTP_PATTERN = "\\d{4,6}";

    @NonNull
    private final OtpResultListener resultListener;

    public OtpReaderReceiver(@NonNull final OtpResultListener listener) {
        this.resultListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        if (!SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) return;
        if (intent.getExtras() == null) {
            this.resultListener.onOtpReadResult(new OtpResult(false, null, "Invalid Argument: In intent extra data not provided.").addStatusCode(OtpResult.STATUS_ERROR));
            return;
        }
        final Object statusObj = intent.getExtras().get(SmsRetriever.EXTRA_STATUS);
        if (!(statusObj instanceof Status)) {
            this.resultListener.onOtpReadResult(new OtpResult(false, null, "Invalid Argument: Status data is not provided.").addStatusCode(OtpResult.STATUS_ERROR));
            return;
        }
        final Status status = (Status) statusObj;
        switch (status.getStatusCode()) {
            case CommonStatusCodes.SUCCESS:
                final Object otpMessageObj = intent.getExtras().get(SmsRetriever.EXTRA_SMS_MESSAGE);
                if (otpMessageObj instanceof String) {
                    this.resultListener.onOtpReadResult(parseOtpMessage((String) otpMessageObj));
                } else {
                    this.resultListener.onOtpReadResult(new OtpResult(false, null, "Invalid Argument: Otp message string is not found.").addStatusCode(OtpResult.STATUS_TIMEOUT));
                }
                break;
            case CommonStatusCodes.TIMEOUT:
                this.resultListener.onOtpReadResult(new OtpResult(false, null, "Timeout: Sms SmsRetriever sent the timeout error.").addStatusCode(OtpResult.STATUS_TIMEOUT));
                break;
            default:
                this.resultListener.onOtpReadResult(new OtpResult(false, null, "Unknown Error: Something went wrong.").addStatusCode(OtpResult.STATUS_ERROR));
        }
    }

    private OtpResult parseOtpMessage(@NonNull final String otpMessage) {
        final Pattern pattern = Pattern.compile(OTP_PATTERN);
        final Matcher matcher = pattern.matcher(otpMessage);
        if (matcher.find()) {
            final String otp = matcher.group(0);
            return new OtpResult(true, otp, null);
        } else {
            return new OtpResult(false, null, "Invalid Sms: No otp string found in sms.");
        }
    }
}
