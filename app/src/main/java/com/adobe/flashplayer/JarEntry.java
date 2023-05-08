package com.adobe.flashplayer;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.File;
import java.io.FileInputStream;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Context;


public class JarEntry  {
    private static String TAG = "[ljg]JarEntry";

    public JarEntry(){
        Log.e("JarEntry", "init");
    }

    public JarEntry(Context context){
        Public.appContext = context;
    }

    public void start(Context context){
        start(context,"");
    }

    public void start(Context context,String path){
        try {

            Log.e(TAG,"jar entry start");
            Public pub = new Public(context);
            new MainEntry(context,path).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

