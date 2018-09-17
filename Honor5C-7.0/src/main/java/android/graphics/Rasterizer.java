package android.graphics;

@Deprecated
public class Rasterizer {
    long native_instance;

    private static native void finalizer(long j);

    protected void finalize() throws Throwable {
        finalizer(this.native_instance);
        this.native_instance = 0;
    }
}
