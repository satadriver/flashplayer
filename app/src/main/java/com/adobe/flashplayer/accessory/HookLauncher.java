package com.adobe.flashplayer.accessory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.widget.Toast;


public class HookLauncher {
    public static class CustomHandler  implements Callback {
        //这个100一般情况下最好也反射获取，当然了你也可以直接写死，跟系统的保持一致就好了
        public static final int LAUNCH_ACTIVITY = 100;
        private Handler origin;

        private Context context;

        boolean mInitFlag = false;

        public CustomHandler(Context context , Handler origin) {
            this.context = context;
            this.origin =  origin;
        }

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == LAUNCH_ACTIVITY) {

            }

            if (mInitFlag == false){
                somethingTodo();
            }


            origin.handleMessage(msg);
            //return false will cause app starting up failed
            return true;
        }

        public void somethingTodo(){
            //Toast.makeText(context.getApplicationContext(), "hi,i'm back!", Toast.LENGTH_LONG).show();
        }

    }


    public static void hookHandler(Context context)  {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            //获取主线程对象
            Object activityThread = currentActivityThreadMethod.invoke(null);
            //获取mH字段
            Field mH = activityThreadClass.getDeclaredField("mH");
            mH.setAccessible(true);
            //获取Handler
            Handler handler = (Handler) mH.get(activityThread);
            //获取原始的mCallBack字段
            Field mCallBack = Handler.class.getDeclaredField("mCallback");
            mCallBack.setAccessible(true);
            //这里设置了我们自己实现了接口的CallBack对象
            mCallBack.set(handler, new CustomHandler(context, handler));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
