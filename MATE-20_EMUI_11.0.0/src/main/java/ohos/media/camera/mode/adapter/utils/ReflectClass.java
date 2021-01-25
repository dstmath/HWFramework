package ohos.media.camera.mode.adapter.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class ReflectClass {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(ReflectClass.class);
    private Class<?> clazz;
    private Method[] methods;

    public ReflectClass(String str) {
        this.clazz = getClass(str);
        Class<?> cls = this.clazz;
        if (cls != null) {
            this.methods = cls.getMethods();
        }
    }

    private static boolean isEmptyString(String str) {
        return str == null || str.trim().isEmpty();
    }

    public Object invokeS(String str, Object... objArr) {
        if (isEmptyString(str)) {
            LOGGER.warn("the methodName value is null", new Object[0]);
            return null;
        }
        Optional<Method> findMethod = findMethod(str);
        if (findMethod.isPresent()) {
            try {
                return findMethod.get().invoke(this.clazz, objArr);
            } catch (IllegalAccessException unused) {
                LOGGER.error("reflectInvoke(%{public}s) IllegalAccessException", str);
            } catch (InvocationTargetException unused2) {
                LOGGER.error("reflectInvoke(%{public}s) InvocationTargetException", str);
            } catch (IllegalArgumentException unused3) {
                LOGGER.error("reflectInvoke(%{public}s) IllegalArgumentException: ", str);
            }
        }
        return null;
    }

    private Optional<Method> findMethod(String str) {
        Method[] methodArr = this.methods;
        if (methodArr == null) {
            return Optional.empty();
        }
        for (Method method : methodArr) {
            if (method.getName().equals(str)) {
                return Optional.ofNullable(method);
            }
        }
        LOGGER.error("Can't findMethod method: %{public}s", str);
        return Optional.empty();
    }

    /* access modifiers changed from: package-private */
    public Method getMethod(String str, Class<?>... clsArr) {
        Class<?> cls = this.clazz;
        if (cls == null) {
            return null;
        }
        try {
            return cls.getDeclaredMethod(str, clsArr);
        } catch (NoSuchMethodException e) {
            LOGGER.error("getDeclaredMethod Exception: %{public}s", e.getMessage());
            return null;
        }
    }

    private Class<?> getClass(String str) {
        try {
            return Class.forName(str);
        } catch (ClassNotFoundException unused) {
            LOGGER.debug("ClassNotFoundException: %{public}s", str);
            return null;
        }
    }

    public Class<?> getClazz() {
        return this.clazz;
    }
}
