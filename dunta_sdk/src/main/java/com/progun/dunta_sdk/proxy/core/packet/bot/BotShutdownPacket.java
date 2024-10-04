package com.progun.dunta_sdk.proxy.core.packet.bot;

import com.progun.dunta_sdk.proxy.utils.ProtocolConstants;

public class BotShutdownPacket extends BotPacket {

    private final int channelId;
    public BotShutdownPacket(int channelId) {
        super(BotCommandType.SHUTDOWN, ProtocolConstants.PROXY_CMD_MARKER);

        this.channelId = channelId;

    }

    @Override
    public byte[] toByteArray() {
        return new byte[]{
                (byte) BotPacket.PROXY_BOT_SHUTDOWN,
                (byte) (BotPacket.PROXY_BOT_SHUTDOWN >> 8),

                (byte) channelId,
                (byte) (channelId >> 8),
                (byte) (channelId >> 16),
                (byte) (channelId >> 24),

                (byte) mark,
                (byte) (mark >> 8)
        };
    }

    @Override
    public String convertToString(boolean fullString) {
        if (!fullString) return super.convertToString();

        return super.convertToString() + System.lineSeparator() +
                "id=" + channelId + System.lineSeparator() +
                "mark=" + mark + System.lineSeparator();
    }
}
