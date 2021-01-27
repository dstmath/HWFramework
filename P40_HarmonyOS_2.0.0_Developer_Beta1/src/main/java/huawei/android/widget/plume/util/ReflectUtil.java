package huawei.android.widget.plume.util;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import huawei.android.widget.plume.model.AttrInfo;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectUtil {
    private static final String TAG = ReflectUtil.class.getSimpleName();

    private ReflectUtil() {
    }

    public static Class<?> getClass(Context context, String className) {
        try {
            return Class.forName(className, false, context.getClassLoader());
        } catch (ClassNotFoundException e) {
            String str = TAG;
            Log.e(str, "Plume: ClassNotFoundException in reflect call " + className);
            return null;
        }
    }

    public static Method getMethod(String methodName, Class[] classesArgs, Class<?> clazz) {
        try {
            Method method = clazz.getMethod(methodName, classesArgs);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            String str = TAG;
            Log.e(str, "Plume: There is no " + methodName + " method");
            return null;
        } catch (SecurityException e2) {
            String str2 = TAG;
            Log.e(str2, "Plume: SecurityException in reflect call " + methodName);
            return null;
        }
    }

    public static Object invokeMethod(Object instance, Method method, Object[] objectsArgs) {
        try {
            return method.invoke(instance, objectsArgs);
        } catch (IllegalAccessException e) {
            String str = TAG;
            Log.e(str, "Plume: IllegalAccessException in reflect call " + method.getName());
            return null;
        } catch (IllegalArgumentException e2) {
            String str2 = TAG;
            Log.e(str2, "Plume: IllegalArgumentException in reflect call " + method.getName());
            return null;
        } catch (InvocationTargetException e3) {
            String str3 = TAG;
            Log.e(str3, "Plume: InvocationTargetException in reflect call " + method.getName());
            return null;
        }
    }

    public static void invokeReflect(AttrInfo attrInfo) {
        if (attrInfo == null) {
            Log.e(TAG, "Plume: attr info is null.");
            return;
        }
        Object host = attrInfo.getHost();
        int attrFlag = attrInfo.getFlag();
        if (attrFlag == 1) {
            invokeReflectCore(host, attrInfo);
        } else if (attrFlag != 2) {
            String str = TAG;
            Log.w(str, "Plume: reflect: unhandled attr flag: " + attrFlag);
        } else if (!(host instanceof View)) {
            String str2 = TAG;
            Log.e(str2, "Plume: host isn't instance of View, method: " + attrInfo.getMethodName());
        } else {
            View view = (View) host;
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            invokeReflectCore(layoutParams, attrInfo);
            view.setLayoutParams(layoutParams);
        }
    }

    private static void invokeReflectCore(Object instance, AttrInfo attrInfo) {
        if (instance == null || attrInfo == null) {
            Log.e(TAG, "Plume: invoke reflect failed.");
            return;
        }
        Method method = getMethod(attrInfo.getMethodName(), attrInfo.getClassArgs(), instance.getClass());
        if (method == null) {
            String str = TAG;
            Log.e(str, "Plume: method is null, method: " + attrInfo.getMethodName());
            return;
        }
        invokeMethod(instance, method, attrInfo.getObjectArgs());
    }
}
