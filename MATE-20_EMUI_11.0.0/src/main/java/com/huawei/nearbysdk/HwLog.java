package com.huawei.nearbysdk;

import android.util.Log;
import java.lang.reflect.Field;

public class HwLog {
    private static final int BYTE_MASK = 255;
    private static final int BYTE_TO_HEX_HIGHER_OFFSET = 4;
    private static final int BYTE_TO_HEX_LOWER_MASK = 15;
    private static final String TAG = "nearby";
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static boolean sHwDetailLog;
    private static boolean sHwInfo;
    private static boolean sHwModuleDebug;

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
        if (!sHwDetailLog) {
            return;
        }
        if (sHwInfo) {
            Log.i(TAG, tag + ":" + msg);
            return;
        }
        Log.d(TAG, tag + ":" + msg);
    }

    public static void s(String tag, String msg, Throwable tr) {
        if (sHwDetailLog) {
            Log.d(TAG, tag + ":" + msg, tr);
        }
    }

    static {
        boolean z;
        try {
            Field field_HwModuleLog = Log.class.getField("HWModuleLog");
            sHwInfo = Log.class.getField("HWINFO").getBoolean(null);
            sHwModuleDebug = field_HwModuleLog.getBoolean(null);
            if (!sHwInfo) {
                if (!sHwModuleDebug || !Log.isLoggable(TAG, 4)) {
                    z = false;
                    sHwDetailLog = z;
                    e(TAG, "sHwDetailLog:" + sHwDetailLog + " HwModuleDebug:" + sHwModuleDebug);
                }
            }
            z = true;
            sHwDetailLog = z;
            e(TAG, "sHwDetailLog:" + sHwDetailLog + " HwModuleDebug:" + sHwModuleDebug);
        } catch (IllegalArgumentException e) {
            e(TAG, "error:getLogField--IllegalArgumentException" + e.getMessage());
        } catch (IllegalAccessException e2) {
            e(TAG, "error:getLogField--IllegalAccessException" + e2.getMessage());
        } catch (NoSuchFieldException e3) {
            e(TAG, "error:getLogField--NoSuchFieldException" + e3.getMessage());
        }
    }

    public static void logByteArray(String tag, byte[] data) {
        logByteArray(tag, null, data);
    }

    public static void logByteArray(String tag, String what, byte[] data) {
        if (sHwDetailLog && data != null) {
            StringBuilder dataStr = new StringBuilder();
            if (what != null) {
                dataStr.append(what);
            }
            dataStr.append(" len: ");
            dataStr.append(data.length);
            dataStr.append(" ## ");
            for (byte aData : data) {
                int v = aData & 255;
                dataStr.append(" 0x");
                dataStr.append(hexArray[v >>> 4]);
                dataStr.append(hexArray[v & 15]);
            }
            d(tag, dataStr.toString());
        }
    }
}
