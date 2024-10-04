package com.progun.dunta_sdk.proxy.exception;

/**
 * Throws when client can't parse node in responded JSON from server.
 * */
public class ParseJSONNodeException extends Exception {
    public ParseJSONNodeException() {
    }

    public ParseJSONNodeException(String message) {
        super(message);
    }

    public ParseJSONNodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseJSONNodeException(Throwable cause) {
        super(cause);
    }

    public ParseJSONNodeException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace
    ) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
