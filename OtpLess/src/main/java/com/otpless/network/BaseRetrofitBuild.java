package com.otpless.network;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BaseRetrofitBuild {

    private volatile static BaseRetrofitBuild baseRetrofitBuildInstance;

    private static volatile Retrofit.Builder retrofitBuilder;

    private BaseRetrofitBuild(){
    }
    /**
     * creates a new instance of retrofit builder
     * @return
     */
    public static BaseRetrofitBuild getInstance() {
        if (baseRetrofitBuildInstance == null) {
            synchronized (BaseRetrofitBuild.class) {
                baseRetrofitBuildInstance = new BaseRetrofitBuild();
            }
        }
        return baseRetrofitBuildInstance;
    }

    private static Retrofit.Builder getRetrofitBuilder() {
        if (retrofitBuilder == null) {
            synchronized (Retrofit.Builder.class) {
                retrofitBuilder = new Retrofit.Builder()
                        .addConverterFactory(GsonConverterFactory.create(new Gson()));
            }
        }
        return retrofitBuilder;
    }

    /**
     * gets instance and sets the provided base url
     *
     * @param baseUrl
     * @return
     */
    public Retrofit build(String baseUrl) {
        return getRetrofitBuilder()
                .baseUrl(baseUrl)
                .client(getClient())
                .build();
    }

    private OkHttpClient getClient() {
        return new OkHttpClient
                .Builder()
                .build();
    }

}
