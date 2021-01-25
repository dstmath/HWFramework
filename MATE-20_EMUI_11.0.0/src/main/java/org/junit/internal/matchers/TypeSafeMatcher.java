package org.junit.internal.matchers;

import java.lang.reflect.Method;
import org.hamcrest.BaseMatcher;
import org.junit.internal.MethodSorter;

@Deprecated
public abstract class TypeSafeMatcher<T> extends BaseMatcher<T> {
    private Class<?> expectedType;

    public abstract boolean matchesSafely(T t);

    protected TypeSafeMatcher() {
        this.expectedType = findExpectedType(getClass());
    }

    private static Class<?> findExpectedType(Class<?> fromClass) {
        for (Class<?> c = fromClass; c != Object.class; c = c.getSuperclass()) {
            Method[] declaredMethods = MethodSorter.getDeclaredMethods(c);
            for (Method method : declaredMethods) {
                if (isMatchesSafelyMethod(method)) {
                    return method.getParameterTypes()[0];
                }
            }
        }
        throw new Error("Cannot determine correct type for matchesSafely() method.");
    }

    private static boolean isMatchesSafelyMethod(Method method) {
        if (!method.getName().equals("matchesSafely") || method.getParameterTypes().length != 1 || method.isSynthetic()) {
            return false;
        }
        return true;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: java.lang.Class<T> */
    /* JADX WARN: Multi-variable type inference failed */
    protected TypeSafeMatcher(Class<T> expectedType2) {
        this.expectedType = expectedType2;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // org.hamcrest.Matcher
    public final boolean matches(Object item) {
        return item != 0 && this.expectedType.isInstance(item) && matchesSafely(item);
    }
}
