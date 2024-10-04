package com.progun.dunta_sdk.proxy.exception;

public class ServerResetSocketException extends ReadSocketException {

    public ServerResetSocketException(Throwable cause) {
        super(cause);
    }

    public ServerResetSocketException(Throwable cause, short causeFlag) {
        super(cause, causeFlag);
    }
}
