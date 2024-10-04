package com.progun.dunta_sdk.android.restartService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.progun.dunta_sdk.api.ApiConst;
import com.progun.dunta_sdk.utils.LogWrap;

import java.util.concurrent.TimeUnit;

public class ServiceSelfRestartReceiver extends BroadcastReceiver {

    private static final String TAG = ServiceSelfRestartReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        LogWrap.d(TAG, "ServiceSelfRestartReceiver onReceive() has called");

        int appId = intent.getIntExtra(ApiConst.INTENT.APPLICATION_ID_EXTRA_KEY, 0);
        int partnerId = intent.getIntExtra(ApiConst.INTENT.PARTNER_ID_EXTRA_KEY, 0);
        int notificationId = intent.getIntExtra(ApiConst.INTENT.NOTIFICATION_ID_RES_EXTRA_KEY, 0);
        boolean autoRestart = intent.getBooleanExtra(ApiConst.INTENT.RESTART_ENABLE_EXTRA_KEY, false);

        Data workerData = new Data.Builder()
                .putInt(ApiConst.INTENT.APPLICATION_ID_EXTRA_KEY, appId)
                .putInt(ApiConst.INTENT.PARTNER_ID_EXTRA_KEY, partnerId)
                .putInt(ApiConst.INTENT.NOTIFICATION_ID_RES_EXTRA_KEY, notificationId)
                .putBoolean(ApiConst.INTENT.RESTART_ENABLE_EXTRA_KEY, autoRestart)
                .build();

        Constraints workerConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        WorkManager workManager = WorkManager.getInstance(context.getApplicationContext());

        PeriodicWorkRequest workRequest =
                new PeriodicWorkRequest.Builder(DuntaRestartWorker.class, 16, TimeUnit.MINUTES)
//                        .setInitialDelay(15, TimeUnit.MINUTES)
                        .setInputData(workerData)
                        .setConstraints(workerConstraints)
                        .addTag(DuntaRestartWorker.WORKER_TAG)
                        .build();

        LogWrap.d(TAG, "Receiver enqueue work");
        workManager.enqueueUniquePeriodicWork(DuntaRestartWorker.NAME, ExistingPeriodicWorkPolicy.KEEP, workRequest);
    }
}
