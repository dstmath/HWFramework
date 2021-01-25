package com.huawei.utils.reflect;

import android.net.Uri;
import android.util.Log;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class HwReflectUtils {
    private static final String TAG = HwReflectUtils.class.getSimpleName();

    public static Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            String str = TAG;
            Log.e(str, "className not found:" + className);
            return null;
        }
    }

    public static Method getMethod(Class<?> targetClass, String name, Class<?>... parameterTypes) {
        if (targetClass == null || isEmpty(name)) {
            return null;
        }
        try {
            return targetClass.getMethod(name, parameterTypes);
        } catch (SecurityException e) {
            Log.e(TAG, "getMethod SecurityException");
            return null;
        } catch (NoSuchMethodException e2) {
            String str = TAG;
            Log.e(str, name + ", not such method.");
            return null;
        }
    }

    public static Field getField(Class<?> targetClass, String name) {
        if (targetClass == null || isEmpty(name)) {
            return null;
        }
        try {
            return targetClass.getField(name);
        } catch (SecurityException e) {
            Log.e(TAG, "getField SecurityException");
            return null;
        } catch (NoSuchFieldException e2) {
            String str = TAG;
            Log.e(str, name + ", no such field.");
            return null;
        }
    }

    public static Field getDeclaredField(Class<?> targetClass, String name) {
        if (targetClass == null || isEmpty(name)) {
            return null;
        }
        try {
            return targetClass.getDeclaredField(name);
        } catch (SecurityException e) {
            Log.e(TAG, "getDeclaredField SecurityException");
            return null;
        } catch (NoSuchFieldException e2) {
            String str = TAG;
            Log.e(str, name + ", no such field.");
            return null;
        }
    }

    public static Constructor<?> getConstructor(Class<?> targetClass, Class<?>... types) {
        if (targetClass == null || types == null) {
            return null;
        }
        try {
            return targetClass.getConstructor(types);
        } catch (SecurityException e) {
            Log.e(TAG, "getConstructor SecurityException.");
            return null;
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "getConstructor NoSuchMethodException.");
            return null;
        }
    }

    public static Object newInstance(Constructor<?> constructor, Object... args) {
        if (constructor == null) {
            return null;
        }
        try {
            return constructor.newInstance(args);
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, "Exception in newInstance: " + e.getClass().getSimpleName());
            return null;
        }
    }

    public static Object invoke(Object receiver, Method method, Object... args) {
        if (method != null) {
            try {
                return method.invoke(receiver, args);
            } catch (RuntimeException re) {
                String str = TAG;
                Log.e(str, "Exception in invoke: " + re.getClass().getSimpleName());
                if ("com.huawei.android.util.NoExtAPIException".equals(re.getClass().getName())) {
                    throw new UnsupportedOperationException();
                }
                throw new UnsupportedOperationException();
            } catch (Exception e) {
                String str2 = TAG;
                Log.e(str2, "Exception in invoke: " + e.getCause() + "; method=" + method.getName());
                throw new UnsupportedOperationException();
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public static Object getFieldValue(Object receiver, Field field) {
        if (field == null) {
            return null;
        }
        try {
            return field.get(receiver);
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, "Exception in getFieldValue: " + e.getClass().getSimpleName());
            throw new UnsupportedOperationException();
        }
    }

    public static void setFieldValue(Object receiver, Field field, Object value) {
        if (field != null) {
            try {
                field.set(receiver, value);
            } catch (Exception e) {
                String str = TAG;
                Log.e(str, "Exception in setFieldValue: " + e.getClass().getSimpleName());
            }
        }
    }

    private static boolean isEmpty(String str) {
        if (str == null || str.length() == 0 || str.trim().length() == 0) {
            return true;
        }
        return false;
    }

    public static int objectToInt(Object object) {
        if (object != null && (object instanceof Integer)) {
            return ((Integer) object).intValue();
        }
        return -1;
    }

    public static int objectToInt(Object object, int def) {
        if (object != null && (object instanceof Integer)) {
            return ((Integer) object).intValue();
        }
        return def;
    }

    public static String objectToString(Object object) {
        if (object != null && (object instanceof String)) {
            return (String) object;
        }
        return null;
    }

    public static Uri objectToUri(Object object) {
        if (object != null && (object instanceof Uri)) {
            return (Uri) object;
        }
        return null;
    }

    public static boolean objectToBoolean(Object object) {
        if (object != null && (object instanceof Boolean)) {
            return ((Boolean) object).booleanValue();
        }
        return false;
    }
}
