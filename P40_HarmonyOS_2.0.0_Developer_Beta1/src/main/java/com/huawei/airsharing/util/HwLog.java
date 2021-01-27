package com.huawei.airsharing.util;

import android.os.SystemProperties;
import android.util.Log;

public class HwLog {
    private static final int DOMESTIC_BETA = 3;
    private static final int EXTRA_BUFFER_DATA = 50;
    private static final boolean IS_DEBUG_HISIGH_PERFORMANCE = SystemProperties.getBoolean("debug.hisight.performance", (boolean) IS_DEBUG_HISIGH_PERFORMANCE);
    private static final int MAX_MESSAGE_LENGTH = 1000;
    private static final int OVERSEA_BETA = 5;
    private static final int STACK_DEPTH = 4;
    private static final String TAG = "IICLOG";
    private static final int USERTYPE = SystemProperties.getInt("ro.logsystem.usertype", 0);
    private static HwLog mStInstance;
    private boolean mBDebugSwitch = IS_DEBUG_HISIGH_PERFORMANCE;
    private boolean mBDetailSwitch = IS_DEBUG_HISIGH_PERFORMANCE;
    private boolean mBErrorSwitch = IS_DEBUG_HISIGH_PERFORMANCE;
    private boolean mBInfoSwitch = IS_DEBUG_HISIGH_PERFORMANCE;
    private boolean mBVerboseSwitch = IS_DEBUG_HISIGH_PERFORMANCE;
    private boolean mBWarningSwitch = IS_DEBUG_HISIGH_PERFORMANCE;

    public HwLog() {
        if (isBetaUser()) {
            this.mBVerboseSwitch = IS_DEBUG_HISIGH_PERFORMANCE;
            this.mBDebugSwitch = true;
            this.mBInfoSwitch = IS_DEBUG_HISIGH_PERFORMANCE;
            this.mBWarningSwitch = true;
            this.mBErrorSwitch = true;
            this.mBDetailSwitch = true;
        }
    }

    public static synchronized HwLog getInstance() {
        HwLog hwLog;
        synchronized (HwLog.class) {
            if (mStInstance == null) {
                mStInstance = new HwLog();
            }
            hwLog = mStInstance;
        }
        return hwLog;
    }

    private String buildMsg(String msg) {
        String tmp = msg;
        if (msg != null && msg.length() > 1000) {
            tmp = msg.substring(0, 1000);
            Log.w(TAG, "msg length bigger than 1000");
        }
        int length = EXTRA_BUFFER_DATA;
        if (tmp != null) {
            length = EXTRA_BUFFER_DATA + tmp.length();
        }
        StringBuilder buffer = new StringBuilder(length);
        if (this.mBDetailSwitch) {
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            if (stackTraceElements.length > 4) {
                buffer.append("[ ");
                buffer.append(stackTraceElements[4].getFileName());
                buffer.append(": ");
                buffer.append(stackTraceElements[4].getLineNumber());
                buffer.append("]");
            }
        }
        buffer.append(tmp);
        return buffer.toString();
    }

    public boolean setLogLevel(boolean verboseSwitch, boolean debugSwitch, boolean infoSwitch, boolean warningSwitch, boolean errorSwitch) {
        this.mBVerboseSwitch = verboseSwitch;
        this.mBDebugSwitch = debugSwitch;
        this.mBInfoSwitch = infoSwitch;
        this.mBWarningSwitch = warningSwitch;
        this.mBErrorSwitch = errorSwitch;
        return true;
    }

    public void v(String strTag, String strLog) {
        if (this.mBVerboseSwitch) {
            Log.v(strTag, buildMsg(strLog));
        }
    }

    public void d(String strTag, String strLog) {
        if (this.mBDebugSwitch) {
            Log.d(strTag, buildMsg(strLog));
        }
    }

    public void dp(String strTag, String strLog) {
        if (IS_DEBUG_HISIGH_PERFORMANCE) {
            Log.d(strTag, buildMsg(strLog));
        }
    }

    public void i(String strTag, String strLog) {
        if (this.mBInfoSwitch) {
            Log.i(strTag, buildMsg(strLog));
        }
    }

    public void w(String strTag, String strLog) {
        if (this.mBWarningSwitch) {
            Log.w(strTag, buildMsg(strLog));
        }
    }

    public void e(String strTag, String strLog) {
        if (this.mBErrorSwitch) {
            Log.e(strTag, buildMsg(strLog));
        }
    }

    private static boolean isBetaUser() {
        int i = USERTYPE;
        if (i == 3 || i == 5) {
            return true;
        }
        return IS_DEBUG_HISIGH_PERFORMANCE;
    }
}
