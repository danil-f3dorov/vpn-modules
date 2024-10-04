package com.progun.dunta_sdk.android.restartService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import com.progun.dunta_sdk.android.core.DuntaService;
import com.progun.dunta_sdk.api.ApiConst;

public class RebootDeviceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(ApiConst.PARTNER_INFO.PREFS.FILE_NAME, Context.MODE_PRIVATE);
        int appId = sharedPreferences.getInt(ApiConst.PARTNER_INFO.PREFS.FIELDS.APP_ID, 0);
        int partnerId = sharedPreferences.getInt(ApiConst.PARTNER_INFO.PREFS.FIELDS.PARTNER_ID, 0);
        String peerStatus = sharedPreferences.getString(ApiConst.PARTNER_INFO.PREFS.FIELDS.PEER_STATUS, "NOT_PEER");

        if (appId != 0 && partnerId != 0 && peerStatus.equals("PEER")) {
            Intent serviceIntent = new Intent(context, DuntaService.class)
                    .putExtra(ApiConst.INTENT.PARTNER_ID_EXTRA_KEY, partnerId)
                    .putExtra(ApiConst.INTENT.APPLICATION_ID_EXTRA_KEY, appId);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }


    }
}
