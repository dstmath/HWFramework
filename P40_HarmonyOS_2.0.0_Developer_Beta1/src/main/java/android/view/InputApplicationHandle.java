package android.view;

import android.os.IBinder;

public final class InputApplicationHandle {
    public long dispatchingTimeoutNanos;
    public String name;
    private long ptr;
    public IBinder token;

    private native void nativeDispose();

    public InputApplicationHandle(IBinder token2) {
        this.token = token2;
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
