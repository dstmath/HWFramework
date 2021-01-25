package ohos.security.keystore.provider;

import android.text.TextUtils;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class ReflectUtil {
    private static final HiLogLabel LABEL = KeyStoreLogger.getLabel(TAG);
    private static final String TAG = "ReflectUtil";

    private ReflectUtil() {
    }

    public static <T> T getField(Object obj, String str, Class<T> cls) {
        Class<?> cls2 = obj.getClass();
        boolean z = false;
        while (cls2 != null && cls2 != Object.class) {
            try {
                Field declaredField = cls2.getDeclaredField(str);
                setAccessible(declaredField);
                z = true;
                Object obj2 = declaredField.get(obj);
                if (obj2 != null && obj2.getClass().equals(cls)) {
                    return cls.cast(obj2);
                }
            } catch (IllegalAccessException | NoSuchFieldException unused) {
                if (z) {
                    HiLog.error(LABEL, "getField failed!", new Object[0]);
                }
            }
            cls2 = cls2.getSuperclass();
        }
        return null;
    }

    public static Object getInstance(String str) {
        try {
            Constructor<?> declaredConstructor = Class.forName(str).getDeclaredConstructor(new Class[0]);
            setAccessible(declaredConstructor);
            return declaredConstructor.newInstance(new Object[0]);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException unused) {
            HiLog.error(LABEL, "get instance failed!", new Object[0]);
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x003c  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x004d A[SYNTHETIC] */
    public static <T> InvokeResult<T> invoke(Object obj, Class<?>[] clsArr, Object[] objArr, Class<T> cls) {
        InvocationTargetException e;
        Throwable reThrowException;
        InvokeResult<T> invokeResult = new InvokeResult<>();
        String methodName = getMethodName();
        if (!checkParameters(obj, clsArr, objArr, cls, methodName)) {
            return invokeResult;
        }
        Class<?> cls2 = obj.getClass();
        Object obj2 = null;
        boolean z = false;
        while (cls2 != null && cls2 != Object.class) {
            try {
                Method declaredMethod = cls2.getDeclaredMethod(methodName, clsArr);
                setAccessible(declaredMethod);
                try {
                    obj2 = declaredMethod.invoke(obj, objArr);
                    break;
                } catch (IllegalAccessException | NoSuchMethodException unused) {
                    z = true;
                } catch (InvocationTargetException e2) {
                    e = e2;
                    z = true;
                    reThrowException = ExceptionAdapter.reThrowException(e.getCause());
                    if (reThrowException == null) {
                        invokeResult.setThrowable(reThrowException);
                    }
                    cls2 = cls2.getSuperclass();
                }
            } catch (IllegalAccessException | NoSuchMethodException unused2) {
                if (z) {
                    HiLog.error(LABEL, "invoke %{public}s + failed!", methodName);
                }
                cls2 = cls2.getSuperclass();
            } catch (InvocationTargetException e3) {
                e = e3;
                reThrowException = ExceptionAdapter.reThrowException(e.getCause());
                if (reThrowException == null) {
                }
                cls2 = cls2.getSuperclass();
            }
        }
        if (obj2 != null && cls.isInstance(obj2)) {
            invokeResult.setResult(cls.cast(obj2));
        }
        return invokeResult;
    }

    private static <T> boolean checkParameters(Object obj, Class<?>[] clsArr, Object[] objArr, Class<T> cls, String str) {
        if (obj == null || clsArr == null || objArr == null || cls == null) {
            return false;
        }
        if (!TextUtils.isEmpty(str)) {
            return true;
        }
        HiLog.warn(LABEL, "unexpected invocation failure, method not found", new Object[0]);
        return false;
    }

    private static void setAccessible(AccessibleObject accessibleObject) {
        if (!accessibleObject.isAccessible()) {
            AccessController.doPrivileged(new PrivilegedAction(accessibleObject) {
                /* class ohos.security.keystore.provider.$$Lambda$ReflectUtil$EnqSaBiDI2S6rNow6t1KrobRUA */
                private final /* synthetic */ AccessibleObject f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.security.PrivilegedAction
                public final Object run() {
                    return this.f$0.setAccessible(true);
                }
            });
        }
    }

    private static String getMethodName() {
        int i;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int i2 = 0;
        while (true) {
            if (i2 >= stackTrace.length) {
                i = -1;
                break;
            } else if (TextUtils.equals(stackTrace[i2].getMethodName(), "invoke")) {
                i = i2 + 1;
                break;
            } else {
                i2++;
            }
        }
        return (i < 0 || i >= stackTrace.length) ? "" : stackTrace[i].getMethodName();
    }
}
