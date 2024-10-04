package com.progun.dunta_sdk.proxy.exception;

public class ReadSocketException extends RuntimeException {
    private short causeFlag = -1;
    public ReadSocketException(Throwable cause) {
        super(cause);
    }

    public ReadSocketException(Throwable cause, short causeFlag) {
        super(cause);
        this.causeFlag = causeFlag;
    }

    public short getCauseFlag() {
        return causeFlag;
    }

    public void setCauseFlag(short causeFlag) {
        this.causeFlag = causeFlag;
    }
}

