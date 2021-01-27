package com.huawei.coauthservice.identitymgr.utils;

import android.util.Log;
import com.huawei.util.LogEx;

public class LogUtils {
    private static final String COLON = ":";
    private static final String TAG = "HwIdentityMgrSDK";
    private static volatile boolean isDebugOn;

    static {
        boolean z = false;
        isDebugOn = false;
        if (LogEx.getLogHWInfo() || (LogEx.getHWModuleLog() && Log.isLoggable(TAG, 3))) {
            z = true;
        }
        isDebugOn = z;
    }

    private LogUtils() {
    }

    public static void debug(String tag, String msg) {
        if (!ArgsValidationUtils.isEmpty(tag, msg) && isDebugOn) {
            Log.d(TAG, tag + COLON + msg);
        }
    }

    public static void error(String tag, String msg) {
        if (!ArgsValidationUtils.isEmpty(tag, msg)) {
            Log.e(TAG, tag + COLON + msg);
        }
    }

    public static void info(String tag, String msg) {
        if (!ArgsValidationUtils.isEmpty(tag, msg)) {
            Log.i(TAG, tag + COLON + msg);
        }
    }

    public static void warn(String tag, String msg) {
        if (!ArgsValidationUtils.isEmpty(tag, msg)) {
            Log.w(TAG, tag + COLON + msg);
        }
    }
}
