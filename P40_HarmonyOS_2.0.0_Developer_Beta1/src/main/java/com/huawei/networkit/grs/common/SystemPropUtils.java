package com.huawei.networkit.grs.common;

import android.text.TextUtils;

public class SystemPropUtils {
    private static final String TAG = SystemPropUtils.class.getSimpleName();

    public static String getProperty(String method, String key, String className, String defaultVal) {
        if (TextUtils.isEmpty(method) || TextUtils.isEmpty(key) || TextUtils.isEmpty(className)) {
            Logger.w(TAG, "reflect class for method has exception.");
            return defaultVal;
        }
        try {
            Class<?> c = Class.forName(className);
            return (String) c.getMethod(method, String.class, String.class).invoke(c, key, defaultVal);
        } catch (Exception e) {
            Logger.e(TAG, "getProperty catch exception: ", e);
            return defaultVal;
        }
    }
}
