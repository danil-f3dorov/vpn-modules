package com.progun.dunta_sdk.android.restartService;

import android.app.ForegroundServiceStartNotAllowedException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.progun.dunta_sdk.android.core.DuntaService;
import com.progun.dunta_sdk.api.ApiConst;
import com.progun.dunta_sdk.utils.LogWrap;

public class DuntaRestartWorker extends Worker {
    public static final String TAG = DuntaRestartWorker.class.getSimpleName();
    public static final String NAME = "restart_worker";
    public static final String WORKER_TAG = "prxy-wrkr:23";
    private final Context context;

    private int applicationId = 0;
    private int partnerId = 0;

    public DuntaRestartWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        LogWrap.d(DuntaRestartWorker.class.getSimpleName(), "ProxyRestartWorker() constructor");
    }

    @NonNull
    @Override
    public Result doWork() {
        LogWrap.d(TAG, "ProxyClient SingleWorker starts doWork()...");

        applicationId = getInputData().getInt(ApiConst.INTENT.APPLICATION_ID_EXTRA_KEY, 0);
        partnerId = getInputData().getInt(ApiConst.INTENT.PARTNER_ID_EXTRA_KEY, 0);

        LogWrap.d(TAG, "doWork(): appID=" + applicationId + " /partnerID=" + partnerId);

        if (applicationId == 0) {
            LogWrap.e(TAG, "appID is NULL");
            SharedPreferences sp = context.getSharedPreferences(ApiConst.PARTNER_INFO.PREFS.FILE_NAME, Context.MODE_PRIVATE);
            int appId = sp.getInt(ApiConst.PARTNER_INFO.PREFS.FIELDS.APP_ID, 0);
            LogWrap.d(TAG, "appID from prefs = " + appId);
            if (appId != 0) this.applicationId = appId;
        }
        if (partnerId == 0) {
            LogWrap.e(TAG, "partnerID is NULL");
            SharedPreferences sp = context.getSharedPreferences(ApiConst.PARTNER_INFO.PREFS.FILE_NAME, Context.MODE_PRIVATE);
            int partnerId = sp.getInt(ApiConst.PARTNER_INFO.PREFS.FIELDS.PARTNER_ID, 0);
            LogWrap.d(TAG, "partnerID from prefs = " + partnerId);
            if (partnerId != 0) this.partnerId = partnerId;
        }

        launchService(
                this.getInputData().getInt(ApiConst.INTENT.NOTIFICATION_ID_RES_EXTRA_KEY, 0),
                this.getInputData().getBoolean(ApiConst.INTENT.RESTART_ENABLE_EXTRA_KEY, false)
        );

        return Result.success();
    }

    private void launchService(
            int notificationIdRes, boolean restartEnabled
    ) {
        Intent serviceIntent = new Intent(context, DuntaService.class)
                .putExtra(ApiConst.INTENT.PARTNER_ID_EXTRA_KEY, partnerId)
                .putExtra(ApiConst.INTENT.APPLICATION_ID_EXTRA_KEY, applicationId)
                .putExtra(ApiConst.INTENT.RESTART_ENABLE_EXTRA_KEY, restartEnabled);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            launchServiceAboveAndroid12(serviceIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            launchServiceAboveAndroid8(serviceIntent);
        else
            launchServiceAboveAndroid7(serviceIntent);
    }

    @RequiresApi(31)
    private void launchServiceAboveAndroid12(Intent serviceIntent) {
        LogWrap.w(WORKER_TAG, "launchServiceAboveAndroid12() has called");
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm.isIgnoringBatteryOptimizations(context.getPackageName())) {
            try {
                LogWrap.w(WORKER_TAG, "battery optimized");
                context.startForegroundService(serviceIntent);
            } catch (ForegroundServiceStartNotAllowedException e) {
                LogWrap.w(WORKER_TAG, "battery does not optimized");
                e.printStackTrace();
            }
        } else {
            context.startForegroundService(serviceIntent);
        }
    }

    @RequiresApi(26)
    private void launchServiceAboveAndroid8(Intent serviceIntent) {
        context.startForegroundService(serviceIntent);
    }

    private void launchServiceAboveAndroid7(Intent serviceIntent) {
        context.startService(serviceIntent);
    }
}
