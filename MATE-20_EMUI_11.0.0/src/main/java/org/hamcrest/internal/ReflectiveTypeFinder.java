package org.hamcrest.internal;

import java.lang.reflect.Method;

public class ReflectiveTypeFinder {
    private final int expectedNumberOfParameters;
    private final String methodName;
    private final int typedParameter;

    public ReflectiveTypeFinder(String methodName2, int expectedNumberOfParameters2, int typedParameter2) {
        this.methodName = methodName2;
        this.expectedNumberOfParameters = expectedNumberOfParameters2;
        this.typedParameter = typedParameter2;
    }

    public Class<?> findExpectedType(Class<?> fromClass) {
        for (Class<?> c = fromClass; c != Object.class; c = c.getSuperclass()) {
            Method[] declaredMethods = c.getDeclaredMethods();
            for (Method method : declaredMethods) {
                if (canObtainExpectedTypeFrom(method)) {
                    return expectedTypeFrom(method);
                }
            }
        }
        throw new Error("Cannot determine correct type for " + this.methodName + "() method.");
    }

    /* access modifiers changed from: protected */
    public boolean canObtainExpectedTypeFrom(Method method) {
        return method.getName().equals(this.methodName) && method.getParameterTypes().length == this.expectedNumberOfParameters && !method.isSynthetic();
    }

    /* access modifiers changed from: protected */
    public Class<?> expectedTypeFrom(Method method) {
        return method.getParameterTypes()[this.typedParameter];
    }
}
