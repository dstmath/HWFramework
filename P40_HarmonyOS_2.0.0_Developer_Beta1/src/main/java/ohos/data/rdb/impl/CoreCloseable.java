package ohos.data.rdb.impl;

import java.io.Closeable;

public abstract class CoreCloseable implements Closeable {
    private final Object closeLock = new Object();
    private int referenceCount = 1;

    /* access modifiers changed from: protected */
    public abstract void onAllRefReleased();

    public void acquireRef() {
        synchronized (this.closeLock) {
            if (this.referenceCount > 0) {
                this.referenceCount++;
            } else {
                throw new IllegalStateException("attempt to re-open an already-closed object: " + this);
            }
        }
    }

    public void releaseRef() {
        boolean z;
        synchronized (this.closeLock) {
            z = true;
            int i = this.referenceCount - 1;
            this.referenceCount = i;
            if (i != 0) {
                z = false;
            }
        }
        if (z) {
            onAllRefReleased();
        }
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        releaseRef();
    }
}
