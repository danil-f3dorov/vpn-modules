package com.progun.dunta_sdk.proxy.core.packet.server;

import com.progun.dunta_sdk.proxy.core.packet.Packet;
import com.progun.dunta_sdk.proxy.utils.ProtocolConstants;

/**
 * Абстрактный класс, описвающий пакет данных от сервера.
 * Все его наследники - просто data классы.
 */
public abstract class ServerPacket extends Packet {
    public int marker;
    private final int channelId;
    private final ServerCommand cmdCommandType;

    protected ServerPacket(int channelId, ServerCommand cmdCommandType) {
        this.channelId = channelId;
        this.cmdCommandType = cmdCommandType;
    }

    protected ServerPacket(ServerCommand cmdCommandType) {
        this.channelId = 0;
        this.cmdCommandType = cmdCommandType;
    }

    public int getChannelId() {
        return channelId;
    }

    public ServerCommand getCommand() {
        return cmdCommandType;
    }

    protected boolean isValid() {
        return marker == ProtocolConstants.PROXY_CMD_MARKER;
    }

    /* Server commands */
    public static final int PROXY_SRV_CONNECT = 0x01; // Connect request to external host
    public static final int PROXY_SRV_SENT = 0x02;
            // Server is ready for receiving (accepted last data)
    public static final int PROXY_SRV_RECV = 0x03; // Server send data (bot receives)
    public static final int PROXY_SRV_SHUTDOWN = 0x04; // Server send FIN (recv 0 on proxy)
    public static final int PROXY_SRV_CLOSE = 0x05; // Close channel
    public static final int PROXY_SRV_PONG = 0x06; // Server response for "ping" from bot
    public static final int PROXY_SRV_HELLO = 0x0010; // Server response for bot hello
}
