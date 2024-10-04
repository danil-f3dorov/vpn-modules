package com.progun.dunta_sdk.proxy.exception;

public class JsonServerUnexpectedBehaviorException extends RuntimeException{
    public JsonServerUnexpectedBehaviorException() {
    }

    public JsonServerUnexpectedBehaviorException(String message) {
        super(message);
    }

    public JsonServerUnexpectedBehaviorException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonServerUnexpectedBehaviorException(Throwable cause) {
        super(cause);
    }

    public JsonServerUnexpectedBehaviorException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace
    ) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
