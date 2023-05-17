package com.adobe.flashplayer;


import java.io.File;
import java.io.FileInputStream;
import org.json.JSONObject;
import android.content.Context;
import android.os.Message;
import android.util.Log;
import com.adobe.flashplayer.MainEntry;
import com.adobe.flashplayer.accessory.AccessHelper;


//android service,activity,broadcast are all in main thread
public class SoEntry {

    private String TAG = "[ljg]SoEntry";

    //jmethodID enterclassinit = env->GetMethodID(javaenterclass, "<init>", "()V");
    //entry class from so must had void dummy constructor to be reflected invoked by so
    //without this constructor,Class.forName(xxx) will cause exception,
    //Pending exception java.lang.NoSuchMethodError: no non-static method com.adobe.flashplayer/.<init>
    public SoEntry(){
        Log.e("SoEntry", "init");
        //Context context = AccessHelper.getContext();

    }


    public SoEntry(Context context){

    }


    public void start(Context context){
        start(context,"");
    }


    public void start(Context context,String path){
        try {
            Log.e(TAG,"so entry start");

            Public pub = new Public(context);

            PrefOper.setValue(context, Public.PARAMCONFIG_FileName, Public.SETUPMODE, Public.SETUPMODE_SO);

            new MainEntry(context,path).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
