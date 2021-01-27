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

    public WifiProInvokeUtils() {
        loadReflectMethods();
    }

    public class MethodObject<T> {
        Method method;

        public MethodObject() {
        }
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

    /* access modifiers changed from: protected */
    public <T> T invokeMethod(MethodObject<T> methodObject, Object target, Object... args) {
        if (methodObject == null || methodObject.method == null) {
            Log.e(TAG, "method is null");
        }
        if (methodObject == null) {
            return null;
        }
        try {
            if (methodObject.method != null) {
                return (T) methodObject.method.invoke(target, args);
            }
            return null;
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

    public static synchronized <T extends WifiProInvokeUtils> T getInvokeUtils(Class<T> clazz) {
        WifiProInvokeUtils invokeUtil;
        synchronized (WifiProInvokeUtils.class) {
            invokeUtil = (T) invokeUtilsMap.get(clazz);
            if (invokeUtil == null) {
                try {
                    WifiProInvokeUtils invokeUtil2 = clazz.getConstructor(new Class[0]).newInstance(new Object[0]);
                    invokeUtil = (T) invokeUtil2;
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
        return (T) invokeUtil;
    }
}
