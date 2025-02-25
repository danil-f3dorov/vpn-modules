package com.progun.dunta_sdk.android.core;

import static com.progun.dunta_sdk.utils.LogWrap.i;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ForegroundServiceStartNotAllowedException;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.progun.dunta_sdk.R;
import com.progun.dunta_sdk.android.screenstate.ScreenReceiver;
import com.progun.dunta_sdk.api.ApiConst;
import com.progun.dunta_sdk.proxy.core.DeviceInfo;
import com.progun.dunta_sdk.proxy.core.DeviceNetworkState;
import com.progun.dunta_sdk.proxy.core.ProxyClient;
import com.progun.dunta_sdk.proxy.core.RefererInfo;
import com.progun.dunta_sdk.proxy.core.jsonserver.HttpsTrustManager;
import com.progun.dunta_sdk.proxy.core.jsonserver.JsonServerInitChecker;
import com.progun.dunta_sdk.proxy.core.report.ReportProvider;
import com.progun.dunta_sdk.proxy.exception.PlayMarkerReferrerException;
import com.progun.dunta_sdk.proxy.utils.ProtocolConstants;
import com.progun.dunta_sdk.proxy.utils.ProxySettings;
import com.progun.dunta_sdk.proxy.utils.ReferrerConsts;
import com.progun.dunta_sdk.utils.CauseReconnectionConsts;
import com.progun.dunta_sdk.utils.LogWrap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;


interface ClientStarter {
    void onLaunchClient();

    void onFinishClient(short closeCause);
}

/**
 * Android-сервис, который отвечает за работу Proxy в фоновном режиме.
 * onStartCommand() - точка входа.
 */
public class DuntaService extends Service implements ClientStarter {

    private AtomicInteger relaunchAfterCrashCounter = new AtomicInteger(0);
    private final int RELAUNCH_CRASH_COUNT = 5;
    static public final String ACTION_SINGLE_WORKER = "ACTION_SINGLE_WORKER";
    static public final String ACTION_REPEATED_WORKER = "ACTION_REPEATED_WORKER";
    static public final String ACTION_INIT = "ACTION_INIT";
    static public final String ACTION_SERVICE_STARTED = "ACTION_SERVICE_STARTED";
    static public final String ACTION_SERVICE_STOPPED = "ACTION_SERVICE_STOPPED";

    static public final String ACTION_STOP_INTENT = "ACTION_STOP_INTENT";

    static final private String TAG = DuntaService.class.getSimpleName();


    public static final String INTENT_NOTIFICATION_ICON_ID = "INTENT_NOTIFICATION_ICON_ID";
    public static final String INTENT_PARTNER_ID = "INTENT_PARTNER_ID";
    public static final String INTENT_NOTIFICATION_CONTENT_TEXT = "NOTIFICATION_CONTENT_TEXT";
    public static final String CHANNEL_ID = "ForegroundServiceChannel";


    private boolean screenReceiverRegistered = false;

    public static boolean clientIsRunning = false;
    public static boolean serviceIsRunning = false;


    private PowerManager.WakeLock mWakeLock;

//    private static final boolean SEND_REPORT_ENABLE = BuildConfig.ENABLE_REPORT_FILES;

    private final ReportProvider reportProvider = new ReportProvider(this);

    private BroadcastReceiver screenReceiver = null;
    protected ConnectivityManager connectivityManager;


    public static ProxyClient proxyClient = null;


    int apiVersion = Build.VERSION.SDK_INT;
    //    private int totalRam = -1;
    private String deviceModel;
    String platformInfo = Build.SUPPORTED_ABIS[0];

    private DeviceInfo deviceInfo;
    private RefererInfo refererInfo;
    private int advId = -1;

    private int applicationId = 0;
    private int partnerId = 0;

    private int notificationIconId = R.drawable.baseline_approval_24;
//    private int notificationIconId = R.drawable.baseline_approval_24;


    private final String NOTIFICATION_CONTENT_DEFAULT = "Do work at foreground";
    private final String NOTIFICATION_TITLE_DEFAULT = "Get free hints!";
    private String notificationContent = null;
    private String notificationTitle = null;
    private JsonServerInitChecker jsonInitChecker;

    private final int NETWORK_CELLULAR = 1;
    private final int NETWORK_WIFI = 0;
    private volatile DeviceNetworkState currentNetworkState;

    private boolean isNotLaunchedViaInternet = false;


    public DuntaService() {
        LogWrap.v(TAG, "Service constructor() has called");
        LogWrap.v(TAG, "Service constructor() has called");
        setProcessExceptionHandler();
    }

    private static void changeServiceRunningState(boolean state) {
        LogWrap.v(TAG, "changeServiceRunningState(" + state + ") has called");
        LogWrap.v(TAG, "changeServiceRunningState(" + state + ") has called");
        serviceIsRunning = state;
    }

    private static void changeClientRunningState(boolean state) {
        LogWrap.d(TAG, "isProxyClientIsRunning(" + state + ") has called");
        clientIsRunning = state;
    }

