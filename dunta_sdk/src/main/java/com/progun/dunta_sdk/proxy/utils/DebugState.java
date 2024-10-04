package com.progun.dunta_sdk.proxy.utils;

import com.progun.dunta_sdk.proxy.core.packet.server.ServerCommand;
import com.progun.dunta_sdk.proxy.core.packet.server.ServerPacket;
import com.progun.dunta_sdk.proxy.core.packet.server.ServerRecvPacket;

import java.nio.ByteBuffer;

public class DebugState {
    public static final boolean DEBUG = false;

    public static String convertToString(ByteBuffer buffer, ServerPacket serverPacket) {
        if (serverPacket instanceof ServerRecvPacket) {
            return "BUFF:[pos=" + buffer.position() + "], lim=[" + buffer.limit() + "] -> convert(): " + cmdToString(
                    serverPacket.getCommand()) + " len=" + ((ServerRecvPacket) serverPacket).getLength();
        }
        return "BUFF:[pos=" + buffer.position() + "], lim=[" + buffer.limit() + "] -> convert(): " + cmdToString(
                serverPacket.getCommand());
    }

    public static String cmdToString(ServerCommand cmd) {
        switch (cmd) {
            case RECV:
                return "[SRV_RECV]";
            case SENT:
                return "[SRV_SENT]";
            case CLOSE:
                return "[SRV_CLOSE]";
            case CONNECT:
                return "[SRV_CONNECT]";
            case SHUTDOWN:
                return "[SRV_SHUTDOWN]";
            case PONG:
                return "[SRV_PONG]";
            default:
                return "[UNKNOWN]";
        }
    }
}
