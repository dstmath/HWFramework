package com.huawei.iimagekit.blur.util;

import com.huawei.uikit.effect.BuildConfig;
import java.lang.reflect.InvocationTargetException;

public class SystemUtil {
    private static final int DECIMAL = 10;

    public static int getSystemProperty(String key, int defaultValue) {
        String val = getSystemProperty(key);
        if (val.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val, 10);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
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
            return (String) Class.forName("android.os.SystemProperties").getMethod("get", String.class).invoke(null, key);
        } catch (ClassNotFoundException e) {
            return BuildConfig.FLAVOR;
        } catch (NoSuchMethodException e2) {
            return BuildConfig.FLAVOR;
        } catch (IllegalAccessException e3) {
            return BuildConfig.FLAVOR;
        } catch (InvocationTargetException e4) {
            return BuildConfig.FLAVOR;
        }
    }

    public static String getSystemProperty(String key, String defaultValue) {
        String value = getSystemProperty(key);
        return value.isEmpty() ? defaultValue : value;
    }
}
