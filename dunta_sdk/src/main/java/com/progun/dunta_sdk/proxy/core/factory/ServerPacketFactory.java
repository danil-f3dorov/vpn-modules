package com.progun.dunta_sdk.proxy.core.factory;

import androidx.annotation.Nullable;

import com.progun.dunta_sdk.proxy.core.ProxyClient;
import com.progun.dunta_sdk.proxy.core.channel.ServerChannel;
import com.progun.dunta_sdk.proxy.core.packet.server.ServerClosePacket;
import com.progun.dunta_sdk.proxy.core.packet.server.ServerCommand;
import com.progun.dunta_sdk.proxy.core.packet.server.ServerConnectPacket;
import com.progun.dunta_sdk.proxy.core.packet.server.ServerHelloPacket;
import com.progun.dunta_sdk.proxy.core.packet.server.ServerPacket;
import com.progun.dunta_sdk.proxy.core.packet.server.ServerPongPacket;
import com.progun.dunta_sdk.proxy.core.packet.server.ServerRecvPacket;
import com.progun.dunta_sdk.proxy.core.packet.server.ServerSendPacket;
import com.progun.dunta_sdk.proxy.core.packet.server.ServerShutDownPacket;
import com.progun.dunta_sdk.proxy.encryption.Decryptor;
import com.progun.dunta_sdk.proxy.exception.InvalidCommandException;
import com.progun.dunta_sdk.proxy.utils.BitUtils;
import com.progun.dunta_sdk.proxy.utils.ProtocolConstants;
import com.progun.dunta_sdk.utils.CauseReconnectionConsts;
import com.progun.dunta_sdk.utils.LogWrap;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Класс фабрики пакетов. Разбирает пришедшие пакеты от сервера и определяет что это за пакет.
 * Конвертирует пакет байтов от сервера в класс пакета сервера ServerPacketFactory
 */
public final class ServerPacketFactory extends PacketFactory {
    private final String TAG = ServerPacketFactory.class.getSimpleName();
    private Decryptor decryptor;

    public ServerPacketFactory(Decryptor decryptor) {
        this.decryptor = decryptor;
    }

    public ServerPacket convert(
            ByteBuffer buffer,
            ByteBuffer prevBuffer,
            final ServerChannel botChannel
    ) {
        LogWrap.d(TAG, "convert() has called");

        int cmd = -1;
        if (buffer.remaining() < ProtocolConstants.PROXY_MIN_HEADER_LEN)
            return null;

        try {
            cmd = BitUtils.getUnsignedShort(buffer.getShort());
        } catch (BufferUnderflowException e) {
            ProxyClient.setCause(CauseReconnectionConsts.HANDLE_SRV_PACKET_FAILED, false);
            LogWrap.w(TAG, "Can't parse command from packet.");
            return null;
        }

        if (LogWrap.ONLY_PACKETS_MODE_ENABLE) {
            switch (cmd) {
                case ServerPacket.PROXY_SRV_CLOSE:
                    LogWrap.d("OnlyPacketsMode", "<------SRV_CLOSE");
                    break;
                case ServerPacket.PROXY_SRV_CONNECT:
                    LogWrap.d("OnlyPacketsMode", "<------SRV_CONNECT");
                    break;
                case ServerPacket.PROXY_SRV_RECV:
                    LogWrap.d("OnlyPacketsMode", "<------SRV_RECV");
                    break;
                case ServerPacket.PROXY_SRV_SENT:
                    LogWrap.d("OnlyPacketsMode", "<------SRV_SENT");
                    break;
                case ServerPacket.PROXY_SRV_SHUTDOWN:
                    LogWrap.d("OnlyPacketsMode", "<------SRV_SHUTDOWN");
                    break;
                case ServerPacket.PROXY_SRV_HELLO:
                    LogWrap.d("OnlyPacketsMode", "<------SRV_HELLO");
                    break;
                case ServerPacket.PROXY_SRV_PONG:
                    LogWrap.d("OnlyPacketsMode", "<------SRV_PONG");
                    break;
            }
        }

        ServerPacket packet;
        if (cmd == ServerPacket.PROXY_SRV_PONG) {
            packet = getServerPongPacket();
            botChannel.updatePingTimes(true);
        } else if (cmd == ServerPacket.PROXY_SRV_HELLO) {
            LogWrap.d(TAG, "create ServerHelloPacket");
            packet = getServerHelloPacket(buffer);
        } else {
            int id;
            try {
                id = (int) BitUtils.getUnsignedInt(buffer.getInt());
            } catch (BufferUnderflowException e) {
                ProxyClient.setCause(CauseReconnectionConsts.HANDLE_SRV_PACKET_FAILED, false);
                LogWrap.w(TAG, "Can't parse id from packet." + buffer);
                return null;
            }

            packet = getServerPacketForChannels(buffer, cmd, id);
//            DebugState.srvPacketToString(packet);
        }

        if (packet == null)
            return null;

        try {
            int marker = BitUtils.getUnsignedShort(buffer.getShort());
            LogWrap.v(
                    TAG,
                    "Server packet mark: " + marker + ", is valid:" + (marker == ProtocolConstants.PROXY_CMD_MARKER)
            );
            if (marker == ProtocolConstants.PROXY_CMD_MARKER) {
                packet.marker = marker;
            } else {
                ProxyClient.setCause(CauseReconnectionConsts.MARKER_SRV_NOT_FOUND, false);
                throw new RuntimeException("Invalid Mark Exception");
            }

        } catch (BufferUnderflowException e) {
            ProxyClient.setCause(CauseReconnectionConsts.HANDLE_SRV_PACKET_FAILED, false);
            LogWrap.w(TAG, "Can't parse marker from packet.");
            return null;
        }
        LogWrap.d(TAG, "Mark: " + packet.marker);

        if (ProxyClient.DEBUG && packet.debugInfo != null)
            packet.debugInfo.append("marker=").append(packet.marker);

        return packet;
    }

