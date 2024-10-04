package com.progun.dunta_sdk.proxy.exception;

public class PlayMarkerReferrerException extends RuntimeException {
    public PlayMarkerReferrerException(Throwable e) {
        super(e);
    }

    public PlayMarkerReferrerException(String msg, Throwable e) {
        super(msg, e);
    }

    public PlayMarkerReferrerException(String msg) {
        super(msg);
    }
}
