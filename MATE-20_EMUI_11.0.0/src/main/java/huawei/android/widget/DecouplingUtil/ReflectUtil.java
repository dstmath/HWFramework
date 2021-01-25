package huawei.android.widget.DecouplingUtil;

import android.util.Log;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectUtil {
    private static final String TAG = "ReflectUtil";

    public static void setObject(String reflectName, Object instance, Object object, Class<?> clazz) {
        if (instance == null) {
            Log.w(TAG, "reflect setObject instance is null");
            return;
        }
        try {
            Field field = clazz.getDeclaredField(reflectName);
            field.setAccessible(true);
            field.set(instance, object);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "no field in reflect " + reflectName + " in set object");
        } catch (SecurityException e2) {
            Log.e(TAG, "SecurityException in reflect " + reflectName + " in set object");
        } catch (IllegalArgumentException e3) {
            Log.e(TAG, "IllegalArgumentException in reflect " + reflectName + " in set object");
        } catch (IllegalAccessException e4) {
            Log.e(TAG, "IllegalAccessException in reflect " + reflectName + " in set object");
        }
    }

    public static Object getObject(Object instance, String reflectName, Class<?> clazz) {
        if (instance == null) {
            Log.w(TAG, "reflect getObject instance is null");
            return null;
        }
        try {
            Field field = clazz.getDeclaredField(reflectName);
            field.setAccessible(true);
            return field.get(instance);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "no field in reflect " + reflectName + " in get object");
            return null;
        } catch (IllegalAccessException e2) {
            Log.e(TAG, "IllegalAccessException in reflect " + reflectName + " in get object");
            return null;
        }
    }

    public static Object callMethod(Object instance, String methodName, Class[] classesArgs, Object[] objectsArgs, Class<?> clazz) {
        if (instance == null) {
            Log.w(TAG, "reflect callMethod instance is null");
            return null;
        }
        try {
            Method method = clazz.getDeclaredMethod(methodName, classesArgs);
            method.setAccessible(true);
            return method.invoke(instance, objectsArgs);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "there is no " + methodName + "method");
            return null;
        } catch (IllegalArgumentException e2) {
            Log.e(TAG, "IllegalArgumentException in reflect call " + methodName);
            return null;
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "IllegalAccessException in reflect call " + methodName);
            return null;
        } catch (InvocationTargetException e4) {
            Log.e(TAG, "InvocationTargetException in reflect call " + methodName);
            return null;
        }
    }

    public static Class<?> getPrivateClass(String clazzName) {
        try {
            return Class.forName(clazzName);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "getPrivateClass: no class named " + clazzName);
            return null;
        }
    }

    public static Object createPrivateInnerInstance(Class<?> clazz, Class<?> outClass, Object outInstance, Class[] argsSignature, Object[] argsInstance) {
        if (clazz == null) {
            return null;
        }
        if (argsSignature == null || argsInstance == null) {
            Constructor<?> constructor = clazz.getDeclaredConstructor(outClass);
            constructor.setAccessible(true);
            return constructor.newInstance(outInstance);
        }
        try {
            Constructor<?> constructor2 = clazz.getDeclaredConstructor(argsSignature);
            constructor2.setAccessible(true);
            return constructor2.newInstance(argsInstance);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "createPrivateInnerInstance: no constructor");
            return null;
        } catch (IllegalAccessException e2) {
            Log.e(TAG, "createPrivateInnerInstance: IllegalAccessException");
            return null;
        } catch (InstantiationException e3) {
            Log.e(TAG, "createPrivateInnerInstance: InstantiationException");
            return null;
        } catch (InvocationTargetException e4) {
            Log.e(TAG, "createPrivateInnerInstance: InvocationTargetException");
            return null;
        }
    }
}
