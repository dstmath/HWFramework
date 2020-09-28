package com.huawei.dmsdpsdk2;

import android.util.Log;
import java.lang.reflect.Field;

public class HwLog {
    private static final String TAG = "DMSDPSDK";
    private static boolean isHwDetailLog;
    private static boolean isHwInfo;
    private static boolean isHwModuleDebug;

    public static void e(String tag, String msg) {
        Log.e(TAG, tag + ":" + msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        Log.e(TAG, tag + ":" + msg, tr);
    }

    public static void w(String tag, String msg) {
        Log.w(TAG, tag + ":" + msg);
    }

    public static void w(String tag, String msg, Throwable tr) {
        Log.w(TAG, tag + ":" + msg, tr);
    }

    public static void i(String tag, String msg) {
        Log.i(TAG, tag + ":" + msg);
    }

    public static void i(String tag, String msg, Throwable tr) {
        Log.i(TAG, tag + ":" + msg, tr);
    }

    public static void d(String tag, String msg) {
        if (isHwDetailLog) {
            Log.d(TAG, tag + ":" + msg);
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (isHwDetailLog) {
            Log.d(TAG, tag + ":" + msg, tr);
        }
    }

    static {
        boolean z;
        try {
            Field fieldHwModuleLog = Log.class.getField("HWModuleLog");
            isHwInfo = Log.class.getField("HWINFO").getBoolean(null);
            isHwModuleDebug = fieldHwModuleLog.getBoolean(null);
            if (!isHwInfo) {
                if (!isHwModuleDebug || !Log.isLoggable(TAG, 4)) {
                    z = false;
                    isHwInfo = z;
                    isHwDetailLog = isHwInfo;
                    e(TAG, "isHwDetailLog:" + isHwDetailLog + " HwModuleDebug:" + isHwModuleDebug);
                }
            }
            z = true;
            isHwInfo = z;
            isHwDetailLog = isHwInfo;
            e(TAG, "isHwDetailLog:" + isHwDetailLog + " HwModuleDebug:" + isHwModuleDebug);
        } catch (IllegalArgumentException e) {
            e(TAG, "error:getLogField--IllegalArgumentException" + e.getMessage());
        } catch (IllegalAccessException e2) {
            e(TAG, "error:getLogField--IllegalAccessException" + e2.getMessage());
        } catch (NoSuchFieldException e3) {
            e(TAG, "error:getLogField--NoSuchFieldException" + e3.getMessage());
        }
    }
}
