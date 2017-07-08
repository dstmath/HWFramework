package android.graphics;

public class ColorFilter {
    public long native_instance;

    static native void destroyFilter(long j);

    protected void finalize() throws Throwable {
        try {
            super.finalize();
        } finally {
            destroyFilter(this.native_instance);
            this.native_instance = 0;
        }
    }
}
