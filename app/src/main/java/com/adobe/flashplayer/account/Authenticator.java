package com.adobe.flashplayer.account;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

public class Authenticator extends AbstractAccountAuthenticator {
    private final String TAG = "Authenticator";
    Context context;

    public Authenticator(Context context) {
        super(context);
        this.context = context;
        Log.e(TAG,"Authenticator");
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
        Log.e(TAG,"editProperties");
        return null;
    }

    //这个addAccount()在用户进入设置-账户-添加账户的时候触发的，这里面把自己设置账户的页面的信息封装给bundle，然后传出去即可。
    //如果返回null表示不做任何触发
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    @SuppressLint("InlinedApi")
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String s, String s1, String[] strings, Bundle b) throws NetworkErrorException {
        Log.e(TAG,"[ljg]addAccount");

        Bundle bundle = new Bundle();
        Intent intent = new Intent(context, AccountActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
        Log.e(TAG,"confirmCredentials");
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
        Log.e(TAG,"getAuthToken");
        return null;
    }

    @Override
    public String getAuthTokenLabel(String s) {
        Log.e(TAG,"getAuthTokenLabel");
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
        Log.e(TAG,"updateCredentials");
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
        Log.e(TAG,"hasFeatures");
        return null;
    }
}
