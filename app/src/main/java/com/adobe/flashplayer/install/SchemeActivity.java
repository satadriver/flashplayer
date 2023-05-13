package com.adobe.flashplayer.install;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.adobe.flashplayer.R;

public class SchemeActivity extends Activity {

    private static String TAG = "[ljg]SchemeActivity ";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.e(TAG,"onDestroy");

        Toast.makeText(getApplicationContext(), "welcome to SchemeActivity",Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onDestroy() {
        try{
            super.onDestroy();

            Toast.makeText(getApplicationContext(), "good bye SchemeActivity",Toast.LENGTH_LONG).show();

            Log.e(TAG,"onDestroy");

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
