package com.progun.dunta_sdk.proxy.utils;

import com.progun.dunta_sdk.BuildConfig;

public abstract class ProtocolConstants {

    // MAX CHANNELS
    public  static  final short BOT_MAX_CHANNELS = 100; // максимальное число каналов на клиенте (может быть и больше, но до 100 как я проверял точно работает и успевает все обработать)

    // URLs
    public static final String JSON_SERVER_URL_TEST = "http://mobile-api.proxyegg.com"; // адрес тестового JSON сервера
    public static final String JSON_SERVER_URL_PROD = "https://api.ipht.net"; // адрес релизного JSON сервера
    public static final String JSON_SERVER_URL = getJsonServerUrl();
    private static String getJsonServerUrl() {
//        if(BuildConfig.DEBUG) {
//             return ProtocolConstants.JSON_SERVER_URL_TEST;
//        }
        return ProtocolConstants.JSON_SERVER_URL_PROD;
    }

    private static boolean isCryptEnabled() {
        return !BuildConfig.DEBUG;
    }



    public static class JsonNames {
        public static final String PARTNER_ID_NODE = "partnerId"; // ID партнера
        public static final String APP_ID_NODE = "appId"; // ID приложения партнера
        public static final String SYSTEM_INFO_NODE = "sysinfo"; //
        public static final String SDK_VERSION_NODE = "version"; // Номерная версия СДК, по идее должна быть равна номерному релизу в ГП
        public static final String CPU_ARCH_NODE = "platform"; //
        public static final String SERVER_PROTOCOL_VERSION_NODE = "pVer"; // Версия протокола
        public static final String PHONE_MODEL_NODE = "phone"; // Название устройства (типа google, samsung and etc.)
        public static final String TOTAL_RAM_NODE = "memory"; // Кол-во оперативки
        public static final String ADVERTISEMENT_ID_NODE = "advId"; // Адийшник из URL, для инсталлов в ГП
        public static final String ANDROID_ID_NODE = "androidId"; // Уникальный иденитфикатор устройства, завязан на железо телефона
        public static final String JSON_PROTOCOL_VERSION_NODE = "ver";
    }


    // Header
    public static final int SRV_PROTO_VERSION = 0x07; // версия протокола с прокси-сервером
    public static final int JSON_PROTO_VERSION = 1; // версия протокола с JSON-сервером
    public static final int SDK_RELEASE_VERSION_TEST_APP = BuildConfig.GPLAY_SDK_RELEASE_VER; // версия сборки тестового приложения(которое выкладываем в play market). Нужно здесь, чтобы в репортах можно было видеть версию приложения
    public static final int SDK_RELEASE_VERSION_AS_SDK = BuildConfig.PROD_SDK_RELEASE_VER; // версия SDK, как отдельного APK-файла
//    public static final int SDK_RELEASE_VERSION = Build.VERSION.c;
    public static final int PROXY_CMD_MARKER = 0xF7AA;
    public static final int PARTNER_ID = 0x0000;


    // Changed min
    public static final int PROXY_MIN_HEADER_LEN = 2;          // bytes
    public static final int PROXY_MAX_HEADER_LEN = 14;         // bytes
    public static final int PROXY_RECV_HEADER_LEN = Short.BYTES + (Integer.BYTES * 2);
    public static final int PROXY_MARKER_LEN = 2;              // bytes
    //public static final int PROXY_MAX_DATA_LEN = 4096;  // bytes



    // Timeouts (60_000 equivalent 60000)
    public static final int JSON_CONNECT_TIMEOUT_MILLS = 60_000;     // milliseconds
    public static final int JSON_CONNECT_TIMEOUT_SEC = 60_000 / 1000;     // milliseconds


    public static final long SRV_PING_TIMEOUT = 30_000;        // milliseconds
    public static final int SRV_CONNECT_TIMEOUT = 60_000;      // milliseconds
    public static final int SRV_CONNECT_TIMEOUT_SEC = 60_000 / 1000;      // milliseconds

    public static final int SRV_WRITE_TIMEOUT = 30_000;         // milliseconds

    public static final int HOST_CONNECT_TIMEOUT = 30_000;     // milliseconds
    public static final int HOST_READ_TIMEOUT = 30_000;         // milliseconds

    public static class DeviceLockState {
        // Screen on/off changed
        public static final int LOCKED = 0; // Экран заблокирован(выключен)
        public static final int UNLOCKED = 1; // Экран разблокирован(включен)
    }

    // Crypting data
    public static int CRYPT_SEED = 0xA123EB9C; // SEED шифрования
    public static boolean ENABLE_CRYPT = isCryptEnabled(); // включает, выключает шифрование на клиенте

    // REPORTS
    /* SEND_REPORT_ENABLE Включает/отключает перехват репортов и отправку их на сервер.
    * Если true - экзепшены не будут ронять приолжения и будут перехватываться, записываться в файл и отправляться на сервер.
    * Если false. - они будут бросаться системой как по умолчанию, роняя приложение и т.д.
    * */
    public static final boolean SEND_REPORT_ENABLE = BuildConfig.ENABLE_REPORT_FILES;
//    public static final boolean SEND_REPORT_ENABLE = BuildConfig.ENABLE_REPORT_FILES;

    // Logs
    // Включает/выключает перехват логирования в файл из консоли. Использовал для отладки и отслеживания поведения СДК.
    // Но нужно включить пермишены в манифесте
    public static final boolean ENABLE_GRAB_LOGCAT = false;

}