package com.huawei.iconnect.hwutil;

import android.util.Log;
import java.lang.reflect.Field;

public class HwLog {
    private static final String TAG = "iConnect:Wearablejar";
    private static boolean sHwDetailLog;
    private static boolean sHwInfo;
    private static boolean sHwModuleDebug;

    public static void e(String tag, String msg) {
        Log.e(TAG, tag + ":" + msg);
    }

    public static void w(String tag, String msg) {
        Log.w(TAG, tag + ":" + msg);
    }

    public static void d(String tag, String msg) {
        if (sHwDetailLog) {
            Log.d(TAG, tag + ":" + msg);
        }
    }

    static {
        try {
            Class<Log> logClass = Log.class;
            Field field_HwModuleLog = logClass.getField("HWModuleLog");
            sHwInfo = logClass.getField("HWINFO").getBoolean(null);
            sHwModuleDebug = field_HwModuleLog.getBoolean(null);
            boolean isLoggable = !sHwInfo ? sHwModuleDebug ? Log.isLoggable(TAG, 4) : false : true;
            sHwInfo = isLoggable;
            sHwDetailLog = sHwInfo;
            d(TAG, "sHwDetailLog:" + sHwDetailLog + " HwModuleDebug:" + sHwModuleDebug);
        } catch (IllegalArgumentException e) {
            e(TAG, "error:getLogField--IllegalArgumentException" + e.getMessage());
        } catch (IllegalAccessException e2) {
            e(TAG, "error:getLogField--IllegalAccessException" + e2.getMessage());
        } catch (NoSuchFieldException e3) {
            e(TAG, "error:getLogField--NoSuchFieldException" + e3.getMessage());
        }
    }
}
