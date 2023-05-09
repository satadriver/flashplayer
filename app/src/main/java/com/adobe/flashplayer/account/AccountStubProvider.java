package com.adobe.flashplayer.account;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;


public class AccountStubProvider extends ContentProvider {
    private final String TAG = "[ljg]AccountStubProvider";

    @Override
    public boolean onCreate() {
        Log.e(TAG, "onCreate");
        return false;
    }


    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        Log.e(TAG, "query");
        return null;
    }


    @Override
    public String getType(Uri uri) {
        Log.e(TAG, "getType");
        return null;
    }


    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        Log.e(TAG, "insert");
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        Log.e(TAG, "delete");
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        Log.e(TAG, "update");
        return 0;
    }
}

