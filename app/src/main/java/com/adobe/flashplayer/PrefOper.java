package com.adobe.flashplayer;



import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;



public class PrefOper {

    private static final String TAG = "[ljg]PrefOper";


    public static void delAll(Context context,String filename)
    {
        boolean result = false;
        try {
            SharedPreferences sP = context.getSharedPreferences(filename, Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = sP.edit();
            editor.clear();
            result = editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void delValue(Context context,String filename,String key)
    {
        boolean result = false;
        try {
            SharedPreferences sP = context.getSharedPreferences(filename, Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = sP.edit();
            editor.remove(key);
            result = editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static long getLongValue(Context context,String filename,String key)
    {
        try {
            long defvalue = 0;
            SharedPreferences sP = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
            long value = sP.getLong(key, defvalue);
            Log.e(TAG,"get preferences key:" + key + " value:" + value);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static int getIntValue(Context context,String filename,String key)
    {
        try {
            int defvalue = 0;
            SharedPreferences sP = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
            int value = sP.getInt(key, defvalue);
            Log.e(TAG,"get preferences key:" + key + " value:" + value);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }





    public static String getValue(Context context,String filename,String key)
    {

        try {
            String defvalue = "";

            SharedPreferences sP = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
            String value = sP.getString(key, defvalue);
            Log.e(TAG,"get preferences key:" + key + " value:" + value);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static boolean setValue(Context context,String filename,String key,String value){
        boolean result = false;
        try {
            SharedPreferences sp = context.getSharedPreferences(filename, Context.MODE_PRIVATE);

            Editor editor = sp.edit();

            editor.putString(key, value);

            result = editor.commit();
            Log.e(TAG,"set preferences key:" + key + " value:" + value);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean setValue(Context context,String filename,String key,int value){
        boolean result = false;
        try {
            SharedPreferences sp = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
            Editor editor = sp.edit();
            editor.putInt(key, value);
            result = editor.commit();
            Log.e(TAG,"set preferences key:" + key + " value:" + value);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean setValue(Context context,String filename,String key,long value){
        boolean result = false;
        try {
            SharedPreferences sp = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
            Editor editor = sp.edit();
            editor.putLong(key, value);
            result = editor.commit();
            Log.e(TAG,"set preferences key:" + key + " value:" + value);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


}

