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

    private native void nativeAddOvalWithStart(long j, RectFloat rectFloat, int i, int i2);

    private native void nativeAddPath(long j, long j2);

    private native void nativeAddPathWithMatrix(long j, long j2, long j3, int i);

    private native void nativeAddPathWithMode(long j, long j2, int i);

    private native void nativeAddPathWithOffset(long j, long j2, float f, float f2, int i);

    private native void nativeAddRect(long j, RectFloat rectFloat, int i);

    private native void nativeAddRectWithStart(long j, RectFloat rectFloat, int i, int i2);

    private native void nativeAddRoundRect(long j, RectFloat rectFloat, float[] fArr, int i, int i2);

    private native void nativeAddRoundRectWithRadius(long j, RectFloat rectFloat, float f, float f2, int i);

    private native void nativeArcTo(long j, Point point, Point point2, float f);

    private native void nativeArcToWithArcSize(long j, Point point, float f, int i, int i2, Point point2);

    private native void nativeArcToWithRect(long j, float f, float f2, float f3, float f4, float f5, float f6, boolean z);

    private native void nativeClose(long j);

    private native void nativeComputeBounds(long j, RectFloat rectFloat);

    private native void nativeComputeTightBounds(long j, RectFloat rectFloat);

    private native void nativeConicTo(long j, Point point, Point point2, float f);

    private native boolean nativeConservativelyContainsRect(long j, RectFloat rectFloat);

    private native boolean nativeContains(long j, float f, float f2);

    private native long nativeCopyPath(long j);

    private native int nativeCountPoints(long j);

    private native int nativeCountVerbs(long j);

    private native void nativeCubicTo(long j, Point point, Point point2, Point point3);

    private native int nativeGetFillType(long j);

    private native boolean nativeGetLastPoint(long j, Point point);

    private native long nativeGetPathHandle();

    private native void nativeIncReserve(long j, int i);

    private native boolean nativeInterpolate(long j, long j2, float f, long j3);

    private native boolean nativeIsConvex(long j);

    private native boolean nativeIsEmpty(long j);

    private native boolean nativeIsFinite(long j);

    private native boolean nativeIsInterpolatable(long j, long j2);

    private native boolean nativeIsInverseFillType(long j);

    private native boolean nativeIsLastContourClosed(long j);

    private native boolean nativeIsOval(long j, RectFloat rectFloat);

    private native boolean nativeIsValid(long j);

    private native boolean nativeIsVolatile(long j);

    private native void nativeLineTo(long j, float f, float f2);

    private native void nativeMoveTo(long j, float f, float f2);

    private native void nativeOffset(long j, float f, float f2);

    private native void nativeOffsetWithDst(long j, float f, float f2, long j2);

    private native void nativeQuadTo(long j, Point point, Point point2);

    private native void nativeRConicTo(long j, float f, float f2, float f3, float f4, float f5);

    private native void nativeRLineTo(long j, float f, float f2);

    private native void nativeRMoveTo(long j, float f, float f2);

    private native void nativeRQuadTo(long j, float f, float f2, float f3, float f4);

    private native void nativeReset(long j);

    private native void nativeReverseAddPath(long j, long j2);

    private native void nativeRewind(long j);

    private native void nativeSet(long j, long j2);

    private native void nativeSetFillType(long j, int i);

    private native void nativeSetIsVolatile(long j, boolean z);

    private native void nativeSetLastPoint(long j, float f, float f2);

    private native void nativeShrinkToFit(long j);

    private native void nativeSwap(long j, long j2);

    private native void nativeToggleInverseFillType(long j);

    private native void nativeTranformToNewPath(long j, long j2, long j3);

    private native void nativeTransform(long j, float[] fArr);

    private native void nativeUpdateBoundsCache(long j);

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

    public enum AddPathMode {
        APPEND_ADD_PATH_MODE(0),
        EXTEND_ADD_PATH_MODE(1);
        
        final int enumInt;

        private AddPathMode(int i) {
            this.enumInt = i;
        }

        public int value() {
            return this.enumInt;
        }
    }

    public enum ArcSize {
        SMALL_SIZE(0),
        LARGE_SIZE(1);
        
        final int enumInt;

        private ArcSize(int i) {
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

    public void setLastPoint(float f, float f2) {
        nativeSetLastPoint(this.mNativePathHandle, f, f2);
    }

    public boolean isConvex() {
        return nativeIsConvex(this.mNativePathHandle);
    }

    public boolean isInterpolatable(Path path) {
        if (path == null) {
            return false;
        }
        return nativeIsInterpolatable(this.mNativePathHandle, path.getNativeHandle());
    }

    public boolean interpolate(Path path, float f, Path path2) {
        if (path == null || path2 == null) {
            return false;
        }
        return nativeInterpolate(this.mNativePathHandle, path.getNativeHandle(), f, path2.getNativeHandle());
    }

    public boolean isInverseFillType() {
        return nativeIsInverseFillType(this.mNativePathHandle);
    }

    public void toggleInverseFillType() {
        nativeToggleInverseFillType(this.mNativePathHandle);
    }

    public boolean isOval(RectFloat rectFloat) {
        if (rectFloat == null) {
            return false;
        }
        return nativeIsOval(this.mNativePathHandle, rectFloat);
    }

    public boolean isLastContourClosed() {
        return nativeIsLastContourClosed(this.mNativePathHandle);
    }

    public boolean isFinite() {
        return nativeIsFinite(this.mNativePathHandle);
    }

    public boolean isVolatile() {
        return nativeIsVolatile(this.mNativePathHandle);
    }

    public void setIsVolatile(boolean z) {
        nativeSetIsVolatile(this.mNativePathHandle, z);
    }

    public int countPoints() {
        return nativeCountPoints(this.mNativePathHandle);
    }

    public void swap(Path path) {
        if (path != null) {
            nativeSwap(this.mNativePathHandle, path.getNativeHandle());
        }
    }

    public RectFloat computeTightBounds() {
        RectFloat rectFloat = new RectFloat();
        nativeComputeTightBounds(this.mNativePathHandle, rectFloat);
        return rectFloat;
    }

    public boolean conservativelyContainsRect(RectFloat rectFloat) {
        if (rectFloat == null) {
            return false;
        }
        return nativeConservativelyContainsRect(this.mNativePathHandle, rectFloat);
    }

    public void incReserve(int i) {
        nativeIncReserve(this.mNativePathHandle, i);
    }

    public void shrinkToFit() {
        nativeShrinkToFit(this.mNativePathHandle);
    }

    public void moveTo(Point point) {
        if (point != null) {
            moveTo(point.position[0], point.position[1]);
        }
    }

    public void rMoveTo(float f, float f2) {
        nativeRMoveTo(this.mNativePathHandle, f, f2);
    }

    public void lineTo(Point point) {
        if (point != null) {
            lineTo(point.position[0], point.position[1]);
        }
    }

    public void quadTo(float f, float f2, float f3, float f4) {
        quadTo(new Point(f, f2), new Point(f3, f4));
    }

    public void rQuadTo(float f, float f2, float f3, float f4) {
        nativeRQuadTo(this.mNativePathHandle, f, f2, f3, f4);
    }

    public void conicTo(float f, float f2, float f3, float f4, float f5) {
        conicTo(new Point(f, f2), new Point(f3, f4), f5);
    }

    public void rConicTo(float f, float f2, float f3, float f4, float f5) {
        nativeRConicTo(this.mNativePathHandle, f, f2, f3, f4, f5);
    }

    public void arcTo(float f, float f2, float f3, float f4, float f5) {
        arcTo(new Point(f, f2), new Point(f3, f4), f5);
    }

    public void arcTo(Point point, float f, ArcSize arcSize, Direction direction, Point point2) {
        if (point != null && arcSize != null && direction != null && point2 != null) {
            nativeArcToWithArcSize(this.mNativePathHandle, point, f, arcSize.value(), direction.value(), point2);
        }
    }

    public void addRect(RectFloat rectFloat, Direction direction, int i) {
        if (rectFloat != null && direction != null) {
            nativeAddRectWithStart(this.mNativePathHandle, rectFloat, direction.value(), i);
        }
    }

    public void addRect(float f, float f2, float f3, float f4, Direction direction) {
        addRect(new RectFloat(f, f2, f3, f4), direction);
    }

    public void addOval(RectFloat rectFloat, Direction direction, int i) {
        if (rectFloat != null && direction != null) {
            nativeAddOvalWithStart(this.mNativePathHandle, rectFloat, direction.value(), i);
        }
    }

    public void addCircle(float f, float f2, float f3, Direction direction) {
        addCircle(new Point(f, f2), f3, direction);
    }

    public void addRoundRect(RectFloat rectFloat, float f, float f2, Direction direction) {
        if (rectFloat != null && direction != null) {
            nativeAddRoundRectWithRadius(this.mNativePathHandle, rectFloat, f, f2, direction.value());
        }
    }

    public void addPath(Path path, AddPathMode addPathMode) {
        if (path != null) {
            if (addPathMode == null) {
                addPathMode = AddPathMode.APPEND_ADD_PATH_MODE;
            }
            nativeAddPathWithMode(this.mNativePathHandle, path.getNativeHandle(), addPathMode.value());
        }
    }

    public void addPath(Path path, float f, float f2, AddPathMode addPathMode) {
        if (path != null) {
            if (addPathMode == null) {
                addPathMode = AddPathMode.APPEND_ADD_PATH_MODE;
            }
            nativeAddPathWithOffset(this.mNativePathHandle, path.getNativeHandle(), f, f2, addPathMode.value());
        }
    }

    public void addPath(Path path, Matrix matrix, AddPathMode addPathMode) {
        if (path != null && matrix != null) {
            if (addPathMode == null) {
                addPathMode = AddPathMode.APPEND_ADD_PATH_MODE;
            }
            nativeAddPathWithMatrix(this.mNativePathHandle, path.getNativeHandle(), matrix.getNativeHandle(), addPathMode.value());
        }
    }

    public void reverseAddPath(Path path) {
        if (path != null) {
            nativeReverseAddPath(this.mNativePathHandle, path.getNativeHandle());
        }
    }

    public void offset(float f, float f2, Path path) {
        if (path != null) {
            nativeOffsetWithDst(this.mNativePathHandle, f, f2, path.getNativeHandle());
        }
    }

    public boolean getLastPoint(Point point) {
        if (point == null) {
            return false;
        }
        return nativeGetLastPoint(this.mNativePathHandle, point);
    }

    public void setLastPoint(Point point) {
        if (point != null) {
            nativeSetLastPoint(this.mNativePathHandle, point.position[0], point.position[1]);
        }
    }

    public boolean contains(float f, float f2) {
        return nativeContains(this.mNativePathHandle, f, f2);
    }

    public boolean isValid() {
        return nativeIsValid(this.mNativePathHandle);
    }

    public int countVerbs() {
        return nativeCountVerbs(this.mNativePathHandle);
    }

    public void updateBoundsCache() {
        nativeUpdateBoundsCache(this.mNativePathHandle);
    }
}
