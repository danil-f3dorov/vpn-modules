package com.progun.dunta_sdk.proxy.core;

import android.os.Build;

import androidx.annotation.NonNull;

import com.progun.dunta_sdk.android.connectionstate.ConnectionType;
import com.progun.dunta_sdk.android.core.DuntaService;
import com.progun.dunta_sdk.proxy.core.channel.HostChannel;
import com.progun.dunta_sdk.proxy.core.channel.ServerChannel;
import com.progun.dunta_sdk.proxy.core.channel.channelevent.BotCloseEvent;
import com.progun.dunta_sdk.proxy.core.channel.channelevent.BotEvent;
import com.progun.dunta_sdk.proxy.core.channel.channelevent.BotHelloEvent;
import com.progun.dunta_sdk.proxy.core.channel.channelevent.BotPingEvent;
import com.progun.dunta_sdk.proxy.core.channel.channelevent.BotReadEvent;
import com.progun.dunta_sdk.proxy.core.channel.channelevent.BotShutDownEvent;
import com.progun.dunta_sdk.proxy.core.channel.channelevent.BotWriteEvent;
import com.progun.dunta_sdk.proxy.core.factory.BotPacketFactory;
import com.progun.dunta_sdk.proxy.core.packet.bot.BotHelloPacket;
import com.progun.dunta_sdk.proxy.core.packet.bot.BotPacket;
import com.progun.dunta_sdk.proxy.core.packet.bot.BotReportPacket;
import com.progun.dunta_sdk.proxy.core.packet.bot.BotStatePacket;
import com.progun.dunta_sdk.proxy.core.packet.server.ServerPacket;
import com.progun.dunta_sdk.proxy.exception.ConnectionResetByPeer;
import com.progun.dunta_sdk.proxy.exception.ReadSocketException;
import com.progun.dunta_sdk.proxy.exception.ServerResetSocketException;
import com.progun.dunta_sdk.proxy.utils.ProtocolConstants;
import com.progun.dunta_sdk.proxy.utils.SelectorHelper;
import com.progun.dunta_sdk.utils.CauseReconnectionConsts;
import com.progun.dunta_sdk.utils.LogWrap;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Класс, который описывает логику для обработки методов в канале между SDK и proxy-сервером.
 * Создает экземпляры команд сервера(ServerPacket) и бота(BotPacket) и обрабатывает их/реагирует на них.
 */
public final class BotProtocolProvider {

    final private static String TAG = BotProtocolProvider.class.getSimpleName();

    private SelectionKey botKey;
    private final Selector selector;

    /**
     * Bot's socket channel with proxy-server
     */
    private final SocketChannel serverSocket;

    private final ServerChannel botChannel;

    /**
     * Все зарегистрированные каналы в боте. Хранить может каналы с хостом.
     */
    private final Map<Integer, HostChannel> channels = new HashMap<>();
    private final BotPacketFactory factory = new BotPacketFactory();

    /**
     * Поток данных для записи в канал между SDK и proxy-сервером
     */
    private final BotOutputStream<BotPacket> bout = new BotOutputStream<>();

    /**
     * Поток данных для чтения из канала между SDK и proxy-сервером
     */
    private final BotInputStream<ServerPacket> bin;

    private ServerCommandDispatcher serverCommandDispatcher;

    private String reportMessage;

    //    private final int sdkVersion;
    private final int partnerId;
    private final int applicationId;
    private final int advertisementId;

    //todo SERVER_HELLO_FLAG
    private boolean srvHelloReceived = false;

    public static int crushCounter = 0;


    /**
     * Atomic value using for pass this value to {@link ServerCommandDispatcher} constructor by reference.
     */
    private AtomicLong botId;

    DeviceNetworkState networkState;

    private final String androidId;

