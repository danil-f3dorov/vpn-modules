package com.progun.dunta_sdk.proxy.core;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.progun.dunta_sdk.proxy.core.channel.HostChannel;
import com.progun.dunta_sdk.proxy.core.channel.channelevent.ChannelConnectEvent;
import com.progun.dunta_sdk.proxy.core.factory.BotPacketFactory;
import com.progun.dunta_sdk.proxy.core.packet.bot.BotPacket;
import com.progun.dunta_sdk.proxy.core.packet.bot.Status;
import com.progun.dunta_sdk.proxy.core.packet.server.ServerCommand;
import com.progun.dunta_sdk.proxy.core.packet.server.ServerConnectPacket;
import com.progun.dunta_sdk.proxy.core.packet.server.ServerHelloPacket;
import com.progun.dunta_sdk.proxy.core.packet.server.ServerPacket;
import com.progun.dunta_sdk.proxy.core.packet.server.ServerRecvPacket;
import com.progun.dunta_sdk.proxy.utils.ProtocolConstants;
import com.progun.dunta_sdk.proxy.utils.SelectorHelper;
import com.progun.dunta_sdk.utils.LogWrap;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/*
 * Класс обрабатывает пакеты с сервера
 * */
public class ServerCommandDispatcher {
    private final static String TAG = ServerCommandDispatcher.class.getSimpleName();

    /**
     * Поток данных для записи в канал между SDK и proxy-сервером
     */
    private final BotOutputStream<BotPacket> bout;

    /**
     * Поток данных для чтения из канала между SDK и proxy-сервером
     */
    private final BotInputStream<ServerPacket> bin;

    private final BotPacketFactory factory = new BotPacketFactory();

    private final Map<Integer, HostChannel> channels;

    private final Selector selector;
    private final SelectionKey botKey;
    private boolean srvHelloReceived = false;

    private final AtomicLong botId;

    public ServerCommandDispatcher(
            @NonNull Map<Integer, HostChannel> channels,
            @NonNull BotOutputStream<BotPacket> bout,
            @NonNull BotInputStream<ServerPacket> bin,
            @NonNull Selector selector,
            SelectionKey botKey,
            @NonNull AtomicLong botId
    ) {
        this.channels = channels;
        this.bout = bout;
        this.bin = bin;
        this.selector = selector;
        this.botKey = botKey;
        this.botId = botId;
    }


    private void setChannelKeyTo(int ops) {
        SelectorHelper.setInterestOps(botKey, ops, "");
    }

    private SrvHelloReceiveListener srvHelloReceiveListener;

    public void setHelloReceiverListener(SrvHelloReceiveListener listener) {
        srvHelloReceiveListener = listener;
    }

    private void setChannelKeyTo(int ops, String comment) {
        SelectorHelper.setInterestOps(botKey, ops, comment);
        /*for (int i = 1; i <= DebugState.debugTimeoutMap.size(); i++)
            DebugState.addDebugTimeoutMap("SERV_KEY to: " + SelectorHelper.opsToString(ops) + "/ TIME_FROM_START=" +
                    DebugState.timeFromStart(System.currentTimeMillis()), i);*/
    }

    private void addChannelKeyOps(int ops) {
        if (botKey != null && botKey.isValid())
            SelectorHelper.setInterestOps(botKey, botKey.interestOps() | ops, "");
    }

    private void addChannelKeyOps(int ops, String comment) {
        if (botKey != null && botKey.isValid())
            SelectorHelper.setInterestOps(botKey, botKey.interestOps() | ops, comment);
    }

    private void resetBotKey(int ops) {
        setChannelKeyTo(botKey.interestOps() & (~ops));
    }

