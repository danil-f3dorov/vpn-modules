package com.progun.dunta_sdk.proxy.core.packet.server;

import com.progun.dunta_sdk.proxy.core.ProxyClient;
import com.progun.dunta_sdk.proxy.encryption.Decryptor;
import com.progun.dunta_sdk.proxy.utils.BitUtils;
import com.progun.dunta_sdk.proxy.utils.ProtocolConstants;
import com.progun.dunta_sdk.utils.LogWrap;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Описывает пакет PROXY_SRV_RECV
 */
//@SuppressWarnings("SpellCheckingInspection")
public final class ServerRecvPacket extends ServerPacket {
    private static final String TAG = ServerRecvPacket.class.getSimpleName();
    private final int length;
    private final byte[] data;

    public ServerRecvPacket(
            int id,
            ServerCommand cmd,
            ByteBuffer buffer,
            Decryptor decryptor
    ) throws BufferUnderflowException {
        super(id, cmd);
        LogWrap.d(TAG, "ServerRecvPacket(), buffer remaining=" + buffer.remaining(), id);
        length = (int) BitUtils.getUnsignedInt(buffer.getInt());
        LogWrap.d(TAG, "ServerRecvPacket() length=" + length);
        data = new byte[length];

        try {
            buffer.get(data, 0, length);
        } catch (BufferUnderflowException e) {
            throw e;
        }

        if (ProtocolConstants.ENABLE_CRYPT) decryptor.process(data);

        //if (data.length <= 3) LogWrap.d(TAG, "data length is not normally low");



        if (ProxyClient.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("<------ PROXY_SRV_RECV")
                    .append("\n\tid = ").append(id)
                    .append("\n\tdataLen = ").append(length);
            sb.append("\n packet size: ").append(length);
            debugInfo = sb;
        }
    }

    public int getLength() {
        return length;
    }

    public byte[] getData() {
        return data;
    }
}