    public BotProtocolProvider(
            String androidId,
            @NonNull ServerChannel channel,
            @NonNull Selector selector,
            @NonNull SocketChannel serverSocket,
            int partnerId, int applicationId,
            @NonNull AtomicLong botId,
            int advertisementId,
            @NonNull DeviceNetworkState networkState

    ) {
        this.androidId = androidId;
        this.botChannel = Objects.requireNonNull(channel);
        this.selector = Objects.requireNonNull(selector);
        this.serverSocket = Objects.requireNonNull(serverSocket);
        this.partnerId = partnerId;
        this.applicationId = applicationId;
        this.botId = botId;
        this.advertisementId = advertisementId;
        this.networkState = networkState;

        bin = new BotInputStream<>(this.botChannel); // Передаем размер буфера в BotInputStream
    }

    private void addChannelKeyOps(int ops) {
        try {
            int interestOps = -1;

            if (botKey != null && botKey.isValid()) {
                interestOps = botKey.interestOps();
            }

            if (botKey != null && botKey.isValid())
                SelectorHelper.setInterestOps(botKey, interestOps | ops, "");

        } catch (CancelledKeyException cke) {
            String exceptionData;
            if (botKey != null && botKey.isValid() && selector.isOpen()) {
                boolean serviceRunning = DuntaService.serviceIsRunning;
                int keysSize = selector.keys().size();
                boolean isSelectorOpen = selector.isOpen();
                String botKeyOps = SelectorHelper.keyContent(botKey);

                exceptionData = "serviceRunning = " + serviceRunning +
                        ", keysSize=" + keysSize +
                        ", isOpen=" + isSelectorOpen +
                        ", botKeyOps=" + botKeyOps +
                        ", new ops=" + ops;
            } else {
                boolean serviceRunning = DuntaService.serviceIsRunning;
                int keysSize = selector.keys().size();
                boolean isSelectorOpen = selector.isOpen();

                exceptionData = "serviceRunning = " + serviceRunning +
                        ", keysSize=" + keysSize +
                        ", isOpen=" + isSelectorOpen +
                        ", new ops=" + ops;
            }
            throw new IllegalStateException(exceptionData, cke);
        }
    }

    private void addChannelKeyOps(int ops, String comment) {
        if (botKey != null && botKey.isValid())
            SelectorHelper.setInterestOps(botKey, botKey.interestOps() | ops, comment);
    }

    public void state(int network, int use) {
        // queue for packets which will be sent to server
        if (botKey != null && selector.isOpen() && botKey.isValid()) {
            bout.add(factory.statePacket(network, use));
            //addChannelKeyOps(SelectionKey.OP_WRITE);
        }
    }

    private void setChannelKeyOps(int ops) {
        SelectorHelper.setInterestOps(botKey, ops, "");
    }

    private void setChannelKeyOps(int ops, String comment) {
        SelectorHelper.setInterestOps(botKey, ops, comment);
    }

    private void resetBotKeyOps(int ops) {
        if (botKey != null && botKey.isValid()) {
            setChannelKeyOps(botKey.interestOps() & (~ops));
        }
    }


    /**
     * Обрабатывает полученные ивенты, описывающие действия бота в общении с сервером.
     * Реализация записи/чтения в поток данных BotStream
     */
    public <T extends BotEvent> void handleBotEvent(T event) throws IOException, ReadSocketException {
        crushCounter++;
        if (event instanceof BotHelloEvent)
            handleBotHelloEvent();
        else if (event instanceof BotReadEvent)
            handleBotReadEvent(event);
        else if (event instanceof BotPingEvent)
            handleBotPingEvent(event);
        else if (event instanceof BotWriteEvent)
            handleBotWriteEvent(event);
        else if (event instanceof BotCloseEvent)
            handleBotCloseEvent(event);
        else if (event instanceof BotShutDownEvent)
            LogWrap.d(TAG, "Called handleEvent(). Event commandType is [" +
                    BotShutDownEvent.class.getSimpleName() + "]");
    }

