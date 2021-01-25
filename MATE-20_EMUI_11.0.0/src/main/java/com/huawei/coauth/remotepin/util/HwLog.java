package com.huawei.coauth.remotepin.util;

import android.util.Log;
import java.lang.reflect.Field;

public class HwLog {
    private static final String TAG = "RemotePin-Manager";
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
            Field fieldHwLog = Log.class.getField("HWLog");
            Field fieldHwModuleLog = Log.class.getField("HWModuleLog");
            Field fieldHwInfoLog = Log.class.getField("HWINFO");
            sHwDebug = fieldHwLog.getBoolean(null);
            sHwInfo = fieldHwInfoLog.getBoolean(null);
            sHwModuleDebug = fieldHwModuleLog.getBoolean(null);
            if (!sHwDebug) {
                if (!sHwModuleDebug || !Log.isLoggable(TAG, 3)) {
                    z = false;
                    sHwDebug = z;
                    if (sHwInfo || (sHwModuleDebug && Log.isLoggable(TAG, 4))) {
                        z2 = true;
                    }
                    sHwInfo = z2;
                }
            }
            z = true;
            sHwDebug = z;
            z2 = true;
            sHwInfo = z2;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "error:getLogField--IllegalArgumentException");
        } catch (IllegalAccessException e2) {
            Log.e(TAG, "error:getLogField--IllegalAccessException");
        } catch (NoSuchFieldException e3) {
            Log.e(TAG, "error:getLogField--NoSuchFieldException");
        }
    }

    public static void v(String tag, String msg) {
        if (sHwDebug) {
            Log.v(TAG, tag + ":" + msg);
        }
    }

    public static void d(String tag, String msg) {
        if (sHwDebug) {
            Log.d(TAG, tag + ":" + msg);
        }
    }

    public static void i(String tag, String msg) {
        if (sHwInfo) {
            Log.i(TAG, tag + ":" + msg);
        }
    }

    public static void w(String tag, String msg) {
        Log.w(TAG, tag + ":" + msg);
    }

    public static void e(String tag, String msg) {
        Log.e(TAG, tag + ":" + msg);
    }
}
