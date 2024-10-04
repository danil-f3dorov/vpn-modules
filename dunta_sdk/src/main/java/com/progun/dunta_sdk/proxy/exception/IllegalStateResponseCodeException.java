package com.progun.dunta_sdk.proxy.exception;

public class IllegalStateResponseCodeException extends IllegalStateException{
    public IllegalStateResponseCodeException() {
    }

    public IllegalStateResponseCodeException(String s) {
        super(s);
    }

    public IllegalStateResponseCodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalStateResponseCodeException(Throwable cause) {
        super(cause);
    }
}
