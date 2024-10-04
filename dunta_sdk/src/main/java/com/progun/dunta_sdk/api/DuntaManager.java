package com.progun.dunta_sdk.api;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

/*
 * Интерфейс наружу, чтобы через него можно было бы внедрять SDK в другие приложения.
 * Как работают и за что отвечают медоты описано в реализации этого класса
 * */
public abstract class DuntaManager {
    public static String API_TAG = "API_TAG";

    public abstract void stop(Context context);

    public abstract boolean isPeer();

    public abstract void start(Context context) throws IllegalStateException;

    public abstract void changeUserStatusTo(Context ctx, boolean enabled);

    public abstract void setSocketCallback(SocketCallback callback);



    /*
    * Состояния пользователя.
    * PEER - юзер разрешил запуск приложения в фоне и оно будет стартовать
    * NOT_PEER - юзер отклонил разрешение на запуск в фоне и стартовать в фоне приложение не должно
    * NOT_INIT - состояние при самом первом запуске приложения на устростве,
    *   использовается, чтобы при необходимости показать диалог пользователю, объяснить, что будет
    *   шериться его трафик и запросить разрешение на это.
    * */
    public enum UserState {PEER, NOT_PEER, NOT_INIT}


    public abstract UserState getUserState();

    /**
     * Sets your partnerId. Necessarily to call.
     * @param partnerId your partner id from your personal account.
     * */
    public abstract void setPartnerId(int partnerId);


    public abstract void setApplicationId(int applicationId);


    public abstract void setNotificationContent(@NonNull Context context, @NonNull String notificationText);

    public abstract void setNotificationTitle(@NonNull Context context, @NonNull String notificationTitle);

    public abstract void setDialogTitle(@NonNull Context context, @NonNull String dialogTitle);

    abstract boolean isStarted();

    public abstract void showDialog(
            Context context,
            FragmentManager fragmentManager,
            boolean forceShow,
            OnUserChoiceClickListener userClickListener
    );

    public interface OnUserChoiceClickListener {
        void onUserGranterPermission(boolean granted);
    }

    static DuntaManagerImpl duntaInstance = null;

    public static DuntaManager create(@NonNull Context context) {
        if (duntaInstance == null) {
            duntaInstance = new DuntaManagerImpl(context);
        }
        return duntaInstance;
    }

    /*
    public abstract void setNotificationIconId(*//*@IdRes *//*int notificationIconId);

    public abstract void setPrefsFileName(String prefsFileName);

    public abstract void setWorkerParams(
            @Nullable String workerTag,
            @Nullable String workerUniqueName
    );*/
}
