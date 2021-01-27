package com.huawei.secure.android.common.util;

import android.text.TextUtils;
import java.lang.reflect.InvocationTargetException;

public class ReflectUtil {
    private static final String TAG = "ReflectUtil";

    public static Object invoke(String className, String funName, Class<?>[] paramsType, Object[] params) throws Exception {
        Class cls = Class.forName(className);
        return invoke(cls, cls.newInstance(), funName, paramsType, params);
    }

    public static String getSystemProperty(String propertyName) {
        if (TextUtils.isEmpty(propertyName)) {
            return null;
        }
        try {
            Object object = invoke("android.os.SystemProperties", "get", new Class[]{String.class}, new Object[]{propertyName});
            if (object instanceof String) {
                return (String) object;
            }
            return null;
        } catch (Exception e) {
            LogsUtil.e(TAG, "getSystemProperties, Excetion." + e.getMessage());
            return null;
        }
    }

    private static Object invoke(Class<?> cls, Object obj, String funName, Class<?>[] paramsType, Object[] params) throws Exception {
        paramsCheck(cls, paramsType, params);
        try {
            try {
                return cls.getMethod(funName, paramsType).invoke(obj, params);
            } catch (IllegalAccessException e) {
                LogsUtil.e(TAG, "IllegalAccessException" + e.getMessage(), true);
                return null;
            } catch (IllegalArgumentException e2) {
                LogsUtil.e(TAG, "IllegalArgumentException" + e2.getMessage(), true);
                return null;
            } catch (InvocationTargetException e3) {
                LogsUtil.e(TAG, "InvocationTargetException" + e3.getMessage(), true);
                return null;
            }
        } catch (NoSuchMethodException e4) {
            LogsUtil.e(TAG, "NoSuchMethodException" + e4.getMessage(), true);
            return null;
        }
    }

    private static void paramsCheck(Class cls, Class[] paramsType, Object[] params) throws Exception {
        if (cls == null) {
            throw new Exception("class is null in staticFun");
        } else if (paramsType == null) {
            if (params != null) {
                throw new Exception("paramsType is null, but params is not null");
            }
        } else if (params == null) {
            throw new Exception("paramsType or params should be same");
        } else if (paramsType.length != params.length) {
            throw new Exception("paramsType len:" + paramsType.length + " should equal params.len:" + params.length);
        }
    }

    public static int getIntFiled(Class<?> cls, String filedName, int def) {
        try {
            return cls.getField(filedName).getInt(null);
        } catch (IllegalArgumentException e) {
            LogsUtil.e(TAG, "IllegalArgumentException err:" + e.getMessage());
            return def;
        } catch (IllegalAccessException e2) {
            LogsUtil.e(TAG, "IllegalAccessException err:" + e2.getMessage());
            return def;
        } catch (NoSuchFieldException e3) {
            LogsUtil.e(TAG, "NoSuchFieldException err:" + e3.getMessage());
            return def;
        }
    }

    public static int getIntFiled(String className, String filedName, int def) {
        try {
            return getIntFiled(Class.forName(className), filedName, def);
        } catch (Exception e) {
            LogsUtil.e(TAG, "getIntFiled exception" + e.getMessage(), true);
            return def;
        }
    }

    public static Object getFiled(Class<?> cls, String filedName, Object def) {
        try {
            return cls.getField(filedName).get(null);
        } catch (IllegalArgumentException e) {
            LogsUtil.e(TAG, "IllegalArgumentException" + e.getMessage(), true);
            return def;
        } catch (IllegalAccessException e2) {
            LogsUtil.e(TAG, "IllegalAccessException" + e2.getMessage(), true);
            return def;
        } catch (NoSuchFieldException e3) {
            LogsUtil.e(TAG, "NoSuchFieldException" + e3.getMessage(), true);
            return def;
        } catch (Exception e4) {
            LogsUtil.e(TAG, "Exception" + e4.getMessage(), true);
            return def;
        }
    }

    public static Object getFiled(String className, String filedName, Object def) {
        try {
            return Class.forName(className).getField(filedName).get(null);
        } catch (IllegalArgumentException e) {
            LogsUtil.e(TAG, "IllegalArgumentException" + e.getMessage(), true);
            return def;
        } catch (IllegalAccessException e2) {
            LogsUtil.e(TAG, "IllegalAccessException" + e2.getMessage(), true);
            return def;
        } catch (NoSuchFieldException e3) {
            LogsUtil.e(TAG, "NoSuchMethodException" + e3.getMessage(), true);
            return def;
        } catch (ClassNotFoundException e4) {
            LogsUtil.e(TAG, "ClassNotFoundException" + e4.getMessage(), true);
            return def;
        } catch (Exception e5) {
            LogsUtil.e(TAG, "Exception" + e5.getMessage(), true);
            return def;
        }
    }

    public static Class getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            LogsUtil.e(TAG, "ClassNotFoundException" + e.getMessage(), true);
            return null;
        } catch (Exception e2) {
            LogsUtil.e(TAG, "Exception" + e2.getMessage(), true);
            return null;
        } catch (Throwable e3) {
            LogsUtil.e(TAG, "Throwable" + e3.getMessage(), true);
            return null;
        }
    }
}
