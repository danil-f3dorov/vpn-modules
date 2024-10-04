package com.progun.dunta_sdk.proxy.exception;

public class ConnectionResetByPeer extends ReadSocketException {

    public ConnectionResetByPeer(Throwable cause) {
        super(cause);
//        if (cause instanceof ReadSocketException exc) {
//            if (exc.getCauseFlag() != -1) {
//                exc.setCauseFlag(exc.getCauseFlag());
//                super(exc);
//            }
//        } else
//            super(cause);
    }

    public ConnectionResetByPeer(Throwable cause, short causeFlag) {
        super(cause, causeFlag);
    }
}
