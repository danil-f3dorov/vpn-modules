package com.progun.dunta_sdk.proxy.core.channel;

import android.annotation.SuppressLint;
import android.util.Log;

import com.progun.dunta_sdk.api.DuntaManagerImpl;
import com.progun.dunta_sdk.proxy.core.ProxyClient;
import com.progun.dunta_sdk.proxy.core.channel.channelevent.ChannelCloseEvent;
import com.progun.dunta_sdk.proxy.core.channel.channelevent.ChannelConnectEvent;
import com.progun.dunta_sdk.proxy.core.channel.channelevent.ChannelReadEvent;
import com.progun.dunta_sdk.proxy.core.channel.channelevent.ChannelShutdownEvent;
import com.progun.dunta_sdk.proxy.core.channel.channelevent.ChannelWriteEvent;
import com.progun.dunta_sdk.proxy.utils.ProtocolConstants;
import com.progun.dunta_sdk.proxy.utils.ProxySettings;
import com.progun.dunta_sdk.proxy.utils.SelectorHelper;
import com.progun.dunta_sdk.utils.CauseReconnectionConsts;
import com.progun.dunta_sdk.utils.LogWrap;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Этот класс описывает соединение с целевым хостом.
 * Определяет как происходит коннект/чтение/запись в сокеты
 */
public final class HostChannel implements Channel {

    public boolean canRead = false;
    public boolean canWrite = false;
    long startTimeIO = -1;
    public long startTimeConnect = -1;
    public boolean FLAG_HOST_RECV = false;
    public boolean FLAG_HOST_SEND = false;

    ChannelStatus channelStatus = ChannelStatus.EMPTY;
    /**
     * isSrvShutDowned provides info is server send ShutDown command for specific channel ID
     */
    private boolean isSrvShutDowned = false;

    /**
     * isBotShutDowned provides info is bot send ShutDown command for specific channel ID
     */
    private boolean isBotShutDowned = false;

    public boolean isSrvShutDowned() {
        return isSrvShutDowned;
    }

    public boolean isBotShutDowned() {
        return isBotShutDowned;
    }

    private boolean isHostClosed = false;

    public boolean isHostClosed() {
        return isHostClosed;
    }

    private final String TAG = HostChannel.class.getSimpleName();
    //private int recv;

    private SelectionKey hostKey;

    private final int id;

    public int getId() {
        return id;
    }

    private final ByteBuffer in;
    private final ByteBuffer out;

    private final SocketChannel socket;
    private final SocketAddress address;

    /**
     * Обработчик событий
     */
    private ChannelReadEvent reader;
    private ChannelWriteEvent writer;
    private ChannelCloseEvent closer;
    private ChannelConnectEvent connector;
    private ChannelShutdownEvent shutdowner;

    public void setChannelKey(int ops) {
        if (ops == 0) {
            Log.d(TAG, "setChannelKey: ");
        }
        try {
            SelectorHelper.setInterestOps(hostKey, ops, "");
        } catch (CancelledKeyException e) {
            throw new RuntimeException(e);
        }

    }

    private void clearIOTimeout() {
        startTimeIO = -1;
    }

    //private void setStartTime() {
    //    startTimeIO = System.currentTimeMillis();
    //}

    private void resetHostKeyOps(int ops) {
        LogWrap.v(TAG, "resetHostKeyOps(), reset key: " + SelectorHelper.opsToString(ops), id);
        if (hostKey != null) {
            if (hostKey.isValid())
                setChannelKey(hostKey.interestOps() & (~ops));
        }
    }

    public HostChannel(int id, SocketAddress address) throws IOException {
        this.id = id;
        this.address = address;
        this.socket = SocketChannel.open();
        if(DuntaManagerImpl.socketCallback != null) {
            DuntaManagerImpl.socketCallback.onSocketCreate(socket.socket());
        }
        this.socket.configureBlocking(false);
        this.in = ByteBuffer.allocate(ProxySettings.PROXY_MAX_DATA_LEN);
        this.out = ByteBuffer.allocate(ProxySettings.PROXY_MAX_DATA_LEN);
    }

    public void onBotShutdown(ChannelShutdownEvent event) {
        LogWrap.v(TAG, "setShutdowner()", id);
        shutdowner = event;
    }

    public void onBotRecv(ChannelReadEvent event) {
        LogWrap.d(TAG, "setReader()", id);
        reader = event;
    }

    public void onBotClose(ChannelCloseEvent event) {
        LogWrap.v(TAG, "setCloser()", id);
        closer = event;
    }

