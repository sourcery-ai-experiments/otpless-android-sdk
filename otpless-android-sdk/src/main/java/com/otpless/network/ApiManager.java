package com.otpless.network;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.otpless.BuildConfig;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class ApiManager {

    private static ApiManager sInstance;
    private final Handler mUiHandler;
    private final OkHttpClient mOkHttpClient;

    public static ApiManager getInstance() {
        if (sInstance == null) {
            synchronized (ApiManager.class) {
                if (sInstance != null) {
                    return sInstance;
                }
                sInstance = new ApiManager();
            }
        }
        return sInstance;
    }

    private ApiManager() {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(40, TimeUnit.SECONDS)
                .connectTimeout(40, TimeUnit.SECONDS);
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }
        mOkHttpClient = builder.build();
        mUiHandler = new Handler(Looper.getMainLooper());
    }

    public void pushEvents(final JSONObject eventParam, final ApiCallback<JSONObject> callback) {
        final Uri.Builder eventUrlBuilder = Uri.parse("https://mtkikwb8yc.execute-api.ap-south-1.amazonaws.com/prod/appevent").buildUpon();
        Request.Builder requestBuilder = new Request.Builder();
        if (eventParam != null) {
            for (final Iterator<String> iter = eventParam.keys(); iter.hasNext(); ) {
                final String key = iter.next();
                final String value = eventParam.optString(key);
                if (key.length() == 0) continue;
                eventUrlBuilder.appendQueryParameter(key, value);
            }
        }
        requestBuilder.url(eventUrlBuilder.build().toString());
        requestBuilder.method("GET", null);
        final Call call = mOkHttpClient.newCall(requestBuilder.build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mUiHandler.post(() -> callback.onError(e));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                final int responseCode = response.code();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED ||
                        responseCode == HttpURLConnection.HTTP_ACCEPTED) {
                    // this api gives 0 len response
                    mUiHandler.post(() -> callback.onSuccess(new JSONObject()));
                } else {
                    final Exception ex = new Exception("" + responseCode + " response code");
                    mUiHandler.post(() -> callback.onError(ex));
                }
            }
        });
    }
}
