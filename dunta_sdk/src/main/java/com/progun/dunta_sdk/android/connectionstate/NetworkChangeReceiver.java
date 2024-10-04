package com.progun.dunta_sdk.android.connectionstate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

import com.progun.dunta_sdk.utils.LogWrap;

/**
 * Видимо стаый класс для уведомления о состоянии сети. Не используется.
 * На замену ему существуют классы {@link ConnectionStateMonitor}
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String TAG = "v";
    private static ConnectivityManager connectivityManager;
    NetworkChangeListener networkChangeListener;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        LogWrap.d(TAG, "onReceive() has called");
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        checkNetWorkType();
    }

    public NetworkChangeReceiver(NetworkChangeListener networkChangeListener) {
        this.networkChangeListener = networkChangeListener;
    }

    public static void checkNetWorkType() {
        LogWrap.d(TAG, "CheckNetWorkType() has called");
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                informUser("Wifi connection");
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                informUser("Mobile connection");
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                informUser("Ethernet connection");
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                informUser("Vpn connection");
            }
        } else
            informUser("No connection");
    }

    public static void informUser(String msg) {
        LogWrap.d(TAG, "informUser() has called");
        LogWrap.d(TAG, "Network type has changed to \"" + msg + "\"");
    }

    public static void sendNetworkChange(){

    }
}
