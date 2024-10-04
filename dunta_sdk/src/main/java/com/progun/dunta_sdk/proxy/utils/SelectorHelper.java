package com.progun.dunta_sdk.proxy.utils;

import com.progun.dunta_sdk.utils.LogWrap;

import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;

// Утилитарный класс для логирования состояния селектора и его каналов
public class SelectorHelper {
    private static final String TAG = SelectorHelper.class.getSimpleName();

    public static String opsToString(int ops) {
        String stringOps = "";
        switch (ops) {
            case (SelectionKey.OP_WRITE):
                stringOps = "OP_WRITE";
            case SelectionKey.OP_READ:
                stringOps = "OP_READ";
            case SelectionKey.OP_CONNECT:
                stringOps = "OP_CONNECT";
            case SelectionKey.OP_WRITE | SelectionKey.OP_READ:
                stringOps = "OP_WRITE | OP_READ";
            case 0: stringOps = "NULL";
            default: stringOps = "OTHER";
        };
        return stringOps;
    }

    public static String keyContent(SelectionKey key) {
        StringBuilder resultOps = new StringBuilder();
        if (key != null && key.isValid()){
            boolean resultEmpty = true;
            resultOps.append("interestSet=[");

            if ((key.interestOps() & SelectionKey.OP_CONNECT) != 0) {
                resultOps.append("OP_CONNECT/");
                resultEmpty = false;
            }
            if ((key.interestOps() & SelectionKey.OP_READ) != 0) {
                resultOps.append("OP_READ/");
                resultEmpty = false;
            }
            if ((key.interestOps() & SelectionKey.OP_WRITE) != 0) {
                resultOps.append("OP_WRITE");
                resultEmpty = false;
            }
            if (resultEmpty) resultOps.append("*");
            resultOps.append("]").append("readySet=[");

            resultEmpty = true;
            if ((key.readyOps() & SelectionKey.OP_CONNECT) != 0) {
                resultOps.append("OP_CONNECT/");
                resultEmpty = false;
            }
            if ((key.readyOps() & SelectionKey.OP_READ) != 0) {
                resultOps.append("OP_READ/");
                resultEmpty = false;
            }
            if ((key.readyOps() & SelectionKey.OP_WRITE) != 0) {
                resultOps.append("OP_WRITE");
                resultEmpty = false;
            }
            if (resultEmpty) resultOps.append("*");
            resultOps.append("]");
        }
        /* k.readyOps() & OP_READ != 0*/
        return resultOps.toString();
    }


    /*
    * Утилитарный класс для логирования состояния каналов силектора
    * */
    public synchronized static void setInterestOps(
            SelectionKey key,
            int ops,
            String comment
    ) throws CancelledKeyException
    {
        boolean keyIsNull = true;
        boolean ketIsValid = false;
        if (key != null) {
            keyIsNull = false;
            if (key.isValid()) {
                ketIsValid = true;
                key.interestOps(ops);
            }
        }
        LogWrap.v(TAG, "setInterestOps(), key IsNull=" + keyIsNull + ", isValid=" + ketIsValid + ", newOps=" + opsToString(ops));
    }
}