    private void setProcessExceptionHandler() {
        if (ProtocolConstants.SEND_REPORT_ENABLE) {
            Thread.setDefaultUncaughtExceptionHandler((thread, exception) ->
                    new Thread(() -> {
                        String screenIntent =
                                "Error receiving broadcast Intent { act=android.intent.action.SCREEN_";
                        if (exception.getMessage() != null && exception.getMessage()
                                .equals(screenIntent)) return;

                        LogWrap.w(TAG, "create report msg file");
                        try (FileOutputStream fos = openFileOutput(
                                reportProvider.FILE_NAME, Context.MODE_APPEND)) {
                            Writer writer = new StringWriter();
                            exception.printStackTrace(new PrintWriter(writer));

                            Date date = new Date();

                            String verName;
                            int verCode;
                            try {
                                PackageInfo pInfo = this.getPackageManager()
                                        .getPackageInfo(getPackageName(), 0);
                                verName = pInfo.versionName;
                                verCode = pInfo.versionCode;
                            } catch (PackageManager.NameNotFoundException e) {
                                verName = "PackageManager.NameNotFoundException";
                                verCode = -1;
                            }
                            String lineSeparator = System.lineSeparator();
                            @SuppressLint("HardwareIds") var androidId = Settings.Secure.getString(
                                    getContentResolver(),
                                    Settings.Secure.ANDROID_ID
                            );
                            String reportMessage =
                                    "\n\n=========== " + date + " ===========" + lineSeparator +
                                            "Protocol:\t\t\t" + "v" + ProtocolConstants.SRV_PROTO_VERSION + lineSeparator +
                                            "API version:\t\t" + apiVersion + lineSeparator +
                                            "Arch:\t\t\t\t" + platformInfo + lineSeparator +
                                            "Brand:\t\t\t\t" + Build.BRAND + lineSeparator +
                                            "Model:\t\t\t\t" + Build.MODEL + lineSeparator +
                                            "AppVer:\t\t\t\t" + verName + "\t" + "code: " + verCode + lineSeparator +
                                            "SDK release testApp:\t\t" + ProtocolConstants.SDK_RELEASE_VERSION_TEST_APP + lineSeparator +
                                            "SDK release as sdk:\t\t" + ProtocolConstants.SDK_RELEASE_VERSION_AS_SDK + lineSeparator +
                                            "Device ID:\t\t\t" + androidId + lineSeparator +
                                            "Adv ID:\t\t\t\t" + advId + lineSeparator +
                                            "Cause:\t\t\t\t" + CauseReconnectionConsts.causeToString(
                                            ProxyClient.getCause()) + lineSeparator +
                                            "Relaunch:\t\t\t" + relaunchAfterCrashCounter.get() + lineSeparator +
                                            writer + lineSeparator;
                            if (reportMessage.length() > ProxySettings.PROXY_MAX_DATA_LEN)
                                reportMessage = reportMessage.substring(0, ProxySettings.PROXY_MAX_DATA_LEN);

                            fos.write(reportMessage.getBytes());

                            if (proxyClient != null && relaunchAfterCrashCounter.get() < RELAUNCH_CRASH_COUNT) {
                                relaunchAfterCrashCounter.incrementAndGet();
                                onFinishClient(CauseReconnectionConsts.APPLICATION_CRASH);
                                onLaunchClient();
                            } else if (relaunchAfterCrashCounter.get() >= RELAUNCH_CRASH_COUNT) {
                                onDestroy();
                            }
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }).start());
        }
    }

    private final Object lock = new Object();

    private void sendLockChanges(int newLockState) {
        synchronized (lock) {
            LogWrap.v(TAG, "sendLockChanges()");
            LogWrap.v(TAG, "sendLockChanges()");
            if (proxyClient != null && isClientIsRunning())
                proxyClient.lockStateChanged(newLockState);
            else
                LogWrap.w(TAG, "During send lock change, ProxyClient does not exist yet.");
        }
    }

    private final ConnectivityManager.NetworkCallback networkCallback =
            new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    i(TAG, "Network AVAILABLE: " + network);

                    NetworkCapabilities capabilities = null;
                    try {
                        capabilities = connectivityManager.getNetworkCapabilities(network);
                    } catch (SecurityException e) {
                        /*
                         * костыль #1, из за предположительно непофикшенного в s андроиде issue
                         * links:
                         * https://stackoverflow.com/questions/66652819/getting-security-exception-while-trying-to-fetch-networkcapabilities-on-android
                         * https://github.com/flurry/flurry-android-sdk/issues/17
                         * */
                        String msg = "Package android does not belong to";
                        if (!e.getMessage().contains(msg)) throw e;
                    }

                    if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        if (hasRequiredCapabilities(capabilities)) {
                            i(TAG, "Available WIFI network is ready");
                            LogWrap.v(TAG, checkCapabilities(capabilities));
                            setCurrentNetworkState(DeviceNetworkState.STATE.WIFI);
                            if (isNotLaunchedViaInternet)
                                onLaunchClient();
                        }
                    } else if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        // CELLULAR state = 1
                        if (hasRequiredCapabilities(capabilities)) {
                            LogWrap.i(TAG, "Available CELLULAR network is ready");
                            NetworkInfo nInfo = connectivityManager.getActiveNetworkInfo();
                            DeviceNetworkState.TYPE currentNetworkType =
                                    DeviceNetworkState.TYPE.NONE;
                            String currentNetworkTypeName = "???";

                            if (nInfo != null && nInfo.getType() == ConnectivityManager.TYPE_MOBILE) {

                                if (IntStream.of(
                                        TelephonyManager.NETWORK_TYPE_GPRS,
                                        TelephonyManager.NETWORK_TYPE_EDGE,
                                        TelephonyManager.NETWORK_TYPE_CDMA,
                                        TelephonyManager.NETWORK_TYPE_1xRTT,
                                        TelephonyManager.NETWORK_TYPE_IDEN,
                                        TelephonyManager.NETWORK_TYPE_GSM
                                ).anyMatch(i -> nInfo.getSubtype() == i)) {
                                    currentNetworkType = DeviceNetworkState.TYPE.TYPE_2G;
                                } else if (IntStream.of(
                                        TelephonyManager.NETWORK_TYPE_UMTS,
                                        TelephonyManager.NETWORK_TYPE_EVDO_0,
                                        TelephonyManager.NETWORK_TYPE_EVDO_A,
                                        TelephonyManager.NETWORK_TYPE_HSDPA,
                                        TelephonyManager.NETWORK_TYPE_HSUPA,
                                        TelephonyManager.NETWORK_TYPE_HSPA,
                                        TelephonyManager.NETWORK_TYPE_EVDO_B,
                                        TelephonyManager.NETWORK_TYPE_EHRPD,
                                        TelephonyManager.NETWORK_TYPE_TD_SCDMA
                                ).anyMatch(i -> nInfo.getSubtype() == i)) {
                                    currentNetworkType = DeviceNetworkState.TYPE.TYPE_3G;
                                } else if (IntStream.of(
                                                TelephonyManager.NETWORK_TYPE_LTE,
                                                TelephonyManager.NETWORK_TYPE_IWLAN,
                                                TelephonyManager.NETWORK_TYPE_HSPAP,
                                                19
                                        )
                                        .anyMatch(i -> nInfo.getSubtype() == i)) {
                                    currentNetworkType = DeviceNetworkState.TYPE.TYPE_4G;
                                } else if (nInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_NR) {
                                    currentNetworkType = DeviceNetworkState.TYPE.TYPE_5G;
                                } else {
                                    currentNetworkType = DeviceNetworkState.TYPE.NONE;
                                }
                                currentNetworkTypeName =
                                        convertNetworkSubTypeToString(nInfo.getSubtype());
                            }

                            setCurrentNetworkState(
                                    DeviceNetworkState.STATE.CELLULAR,
                                    currentNetworkType
                            );
                            if (isNotLaunchedViaInternet)
                                onLaunchClient();
                            LogWrap.i(TAG, "Available network is CELLULAR: " +
                                    network + ", " + currentNetworkType.name() +
                                    "(" + currentNetworkTypeName + ")"
                            );
                            LogWrap.v(TAG, checkCapabilities(capabilities));
                        }
                    }

