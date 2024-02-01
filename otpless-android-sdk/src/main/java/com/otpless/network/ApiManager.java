package com.otpless.network;


import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.otpless.BuildConfig;
import com.otpless.fedo.WebAuthnApi;
import com.otpless.fedo.WebAuthnBaseResponse;
import com.otpless.fedo.models.WebAuthnLoginCompleteRequest;
import com.otpless.fedo.models.WebAuthnLoginInitData;
import com.otpless.fedo.models.WebAuthnLoginInitRequest;
import com.otpless.fedo.models.WebAuthnRegistrationCompleteData;
import com.otpless.fedo.models.WebAuthnRegistrationCompleteRequest;
import com.otpless.fedo.models.WebAuthnRegistrationInitData;
import com.otpless.fedo.models.WebAuthnRegistrationInitRequest;

import org.json.JSONObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
//import ret

public class ApiManager {

    private static ApiManager sInstance;
    private final Retrofit.Builder mRetrofitBuilder;

    private final EventApi eventApi;
    private final WebAuthnApi webAuthnApi;

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
        final GsonConverterFactory factory = GsonConverterFactory.create(new Gson());
        mRetrofitBuilder = new Retrofit.Builder();
        mRetrofitBuilder.addConverterFactory(new NullOnEmptyConverterFactory());
        mRetrofitBuilder.addConverterFactory(factory);

        // setting push event service
        eventApi = build("https://mtkikwb8yc.execute-api.ap-south-1.amazonaws.com/")
                .create(EventApi.class);
        // setting webauthn api service
        webAuthnApi = build("https://webauthn.otpless.app/v1/")
                .create(WebAuthnApi.class);
    }

    private Retrofit build(final String baseUrl) {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(40, TimeUnit.SECONDS)
                .connectTimeout(40, TimeUnit.SECONDS);
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }
        return mRetrofitBuilder.baseUrl(baseUrl)
                .client(builder.build())
                .build();
    }

    public void pushEvents(final JSONObject eventParam, final ApiCallback<JSONObject> callback) {
        final HashMap<String, String> map = new HashMap<>();
        if (eventParam != null) {
            for (final Iterator<String> iter = eventParam.keys(); iter.hasNext(); ) {
                final String key = iter.next();
                final String value = eventParam.optString(key);
                if (key.length() == 0) continue;
                map.put(key, value);
            }
        }
        eventApi.pushEvents(map).enqueue(new Callback<JSONObject>() {
            @Override
            public void onResponse(@NonNull Call<JSONObject> call, @NonNull Response<JSONObject> response) {
                callback.onSuccess(new JSONObject());
            }

            @Override
            public void onFailure(@NonNull Call<JSONObject> call, @NonNull Throwable t) {
                callback.onError(t);
            }
        });
    }

    public void initWebAuthnRegistration(final WebAuthnRegistrationInitRequest request,
                                         final ApiCallback<WebAuthnBaseResponse<WebAuthnRegistrationInitData>> callback) {
        webAuthnApi.initRegistration(request)
                .enqueue(new WebAuthnApiCallback<>(callback));
    }

    public void completeWebAuthnRegistration(final WebAuthnRegistrationCompleteRequest request,
                                             final ApiCallback<WebAuthnRegistrationCompleteData> callback) {
        webAuthnApi.completeRegistration(request)
                .enqueue(new WebAuthnApiCallback<>(callback));
    }

    public void initWebAuthnLogin(final WebAuthnLoginInitRequest request,
                                  final ApiCallback<WebAuthnBaseResponse<WebAuthnLoginInitData>> callback) {
        webAuthnApi.initLogin(request)
                .enqueue(new WebAuthnApiCallback<>(callback));
    }

    public void completeWebAuthnLogin(final WebAuthnBaseResponse<WebAuthnLoginCompleteRequest> request,
                                      final ApiCallback<WebAuthnRegistrationCompleteData> callback) {
        webAuthnApi.completeLogin(request)
                .enqueue(new WebAuthnApiCallback<>(callback));
    }
}

class NullOnEmptyConverterFactory extends Converter.Factory {

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        final Converter<ResponseBody, ?> delegate = retrofit.nextResponseBodyConverter(this, type, annotations);
        return (Converter<ResponseBody, Object>) body -> {
            if (body.contentLength() == 0) return null;
            return delegate.convert(body);
        };
    }
}

class WebAuthnApiCallback<T> implements Callback<T> {

    private final ApiCallback<T> callback;

    WebAuthnApiCallback(final ApiCallback<T> callback) {
        this.callback = callback;
    }

    @Override
    public void onResponse(@NonNull final Call<T> call, @NonNull final Response<T> response) {
        if (response.code() >= HttpsURLConnection.HTTP_OK && response.code() <= HttpsURLConnection.HTTP_ACCEPTED) {
            callback.onSuccess(response.body());
        } else {
            callback.onError(new Exception("response code do not match"));
        }
    }

    @Override
    public void onFailure(@NonNull final Call<T> call, @NonNull final Throwable t) {
        callback.onError(t);
    }
}
