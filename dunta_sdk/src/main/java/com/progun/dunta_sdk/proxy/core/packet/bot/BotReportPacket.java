package com.progun.dunta_sdk.proxy.core.packet.bot;

import androidx.annotation.NonNull;

import com.progun.dunta_sdk.proxy.utils.ProtocolConstants;

public class BotReportPacket extends BotPacket {

    private final String reportMessage;
    public BotReportPacket(@NonNull String reportMessage) {
        super(BotCommandType.REPORT, ProtocolConstants.PROXY_CMD_MARKER);
        this.reportMessage = reportMessage;
    }

    @Override
    public byte[] toByteArray() {
        byte[] data = reportMessage.getBytes();
        byte[] payload = new byte[Sizes.CMD + Sizes.REPORT_LEN + data.length + Sizes.MARK];

        payload[0] = (byte) BotPacket.PROXY_BOT_REPORT;
        payload[1] = (byte) (BotPacket.PROXY_BOT_REPORT >> 8);

        payload[2] = (byte) data.length;
        payload[3] = (byte) (data.length >> 8);
        payload[4] = (byte) (data.length >> 16);
        payload[5] = (byte) (data.length >> 24);

        System.arraycopy(data, 0, payload, 6, data.length);

        payload[payload.length - 2] = (byte) mark;
        payload[payload.length - 1] = (byte) (mark >> 8);

        return payload;
    }

    @Override
    public String convertToString(boolean fullString) {
        if (!fullString) return super.convertToString();

        return super.convertToString() + System.lineSeparator() +
                "len=" + reportMessage.length() + System.lineSeparator() +
                "mark=" + mark + System.lineSeparator();
    }
}
