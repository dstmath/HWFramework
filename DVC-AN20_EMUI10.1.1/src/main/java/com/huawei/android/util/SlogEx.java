package com.huawei.android.util;

import android.util.Slog;
import com.huawei.annotation.HwSystemApi;

public class SlogEx {
    private SlogEx() {
    }

    public static int wtfStack(String tag, String msg) {
        return Slog.wtfStack(tag, msg);
    }

    @HwSystemApi
    public static int v(String tag, String msg) {
        return Slog.v(tag, msg);
    }

    @HwSystemApi
    public static int d(String tag, String msg) {
        return Slog.d(tag, msg);
    }

    @HwSystemApi
    public static int i(String tag, String msg) {
        return Slog.i(tag, msg);
    }

    @HwSystemApi
    public static int e(String tag, String msg) {
        return Slog.e(tag, msg);
    }

    @HwSystemApi
    public static int w(String tag, String msg) {
        return Slog.w(tag, msg);
    }
}
