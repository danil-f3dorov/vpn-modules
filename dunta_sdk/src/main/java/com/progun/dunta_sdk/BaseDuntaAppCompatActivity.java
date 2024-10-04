package com.progun.dunta_sdk;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.progun.dunta_sdk.api.DuntaManager;

public class BaseDuntaAppCompatActivity extends AppCompatActivity {
    public DuntaManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w("TestSDKapi", "onCreate()");
        Log.w("TestSDKapi", "savedInstanceState==null");
        pm = DuntaManager.create(this);
    }
}