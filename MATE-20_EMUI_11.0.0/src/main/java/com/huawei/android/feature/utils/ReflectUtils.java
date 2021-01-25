package com.huawei.android.feature.utils;

import android.util.Log;
import dalvik.system.BaseDexClassLoader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectUtils {
    private static final String TAG = ReflectUtils.class.getSimpleName();

    public static Field findField(Object obj, String str) {
        for (Class<?> cls = obj.getClass(); cls != null; cls = cls.getSuperclass()) {
            try {
                Field declaredField = cls.getDeclaredField(str);
                setFieldAccess(declaredField);
                return declaredField;
            } catch (NoSuchFieldException e) {
                Log.d(TAG, "findField " + str + " is not found ");
            }
        }
        throw new NoSuchFieldException("findField " + str + " is not found ");
    }

    public static Method findMethod(String str, String str2, Class<?>... clsArr) {
        for (Class<?> cls = Class.forName(str); cls != null; cls = cls.getSuperclass()) {
            try {
                Method declaredMethod = cls.getDeclaredMethod(str2, clsArr);
                setMethodAccess(declaredMethod);
                return declaredMethod;
            } catch (NoSuchMethodException e) {
                Log.d(TAG, "findMethod " + str2 + " is not found ");
            }
        }
        throw new NoSuchMethodException("findMethod " + str2 + " is not found ");
    }

    public static Method findMethodByClassLoader(ClassLoader classLoader, String str, Class<?>... clsArr) {
        for (Class<?> cls = getPathList(classLoader).getClass(); cls != null; cls = cls.getSuperclass()) {
            try {
                Method declaredMethod = cls.getDeclaredMethod(str, clsArr);
                setMethodAccess(declaredMethod);
                return declaredMethod;
            } catch (NoSuchMethodException e) {
                Log.d(TAG, "findMethod " + str + " is not found ");
            }
        }
        throw new NoSuchMethodException("findMethod " + str + " is not found ");
    }

    public static Object getPathList(ClassLoader classLoader) {
        if (classLoader == null) {
            return null;
        }
        Field declaredField = BaseDexClassLoader.class.getDeclaredField("pathList");
        setFieldAccess(declaredField);
        return declaredField.get((BaseDexClassLoader) classLoader);
    }

    public static void insertNewElements(Object obj, String str, Object[] objArr) {
        Field findField = findField(obj, str);
        Object[] objArr2 = (Object[]) findField.get(obj);
        Object[] objArr3 = (Object[]) Array.newInstance(objArr2.getClass().getComponentType(), objArr2.length + objArr.length);
        System.arraycopy(objArr2, 0, objArr3, 0, objArr2.length);
        System.arraycopy(objArr, 0, objArr3, objArr2.length, objArr.length);
        findField.set(obj, objArr3);
    }

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
