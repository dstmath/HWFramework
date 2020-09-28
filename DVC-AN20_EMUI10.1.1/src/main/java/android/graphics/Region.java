package android.graphics;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pools;

public class Region implements Parcelable {
    public static final Parcelable.Creator<Region> CREATOR = new Parcelable.Creator<Region>() {
        /* class android.graphics.Region.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Region createFromParcel(Parcel p) {
            long ni = Region.nativeCreateFromParcel(p);
            if (ni != 0) {
                return new Region(ni);
            }
            throw new RuntimeException();
        }

        @Override // android.os.Parcelable.Creator
        public Region[] newArray(int size) {
            return new Region[size];
        }
    };
    private static final int MAX_POOL_SIZE = 10;
    private static final Pools.SynchronizedPool<Region> sPool = new Pools.SynchronizedPool<>(10);
    @UnsupportedAppUsage
    public long mNativeRegion;

    private static native long nativeConstructor();

    /* access modifiers changed from: private */
    public static native long nativeCreateFromParcel(Parcel parcel);

    private static native void nativeDestructor(long j);

    private static native boolean nativeEquals(long j, long j2);

    private static native boolean nativeGetBoundaryPath(long j, long j2);

    private static native boolean nativeGetBounds(long j, Rect rect);

    private static native boolean nativeOp(long j, int i, int i2, int i3, int i4, int i5);

    private static native boolean nativeOp(long j, long j2, long j3, int i);

    private static native boolean nativeOp(long j, Rect rect, long j2, int i);

    private static native boolean nativeSetPath(long j, long j2, long j3);

    private static native boolean nativeSetRect(long j, int i, int i2, int i3, int i4);

    private static native void nativeSetRegion(long j, long j2);

    private static native String nativeToString(long j);

    private static native boolean nativeWriteToParcel(long j, Parcel parcel);

    public native boolean contains(int i, int i2);

    public native boolean isComplex();

    public native boolean isEmpty();

    public native boolean isRect();

    public native boolean quickContains(int i, int i2, int i3, int i4);

    public native boolean quickReject(int i, int i2, int i3, int i4);

    public native boolean quickReject(Region region);

    public native void scale(float f, Region region);

    public native void translate(int i, int i2, Region region);

    public enum Op {
        DIFFERENCE(0),
        INTERSECT(1),
        UNION(2),
        XOR(3),
        REVERSE_DIFFERENCE(4),
        REPLACE(5);
        
        @UnsupportedAppUsage
        public final int nativeInt;

        private Op(int nativeInt2) {
            this.nativeInt = nativeInt2;
        }
    }

    public Region() {
        this(nativeConstructor());
    }

    public Region(Region region) {
        this(nativeConstructor());
        nativeSetRegion(this.mNativeRegion, region.mNativeRegion);
    }

    public Region(Rect r) {
        this.mNativeRegion = nativeConstructor();
        nativeSetRect(this.mNativeRegion, r.left, r.top, r.right, r.bottom);
    }

    public Region(int left, int top, int right, int bottom) {
        this.mNativeRegion = nativeConstructor();
        nativeSetRect(this.mNativeRegion, left, top, right, bottom);
    }

    public void setEmpty() {
        nativeSetRect(this.mNativeRegion, 0, 0, 0, 0);
    }

    public boolean set(Region region) {
        nativeSetRegion(this.mNativeRegion, region.mNativeRegion);
        return true;
    }

    public boolean set(Rect r) {
        return nativeSetRect(this.mNativeRegion, r.left, r.top, r.right, r.bottom);
    }

    public boolean set(int left, int top, int right, int bottom) {
        return nativeSetRect(this.mNativeRegion, left, top, right, bottom);
    }

    public boolean setPath(Path path, Region clip) {
        return nativeSetPath(this.mNativeRegion, path.readOnlyNI(), clip.mNativeRegion);
    }

    public Rect getBounds() {
        Rect r = new Rect();
        nativeGetBounds(this.mNativeRegion, r);
        return r;
    }

    public boolean getBounds(Rect r) {
        if (r != null) {
            return nativeGetBounds(this.mNativeRegion, r);
        }
        throw new NullPointerException();
    }

    public Path getBoundaryPath() {
        Path path = new Path();
        nativeGetBoundaryPath(this.mNativeRegion, path.mutateNI());
        return path;
    }

    public boolean getBoundaryPath(Path path) {
        return nativeGetBoundaryPath(this.mNativeRegion, path.mutateNI());
    }

    public boolean quickContains(Rect r) {
        return quickContains(r.left, r.top, r.right, r.bottom);
    }

    public boolean quickReject(Rect r) {
        return quickReject(r.left, r.top, r.right, r.bottom);
    }

    public void translate(int dx, int dy) {
        translate(dx, dy, null);
    }

    @UnsupportedAppUsage
    public void scale(float scale) {
        scale(scale, null);
    }

    public final boolean union(Rect r) {
        return op(r, Op.UNION);
    }

    public boolean op(Rect r, Op op) {
        return nativeOp(this.mNativeRegion, r.left, r.top, r.right, r.bottom, op.nativeInt);
    }

    public boolean op(int left, int top, int right, int bottom, Op op) {
        return nativeOp(this.mNativeRegion, left, top, right, bottom, op.nativeInt);
    }

    public boolean op(Region region, Op op) {
        return op(this, region, op);
    }

    public boolean op(Rect rect, Region region, Op op) {
        return nativeOp(this.mNativeRegion, rect, region.mNativeRegion, op.nativeInt);
    }

    public boolean op(Region region1, Region region2, Op op) {
        return nativeOp(this.mNativeRegion, region1.mNativeRegion, region2.mNativeRegion, op.nativeInt);
    }

    public String toString() {
        return nativeToString(this.mNativeRegion);
    }

    public static Region obtain() {
        Region region = sPool.acquire();
        return region != null ? region : new Region();
    }

    public static Region obtain(Region other) {
        Region region = obtain();
        region.set(other);
        return region;
    }

    @UnsupportedAppUsage
    public void recycle() {
        setEmpty();
        sPool.release(this);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel p, int flags) {
        if (!nativeWriteToParcel(this.mNativeRegion, p)) {
            throw new RuntimeException();
        }
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Region)) {
            return false;
        }
        return nativeEquals(this.mNativeRegion, ((Region) obj).mNativeRegion);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            nativeDestructor(this.mNativeRegion);
            this.mNativeRegion = 0;
        } finally {
            super.finalize();
        }
    }

    Region(long ni) {
        if (ni != 0) {
            this.mNativeRegion = ni;
            return;
        }
        throw new RuntimeException();
    }

    @UnsupportedAppUsage
    private Region(long ni, int dummy) {
        this(ni);
    }

    /* access modifiers changed from: package-private */
    public final long ni() {
        return this.mNativeRegion;
    }
}
