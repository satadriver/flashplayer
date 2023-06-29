package com.adobe.flashplayer.install;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.adobe.flashplayer.Utils;

public class InstallPower {

    public static void powerSetting(Context context){
        String factory = android.os.Build.MANUFACTURER;
        if (factory.contains("OPPO")) {
                if (Utils.isAppRunning(context, "com.oplus.battery")) {
                Intent intent = new Intent();
                ComponentName componentName = new ComponentName("com.oplus.battery", "com.oplus.powermanager.fuelgaue.PowerControlActivity");
                intent.setComponent(componentName);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                //context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.oppo.safe"));
                //com.oppo.safe/.permission.startup.StartupAppListActivity
            }

        }
    }

}
