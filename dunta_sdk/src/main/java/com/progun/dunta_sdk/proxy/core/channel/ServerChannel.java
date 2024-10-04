package com.progun.dunta_sdk.proxy.core.channel;

import androidx.annotation.NonNull;

import com.progun.dunta_sdk.api.DuntaManagerImpl;
import com.progun.dunta_sdk.proxy.core.BotProtocolProvider;
import com.progun.dunta_sdk.proxy.core.DeviceNetworkState;
import com.progun.dunta_sdk.proxy.core.ProxyClient;
import com.progun.dunta_sdk.proxy.core.channel.channelevent.BotCloseEvent;
import com.progun.dunta_sdk.proxy.core.channel.channelevent.BotHelloEvent;
import com.progun.dunta_sdk.proxy.core.channel.channelevent.BotPingEvent;
import com.progun.dunta_sdk.proxy.core.channel.channelevent.BotReadEvent;
import com.progun.dunta_sdk.proxy.core.channel.channelevent.BotShutDownEvent;
import com.progun.dunta_sdk.proxy.core.channel.channelevent.BotWriteEvent;
import com.progun.dunta_sdk.proxy.exception.ReadSocketException;
import com.progun.dunta_sdk.proxy.utils.ProtocolConstants;
import com.progun.dunta_sdk.utils.CauseReconnectionConsts;
import com.progun.dunta_sdk.utils.LogWrap;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Канал соединения с proxy-сервером. Синглтон.
 * Работает с провайдером(BotProtocolProvider),
 * который управляет всей логикой взаимодействия с каналом между сервером и SDK
 */
public final class ServerChannel implements Channel {
    private static final String TAG = ServerChannel.class.getSimpleName();
    private static ServerChannel instance;
    ChannelStatus channelStatus = ChannelStatus.EMPTY;
    private SocketChannel socket;

    private final String androidID;

    private BotProtocolProvider provider;

    //public long startTimeRead = -1;
    long startTimeConnect = -1;
    //public long startTimePing = -1;

    long mWriteTime;    // Last write time
    long mPingTime;     // Ping sent time
    long mPongTime;     // Pong received time
    boolean mPingSent = false;  // Ping was sent, waiting for server pong
    private AtomicLong botId = new AtomicLong(0);

    private final int applicationId;
    private final int partnerId;
    private final int advertisementId;
    private final DeviceNetworkState networkState;

    public void setReportMessage(String reportMessage) {
        LogWrap.v(TAG, "setReportMessage() has called");
        assert(provider != null);
        provider.setReportMessage(reportMessage);
    }

    private ServerChannel(
            String androidID,
            int partnerId,
            int applicationId,
            int advId,
            @NonNull DeviceNetworkState networkState
    ) {
        LogWrap.v(TAG, "ServerChannel constructor() has called");
        this.androidID = androidID;
        this.applicationId = applicationId;
        this.partnerId = partnerId;
        advertisementId = advId;
        this.networkState = networkState;
    }

    //public void refreshNewTimeoutRead() {
    //    newStartTimeRead = System.currentTimeMillis();
    //}

    //public void refreshNewTimeoutPing() {
    //    newStartTimePing = System.currentTimeMillis();
    //}

    //public void refreshNewTimeouts() {
    //    LogWrap.v(TAG, "Refresh timeouts.");
    //    refreshNewTimeoutPing();
    //    refreshNewTimeoutRead();
    //}

    public void updatePingTimes(boolean pong) {
        mPingTime = System.currentTimeMillis();
        mPongTime = System.currentTimeMillis();
        mPingSent = !pong;
    }

    private void updateWriteTimeout() {
        mWriteTime = System.currentTimeMillis();
        mPingTime  = System.currentTimeMillis();
    }

    public void lockStateChanged(int changedTo) {
        LogWrap.v(TAG, "stateChanged() has called");
        int screenLock = 1;
        if (provider != null){
            provider.state(screenLock, changedTo);
        } else {
            LogWrap.e(TAG, "ProtocolProvider is null");
        }
    }

    public boolean socketIsConnected() {
        LogWrap.v(TAG, "socketIsConnected() has called");
        return socket != null && socket.isConnected();
    }


    public AtomicLong getBotId() {
        return botId;
    }

    public void setBotId(AtomicLong botId) {
        this.botId = botId;
    }

