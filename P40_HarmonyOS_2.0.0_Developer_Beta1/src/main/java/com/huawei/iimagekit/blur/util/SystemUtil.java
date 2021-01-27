package com.huawei.iimagekit.blur.util;

import android.os.SystemProperties;

public class SystemUtil {
    public static int getSystemProperty(String key, int defaultValue) {
        return SystemProperties.getInt(key, defaultValue);
    }

    public static boolean getSystemProperty(String key, boolean defaultValue) {
        return SystemProperties.getBoolean(key, defaultValue);
    }
}
