package com.progun.dunta_sdk.proxy.exception;

import androidx.annotation.NonNull;

public class SuchUserDataNotFound extends IllegalStateException{
    public SuchUserDataNotFound(@NonNull String s) {
        super(s);
    }

    public SuchUserDataNotFound(@NonNull String message, Throwable cause) {
        super(message, cause);
    }

    public SuchUserDataNotFound(@NonNull Throwable cause) {
        super(cause);
    }
}
