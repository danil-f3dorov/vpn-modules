package com.progun.dunta_sdk.proxy.core.packet.server;


import com.progun.dunta_sdk.proxy.core.ProxyClient;
import com.progun.dunta_sdk.utils.LogWrap;

/**
 * Описывает пакет PROXY_SRV_SHUTDOWN
 */
public final class ServerShutDownPacket extends ServerPacket {
    private static final String TAG = ServerShutDownPacket.class.getSimpleName();

    public ServerShutDownPacket(int id, ServerCommand commandType) {
        super(id, commandType);
        LogWrap.d(TAG, "ServerShutDownPacket() has called", id);
        if (ProxyClient.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("<------ PROXY_SRV_SHUTDOWN").append("\nid = ").append(id);
            debugInfo = sb;
        }
    }


}

