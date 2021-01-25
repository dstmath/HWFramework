package com.huawei.itouch;

import android.util.Log;

public class HwITouchJniAdapter {
    private static HwITouchJniAdapter sHwITouchJniAdapter = null;

    private native int nativeGetAppType();

    private native void nativeRegisterListener();

    public static synchronized HwITouchJniAdapter getInstance() {
        HwITouchJniAdapter hwITouchJniAdapter;
        synchronized (HwITouchJniAdapter.class) {
            if (sHwITouchJniAdapter == null) {
                sHwITouchJniAdapter = new HwITouchJniAdapter();
            }
            hwITouchJniAdapter = sHwITouchJniAdapter;
        }
        return hwITouchJniAdapter;
    }

    public synchronized void registerJniListener() {
        nativeRegisterListener();
    }

    public synchronized int getAppType() {
        return nativeGetAppType();
    }

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
}