    private <T extends BotEvent> void handleBotCloseEvent(T event) throws IOException {
        LogWrap.v(TAG, "Bot_Event: Close");

        //resetBotKeyOps(SelectionKey.OP_READ);
        //setChannelKeyOps(SelectionKey.OP_WRITE, "BotCloseEvent");

        if (ProxyClient.getCause() == CauseReconnectionConsts.TIMEOUT)
            close(CauseReconnectionConsts.TIMEOUT);
        else
            close((short) 0);

        event.onSuccess();
    }

    private <T extends BotEvent> void handleBotWriteEvent(T event) {
        LogWrap.v(
                TAG,
                "Bot_Event: Write, queue bin/bout = " + getBinQueueSize() + "/" + getBoutQueueSize()
        );

        if (bout.writeToServer(serverSocket)) {
            event.onSuccess();
        } else {
            event.onFailed();
            try {
                close(CauseReconnectionConsts.WRITE_SERVER_DATA_FAILED);
            } catch (IOException e) {
                StringBuilder queueContent = new StringBuilder("Queue contains: [");
                bout.mPacketsQueue.forEach(it -> queueContent.append(((BotPacket) it).convertToString(
                        false)).append(" "));
                throw new RuntimeException(e + queueContent.toString());
            }
        }
    }

    private <T extends BotEvent> void handleBotPingEvent(T event) {
        LogWrap.v(TAG, "Bot_Event: Ping");
        bout.add(factory.pingPacket());
        event.onSuccess();
        //botChannel.refreshNewTimeouts();
        //addChannelKeyOps(SelectionKey.OP_WRITE, "BotPingEvent");
    }

    private <T extends BotEvent> void handleBotReadEvent(T event) throws IOException {
        LogWrap.v(
                TAG,
                "Bot_Event: Read, queue bin/bout = " + getBinQueueSize() + "/" + getBoutQueueSize()
        );
        boolean readResult;
        try {
            try {
                readResult = bin.readSocket(serverSocket);
                LogWrap.v(TAG, "readResult=" + readResult);
            } catch (ReadSocketException | ClosedChannelException rse) {
                event.onFailed();
                throw rse;
            }

            ServerPacket packet;

            try {
                if (readResult) {
                    event.onSuccess();
                    while ((packet = bin.get()) != null)
                        serverCommandDispatcher.handleSrvPacket(packet);
                    return;
                } else {
                    event.onFailed();
                }
                close(CauseReconnectionConsts.READ_SERVER_DATA_FAILED);
            } catch (IOException e) {
                LogWrap.e(TAG, "handleBotReadEvent() has called: " + e.getMessage());
                event.onFailed();
                close(CauseReconnectionConsts.HANDLE_BOT_PACKET_FAILED);
            }
        } catch (ReadSocketException rse) {
            LogWrap.e(TAG, "handleBotReadEvent() has called: " + rse.getMessage());
            event.onFailed();
            if (rse instanceof ServerResetSocketException) {
                rse.setCauseFlag(CauseReconnectionConsts.UNEXPECTED_CONNECTION_RESET);
            } else if (rse instanceof ConnectionResetByPeer) {
                rse.setCauseFlag(CauseReconnectionConsts.NETWORK_WAS_LOST);
            }
            if (rse.getCauseFlag() != -1) {
                close(rse.getCauseFlag());
            } else {
                close((short) 0);
            }
        }
    }

