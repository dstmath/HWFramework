package com.huawei.nb.coordinator.helper;

import com.huawei.nb.utils.logger.DSLog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PropertyUtils {
    private static volatile Method get = null;
    private static final Object lock = new Object();

    private static String getProperty(String prop, String defaultValue) {
        DSLog.d("PropertyUtils defaultValue: " + defaultValue, new Object[0]);
        try {
            if (get == null) {
                synchronized (lock) {
                    if (get == null) {
                        get = Class.forName("android.os.SystemProperties").getDeclaredMethod("get", new Class[]{String.class, String.class});
                    }
                }
            }
            return (String) get.invoke(null, new Object[]{prop, defaultValue});
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            DSLog.e("PropertyUtils failed: " + e.getMessage(), new Object[0]);
            return null;
        }
    }

    public static boolean isSupportHwPKI() {
        return "true".equalsIgnoreCase(getProperty("ro.config.support_hwpki", "false"));
    }
}
