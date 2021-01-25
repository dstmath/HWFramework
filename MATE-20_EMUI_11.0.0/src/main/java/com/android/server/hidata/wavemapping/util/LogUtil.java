package com.android.server.hidata.wavemapping.util;

import android.util.Log;
import android.util.wifi.HwHiLog;
import com.android.server.hidata.wavemapping.cons.Constant;

public class LogUtil {
    private static final String TAG = "WMapping.LogUtil";
    private static final String TAG_PREFIX = "WMapping";
    private static boolean debugFlag = false;
    private static boolean showD = true;
    private static boolean showE = true;
    private static boolean showI = false;
    private static boolean showV = false;
    private static boolean showW = true;
    private static boolean showWTF = true;

    private LogUtil() {
    }

    public static void v(boolean isPrivateFmtStr, String msg, Object... args) {
        if (showV) {
            HwHiLog.v(TAG_PREFIX, isPrivateFmtStr, msg, args);
        }
    }

    public static void v(String msg, Throwable tr) {
        if (showV) {
            Log.v(TAG_PREFIX, msg, tr);
        }
    }

    public static void d(boolean isPrivateFmtStr, String msg, Object... args) {
        if (showD) {
            HwHiLog.d(TAG_PREFIX, isPrivateFmtStr, msg, args);
        }
    }

    public static void d(String msg, Throwable tr) {
        if (showD) {
            Log.d(TAG_PREFIX, msg, tr);
        }
    }

    public static void i(boolean isPrivateFmtStr, String msg, Object... args) {
        if (showI) {
            HwHiLog.i(TAG_PREFIX, isPrivateFmtStr, msg, args);
        }
    }

    public static void i(String msg, Throwable tr) {
        if (showI) {
            Log.i(TAG_PREFIX, msg, tr);
        }
    }

    public static void w(boolean isPrivateFmtStr, String msg, Object... args) {
        if (showW) {
            HwHiLog.w(TAG_PREFIX, isPrivateFmtStr, msg, args);
        }
    }

    public static void w(String msg, Throwable tr) {
        if (showW) {
            Log.w(TAG_PREFIX, msg, tr);
        }
    }

    public static void e(boolean isPrivateFmtStr, String msg, Object... args) {
        if (showE) {
            HwHiLog.e(TAG_PREFIX, isPrivateFmtStr, msg, args);
        }
    }

    public static void e(String msg, Throwable tr) {
        if (showE) {
            Log.e(TAG_PREFIX, msg, tr);
        }
    }

    public static void wtf(String msg) {
        if (showWTF) {
            Log.wtf(TAG_PREFIX, msg);
        }
    }

    public static void wtf(String msg, Throwable tr) {
        if (showWTF) {
            Log.wtf(TAG_PREFIX, msg, tr);
        }
    }

    public static void wtLogFile(String msg) {
        if (debugFlag) {
            String logFilePath = Constant.getLogFilePath();
            FileUtils.writeFile(logFilePath, TAG_PREFIX + msg);
            Log.i(TAG_PREFIX, msg);
        }
    }

    public static boolean isShowV() {
        return showV;
    }

    public static void setShowV(boolean showV2) {
        showV = showV2;
    }

    public static boolean isShowD() {
        return showD;
    }

    public static void setShowD(boolean showD2) {
        showD = showD2;
    }

    public static boolean isShowI() {
        return showI;
    }

    public static void setShowI(boolean showI2) {
        showI = showI2;
    }

    public static boolean isShowW() {
        return showW;
    }

    public static void setShowW(boolean showW2) {
        showW = showW2;
    }

    public static boolean isShowE() {
        return showE;
    }

    public static void setShowE(boolean showE2) {
        showE = showE2;
    }

    public static boolean getDebugFlag() {
        return debugFlag;
    }

    public static void setDebugFlag(boolean flag) {
        debugFlag = flag;
    }
}
