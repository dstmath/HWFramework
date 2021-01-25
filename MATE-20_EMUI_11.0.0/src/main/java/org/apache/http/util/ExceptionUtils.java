package org.apache.http.util;

import java.lang.reflect.Method;

@Deprecated
public final class ExceptionUtils {
    private static final Method INIT_CAUSE_METHOD = getInitCauseMethod();

    private static Method getInitCauseMethod() {
        try {
            return Throwable.class.getMethod("initCause", Throwable.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static void initCause(Throwable throwable, Throwable cause) {
        Method method = INIT_CAUSE_METHOD;
        if (method != null) {
            try {
                method.invoke(throwable, cause);
            } catch (Exception e) {
            }
        }
    }

    private ExceptionUtils() {
    }
}
