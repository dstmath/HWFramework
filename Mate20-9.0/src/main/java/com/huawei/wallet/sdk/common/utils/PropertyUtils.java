package com.huawei.wallet.sdk.common.utils;

import com.huawei.wallet.sdk.common.log.LogC;
import java.lang.reflect.Method;

public class PropertyUtils {
    private static final byte[] SYNC_LOCK = new byte[0];
    private static volatile Method get = null;
    private static volatile Method set = null;

    public static String getProperty(String prop, String defaultValue) {
        String value = "";
        try {
            if (get == null) {
                synchronized (SYNC_LOCK) {
                    if (get == null) {
                        get = Class.forName("android.os.SystemProperties").getDeclaredMethod("get", new Class[]{String.class, String.class});
                    }
                }
            }
            value = (String) get.invoke(null, new Object[]{prop, defaultValue});
        } catch (RuntimeException e) {
            LogC.d("PropertyUtils RuntimeException", true);
        } catch (Exception e2) {
            LogC.d("PropertyUtils Exception ", true);
        }
        LogC.d("PropertyUtils getProperty: " + value, true);
        return value;
    }

    public static boolean setProperty(String key, String value) {
        try {
            if (set == null) {
                synchronized (SYNC_LOCK) {
                    if (set == null) {
                        set = Class.forName("android.os.SystemProperties").getDeclaredMethod("set", new Class[]{String.class, String.class});
                    }
                }
            }
            set.invoke(null, new Object[]{key, value});
            return true;
        } catch (RuntimeException e) {
            LogC.d("PropertyUtils RuntimeException", true);
            return false;
        } catch (Exception e2) {
            LogC.d("PropertyUtils Exception ", true);
            return false;
        }
    }
}
