package com.huawei.android.hwaps;

import android.util.Log;

public class HwApsInterface {
    private static final String TAG = "Hwaps";

    public static native void nativeFpsControllerRelease(long j);

    public static native void nativeFpsRequestRelease(long j);

    public static native int nativeGetCurFPS(long j);

    public static native int nativeGetTargetFPS(long j);

    public static native long nativeInitFpsController();

    public static native long nativeInitFpsRequest(long j);

    public static native void nativePowerCtroll(long j);

    public static native void nativeSetGamePid(int i);

    public static native void nativeSetUIFrameState(boolean z);

    public static native void nativeStart(long j, int i);

    public static native void nativeStartFeedback(long j, int i);

    public static native void nativeStop(long j);

    static {
        try {
            System.loadLibrary("hwaps");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "LoadLibrary occurs error " + e.toString());
        }
    }
}
