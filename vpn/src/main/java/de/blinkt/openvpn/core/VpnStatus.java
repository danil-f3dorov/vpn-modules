/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Message;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Objects;
import java.util.Vector;

import openvpn.R;


public class VpnStatus {
    public static LinkedList<LogItem> logbuffer;

    private static final Vector<LogListener> logListener;
    private static final Vector<StateListener> stateListener;
    private static final Vector<ByteCountListener> byteCountListener;

    private static String mLaststatemsg = "";

    private static String mLaststate = "NO_PROCESS";

    private static int mLastStateresid = R.string.state_noprocess;

    private static long[] mlastByteCount = {0, 0, 0, 0};
    private static HandlerThread mHandlerThread;

    public static void logException(LogLevel ll, String context, Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        LogItem li;
        if (context != null) {
            li = new LogItem(ll, R.string.unhandled_exception_context, e.getMessage(), sw.toString(), context);
        } else {
            li = new LogItem(ll, R.string.unhandled_exception, e.getMessage(), sw.toString());
        }
        newLogItem(li);
    }

    public static void logException(Exception e) {
        logException(LogLevel.ERROR, null, e);
    }

    public static void logException(String context, Exception e) {
        logException(LogLevel.ERROR, context, e);
    }

    static final int MAXLOGENTRIES = 1000;

    public static boolean isVPNActive() {
        return mLastLevel != ConnectionState.LEVEL_AUTH_FAILED && !(mLastLevel == ConnectionState.LEVEL_NOT_CONNECTED);
    }

    public static String getLastCleanLogMessage(Context c) {
        String message = mLaststatemsg;
        if (Objects.requireNonNull(mLastLevel) == ConnectionState.LEVEL_CONNECTED) {
            String[] parts = mLaststatemsg.split(",");
                /*
                   (a) the integer unix date/time,
                   (b) the state name,
                   0 (c) optional descriptive string (used mostly on RECONNECTING
                    and EXITING to show the reason for the disconnect),

                    1 (d) optional TUN/TAP local IPv4 address
                   2 (e) optional address of remote server,
                   3 (f) optional port of remote server,
                   4 (g) optional local address,
                   5 (h) optional local port, and
                   6 (i) optional TUN/TAP local IPv6 address.
*/
            // Return only the assigned IP addresses in the UI
            if (parts.length >= 7)
                message = String.format(Locale.US, "%s %s", parts[1], parts[6]);
        }

        while (message.endsWith(","))
            message = message.substring(0, message.length() - 1);

        String status = mLaststate;
        if (status.equals("NO_PROCESS"))
            return message;

        if (mLastStateresid == R.string.state_waitconnectretry) {
            return c.getString(R.string.state_waitconnectretry, mLaststatemsg);
        }

        String prefix = c.getString(mLastStateresid);
        if (mLastStateresid == R.string.unknown_state)
            message = status + message;
        if (message.length() > 0)
            prefix += ": ";

        return prefix + message;

    }

    public static void initLogCache(File cacheDir) {
        mHandlerThread = new HandlerThread("LogFileWriter", Thread.MIN_PRIORITY);
        mHandlerThread.start();
        mLogFileHandler = new LogFileHandler(mHandlerThread.getLooper());


        Message m = mLogFileHandler.obtainMessage(LogFileHandler.LOG_INIT, cacheDir);
        mLogFileHandler.sendMessage(m);

    }

    public static void flushLog() {
        if (mLogFileHandler != null)
            mLogFileHandler.sendEmptyMessage(LogFileHandler.FLUSH_TO_DISK);
    }

    public enum ConnectionState {
        LEVEL_CONNECTED,
        LEVEL_VPN_PAUSED,
        LEVEL_CONNECTING_SERVER_REPLIED,
        LEVEL_CONNECTING_NO_SERVER_REPLY_YET,
        LEVEL_NO_NETWORK,
        LEVEL_NOT_CONNECTED,
        LEVEL_START,
        LEVEL_AUTH_FAILED,
        LEVEL_WAITING_FOR_USER_INPUT,
        UNKNOWN_LEVEL
    }

    public enum LogLevel {
        INFO(2),
        ERROR(-2),
        WARNING(1),
        VERBOSE(3),
        DEBUG(4);

        private final int mValue;

        LogLevel(int value) {
            mValue = value;
        }

        public int getInt() {
            return mValue;
        }

        public static LogLevel getEnumByValue(int value) {
            return switch (value) {
                case 1 -> INFO;
                case 2 -> ERROR;
                case 3 -> WARNING;
                case 4 -> DEBUG;
                default -> null;
            };
        }
    }