    /*
     * Обрабатывает команды от сервера. В зависимости от типа команды порождает соединенения(Connection),
     * добавляет пакеты в очередь для отправки на proxy-сервер.
     */
    @SuppressLint("DefaultLocale")
    <T extends ServerPacket> boolean handleSrvPacket(T packet) throws IOException {
        int channelId = packet.getChannelId();
        ServerCommand serverCommand = packet.getCommand();
        LogWrap.v(
                TAG,
                "Dispatching packet with id=" + channelId + " and serverCommand=" + serverCommand.name() + "channelSize= " + channels.size()
        );

        if (srvHelloReceived) {
            if (serverCommand == ServerCommand.CONNECT)
                return handleServerConnect((ServerConnectPacket) packet, channelId);
            else if (serverCommand == ServerCommand.RECV)
                return !handleServerRecv((ServerRecvPacket) packet, channelId);
            else if (serverCommand == ServerCommand.SENT)
                return !handleServerSent(channelId);
            else if (serverCommand == ServerCommand.SHUTDOWN)
                return handleServerShutdown(channelId);
            else if (serverCommand == ServerCommand.CLOSE) return !handleServerClose(channelId);
        } else {
            if (serverCommand == ServerCommand.HELLO) {
                srvHelloReceived = true;
                ServerHelloPacket helloPacket = ((ServerHelloPacket) packet);
                long botId = helloPacket.getBotId();
                this.botId.set(botId);
                srvHelloReceiveListener.onReceived(botId);
            }
        }
        return true;
    }

    private <T extends ServerPacket> boolean handleServerConnect(
            ServerConnectPacket packet,
            int channelId
    ) {
        HostChannel hostChannel;
        SocketAddress address;

        int port = packet.getPort();
        byte[] bytes = packet.getAddress();

        if (channels.containsKey(channelId)) {
            LogWrap.e(TAG, "Channel with id " + channelId + " existed, something went wrong");
            assert !channels.containsKey(channelId);
            return false;
        }

        try {
            if (isMaxChannelReached(channelId)) return true;
            address = new InetSocketAddress(InetAddress.getByAddress(bytes), port);
            hostChannel = new HostChannel(channelId, address);
            channels.put(channelId, hostChannel);
        } catch (IOException e) {
            bout.add(factory.connectPacket(channelId, Status.FAILED));
            return true;
        } catch (IllegalArgumentException e) {
            LogWrap.e(TAG, "Cant parse socket address from packet. Exception: " + e);
            bout.add(factory.connectPacket(channelId, Status.FAILED));
            return true;
        }

        registerHostShutdownAction(hostChannel);
        registerHostCloseAction(hostChannel);
        registerHostReadAction(hostChannel);
        registerHostWriteAction(hostChannel);
        registerHostConnectAction(hostChannel);

        hostChannel.connect(selector);
        return true;
    }

    private boolean isMaxChannelReached(int channelId) {
        if (channels.size() > ProtocolConstants.BOT_MAX_CHANNELS) {
            LogWrap.e(
                    TAG,
                    "Failed to create JsonResponseResult2 connection. Limit of max channels reached"
            );
            bout.add(factory.connectPacket(channelId, Status.MAX_CHANNELS));
            return true;
        }
        return false;
    }

    private <T extends ServerPacket> boolean handleServerRecv(
            ServerRecvPacket packet, int channelId
    ) {
        HostChannel hostChannel;
        if (!channels.containsKey(channelId)) {
            LogWrap.d(TAG, "SRV_RECV: this channel ID does not exist");
            return true;
        }
        hostChannel = Objects.requireNonNull(channels.get(channelId));

        byte[] data = packet.getData();
        //int length = packet.getLength();
        LogWrap.d(TAG, "handleServerRecv-------------------------");
        //hostChannel.setRecv(length);
        hostChannel.allowSrvRecv(data);
        return false;
    }

    private boolean handleServerSent(int channelId) {
        HostChannel hostChannel;
        if (!channels.containsKey(channelId)) {
            LogWrap.d(TAG, "SRV_SENT: this channel ID does not exist");
            return true;
        }

        hostChannel = Objects.requireNonNull(channels.get(channelId));
        hostChannel.allowSrvSent();
        return false;
    }

    private boolean handleServerShutdown(int channelId) throws IOException {
        HostChannel hostChannel;
        if (!channels.containsKey(channelId)) {
            LogWrap.d(TAG, "SRV_SHUTDOWN: this channel ID does not exist");
            return false;
        }


        hostChannel = Objects.requireNonNull(channels.get(channelId));
        LogWrap.d(TAG, "Receive SRV_SHUTDOWN command: current state is: " +
                "isSrvShutDowned=" + hostChannel.isSrvShutDowned() +
                ", isBotShutDowned:" + hostChannel.isBotShutDowned());

        LogWrap.d("SE-SRV", "SRV_SHUTDOWN before set: " + SelectorHelper.keyContent(botKey));
        // Probably need throw exception for future bug reporting
        if (hostChannel.isSrvShutDowned())
            return false;


        if (hostChannel.isBotShutDowned()) {
            hostChannel.close((short) 0);
            return false;
//            setChannelKeyTo(SelectionKey.OP_READ);
        }

        hostChannel.shutDown();
        return false;
    }

