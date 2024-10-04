package com.progun.dunta_sdk.utils;

import android.util.Log;

import com.progun.dunta_sdk.BuildConfig;

// Класс обертка над стандартным логированием андроида. шорткат в сутдии - wlog
public class LogWrap {
    final public static boolean LOG_ENABLE = false; // включает/отключает логи в приложении
    final public static boolean ONLY_PACKETS_MODE_ENABLE = true; // включает/отключает логирование только отпралвенных/полученных пакетов
    final private static String SEP = "::";
    final private static String EXTENSION_MSG = "[Call_history]";


    public static void d(String tag, String msg) {
        if (BuildConfig.DEBUG_LOG) Log.d(EXTENSION_MSG + SEP + tag + SEP + SEP, msg);
    }
    public static void d(String tag, String msg, int channelId) {
        if (BuildConfig.DEBUG_LOG) Log.d(EXTENSION_MSG + SEP + tag + SEP + SEP, "cnl=" + channelId + " | " + msg);
    }


    public static void v(String tag, String msg) {
        if (BuildConfig.DEBUG_LOG) Log.v(EXTENSION_MSG + SEP + tag + SEP + SEP, msg);
    }
    public static void v(String tag, String msg, int channelId) {
        if (BuildConfig.DEBUG_LOG) Log.v(EXTENSION_MSG + SEP + tag + SEP + SEP, "cnl=" + channelId + " | " + msg);
    }


    public static void e(String tag, String msg) {
        if (BuildConfig.DEBUG_LOG) Log.e(EXTENSION_MSG + SEP + tag + SEP + SEP, msg);
    }
    public static void e(String tag, String msg, int channelId) {
        if (BuildConfig.DEBUG_LOG) Log.e(EXTENSION_MSG + SEP + tag + SEP + SEP, "cnl=" + channelId + " | " + msg);
    }


    public static void i(String tag, String msg, int channelId) {
        if (BuildConfig.DEBUG_LOG) Log.i(EXTENSION_MSG + SEP + tag + SEP + SEP, "cnl=" + channelId + " | " + msg);
    }
    public static void i(String tag, String msg) {
        if (BuildConfig.DEBUG_LOG) Log.i(EXTENSION_MSG + SEP + tag + SEP + SEP, msg);
    }


    public static void w(String tag, String msg) {
        if (BuildConfig.DEBUG_LOG) Log.w(EXTENSION_MSG + SEP + tag + SEP + SEP,msg);
    }
    public static void w(String tag, String msg, int channelId) {
        if (BuildConfig.DEBUG_LOG) Log.w(EXTENSION_MSG + SEP + tag + SEP + SEP,"CHANNEL_ID:" + channelId + " | " + msg);
    }
}
