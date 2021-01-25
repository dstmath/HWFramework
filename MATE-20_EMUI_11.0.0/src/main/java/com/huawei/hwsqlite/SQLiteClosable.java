package com.huawei.hwsqlite;

import java.io.Closeable;

public abstract class SQLiteClosable implements Closeable {
    private int mReferenceCount = 1;

    /* access modifiers changed from: protected */
    public abstract void onAllReferencesReleased();

    /* access modifiers changed from: protected */
    @Deprecated
    public void onAllReferencesReleasedFromContainer() {
        onAllReferencesReleased();
    }

    public void acquireReference() {
        synchronized (this) {
            if (this.mReferenceCount > 0) {
                this.mReferenceCount++;
            } else {
                throw new IllegalStateException("attempt to re-open an already-closed object: " + this);
            }
        }
    }

    public void releaseReference() {
        boolean refCountIsZero;
        synchronized (this) {
            refCountIsZero = true;
            int i = this.mReferenceCount - 1;
            this.mReferenceCount = i;
            if (i != 0) {
                refCountIsZero = false;
            }
        }
        if (refCountIsZero) {
            onAllReferencesReleased();
        }
    }

    @Deprecated
    public void releaseReferenceFromContainer() {
        boolean refCountIsZero;
        synchronized (this) {
            refCountIsZero = true;
            int i = this.mReferenceCount - 1;
            this.mReferenceCount = i;
            if (i != 0) {
                refCountIsZero = false;
            }
        }
        if (refCountIsZero) {
            onAllReferencesReleasedFromContainer();
        }
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        releaseReference();
    }
}
