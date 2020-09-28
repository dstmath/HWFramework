package com.huawei.utils.reflect;

import android.util.Log;
import com.huawei.utils.reflect.annotation.GetField;
import com.huawei.utils.reflect.annotation.InvokeMethod;
import com.huawei.utils.reflect.annotation.SetField;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class EasyInvokeUtils {
    private static final String TAG = "EasyInvokeUtils";

    public EasyInvokeUtils() {
        loadReflectMethods();
    }

    private void loadReflectMethods() {
        Method[] methods = getClass().getDeclaredMethods();
        for (Method m : methods) {
            Annotation[] annotations = m.getAnnotations();
            if (annotations != null && annotations.length > 0) {
                Annotation target = annotations[0];
                if (target instanceof InvokeMethod) {
                    loadInvokeMethodInfo(m, (InvokeMethod) target);
                } else if (target instanceof GetField) {
                    loadGetFieldInfo(m, (GetField) target);
                } else if (target instanceof SetField) {
                    loadSetFieldInfo(m, (SetField) target);
                }
            }
        }
    }

    private void loadInvokeMethodInfo(Method m, InvokeMethod targetMethodInfo) {
        if (targetMethodInfo != null) {
            Class<?>[] originParams = m.getParameterTypes();
            Class<?>[] targetParams = null;
            if (originParams.length < 1) {
                Log.e(TAG, "no target class");
            }
            Class<?> clazz = originParams[0];
            if (originParams.length > 1) {
                targetParams = (Class[]) Arrays.copyOfRange(originParams, 1, originParams.length);
            }
            try {
                String targetMethodName = targetMethodInfo.methodName();
                if (targetMethodName == null || targetMethodName.trim().length() == 0) {
                    targetMethodName = m.getName();
                }
                Method targetMethod = clazz.getDeclaredMethod(targetMethodName, targetParams);
                targetMethod.setAccessible(true);
                setMethodObject(m, targetMethod, targetMethodInfo);
            } catch (SecurityException e) {
                Log.e(TAG, "loadInvokeMethodInfo catch SecurityException : " + e.toString());
            } catch (NoSuchMethodException e2) {
                Log.e(TAG, "loadInvokeMethodInfo catch NoSuchMethodException : " + e2.toString());
            }
        }
    }

    private void loadGetFieldInfo(Method m, GetField targetFieldInfo) {
        if (targetFieldInfo != null) {
            Class<?>[] originParams = m.getParameterTypes();
            if (originParams.length < 1) {
                Log.e(TAG, "no target class");
            }
            Class<?> clazz = originParams[0];
            String fieldName = targetFieldInfo.fieldName();
            String fieldObjectName = targetFieldInfo.fieldObject();
            if (fieldName == null || fieldName.trim().length() == 0) {
                fieldName = fieldObjectName;
                Log.e(TAG, "no target fieldName");
            }
            try {
                Field targetFiled = clazz.getDeclaredField(fieldName);
                targetFiled.setAccessible(true);
                setFieldObject(m, targetFiled, targetFieldInfo);
            } catch (SecurityException e) {
                Log.e(TAG, "loadGetFieldInfo catch SecurityException : " + e.toString());
            } catch (NoSuchFieldException e2) {
                Log.e(TAG, "loadGetFieldInfo catch NoSuchFieldException : " + e2.toString());
            }
        }
    }

    private void loadSetFieldInfo(Method m, SetField targetFieldInfo) {
        if (targetFieldInfo != null) {
            Class<?>[] originParams = m.getParameterTypes();
            if (originParams.length < 1) {
                Log.e(TAG, "no target class");
                return;
            }
            Class<?> clazz = originParams[0];
            String fieldName = targetFieldInfo.fieldName();
            String fieldObjectName = targetFieldInfo.fieldObject();
            if (fieldName == null || fieldName.trim().length() == 0) {
                fieldName = fieldObjectName;
                Log.e(TAG, "no target fieldName");
            }
            try {
                Field targetFiled = clazz.getDeclaredField(fieldName);
                targetFiled.setAccessible(true);
                setFieldObject(m, targetFiled, targetFieldInfo);
            } catch (SecurityException e) {
                Log.e(TAG, "loadSetFieldInfo catch SecurityException : " + e.toString());
            } catch (NoSuchFieldException e2) {
                Log.e(TAG, "loadSetFieldInfo catch NoSuchFieldException : " + e2.toString());
            }
        }
    }

    private void setFieldObject(Method m, Field targetField, GetField targetFieldInfo) {
        if (targetFieldInfo != null) {
            String fieldName = targetFieldInfo.fieldObject();
            if (fieldName == null || fieldName.trim().length() == 0) {
                fieldName = targetFieldInfo.fieldName();
            }
            setFieldObject(m, targetField, fieldName);
        }
    }

    private void setFieldObject(Method m, Field targetField, SetField targetFieldInfo) {
        if (targetFieldInfo != null) {
            String fieldName = targetFieldInfo.fieldObject();
            if (fieldName == null || fieldName.trim().length() == 0) {
                fieldName = targetFieldInfo.fieldName();
            }
            setFieldObject(m, targetField, fieldName);
        }
    }

    private void setFieldObject(Method m, Field targetFiled, String fieldName) {
        FieldObject fieldObject = new FieldObject();
        fieldObject.field = targetFiled;
        try {
            Field field = getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(this, fieldObject);
        } catch (SecurityException e) {
            Log.e(TAG, "setFieldObject catch SecurityException : " + e.toString());
        } catch (NoSuchFieldException e2) {
            Log.e(TAG, "setFieldObject catch NoSuchFieldException : " + e2.toString());
        } catch (IllegalArgumentException e3) {
            Log.e(TAG, "setFieldObject catch IllegalArgumentException : " + e3.toString());
        } catch (IllegalAccessException e4) {
            Log.e(TAG, "setFieldObject catch IllegalAccessException : " + e4.toString());
        }
    }

    private void setMethodObject(Method m, Method targetMethod, InvokeMethod targetMethodInfo) {
        try {
            Field field = getClass().getDeclaredField(targetMethodInfo.methodObject());
            field.setAccessible(true);
            MethodObject methodObject = new MethodObject();
            methodObject.method = targetMethod;
            field.set(this, methodObject);
        } catch (SecurityException e) {
            Log.e(TAG, "setMethodObject catch SecurityException : " + e.toString());
        } catch (NoSuchFieldException e2) {
            Log.e(TAG, "setMethodObject catch NoSuchFieldException : " + e2.toString());
        } catch (IllegalArgumentException e3) {
            Log.e(TAG, "setMethodObject catch IllegalArgumentException : " + e3.toString());
        } catch (IllegalAccessException e4) {
            Log.e(TAG, "setMethodObject catch IllegalAccessException : " + e4.toString());
        }
    }

    /* access modifiers changed from: protected */
    public <T> T invokeMethod(MethodObject<T> methodObject, Object target, Object... args) {
        if (methodObject == null || methodObject.method == null) {
            Log.e(TAG, "method is null");
            return null;
        }
        try {
            return (T) methodObject.method.invoke(target, args);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "invokeMethod catch IllegalArgumentException : " + e.toString());
            return null;
        } catch (IllegalAccessException e2) {
            Log.e(TAG, "invokeMethod catch IllegalAccessException : " + e2.toString());
            return null;
        } catch (InvocationTargetException e3) {
            Log.e(TAG, "invokeMethod catch InvocationTargetException : " + e3.toString());
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public <T> T getField(FieldObject<T> filedObject, Object target) {
        if (filedObject == null || filedObject.field == null) {
            Log.e(TAG, "field is null");
            return null;
        }
        try {
            return (T) filedObject.field.get(target);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "getField catch IllegalArgumentException : " + e.toString());
            return null;
        } catch (IllegalAccessException e2) {
            Log.e(TAG, "getField catch IllegalAccessException : " + e2.toString());
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void setField(FieldObject filedObject, Object target, Object value) {
        if (filedObject == null || filedObject.field == null) {
            Log.e(TAG, "field is null");
            return;
        }
        try {
            filedObject.field.set(target, value);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "setField catch IllegalArgumentException : " + e.toString());
        } catch (IllegalAccessException e2) {
            Log.e(TAG, "setField catch IllegalAccessException : " + e2.toString());
        }
    }
}