    private void handleBotHelloEvent() {
        LogWrap.v(TAG, "Bot_Event: Hello");
        String model = Build.MODEL;
        String platform = Build.SUPPORTED_ABIS[0];

        // 0 - wifi 1 - cellular
        BotHelloPacket helloPacket = factory.helloPacket(
                androidId,
                partnerId,
                applicationId,
                botId.get(),
                advertisementId,
                networkState.getStateInt(),
                networkState.getTypeInt(),
                Build.VERSION.SDK_INT,
                model,
                platform,
                ProtocolConstants.SDK_RELEASE_VERSION_TEST_APP
        );

        BotReportPacket report = null;

        if (reportMessage != null)
            report = factory.reportPacket(reportMessage);

        if (serverSocket.isConnected()) {
            bout.add(helloPacket);

            if (report != null)
                bout.add(report);

            bout.add(currentNetworkState(ProxyClient.CONNECTION_TYPE));

            setHelloReceived(false);

            register(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        } else LogWrap.e(TAG, "Server does not connected yet");

        serverCommandDispatcher =
                new ServerCommandDispatcher(channels, bout, bin, selector, botKey, botId);
        serverCommandDispatcher.setHelloReceiverListener((botId) -> setHelloReceived(true));
    }

    private void setHelloReceived(boolean received) {
        srvHelloReceived = received;
    }


    public void setReportMessage(String reportMessage) {
        this.reportMessage = reportMessage;
    }

    public boolean isQueuesEmpty() {
        return bin.isPacketsEmpty() && bout.isPacketsEmpty();
    }

    public boolean isOutQueueEmpty() {
        return bout.isPacketsEmpty();
    }

    private BotStatePacket currentNetworkState(int networkType) {
        LogWrap.d(TAG, "currentNetworkState() with type=" + networkType);
        if (networkType != ConnectionType.NONE)
            return factory.statePacket(0, networkType);
        else
            throw new IllegalStateException("Internet connection does not exists");
    }

    private void register(int ops) {
        LogWrap.v(TAG, "Called register()");
        try {
            botKey = serverSocket.register(selector, ops, botChannel);
            LogWrap.v(TAG, "Registered socket with ops: " + SelectorHelper.opsToString(ops));
        } catch (IOException e) {
            LogWrap.e(TAG, "Failed to register channel." + e.getMessage());
        }
    }

    public void close(short closeCause) throws IOException {
        LogWrap.v(TAG, "Starting close bpp...");
        try {
            serverSocket.close();
            selector.close();
            LogWrap.d(TAG, "socket and selector is CLOSED");
        } catch (IOException e) {
            LogWrap.e(TAG, "Failed to close socket. " + e.getMessage());
        } finally {
            if (!channels.isEmpty()) {
                Set<HostChannel> channelsToClose = new HashSet<>();

                channels.forEach((key, value) -> {
                    if (!value.isHostClosed()) channelsToClose.add(value);
                });
                channelsToClose.forEach((channel) -> channel.close((short) 0));
            }
            ProxyClient.setCause(closeCause, false);

            bin.close();
            bout.close();
            channels.clear();
        }
        LogWrap.d(TAG, "Bpp has closed");
    }

    private void closeChannelIfHostClosed(Integer key, HostChannel value) {
        if (!value.isHostClosed()) value.close((short) 0);
    }

    public int getChannelsSize() {
        return channels.size();
    }

    public int getBinQueueSize() {
        return bin.mPacketsQueue.size();
    }

    public int getBoutQueueSize() {
        return bout.mPacketsQueue.size();
    }

    public boolean isChannelsStateEmpty() {
        boolean isBoutPacketsEmpty = bout.isPacketsEmpty();
        boolean isBinPacketsEmpty = bin.isPacketsEmpty();
        boolean isChannelsEmpty = channels.isEmpty();
//        LogWrap.d(TAG, "isChannelsStateEmpty():" + " boutIsEmpty=" + isBoutPacketsEmpty + " binIsEmpty=" + isBinPacketsEmpty + " channelsIsEmpty=" + isChannelsEmpty);
        return isBinPacketsEmpty && isBoutPacketsEmpty && isChannelsEmpty;
    }

    public void setOps() {
        int iOps = SelectionKey.OP_READ;
        if(!bout.isPacketsEmpty())
            iOps |= SelectionKey.OP_WRITE;

        setChannelKeyOps(iOps);
    }
}