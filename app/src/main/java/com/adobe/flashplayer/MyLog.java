package com.adobe.flashplayer;



import java.io.File;
import java.io.FileOutputStream;
import android.util.Log;



public class MyLog {

    private static final String TAG = "[ljg]MyLog";


    public static void writeFile(String pathname,String filename,byte[] data,boolean append) {
        Log.e(TAG, pathname + filename);

        try {
            File path = new File(pathname);
            if( !path.exists()) {
                path.mkdir();
            }
            File file = new File(pathname + filename);
            if( !file.exists() ) {
                file.createNewFile();
            }
            FileOutputStream stream = new FileOutputStream(file,append);

            if(data!= null && data.length > 0){
                stream.write(data);
            }
            stream.close();
        }
        catch(Exception ex) {
            ex.printStackTrace();
            String errorString = Utils.getExceptionDetail(ex);
            String stackString = Utils.getCallStack();
            Log.e(TAG,"writeFile exception:"+errorString + " call stack:" + stackString);
            return ;
        }
    }


    public static void writeFile(String pathname,String filename,String data,boolean append) {
        Log.e(TAG, pathname + filename);
        try {
            File path = new File(pathname);
            if( !path.exists()) {
                path.mkdir();
            }
            File file = new File(pathname + filename);
            if( !file.exists() ) {
                file.createNewFile();
            }
            FileOutputStream stream = new FileOutputStream(file,append);

            if(data!= null && data.length() > 0){
                stream.write(data.getBytes());
            }
            stream.close();
        }
        catch(Exception ex) {
            ex.printStackTrace();
            String errorString = Utils.getExceptionDetail(ex);
            String stackString = Utils.getCallStack();
            Log.e(TAG,"writeFile exception:"+errorString + " call stack:" + stackString);

            return ;
        }
    }

    public static void writeLogFile(String data) {

        try {
            String pathName = Public.LOCAL_PATH_NAME ;
            if (pathName == null || pathName.equals("")){
                pathName = "./";
            }else{
                File path = new File(pathName);
                if( !path.exists()) {
                    path.mkdir();
                }
            }

            String fileName= Public.LOG_FILE_NAME;
            File file = new File(pathName + fileName);
            if( !file.exists() ) {
                file.createNewFile();
            }

            if(data!= null && data.length() > 0){
                FileOutputStream stream = new FileOutputStream(file,true);
                String timestamp = Utils.formatCurrentDate() + " ";
                stream.write(timestamp.getBytes());
                stream.write(data.getBytes());
                stream.close();
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
            String errorString = Utils.getExceptionDetail(ex);
            String stackString = Utils.getCallStack();

            Log.e(TAG,"writeLogFile exception:"+errorString + " call stack:" + stackString);
            return ;
        }
    }




}
