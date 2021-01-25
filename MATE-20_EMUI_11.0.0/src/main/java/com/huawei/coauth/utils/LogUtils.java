package com.huawei.coauth.utils;

import android.util.Log;

public class LogUtils {
    public static void info(String tag, String content) {
        Log.i(tag, content);
    }

    public static void error(String tag, String content) {
        Log.e(tag, content);
    }
}
