package com.otpless.network;

import androidx.annotation.NonNull;

public interface ApiCallback<T> {
    void onSuccess(@NonNull final T data);

    void onError(@NonNull final Exception exception);
}
