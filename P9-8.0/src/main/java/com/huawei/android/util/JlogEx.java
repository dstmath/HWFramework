package com.huawei.android.util;

import android.util.Jlog;

public class JlogEx {
    public static int d(int tag, String msg) {
        return Jlog.d(tag, msg);
    }

    public static int d(int tag, String arg1, String msg) {
        return Jlog.d(tag, arg1, msg);
    }

    public static int d(int tag, int arg2, String msg) {
        return Jlog.d(tag, arg2, msg);
    }
}