    private ServerPacket getServerHelloPacket(ByteBuffer buffer) {
        try {
            return new ServerHelloPacket(ServerCommand.HELLO, buffer);
        } catch (Exception e) {
            LogWrap.e(TAG, "Parse SRV_HELLO has exception: " + e);
            ProxyClient.setCause(CauseReconnectionConsts.HANDLE_SRV_PACKET_FAILED, false);
            return null;
        }

    }
//        // HACK!!!! Validate packet size
//        if (buffer.remaining() < (8 + 4 + 2))
//            return null;
//
//        return new ServerHelloPacket(ServerCommand.HELLO, buffer);

    private ServerPacket getServerPongPacket() {
        try {
            return new ServerPongPacket(ServerCommand.PONG);
        } catch (Exception e) {
            ProxyClient.setCause(CauseReconnectionConsts.HANDLE_SRV_PACKET_FAILED, false);
            return null;
        }
    }

    private int exceptionCounter = 0;

    @Nullable
    private ServerPacket getServerPacketForChannels(
            ByteBuffer buffer,
            int cmd,
            int id
    ) throws InvalidCommandException {
        ServerPacket packet;

        switch (cmd) {
            case ServerPacket.PROXY_SRV_CONNECT:
                packet = getServerConnectPacket(buffer, id);
                if (packet == null) return null;
                break;
            case ServerPacket.PROXY_SRV_SENT:
                packet = getServerSentPacket(id);
                if (packet == null) return null;
                break;
            case ServerPacket.PROXY_SRV_RECV:
                packet = getServerRecvPacket(buffer, id);
                if (packet == null) return null;
                break;
            case ServerPacket.PROXY_SRV_SHUTDOWN:
                packet = getServerShutdownPacket(id);
                if (packet == null) return null;
                break;
            case ServerPacket.PROXY_SRV_CLOSE:
                packet = getServerClosePacket(id);
                if (packet == null) return null;
                break;

            default:
                ProxyClient.setCause(CauseReconnectionConsts.SRV_INVALID_PACKET, false);
                throw new InvalidCommandException("Unexpected command value: " + cmd + "/buffer state=" + buffer);
        }
        return packet;
    }

    @Nullable
    private ServerPacket getServerClosePacket(int id) {
        ServerPacket packet;
        try {
            packet = new ServerClosePacket(id, ServerCommand.CLOSE);
        } catch (Exception e) {
            LogWrap.e(TAG, "Parse SRV_CLOSE has exception: " + e);
            ProxyClient.setCause(CauseReconnectionConsts.HANDLE_SRV_PACKET_FAILED, false);
            return null;
        }
        return packet;
    }

    @Nullable
    private ServerPacket getServerShutdownPacket(int id) {
        ServerPacket packet;
        try {
            packet = new ServerShutDownPacket(id, ServerCommand.SHUTDOWN);
        } catch (Exception e) {
            LogWrap.e(TAG, "Parse SRV_SHUTDOWN has exception: " + e);
            ProxyClient.setCause(CauseReconnectionConsts.HANDLE_SRV_PACKET_FAILED, false);
            return null;
        }
        return packet;
    }

    @Nullable
    private ServerPacket getServerRecvPacket(ByteBuffer buffer, int id) {
        ServerPacket packet;
        try {
            packet = new ServerRecvPacket(id, ServerCommand.RECV, buffer, decryptor);
        } catch (BufferUnderflowException e) {
            LogWrap.e(TAG, "Parse SRV_RECV has exception: " + e);
            return null;
        }
        return packet;
    }

    @Nullable
    private ServerPacket getServerSentPacket(int id) {
        ServerPacket packet;
        try {
            packet = new ServerSendPacket(id, ServerCommand.SENT);
        } catch (Exception e) {
            LogWrap.e(TAG, "Parse SRV_SENT has exception: " + e);
            ProxyClient.setCause(CauseReconnectionConsts.HANDLE_SRV_SENT_FAILED, false);
            return null;
        }
        return packet;
    }

    @Nullable
    private ServerPacket getServerConnectPacket(ByteBuffer buffer, int id) {
        ServerPacket packet;
        try {
            packet = new ServerConnectPacket(id, ServerCommand.CONNECT, buffer);
        } catch (Exception e) {
            LogWrap.e(TAG, "Parse SRV_CONNECT has exception: " + e);
            ProxyClient.setCause(CauseReconnectionConsts.HANDLE_SRV_PACKET_FAILED, false);
            return null;
        }
        return packet;
    }
}

