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
        for (Method m : getClass().getDeclaredMethods()) {
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
            Class[] clsArr = null;
            if (originParams.length < 1) {
                Log.e(TAG, "no target class");
            }
            Class<?> clazz = originParams[0];
            if (originParams.length > 1) {
                clsArr = (Class[]) Arrays.copyOfRange(originParams, 1, originParams.length);
            }
            try {
                String targetMethodName = targetMethodInfo.methodName();
                if (targetMethodName == null || targetMethodName.trim().length() == 0) {
                    targetMethodName = m.getName();
                }
                Method targetMethod = clazz.getDeclaredMethod(targetMethodName, clsArr);
                targetMethod.setAccessible(true);
                setMethodObject(m, targetMethod, targetMethodInfo);
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
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
                e.printStackTrace();
            } catch (NoSuchFieldException e2) {
                e2.printStackTrace();
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
                e.printStackTrace();
            } catch (NoSuchFieldException e2) {
                e2.printStackTrace();
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
            e.printStackTrace();
        } catch (NoSuchFieldException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
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
            e.printStackTrace();
        } catch (NoSuchFieldException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        }
    }

    protected <T> T invokeMethod(MethodObject<T> methodObject, Object target, Object... args) {
        if (methodObject == null || methodObject.method == null) {
            Log.e(TAG, "method is null");
            return null;
        }
        try {
            return methodObject.method.invoke(target, args);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            return null;
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
            return null;
        }
    }

    protected <T> T getField(FieldObject<T> filedObject, Object target) {
        if (filedObject == null || filedObject.field == null) {
            Log.e(TAG, "field is null");
            return null;
        }
        try {
            return filedObject.field.get(target);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    protected void setField(FieldObject filedObject, Object target, Object value) {
        if (filedObject == null || filedObject.field == null) {
            Log.e(TAG, "field is null");
            return;
        }
        try {
            filedObject.field.set(target, value);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        }
    }
}