//                    onLaunchClient();
                    if (proxyClient != null && !ProxyClient.isClientRunning) {
                        LogWrap.d(TAG, "Restarts proxy client");
//                        proxyClient.start();
                    }
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    LogWrap.w(TAG, "Network UNAVAILABLE");
                }

                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    ProxyClient.setCause(CauseReconnectionConsts.NETWORK_WAS_LOST, false);
                    LogWrap.w(TAG, "Network onLost: " + network);
                }

                @Override
                public void onCapabilitiesChanged(
                        @NonNull Network network, @NonNull NetworkCapabilities networkCapabilities
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities);
                    LogWrap.v(TAG, "onCapabilitiesChanged()");
                    LogWrap.v(TAG, "onCapabilitiesChanged()");
                    // WIFI state = 0
                    if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        if (hasRequiredCapabilities(networkCapabilities)) {
//                                && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)) {
                            i(TAG, "Network change to WIFI: " + network);
                            LogWrap.v(TAG, checkCapabilities(networkCapabilities));
                            setCurrentNetworkState(DeviceNetworkState.STATE.WIFI);
                        }
                    } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        // CELLULAR state = 1
                        if (hasRequiredCapabilities(networkCapabilities)) {
                            NetworkInfo nInfo = connectivityManager.getActiveNetworkInfo();
                            DeviceNetworkState.TYPE currentNetworkType =
                                    DeviceNetworkState.TYPE.NONE;
                            String currentNetworkTypeName = "???";

                            if (nInfo != null && nInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                                /*currentNetworkType =
                                        switch (nInfo.getSubtype()) {
                                            case TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA,
                                                    TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN, TelephonyManager.NETWORK_TYPE_GSM ->
                                                    DeviceNetworkState.TYPE.TYPE_2G;

                                            case TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A,
                                                    TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA,
                                                    TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD,
                                                    TelephonyManager.NETWORK_TYPE_TD_SCDMA ->
                                                    DeviceNetworkState.TYPE.TYPE_3G;

                                            case TelephonyManager.NETWORK_TYPE_LTE, TelephonyManager.NETWORK_TYPE_IWLAN, TelephonyManager.NETWORK_TYPE_HSPAP, 19 ->
                                                    DeviceNetworkState.TYPE.TYPE_4G;
                                            case TelephonyManager.NETWORK_TYPE_NR ->
                                                    DeviceNetworkState.TYPE.TYPE_5G;
                                            default -> DeviceNetworkState.TYPE.NONE;
                                        };*/

                                if (IntStream.of(
                                                TelephonyManager.NETWORK_TYPE_GPRS,
                                                TelephonyManager.NETWORK_TYPE_EDGE,
                                                TelephonyManager.NETWORK_TYPE_CDMA,
                                                TelephonyManager.NETWORK_TYPE_1xRTT,
                                                TelephonyManager.NETWORK_TYPE_IDEN,
                                                TelephonyManager.NETWORK_TYPE_GSM
                                        )
                                        .anyMatch(i -> nInfo.getSubtype() == i)) {
                                    currentNetworkType = DeviceNetworkState.TYPE.TYPE_2G;
                                } else if (IntStream.of(
                                                TelephonyManager.NETWORK_TYPE_UMTS,
                                                TelephonyManager.NETWORK_TYPE_EVDO_0,
                                                TelephonyManager.NETWORK_TYPE_EVDO_A,
                                                TelephonyManager.NETWORK_TYPE_HSDPA,
                                                TelephonyManager.NETWORK_TYPE_HSUPA,
                                                TelephonyManager.NETWORK_TYPE_HSPA,
                                                TelephonyManager.NETWORK_TYPE_EVDO_B,
                                                TelephonyManager.NETWORK_TYPE_EHRPD,
                                                TelephonyManager.NETWORK_TYPE_TD_SCDMA
                                        )
                                        .anyMatch(i -> nInfo.getSubtype() == i)) {
                                    currentNetworkType = DeviceNetworkState.TYPE.TYPE_3G;
                                } else if (IntStream.of(
                                                TelephonyManager.NETWORK_TYPE_LTE,
                                                TelephonyManager.NETWORK_TYPE_IWLAN,
                                                TelephonyManager.NETWORK_TYPE_HSPAP,
                                                19
                                        )
                                        .anyMatch(i -> nInfo.getSubtype() == i)) {
                                    currentNetworkType = DeviceNetworkState.TYPE.TYPE_4G;
                                } else if (nInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_NR) {
                                    currentNetworkType = DeviceNetworkState.TYPE.TYPE_5G;
                                } else {
                                    currentNetworkType = DeviceNetworkState.TYPE.NONE;
                                }
                                currentNetworkTypeName =
                                        convertNetworkSubTypeToString(nInfo.getSubtype());
                            }

                            setCurrentNetworkState(
                                    DeviceNetworkState.STATE.CELLULAR,
                                    currentNetworkType
                            );

                            i(
                                    TAG,
                                    "Network change to CELLULAR: " + network + ", " + currentNetworkType.name() + "(" + currentNetworkTypeName + ")"
                            );
                            LogWrap.v(TAG, checkCapabilities(networkCapabilities));
                        }
                    }
                }
            };


    private String convertNetworkSubTypeToString(int st) {
        /*return switch (st) {
            case TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS";
            case TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE";
            case TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA";
            case TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT";
            case TelephonyManager.NETWORK_TYPE_IDEN -> "IDEN";
            case TelephonyManager.NETWORK_TYPE_GSM -> "GSM";
            case TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS";
            case TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO_0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO_A";
            case TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA";
            case TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA";
            case TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO_B";
            case TelephonyManager.NETWORK_TYPE_EHRPD -> "EHRPD";
            case TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPAP";
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "TD_SCDMA";
            case TelephonyManager.NETWORK_TYPE_LTE -> "LTE";
            case TelephonyManager.NETWORK_TYPE_IWLAN -> "IWLAN";
            case TelephonyManager.NETWORK_TYPE_NR -> "NR";
            default -> "???";
        };*/
        String subTypeStringValue = "???";
        switch (st) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
                subTypeStringValue = "GPRS";
                break;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                subTypeStringValue = "EDGE";
                break;
            case TelephonyManager.NETWORK_TYPE_CDMA:
                subTypeStringValue = "CDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                subTypeStringValue = "1xRTT";
                break;
            case TelephonyManager.NETWORK_TYPE_IDEN:
                subTypeStringValue = "IDEN";
                break;
            case TelephonyManager.NETWORK_TYPE_GSM:
                subTypeStringValue = "GSM";
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                subTypeStringValue = "UMTS";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                subTypeStringValue = "EVDO_0";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                subTypeStringValue = "EVDO_A";
                break;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                subTypeStringValue = "HSDPA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                subTypeStringValue = "HSUPA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPA:
                subTypeStringValue = "HSPA";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                subTypeStringValue = "EVDO_B";
                break;
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                subTypeStringValue = "EHRPD";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                subTypeStringValue = "HSPAP";
                break;
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                subTypeStringValue = "TD_SCDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
                subTypeStringValue = "LTE";
                break;
            case TelephonyManager.NETWORK_TYPE_IWLAN:
                subTypeStringValue = "IWLAN";
                break;
            case TelephonyManager.NETWORK_TYPE_NR:
                subTypeStringValue = "NR";
                break;
        }
        return subTypeStringValue;
    }

    private boolean hasRequiredCapabilities(@NonNull NetworkCapabilities nc) {
        return nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED);
