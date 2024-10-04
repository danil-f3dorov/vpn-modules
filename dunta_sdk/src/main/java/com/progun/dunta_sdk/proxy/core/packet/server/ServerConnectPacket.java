package com.progun.dunta_sdk.proxy.core.packet.server;

import com.progun.dunta_sdk.proxy.core.ProxyClient;
import com.progun.dunta_sdk.proxy.utils.BitUtils;
import com.progun.dunta_sdk.utils.LogWrap;

import java.nio.ByteBuffer;

/**
 * Описывает пакет PROXY_SRV_CONNECT
 */
public final class ServerConnectPacket extends ServerPacket {
    private final int port;
    private final byte[] address = new byte[4];
    private static final String TAG = ServerConnectPacket.class.getSimpleName();

    public ServerConnectPacket(int id, ServerCommand commandType, ByteBuffer buffer) {
        super(id, commandType);
        LogWrap.d(TAG, "ServerConnectPacket() has called");

        buffer.get(address, 0, 4);
        port = BitUtils.getUnsignedShortBEOrder(buffer.getShort());

        if (ProxyClient.DEBUG)
            printPacketContent(id, commandType);
    }

    public int getPort() {
        return port;
    }

    public byte[] getAddress() {
        return address;
    }

    @SuppressWarnings("ConstantConditions")
    private void printPacketContent(int id, ServerCommand commandType) {
        StringBuilder sb = new StringBuilder();
        sb.append("<------ PROXY_SRV_CONNECT")
                .append("\n\tid=").append(id)
                .append("\n\taddress=");
        int[] test = new int[address.length];
        for (int i = 0; i < test.length; i++) {
            test[i] = Byte.toUnsignedInt(address[i]);
            sb.append(test[i]).append(".");
        }
        sb.append(":").append(port)
                .append("\nDump ");

        debugInfo = sb;
    }
}
