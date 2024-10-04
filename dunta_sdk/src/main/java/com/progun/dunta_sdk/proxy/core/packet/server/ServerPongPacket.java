package com.progun.dunta_sdk.proxy.core.packet.server;


import com.progun.dunta_sdk.proxy.core.ProxyClient;

/**
 * Описывает пакет PROXY_SRV_PONG.
 */

public final class ServerPongPacket extends ServerPacket {
    private static final String TAG = ServerPongPacket.class.getSimpleName();

    public ServerPongPacket(ServerCommand type) {
        super(type);
        if (ProxyClient.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("<------ PROXY_SRV_PONG.");
            debugInfo = sb;
        }
    }
}
