package com.huawei.okhttp3.internal.connection;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class RouteException extends RuntimeException {
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

    /* JADX WARNING: Removed duplicated region for block: B:4:0x0010 A:{ExcHandler: java.lang.reflect.InvocationTargetException (e java.lang.reflect.InvocationTargetException), Splitter: B:2:0x0004} */
    /* JADX WARNING: Missing block: B:7:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void addSuppressedIfPossible(IOException e, IOException suppressed) {
        if (addSuppressedExceptionMethod != null) {
            try {
                addSuppressedExceptionMethod.invoke(e, new Object[]{suppressed});
            } catch (InvocationTargetException e2) {
            }
        }
    }
}
