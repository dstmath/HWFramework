package com.huawei.okhttp3.internal.connection;

import com.huawei.okhttp3.internal.Util;
import java.io.IOException;

public final class RouteException extends RuntimeException {
    private IOException firstException;
    private IOException lastException;

    RouteException(IOException cause) {
        super(cause);
        this.firstException = cause;
        this.lastException = cause;
    }

    public IOException getFirstConnectException() {
        return this.firstException;
    }

    public IOException getLastConnectException() {
        return this.lastException;
    }

    /* access modifiers changed from: package-private */
    public void addConnectException(IOException e) {
        Util.addSuppressedIfPossible(this.firstException, e);
        this.lastException = e;
    }
}
