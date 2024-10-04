package com.progun.dunta_sdk.api;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.progun.dunta_sdk.android.core.AskUserDialogFragment;
import com.progun.dunta_sdk.android.core.DuntaService;
import com.progun.dunta_sdk.android.restartService.DuntaRestartWorker;

import java.util.concurrent.TimeUnit;

public class DuntaManagerImpl extends DuntaManager {

    private final String TAG = DuntaManagerImpl.class.getSimpleName();

    private final int MAX_APP_ID_BOUND = 65536;

    // Дефолтное значение для идентификатора реферальной ссылки на приложение в маркете
    private final int PARTNER_AND_APP_ID_DEF_VALUE = 0;

    // Имена файлов, значений в shared prefs
    private final String SHARED_PREFS_FILE_NAME = "ProxyServiceFile";

    private final String PREFS_FIELD_APP_ID = "application_id";
    private final String PREFS_FIELD_PARTNER_ID = "partner_id";
    private final String PREFS_FIELD_PROXY_STATE = "proxy_state";

    private final String INTENT_KEY_APP_ID = PREFS_FIELD_APP_ID;
    private final String INTENT_KEY_PARTNER_ID = PREFS_FIELD_PARTNER_ID;

    // флаг проверки запущен ли фоновый сервис с прокси в фоне
    private boolean isStarted = false;

    private String prefsFileName = SHARED_PREFS_FILE_NAME;

    private UserState userState = UserState.NOT_INIT;
    @DrawableRes
    private int notificationIconId = 0;
    private int applicationId = PARTNER_AND_APP_ID_DEF_VALUE;
    private int partnerId = PARTNER_AND_APP_ID_DEF_VALUE;
    private String notificationContent = "Service is running";
    private String notificationTitle = "Matfix service";

    private String workerTag = DuntaRestartWorker.WORKER_TAG;
    private String workerUniqueName = DuntaRestartWorker.NAME;

    private String dialogTitle = null;


    public static SocketCallback socketCallback;

    @Override
    public void setSocketCallback(SocketCallback callback) {
        socketCallback = callback;
    }

     /*
     * При создании экземпляра класса проверяет впервые ли запущено приложение,
     * ставит флаги состояния SDK
     */
    public DuntaManagerImpl(@NonNull Context context) {
        Log.d(TAG, "API: constructor");
        String initProxyState = context.getSharedPreferences(
                        ApiConst.PARTNER_INFO.PREFS.FILE_NAME,
                        Context.MODE_PRIVATE
                )
                .getString(
                        ApiConst.PARTNER_INFO.PREFS.FIELDS.PEER_STATUS,
                        UserState.NOT_INIT.name()
                );
        if (initProxyState.equals(UserState.NOT_INIT.name())) {
            userState = UserState.NOT_INIT;
        } else if (initProxyState.equals(UserState.PEER.name())) {
            userState = UserState.PEER;
        } else if (initProxyState.equals(UserState.NOT_PEER.name())) {
            userState = UserState.NOT_PEER;
        } else {
            throw new IllegalStateException("Proxy state has invalid value");
        }
    }

    @Override
    public void stop(@NonNull Context context) {

        Log.d(TAG, "API: stop");
        WorkManager.getInstance(context).cancelUniqueWork(DuntaRestartWorker.NAME);

        Intent stopIntent =
                new Intent(context, DuntaService.class).setAction(DuntaService.ACTION_STOP_INTENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(stopIntent);
        }
        setUserPermissionForProxyState(context, UserState.NOT_PEER);
    }


    @Override
    public boolean isPeer() {
        Log.d(TAG, "API: isPeer" + getUserState());
        return getUserState() == UserState.PEER;
    }

    @Override
    public void start(@NonNull Context context) throws IllegalStateException {
        Log.d(TAG, "API: start");
//        validateUserInfo(applicationId, partnerId);
        Log.d(TAG, "API: start completed");
        checkAppAndPartnerIds(context);
//        checkNotificationTexts(context);
        launchService(context);
    }

//    private final String NOTIFICATION_CONTENT_DEFAULT = "Dunta work started";
//    private final String NOTIFICATION_TITLE_DEFAULT = "MAFIX LTD service";
//
//    private void checkNotificationTexts(@NonNull Context ctx) {
//        if (!customNotificationContent) {
//            ctx.getSharedPreferences(
//                            ApiConst.PARTNER_INFO.PREFS.FILE_NAME,
//                            Context.MODE_PRIVATE
//                    ).edit()
//                    .putString(
//                            ApiConst.PARTNER_INFO.PREFS.FIELDS.NOTIFICATION_CONTENT,
//                            NOTIFICATION_CONTENT_DEFAULT
//                    ).apply();
//        }
//
//        if (!customNotificationTitle) {
//            ctx.getSharedPreferences(
//                            ApiConst.PARTNER_INFO.PREFS.FILE_NAME,
//                            Context.MODE_PRIVATE
//                    ).edit()
//                    .putString(
//                            ApiConst.PARTNER_INFO.PREFS.FIELDS.NOTIFICATION_TITLE,
//                            NOTIFICATION_TITLE_DEFAULT
//                    ).apply();
//        }
//    }
//
//    private void validateUserInfo(int applicationId, int partnerId) {
//        if (applicationId == PARTNER_AND_APP_ID_DEF_VALUE)
//            throw new IllegalStateException("You need to set your personal application ID");
//        if (partnerId == PARTNER_AND_APP_ID_DEF_VALUE)
//            throw new IllegalStateException("You need to set your personal partner ID");
//    }

