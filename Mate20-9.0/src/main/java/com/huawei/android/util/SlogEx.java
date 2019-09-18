package com.huawei.android.util;

import android.util.Slog;

public class SlogEx {
    private SlogEx() {
    }

    public static int wtfStack(String tag, String msg) {
        return Slog.wtfStack(tag, msg);
    }
}
