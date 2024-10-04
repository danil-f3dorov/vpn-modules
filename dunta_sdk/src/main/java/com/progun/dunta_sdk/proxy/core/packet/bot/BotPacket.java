package com.progun.dunta_sdk.proxy.core.packet.bot;

import androidx.annotation.NonNull;

public abstract class BotPacket {

    protected final BotCommandType cmd;
    protected final int mark;

    public BotPacket(BotCommandType cmd, int mark) {
        this.cmd = cmd;
        this.mark = mark;
    }

    public abstract byte[] toByteArray();

    protected String convertToString() {
        return "------> " + this.toString();
    }
    abstract public String convertToString(boolean fullString);

    public BotCommandType getCmd() {
        return cmd;
    }

    @NonNull
    @Override
    public String toString() {
        return "BOT_" + cmd.name();
    }

    // размеры некоторых заголовков пакетов
    protected static class Sizes {
        protected static final int CMD = Short.BYTES;
        protected static final int CHANNEL_ID = Integer.BYTES;
        protected static final int MARK = Short.BYTES;
        protected static final int DATA_LEN = Integer.BYTES;
        protected static final int REPORT_LEN = Integer.BYTES;
    }

    /* Bot commands */
    public static final int PROXY_BOT_HELLO = 0x0001; // Start session
    public static final int PROXY_BOT_PING = 0x0002; // Ping
    public static final int PROXY_BOT_CONNECT = 0x0003; // Bot answer to connect
    public static final int PROXY_BOT_SENT = 0x0004; // Bot send data to external host
    public static final int PROXY_BOT_RECV = 0x0005; // Bot send data (server receives)
    public static final int PROXY_BOT_SHUTDOWN = 0x0006; // Bot send FIN (recv 0 on bot)
    public static final int PROXY_BOT_CLOSE = 0x0007; // Close channel
    public static final int PROXY_BOT_STATE = 0x0008; // Send information about Display activity and type of connection(Wifi, ethernet/mobile)
    public static final int PROXY_BOT_REPORT = 0x0009; // Send reports to server about issues
    /* Error Codes */
    public static final int PROXY_ERROR_SUCCESS = 0x00;
    public static final int PROXY_ERROR_FAILED = 0x01;
    public static final int PROXY_ERROR_UNKNOWN = 0x02;
    public static final int PROXY_ERROR_TIMEOUT = 0x03;
    public static final int PROXY_ERROR_MAX_CHANNELS = 0x04;

}
