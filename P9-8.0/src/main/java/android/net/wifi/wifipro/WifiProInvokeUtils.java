package android.net.wifi.wifipro;

import android.util.Log;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class WifiProInvokeUtils {
    private static final String TAG = "WifiProInvokeUtils";
    private static Map<Class<?>, WifiProInvokeUtils> invokeUtilsMap = new HashMap();

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface InvokeMethod {
        String methodName() default "";

        String methodObject();
    }

    public class MethodObject<T> {
        Method method;
    }

    public WifiProInvokeUtils() {
        loadReflectMethods();
    }

    private void loadReflectMethods() {
        for (Method m : getClass().getDeclaredMethods()) {
            loadInvokeMethodInfo(m);
        }
    }

    private void loadInvokeMethodInfo(Method m) {
        InvokeMethod targetMethodInfo = (InvokeMethod) m.getAnnotation(InvokeMethod.class);
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
                setMethodObject(m, targetMethod);
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            }
        }
    }

    private void setMethodObject(Method m, Method targetMethod) {
        try {
            Field field = getClass().getDeclaredField(((InvokeMethod) m.getAnnotation(InvokeMethod.class)).methodObject());
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
        }
        if (methodObject != null) {
            try {
                if (methodObject.method != null) {
                    return methodObject.method.invoke(target, args);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (InvocationTargetException e3) {
                e3.printStackTrace();
            }
        }
        return null;
    }

    public static synchronized <T extends WifiProInvokeUtils> T getInvokeUtils(Class<T> clazz) {
        WifiProInvokeUtils invokeUtil;
        synchronized (WifiProInvokeUtils.class) {
            invokeUtil = (WifiProInvokeUtils) invokeUtilsMap.get(clazz);
            if (invokeUtil == null) {
                try {
                    invokeUtil = (WifiProInvokeUtils) clazz.getConstructor(new Class[0]).newInstance(new Object[0]);
                    invokeUtilsMap.put(clazz, invokeUtil);
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e2) {
                    e2.printStackTrace();
                } catch (IllegalArgumentException e3) {
                    e3.printStackTrace();
                } catch (InstantiationException e4) {
                    e4.printStackTrace();
                } catch (IllegalAccessException e5) {
                    e5.printStackTrace();
                } catch (InvocationTargetException e6) {
                    e6.printStackTrace();
                }
                if (invokeUtil == null) {
                    Log.e(TAG, "create instance error clazz[" + clazz + "]");
                }
            }
        }
        return invokeUtil;
    }
}
