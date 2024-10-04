package com.progun.dunta_sdk.android.screenstate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.progun.dunta_sdk.android.core.DuntaService;
import com.progun.dunta_sdk.proxy.core.ProxyClient;
import com.progun.dunta_sdk.proxy.utils.ProtocolConstants;
import com.progun.dunta_sdk.utils.LogWrap;

/**
 * Системная служба Android, уведомляющая приложение об изменении
 * состояния экрана (вкл/выкл).
 */
public class ScreenReceiver extends BroadcastReceiver {

    final private String TAG = ScreenReceiver.class.getSimpleName();

    public ScreenReceiver(@NonNull ScreenLockChangeListener screenListener) {
        this.screenListener = screenListener;
    }

    ScreenLockChangeListener screenListener;

    public boolean wasScreenOn = true;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        LogWrap.d(TAG, "onReceive() has called(screen)");
        if (DuntaService.serviceIsRunning && ProxyClient.isClientRunning) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    if (wasScreenOn) {
                        LogWrap.d(TAG, "Screen off. ");
                        if (screenListener != null) {
                            screenListener.onLockChanged(ProtocolConstants.DeviceLockState.LOCKED);
                        }
                    }
                    wasScreenOn = false;
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    if (!wasScreenOn) {
                        LogWrap.d(TAG, "Screen on. ");
                        if (screenListener != null) {
                            screenListener.onLockChanged(ProtocolConstants.DeviceLockState.UNLOCKED);
                        }
                    }
                    wasScreenOn = true;
                }
            }
        }
    }


}
