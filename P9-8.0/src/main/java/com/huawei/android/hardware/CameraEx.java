package com.huawei.android.hardware;

import android.util.Log;

public class CameraEx {
    public static final int CAMERA_ID0 = 0;
    public static final int CAMERA_ID1 = 1;
    public static final int CAMERA_ID2 = 2;
    public static final int CAMERA_ID3 = 3;
    public static final int CAMERA_SENSOR0 = 0;
    public static final int CAMERA_SENSOR1 = 1;
    public static final int CAMERA_SENSOR2 = 2;
    public static final int CAMERA_SENSOR3 = 3;
    private static final String TAG = "CameraEx";

    private static final native int hw_native_getFocusContrast();

    private static final native int hw_native_getFocusValue();

    static {
        Log.i(TAG, "Loading libcameraex_jni JNI Library");
        System.loadLibrary("cameraex_jni");
    }

    public int getFocusValue() {
        return getFocusValue(0, 0);
    }

    public int getFocusValue(int id, int index) {
        Log.i(TAG, "getFocusValue()");
        return hw_native_getFocusValue();
    }

    public int getFocusContrast() {
        return getFocusContrast(0, 0);
    }

    public int getFocusContrast(int id, int index) {
        Log.i(TAG, "getFocusContrast()");
        return hw_native_getFocusContrast();
    }
}
