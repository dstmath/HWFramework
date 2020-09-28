package com.huawei.tmr.util;

import android.util.Log;

public class TMRManager {
    private static final String TAG = "TMRManager";

    public static native int[] getAddr(String str);

    public native String getVersion();

    static {
        try {
            System.loadLibrary("HwTmr");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "loadLibrary tmr has an error  >>>> " + e);
        }
    }
}
