package com.progun.dunta_sdk.android.connectionstate;

/** Listener for observe network changes between wifi/cellular */
public interface NetworkChangeListener {
    void onNetworkChanged(int newNetworkType);
}