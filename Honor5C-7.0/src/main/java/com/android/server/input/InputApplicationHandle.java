package com.android.server.input;

public final class InputApplicationHandle {
    public final Object appWindowToken;
    public long dispatchingTimeoutNanos;
    public String name;
    private long ptr;

    private native void nativeDispose();

    public InputApplicationHandle(Object appWindowToken) {
        this.appWindowToken = appWindowToken;
    }

    protected void finalize() throws Throwable {
        try {
            nativeDispose();
        } finally {
            super.finalize();
        }
    }
}
