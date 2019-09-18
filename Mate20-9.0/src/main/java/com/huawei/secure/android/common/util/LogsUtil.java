package com.huawei.secure.android.common.util;

import android.text.TextUtils;
import android.util.Log;
import java.util.regex.Pattern;

public class LogsUtil {
    private static final int LEN_CONST = 2;
    private static final Pattern M_PATTERN = Pattern.compile("[0-9]*[a-z|A-Z]*[一-龥]*");
    private static final char STAR = '*';

    private static String getLogMsg(String msg, boolean isNeedProguard) {
        StringBuilder retStr = new StringBuilder(512);
        if (!TextUtils.isEmpty(msg)) {
            if (isNeedProguard) {
                retStr.append(formatLogWithStar(msg));
            } else {
                retStr.append(msg);
            }
        }
        return retStr.toString();
    }

    private static String getLogMsg(String noProguardMsg, String msg) {
        StringBuilder retStr = new StringBuilder(512);
        if (!TextUtils.isEmpty(noProguardMsg)) {
            retStr.append(noProguardMsg);
        }
        if (!TextUtils.isEmpty(msg)) {
            retStr.append(formatLogWithStar(msg));
        }
        return retStr.toString();
    }

    public static void d(String tag, String msg, boolean isNeedProguard) {
        if (!TextUtils.isEmpty(msg)) {
            Log.d(tag, getLogMsg(msg, isNeedProguard));
        }
    }

    public static void d(String tag, String noProguardMsg, String msg) {
        if (!TextUtils.isEmpty(noProguardMsg) || !TextUtils.isEmpty(msg)) {
            Log.d(tag, getLogMsg(noProguardMsg, msg));
        }
    }

    public static void d(String tag, String noProguardMsg, String msg, Throwable e) {
        if (!TextUtils.isEmpty(noProguardMsg) || !TextUtils.isEmpty(msg)) {
            Log.d(tag, getLogMsg(noProguardMsg, msg), e);
        }
    }

    public static void d(String tag, String msg) {
        if (!TextUtils.isEmpty(msg)) {
            Log.d(tag, getLogMsg(msg, false));
        }
    }

    public static void d(String tag, String msg, Throwable e, boolean isNeedProguard) {
        if (!TextUtils.isEmpty(msg)) {
            Log.d(tag, getLogMsg(msg, isNeedProguard), e);
        }
    }

    public static void d(String tag, String msg, Throwable e) {
        if (!TextUtils.isEmpty(msg) || e != null) {
            Log.d(tag, getLogMsg(msg, false), e);
        }
    }

    public static void i(String tag, String msg, boolean isNeedProguard) {
        if (!TextUtils.isEmpty(msg)) {
            Log.i(tag, getLogMsg(msg, isNeedProguard));
        }
    }

    public static void i(String tag, String noProguardMsg, String msg) {
        if (!TextUtils.isEmpty(noProguardMsg) || !TextUtils.isEmpty(msg)) {
            Log.i(tag, getLogMsg(noProguardMsg, msg));
        }
    }

    public static void i(String tag, String noProguardMsg, String msg, Throwable e) {
        if (!TextUtils.isEmpty(noProguardMsg) || !TextUtils.isEmpty(msg)) {
            Log.i(tag, getLogMsg(noProguardMsg, msg), e);
        }
    }

    public static void i(String tag, String msg) {
        if (!TextUtils.isEmpty(msg)) {
            Log.i(tag, getLogMsg(msg, false));
        }
    }

    public static void i(String tag, String msg, Throwable e, boolean isNeedProguard) {
        if (!TextUtils.isEmpty(msg) || e != null) {
            Log.i(tag, getLogMsg(msg, isNeedProguard), e);
        }
    }

    public static void i(String tag, String msg, Throwable e) {
        if (!TextUtils.isEmpty(msg) || e != null) {
            Log.i(tag, getLogMsg(msg, false), e);
        }
    }

    public static void w(String tag, String msg, boolean isNeedProguard) {
        if (!TextUtils.isEmpty(msg)) {
            Log.w(tag, getLogMsg(msg, isNeedProguard));
        }
    }

    public static void w(String tag, String noProguardMsg, String msg) {
        if (!TextUtils.isEmpty(noProguardMsg) || !TextUtils.isEmpty(msg)) {
            Log.w(tag, getLogMsg(noProguardMsg, msg));
        }
    }

    public static void w(String tag, String noProguardMsg, String msg, Throwable e) {
        if (!TextUtils.isEmpty(noProguardMsg) || !TextUtils.isEmpty(msg)) {
            Log.w(tag, getLogMsg(noProguardMsg, msg), e);
        }
    }

    public static void w(String tag, String msg) {
        if (!TextUtils.isEmpty(msg)) {
            Log.w(tag, getLogMsg(msg, false));
        }
    }

    public static void w(String tag, String msg, Throwable e, boolean isNeedProguard) {
        if (!TextUtils.isEmpty(msg) || e != null) {
            Log.w(tag, getLogMsg(msg, isNeedProguard), e);
        }
    }

    public static void w(String tag, String msg, Throwable e) {
        if (!TextUtils.isEmpty(msg) || e != null) {
            Log.w(tag, getLogMsg(msg, false), e);
        }
    }

    public static void e(String tag, String msg, boolean isNeedProguard) {
        if (!TextUtils.isEmpty(msg)) {
            Log.e(tag, getLogMsg(msg, isNeedProguard));
        }
    }

    public static void e(String tag, String noProguardMsg, String msg) {
        if (!TextUtils.isEmpty(noProguardMsg) || !TextUtils.isEmpty(msg)) {
            Log.e(tag, getLogMsg(noProguardMsg, msg));
        }
    }

    public static void e(String tag, String noProguardMsg, String msg, Throwable e) {
        if (!TextUtils.isEmpty(noProguardMsg) || !TextUtils.isEmpty(msg)) {
            Log.e(tag, getLogMsg(noProguardMsg, msg), e);
        }
    }

    public static void e(String tag, String msg) {
        if (!TextUtils.isEmpty(msg)) {
            Log.e(tag, getLogMsg(msg, false));
        }
    }

    public static void e(String tag, String msg, Throwable e, boolean isNeedProguard) {
        if (!TextUtils.isEmpty(msg) || e != null) {
            Log.e(tag, getLogMsg(msg, isNeedProguard), e);
        }
    }

    public static void e(String tag, String msg, Throwable e) {
        if (!TextUtils.isEmpty(msg) || e != null) {
            Log.e(tag, getLogMsg(msg, false), e);
        }
    }

    private static String formatLogWithStar(String logStr) {
        if (TextUtils.isEmpty(logStr)) {
            return logStr;
        }
        int len = logStr.length();
        int k = 1;
        if (1 == len) {
            return String.valueOf(STAR);
        }
        StringBuilder retStr = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char charAt = logStr.charAt(i);
            if (M_PATTERN.matcher(String.valueOf(charAt)).matches()) {
                if (k % 2 == 0) {
                    charAt = STAR;
                }
                k++;
            }
            retStr.append(charAt);
        }
        return retStr.toString();
    }
}
