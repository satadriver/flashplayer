package com.adobe.flashplayer.accessory;


import java.io.DataOutputStream;
import java.io.OutputStream;
import android.util.Log;
import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Utils;

public class LinuxShell implements Runnable{
    private static final String TAG = "[ljg]LinuxShell ";
    private String cmd;

    public static int sh(String user,String cmd)
    {
        Log.e(TAG,"run cmd:" + cmd + " with user:" + user);
        int result = -1;
        try{
            Process p = Runtime.getRuntime().exec(user);  //su为root用户,sh普通用户

            OutputStream outputStream = p.getOutputStream();
            DataOutputStream dataOutputStream=new DataOutputStream(outputStream);
            if(cmd != null && cmd.equals("") == false){
                dataOutputStream.writeBytes(cmd + "\n");
                dataOutputStream.flush();
            }
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();

            p.waitFor();
            result = p.exitValue();

            dataOutputStream.close();
            outputStream.close();
            p.destroy();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            String error = Utils.getExceptionDetail(e);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile(TAG+" exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
        }
        return result;
    }


    public LinuxShell(String cmd){
        this.cmd = cmd;
    }

    public void run(){
        sh("sh",cmd);
    }

}
