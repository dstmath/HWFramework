package android.graphics;

public class ColorFilter {
    private long mNativeInstance;

    static native void nSafeUnref(long j);

    long createNativeInstance() {
        return 0;
    }

    void discardNativeInstance() {
        if (this.mNativeInstance != 0) {
            nSafeUnref(this.mNativeInstance);
            this.mNativeInstance = 0;
        }
    }

    protected void finalize() throws Throwable {
        try {
            if (this.mNativeInstance != 0) {
                nSafeUnref(this.mNativeInstance);
            }
            this.mNativeInstance = -1;
        } finally {
            super.finalize();
        }
    }

    public long getNativeInstance() {
        if (this.mNativeInstance == -1) {
            throw new IllegalStateException("attempting to use a finalized ColorFilter");
        }
        if (this.mNativeInstance == 0) {
            this.mNativeInstance = createNativeInstance();
        }
        return this.mNativeInstance;
    }
}