    private boolean handleServerClose(int channelId) {
        HostChannel hostChannel;
        if (!channels.containsKey(channelId)) {
            LogWrap.d(TAG, "SRV_CLOSE: this channel ID does not exist");
            return true;
        }

        hostChannel = Objects.requireNonNull(channels.get(channelId));

        LogWrap.d(
                TAG,
                "dispatchServerCommand(): channels hashmap has remove channel with id=" + channelId
        );
        hostChannel.close((short) 0);

//        channels.remove(channelId);

        channels.entrySet()
                .removeIf(integerHostChannelEntry -> integerHostChannelEntry.getKey() == channelId);

        if (channels.isEmpty()) {
            setChannelKeyTo(SelectionKey.OP_READ, "SRV_CLOSE");
        }
        return false;
    }

    // Register event callbacks
    private void registerHostShutdownAction(HostChannel hostChannel) {
        hostChannel.onBotShutdown(id -> {
            bout.add(factory.shutDownPacket(id));
//            bout.add(factory.shutDown(id));
            if (hostChannel.isSrvShutDowned()) {
//                channels.remove(id);
                channels.entrySet().removeIf(integerHostChannelEntry ->
                        integerHostChannelEntry.getKey() == id);
//                setChannelKeyTo(SelectionKey.OP_READ, "host_" + hostChannel.getId() + " shutdowner");
            }
            addChannelKeyOps(SelectionKey.OP_WRITE, "host_" + hostChannel.getId() + " shutdowner");
        });
    }

    private void registerHostConnectAction(HostChannel hostChannel) {
        LogWrap.d(TAG, "Setting connection connector ChannelConnectEvent");
        hostChannel.setConnector(new ChannelConnectEvent() {
            @Override
            public void onFailed(int channelId, Status status) {
                LogWrap.d(TAG, "Connection from server has failed. Called onFailed()");
                bout.add(factory.connectPacket(channelId, Status.FAILED));
                addChannelKeyOps(
                        SelectionKey.OP_WRITE,
                        "host_" + hostChannel.getId() + " connector fail"
                );
                LogWrap.e(TAG, "Client does not connect to server.");
            }

            @Override
            public void onSuccess(int id, HostChannel channel) {
                LogWrap.d(TAG, "Connection from server has successful. Called onSuccess()");
                bout.add(factory.connectPacket(id, Status.SUCCESS));
                addChannelKeyOps(
                        SelectionKey.OP_WRITE,
                        "host_" + hostChannel.getId() + " connector success"
                );
            }
        });
    }

    OnRecvSentToServerListener sentToServerListener = null;

    private void registerHostWriteAction(HostChannel hostChannel) {
        LogWrap.d(TAG, "Setting connection writer ChannelWriteEvent");

        hostChannel.onBotSent((id, size) -> {
            bout.add(factory.sentPacket(id));
            addChannelKeyOps(SelectionKey.OP_WRITE, "host_" + hostChannel.getId() + " writer");
        });
    }

    private void registerHostReadAction(HostChannel hostChannel) {
        LogWrap.d(TAG, "Setting connection reader ChannelReadEvent");
        hostChannel.onBotRecv((id, data) -> {
            bout.add(factory.recvPacket(id, data));
            addChannelKeyOps(SelectionKey.OP_WRITE, "host_" + hostChannel.getId() + " reader");
        });
    }

    private void registerHostCloseAction(HostChannel hostChannel) {
        LogWrap.v(TAG, "Setting connection closer ChannelCloseEvent");
        hostChannel.onBotClose(id -> {
            bout.add(factory.closePacket(id));
            channels.entrySet()
                    .removeIf(integerHostChannelEntry -> integerHostChannelEntry.getKey() == id);
            addChannelKeyOps(SelectionKey.OP_WRITE, "host_" + hostChannel.getId() + " reader");
        });
    }

    private void setBoutListener() {
        if (!bout.isListenerInit()) {
            sentToServerListener = (cmd, id) -> {
                LogWrap.d(TAG, "setBoutListener() has called, cmd=" + cmd.name() + " id=" + id);
                HostChannel hostChannel = null;
                if (id != -1)
                    hostChannel = channels.get(id);
            };
            bout.setWriteCompleteListener(sentToServerListener);
        }
    }
}
