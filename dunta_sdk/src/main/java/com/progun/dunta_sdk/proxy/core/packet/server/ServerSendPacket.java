package com.progun.dunta_sdk.proxy.core.packet.server;

import com.progun.dunta_sdk.proxy.core.ProxyClient;
import com.progun.dunta_sdk.utils.LogWrap;

/**
 * Описывает пакет PROXY_SRV_SEND
 */
//@SuppressWarnings("SpellCheckingInspection")
public final class ServerSendPacket extends ServerPacket {
    private static final String TAG = ServerSendPacket.class.getSimpleName();

    public ServerSendPacket(int id, ServerCommand commandType) {
        super(id, commandType);
        LogWrap.d(TAG, "Create server packet: cmd=" + commandType, id);

        if (ProxyClient.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("<------ PROXY_SRV_SENT")
                    .append("\n\tid = ").append(id)
                    .append("marker = ").append(marker);

            debugInfo = sb;
        }
    }
}
