package com.progun.dunta_sdk.proxy.core;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

public class DeviceNetworkState {
    public enum STATE { WIFI, CELLULAR }
    public enum TYPE { NONE, TYPE_2G, TYPE_3G, TYPE_4G, TYPE_5G }

    private STATE state;
    private TYPE type;

    public DeviceNetworkState(@IntRange(from = 0, to = 1) int networkState) {
        if (networkState == 1) {
            this.state = STATE.CELLULAR;
        } else if (networkState == 0) {
            this.state = STATE.WIFI;
        } else throw new IllegalStateException("Invalid network state input data");
    }

    public DeviceNetworkState(@NonNull STATE networkState, @NonNull TYPE networkType) {
        this.state = networkState;
        this.type = networkType;
    }


    public synchronized void changeStateTo(@NonNull STATE state) {
        this.state = state;
        this.type = TYPE.NONE;
    }

    public synchronized void changeStateTo(@NonNull STATE state, @NonNull TYPE type) {
        this.state = state;
        this.type = type;
    }

    @NonNull
    public STATE getStateEnum() {
        if (state != null) return state;
        else throw new IllegalStateException("network state is null");
    }

    @IntRange(from = 0, to = 1)
    public int getStateInt() {
        return state.ordinal();
    }

    @IntRange(from = 0, to = 4)
    public int getTypeInt() {
        return type.ordinal();
    }

    @NonNull
    @Override
    public synchronized String toString() {
        return "network-" + state.name();
    }
}
