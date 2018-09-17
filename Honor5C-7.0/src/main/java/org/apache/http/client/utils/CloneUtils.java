package org.apache.http.client.utils;

import java.lang.reflect.InvocationTargetException;

@Deprecated
public class CloneUtils {
    public static Object clone(Object obj) throws CloneNotSupportedException {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Cloneable) {
            try {
                try {
                    return obj.getClass().getMethod("clone", (Class[]) null).invoke(obj, (Object[]) null);
                } catch (InvocationTargetException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof CloneNotSupportedException) {
                        throw ((CloneNotSupportedException) cause);
                    }
                    throw new Error("Unexpected exception", cause);
                } catch (IllegalAccessException ex2) {
                    throw new IllegalAccessError(ex2.getMessage());
                }
            } catch (NoSuchMethodException ex3) {
                throw new NoSuchMethodError(ex3.getMessage());
            }
        }
        throw new CloneNotSupportedException();
    }

    private CloneUtils() {
    }
}
