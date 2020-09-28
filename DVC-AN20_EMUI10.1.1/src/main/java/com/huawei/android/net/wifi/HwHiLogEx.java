package com.huawei.android.net.wifi;

import android.util.HiLog;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class HwHiLogEx {
    public static final int DOMAIN = 218104322;

    private HwHiLogEx() {
    }

    public static void d(String tag, boolean isFmtStrPrivate, String format, Object... args) {
        HiLog.w((int) DOMAIN, tag, isFmtStrPrivate, format, args);
    }

    public static void i(String tag, boolean isFmtStrPrivate, String format, Object... args) {
        HiLog.i((int) DOMAIN, tag, isFmtStrPrivate, format, args);
    }

    public static void w(String tag, boolean isFmtStrPrivate, String format, Object... args) {
        HiLog.w((int) DOMAIN, tag, isFmtStrPrivate, format, args);
    }

    public static void e(String tag, boolean isFmtStrPrivate, String format, Object... args) {
        HiLog.e((int) DOMAIN, tag, isFmtStrPrivate, format, args);
    }

    public static void v(String tag, boolean isFmtStrPrivate, String format, Object... args) {
        HiLog.w((int) DOMAIN, tag, isFmtStrPrivate, format, args);
    }
}
