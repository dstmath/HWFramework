package com.huawei.iimagekit.blur.util;

import android.util.Log;

public class SystemUtil {
    private static final String TAG = "SystemUtil";

    public static int getSystemProperty(String key, int defaultValue) {
        String val = getSystemProperty(key);
        if (val.isEmpty()) {
            return defaultValue;
        }
        return Integer.parseInt(val, 10);
    }

    public static boolean getSystemProperty(String key, boolean defaultValue) {
        String val = getSystemProperty(key);
        if (val.isEmpty()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(val);
    }

    public static String getSystemProperty(String key) {
        try {
            return (String) Class.forName("android.os.SystemProperties").getMethod("get", new Class[]{String.class}).invoke(null, new Object[]{key});
        } catch (Exception e) {
            Log.e(TAG, "String getSystemProperty with parameter String key Error", e);
            return "";
        }
    }

    public static String getSystemProperty(String key, String defaultValue) {
        String value = getSystemProperty(key);
        if (value.isEmpty()) {
            return defaultValue;
        }
        return value;
    }
}
