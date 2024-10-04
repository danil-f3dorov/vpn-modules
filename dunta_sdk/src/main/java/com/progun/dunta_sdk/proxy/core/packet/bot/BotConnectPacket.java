package com.progun.dunta_sdk.proxy.core.packet.bot;

import com.progun.dunta_sdk.proxy.utils.ProtocolConstants;

public class BotConnectPacket extends BotPacket {
    private final Status status;
    private final int channelId;
    public BotConnectPacket(int channelId, Status status) {
        super(BotCommandType.CONNECT, ProtocolConstants.PROXY_CMD_MARKER);

        this.status = status;
        this.channelId = channelId;
    }

    @Override
    public byte[] toByteArray() {
        return new byte[]{
                (byte) BotPacket.PROXY_BOT_CONNECT,
                (byte) (BotPacket.PROXY_BOT_CONNECT >> 8),

                (byte) channelId,
                (byte) (channelId >> 8),
                (byte) (channelId >> 16),
                (byte) (channelId >> 24),

                (byte) status.ordinal(),
                (byte) (status.ordinal() >> 8),

                (byte) mark,
                (byte) (mark >> 8)
        };
    }

    @Override
    public String convertToString(boolean fullString) {
        if (!fullString) return super.convertToString();

        return super.convertToString() + System.lineSeparator() +
                "id=" + channelId + System.lineSeparator() +
                "status=" + status.name() + System.lineSeparator() +
                "mark=" + mark + System.lineSeparator();
    }
}
