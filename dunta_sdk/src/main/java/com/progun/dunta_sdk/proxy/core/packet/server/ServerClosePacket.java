package com.progun.dunta_sdk.proxy.core.packet.server;

import com.progun.dunta_sdk.proxy.core.ProxyClient;
import com.progun.dunta_sdk.utils.LogWrap;

/**
 * Описывает пакет PROXY_SRV_CLOSE
 */
//@SuppressWarnings("SpellCheckingInspection")
public final class ServerClosePacket extends ServerPacket {
    private static final String TAG = ServerClosePacket.class.getSimpleName();

    public ServerClosePacket(int id, ServerCommand commandType) {
        super(id, commandType);
        LogWrap.d(TAG, "ServerClosePacket() has called", id);
        debugPacket(id, commandType);
    }

    private void debugPacket(int id, ServerCommand commandType) {
        if (ProxyClient.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("<------ PROXY_SRV_CLOSE")
                    .append("\n\tchannelId=").append(id);
            debugInfo = sb;
        }
    }
}