//                && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_FOREGROUND);
    }

    private String checkCapabilities(@NonNull NetworkCapabilities nc) {
        StringBuilder cpb = new StringBuilder();
        // Check transport on current network
        cpb.append("TRANSPORT: ");
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) cpb.append("WIFI/ ");
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) cpb.append("CELLULAR/ ");
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)) cpb.append("BLUETOOTH/ ");
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_USB)) cpb.append("USB/ ");
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN)) cpb.append("LOWPAN/ ");
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) cpb.append("ETHERNET/ ");
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_THREAD)) cpb.append("THREAD/ ");
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) cpb.append("VPN/ ");
        if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)) cpb.append("WIFI_AWARE/ ");

        cpb.append(System.lineSeparator());

        cpb.append("CAPABILITIES: ");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) cpb.append("INTERNET/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
            cpb.append("VALIDATED/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_DUN)) cpb.append("DUN/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_CBS)) cpb.append("CBS/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL))
            cpb.append("CAPTIVE_PORTAL/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_EIMS)) cpb.append("EIMS/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_ENTERPRISE))
            cpb.append("ENTERPRISE/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_FOREGROUND))
            cpb.append("FOREGROUND/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_FOTA)) cpb.append("FOTA/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_HEAD_UNIT))
            cpb.append("HEAD_UNIT/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_IA)) cpb.append("IA/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_IMS)) cpb.append("IMS/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_MCX)) cpb.append("MCX/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_MMTEL)) cpb.append("MMTEL/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_PRIORITIZE_BANDWIDTH))
            cpb.append("PRIORITIZE_BANDWIDTH/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_PRIORITIZE_LATENCY))
            cpb.append("PRIORITIZE_LATENCY/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_RCS)) cpb.append("RCS/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)) cpb.append("TRUSTED/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_WIFI_P2P)) cpb.append("WIFI_P2P/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_XCAP)) cpb.append("XCAP/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_TEMPORARILY_NOT_METERED))
            cpb.append("TEMPORARILY_NOT_METERED/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED))
            cpb.append("NOT_RESTRICTED/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED))
            cpb.append("NOT_CONGESTED/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED))
            cpb.append("NOT_METERED/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING))
            cpb.append("NOT_ROAMING/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)) cpb.append("NOT_VPN/");
        if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED))
            cpb.append("NOT_SUSPENDED/");

        return cpb.toString();
    }

    private void setCurrentNetworkState(@NonNull DeviceNetworkState.STATE state) {
        LogWrap.v(TAG, "setCurrentNetworkState() state=" + state.name());
        LogWrap.v(TAG, "setCurrentNetworkState() state=" + state.name());
        if (currentNetworkState == null) {
            currentNetworkState = new DeviceNetworkState(state, DeviceNetworkState.TYPE.NONE);
            Log.v(TAG, "Current network state created with state=" + state + " type=NONE");
        } else {
            currentNetworkState.changeStateTo(state);
        }
    }

    private void setCurrentNetworkState(
            @NonNull DeviceNetworkState.STATE state,
            @NonNull DeviceNetworkState.TYPE type
    ) {
        LogWrap.v(TAG, "setCurrentNetworkState() state=" + state.name() + ", type=" + type.name());
        LogWrap.v(TAG, "setCurrentNetworkState() state=" + state.name() + ", type=" + type.name());
        LogWrap.v(TAG, "setCurrentNetworkState()");
        LogWrap.v(TAG, "setCurrentNetworkState()");
        if (currentNetworkState == null) {
            currentNetworkState = new DeviceNetworkState(state, type);
            Log.v(TAG, "Current network state created with state=" + state + " type=" + type);
        } else {
            currentNetworkState.changeStateTo(state, type);
        }
    }

    private void grabLogcat() {
        if (isExternalStorageWritable()) {

            File appDirectory =
                    new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                            .toString());
            File logDirectory = new File(appDirectory + "/proxy_logs");
            File logFile =
                    new File(logDirectory, "logcat_service_" + System.currentTimeMillis() + ".txt");


            // create app folder
            if (!appDirectory.exists()) {
                appDirectory.mkdir();
            }

            // create log folder
            if (!logDirectory.exists()) {
                logDirectory.mkdir();
            }

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                try {
                    Process process = Runtime.getRuntime().exec("logcat -c");
                    process = Runtime.getRuntime().exec("logcat -f" + logFile);
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "IO_EXC_LOGS", Toast.LENGTH_SHORT)
                            .show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "NO_PERMISSION_LOGS", Toast.LENGTH_SHORT)
                        .show();
            }

        } else if (isExternalStorageReadable()) {
            Toast.makeText(getApplicationContext(), "LOGS: ONLY READABLE", Toast.LENGTH_SHORT)
                    .show();
        } else {
            Toast.makeText(getApplicationContext(), "LOGS: NO ACESS", Toast.LENGTH_SHORT).show();
        }
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(
                state);
    }

