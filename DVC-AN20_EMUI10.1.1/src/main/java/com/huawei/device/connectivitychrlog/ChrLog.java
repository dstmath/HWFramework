package com.huawei.device.connectivitychrlog;

import android.util.Log;
import android.util.wifi.HwHiLog;

public class ChrLog {
    public static final boolean CHRLOG_FALSE = false;
    public static final boolean HWDBG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG_PREFIX, 3)));
    public static final boolean HWFLOW;
    private static final String TAG = TAG_PREFIX;
    private static String TAG_PREFIX = "CHR_";

    static {
        boolean z = false;
        if (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG_PREFIX, 4))) {
            z = true;
        }
        HWFLOW = z;
    }

    public static void chrLogD(String tag, boolean isFmtStrPrivate, String values, Object... args) {
        if (HWDBG) {
            HwHiLog.d(TAG_PREFIX + tag, isFmtStrPrivate, values, args);
        }
    }

    public static void chrLogE(String tag, boolean isFmtStrPrivate, String values, Object... args) {
        HwHiLog.e(TAG_PREFIX + tag, isFmtStrPrivate, values, args);
    }

    public static void chrLogI(String tag, boolean isFmtStrPrivate, String values, Object... args) {
        if (HWFLOW) {
            HwHiLog.i(TAG_PREFIX + tag, isFmtStrPrivate, values, args);
        }
    }

    public static void chrLogW(String tag, boolean isFmtStrPrivate, String values, Object... args) {
        HwHiLog.w(TAG_PREFIX + tag, isFmtStrPrivate, values, args);
    }
}
