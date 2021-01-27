package android.graphics;

public class DrawFilter {
    public long mNativeInt;

    private static native void nativeDestructor(long j);

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            nativeDestructor(this.mNativeInt);
            this.mNativeInt = 0;
        } finally {
            super.finalize();
        }
    }
}
