package com.progun.dunta_sdk.android.connectionstate;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.progun.dunta_sdk.utils.LogWrap;

/**
 * Вспомогательгый класс для определения типа соединения,
 * получаемого коллбеками от класса
 */
public class ConnectionType {
    public static final int WIFI = 0;
    public static final int CELLULAR = 1;
    public static final int OTHER = 2;
    public static final int NONE = 3;
    private static final String TAG = ConnectionType.class.getSimpleName();

    public static int getType(@NonNull ConnectivityManager connectivityManager) {
        LogWrap.d(TAG, "getType() has called");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities capabilities = connectivityManager
                    .getNetworkCapabilities(connectivityManager.getActiveNetwork());
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    LogWrap.d(TAG, "Wifi connection");
                    return 0;
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    LogWrap.d(TAG, "Cellular connection");
                    return 1;
                }
            }
        } else {
            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected();
            int networkType = connectivityManager.getActiveNetworkInfo().getType();
            if (networkType == ConnectivityManager.TYPE_WIFI)
                return 0;
            else if (networkType == ConnectivityManager.TYPE_MOBILE)
                return 1;
        }
        LogWrap.d(TAG, "No connection");
        return -1;
    }

    public static int getCurrentConnectionType(@NonNull Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        Integer wifi;
        wifi = currentConnectionTypeApiHeight(cm);

        LogWrap.d(TAG, "getCurrentConnectionType()=" + wifi);
        if (wifi != null) return wifi;
        return NONE;
    }

    /** For API <= 21 */
    @Nullable
    private static Integer currentConnectionTypeApiLow(ConnectivityManager cm) {
        cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected();
        int networkType = cm.getActiveNetworkInfo().getType();
        if (networkType >= 0) {
            if (networkType == ConnectivityManager.TYPE_WIFI) {
                return WIFI;
            } else if (networkType == ConnectivityManager.TYPE_MOBILE) {
                return CELLULAR;
            } else {
                return OTHER;
            }
        }
        return null;
    }

    /** For API >= 23 */
    @Nullable
    private static Integer currentConnectionTypeApiHeight(ConnectivityManager cm) {
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());

        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                LogWrap.d(TAG, "Device using wi-fi connection");
                return WIFI;
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                LogWrap.d(TAG, "Device using cellular connection");
                return CELLULAR;
            }
        }
        return null;
    }


}
