package com.huawei.android.telephony;

import android.telephony.Rlog;
import android.util.Log;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class RlogEx {
    private RlogEx() {
    }

    public static int v(String tag, String msg) {
        return Rlog.v(tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        return Rlog.v(tag, msg, tr);
    }

    public static int d(String tag, String msg) {
        return Rlog.d(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        return Rlog.d(tag, msg, tr);
    }

    public static int i(String tag, String msg) {
        return Rlog.i(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        return Rlog.i(tag, msg, tr);
    }

    public static int w(String tag, String msg) {
        return Rlog.w(tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return Rlog.w(tag, msg, tr);
    }

    public static int w(String tag, Throwable tr) {
        return Rlog.w(tag, tr);
    }

    public static int e(String tag, String msg) {
        return Rlog.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return Rlog.d(tag, msg, tr);
    }

    public static int println(int priority, String tag, String msg) {
        return Rlog.println(priority, tag, msg);
    }

    public static boolean isLoggable(String tag, int level) {
        return Log.isLoggable(tag, level);
    }

    public static String pii(String tag, Object pii) {
        return Rlog.pii(tag, pii);
    }

    public static String pii(boolean enablePiiLogging, Object pii) {
        return Rlog.pii(enablePiiLogging, pii);
    }
}
