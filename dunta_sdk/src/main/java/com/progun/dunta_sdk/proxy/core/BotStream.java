package com.progun.dunta_sdk.proxy.core;

import androidx.annotation.NonNull;

import com.progun.dunta_sdk.proxy.core.channel.ServerChannel;
import com.progun.dunta_sdk.proxy.core.factory.ServerPacketFactory;
import com.progun.dunta_sdk.proxy.core.packet.bot.BotCommandType;
import com.progun.dunta_sdk.proxy.core.packet.bot.BotPacket;
import com.progun.dunta_sdk.proxy.core.packet.bot.BotRecvPacket;
import com.progun.dunta_sdk.proxy.core.packet.server.ServerPacket;
import com.progun.dunta_sdk.proxy.encryption.Decryptor;
import com.progun.dunta_sdk.proxy.encryption.Encryptor;
import com.progun.dunta_sdk.proxy.exception.ConnectionResetByPeer;
import com.progun.dunta_sdk.proxy.exception.ReadSocketException;
import com.progun.dunta_sdk.proxy.exception.ServerResetSocketException;
import com.progun.dunta_sdk.proxy.utils.ProtocolConstants;
import com.progun.dunta_sdk.proxy.utils.ProxySettings;
import com.progun.dunta_sdk.utils.CauseReconnectionConsts;
import com.progun.dunta_sdk.utils.LogWrap;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Абстрактный класс, описывающий поток данных между SDK и прокси-сервером
 */
@SuppressWarnings("SpellCheckingInspection")
abstract class BotStream<T> {
    private final String TAG = BotStream.class.getSimpleName();

    protected final Queue<? super T> mPacketsQueue = new ConcurrentLinkedQueue<>();
    ByteBuffer mBuffer =
            ByteBuffer.allocate(ProxySettings.PROXY_MAX_DATA_LEN + ProtocolConstants.PROXY_RECV_HEADER_LEN + ProtocolConstants.PROXY_MARKER_LEN)
                    .order(ByteOrder.LITTLE_ENDIAN);

    boolean isPacketsEmpty() {
        return mPacketsQueue.isEmpty();
    }

    void close() {
        LogWrap.d(TAG, "packets queue has closed");
        mPacketsQueue.clear();
    }
}

/**
 * Класс, описывающий входящий поток данных между SDK и прокси-сервером, принимает данные
 */
@SuppressWarnings("SpellCheckingInspection")
final class BotInputStream<T extends ServerPacket> extends BotStream<ServerPacket> {
    private final String TAG = BotInputStream.class.getSimpleName();
    private final ServerChannel botChannel;
    private final Decryptor decryptor = new Decryptor(ProtocolConstants.CRYPT_SEED);
    private final ServerPacketFactory factory = new ServerPacketFactory(decryptor);
    private final ByteBuffer prevBuffer =
            ByteBuffer.allocate(ProxySettings.PROXY_MAX_DATA_LEN + ProtocolConstants.PROXY_MAX_HEADER_LEN + ProtocolConstants.PROXY_MARKER_LEN)
                    .order(ByteOrder.LITTLE_ENDIAN);
    int amountReadsPacketsCounter = 0;

    BotInputStream(@NonNull ServerChannel serverChannel) {
        this.botChannel = serverChannel;
    }

    boolean readSocket(SocketChannel socket) throws ClosedChannelException, ReadSocketException {
        boolean needNext = false;
        LogWrap.d(TAG, "readSocket() called");


        int read;
        int startedBufferPos;
        int amountReadBytes = 0;
        ServerPacket serverPacket;

        try {
            read = socket.read(mBuffer);
            LogWrap.i(TAG, "InputSocket: read " + read + " bytes");
            if (read == -1) {
                LogWrap.e(TAG, "Server channel has reached end-of-stream");
                LogWrap.w(
                        TAG,
                        "Waiting for " + (ProtocolConstants.HOST_CONNECT_TIMEOUT / 1000) + " sec for reconnecting"
                );
                mBuffer.clear();
                try {
                    Thread.sleep(ProtocolConstants.HOST_CONNECT_TIMEOUT);
                } catch (InterruptedException ex) {
                    LogWrap.w(TAG, "readSocket(): interrupted current thread");
                    Thread.currentThread().interrupt();
                }
                return false;
            }

            amountReadBytes += read;

            if (read == 0) {
                LogWrap.d(TAG, "readSocket() read 0 bytes");
                if ((mBuffer.limit() < mBuffer.capacity()) && !mBuffer.hasRemaining()) {
                    mBuffer.limit(mBuffer.capacity());
                }
            }
            mBuffer.flip();

        } catch (ClosedChannelException e) {
//            ProxyClient.setCause(CauseReconnectionConsts.READ_SERVER_DATA_FAILED);
            LogWrap.e(TAG, "readSocket() has exception: " + e);
            throw e;
        } catch (IOException e) {
            ReadSocketException readSocketException = new ReadSocketException(e);
            readSocketException.setCauseFlag(CauseReconnectionConsts.READ_SERVER_DATA_FAILED);
//            ProxyClient.setCause(CauseReconnectionConsts.READ_SERVER_DATA_FAILED);
            String internetConnectionAbort = "Software caused connection abort";
            String serverOff = "Connection reset by peer";
            if (ProxyClient.getCause() != CauseReconnectionConsts.HANDLE_SRV_PACKET_FAILED
                    && ProxyClient.getCause() != CauseReconnectionConsts.HANDLE_SRV_SENT_FAILED
                    && ProxyClient.getCause() != CauseReconnectionConsts.HANDLE_SRV_RECV_FAILED) {
                if (e.getMessage() != null) {
                    if (e.getMessage().equals(internetConnectionAbort)) {
                        readSocketException = new ConnectionResetByPeer(readSocketException);
//                        readSocketException.setCauseFlag(CauseReconnectionConsts.NETWORK_WAS_LOST);
                        LogWrap.e(TAG, "Probably internet conenction was lost or changes");
                    } else if (e.getMessage().equals(serverOff)) {
                        readSocketException = new ServerResetSocketException(readSocketException);
//                        readSocketException.setCauseFlag(CauseReconnectionConsts.UNEXPECTED_CONNECTION_RESET);
                        LogWrap.e(TAG, "Probably server off");
                    }
                }
            }
            // Если внезапно оборвалось соединение
            LogWrap.e(TAG, "readSocket() has exception: " + e);
            LogWrap.w(
                    TAG,
                    "Waiting for " + (ProtocolConstants.HOST_CONNECT_TIMEOUT / 1000) + " sec for reconnecting"
            );
            try {
                Thread.sleep(ProtocolConstants.HOST_CONNECT_TIMEOUT);
            } catch (InterruptedException ex) {
                LogWrap.w(TAG, "readSocket(): interrupted current thread");
                Thread.currentThread().interrupt();
            }

            throw readSocketException;
        }

        while (mBuffer.hasRemaining() && read > 0) {
            startedBufferPos = mBuffer.position();
            if ((serverPacket = factory.convert(mBuffer, prevBuffer, botChannel)) != null) {
                mPacketsQueue.offer(serverPacket);
                amountReadsPacketsCounter++;
                LogWrap.i(TAG, String.valueOf(serverPacket.debugInfo));
            } else {
                LogWrap.e(TAG, "Covert packets with NULL.");
                if (mBuffer.position() != 0) needNext = true;
                mBuffer.position(startedBufferPos);
                mBuffer.compact();
                break;
            }
        }

        if (!needNext) {
            mBuffer.clear();
        }
        return amountReadBytes > 0;
    }

