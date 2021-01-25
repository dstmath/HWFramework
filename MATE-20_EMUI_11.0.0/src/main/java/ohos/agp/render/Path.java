package ohos.agp.render;

import ohos.agp.utils.Matrix;
import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.NativeMemoryCleanerHelper;
import ohos.agp.utils.Point;
import ohos.agp.utils.RectFloat;

public class Path {
    private static final int CORNER_RADIUS_SIZE = 8;
    static final FillType[] FILLTYPE_ARRAY = {FillType.WINDING_ORDER, FillType.EVEN_ODD, FillType.INVERSE_WINDING, FillType.INVERSE_EVEN_ODD};
    protected final long mNativePathHandle;

    private native void nativeAddArc(long j, RectFloat rectFloat, float f, float f2);

    private native void nativeAddCircle(long j, Point point, float f, int i);

    private native void nativeAddOval(long j, RectFloat rectFloat, int i);

    private native void nativeAddPath(long j, long j2);

    private native void nativeAddRect(long j, RectFloat rectFloat, int i);

    private native void nativeAddRoundRect(long j, RectFloat rectFloat, float[] fArr, int i, int i2);

    private native void nativeArcTo(long j, Point point, Point point2, float f);

    private native void nativeArcToWithRect(long j, float f, float f2, float f3, float f4, float f5, float f6, boolean z);

    private native void nativeClose(long j);

    private native void nativeComputeBounds(long j, RectFloat rectFloat);

    private native void nativeConicTo(long j, Point point, Point point2, float f);

    private native long nativeCopyPath(long j);

    private native void nativeCubicTo(long j, Point point, Point point2, Point point3);

    private native int nativeGetFillType(long j);

    private native long nativeGetPathHandle();

    private native boolean nativeIsEmpty(long j);

    private native void nativeLineTo(long j, float f, float f2);

    private native void nativeMoveTo(long j, float f, float f2);

    private native void nativeOffset(long j, float f, float f2);

    private native void nativeQuadTo(long j, Point point, Point point2);

    private native void nativeRLineTo(long j, float f, float f2);

    private native void nativeReset(long j);

    private native void nativeRewind(long j);

    private native void nativeSet(long j, long j2);

    private native void nativeSetFillType(long j, int i);

    private native void nativeTranformToNewPath(long j, long j2, long j3);

    private native void nativeTransform(long j, float[] fArr);

    public enum FillType {
        WINDING_ORDER(0),
        EVEN_ODD(1),
        INVERSE_WINDING(2),
        INVERSE_EVEN_ODD(3);
        
        final int enumInt;

        private FillType(int i) {
            this.enumInt = i;
        }

        public int value() {
            return this.enumInt;
        }
    }

    public enum Direction {
        CLOCK_WISE(0),
        COUNTER_CLOCK_WISE(1);
        
        final int enumInt;

        private Direction(int i) {
            this.enumInt = i;
        }

        public int value() {
            return this.enumInt;
        }
    }

    public Path() {
        this.mNativePathHandle = nativeGetPathHandle();
        MemoryCleanerRegistry.getInstance().register(this, new PathCleaner(this.mNativePathHandle));
    }

    public Path(Path path) {
        this.mNativePathHandle = nativeCopyPath(path.getNativeHandle());
        MemoryCleanerRegistry.getInstance().register(this, new PathCleaner(this.mNativePathHandle));
    }

    protected static class PathCleaner extends NativeMemoryCleanerHelper {
        private native void nativePathRelease(long j);

        public PathCleaner(long j) {
            super(j);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.utils.NativeMemoryCleanerHelper
        public void releaseNativeMemory(long j) {
            if (j != 0) {
                nativePathRelease(j);
            }
        }
    }

    public long getNativeHandle() {
        return this.mNativePathHandle;
    }

    public void moveTo(float f, float f2) {
        nativeMoveTo(this.mNativePathHandle, f, f2);
    }

    public void lineTo(float f, float f2) {
        nativeLineTo(this.mNativePathHandle, f, f2);
    }

    public void quadTo(Point point, Point point2) {
        nativeQuadTo(this.mNativePathHandle, point, point2);
    }

    public void cubicTo(Point point, Point point2, Point point3) {
        nativeCubicTo(this.mNativePathHandle, point, point2, point3);
    }

    public void arcTo(Point point, Point point2, float f) {
        nativeArcTo(this.mNativePathHandle, point, point2, f);
    }

    public void arcTo(RectFloat rectFloat, float f, float f2) {
        arcTo(rectFloat, f, f2, false);
    }

    public void arcTo(RectFloat rectFloat, float f, float f2, boolean z) {
        nativeArcToWithRect(this.mNativePathHandle, rectFloat.left, rectFloat.top, rectFloat.right, rectFloat.bottom, f, f2, z);
    }

    public void conicTo(Point point, Point point2, float f) {
        nativeConicTo(this.mNativePathHandle, point, point2, f);
    }

    public void close() {
        nativeClose(this.mNativePathHandle);
    }

    public void reset() {
        nativeReset(this.mNativePathHandle);
    }

    public void addPath(Path path) {
        nativeAddPath(this.mNativePathHandle, path.getNativeHandle());
    }

    public void addArc(RectFloat rectFloat, float f, float f2) {
        nativeAddArc(this.mNativePathHandle, rectFloat, f, f2);
    }

    public void addCircle(Point point, float f, Direction direction) {
        nativeAddCircle(this.mNativePathHandle, point, f, direction.value());
    }

    public void addOval(RectFloat rectFloat, Direction direction) {
        nativeAddOval(this.mNativePathHandle, rectFloat, direction.value());
    }

    public boolean isEmpty() {
        return nativeIsEmpty(this.mNativePathHandle);
    }

    public void computeBounds(RectFloat rectFloat) {
        nativeComputeBounds(this.mNativePathHandle, rectFloat);
    }

    public void addRect(RectFloat rectFloat, Direction direction) {
        nativeAddRect(this.mNativePathHandle, rectFloat, direction.value());
    }

    public void addRoundRect(RectFloat rectFloat, float[] fArr, Direction direction) {
        if (fArr.length == 8) {
            nativeAddRoundRect(this.mNativePathHandle, rectFloat, fArr, fArr.length, direction.value());
        }
    }

    public void rLineTo(float f, float f2) {
        nativeRLineTo(this.mNativePathHandle, f, f2);
    }

    public void offset(float f, float f2) {
        nativeOffset(this.mNativePathHandle, f, f2);
    }

    public void rewind() {
        nativeRewind(this.mNativePathHandle);
    }

    public void transform(Matrix matrix) {
        if (matrix != null) {
            nativeTransform(this.mNativePathHandle, matrix.getData());
        }
    }

    public void transformToNewPath(Matrix matrix, Path path) {
        if (matrix != null) {
            long j = 0;
            if (path != null) {
                j = path.getNativeHandle();
            }
            nativeTranformToNewPath(this.mNativePathHandle, matrix.getNativeHandle(), j);
        }
    }

    public void set(Path path) {
        nativeSet(this.mNativePathHandle, path.getNativeHandle());
    }

    public void setFillType(FillType fillType) {
        nativeSetFillType(this.mNativePathHandle, fillType.value());
    }

    public FillType getFillType() {
        int nativeGetFillType = nativeGetFillType(this.mNativePathHandle);
        if (nativeGetFillType >= 0) {
            FillType[] fillTypeArr = FILLTYPE_ARRAY;
            if (nativeGetFillType < fillTypeArr.length) {
                return fillTypeArr[nativeGetFillType];
            }
        }
        return FILLTYPE_ARRAY[0];
    }
}
