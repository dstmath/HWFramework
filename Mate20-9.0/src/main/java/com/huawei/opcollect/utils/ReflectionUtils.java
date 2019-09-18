package com.huawei.opcollect.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class ReflectionUtils {
    private static final String TAG = "ReflectionUtils";

    private ReflectionUtils() {
        OPCollectLog.e(TAG, "static class should not initialize.");
    }

    public static Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            OPCollectLog.e(TAG, "ReflectionUtils : className not found:" + className);
            return null;
        }
    }

    public static Method getMethod(String className, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        Class<?> targetClass = getClass(className);
        if (targetClass == null || methodName == null) {
            return method;
        }
        try {
            return targetClass.getMethod(methodName, parameterTypes);
        } catch (SecurityException e) {
            OPCollectLog.e(TAG, "ReflectionUtils : " + e.getMessage());
            return method;
        } catch (NoSuchMethodException e2) {
            OPCollectLog.e(TAG, "ReflectionUtils : " + methodName + ", not such method.");
            return method;
        }
    }

    public static Object invoke(Method method, Object receiver, Object... args) {
        if (method != null) {
            try {
                return method.invoke(receiver, args);
            } catch (IllegalAccessException | RuntimeException | InvocationTargetException e) {
                OPCollectLog.e(TAG, "failed to invoke, method is " + method.getName() + ", cause: " + e.getMessage());
            }
        }
        return null;
    }

    public static Object invoke(Method method, Object receiver) {
        if (method != null) {
            try {
                return method.invoke(receiver, new Object[0]);
            } catch (IllegalAccessException | RuntimeException | InvocationTargetException e) {
                OPCollectLog.e(TAG, "failed to invoke, method is " + method.getName() + ", cause: " + e.getMessage());
            }
        }
        return null;
    }

    public static Object newProxyInstance(String className, InvocationHandler h) {
        if (h == null || className == null) {
            return null;
        }
        try {
            Class clazz = getClass(className);
            if (clazz == null || clazz.getClassLoader() == null) {
                return null;
            }
            return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, h);
        } catch (IllegalArgumentException e) {
            OPCollectLog.e(TAG, "ReflectionUtils: IllegalArgumentException " + e.getMessage());
            return null;
        }
    }

    public static Field getField(String className, String fieldName) {
        if (className == null || fieldName == null) {
            return null;
        }
        try {
            Class clazz = getClass(className);
            if (clazz != null) {
                return clazz.getDeclaredField(fieldName);
            }
            return null;
        } catch (SecurityException e) {
            OPCollectLog.e(TAG, "ReflectionUtils SecurityException: " + e.getMessage());
            return null;
        } catch (NoSuchFieldException e2) {
            OPCollectLog.e(TAG, "ReflectionUtils : " + fieldName + ", not such field.");
            return null;
        }
    }
}
