package com.huawei.android.feature.compat.adapter;

import android.util.Log;
import com.huawei.android.feature.utils.ReflectUtils;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CompatUtils {
    private static final String TAG = CompatUtils.class.getSimpleName();

    public static boolean isDexOptNeeded(String str, String str2) {
        Method method;
        String str3;
        try {
            method = Class.forName("dalvik.system.DexPathList").getDeclaredMethod("optimizedPathFor", File.class, File.class);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, e.getMessage());
            method = null;
        } catch (ClassNotFoundException e2) {
            Log.e(TAG, e2.getMessage());
            method = null;
        }
        if (method == null) {
            return false;
        }
        ReflectUtils.setMethodAccess(method);
        try {
            str3 = (String) method.invoke(null, new File(str), new File(str2));
        } catch (IllegalAccessException e3) {
            Log.e(TAG, e3.getMessage());
            str3 = null;
        } catch (InvocationTargetException e4) {
            Log.e(TAG, e4.getMessage());
            str3 = null;
        }
        if (str3 == null) {
            return false;
        }
        return !new File(str3).exists();
    }
}