    @Override
    @NonNull
    public UserState getUserState() {
        return userState;
    }

    @Override
    public void setPartnerId(int partnerId) {
        Log.d(TAG, "API: setPartnerId");
        if (partnerId <= 0)
            throw new IllegalStateException("partner_id invalid state of partnerId value: " + partnerId);
        this.partnerId = partnerId;
    }

    @Override
    public void setApplicationId(int applicationId) {
        Log.d(TAG, "API: appId");
        if (applicationId <= 0)
            throw new IllegalStateException("application_id invalid state of partnerId value: " + applicationId);
        this.applicationId = applicationId;
    }


/*    @Override
    @Deprecated
    public void setNotificationIconId(@DrawableRes int notificationIconId) {
        Log.d(TAG, "API: setNotificationIconId");
        this.notificationIconId = notificationIconId;
    }*/

/*    @Override
    public void setPrefsFileName(@NonNull String prefsFileName) {
        this.prefsFileName = prefsFileName;
    }

    @Override
    public void setWorkerParams(@Nullable String workerTag, @Nullable String workerUniqueName) {
        Log.d(TAG, "API: workerParams");
        if (workerTag != null) this.workerTag = workerTag;
        if (workerUniqueName != null) this.workerUniqueName = workerUniqueName;
    }*/


//    private boolean customNotificationTitle = false;
//    private boolean customNotificationContent = false;

    @Override
    public void setNotificationContent(@NonNull Context context, @NonNull String notificationText) {
//        this.notificationContent = notificationText;
//        customNotificationContent = true;
//
//        context.getSharedPreferences(
//                        ApiConst.PARTNER_INFO.PREFS.FILE_NAME,
//                        Context.MODE_PRIVATE
//                ).edit()
//                .putString(
//                        ApiConst.PARTNER_INFO.PREFS.FIELDS.NOTIFICATION_CONTENT,
//                        notificationText
//                ).apply();
    }

    @Override
    public void setNotificationTitle(@NonNull Context context, @NonNull String notificationTitle) {
//        customNotificationTitle = true;
//        this.notificationTitle = notificationTitle;
//        context.getSharedPreferences(
//                        ApiConst.PARTNER_INFO.PREFS.FILE_NAME,
//                        Context.MODE_PRIVATE
//                ).edit()
//                .putString(
//                        ApiConst.PARTNER_INFO.PREFS.FIELDS.NOTIFICATION_TITLE,
//                        notificationTitle
//                ).apply();
    }

    @Override
    public void setDialogTitle(@NonNull Context context, @NonNull String dialogTitle) {
        if (!dialogTitle.isBlank()) {
            this.dialogTitle = dialogTitle;
        }
    }

    @Override
    boolean isStarted() {
        return isStarted;
    }

    @Override
    public void showDialog(
            @NonNull Context context,
            @NonNull FragmentManager fragmentManager,
            boolean forceShow,
            @NonNull OnUserChoiceClickListener userClickListener
    ) {
        Log.d(TAG, "API: showDialog");
        boolean isInit = isProxyAlreadyInitialized();
        if (!isInit | forceShow) {
            Log.d(TAG, "API: showDialog first");
            DialogFragment dialog = buildDialog(userClickListener);
            dialog.show(fragmentManager, "PROXY_DIALOG_DESC");
        }
    }

    @Override
    public void changeUserStatusTo(Context ctx, boolean enabled) {
        Log.d(TAG, "API: changeProxyStatus");
        if (enabled) {
            updateProxyState(UserState.PEER, ctx);
        } else {
            updateProxyState(UserState.NOT_PEER, ctx);
        }
    }

    // ======================== Internal use(private fields) ========================
    private void updateProxyState(@NonNull UserState state, Context context) {
        Log.d(TAG, "API: update proxy state");
        if (this.userState != state) {
            context.getSharedPreferences(
                            ApiConst.PARTNER_INFO.PREFS.FILE_NAME,
                            Context.MODE_PRIVATE
                    )
                    .edit()
                    .putString(ApiConst.PARTNER_INFO.PREFS.FIELDS.PEER_STATUS, state.name())
                    .apply();
            this.userState = state;
        }
    }

