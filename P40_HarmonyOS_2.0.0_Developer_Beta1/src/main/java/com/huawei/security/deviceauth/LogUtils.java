package com.huawei.security.deviceauth;

import android.util.Log;
import com.huawei.util.LogEx;

public class LogUtils {
    private static final String TAG = "HwDeviceAuth";
    private static boolean sHwDebug;
    private static boolean sHwInfo;
    private static boolean sHwModuleDebug;

    static {
        boolean z;
        boolean z2 = false;
        sHwDebug = false;
        sHwInfo = true;
        sHwModuleDebug = false;
        try {
            sHwDebug = LogEx.getLogHWInfo();
            sHwInfo = LogEx.getLogHWInfo();
            sHwModuleDebug = LogEx.getHWModuleLog();
            if (!sHwDebug) {
                if (!sHwModuleDebug || !Log.isLoggable(TAG, 3)) {
                    z = false;
                    sHwDebug = z;
                    if (sHwInfo || (sHwModuleDebug && Log.isLoggable(TAG, 4))) {
                        z2 = true;
                    }
                    sHwInfo = z2;
                    Log.i(TAG, "isDebugOn:" + sHwDebug + ", isInfoOn:" + sHwInfo);
                }
            }
            z = true;
            sHwDebug = z;
            z2 = true;
            sHwInfo = z2;
            Log.i(TAG, "isDebugOn:" + sHwDebug + ", isInfoOn:" + sHwInfo);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "error:getLogField--IllegalArgumentException");
        }
    }

    private LogUtils() {
    }

    public static boolean isDebug() {
        return sHwDebug;
    }

    public static boolean isInfom() {
        return sHwInfo;
    }

    public static void v(String tag, String msg) {
        if (tag != null && msg != null && sHwDebug) {
            Log.v(TAG, tag + ":" + msg);
        }
    }

    public static void d(String tag, String msg) {
        if (tag != null && msg != null && sHwDebug) {
            Log.d(TAG, tag + ":" + msg);
        }
    }

    public static void i(String tag, String msg) {
        if (tag != null && msg != null && sHwInfo) {
            Log.i(TAG, tag + ":" + msg);
        }
    }

    public static void w(String tag, String msg) {
        if (tag != null && msg != null) {
            Log.w(TAG, tag + ":" + msg);
        }
    }

    public static void e(String tag, String msg) {
        if (tag != null && msg != null) {
            Log.e(TAG, tag + ":" + msg);
        }
    }
}
