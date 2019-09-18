package android.graphics;

public class MaskFilter {
    long native_instance;

    private static native void nativeDestructor(long j);

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        nativeDestructor(this.native_instance);
        this.native_instance = 0;
    }
}