    @Override
    public void checkTimeouts(long currentTime) {
        //logNewTimeout(currentTime);

        if(socketIsConnected()) {
            if(!provider.isOutQueueEmpty()) {
                if (currentTime - mWriteTime >= ProtocolConstants.SRV_WRITE_TIMEOUT) {
                    LogWrap.e(TAG, "timeout read reached");
                    closeServer(CauseReconnectionConsts.TIMEOUT);
                }
            }

            if (!mPingSent && ((currentTime - mPingTime) >= (ProtocolConstants.SRV_PING_TIMEOUT))) {
                LogWrap.d(TAG, "Ping timeout reached");
                pingServer();
            }
            if (mPingSent && ((currentTime - mPongTime) >= (2 * ProtocolConstants.SRV_PING_TIMEOUT))) {
                LogWrap.e(TAG, "timeout read reached");
                closeServer(CauseReconnectionConsts.TIMEOUT);
            }
        }

        /*if (socketIsConnected() && isHostChannelsEmpty() && isServerQueuesEmpty()) {
            LogWrap.v(TAG, "Check ping timeout...");
            if (currentTime - newStartTimePing >= (ProtocolConstants.SRV_PING_TIMEOUT - 1_000)) {
                LogWrap.d(TAG, "Ping timeout reached");
                pingServer();
            }
        } else if (socketIsConnected() && (!isHostChannelsEmpty() || !isServerQueuesEmpty())) {
            LogWrap.v(TAG, "Check read timeout...");
            if (currentTime - newStartTimeRead >= ProtocolConstants.SRV_READ_TIMEOUT) {
                LogWrap.e(TAG, "timeout read reached");
                closeServer(CauseReconnectionConsts.TIMEOUT);
            }
        } else {
            LogWrap.d(TAG, "Check no timeout: size of channels=" + provider.getChannelsSize() + ", bin=" + provider.getBinQueueSize() + ", bout=" + provider.getBoutQueueSize());
            LogWrap.d(TAG, "socketIsConnected=" + socketIsConnected() + ", isHostChannelsEmpty=" + isHostChannelsEmpty() + ", isServerQueuesEmpty=" + isServerQueuesEmpty());
        }*/
    }

    /*private void logNewTimeout(long currentTime) {
        float readTimeout = -1;
        float pingTimeout = -1;
        if (newStartTimeRead > 0) readTimeout = ((float) (currentTime - newStartTimeRead)) / 1000;
        if (newStartTimePing > 0) pingTimeout = ((float) (currentTime - newStartTimePing)) / 1000;
        LogWrap.d(TAG, String.format(Locale.ENGLISH, "Check timeouts: read=%fs., ping=%fs.", readTimeout, pingTimeout)
        );
    }*/

//    private void checkPingTimeout(long currentTime) {
//        LogWrap.d(TAG, "checkPingTimeout() has called");
//        if (currentTime - startTimePing >= ProtocolConstants.SRV_PING_TIMEOUT) {
//            LogWrap.w(TAG, "timeout ping reached");
//            pingServer();
//            refreshOldTimeoutPing();
//        }
//    }

    /*private void refreshOldTimeoutPing() {
        startTimePing = System.currentTimeMillis();
    }

    public void disableOldTimeoutPing() {
        startTimePing = -1;
    }*/

    /*private void refreshOldReadTimeout() {
        startTimeRead = System.currentTimeMillis();
    }

    private void disableOldTimeoutRead() {
        startTimeRead = 1;
    }*/


    public void checkRead() {

    }

    public synchronized void closeServer(short closeCause) {
        LogWrap.v(TAG, "closeServer() has called");
        try {
            close(closeCause);
        } catch (IOException e) {
            LogWrap.e(TAG, "closeServer() has called " + e);
            ProxyClient.setCause(CauseReconnectionConsts.CLASSES_CLOSE_FAILED, false);
        }
    }

    private void pingServer() {
        LogWrap.v(TAG, "pingServer() has called");
        try {
            ping();
        } catch (IOException e) {
            LogWrap.e(TAG, "checkTimeouts() has called " + e);
        }
    }

