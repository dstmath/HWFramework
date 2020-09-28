package huawei.android.widget.utils;

import android.util.Log;
import java.lang.reflect.Field;

public class ReflectUtil {
    private static final String EXCEPTION_SUFFIX = " in set object";
    private static final String TAG = "ReflectUtil";

    private ReflectUtil() {
    }

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
            Log.e(TAG, "no field in reflect " + reflectName + EXCEPTION_SUFFIX);
        } catch (SecurityException e2) {
            Log.e(TAG, "SecurityException in reflect " + reflectName + EXCEPTION_SUFFIX);
        } catch (IllegalArgumentException e3) {
            Log.e(TAG, "IllegalArgumentException in reflect " + reflectName + EXCEPTION_SUFFIX);
        } catch (IllegalAccessException e4) {
            Log.e(TAG, "IllegalAccessException in reflect " + reflectName + EXCEPTION_SUFFIX);
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
            Log.e(TAG, "no field in reflect " + reflectName + EXCEPTION_SUFFIX);
            return null;
        } catch (IllegalAccessException e2) {
            Log.e(TAG, "IllegalAccessException in reflect " + reflectName + EXCEPTION_SUFFIX);
            return null;
        }
    }
}
