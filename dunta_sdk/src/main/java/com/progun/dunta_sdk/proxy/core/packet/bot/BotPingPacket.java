package com.progun.dunta_sdk.proxy.core.packet.bot;

import com.progun.dunta_sdk.proxy.utils.ProtocolConstants;

public class BotPingPacket extends BotPacket {
    public BotPingPacket() {
        super(BotCommandType.PING, (short) ProtocolConstants.PROXY_CMD_MARKER);
    }

    @Override
    public byte[] toByteArray() {
        return new byte[]{
                (byte) BotPacket.PROXY_BOT_PING,
                (byte) (BotPacket.PROXY_BOT_PING >> 8),
                (byte) mark,
                (byte) (mark >> 8)
        };
    }

    @Override
    public String convertToString(boolean fullString) {
        return super.convertToString() + System.lineSeparator() +
                "\tmark=" + mark + System.lineSeparator();
    }
}
