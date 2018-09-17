package com.android.internal.os;

import android.util.Slog;

public class HwBootAnimationOeminfo {
    private static final boolean DEBUG_NATIVE = false;
    private static final int OEMINFO_BOOTANIM_RINGDEX = 107;
    private static final int OEMINFO_BOOTANIM_RINGMODE = 106;
    private static final int OEMINFO_BOOTANIM_SHUTFLAG = 105;
    private static final int OEMINFO_BOOTANIM_SWITCH = 104;
    private static final String TAG = "HwBootAnimationNative";
    private static final Object mLock = new Object();

    private static native int nativeGetBootAnimationParam(int i);

    private static native int nativeSetBootAnimationParam(int i, int i2);

    public static int setBootAnimSoundSwitch(int value) {
        try {
            int nativeSetBootAnimationParam;
            synchronized (mLock) {
                nativeSetBootAnimationParam = nativeSetBootAnimationParam(104, value);
            }
            return nativeSetBootAnimationParam;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "HwBootAnimationNative error >>>>>" + e);
            return -1;
        }
    }

    public static int getBootAnimSoundSwitch() {
        try {
            int nativeGetBootAnimationParam;
            synchronized (mLock) {
                nativeGetBootAnimationParam = nativeGetBootAnimationParam(104);
            }
            return nativeGetBootAnimationParam;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "HwBootAnimationNative error >>>>>" + e);
            return -1;
        }
    }

    public static int setBootAnimShutFlag(int value) {
        try {
            int nativeSetBootAnimationParam;
            synchronized (mLock) {
                nativeSetBootAnimationParam = nativeSetBootAnimationParam(105, value);
            }
            return nativeSetBootAnimationParam;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "HwBootAnimationNative error >>>>>" + e);
            return -1;
        }
    }

    public static int getBootAnimShutFlag() {
        try {
            int nativeGetBootAnimationParam;
            synchronized (mLock) {
                nativeGetBootAnimationParam = nativeGetBootAnimationParam(105);
            }
            return nativeGetBootAnimationParam;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "HwBootAnimationNative error >>>>>" + e);
            return -1;
        }
    }

    public static int setBootAnimRingMode(int value) {
        try {
            int nativeSetBootAnimationParam;
            synchronized (mLock) {
                nativeSetBootAnimationParam = nativeSetBootAnimationParam(106, value);
            }
            return nativeSetBootAnimationParam;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "HwBootAnimationNative error >>>>>" + e);
            return -1;
        }
    }

    public static int setBootAnimRing(int value) {
        try {
            int nativeSetBootAnimationParam;
            synchronized (mLock) {
                nativeSetBootAnimationParam = nativeSetBootAnimationParam(107, value);
            }
            return nativeSetBootAnimationParam;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "HwBootAnimationNative error >>>>>" + e);
            return -1;
        }
    }
}