    public boolean connectToServer(SocketAddress address, Selector selector) {
        LogWrap.v(TAG, "connectToServer() has called");
        try {
            if (address != null)
                LogWrap.i(TAG, "Start connect to server: " + address + "...");
            else {
                LogWrap.d(TAG, "Start connect to server: null...");
            }
            try {
                socket = SocketChannel.open();
                if(DuntaManagerImpl.socketCallback != null) {
                    DuntaManagerImpl.socketCallback.onSocketCreate(socket.socket());
                }

                socket.configureBlocking(true);
                if (socket.isBlocking()) {
                    socket.socket().connect(address, ProtocolConstants.SRV_CONNECT_TIMEOUT);
                    startTimeConnect = System.currentTimeMillis();
                } else {
                    throw new IOException("Trying to connect to server in socket non-blocking mode");
                }
            } catch (IOException | IllegalArgumentException e) {
                LogWrap.e(TAG, "Connect to server has ended with exception: " + e);
                startTimeConnect = -1;

                return false;
            } catch (IllegalBlockingModeException exception) {
                throw new RuntimeException(
                        "to=" + socket.getRemoteAddress() +
                        ", block=" + socket.isBlocking() +
                        ", open=" + socket.isOpen() +
                        ", time=" + (System.currentTimeMillis() - startTimeConnect)
                        , exception);
            }
            socket.configureBlocking(false);

            provider = new BotProtocolProvider(
                    androidID,
                    this,
                    selector,
                    socket,
                    partnerId,
                    applicationId,
                    botId,
                    advertisementId,
                    Objects.requireNonNull(networkState)
            );

            updatePingTimes(true);

            return true;
        } catch (IOException e) {
            LogWrap.e(TAG, "socket.configureBlocking(false) throw exception: " + e);
            startTimeConnect = -1;
            return false;
        }
    }

    @Override
    public void read() throws IOException, ReadSocketException {
        LogWrap.d(TAG, "read()");
        provider.handleBotEvent(new BotReadEvent() {
            @Override
            public void onFailed() {
                LogWrap.d(TAG, "read onFailed()");
                //refreshNewTimeouts();
            }

            @Override
            public void onSuccess() {
                LogWrap.d(TAG, "read onSuccess()");
                //refreshNewTimeouts();
            }
        });
    }

    @Override
    public void write() throws IOException {
        provider.handleBotEvent(new BotWriteEvent() {
            @Override
            public void onFailed() {
                LogWrap.w(TAG, "Server write failed");
                //refreshNewTimeouts();
            }

            @Override
            public void onSuccess() {
                LogWrap.v(TAG, "Server write success");
                updateWriteTimeout();
            }
        });
    }

    @Override
    public void close(short closeCause) throws IOException {
        if (closeCause == CauseReconnectionConsts.TIMEOUT) {
            ProxyClient.setCause(CauseReconnectionConsts.TIMEOUT, false);
        }
        if (provider == null) return;
        provider.handleBotEvent(new BotCloseEvent() {
            @Override
            public void onFailed() {
                LogWrap.e(TAG, "Server close failed");
                //refreshNewTimeouts();
            }

            @Override
            public void onSuccess() {
                LogWrap.v(TAG, "Server close success");
                //refreshNewTimeouts();
            }
        });
    }

    @Override
    public void shutDown() throws IOException {
        provider.handleBotEvent(new BotShutDownEvent() {
            @Override
            public void onFailed() {
                LogWrap.e(TAG, "Server shutDown failed");
            }

            @Override
            public void onSuccess() {
                LogWrap.d(TAG, "Server shutDown success");
            }
        });
    }

    @Override
    public void connect(Selector selector) throws IOException {
        provider.handleBotEvent(new BotHelloEvent() {
            @Override
            public void onFailed() {
                LogWrap.e(TAG, "Server connect failed");
            }

            @Override
            public void onSuccess() {
                LogWrap.v(TAG, "Server connect success");
            }
        });
    }

    @Override
    public ChannelStatus getStatus() {
        return channelStatus;
    }

    @Override
    public boolean isConnecting() {
        return socket != null && socket.isConnectionPending();
    }

    @Override
    public boolean isReading() {
        return channelStatus == ChannelStatus.READING;
    }

    public void ping() throws IOException {
        provider.handleBotEvent(new BotPingEvent() {
            @Override
            public void onFailed() {
                LogWrap.e(TAG, "Server ping failed");
                updatePingTimes(false);
            }

            @Override
            public void onSuccess() {
                LogWrap.v(TAG, "Server ping packet add to bout queue");
                updatePingTimes(false);
            }
        });
    }

    public boolean isHostChannelsEmpty() {
        return provider.isChannelsStateEmpty();
    }

    /*public boolean isServerQueuesEmpty() {
        return provider.isQueuesEmpty();
    }*/

    public void setServerOps() {
        provider.setOps();
    }


    public static ServerChannel create(
            String androidID,
            int partnerId,
            int applicationId,
            int advId,
            @NonNull DeviceNetworkState networkState
    ) {
        return new ServerChannel(androidID, partnerId, applicationId, advId, networkState);
    }
}
