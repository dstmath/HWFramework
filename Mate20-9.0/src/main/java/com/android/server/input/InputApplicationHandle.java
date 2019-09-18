package com.android.server.input;

import java.lang.annotation.RCUnownedRef;

public final class InputApplicationHandle {
    @RCUnownedRef
    public final Object appWindowToken;
    public long dispatchingTimeoutNanos;
    public String name;
    private long ptr;

    private native void nativeDispose();

    public InputApplicationHandle(Object appWindowToken2) {
        this.appWindowToken = appWindowToken2;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            nativeDispose();
        } finally {
            super.finalize();
        }
    }
}