    // keytool -printcert -jarfile de.blinkt.openvpn_85.apk
    public static final byte[] officalkey = {-58, -42, -44, -106, 90, -88, -87, -88, -52, -124, 84, 117, 66, 79, -112, -111, -46, 86, -37, 109};
    public static final byte[] officaldebugkey = {-99, -69, 45, 71, 114, -116, 82, 66, -99, -122, 50, -70, -56, -111, 98, -35, -65, 105, 82, 43};
    public static final byte[] amazonkey = {-116, -115, -118, -89, -116, -112, 120, 55, 79, -8, -119, -23, 106, -114, -85, -56, -4, 105, 26, -57};
    public static final byte[] fdroidkey = {-92, 111, -42, -46, 123, -96, -60, 79, -27, -31, 49, 103, 11, -54, -68, -27, 17, 2, 121, 104};


    private static ConnectionState mLastLevel = ConnectionState.LEVEL_NOT_CONNECTED;

    private static LogFileHandler mLogFileHandler;

    static {
        logbuffer = new LinkedList<>();
        logListener = new Vector<>();
        stateListener = new Vector<>();
        byteCountListener = new Vector<>();


        logInformation();

    }


    public interface LogListener {
        void newLog(LogItem logItem);
    }

    public interface StateListener {
        void updateState(String state, String logMessage, int localizedResId, ConnectionState level);
    }

    public interface ByteCountListener {
        void updateByteCount(long in, long out, long diffIn, long diffOut);
    }

    public synchronized static void logMessage(LogLevel level, String prefix, String message) {
        newLogItem(new LogItem(level, prefix + message));

    }

    public synchronized static void clearLog() {
        logbuffer.clear();
        logInformation();
        if (mLogFileHandler != null)
            mLogFileHandler.sendEmptyMessage(LogFileHandler.TRIM_LOG_FILE);
    }

    private static void logInformation() {
      /*  String nativeAPI;
        try {
            nativeAPI = NativeUtils.getNativeAPI();
        } catch (UnsatisfiedLinkError ignore) {
            nativeAPI = "error";
        }

        logInfo(R.string.mobile_info, Build.MODEL, Build.BOARD, Build.BRAND, Build.VERSION.SDK_INT,
                nativeAPI, Build.VERSION.RELEASE, Build.ID, Build.FINGERPRINT, "", "");*/
    }

    public synchronized static void addLogListener(LogListener ll) {
        logListener.add(ll);
    }

    public synchronized static void removeLogListener(LogListener ll) {
        logListener.remove(ll);
    }

    public synchronized static void addByteCountListener(ByteCountListener bcl) {
        bcl.updateByteCount(mlastByteCount[0], mlastByteCount[1], mlastByteCount[2], mlastByteCount[3]);
        byteCountListener.add(bcl);
    }

    public synchronized static void removeByteCountListener(ByteCountListener bcl) {
        byteCountListener.remove(bcl);
    }


    public synchronized static void addStateListener(StateListener sl) {
        if (!stateListener.contains(sl)) {
            stateListener.add(sl);
            if (mLaststate != null)
                sl.updateState(mLaststate, mLaststatemsg, mLastStateresid, mLastLevel);
        }
    }

    private static int getLocalizedState(String state) {
        return switch (state) {
            case "CONNECTING" -> R.string.state_connecting;
            case "WAIT" -> R.string.state_wait;
            case "AUTH" -> R.string.state_auth;
            case "GET_CONFIG" -> R.string.state_get_config;
            case "ASSIGN_IP" -> R.string.state_assign_ip;
            case "ADD_ROUTES" -> R.string.state_add_routes;
            case "CONNECTED" -> R.string.state_connected;
            case "DISCONNECTED" -> R.string.state_disconnected;
            case "RECONNECTING" -> R.string.state_reconnecting;
            case "EXITING" -> R.string.state_exiting;
            case "RESOLVE" -> R.string.state_resolve;
            case "TCP_CONNECT" -> R.string.state_tcp_connect;
            default -> R.string.unknown_state;
        };

    }

    public static void updateStatePause(OpenVPNManagement.pauseReason pauseReason) {
        switch (pauseReason) {
            case noNetwork ->
                    VpnStatus.updateStateString("NONETWORK", "", R.string.state_nonetwork, ConnectionState.LEVEL_NO_NETWORK);
            case screenOff ->
                    VpnStatus.updateStateString("SCREENOFF", "", R.string.state_screenoff, ConnectionState.LEVEL_VPN_PAUSED);
            case userPause ->
                    VpnStatus.updateStateString("USERPAUSE", "", R.string.state_userpause, ConnectionState.LEVEL_VPN_PAUSED);
        }

    }

    private static ConnectionState getLevel(String state) {
        String[] noReplyYet = {"CONNECTING", "WAIT", "RECONNECTING", "RESOLVE", "TCP_CONNECT"};
        String[] reply = {"AUTH", "GET_CONFIG", "ASSIGN_IP", "ADD_ROUTES"};
        String[] connected = {"CONNECTED"};
        String[] notConnected = {"DISCONNECTED", "EXITING"};

        for (String x : noReplyYet)
            if (state.equals(x))
                return ConnectionState.LEVEL_CONNECTING_NO_SERVER_REPLY_YET;

        for (String x : reply)
            if (state.equals(x))
                return ConnectionState.LEVEL_CONNECTING_SERVER_REPLIED;

        for (String x : connected)
            if (state.equals(x))
                return ConnectionState.LEVEL_CONNECTED;

        for (String x : notConnected)
            if (state.equals(x))
                return ConnectionState.LEVEL_NOT_CONNECTED;

        return ConnectionState.UNKNOWN_LEVEL;

    }


