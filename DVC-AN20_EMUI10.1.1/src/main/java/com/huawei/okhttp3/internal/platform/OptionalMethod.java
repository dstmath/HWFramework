package com.huawei.okhttp3.internal.platform;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/* access modifiers changed from: package-private */
public class OptionalMethod<T> {
    private final String methodName;
    private final Class[] methodParams;
    private final Class<?> returnType;

    OptionalMethod(Class<?> returnType2, String methodName2, Class... methodParams2) {
        this.returnType = returnType2;
        this.methodName = methodName2;
        this.methodParams = methodParams2;
    }

    public boolean isSupported(T target) {
        return getMethod(target.getClass()) != null;
    }

    public Object invokeOptional(T target, Object... args) throws InvocationTargetException {
        Method m = getMethod(target.getClass());
        if (m == null) {
            return null;
        }
        try {
            return m.invoke(target, args);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    public Object invokeOptionalWithoutCheckedException(T target, Object... args) {
        try {
            return invokeOptional(target, args);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw ((RuntimeException) targetException);
            }
            AssertionError error = new AssertionError("Unexpected exception");
            error.initCause(targetException);
            throw error;
        }
    }

    public Object invoke(T target, Object... args) throws InvocationTargetException {
        Method m = getMethod(target.getClass());
        if (m != null) {
            try {
                return m.invoke(target, args);
            } catch (IllegalAccessException e) {
                AssertionError error = new AssertionError("Unexpectedly could not call: " + m);
                error.initCause(e);
                throw error;
            }
        } else {
            throw new AssertionError("Method " + this.methodName + " not supported for object " + ((Object) target));
        }
    }

    public Object invokeWithoutCheckedException(T target, Object... args) {
        try {
            return invoke(target, args);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw ((RuntimeException) targetException);
            }
            AssertionError error = new AssertionError("Unexpected exception");
            error.initCause(targetException);
            throw error;
        }
    }

    private Method getMethod(Class<?> clazz) {
        Class<?> cls;
        String str = this.methodName;
        if (str == null) {
            return null;
        }
        Method method = getPublicMethod(clazz, str, this.methodParams);
        if (method == null || (cls = this.returnType) == null || cls.isAssignableFrom(method.getReturnType())) {
            return method;
        }
        return null;
    }

    private static Method getPublicMethod(Class<?> clazz, String methodName2, Class[] parameterTypes) {
        Method method = null;
        try {
            method = clazz.getMethod(methodName2, parameterTypes);
            if ((method.getModifiers() & 1) == 0) {
                return null;
            }
            return method;
        } catch (NoSuchMethodException e) {
            return method;
        }
    }
}
