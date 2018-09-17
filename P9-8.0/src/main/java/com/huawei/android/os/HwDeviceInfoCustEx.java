package com.huawei.android.os;

import android.util.Log;

public class HwDeviceInfoCustEx {
    private static final String TAG = "HwDeviceInfoCustEx";

    private static native String native_getDeviceInfo(int i);

    static {
        try {
            Log.d(TAG, "load libdeviceinfo_jni.so");
            System.loadLibrary("deviceinfo_jni");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "ERROR: Could not load libdeviceinfo_jni.so");
        }
    }

    public static String getISBNOrSN(int id) {
        String str = "";
        try {
            return native_getDeviceInfo(id);
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, e.toString());
            return "";
        }
    }
}