    public void onBotSent(ChannelWriteEvent event) {
        LogWrap.v(TAG, "setWriter()", id);
        writer = event;
    }

    public void setConnector(ChannelConnectEvent event) {
        LogWrap.v(TAG, "setConnector()", id);
        connector = event;
    }


    public void read() {
        LogWrap.d(TAG, "read() method called", id);
        LogWrap.d(
                TAG,
                String.format(
                        "HostChannel starting read() to buffer. BEFORE: [interestOps=%s], [readyOps=%s]",
                        SelectorHelper.opsToString(hostKey.interestOps()),
                        SelectorHelper.opsToString(hostKey.readyOps())
                ),
                id
        );
        //GlobalChannelCounter.addToChannelString(id, "BOT - read1()","FLAG_HOST_RECV = "+FLAG_HOST_RECV);

        int read;
        in.clear();

        if (!isBotShutDowned) {
            try {
                LogWrap.d(TAG, "FLAG_HOST_RECV12 = " + FLAG_HOST_RECV, id);
                LogWrap.d(TAG, "isBotShutDowned = " + isBotShutDowned, id);
                LogWrap.v(TAG, "read() from host has called");
                if (FLAG_HOST_RECV) {
                    LogWrap.e(TAG, "read() HOST RECV ERROR");
                }
                assert !FLAG_HOST_RECV;
                //FLAG_HOST_RECV = false;

                // Logging before reading from socket
                LogWrap.d(
                        TAG,
                        "Before reading from socket. Buffer position: " + in.position() + ", limit: " + in.limit(),
                        id
                );
                int startPos = in.position();
                read = socket.read(in);
                // Logging after reading from socket
                LogWrap.i(TAG, "After reading from socket. Read " + read + " bytes.", id);
                if (read > 0) {
                    //readCounter++;
                    //bytesFromHost += read;

                    byte[] data = toByteArray(in, read);
                    // Logging before calling reader.onSuccess()
                    LogWrap.d(
                            TAG,
                            "Before calling reader.onSuccess(). Data length: " + data.length,
                            id
                    );
                    canRead = false;
                    resetHostKeyOps(SelectionKey.OP_READ);
                    reader.onSuccess(id, data);
                    FLAG_HOST_RECV = true;
                    LogWrap.d(TAG, "FLAG_HOST_RECV123 = " + FLAG_HOST_RECV, id);
                } else if (read == -1) {
                    LogWrap.w(TAG, "channel reads -1 bytes, host reached end-of-stream", id);
                    selfShutDown();
                } else {
                    close((short) 0);
                    LogWrap.e(TAG, "Channel read " + read + " bytes from host", id);
                    resetHostKeyOps(SelectionKey.OP_READ);
                }
            } catch (IOException e) {
                ProxyClient.setCause(CauseReconnectionConsts.READ_HOST_DATA_FAILED, false);
                close((short) 0);
                LogWrap.e(TAG, "read(): Failed to read from remote server. " + e.getMessage(), id);
            }
        } else {
            LogWrap.d(TAG, "Sdk doesn't ready for read more data from host", id);
            setChannelKey(hostKey.interestOps() & (~SelectionKey.OP_READ));
            resetHostKeyOps(SelectionKey.OP_READ);
        }
    }


    @Override
    public void write() {
        int write;
        if (!FLAG_HOST_SEND) {
            LogWrap.e(TAG, "_____________________FLAG ERROR_________________________()");
            assert FLAG_HOST_SEND;
        }
        try {
            write = socket.write(out);
            canWrite = false;
            LogWrap.i(TAG, "host write " + write + " bytes", id);
            if (write > 0) {
                //bytesToHost += write;
                //writeCounter += write;
                //out.compact();
                //recv -= write;
                LogWrap.d(TAG, "Channel has write to socket successfully", id);

                // Если отправил не всё, то устанавливает startTimeIO = System.currentTimeMillis()
                if(out.hasRemaining()) {
                    startTimeIO = System.currentTimeMillis();
                }
                // Если отправил всё - то тогда  writer.onSuccess(id, write); startTimeIO -1
                else {
                    writer.onSuccess(id, write);
                    startTimeIO = -1;
                    resetHostKeyOps(SelectionKey.OP_WRITE);
                    FLAG_HOST_SEND = false;
                    out.clear();
                }

            } else {
                close((short) 0);
            }
        } catch (IOException | CancelledKeyException e) {
            close((short) 0);
            LogWrap.e(TAG, "Failed to write to remote connection. " + e.getMessage(), id);
        }

        String debugKeyInterestOpsValue;
        String debugKeyReadyOpsValue;
        if (hostKey.isValid()) {
            debugKeyInterestOpsValue = SelectorHelper.opsToString(hostKey.interestOps());
            debugKeyReadyOpsValue = SelectorHelper.opsToString(hostKey.readyOps());
        } else {
            debugKeyInterestOpsValue = "NaN";
            debugKeyReadyOpsValue = "NaN";
        }
        LogWrap.d(
                TAG,
                String.format(
                        "HostChannel starting write() to buffer. AFTER: [interestOps=%s], [readyOps=%s]",
                        debugKeyInterestOpsValue,
                        debugKeyReadyOpsValue
                ),
                id
        );
    }