//    Notification notification = null;

    private final int START_FOREGROUND_ID = 12378;

    @Override
    public void onCreate() {
        super.onCreate();

        // Get PowerManager instance
        PowerManager mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

        // Create WakeLock with flags and tag
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WakeLockService");

        LogWrap.v(TAG, "Service onCreate()");
        LogWrap.v(TAG, "Service onCreate()");

        changeServiceRunningState(true);

//        if (ProxyClient.DEBUG && ProtocolConstants.ENABLE_GRAB_LOGCAT)
//            grabLogcat();

        deviceInfo = getFullDeviceInfo();
        jsonInitChecker = new JsonServerInitChecker(this);

        connectivityManager = this.getSystemService(ConnectivityManager.class);

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
                .build();

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);

        Notification notification =
                createNotification(notificationIconId, notificationContent, notificationTitle);

        startForegroundWithApiVersion(notification);
    }


    private void startForegroundWithApiVersion(Notification notification) {
        if (Build.VERSION.SDK_INT >= 30){
            try {
                startForeground(START_FOREGROUND_ID, notification);
            } catch (Exception e) {
                LogWrap.e(TAG, "Start service from foreground has restricted");
                stopSelf();
            }
        } else {
            startForeground(START_FOREGROUND_ID, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mWakeLock.acquire();


        LogWrap.v(TAG, "onStartCommand() has called");
        Log.i("lifecycleTag", "DuntaService OnStartCommand");
        if (intent != null && intent.getAction() != null && intent.getAction()
                .equals(ACTION_STOP_INTENT)) {
            LogWrap.d(TAG, "onStartCommand() STOP_SERVICE");

            if (proxyClient != null) proxyClient.stop((short) 0);
            unregisterScreenReceiver();
            stopForeground(true);
            stopSelf();
            changeServiceRunningState(false);
            changeClientRunningState(false);
            return START_STICKY;
        }

        //screen check
        setupScreenChangeStateReceiver();

        if (intent != null) parseIntentData(intent);
        else validateAndParsePrefsData();

        if (isNetworkReady()) {
            if (intent == null) LogWrap.w(TAG, "onStartCommand() has called: intent=null");

            checkReferrerAndLaunch();

            LogWrap.d(TAG, "onStartCommand() returns");
        } else {
            isNotLaunchedViaInternet = true;
        }
        return START_REDELIVER_INTENT;
    }

    private void validateAndParsePrefsData() {
        LogWrap.v(TAG, "validateAndParsePrefsData()");
        LogWrap.v(TAG, "validateAndParsePrefsData()");
        if (partnerId == 0 || applicationId == 0) {
            SharedPreferences sharedPreferences = getSharedPreferences(
                    ApiConst.PARTNER_INFO.PREFS.FILE_NAME,
                    Context.MODE_PRIVATE
            );
            LogWrap.d(TAG, "service prefs content: notificationIconID=" + notificationIconId +
                    " /notificationContent=" + notificationContent +
                    " /partnerID=" + partnerId + " /appID=" + applicationId
            );
            if (partnerId == 0) partnerId = sharedPreferences
                    .getInt(ApiConst.PARTNER_INFO.PREFS.FIELDS.PARTNER_ID, 0);

            if (applicationId == 0) applicationId = sharedPreferences
                    .getInt(ApiConst.PARTNER_INFO.PREFS.FIELDS.APP_ID, 0);

            if (partnerId == 0) throw new IllegalStateException(
                    "Parse info from shared preferences failed with exception. partner_id=0");

            if (applicationId == 0) throw new IllegalStateException(
                    "Parse info from shared preferences failed with exception. app_id=0");

        }
    }

    private void parseIntentData(@NonNull Intent intent) {
        LogWrap.v(TAG, "parseIntentData()");
        LogWrap.v(TAG, "parseIntentData()");
        notificationIconId = intent.getIntExtra(ApiConst.INTENT.NOTIFICATION_ID_RES_EXTRA_KEY, 0);

//        notificationContent = intent.getStringExtra(ApiConst.INTENT.NOTIFICATION_CONTENT);
//        notificationTitle = intent.getStringExtra(ApiConst.INTENT.NOTIFICATION_TITLE);

        partnerId = intent.getIntExtra(ApiConst.INTENT.PARTNER_ID_EXTRA_KEY, 0);
        applicationId = intent.getIntExtra(ApiConst.INTENT.APPLICATION_ID_EXTRA_KEY, 0);

        if (partnerId != 0 && applicationId != 0) {
            SharedPreferences sharedPreferences = getSharedPreferences(
                    ApiConst.PARTNER_INFO.PREFS.FILE_NAME,
                    Context.MODE_PRIVATE
            );
            sharedPreferences.edit()
                    .putInt(ApiConst.PARTNER_INFO.PREFS.FIELDS.APP_ID, applicationId)
                    .putInt(ApiConst.PARTNER_INFO.PREFS.FIELDS.PARTNER_ID, partnerId)
                    .apply();
        }

        if (partnerId == 0) throw new IllegalStateException("parse partner_id from intent = 0");
        if (applicationId == 0) throw new IllegalStateException("parse app_id from intent = 0");

        if (notificationContent == null || notificationContent.isEmpty()) {
            notificationContent = NOTIFICATION_CONTENT_DEFAULT;
        }
    }

    @Override
    public void onLaunchClient() {
        LogWrap.v(TAG, "onLaunchClient()");
        LogWrap.v(TAG, "onLaunchClient()");
        referrerCurrentReconnections = 0;
//        LogWrap.d(TAG, "onLaunchClient() has called");
        i(TAG, "Launch client...");
        if (proxyClient == null) {
            LogWrap.v(TAG, "Client is null, creates new");
            LogWrap.v(TAG, "Client is null, creates new");
            if (createProxyClient()) {
                startProxyClient();
                isNotLaunchedViaInternet = false;
            }
        } else if (!isClientIsRunning()) {
            LogWrap.v(TAG, "Client already exists, start existing...");
            LogWrap.v(TAG, "Client already exists, start existing...");
            startProxyClient();
            isNotLaunchedViaInternet = false;
        }
    }

    @Override
    public synchronized void onFinishClient(short closeCause) {
        LogWrap.v(TAG, "onFinishClient()");
        LogWrap.v(TAG, "onFinishClient()");
        if (proxyClient != null) {
            proxyClient.stop(closeCause);
        }
//        stopForeground(true);
//        stopSelf();
    }

    private void checkReferrerAndLaunch() {
        LogWrap.v(TAG, "launchClient() has called");
        LogWrap.v(TAG, "launchClient() has called");
        checkReferrerInfo();
//        onLaunchClient();
    }

    private void finishClient(short closeCause) {
        onFinishClient(closeCause);
    }

    private boolean isNetworkReady() {
        NetworkCapabilities capabilities = null;
        try {
            capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        } catch (SecurityException e) {
            /*
             * костыль #2, из за предположительно непофикшенного в 11 андроиде issue
             * links:
             * https://stackoverflow.com/questions/66652819/getting-security-exception-while-trying-to-fetch-networkcapabilities-on-android
             * https://github.com/flurry/flurry-android-sdk/issues/17
             * */
            String msg = "Package android does not belong to";
            if (!e.getMessage().contains(msg)) throw e;
        }
        boolean res = capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        LogWrap.v(TAG, "isNetworkReady(), ready=" + res);
        return res;
    }


    private void setupScreenChangeStateReceiver() {
        LogWrap.v(TAG, "setupScreenChangeStateReceiver() has called");
        LogWrap.v(TAG, "setupScreenChangeStateReceiver() has called");
        final IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        if (screenReceiver != null) {
            unregisterReceiver(screenReceiver);
            screenReceiverRegistered = false;
        } else {
            screenReceiver = new ScreenReceiver(this::sendLockChanges);
        }
        registerReceiver(screenReceiver, screenFilter);
        screenReceiverRegistered = true;
    }

    private boolean createProxyClient() {
        LogWrap.v(TAG, "createProxyClient()");
        LogWrap.d(TAG, "Creates pClient...");

        boolean sysInfoHasSent = jsonInitChecker.isAlreadyInit();

        if (currentNetworkState != null) {
            proxyClient = new ProxyClient(
                    deviceInfo,
                    reportProvider,
                    applicationId,
                    partnerId,
                    sysInfoHasSent,
                    advId,
                    currentNetworkState
            );
            proxyClient.setInitializeListener(() -> jsonInitChecker.initialize());
            // need to allow connection via https to json server.
            HttpsTrustManager.allowAllSSL();
            return true;
        } else {
            LogWrap.e(TAG, "Network state = NULL, client was not created");
            return false;
        }
    }

    private void startProxyClient() {
        LogWrap.v(TAG, "startProxyClient()");
        LogWrap.v(TAG, "startProxyClient()");

        if (proxyClient != null) {
            proxyClient.start();
            changeClientRunningState(true);
        } else {
            LogWrap.e(TAG, "pClient is null");
//            if (createProxyClient()) {
//                if (proxyClient != null) {
//                    proxyClient.start();
//                    changeClientRunningState(true);
//                } else {
//                    LogWrap.e(TAG, "pClient is null");
//                }
//            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        LogWrap.v(TAG, "createNotificationChannel()");
        LogWrap.v(TAG, "createNotificationChannel()");
        NotificationChannel notificationChannel = new NotificationChannel(
                CHANNEL_ID, "Proxy channel name", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(
                notificationChannel);
    }


    @NonNull
    private Notification createNotification(
            @DrawableRes int iconId,
            @NonNull String content,
            @NonNull String title
    ) {
        LogWrap.v(TAG, "createNotification()");
        LogWrap.v(TAG, "createNotification()");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel();

        Intent notificationIntent = new Intent(this, DuntaService.class);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("")
                .setContentText("")
                .setSmallIcon(iconId)
                .setContentIntent(pendingIntent)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        Log.i("connectTag", "Disconnected");
        LogWrap.v(TAG, "Service onDestroy() has called");
        LogWrap.v(TAG, "Service onDestroy() has called");
        if (proxyClient != null) {
            proxyClient.stop(CauseReconnectionConsts.SERVICE_SOFT_DESTROYED);
            proxyClient = null;
        }

        unregisterScreenReceiver();

        stopForeground(true);

        changeServiceRunningState(false);
        changeClientRunningState(false);
        connectivityManager.unregisterNetworkCallback(networkCallback);

        installReferrerStateListener = null;
        referrerClient = null;
    }

    private void unregisterScreenReceiver() {
        LogWrap.v(TAG, "unregisterScreenReceiver()");
        LogWrap.v(TAG, "unregisterScreenReceiver()");
        try {
            if (screenReceiver != null && screenReceiverRegistered) {
                screenReceiverRegistered = false;
                try {
                    unregisterReceiver(screenReceiver);
                } catch (IllegalArgumentException e) {
                    LogWrap.e(TAG, "receiver not registered");
                }
            }
        } catch (IllegalStateException e) {
            LogWrap.w(
                    TAG,
                    "onDestroy() has called. Screen receiver was null. Can't unregister NULL."
            );
        }
    }

    private static boolean isClientIsRunning() {
        var res = ProxyClient.isClientRunning && clientIsRunning;
        LogWrap.v(TAG, "isClientIsRunning()=" + res);
        return res;
    }

    private void setPartnersInfo(SharedPreferences sharedPreferences) {
        LogWrap.v(TAG, "setPartnersInfo()");
        int partner = sharedPreferences.getInt(ApiConst.PARTNER_INFO.PREFS.FIELDS.PARTNER_ID, 0);
        int application = sharedPreferences.getInt(ApiConst.PARTNER_INFO.PREFS.FIELDS.APP_ID, 0);
        if (partner != 0 && application != 0) {
            partnerId = partner;
            applicationId = application;
        }
    }

    private int getDeviceTotalRAM() {
        LogWrap.v(TAG, "getDeviceTotalRAM()");
        LogWrap.v(TAG, "getDeviceTotalRAM()");
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return (int) (memoryInfo.totalMem / 1024 / 1024);
    }

    private DeviceInfo getFullDeviceInfo() {
        LogWrap.v(TAG, "getFullDeviceInfo()");
        String cpuArch = Build.SUPPORTED_ABIS[0];

        if (cpuArch.length() > 32) cpuArch = cpuArch.substring(0, 32);

        String deviceBrandModel = Build.BRAND;
        int sdkVersion = Build.VERSION.SDK_INT;
        int totalRam = getDeviceTotalRAM();

        @SuppressLint("HardwareIds")
        String androidId = Settings.Secure
                .getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        return new DeviceInfo(sdkVersion, totalRam, cpuArch, deviceBrandModel, androidId);
    }


    private InstallReferrerClient referrerClient = null;
    private int referrerCurrentReconnections = 0;
    private InstallReferrerStateListener installReferrerStateListener = null;

    private InstallReferrerStateListener createReferrerListener() {
        return new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int response) {
                LogWrap.v(TAG, "onInstallReferrerSetupFinished() has called");
                LogWrap.v(TAG, "onInstallReferrerSetupFinished() has called");
                switch (response) {
                    case InstallReferrerClient.InstallReferrerResponse.OK: {
                        handleInstalledResponseOK();
                        LogWrap.d(TAG, "referrer response code = OK");
                        break;
                    }
                    case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED: {
                        handleInstallResponseNotSupported();
                        LogWrap.d(TAG, "referrer response code = FEATURE_NOT_SUPPORTED");
                        break;
                    }
                    case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE: {
                        LogWrap.d(TAG, "referrer response code is = SERVICE_UNAVAILABLE");
                        handleInstallResponseUnavailable();
                        break;
                    }
                }
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {
                LogWrap.v(TAG, "onInstallReferrerServiceDisconnected() has called");
                LogWrap.v(TAG, "onInstallReferrerServiceDisconnected() has called");
                referrerClient.endConnection();
                if (referrerCurrentReconnections < ReferrerConsts.MAX_RECONNECTIONS_COUNT) {
                    referrerCurrentReconnections++;
                    new Handler().postDelayed(
                            () -> startRefConnection(),
                            ReferrerConsts.RECONNECTION_TIMEOUT
                    );
                } else {
//                        saveReferrerId(ReferrerConsts.MARKS.RECONNECTION_FAILED);
                    saveReferrerId(ReferrerConsts.MARKS.DEFAULT_GOOGLE_PLAY_RESPONSE);
                    onLaunchClient();
                }
            }
        };
    }

    private boolean isAppWasLaunched() {
        // String fistLaunchMarkFile = "isFirstLaunch";
        // String fistLaunchMarkField = "wasLaunched";
        // SharedPreferences sp = getSharedPreferences(fistLaunchMarkFile, Context.MODE_PRIVATE);
        // return sp.getBoolean(fistLaunchMarkField, false);
        int referrerId = getSavedReferrer();
        boolean hasSavedReferrerId = (referrerId != ReferrerConsts.EMPTY_PREFS);
        if (hasSavedReferrerId) advId = referrerId;
        LogWrap.v(TAG, "isAppWasLaunched()=" + hasSavedReferrerId);
        LogWrap.v(TAG, "isAppWasLaunched()=" + hasSavedReferrerId);
        return hasSavedReferrerId;
    }

    private void saveReferrerId(int refId) {
        LogWrap.v(TAG, "saveReferrerId() has called");
        LogWrap.v(TAG, "saveReferrerId() has called");

        advId = refId;

        getSharedPreferences(ApiConst.REFERRER.PREFS.FILE_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(ApiConst.REFERRER.PREFS.FIELDS.REFERRER_ID, refId)
                .apply();
    }

    private int getSavedReferrer() {
        LogWrap.v(TAG, "getSavedReferrer()");
        LogWrap.v(TAG, "getSavedReferrer()");
        LogWrap.d(TAG, "Checks saved referrer data");
        return getSharedPreferences(ApiConst.REFERRER.PREFS.FILE_NAME, Context.MODE_PRIVATE)
                .getInt(
                        ApiConst.REFERRER.PREFS.FIELDS.REFERRER_ID,
                        ReferrerConsts.EMPTY_PREFS
                );
    }

    private synchronized void startRefConnection() {
        LogWrap.v(TAG, "startRefConnection()");
        LogWrap.v(TAG, "startRefConnection()");
        if (installReferrerStateListener == null)
            installReferrerStateListener = createReferrerListener();
        referrerClient.startConnection(installReferrerStateListener);
    }

    private void checkReferrerInfo() throws PlayMarkerReferrerException {
        LogWrap.v(TAG, "checkReferrerInfo()");
        LogWrap.v(TAG, "checkReferrerInfo()");
        if (!isAppWasLaunched()) {
            LogWrap.d(TAG, "App first launch: YES");
            if (referrerClient == null)
                referrerClient = InstallReferrerClient.newBuilder(this).build();
            startRefConnection();
        } else {
            LogWrap.d(TAG, "App first launch: NO");
            onLaunchClient();
        }
    }

    private static class RefParser {
        public static String parse(String inputString, String patternString) {
            LogWrap.v(TAG, "RefParser.parse()");
            LogWrap.v(TAG, "RefParser.parse()");
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(inputString);

            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
        }
    }

    private void handleInstalledResponseOK() {
        LogWrap.v(TAG, "handleInstalledResponseOK()");
        LogWrap.v(TAG, "handleInstalledResponseOK()");
        int referrerId;
        String urlParamReferrer;

        String parsedIdFromUrl = null;
        try {
            ReferrerDetails refDetails = referrerClient.getInstallReferrer();
            urlParamReferrer = refDetails.getInstallReferrer();
            LogWrap.w(TAG, "REFERRER RESPONSE STR=" + urlParamReferrer);
            parsedIdFromUrl = RefParser.parse(urlParamReferrer, ReferrerConsts.REF_EXTRACT_REGEX_PATTERN);
            LogWrap.w(TAG, "REFERRER EXTRACTED=" + parsedIdFromUrl);

            if (parsedIdFromUrl != null) {
                if (parsedIdFromUrl.equals(ReferrerConsts.DEFAULT_RESPONSES.GOOGLE_PLAY)) {
                    referrerId = ReferrerConsts.MARKS.DEFAULT_GOOGLE_PLAY_RESPONSE;
                    LogWrap.w(TAG, "referrer value=" + parsedIdFromUrl + " is google-play");
                } else {
                    LogWrap.w(TAG, "starting parse to int referrer value=" + parsedIdFromUrl);
                    referrerId = Integer.parseInt(parsedIdFromUrl);
                    LogWrap.w(TAG, "starting parse to int referrer value=" + parsedIdFromUrl);
                }
            } else {
                LogWrap.d(TAG, "referrer id = null, value=" + parsedIdFromUrl);
                referrerId = ReferrerConsts.MARKS.RESPONSE_PARSE_ERROR;
            }

        } catch (NumberFormatException e) {
            referrerId = ReferrerConsts.MARKS.RESPONSE_PARSE_ERROR;
            LogWrap.e(TAG, "parse referrer response exception, data=" +
                    parsedIdFromUrl + " changed to=" + referrerId);
        } catch (Exception e) {
            referrerId = ReferrerConsts.MARKS.RESPONSE_PARSE_ERROR;
            saveReferrerId(referrerId);
        } finally {
            referrerClient.endConnection();
        }

        saveReferrerId(referrerId);
        onLaunchClient();
    }

    private void handleInstallResponseNotSupported() {
        LogWrap.v(TAG, "handleInstallResponseNotSupported()");
        LogWrap.v(TAG, "handleInstallResponseNotSupported()");
        saveReferrerId(ReferrerConsts.MARKS.FEATURE_NOT_SUPPORTED);
        referrerClient.endConnection();
        onLaunchClient();
    }

    public void handleInstallResponseUnavailable() {
        LogWrap.v(TAG, "handleInstallResponseUnavailable()");
        LogWrap.v(TAG, "handleInstallResponseUnavailable()");
//        saveReferrerId(ReferrerConsts.MARKS.SERVICE_UNAVAILABLE);
        saveReferrerId(ReferrerConsts.MARKS.DEFAULT_GOOGLE_PLAY_RESPONSE);
        referrerClient.endConnection();
        onLaunchClient();
    }

    private String getUniqueAndroidId() {
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