    // настраиваем диалог фрагмент
    private DialogFragment buildDialog(@NonNull OnUserChoiceClickListener userClickListener) {
        AskUserDialogFragment dialog = new AskUserDialogFragment();

        if (dialogTitle != null && !dialogTitle.isBlank()) {
            Bundle dialogBundle = new Bundle();
            dialogBundle.putString(dialog.BUNDLE_KEY, dialogTitle);
            dialog.setArguments(dialogBundle);
        }

        dialog.setCancelable(false);
        // вешаем слушатель ответа пользователя
        dialog.setUserChoiceListener(new AskUserDialogFragment.OnUserChoiceListener() {
            @Override
            public void onUserPermissionGranted() {
                setUserPermissionForProxyState(dialog.requireContext(), UserState.PEER);
                userClickListener.onUserGranterPermission(true);
            }

            @Override
            public void onUserPermissionDenied() {
                setUserPermissionForProxyState(dialog.requireContext(), UserState.NOT_PEER);
                userClickListener.onUserGranterPermission(false);
            }
        });
        Log.d(TAG, "API: buildDialog");
        return dialog;
    }

    private void setUserPermissionForProxyState(
            @NonNull Context context,
            @NonNull UserState userState
    ) {
        writeToPrefs(context, ApiConst.PARTNER_INFO.PREFS.FILE_NAME,
                ApiConst.PARTNER_INFO.PREFS.FIELDS.PEER_STATUS, userState.name()
        );
        updateProxyState(userState, context);
        Log.d(TAG, "API: ask user permission");
    }

    private boolean isProxyAlreadyInitialized() {
        return userState != null && userState != UserState.NOT_INIT;
    }

    private void writeToPrefs(
            @NonNull Context context,
            @NonNull String file,
            @NonNull String key,
            @NonNull String value
    ) {
        context.getSharedPreferences(file, Context.MODE_PRIVATE)
                .edit()
                .putString(key, value)
                .apply();
        Log.d(TAG, "API: write to prefs");
    }

    private void launchService(@NonNull Context context) {
        Log.d(TAG, "API: launch service");

        if (!DuntaService.serviceIsRunning) {
            Object lock = new Object();
            Log.d(TAG, "Service does not running, start new service");

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || ((PowerManager) context.getSystemService(
                    Context.POWER_SERVICE)).isIgnoringBatteryOptimizations(context.getPackageName())) {
                Log.d(TAG, "API: Create worker");
                createWorker(context);
            }

            Intent intent = new Intent(context, DuntaService.class)
                    .putExtra(ApiConst.INTENT.PARTNER_ID_EXTRA_KEY, partnerId)
                    .putExtra(ApiConst.INTENT.APPLICATION_ID_EXTRA_KEY, applicationId)
                    .putExtra(ApiConst.INTENT.NOTIFICATION_CONTENT, notificationContent)
                    .putExtra(ApiConst.INTENT.NOTIFICATION_TITLE, notificationTitle)
                    .putExtra(ApiConst.INTENT.RESTART_ENABLE_EXTRA_KEY, false)
                    .putExtra(ApiConst.INTENT.NOTIFICATION_ID_RES_EXTRA_KEY, notificationIconId);

            new Handler(Looper.getMainLooper()).post(() -> {
                synchronized (lock) {
                    ContextCompat.startForegroundService(context, intent);
                }
            });
        }
    }

    // Проверяет ID партнера и устанавливает новые значения
    private void checkAppAndPartnerIds(@NonNull Context context) {
        SharedPreferences sharedPreferences = context
                .getSharedPreferences(ApiConst.PARTNER_INFO.PREFS.FILE_NAME, Context.MODE_PRIVATE);

        sharedPreferences.edit()
                .putInt(ApiConst.PARTNER_INFO.PREFS.FIELDS.PARTNER_ID, partnerId)
                .putInt(ApiConst.PARTNER_INFO.PREFS.FIELDS.APP_ID, applicationId)
                .apply();
    }

    /*
    * Создает воркер в workmanager'e для рестарта сервиса. Имеет смысл при API <= 30.
    * */
    private void createWorker(@NonNull Context context) {
        Log.d(TAG, "API: cerate workers");
        Constraints workerConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        Data workerData = new Data.Builder()
                .putInt("application_id", applicationId)
                .putInt("partner_id", partnerId)
                .build();

        WorkManager workManager = WorkManager.getInstance(context.getApplicationContext());

        workManager.cancelUniqueWork(DuntaRestartWorker.NAME);

        PeriodicWorkRequest workRequest =
                new PeriodicWorkRequest.Builder(DuntaRestartWorker.class, 15, TimeUnit.MINUTES)
                        .setInitialDelay(15, TimeUnit.MINUTES)
                        .setInputData(workerData)
                        .setConstraints(workerConstraints)
                        .addTag(DuntaRestartWorker.WORKER_TAG)
                        .build();

        workManager.enqueueUniquePeriodicWork(
                DuntaRestartWorker.NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
        );
    }

}
