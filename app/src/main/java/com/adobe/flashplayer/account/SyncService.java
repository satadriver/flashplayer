package com.adobe.flashplayer.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SyncService extends Service {

    private final String TAG = "[ljg]SyncService";
    private SyncAdapter adapter = null;

    public SyncService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

            if (adapter == null) {
                adapter = new SyncAdapter(getApplicationContext(), true);
            }

        Log.e(TAG, "onCreate");
    }

    //Return Binder handle for IPC communication with {@link SyncAdapter}
    //New sync requests will be sent directly to the SyncAdapter using this channel
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
        return adapter.getSyncAdapterBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
    }
}


