package android.graphics;

public class RegionIterator {
    private long mNativeIter;

    private static native long nativeConstructor(long j);

    private static native void nativeDestructor(long j);

    private static native boolean nativeNext(long j, Rect rect);

    public RegionIterator(Region region) {
        this.mNativeIter = nativeConstructor(region.ni());
    }

    public final boolean next(Rect r) {
        if (r != null) {
            return nativeNext(this.mNativeIter, r);
        }
        throw new NullPointerException("The Rect must be provided");
    }

    protected void finalize() throws Throwable {
        nativeDestructor(this.mNativeIter);
        this.mNativeIter = 0;
    }
}
