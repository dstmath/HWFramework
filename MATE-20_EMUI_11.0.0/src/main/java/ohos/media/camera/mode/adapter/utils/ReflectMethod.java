package ohos.media.camera.mode.adapter.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class ReflectMethod {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(ReflectMethod.class);
    private Class<?> clazz;
    private Method method;

    public ReflectMethod(ReflectClass reflectClass, String str, Class<?>... clsArr) {
        if (reflectClass != null) {
            this.clazz = reflectClass.getClazz();
            this.method = reflectClass.getMethod(str, clsArr);
            return;
        }
        LOGGER.error("input value is null", new Object[0]);
    }

    public Object invokeS(Object... objArr) {
        Method method2;
        Class<?> cls = this.clazz;
        if (!(cls == null || (method2 = this.method) == null)) {
            try {
                return method2.invoke(cls, objArr);
            } catch (IllegalAccessException unused) {
                LOGGER.error("reflectInvoke(%{public}s) IllegalAccessException", this.method.getName());
            } catch (InvocationTargetException unused2) {
                LOGGER.error("reflectInvoke(%{public}s) InvocationTargetException", this.method.getName());
            } catch (IllegalArgumentException unused3) {
                LOGGER.error("reflectInvoke(%{public}s) IllegalArgumentException", this.method.getName());
            }
        }
        return null;
    }

    public Object invoke(Object obj, Object... objArr) {
        Method method2;
        if (!(obj == null || this.clazz == null || (method2 = this.method) == null)) {
            try {
                return method2.invoke(obj, objArr);
            } catch (IllegalAccessException unused) {
                LOGGER.error("invoke(%{public}s) IllegalAccessException", this.method.getName());
            } catch (InvocationTargetException unused2) {
                LOGGER.error("invoke(%{public}s) InvocationTargetException", this.method.getName());
            } catch (IllegalArgumentException unused3) {
                LOGGER.error("invoke(%{public}s) IllegalArgumentException", this.method.getName());
            }
        }
        return null;
    }
}
