package com.huawei.android.feature.compat.adapter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class RelectUtils {
    public static void setFieldAccess(Field field) {
        if (field != null) {
            field.setAccessible(true);
        }
    }

    public static void setMethodAccess(Method method) {
        if (method != null) {
            method.setAccessible(true);
        }
    }
}
