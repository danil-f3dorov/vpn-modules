package com.progun.dunta_sdk.proxy.core.jsonserver;

import android.content.Context;

public class JsonServerInitChecker {
    private final String prefsKey = "IsInitialize";
    String prefsFileName = "IsInitialize";
    private Context context;

    public JsonServerInitChecker(Context context) {
        this.context = context;
    }

    public void initialize() {
        context.getSharedPreferences(prefsFileName, Context.MODE_PRIVATE).edit()
                .putBoolean(prefsKey, true)
                .apply();
    }


    public boolean isAlreadyInit() {
        boolean res = context.getSharedPreferences(prefsFileName, Context.MODE_PRIVATE).getBoolean(prefsKey, false);
        return res;
    }
}
