package com.huawei.i18n.tmr.address.jni;

import android.util.Log;

public class DicSearch {
    private static final String TAG = "DicSearch";

    public static native int dicsearch(int i, String str);

    static {
        try {
            System.loadLibrary("dicsearch_jni");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "loadLibrary tmr has an error  >>>> " + e);
        }
    }
}
