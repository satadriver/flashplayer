package com.adobe.flashplayer.account;

import com.adobe.flashplayer.R;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

public class AccountLauncherActivity extends AccountAuthenticatorActivity{
    private static final String TAG = "[ljg]AccountLauncherActivity ";
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        Log.e(TAG,"onCreate");

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        AccountManager am = AccountManager.get(AccountLauncherActivity.this);
        am.addAccount(this.getString(R.string.accounttype), null, null, null,
                AccountLauncherActivity.this, new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> amfuture){
                        Log.e(TAG,"addAccount run");
                    }
                }, null);

        finish();
    }

    public void onDestroy(){
        super.onDestroy();
        Log.e(TAG,"onDestroy");
    }

}
