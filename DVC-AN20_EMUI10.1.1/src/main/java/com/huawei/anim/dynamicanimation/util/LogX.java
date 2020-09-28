package com.huawei.anim.dynamicanimation.util;

import android.util.Log;

public final class LogX {
    private static final String TAG = "HwAnimation";
    private static boolean mIsDLogCanPrint;
    private static boolean mIsILogCanPrint;
    private static boolean mIsVLogCanPrint;
    private static boolean sHWINFO = false;
    private static boolean sHWLog = false;
    private static boolean sHWModuleLog = false;

    static {
        mIsVLogCanPrint = false;
        mIsDLogCanPrint = false;
        mIsILogCanPrint = false;
        initHWLog();
        mIsVLogCanPrint = isNormalLogCanPrint(TAG, 2);
        mIsDLogCanPrint = isNormalLogCanPrint(TAG, 3);
        mIsILogCanPrint = isNormalLogCanPrint(TAG, 4);
    }

    private static boolean isNormalLogCanPrint(String tag, int level) {
        return sHWINFO || (sHWModuleLog && Log.isLoggable(tag, level));
    }

    private static void initHWLog() {
        getHWINFOProperty();
        getHWModuleLogProperty();
        getHWLogProperty();
    }

    private static void getHWLogProperty() {
        try {
            sHWLog = Log.class.getDeclaredField("HWLog").getBoolean(Log.class);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "getHWLogProperty NoSuchFieldException " + e.getMessage());
            sHWLog = false;
        } catch (IllegalAccessException e2) {
            Log.e(TAG, "getHWLogProperty IllegalAccessException " + e2.getMessage());
            sHWLog = false;
        }
    }

    private static void getHWModuleLogProperty() {
        try {
            sHWModuleLog = Log.class.getDeclaredField("HWModuleLog").getBoolean(Log.class);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "getHWModuleLogProperty NoSuchFieldException " + e.getMessage());
            sHWModuleLog = false;
        } catch (IllegalAccessException e2) {
            Log.e(TAG, "getHWModuleLogProperty IllegalAccessException " + e2.getMessage());
            sHWModuleLog = false;
        }
    }

    private static void getHWINFOProperty() {
        try {
            sHWINFO = Log.class.getDeclaredField("HWINFO").getBoolean(Log.class);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "getHWINFOProperty NoSuchFieldException " + e.getMessage());
            sHWINFO = false;
        } catch (IllegalAccessException e2) {
            Log.e(TAG, "getHWINFOProperty IllegalAccessException " + e2.getMessage());
            sHWINFO = false;
        }
    }

    private LogX() {
    }

    public static final void v(String className, String message) {
        if (mIsVLogCanPrint) {
            Log.v(TAG, className + ": " + message);
        }
    }

    public static final void d(String className, String message) {
        if (mIsDLogCanPrint) {
            Log.d(TAG, className + ": " + message);
        }
    }

    public static final void d(String className, String message, Throwable tr) {
        if (mIsDLogCanPrint) {
            Log.d(TAG, className + ": " + message, tr);
        }
    }

    public static final void i(String className, String message) {
        if (mIsILogCanPrint) {
            Log.i(TAG, className + ": " + message);
        }
    }

    public static final void i(String className, String message, Throwable tr) {
        if (mIsILogCanPrint) {
            Log.i(TAG, className + ": " + message, tr);
        }
    }

    public static final void w(String className, String message) {
        Log.w(TAG, className + ": " + message);
    }

    public static final void w(String className, Throwable tr) {
        Log.w(TAG, className + ": ", tr);
    }

    public static final void w(String className, String message, Throwable tr) {
        Log.w(TAG, className + ": " + message, tr);
    }

    public static final void e(String className, String message) {
        Log.e(TAG, className + ": " + message);
    }

    public static final void e(String className, String message, Throwable tr) {
        Log.e(TAG, className + ": " + message, tr);
    }

    public static final void wtf(String className, String message) {
        Log.wtf(TAG, className + ": " + message);
    }

    public static final void wtf(String className, Throwable tr) {
        Log.wtf(TAG, className + ": ", tr);
    }

    public static final void wtf(String className, String message, Throwable tr) {
        Log.wtf(TAG, className + ": " + message, tr);
    }
}
