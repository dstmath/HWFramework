package android.database.sqlite;

import java.io.Closeable;

public abstract class SQLiteClosable implements Closeable {
    private int mReferenceCount;

    protected abstract void onAllReferencesReleased();

    public SQLiteClosable() {
        this.mReferenceCount = 1;
    }

    @Deprecated
    protected void onAllReferencesReleasedFromContainer() {
        onAllReferencesReleased();
    }

    public void acquireReference() {
        synchronized (this) {
            if (this.mReferenceCount <= 0) {
                throw new IllegalStateException("attempt to re-open an already-closed object: " + this);
            }
            this.mReferenceCount++;
        }
    }

    public void releaseReference() {
        synchronized (this) {
            int i = this.mReferenceCount - 1;
            this.mReferenceCount = i;
            boolean refCountIsZero = i == 0;
        }
        if (refCountIsZero) {
            onAllReferencesReleased();
        }
    }

    @Deprecated
    public void releaseReferenceFromContainer() {
        synchronized (this) {
            int i = this.mReferenceCount - 1;
            this.mReferenceCount = i;
            boolean refCountIsZero = i == 0;
        }
        if (refCountIsZero) {
            onAllReferencesReleasedFromContainer();
        }
    }

    public void close() {
        releaseReference();
    }
}
