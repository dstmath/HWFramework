package com.android.okhttp.internal.http;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class RouteException extends Exception {
    private static final Method addSuppressedExceptionMethod;
    private IOException lastException;

    static {
        Method m;
        try {
            m = Throwable.class.getDeclaredMethod("addSuppressed", new Class[]{Throwable.class});
        } catch (Exception e) {
            m = null;
        }
        addSuppressedExceptionMethod = m;
    }

    public RouteException(IOException cause) {
        super(cause);
        this.lastException = cause;
    }

    public IOException getLastConnectException() {
        return this.lastException;
    }

    public void addConnectException(IOException e) {
        addSuppressedIfPossible(e, this.lastException);
        this.lastException = e;
    }

    private void addSuppressedIfPossible(IOException e, IOException suppressed) {
        if (addSuppressedExceptionMethod != null) {
            try {
                addSuppressedExceptionMethod.invoke(e, new Object[]{suppressed});
            } catch (IllegalAccessException | InvocationTargetException e2) {
            }
        }
    }
}
