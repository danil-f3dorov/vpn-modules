package com.progun.dunta_sdk.proxy.core;


import androidx.annotation.NonNull;

import com.progun.dunta_sdk.BuildConfig;
import com.progun.dunta_sdk.proxy.core.channel.Channel;
import com.progun.dunta_sdk.proxy.core.channel.HostChannel;
import com.progun.dunta_sdk.proxy.core.channel.ServerChannel;
import com.progun.dunta_sdk.proxy.core.jsonserver.HttpClient;
import com.progun.dunta_sdk.proxy.core.jsonserver.JsonInitListener;
import com.progun.dunta_sdk.proxy.core.jsonserver.JsonResponseResult;
import com.progun.dunta_sdk.proxy.core.report.ReportProvider;
import com.progun.dunta_sdk.proxy.exception.JsonRequestParametersErrorException;
import com.progun.dunta_sdk.proxy.exception.ParseJSONNodeException;
import com.progun.dunta_sdk.proxy.exception.SuchUserDataNotFound;
import com.progun.dunta_sdk.proxy.utils.ProtocolConstants;
import com.progun.dunta_sdk.proxy.utils.SelectorHelper;
import com.progun.dunta_sdk.utils.CauseReconnectionConsts;
import com.progun.dunta_sdk.utils.LogWrap;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Верхнеуровнево управляет клиентом.
 * По сути просто говорит что делать каналу в клиенте.
 * Зона ответственности ограничевается указанием,
 * типа: "пинг на сервер", "коннект к серверу", "дисконнект"
 */
@SuppressWarnings("SpellCheckingInspection")
public final class ProxyClient implements Runnable {
    final private String TAG = ProxyClient.class.getSimpleName();
    private final boolean HARD_LOG_ENABLE = true;
    public static final boolean DEBUG = BuildConfig.DEBUG_LOG;
    //    public static final boolean DEBUG = true;
    public volatile static boolean isClientRunning = true;
    private Thread proxyThread;
    private static final long SELECTOR_TIMEOUT = 5000; // ms

    private final int applicationId;
    private final int partnerId;

    private final DeviceInfo deviceInfo;
    public static int CONNECTION_TYPE;

    private int currentNetworkState = -1; // 0 - wifi, 1 - mobile, -1 - none
    volatile ServerChannel serverChannel;

    private final ReportProvider reportProvider;
    private JsonInitListener jsonInitListener;

    private final boolean isSentToJson;

    private final int advId;

    public ProxyClient(
            DeviceInfo deviceInfo, ReportProvider reportProvider,
            int applicationId, int partnerId,
            boolean isSentToJson, int advId,
            @NonNull DeviceNetworkState currentDeviceNetworkState
    ) {
        this.deviceInfo = deviceInfo;
        this.reportProvider = reportProvider;
        this.applicationId = applicationId;
        this.partnerId = partnerId;
        this.isSentToJson = isSentToJson;
        this.advId = advId;
        this.currentDeviceNetworkState = currentDeviceNetworkState;
        LogWrap.d(TAG, "ProxyClient created: advID=" + advId + "/partnerID=" + partnerId +
                "/appID=" + applicationId + "/deviceID=" + deviceInfo.getDeviceId() +
                "/deviceModel=" + deviceInfo.getDeviceModel()
        );
    }

    private boolean isSelectorOpen() {
        return selector != null && selector.isOpen();
    }

    public void setInitializeListener(JsonInitListener jsonInitListener) {
        this.jsonInitListener = jsonInitListener;
    }

    public void lockStateChanged(int changedTo) {
        try {
            if (isSelectorOpen() && serverChannel != null && serverChannel.socketIsConnected())
                serverChannel.lockStateChanged(changedTo);
        } catch (ClosedSelectorException e) {

        }
    }

    public void start() {
        isClientRunning = true;
        proxyThread = new Thread(this);
        proxyThread.setName(getClass().getSimpleName());
        proxyThread.start();
        LogWrap.d(TAG, "PClient has started");
    }

    public void stop(short closeCause) {
        LogWrap.d(TAG, "Stops pClient...");

        if (serverChannel != null) {
            serverChannel.closeServer(closeCause);
        }

        proxyThread.interrupt();
        isClientRunning = false;
    }

