package com.progun.dunta_sdk.proxy.core.packet.bot;

import com.progun.dunta_sdk.proxy.utils.ProtocolConstants;

public class BotStatePacket extends BotPacket {
    public enum TYPE {NETWORK, LOCK_STATE}

    public enum NET_STATE {WIFI, CELLULAR}

    public enum LOCK_STATE {LOCK, UNLOCK}

    private final short type;
    private final short state;

    public BotStatePacket(int type, int state) {
        super(BotCommandType.STATE, ProtocolConstants.PROXY_CMD_MARKER);

        this.type = (short) type;
        this.state = (short) state;
    }

    @Override
    public byte[] toByteArray() {
        return new byte[]{
                (byte) BotPacket.PROXY_BOT_STATE,
                (byte) (BotPacket.PROXY_BOT_STATE >> 8),

                (byte) type,
                (byte) (type >> 8),

                (byte) state,
                (byte) (state >> 8),

                (byte) mark,
                (byte) (mark >> 8),
        };
    }

    @Override
    public String convertToString(boolean fullString) {
        if (!fullString) return super.convertToString();

        return super.convertToString() + System.lineSeparator() +
                "\ttype=" + typeToString(type) + System.lineSeparator() +
                "\tstate=" + stateToString(type, state) + System.lineSeparator() +
                "\tmark=" + mark;
    }

    private String typeToString(int type) {
        if (type == TYPE.NETWORK.ordinal())
            return TYPE.NETWORK.name();
        else
            return TYPE.LOCK_STATE.name();
    }

    private String stateToString(int type, int state) {
        if (type == TYPE.NETWORK.ordinal()) {
            if (state == NET_STATE.WIFI.ordinal()) return NET_STATE.WIFI.name();
            else return NET_STATE.CELLULAR.name();
        } else {
            if (state == LOCK_STATE.LOCK.ordinal()) return LOCK_STATE.LOCK.name();
            else return LOCK_STATE.UNLOCK.name();
        }
    }
}
