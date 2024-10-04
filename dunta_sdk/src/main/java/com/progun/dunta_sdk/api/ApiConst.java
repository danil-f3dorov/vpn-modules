package com.progun.dunta_sdk.api;

public final class ApiConst {

    public static final class INTENT {
        public final static String APPLICATION_ID_EXTRA_KEY = "application_id_intent_extra";
        public final static String PARTNER_ID_EXTRA_KEY = "partner_id_intent_extra";
        public final static String NOTIFICATION_ID_RES_EXTRA_KEY = "notification_id_intent_extra";
        public final static String RESTART_ENABLE_EXTRA_KEY = "restart_enable";
        public final static String NOTIFICATION_CONTENT = "notification_content";
        public final static String NOTIFICATION_TITLE = "notification_title";
    }

    public static final class PARTNER_INFO {
        public static final class PREFS {
            public static final String FILE_NAME = "sdk_info";
            public static final class FIELDS {
                public final static String APP_ID = "app_id";
                public final static String PARTNER_ID = "partner_id";
                public final static String PEER_STATUS = "peer_status";
                public final static String NOTIFICATION_CONTENT = "notification_content";
                public final static String NOTIFICATION_TITLE = "notification_title";
            }
        }
    }

    public static final class REFERRER {
        public static int DEBUG = 0;
        public static int NO_DATA = -1;
        public static int EMPTY_TEMP = -2;
        public static final class PREFS {
            public static final String FILE_NAME = "init_info";
            public static final class FIELDS {
                public final static String REFERRER_ID = "referrer_id";
                public final static String SYSTEM_ANDROID_ID = "system_android_id";
                public final static String LAUNCHED_BEFORE = "launched_before";
            }
        }
    }
}
