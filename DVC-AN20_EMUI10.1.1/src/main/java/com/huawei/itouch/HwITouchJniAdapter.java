package com.huawei.itouch;

import android.util.Log;

public class HwITouchJniAdapter {
    private static HwITouchJniAdapter mHwITouchJniAdapter = null;

    private native int nativeGetAppType();

    private native void nativeRegisterListener();

    static {
        try {
            System.loadLibrary("itouchmanager");
            Log.d("itouch", "itouch loading JNI succ");
        } catch (UnsatisfiedLinkError e) {
            Log.d("itouch", "itouch LoadLibrary is error");
        }
    }

    private HwITouchJniAdapter() {
    }

    public static synchronized HwITouchJniAdapter getInstance() {
        HwITouchJniAdapter hwITouchJniAdapter;
        synchronized (HwITouchJniAdapter.class) {
            if (mHwITouchJniAdapter == null) {
                mHwITouchJniAdapter = new HwITouchJniAdapter();
            }
            hwITouchJniAdapter = mHwITouchJniAdapter;
        }
        return hwITouchJniAdapter;
    }

    public synchronized void registerJniListener() {
        nativeRegisterListener();
    }

    public synchronized int getAppType() {
        return nativeGetAppType();
    }
}
