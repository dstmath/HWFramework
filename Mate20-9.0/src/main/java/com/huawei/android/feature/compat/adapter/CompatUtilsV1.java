package com.huawei.android.feature.compat.adapter;

import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CompatUtilsV1 {
    private static final String TAG = CompatUtilsV1.class.getSimpleName();

    public static boolean isDexOptNeeded(String str, String str2) {
        Class<?> cls;
        Method method;
        try {
            cls = Class.forName("dalvik.system.DexFile");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, e.getMessage());
            cls = null;
        }
        if (cls == null) {
            return false;
        }
        try {
            method = cls.getMethod("isDexOptNeeded", new Class[]{String.class});
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, e2.getMessage());
            method = null;
        }
        if (method == null) {
            return false;
        }
        try {
            return ((Boolean) Boolean.TYPE.cast(method.invoke(null, new Object[]{str}))).booleanValue();
        } catch (IllegalAccessException e3) {
            Log.e(TAG, e3.getMessage());
        } catch (InvocationTargetException e4) {
            Log.e(TAG, e4.getMessage());
        }
        return false;
    }
}
