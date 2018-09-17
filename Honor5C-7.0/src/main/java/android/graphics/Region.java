package android.graphics;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Pools.SynchronizedPool;

public class Region implements Parcelable {
    public static final Creator<Region> CREATOR = null;
    private static final int MAX_POOL_SIZE = 10;
    private static final SynchronizedPool<Region> sPool = null;
    public long mNativeRegion;

    public enum Op {
        ;
        
        public final int nativeInt;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.graphics.Region.Op.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.graphics.Region.Op.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.graphics.Region.Op.<clinit>():void");
        }

        private Op(int nativeInt) {
            this.nativeInt = nativeInt;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.graphics.Region.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.graphics.Region.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.graphics.Region.<clinit>():void");
    }

    private static native long nativeConstructor();

    private static native long nativeCreateFromParcel(Parcel parcel);

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
        return nativeSetPath(this.mNativeRegion, path.ni(), clip.mNativeRegion);
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
        nativeGetBoundaryPath(this.mNativeRegion, path.ni());
        return path;
    }

    public boolean getBoundaryPath(Path path) {
        return nativeGetBoundaryPath(this.mNativeRegion, path.ni());
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
        Region region = (Region) sPool.acquire();
        return region != null ? region : new Region();
    }

    public static Region obtain(Region other) {
        Region region = obtain();
        region.set(other);
        return region;
    }

    public void recycle() {
        setEmpty();
        sPool.release(this);
    }

    public int describeContents() {
        return 0;
    }

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

    protected void finalize() throws Throwable {
        try {
            nativeDestructor(this.mNativeRegion);
            this.mNativeRegion = 0;
        } finally {
            super.finalize();
        }
    }

    Region(long ni) {
        if (ni == 0) {
            throw new RuntimeException();
        }
        this.mNativeRegion = ni;
    }

    private Region(long ni, int dummy) {
        this(ni);
    }

    final long ni() {
        return this.mNativeRegion;
    }
}
