package com.adobe.flashplayer.install;


        import com.adobe.flashplayer.R;
        import com.adobe.flashplayer.Utils;

        import android.app.Activity;
        import android.content.ActivityNotFoundException;
        import android.content.ComponentName;
        import android.content.Context;
        import android.content.Intent;
        import android.net.Uri;
        import android.os.Build;
        import android.util.Log;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.Toast;

public class InstallBootup implements OnClickListener{

    private String TAG = "SetupRebootup";
    private Activity context;

    public InstallBootup(Activity context){
        this.context = context;
        Log.e(TAG, "SetupRebootup");
    }

    @Override
    public void onClick(View v) {
        try {
            String factory = android.os.Build.MANUFACTURER;
            if (factory.contains("Xiaomi")) {

                InstallAuthority.EMUI.bootup(context);
            }
            else if (factory.contains("ZTE")) {
                if (Utils.isAppRunning(context, "com.zte.heartyservice")) {
                    Intent intent = new Intent();
                    //intent.setAction("com.zte.powersavemode.autorunmanager");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ComponentName comp = new ComponentName("com.zte.heartyservice", "com.zte.heartyservice.autorun.AppAutoRunManager");
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

                InstallAuthority.Funtouch.bootup(context);
            }
            else if (factory.contains("samsung")) {
                if (Utils.isAppRunning(context, "com.samsung.android.sm_cn")) {
                    //context.startActivity(getPackageManager().getLaunchIntentForPackage("com.samsung.android.sm_cn"));
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ComponentName cn = new ComponentName("com.samsung.android.sm_cn", "com.samsung.android.sm.ui.ram.AutoRunActivity");
                    intent.setComponent(cn);
                    context.startActivity(intent);
                }
                else{
                    InstallAuthority.defaultSettings(context);
                }
            }
            else if (factory.contains("Letv")) {
                if (Utils.isServiceRunning(context, "com.letv.android.letvsafe")) {
                    Intent intent = new Intent();
                    //intent.setAction("com.letv.android.permissionautoboot");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ComponentName comp = new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity");
                    intent.setComponent(comp);
                    context.startActivity(intent);
                    //context.startActivity(getPackageManager().getLaunchIntentForPackage("com.letv.android.letvsafe"));
                }
                else if(Utils.isServiceRunning(context, "com.letv.android.supermanager")){
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ComponentName comp = new ComponentName("com.letv.android.supermanager", "com.letv.android.supermanager.activity.PermissionManagerActivity");
                    intent.setComponent(comp);
                    context.startActivity(intent);
                }
                else{
                    InstallAuthority.defaultSettings(context);
                }
            }
            else if (factory.contains("360")) {
                if (Utils.isAppRunning(context, "com.qihoo360.mobilesafe")) {
                    context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.qihoo360.mobilesafe"));
                }
                else{
                    InstallAuthority.defaultSettings(context);
                }
            }
            else if (factory.contains("Meizu")) {
                InstallAuthority.Meizu.bootup(context);
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
                InstallAuthority.ColorsOS.bootup(context);
            }
            else if (factory.contains("ulong")) {
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
            else if (factory.contains("Yulong")) {
                if (Utils.isAppRunning(context, "com.yulong.android.coolsafe")) {
                    context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.yulong.android.coolsafe"));
                }
                else{
                    InstallAuthority.defaultSettings(context);
                }
            }
            else if(factory.contains("Coolpad")){
                if(Utils.isAppRunning(context, "com.yulong.android.security")) {
                    //context.startActivity(getPackageManager().getLaunchIntentForPackage("com.yulong.android.security.ui.activity.AppManagerActivty"));
                    Intent intent = new Intent();
                    ComponentName componentName = new ComponentName("com.yulong.android.security", "com.yulong.android.security.ui.activity.AppManagerActivty");
                    intent.setComponent(componentName);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
            else if (factory.contains("HUAWEI")) {
                InstallAuthority.EMUI.bootup(context);
            }
            else if (factory.contains("OnePlus")) {
                InstallAuthority.defaultSettings(context);
            }
            else if(factory.contains("GIONEE")){
                if (Utils.isAppRunning(context, "com.gionee.softmanager")) {
                    //context.startActivity(getPackageManager().getLaunchIntentForPackage("com.huawei.systemmanager"));
                    Intent intent = new Intent();
                    try{
                        ComponentName componentName = new ComponentName("com.gionee.softmanager", "com.gionee.softmanager.softmanager.AutoStartMrgActivity");
                        intent.setComponent(componentName);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                    catch(ActivityNotFoundException ex){
                        ex.printStackTrace();
                    }
                }
                else{
                    InstallAuthority.defaultSettings(context);
                }
            }
            else{
                InstallAuthority.defaultSettings(context);
            }

            Toast.makeText(context,
                    "请设置\""+ context.getString(R.string.app_name)+"\"开机启动",Toast.LENGTH_LONG).show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }













}