    private Selector selector;

    @Override
    public void run() {
        LogWrap.d(TAG, "pClient thread starts...");

        //if (selector == null) {
        //    LogWrap.e(TAG, "Open selector failed");
        //    stop(CauseReconnectionConsts.SELECTOR_OPEN_FAILED);
        //    return;
        //А}

        while (!proxyThread.isInterrupted())
        {
            // Получение адреса сервера
            LogWrap.d(TAG, "Try to connect with JSON-server...");

            Selector selector = null; // Открытие Selector
            try {
                selector = Selector.open();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            InetSocketAddress serverAddress = null;
            try {
                serverAddress = getJsonServerResponse();
            } catch (JsonRequestParametersErrorException | ParseJSONNodeException e) {
                LogWrap.e(TAG, "Connect to server with exception: " + e);
            }
            if(serverAddress == null)
                continue;

            String debug = deviceInfo.getDeviceId();
            // Create server mode
            serverChannel = ServerChannel.create(deviceInfo.getDeviceId(), partnerId, applicationId, advId, currentDeviceNetworkState);

            // Подключение к прокси-серверу
            boolean connectRes = connectToProxyServer(selector, serverAddress);
            if (connectRes) {
                try {
                    handleReports();
                    serverChannel.connect(selector);
                } catch (IOException e) {
                    LogWrap.e(TAG, "Connect to server with exception: " + e);
                }
            }

            // Цикл обработки ключей
            while (serverChannel != null
                    && serverChannel.socketIsConnected()
                    && connectRes && !proxyThread.isInterrupted()
                    && selector.isOpen() && selector.keys().size() > 0
            )
            {
                serverChannel.setServerOps();

                int selected = select(selector);
                if (selected < 0)
                    break;

                try {
                    handleKeys(selector, selector.selectedKeys());
                    checkTimeouts(selector);
                } catch (Throwable e) {
                    sleepThread(ProtocolConstants.SRV_CONNECT_TIMEOUT_SEC);
                    break;
                }
            }

            closeServer(CauseReconnectionConsts.END_OF_WORK);
        }
    }

    private void handleReports() {
        boolean isReportsExists = reportProvider.isReportsExists();
        if (isReportsExists && serverChannel != null) {
            LogWrap.d(TAG, "Report-file exists");
            String readReportFiles = reportProvider.readReport();
            serverChannel.setReportMessage(readReportFiles);
            boolean deleteReportFile = reportProvider.deleteReport();
            if (deleteReportFile)
                LogWrap.d(TAG, "Report-file deleted");
        } else
            LogWrap.d(TAG, "Report-file does not exist");
    }

    private void checkTimeouts(Selector selector) {
        selector.keys().forEach((key -> {
            if (key.isValid()) {
                Channel channel = (Channel) key.attachment();
                if (channel != null) channel.checkTimeouts(System.currentTimeMillis());
            }
        }));
    }

    private InetSocketAddress getJsonServerResponse() throws IllegalStateException, JsonRequestParametersErrorException,
            ParseJSONNodeException {
        String serverAddr = ProtocolConstants.JSON_SERVER_URL;
        HttpClient client = new HttpClient(serverAddr, jsonInitListener, isSentToJson);

        while (!proxyThread.isInterrupted()) {
            JsonResponseResult serverResponse = null;

            try {
                serverResponse = client.getJsonServerAddressProd(deviceInfo, partnerId, applicationId, advId);
            } catch (JsonRequestParametersErrorException | ParseJSONNodeException e) {
                LogWrap.e(TAG, "Connect to server with exception: " + e);
            }

            // If we caught exeption or got null response
            if(serverResponse == null)
            {
                sleepThread(ProtocolConstants.JSON_CONNECT_TIMEOUT_SEC);
                continue;
            }

            if (serverResponse instanceof JsonResponseResult.ResponseSuccess) {
                try {
                    String address =
                            ((JsonResponseResult.ResponseSuccess) serverResponse).getServerAddress();
                    int port =
                            ((JsonResponseResult.ResponseSuccess) serverResponse).getServerPort();
                    return new InetSocketAddress(address, port);
                } catch (IllegalStateException exception) {
                    throw new IllegalStateException(
                            "JSON response port out of range or hosname string is null");
                }
            } else if (serverResponse instanceof JsonResponseResult.ResponseNoFreeServers) {
                LogWrap.w(
                        TAG,
                        "No free server, waiting for " + ProtocolConstants.JSON_CONNECT_TIMEOUT_SEC + "s timeout"
                );
                sleepThread(ProtocolConstants.JSON_CONNECT_TIMEOUT_SEC);
            } else if (serverResponse instanceof JsonResponseResult.ResponseNoSuchID) {
                LogWrap.e(TAG, "No such ID's in database" + ProtocolConstants.JSON_CONNECT_TIMEOUT_SEC + "s timeout");
                sleepThread(ProtocolConstants.JSON_CONNECT_TIMEOUT_SEC);
                String response =
                        ((JsonResponseResult.ResponseNoSuchID) serverResponse).getResponse();
                String request =
                        ((JsonResponseResult.ResponseNoSuchID) serverResponse).getRequest();
                throw new SuchUserDataNotFound("Server not found app_id or partner_id. request=" + request + " response=" + response);
            } else if (serverResponse instanceof JsonResponseResult.ParseError) {
                String msg = ((JsonResponseResult.ParseError) serverResponse).getMsg();
                LogWrap.e(
                        TAG,
                        "Parse JSON with error. body=" + msg + ProtocolConstants.JSON_CONNECT_TIMEOUT_SEC + "s timeout"
                );
                sleepThread(ProtocolConstants.JSON_CONNECT_TIMEOUT_SEC);
            } else if (serverResponse instanceof JsonResponseResult.ResponseFailure) {
                int responseCode =
                        ((JsonResponseResult.ResponseFailure) serverResponse).getResponseCode();
                LogWrap.e(TAG, "Failed connect with JSON server, HTTP code=" + responseCode);
                sleepThread(ProtocolConstants.JSON_CONNECT_TIMEOUT_SEC);
            } else if (serverResponse instanceof JsonResponseResult.UnexpectedException) {
                Exception exc = ((JsonResponseResult.UnexpectedException) serverResponse).getExc();
                String msg = ((JsonResponseResult.UnexpectedException) serverResponse).getMsg();
                LogWrap.e(
                        TAG,
                        "Unexpected exception of json server connection, exc=" + exc + "msg=" + msg
                );
                sleepThread(ProtocolConstants.JSON_CONNECT_TIMEOUT_SEC);
            } else {
                LogWrap.e(
                        TAG,
                        "Proxy client failed by unexpected exception. Was connected to ip:" + serverAddr)
                ;
            }
        }

        return null;
    }

    private void sleepThread(long sleepSeconds) {
        try {
            LogWrap.d(TAG, "Thread has sleep for " + sleepSeconds + "s....");
            TimeUnit.SECONDS.sleep(sleepSeconds);
        } catch (InterruptedException e) {
            LogWrap.d(TAG, "Thread has interrupted");
            Thread.currentThread().interrupt();
        }
    }

    private boolean connectToProxyServer(
            @NonNull Selector selector,
            @NonNull InetSocketAddress address
    )
    {
        int connectAttempts = 1;
        while (connectAttempts <= 3)
        {
            LogWrap.d(TAG, connectAttempts + " attempt connect to server:");
            if (!serverChannel.connectToServer(address, selector))
            {
                LogWrap.w(TAG, "Failed connect to server, try again after " + ProtocolConstants.SRV_CONNECT_TIMEOUT_SEC + "s.");
                sleepThread(ProtocolConstants.SRV_CONNECT_TIMEOUT_SEC);
                connectAttempts++;
            }
            else
            {
                LogWrap.d(TAG, "Server is connected");
                return true;
            }
        }

        return false;
    }

    private void closeServer(short closeCause) {
        try {
            serverChannel.close(closeCause);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int select(Selector selector) {
        try {
            LogWrap.v(TAG, "select() has calles");
            int selected = selector.select(SELECTOR_TIMEOUT);
            LogWrap.d(TAG, "Selector has select, selected [" + selected + "] keys");
            return selected;
        } catch (ClosedSelectorException e) {
            LogWrap.e(TAG, "Failed to select keys. " + e.getMessage());
            return -1;
        } catch (IOException e) {
            LogWrap.e(TAG, "Failed to select keys. " + e.getMessage());
            return -1;
        }
    }

    private void handleKeys(Selector selector, Set<SelectionKey> keys) {
        LogWrap.v(TAG, "handleKeys()");
        Iterator<SelectionKey> itKeys = keys.iterator();

        while (itKeys.hasNext()) {
            try {
                SelectionKey key = itKeys.next();
                itKeys.remove();

                if (key.isValid() && key.isConnectable())
                    ((Channel) key.attachment()).connect(selector);
                else if (key.isValid() && key.isReadable())
                    ((Channel) key.attachment()).read();
                else if (key.isValid() && key.isWritable())
                    ((Channel) key.attachment()).write();
            } catch (IOException e) {
                LogWrap.e(TAG, "handleKeys(): " + e.getMessage());
            }
        }
    }


    private void loggingKeysState(Selector selector, Set<SelectionKey> keys) {
        StringBuilder sbLog = new StringBuilder("handleKeys() has called with key set:[");
        int keySize = selector.keys().size();
        int selectedKeySize = selector.selectedKeys().size();
        keys.forEach((SelectionKey selectionKey) -> {
            if (selectionKey != null && selectionKey.isValid()) {

                sbLog.append("inst=");
                sbLog.append(SelectorHelper.opsToString(selectionKey.interestOps())).append("/");

                sbLog.append("rdy=");
                sbLog.append(SelectorHelper.opsToString(selectionKey.readyOps())).append(":");

                if (selectionKey.attachment() instanceof HostChannel)
                    sbLog.append(HostChannel.class.getSimpleName()).append("_")
                            .append(((HostChannel) selectionKey.attachment()).getId());
                else
                    sbLog.append(ServerChannel.class.getSimpleName());
                sbLog.append(", ");

            } else {
                sbLog.append("null");
            }
        });
        sbLog.append("]");
        sbLog.append(keySize).append(" / ").append(selectedKeySize);
        LogWrap.d(TAG, sbLog.toString());
    }

    public boolean isStarted() {
        LogWrap.d(TAG, "isStarted() has called");
        return proxyThread != null && !proxyThread.isInterrupted();
    }

    private final DeviceNetworkState currentDeviceNetworkState;

    public static short RECONNECTION_CAUSE_FLAG = CauseReconnectionConsts.EMPTY_VALUE;

    public static void setCause(short causeFlag, boolean force) {
        String oldCause = CauseReconnectionConsts.causeToString(RECONNECTION_CAUSE_FLAG);
        String newCause = CauseReconnectionConsts.causeToString(causeFlag);

        if (force) {
            if (RECONNECTION_CAUSE_FLAG != causeFlag) {
                RECONNECTION_CAUSE_FLAG = causeFlag;
                LogWrap.v(
                        CauseReconnectionConsts.TAG,
                        "Swap reconnection cause: " + oldCause + " ==> " + newCause
                );
            }
        } else {
            if (RECONNECTION_CAUSE_FLAG != CauseReconnectionConsts.HANDLE_SRV_PACKET_FAILED
                    && RECONNECTION_CAUSE_FLAG != CauseReconnectionConsts.MARKER_SRV_NOT_FOUND
                    && RECONNECTION_CAUSE_FLAG != CauseReconnectionConsts.HANDLE_SRV_SENT_FAILED
                    && RECONNECTION_CAUSE_FLAG != CauseReconnectionConsts.HANDLE_SRV_RECV_FAILED
                    && RECONNECTION_CAUSE_FLAG != CauseReconnectionConsts.SRV_INVALID_PACKET) {
                RECONNECTION_CAUSE_FLAG = causeFlag;
                LogWrap.v(
                        CauseReconnectionConsts.TAG,
                        "Swap reconnection cause: " + oldCause + " ==> " + newCause
                );
            }
        }
    }

    public static short getCause() {
        return RECONNECTION_CAUSE_FLAG;
    }
}

enum NETWORK_STATE {
    CELLULAR, WIFI, NONE
}