    public synchronized static void removeStateListener(StateListener sl) {
        stateListener.remove(sl);
    }


    synchronized public static LogItem[] getLogBuffer() {

        // The stoned way of java to return an array from a vector
        // brought to you by eclipse auto complete
        return logbuffer.toArray(new LogItem[0]);

    }

    public static void updateStateString(String state, String msg) {
        int rid = getLocalizedState(state);
        ConnectionState level = getLevel(state);
        updateStateString(state, msg, rid, level);
    }

    public synchronized static void updateStateString(String state, String msg, int resid, ConnectionState level) {
        // Workound for OpenVPN doing AUTH and wait and being connected
        // Simply ignore these state
        if (mLastLevel == ConnectionState.LEVEL_CONNECTED &&
                (state.equals("WAIT") || state.equals("AUTH"))) {
            newLogItem(new LogItem((LogLevel.DEBUG), String.format("Ignoring OpenVPN Status in CONNECTED state (%s->%s): %s", state, level.toString(), msg)));
            return;
        }

        mLaststate = state;
        mLaststatemsg = msg;
        mLastStateresid = resid;
        mLastLevel = level;


        for (StateListener sl : stateListener) {
            sl.updateState(state, msg, resid, level);
        }
        //newLogItem(new LogItem((LogLevel.DEBUG), String.format("New OpenVPN Status (%s->%s): %s",state,level.toString(),msg)));
    }

    public static void logInfo(String message) {
        newLogItem(new LogItem(LogLevel.INFO, message));
    }

    public static void logDebug(String message) {
        newLogItem(new LogItem(LogLevel.DEBUG, message));
    }

    public static void logInfo(int resourceId, Object... args) {
        newLogItem(new LogItem(LogLevel.INFO, resourceId, args));
    }

    public static void logDebug(int resourceId, Object... args) {
        newLogItem(new LogItem(LogLevel.DEBUG, resourceId, args));
    }

    private static void newLogItem(LogItem logItem) {
        newLogItem(logItem, false);
    }


    synchronized static void newLogItem(LogItem logItem, boolean cachedLine) {
        if (cachedLine) {
            logbuffer.addFirst(logItem);
        } else {
            logbuffer.addLast(logItem);
            if (mLogFileHandler != null) {
                Message m = mLogFileHandler.obtainMessage(LogFileHandler.LOG_MESSAGE, logItem);
                mLogFileHandler.sendMessage(m);
            }
        }

        if (logbuffer.size() > MAXLOGENTRIES + MAXLOGENTRIES / 2) {
            while (logbuffer.size() > MAXLOGENTRIES)
                logbuffer.removeFirst();
            if (mLogFileHandler != null)
                mLogFileHandler.sendMessage(mLogFileHandler.obtainMessage(LogFileHandler.TRIM_LOG_FILE));
        }

        //if (BuildConfig.DEBUG && !cachedLine && !BuildConfig.FLAVOR.equals("test"))
        //    Log.d("OpenVPN", logItem.getString(null));


        for (LogListener ll : logListener) {
            ll.newLog(logItem);
        }
    }


    public static void logError(String msg) {
        newLogItem(new LogItem(LogLevel.ERROR, msg));

    }

    public static void logWarning(int resourceId, Object... args) {
        newLogItem(new LogItem(LogLevel.WARNING, resourceId, args));
    }

    public static void logWarning(String msg) {
        newLogItem(new LogItem(LogLevel.WARNING, msg));
    }


    public static void logError(int resourceId) {
        newLogItem(new LogItem(LogLevel.ERROR, resourceId));
    }

    public static void logError(int resourceId, Object... args) {
        newLogItem(new LogItem(LogLevel.ERROR, resourceId, args));
    }

    public static void logMessageOpenVPN(LogLevel level, int ovpnlevel, String message) {
        newLogItem(new LogItem(level, ovpnlevel, message));

    }


    public static synchronized void updateByteCount(long in, long out) {
        long lastIn = mlastByteCount[0];
        long lastOut = mlastByteCount[1];
        long diffIn = mlastByteCount[2] = Math.max(0, in - lastIn);
        long diffOut = mlastByteCount[3] = Math.max(0, out - lastOut);


        mlastByteCount = new long[]{in, out, diffIn, diffOut};
        for (ByteCountListener bcl : byteCountListener) {
            bcl.updateByteCount(in, out, diffIn, diffOut);
        }
    }


}
