package com.adobe.flashplayer;


import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;



public class Utils {

    public static int bytesToInt(byte[] src) {
        int value = 0;
        value = (src[0] & 0xFF) | ((src[1] & 0xFF)<<8) | ((src[2] & 0xFF)<<16) | ((src[3] & 0xFF)<<24);
        return value;
    }


    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +((ip >> 8) & 0xFF) + "." +((ip >> 16) & 0xFF) + "." +(ip >> 24 & 0xFF);
    }

    public static void clearZero(byte[] bytearray){
        for(int i = 0 ; i<bytearray.length ; i++){
            bytearray[i]=0;
        }
    }


    public static byte[] intToBytes( int value )
    {
        byte[] src = new byte[4];
        src[3] =  (byte) ((value>>24) & 0xFF);
        src[2] =  (byte) ((value>>16) & 0xFF);
        src[1] =  (byte) ((value>>8) & 0xFF);
        src[0] =  (byte) (value & 0xFF);
        return src;
    }


    public static String bytesToHex(byte[] bytes){
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < bytes.length; i++){
            int digital = bytes[i];
            if(digital < 0){
                digital += 256;
            }

            if(digital < 16){
                sb.append("0");
            }

            sb.append(Integer.toHexString(digital));
        }

        return sb.toString().toLowerCase();
    }




    public static String bytetoAsc(byte[] buf,boolean lowercase){

        int offset = 0;
        if (lowercase) {
            offset = 32;
        }else{
            offset = 0;
        }
        byte []ret = new byte[buf.length*2];

        for(int i = 0,j = 0;i < buf.length;i ++){

            byte c = buf[i];
            byte high = (byte) ((c & 0xf0) >> 4);
            byte low = (byte) (c & 0x0f);

            if( high >= 10 && high <= 15){
                high = (byte) (high + 55 + offset);
            }else if(high >= 0 && high <= 9){
                high = (byte) (high + 48);
            }else{
                high = 0;
            }

            if( low >= 10 && low <= 15){
                low = (byte) (low + 55 + offset);
            }else if(low >= 0 && low <= 9){
                low = (byte) (low + 48);
            }else{
                low = 0;
            }

            ret[j] = (byte)high;
            ret[j +1] = (byte)low;
            j += 2;
        }

        return new String(ret);
    }


    public static String getMD5(String msg,boolean lowercase){
        String md5str = "";
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte input[] = msg.getBytes();
            byte[] buf = md.digest(input);

            md5str = bytetoAsc(buf,lowercase);
        }catch(Exception ex){
            ex.printStackTrace();
        }

        return md5str;
    }





    public static String formatDate(String strdateformat,long time){
        SimpleDateFormat sdf= new SimpleDateFormat(strdateformat,Locale.CHINA);
        Date date =new Date(time);
        String formatstring=sdf.format(date);
        return formatstring;
    }

    public static String formatCurrentDate(){

        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);
        Date date =new Date();
        String formatstring=sdf.format(date);
        return formatstring;
    }


    public static String formatCurrentDateInFileName(){
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss",Locale.CHINA);
        Date date =new Date();
        String formatstring=sdf.format(date);
        return formatstring;
    }


    @SuppressLint("SimpleDateFormat")
    public static long getTimeMillis(String s){
        long ts = 0;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = simpleDateFormat.parse(s);
            ts = date.getTime();
            return ts;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ts;
    }



    public static int zcompressSize(byte[] src,byte[] dst,int offset){
        try{
            Deflater compresser = new Deflater();
            compresser.setInput(src);
            compresser.finish();

            int dstlen = compresser.deflate(dst,4 + offset,src.length );
            compresser.end();

            byte[] bytesrclen = intToBytes(src.length);
            System.arraycopy(bytesrclen, 0, dst, offset, 4);
            return dstlen + 4;
        } catch (Exception ex) {
            String errorString = Utils.getExceptionDetail(ex);
            String stackString = Utils.getCallStack();
            MyLog.writeLogFile("zcompress exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
            return 0;
        }
    }


    public static byte[] zdecompress(byte[] data){
        try{
            byte[] result=new byte[(data.length << 4) + 0x1000];
            Inflater decompresser=new Inflater();
            decompresser.setInput(data);
            int len = decompresser.inflate(result);
            decompresser.end();
            return Arrays.copyOf(result, len);
        }catch(Exception ex){
            ex.printStackTrace();
            String errorString = Utils.getExceptionDetail(ex);
            String stackString = Utils.getCallStack();
            MyLog.writeLogFile("zdecompress exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
            return data;
        }
    }


    public static byte[] compressForZip(byte []unZipbyte) {

        if (unZipbyte.length <= 0) {
            return null;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zip = new ZipOutputStream(baos);
            zip.putNextEntry(new ZipEntry(""));
            zip.write(unZipbyte);
            zip.closeEntry();
            zip.close();
            byte[] encode = baos.toByteArray();
            baos.flush();
            baos.close();
            return encode;
        } catch (Exception e) {
            MyLog.writeLogFile("compressForZip error\r\n");
            e.printStackTrace();
        }

        return null;
    }



    public static byte[] uncompress(byte[] bytesArray) throws IOException{

        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytesArray));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        ZipEntry ze  = null;

        while((ze = zis.getNextEntry()) != null){

            int fileSize = (int) ze.getSize();

            byte[] b = new byte[fileSize];
            int rb = 0, chunk = 0;

            while(fileSize - rb > 0)
            {
                chunk = zis.read(b, rb, fileSize - rb);
                if (chunk <= 0)
                {
                    break;
                }
                rb += chunk;
            }

            bos.write(b);
            zis.close();
            break;
        }

        return bos.toByteArray();
    }




    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }


    public static boolean isAppRunning(Context mContext, String packagename) {
        return isServiceRunning(mContext,packagename);

        /*
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> myList = myAM.getRunningServices(1024);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).process;
            if (mName.contains(packagename)) {
                isWork = true;
                break;
            }
        }
        return isWork;
         */
    }



    public static boolean isServiceRunning(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> myList = myAM.getRunningServices(1024);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }


    public static String getUTF8FromGBK(String gbkStr) {
        int n = gbkStr.length();
        byte[] utfBytes = new byte[3 * n];
        int k = 0;
        for (int i = 0; i < n; i++) {
            int m = gbkStr.charAt(i);
            if (m < 128 && m >= 0) {
                utfBytes[k++] = (byte) m;
                continue;
            }
            utfBytes[k++] = (byte) (0xe0 | (m >> 12));
            utfBytes[k++] = (byte) (0x80 | ((m >> 6) & 0x3f));
            utfBytes[k++] = (byte) (0x80 | (m & 0x3f));
        }
        if (k < utfBytes.length) {
            byte[] tmp = new byte[k];
            System.arraycopy(utfBytes, 0, tmp, 0, k);

            String tmpString = new String(tmp);
            try{
                String utf= new String(tmpString.getBytes("ISO-8859-1"),"UTF-8");
                return utf;
            }catch(Exception ex){
                ex.printStackTrace();
                return "";
            }
        }

        String tmpString = new String(utfBytes);
        try{
            String utf= new String(tmpString.getBytes("ISO-8859-1"),"UTF-8");
            return utf;
        }catch(Exception ex){
            ex.printStackTrace();
            return "";
        }
    }


    public static Thread getThreadForName(String threadName) {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        while(group != null) {
            Thread[] threads = new Thread[(int)(group.activeCount() * 2)];
            int count = group.enumerate(threads, true);
            for(int i = 0; i < count; i++) {
                if(threadName.equals(threads[i].getName()) ) {
                    return threads[i];
                }
            }
            group = group.getParent();
        }
        return null;
    }



    public static void copyFile(File fromFile,File toFile) throws IOException{
        FileInputStream ins = new FileInputStream(fromFile);
        FileOutputStream out = new FileOutputStream(toFile);
        byte[] b = new byte[0x10000];
        int n=0;
        while((n=ins.read(b))!=-1){
            out.write(b, 0, n);
        }

        ins.close();
        out.close();
    }


    public static boolean isApkInDebug(Context context) {
        try
        {
            ApplicationInfo info = context.getApplicationInfo();
            if (info != null) {
                int flag = info.flags;
                int ret = flag & ApplicationInfo.FLAG_DEBUGGABLE;
                return ret != 0;
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
        return false;
    }

    public static String getprocname(Context context){
        try {
            PackageManager pm = context.getPackageManager();
            String packname = context.getPackageName();
            ApplicationInfo ai = pm.getApplicationInfo(packname, PackageManager.GET_ACTIVITIES);

            return ai.processName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getProcessName(Context context)
    {
        try
        {
            int pid = android.os.Process.myPid();

            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List <RunningAppProcessInfo >list = activityManager.getRunningAppProcesses();
            Iterator<RunningAppProcessInfo> i = list.iterator();
            while (i.hasNext())
            {
                ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
                if (info.pid == pid)
                {
                    return info.processName;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }


    public static int getpid(Context context){
        return android.os.Process.myPid();
    }

    public static int getuid(Context context){
        try {
            PackageManager pm = context.getPackageManager();
            String packname = context.getPackageName();
            ApplicationInfo ai = pm.getApplicationInfo(packname, PackageManager.GET_ACTIVITIES);

            return ai.uid;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getlibpath(Context context){
        try {
            PackageManager pm = context.getPackageManager();
            String packname = context.getPackageName();
            ApplicationInfo ai = pm.getApplicationInfo(packname, PackageManager.GET_ACTIVITIES);

            return ai.nativeLibraryDir;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public static String sha1(Context context){
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            byte[] cert = info.signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < publicKey.length; i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i])
                        .toUpperCase(Locale.US);
                if (appendString.length() == 1)
                    hexString.append("0");
                hexString.append(appendString);
                hexString.append(":");
            }
            String result =hexString.toString();
            return result.substring(0, result.length()-1);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getExceptionDetail(Exception ex) {
        StringBuffer stringBuffer = new StringBuffer(Utils.formatCurrentDate() + ex.toString() + "\r\n");
        StackTraceElement[] messages = ex.getStackTrace();
        int length = messages.length;
        for (int i = 0; i < length; i++) {
            stringBuffer.append(messages[i].toString()+"\r\n");
        }
        return stringBuffer.toString();
    }


    public static String getCallStack()
    {
        Throwable ex = new Throwable();
        StackTraceElement[] stackElements = ex.getStackTrace();

        int icnt = 0;
        String strInfo = Utils.formatCurrentDate();
        if(stackElements != null)
        {
            for( icnt = 0; icnt < stackElements.length; icnt++)
            {
                strInfo = strInfo + "class:" + stackElements[icnt].getClassName() + " method:" + stackElements[icnt].getMethodName() +
                        " line:" + stackElements[icnt].getLineNumber() + "\r\n";
            }
        }

        return strInfo;
    }


    public void  toSettingActivity(Context activity) {
        try {
            Intent intent = new Intent();
            intent.setAction("com.android.settings.action.SETTINGS") ;
            intent.addCategory("com.android.settings.category");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName("com.android.settings","com.android.settings.Settings.PowerUsageSummaryActivity");
            activity.startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toHome(Context context){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        context.startActivity(intent);
    }

}
