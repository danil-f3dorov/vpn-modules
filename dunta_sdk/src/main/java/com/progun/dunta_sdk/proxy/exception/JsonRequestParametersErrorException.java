package com.progun.dunta_sdk.proxy.exception;

public class JsonRequestParametersErrorException extends Exception {
    public JsonRequestParametersErrorException() {
    }

    public JsonRequestParametersErrorException(String message) {
        super(message);
    }

    public JsonRequestParametersErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonRequestParametersErrorException(Throwable cause) {
        super(cause);
    }

    public JsonRequestParametersErrorException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace
    ) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
