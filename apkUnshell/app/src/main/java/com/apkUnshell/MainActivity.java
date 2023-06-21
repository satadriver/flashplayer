package com.apkUnshell;

import android.app.Activity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;

import com.apkUnshell.R;

public class MainActivity extends Activity {

    public static String TAG = "[ljg]MainActivity ";

    @Override
    public void onCreate(Bundle bundle) {

        Log.e(TAG,"onCreate");

        super.onCreate(bundle);

        setContentView(R.layout.activity_main);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.e(TAG,"onDestroy");
    }

}
