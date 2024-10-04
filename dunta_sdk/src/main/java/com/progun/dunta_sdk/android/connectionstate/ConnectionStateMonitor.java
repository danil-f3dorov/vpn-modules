package com.progun.dunta_sdk.android.connectionstate;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.annotation.NonNull;

import com.progun.dunta_sdk.android.core.DuntaService;
import com.progun.dunta_sdk.utils.LogWrap;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Класс описывающий поведение при получении именений состояния сети
 * пользователя от системы Android.
 */
public class ConnectionStateMonitor extends NetworkCallback {
    public final String TAG = ConnectionStateMonitor.class.getSimpleName();

    private static final AtomicBoolean isOnline = new AtomicBoolean(false);
    private static final AtomicBoolean onAvailable = new AtomicBoolean(false);

    final NetworkRequest networkRequest;
    //    private ConnectionStateMonitor instance;
    private final ConnectivityManager connectivityManager;
    private final NetworkChangeListener networkChangeListener;

    public ConnectionStateMonitor(
            @NonNull NetworkChangeListener networkChangeListener,
            @NonNull Context context
    ) {
        LogWrap.d(TAG, "ConnectionStateMonitor() has called");
        this.networkChangeListener = networkChangeListener;

        networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();

        connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerNetworkCallback(networkRequest, this);
    }

//    public synchronized static AtomicBoolean isOnAvailable() {
//        return onAvailable;
//    }

    public static boolean isOnline() {
        return isOnline.get();
    }

    @Override
    public void onAvailable(Network network) {
        LogWrap.d(TAG, "NetworkCallback_onAvailable() " + network.toString());
        super.onAvailable(network);
        isOnline.set(true);
        onAvailable.set(true);
        LogWrap.d(TAG, "ProxyClient is started=" + DuntaService.clientIsRunning);
        if (DuntaService.clientIsRunning)
            networkChangeListener.onNetworkChanged(ConnectionType.getType(connectivityManager));
    }

    @Override
    public void onLost(Network network) {
        LogWrap.d(TAG, "NetworkCallback_onLost() " + network.toString());
        super.onLost(network);
        isOnline.set(false);
        onAvailable.set(false);
        // when network lost called it
        LogWrap.i(TAG, "onLost called ");
    }

    @Override
    public void onCapabilitiesChanged(
            @NonNull Network network,
            @NonNull NetworkCapabilities networkCapabilities
    ) {
        super.onCapabilitiesChanged(network, networkCapabilities);
        LogWrap.d(TAG, "NetworkCallback_onCapabilitiesChanged() " + networkCapabilities);
    }

    @Override
    public void onLinkPropertiesChanged(
            @NonNull Network network,
            @NonNull LinkProperties linkProperties
    ) {
        super.onLinkPropertiesChanged(network, linkProperties);
        LogWrap.d(TAG, "onLinkPropertiesChanged() " + linkProperties);
    }

    public void unregisterCallback() {
        connectivityManager.unregisterNetworkCallback(this);
    }
}