    private final int serverSentCounter = 0;

    /**
     * Готовит буфер для чтения, ставя позишн в место после прочитанных/записанных данных
     */
    private void reset(ByteBuffer buffer) {
        int limit = buffer.limit();
        int capacity = buffer.capacity();
        buffer.position(limit);
        buffer.limit(capacity);
    }

    ServerPacket get() {
        return (ServerPacket) mPacketsQueue.poll();
    }
}

interface OnRecvSentToServerListener {
    public void onWrite(BotCommandType cmd, int id);
}

/**
 * Класс, описывающий выходящий поток данных между SDK и прокси-сервером.
 * Записывает данные в очередь для отправки на сервер или напрямую в сокет, связанный с сервером.
 */

@SuppressWarnings("SpellCheckingInspection")
final class BotOutputStream<T extends BotPacket> extends BotStream<BotPacket> {
    private final String TAG = BotOutputStream.class.getSimpleName();

    private final Encryptor encryptor = new Encryptor();

    private OnRecvSentToServerListener sentListener;

    BotOutputStream() {
        LogWrap.d(TAG, "constructor()");
    }

    public void setWriteCompleteListener(@NonNull OnRecvSentToServerListener listener) {
        this.sentListener = listener;
    }

    public boolean isListenerInit() {
        return sentListener != null;
    }

    BotPacket mPacket;

    boolean writeToServer(SocketChannel socket) {
        /*if (mPacketsQueue.isEmpty()) {
            LogWrap.v(TAG, "writeToServer() called, queue is empty");
            return false;
        }*/
        LogWrap.v(TAG, "writeToServer() called, queue size=" + mPacketsQueue.size());

        if (mPacket == null) {
            mPacket = (BotPacket) mPacketsQueue.poll();

            if (ProtocolConstants.ENABLE_CRYPT) {
                assert mPacket != null;
                if (mPacket.getCmd() == BotCommandType.RECV) encryptor.process(((BotRecvPacket) mPacket).getRecvData());
            }

            mBuffer.clear();
            mBuffer.put(mPacket.toByteArray());

            mBuffer.flip();
        }
        if (mPacket != null) {
            if (ProxyClient.DEBUG) {
                if (LogWrap.ONLY_PACKETS_MODE_ENABLE)
                    LogWrap.d("OnlyPacketsMode", mPacket.convertToString(false));
                else LogWrap.i(TAG, mPacket.convertToString(true));
            }

            try {
                int iCountOfWritenBytes = socket.write(mBuffer);
                LogWrap.i(TAG, "Write to server: " + iCountOfWritenBytes + " bytes");
                if (!mBuffer.hasRemaining()) {
                    mPacket = null;
                }
            } catch (NotYetConnectedException e) {
                ProxyClient.setCause(CauseReconnectionConsts.WRITE_SERVER_DATA_FAILED, false);
                LogWrap.w(TAG, "Server not yet connected for write to them");
                return false;
            } catch (ClosedChannelException e) {
                ProxyClient.setCause(CauseReconnectionConsts.WRITE_SERVER_DATA_FAILED, false);
                LogWrap.e(TAG, "Server channel for writing is closed: " + e);
                return false;
            } catch (IOException e) {
                ProxyClient.setCause(CauseReconnectionConsts.WRITE_SERVER_DATA_FAILED, false);
                LogWrap.e(TAG, "Write to server failed with exception: " + e);
                return false;
            }
        }

        return true;
    }
    boolean isPacketsEmpty() {
        return mPacketsQueue.isEmpty() && mPacket == null;
    }

    void add(@NonNull T packet) {
        mPacketsQueue.offer(packet);
    }
}