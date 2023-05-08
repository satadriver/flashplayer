// ISvcAidlInterface.aidl
package com.adobe.flashplayer;

// Declare any non-default types here with import statements

interface ISvcAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    //双进程必须在同一个包名下

    String getServiceName();

    //void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,double aDouble, String aString);
}