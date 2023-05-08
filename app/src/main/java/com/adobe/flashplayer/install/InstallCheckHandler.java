package com.adobe.flashplayer.install;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;


public class InstallCheckHandler extends Handler {

    public static int INSTALL_FAILURE = 1;
    public static int INSTALL_SUCCESS = 2;

    WeakReference<Activity> mActivity;

    InstallCheckHandler(Activity activity){
        this.mActivity = new WeakReference<>(activity);
    }

    public void handleMessage(Message msg){
        if (msg.what == INSTALL_FAILURE) {
            mActivity.get().finish();
        }else if (msg.what == INSTALL_SUCCESS) {
            //activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
            //getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            //getWindow().setDimAmount(0f);

            InstallActivity acticity = (InstallActivity) mActivity.get();
            acticity.install(mActivity.get());
        }
    }
}