package com.progun.dunta_sdk.proxy.core.packet.bot;

import androidx.annotation.NonNull;

import com.progun.dunta_sdk.proxy.utils.ProtocolConstants;

public class BotRecvPacket extends BotPacket {

    private final byte[] recvData;
    private final int channelId;

    public byte[] getRecvData() {
        return recvData;
    }

    public BotRecvPacket(int channelId, @NonNull byte[] data) {
        super(BotCommandType.RECV, ProtocolConstants.PROXY_CMD_MARKER);
        this.recvData = data;
        this.channelId = channelId;
    }

    @Override
    public byte[] toByteArray() {
        byte[] bytePacket = new byte[Sizes.CMD + Sizes.CHANNEL_ID + Sizes.DATA_LEN + recvData.length + Sizes.MARK];
        System.out.println("size bytePacket" + bytePacket.length);
        buildRecvArray(bytePacket);
        return bytePacket;
    }

    private void buildRecvArray(byte[] bytePacket) {
        byte[] header = new byte[] {
                (byte) BotPacket.PROXY_BOT_RECV,
                (byte) (BotPacket.PROXY_BOT_RECV >> 8),

                (byte) channelId,
                (byte) (channelId >> 8),
                (byte) (channelId >> 16),
                (byte) (channelId >> 24),

                (byte) recvData.length,
                (byte) (recvData.length >> 8),
                (byte) (recvData.length >> 16),
                (byte) (recvData.length >> 24)
        };

        System.arraycopy(header, 0, bytePacket, 0, header.length);
        System.arraycopy(recvData, 0, bytePacket, header.length, recvData.length);
        bytePacket[bytePacket.length - 2] = (byte) mark;
        bytePacket[bytePacket.length - 1] = (byte) (mark >> 8);
    }

    @Override
    public String convertToString(boolean full) {
        if (!full) return super.convertToString();

        return super.convertToString() + System.lineSeparator() +
                "id=" + channelId + System.lineSeparator() +
                "len=" + recvData.length + System.lineSeparator() +
                "mark=" + mark + System.lineSeparator();
    }
}