    @Override
    public void close(short closeCause) {
        LogWrap.v(TAG, "close()", id);
        try {
            LogWrap.v(TAG, "Channel closing host connection...", id);
            socket.close();
        } catch (IOException e) {
            LogWrap.e(TAG, "Failed to close remote connection " + e.getMessage());
        } finally {
            cancelKey(hostKey);
            startTimeConnect = -1;
            clearIOTimeout();
            in.clear();
            out.clear();
            LogWrap.d(TAG, "Channel success close host connection", id);
            isHostClosed = true;
            closer.onSuccess(id);
        }
    }

    private void cancelKey(SelectionKey key) {

        if (key != null) {
            LogWrap.d(TAG, "cancelKey(" + key.getClass().getSimpleName() + " key )", id);
            key.cancel();
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void shutDown() throws IOException {

        LogWrap.v(TAG, "shutDown()", id);
        isSrvShutDowned = true;
        socket.shutdownOutput();
        clearIOTimeout();
        if (isBotShutDowned) {
            LogWrap.v(TAG, "Bot has shutdowned channel before -> close channel");
            cancelKey(hostKey);
            socket.close();
            startTimeConnect = -1;
            clearIOTimeout();
            in.clear();
            out.clear();
            LogWrap.d(TAG, "Channel success close host connection", id);
            isHostClosed = true;
        } else {
            LogWrap.d(TAG, "SelectionKey.OP_READ", id);
            //setChannelKey(SelectionKey.OP_READ);
        }
    }


    /*
        try {
            LogWrap.v(TAG, "Channel closing host connection...", id);
            socket.close();
        } catch (IOException e) {
            LogWrap.e(TAG, "Failed to close remote connection " + e.getMessage());
        } finally {
            cancelKey(hostKey);
            startTimeConnect = -1;
            clearIOTimeout();

            in.clear();
            out.clear();

            LogWrap.d(TAG, "Channel success close host connection", id);
            isHostClosed = true;
            closer.onSuccess(id);
        }
    */
    /*cancelKey(hostKey);
    startTimeConnect = -1;
    clearIOTimeout();

            in.clear();
            out.clear();

            LogWrap.d(TAG, "Channel success close host connection", id);
    isHostClosed = true;*/
    private void selfShutDown() throws IOException {
        LogWrap.d(TAG, "selfShutDown(), isBotShutDowned=" + isBotShutDowned +
                ", isSrvShutDowned=" + isSrvShutDowned, id);
        isBotShutDowned = true;
        socket.shutdownInput();
        clearIOTimeout();
        startTimeConnect = -1;
        shutdowner.onShutDownEvent(getId());

        if (isSrvShutDowned) {
            cancelKey(hostKey);
            socket.close();
            in.clear();
            out.clear();
            isHostClosed = true;
        } else {
            resetHostKeyOps(SelectionKey.OP_READ);
        }
    }

    @Override
    public void checkTimeouts(long currentTime) {
        if (!socket.isConnected()) {
            boolean isConnectionPending = socket.isConnectionPending();
            boolean isStartTimeConnect = startTimeConnect != -1;
            if (isConnectionPending && isStartTimeConnect) {
                long time = currentTime - startTimeConnect;
                LogWrap.v(TAG, "Check timeout:  " + time + " ms.(connect)", id);
                if (time > ProtocolConstants.HOST_CONNECT_TIMEOUT) {
                    LogWrap.e(TAG, "Host CONNECTION TIMEOUT reached", id);

                    close((short) 0);
                    connector.onFailed(
                            id,
                            com.progun.dunta_sdk.proxy.core.packet.bot.Status.TIMEOUT
                    );
                    channelStatus = ChannelStatus.EMPTY;
                }
            }
        } else {
            if (startTimeIO != -1) {
                long time = currentTime - startTimeIO;
                LogWrap.v(TAG, "Check timeout:  " + time + " ms.(IO)", id);
                if (time > ProtocolConstants.HOST_READ_TIMEOUT) {
                    LogWrap.e(
                            TAG,
                            "Host READ TIMEOUT reached: over " + (time - ProtocolConstants.HOST_READ_TIMEOUT) + "ms.",
                            id
                    );
                    close((short) 0);
                }
            }
        }
    }

    @Override
    public void connect(Selector selector) {
        LogWrap.v(TAG, "host connect() called");
        try {
            if (socket.isConnectionPending() || socket.isConnected()) {
                if (socket.finishConnect()) {
                    LogWrap.i(TAG, "Connect to host success", id);
                    resetHostKeyOps(SelectionKey.OP_CONNECT);
                    LogWrap.w(TAG, "Connect timeout started in:" + startTimeConnect, id);
                    startTimeConnect = -1;
                    connector.onSuccess(id, this);
                    return;
                }
                LogWrap.e(TAG, "Remote connection with channel did not created.", id);
                close((short) 0);
                connector.onFailed(id, com.progun.dunta_sdk.proxy.core.packet.bot.Status.UNKNOWN);
                channelStatus = ChannelStatus.EMPTY;
            } else {
                LogWrap.i(TAG, "Channel connecting to host " + address + "...", id);
                socket.connect(address);
                channelStatus = ChannelStatus.CONNECTION_PENDING;
                startTimeConnect = System.currentTimeMillis();
                hostKey = socket.register(
                        selector,
                        SelectionKey.OP_CONNECT | SelectionKey.OP_READ,
                        this
                );
            }
        } catch (IOException e) {
            close((short) 0);
            LogWrap.e(TAG, "Remote connection with channel did not created.", id);
            connector.onFailed(id, com.progun.dunta_sdk.proxy.core.packet.bot.Status.FAILED);
            channelStatus = ChannelStatus.EMPTY;
        }
    }

    public void connectReachedTimeout() {
        connector.onFailed(id, com.progun.dunta_sdk.proxy.core.packet.bot.Status.TIMEOUT);
        close((short) 0);
    }

    @Override
    public ChannelStatus getStatus() {
        return channelStatus;
    }

    @Override
    public boolean isConnecting() {
        LogWrap.v(TAG, "isConnecting called()", id);
        return socket != null && socket.isConnectionPending() && channelStatus == ChannelStatus.READING;
    }

    @Override
    public boolean isReading() {
        return socket != null && channelStatus == ChannelStatus.READING;
    }


    public void allowSrvRecv(byte[] data) {
        LogWrap.d(TAG, "allow(byte [])", id);
        assert !FLAG_HOST_SEND;
        FLAG_HOST_SEND = true;

        canWrite = true;

        out.put(data, 0, Math.min(out.remaining(), data.length));
        setChannelKey(hostKey.interestOps() | SelectionKey.OP_WRITE);
        LogWrap.d(
                TAG,
                "allow(byte[] data): CHANGE_SELECTOR_KEY to = " + SelectorHelper.keyContent(hostKey),
                id
        );
        out.flip();
    }

    public void allowSrvSent() {
        // Method for reacting to SRV_SENT
        LogWrap.d(TAG, "allow()", id);
        assert FLAG_HOST_RECV;
        FLAG_HOST_RECV = false;

        LogWrap.d(TAG, "FLAG_HOST_RECV after change = " + FLAG_HOST_RECV);
        if (hostKey != null) {
            if (hostKey.isValid()) {
                canRead = true;

                setChannelKey(hostKey.interestOps() | SelectionKey.OP_READ);
                LogWrap.d(TAG, "allow(): CHANGE_SELECTOR_KEY to = OP_READ", id);
            } else {
                LogWrap.w(TAG, "hostKey is invalid key", id);
            }
        } else {
            LogWrap.w(TAG, "hostKey null", id);
        }
    }


    public void handleBotSent() {
        LogWrap.v(TAG, "handleBotSent() called");
        if (hostKey != null && hostKey.isValid()) {
            setChannelKey(SelectionKey.OP_READ);
            canRead = true;
        } else {
            if (hostKey == null) LogWrap.w(TAG, "onBotSent error, hostKey=null");
            if (!hostKey.isValid()) LogWrap.w(TAG, "onBotSent error, hostKey is invalid");
        }
    }

    byte[] toByteArray(ByteBuffer buffer, int length) {
        LogWrap.d(TAG, "toByteArray()", id);
        byte[] array = buffer.array();
        byte[] data = new byte[length];

        System.arraycopy(array, 0, data, 0, length);

        return data;
    }

    //public void setRecv(int value) {
    //    LogWrap.d(TAG, "setRecv()", id);
    //    recv = value;
    //}
}
