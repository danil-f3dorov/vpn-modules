package com.progun.dunta_sdk.proxy.utils;

public class ReferrerConsts {
    public static final int RECONNECTION_TIMEOUT = 30_000;
    public static final int MAX_RECONNECTIONS_COUNT = 3;
    public static final int EMPTY_PREFS = -1;
    public static final String REF_EXTRACT_REGEX_PATTERN = "utm_source=([^&]+)";
    public static class MARKS {
        public static final int RESPONSE_PARSE_ERROR = 1; // источник трафика не определен
        public static final int FEATURE_NOT_SUPPORTED = 2; // источник трафика не определен
        public static final int DEFAULT_GOOGLE_PLAY_RESPONSE = 3; // источник трафика - гугл плей


//        public static final int SERVICE_UNAVAILABLE = 3;
//        public static final int RECONNECTION_FAILED = 4;
    }

    public static class DEFAULT_RESPONSES {
        public static final String GOOGLE_PLAY = "google-play";
    }
}
