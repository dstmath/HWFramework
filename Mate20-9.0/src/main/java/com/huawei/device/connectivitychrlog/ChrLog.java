package com.huawei.device.connectivitychrlog;

import android.util.Log;

public class ChrLog {
    public static final boolean HWDBG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG_PREFIX, 3)));
    public static final boolean HWFLOW;
    private static final String TAG = TAG_PREFIX;
    private static String TAG_PREFIX = "CHR_";

    static {
        boolean z = true;
        if (!Log.HWINFO && (!Log.HWModuleLog || !Log.isLoggable(TAG_PREFIX, 4))) {
            z = false;
        }
        HWFLOW = z;
    }

    public static void chrLogD(String tag, String values) {
        if (HWDBG) {
            Log.d(TAG_PREFIX + tag, values);
        }
    }

    public static void chrLogE(String tag, String values) {
        Log.e(TAG_PREFIX + tag, values);
    }

    public static void chrLogI(String tag, String values) {
        if (HWFLOW) {
            Log.i(TAG_PREFIX + tag, values);
        }
    }

    public static void chrLogW(String tag, String values) {
        Log.w(TAG_PREFIX + tag, values);
    }
}
