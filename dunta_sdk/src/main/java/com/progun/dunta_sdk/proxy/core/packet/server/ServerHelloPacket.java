package com.progun.dunta_sdk.proxy.core.packet.server;

import com.progun.dunta_sdk.proxy.core.ProxyClient;

import java.nio.ByteBuffer;

public class ServerHelloPacket extends ServerPacket {

    private static String TAG = ServerHelloPacket.class.getSimpleName();

    private int cryptKey;
    private final long botId;
    private int bufferSize; // Новое поле для хранения размера буфера

    public long getBotId() {
        return botId;
    }

    public int getCryptKey() {
        return cryptKey;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public ServerHelloPacket(ServerCommand commandType, ByteBuffer buffer) {
        super(commandType);
        this.botId = buffer.getLong();
        buffer.getInt();

        if (ProxyClient.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("<------ PROXY_SRV_HELLO")
                    .append("\n\tbotId = ").append(botId)
                    .append("\n\tbufferSize = ").append(bufferSize)
                    .append("\n\tmark = ").append(marker);
            debugInfo = sb;
        }
    }

}
