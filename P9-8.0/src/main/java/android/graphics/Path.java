package android.graphics;

import android.hardware.camera2.params.TonemapCurve;

public class Path {
    static final FillType[] sFillTypeArray = new FillType[]{FillType.WINDING, FillType.EVEN_ODD, FillType.INVERSE_WINDING, FillType.INVERSE_EVEN_ODD};
    public boolean isSimplePath;
    private Direction mLastDirection;
    public long mNativePath;
    public Region rects;

    public enum Direction {
        CW(0),
        CCW(1);
        
        final int nativeInt;

        private Direction(int ni) {
            this.nativeInt = ni;
        }
    }

    public enum FillType {
        WINDING(0),
        EVEN_ODD(1),
        INVERSE_WINDING(2),
        INVERSE_EVEN_ODD(3);
        
        final int nativeInt;

        private FillType(int ni) {
            this.nativeInt = ni;
        }
    }

    public enum Op {
        DIFFERENCE,
        INTERSECT,
        UNION,
        XOR,
        REVERSE_DIFFERENCE
    }

    private static native void nAddArc(long j, float f, float f2, float f3, float f4, float f5, float f6);

    private static native void nAddCircle(long j, float f, float f2, float f3, int i);

    private static native void nAddOval(long j, float f, float f2, float f3, float f4, int i);

    private static native void nAddPath(long j, long j2);

    private static native void nAddPath(long j, long j2, float f, float f2);

    private static native void nAddPath(long j, long j2, long j3);

    private static native void nAddRect(long j, float f, float f2, float f3, float f4, int i);

    private static native void nAddRoundRect(long j, float f, float f2, float f3, float f4, float f5, float f6, int i);

    private static native void nAddRoundRect(long j, float f, float f2, float f3, float f4, float[] fArr, int i);

    private static native float[] nApproximate(long j, float f);

    private static native void nArcTo(long j, float f, float f2, float f3, float f4, float f5, float f6, boolean z);

    private static native void nClose(long j);

    private static native void nComputeBounds(long j, RectF rectF);

    private static native void nCubicTo(long j, float f, float f2, float f3, float f4, float f5, float f6);

    private static native void nFinalize(long j);

    private static native int nGetFillType(long j);

    private static native void nIncReserve(long j, int i);

    private static native long nInit();

    private static native long nInit(long j);

    private static native boolean nIsConvex(long j);

    private static native boolean nIsEmpty(long j);

    private static native boolean nIsRect(long j, RectF rectF);

    private static native void nLineTo(long j, float f, float f2);

    private static native void nMoveTo(long j, float f, float f2);

    private static native void nOffset(long j, float f, float f2);

    private static native boolean nOp(long j, long j2, int i, long j3);

    private static native void nQuadTo(long j, float f, float f2, float f3, float f4);

    private static native void nRCubicTo(long j, float f, float f2, float f3, float f4, float f5, float f6);

    private static native void nRLineTo(long j, float f, float f2);

    private static native void nRMoveTo(long j, float f, float f2);

    private static native void nRQuadTo(long j, float f, float f2, float f3, float f4);

    private static native void nReset(long j);

    private static native void nRewind(long j);

    private static native void nSet(long j, long j2);

    private static native void nSetFillType(long j, int i);

    private static native void nSetLastPoint(long j, float f, float f2);

    private static native void nTransform(long j, long j2);

    private static native void nTransform(long j, long j2, long j3);

    public Path() {
        this.isSimplePath = true;
        this.mLastDirection = null;
        this.mNativePath = nInit();
    }

    public Path(Path src) {
        this.isSimplePath = true;
        this.mLastDirection = null;
        long valNative = 0;
        if (src != null) {
            valNative = src.mNativePath;
            this.isSimplePath = src.isSimplePath;
            if (src.rects != null) {
                this.rects = new Region(src.rects);
            }
        }
        this.mNativePath = nInit(valNative);
    }

    public void reset() {
        this.isSimplePath = true;
        this.mLastDirection = null;
        if (this.rects != null) {
            this.rects.setEmpty();
        }
        FillType fillType = getFillType();
        nReset(this.mNativePath);
        setFillType(fillType);
    }

    public void rewind() {
        this.isSimplePath = true;
        this.mLastDirection = null;
        if (this.rects != null) {
            this.rects.setEmpty();
        }
        nRewind(this.mNativePath);
    }

    public void set(Path src) {
        if (this != src) {
            this.isSimplePath = src.isSimplePath;
            nSet(this.mNativePath, src.mNativePath);
            if (this.isSimplePath) {
                if (this.rects != null && src.rects != null) {
                    this.rects.set(src.rects);
                } else if (this.rects != null && src.rects == null) {
                    this.rects.setEmpty();
                } else if (src.rects != null) {
                    this.rects = new Region(src.rects);
                }
            }
        }
    }

    public boolean op(Path path, Op op) {
        return op(this, path, op);
    }

    public boolean op(Path path1, Path path2, Op op) {
        if (!nOp(path1.mNativePath, path2.mNativePath, op.ordinal(), this.mNativePath)) {
            return false;
        }
        this.isSimplePath = false;
        this.rects = null;
        return true;
    }

    public boolean isConvex() {
        return nIsConvex(this.mNativePath);
    }

    public FillType getFillType() {
        return sFillTypeArray[nGetFillType(this.mNativePath)];
    }

    public void setFillType(FillType ft) {
        nSetFillType(this.mNativePath, ft.nativeInt);
    }

    public boolean isInverseFillType() {
        if ((FillType.INVERSE_WINDING.nativeInt & nGetFillType(this.mNativePath)) != 0) {
            return true;
        }
        return false;
    }

    public void toggleInverseFillType() {
        nSetFillType(this.mNativePath, nGetFillType(this.mNativePath) ^ FillType.INVERSE_WINDING.nativeInt);
    }

    public boolean isEmpty() {
        return nIsEmpty(this.mNativePath);
    }

    public boolean isRect(RectF rect) {
        return nIsRect(this.mNativePath, rect);
    }

    public void computeBounds(RectF bounds, boolean exact) {
        nComputeBounds(this.mNativePath, bounds);
    }

    public void incReserve(int extraPtCount) {
        nIncReserve(this.mNativePath, extraPtCount);
    }

    public void moveTo(float x, float y) {
        nMoveTo(this.mNativePath, x, y);
    }

    public void rMoveTo(float dx, float dy) {
        nRMoveTo(this.mNativePath, dx, dy);
    }

    public void lineTo(float x, float y) {
        this.isSimplePath = false;
        nLineTo(this.mNativePath, x, y);
    }

    public void rLineTo(float dx, float dy) {
        this.isSimplePath = false;
        nRLineTo(this.mNativePath, dx, dy);
    }

    public void quadTo(float x1, float y1, float x2, float y2) {
        this.isSimplePath = false;
        nQuadTo(this.mNativePath, x1, y1, x2, y2);
    }

    public void rQuadTo(float dx1, float dy1, float dx2, float dy2) {
        this.isSimplePath = false;
        nRQuadTo(this.mNativePath, dx1, dy1, dx2, dy2);
    }

    public void cubicTo(float x1, float y1, float x2, float y2, float x3, float y3) {
        this.isSimplePath = false;
        nCubicTo(this.mNativePath, x1, y1, x2, y2, x3, y3);
    }

    public void rCubicTo(float x1, float y1, float x2, float y2, float x3, float y3) {
        this.isSimplePath = false;
        nRCubicTo(this.mNativePath, x1, y1, x2, y2, x3, y3);
    }

    public void arcTo(RectF oval, float startAngle, float sweepAngle, boolean forceMoveTo) {
        arcTo(oval.left, oval.top, oval.right, oval.bottom, startAngle, sweepAngle, forceMoveTo);
    }

    public void arcTo(RectF oval, float startAngle, float sweepAngle) {
        arcTo(oval.left, oval.top, oval.right, oval.bottom, startAngle, sweepAngle, false);
    }

    public void arcTo(float left, float top, float right, float bottom, float startAngle, float sweepAngle, boolean forceMoveTo) {
        this.isSimplePath = false;
        nArcTo(this.mNativePath, left, top, right, bottom, startAngle, sweepAngle, forceMoveTo);
    }

    public void close() {
        this.isSimplePath = false;
        nClose(this.mNativePath);
    }

    private void detectSimplePath(float left, float top, float right, float bottom, Direction dir) {
        if (this.mLastDirection == null) {
            this.mLastDirection = dir;
        }
        if (this.mLastDirection != dir) {
            this.isSimplePath = false;
            return;
        }
        if (this.rects == null) {
            this.rects = new Region();
        }
        this.rects.op((int) left, (int) top, (int) right, (int) bottom, android.graphics.Region.Op.UNION);
    }

    public void addRect(RectF rect, Direction dir) {
        addRect(rect.left, rect.top, rect.right, rect.bottom, dir);
    }

    public void addRect(float left, float top, float right, float bottom, Direction dir) {
        detectSimplePath(left, top, right, bottom, dir);
        nAddRect(this.mNativePath, left, top, right, bottom, dir.nativeInt);
    }

    public void addOval(RectF oval, Direction dir) {
        addOval(oval.left, oval.top, oval.right, oval.bottom, dir);
    }

    public void addOval(float left, float top, float right, float bottom, Direction dir) {
        this.isSimplePath = false;
        nAddOval(this.mNativePath, left, top, right, bottom, dir.nativeInt);
    }

    public void addCircle(float x, float y, float radius, Direction dir) {
        this.isSimplePath = false;
        nAddCircle(this.mNativePath, x, y, radius, dir.nativeInt);
    }

    public void addArc(RectF oval, float startAngle, float sweepAngle) {
        addArc(oval.left, oval.top, oval.right, oval.bottom, startAngle, sweepAngle);
    }

    public void addArc(float left, float top, float right, float bottom, float startAngle, float sweepAngle) {
        this.isSimplePath = false;
        nAddArc(this.mNativePath, left, top, right, bottom, startAngle, sweepAngle);
    }

    public void addRoundRect(RectF rect, float rx, float ry, Direction dir) {
        addRoundRect(rect.left, rect.top, rect.right, rect.bottom, rx, ry, dir);
    }

    public void addRoundRect(float left, float top, float right, float bottom, float rx, float ry, Direction dir) {
        this.isSimplePath = false;
        nAddRoundRect(this.mNativePath, left, top, right, bottom, rx, ry, dir.nativeInt);
    }

    public void addRoundRect(RectF rect, float[] radii, Direction dir) {
        if (rect == null) {
            throw new NullPointerException("need rect parameter");
        }
        addRoundRect(rect.left, rect.top, rect.right, rect.bottom, radii, dir);
    }

    public void addRoundRect(float left, float top, float right, float bottom, float[] radii, Direction dir) {
        if (radii.length < 8) {
            throw new ArrayIndexOutOfBoundsException("radii[] needs 8 values");
        }
        this.isSimplePath = false;
        nAddRoundRect(this.mNativePath, left, top, right, bottom, radii, dir.nativeInt);
    }

    public void addPath(Path src, float dx, float dy) {
        this.isSimplePath = false;
        nAddPath(this.mNativePath, src.mNativePath, dx, dy);
    }

    public void addPath(Path src) {
        this.isSimplePath = false;
        nAddPath(this.mNativePath, src.mNativePath);
    }

    public void addPath(Path src, Matrix matrix) {
        if (!src.isSimplePath) {
            this.isSimplePath = false;
        }
        nAddPath(this.mNativePath, src.mNativePath, matrix.native_instance);
    }

    public void offset(float dx, float dy, Path dst) {
        if (dst != null) {
            dst.set(this);
        } else {
            dst = this;
        }
        dst.offset(dx, dy);
    }

    public void offset(float dx, float dy) {
        if (!this.isSimplePath || this.rects != null) {
            if (this.isSimplePath && ((double) dx) == Math.rint((double) dx) && ((double) dy) == Math.rint((double) dy)) {
                this.rects.translate((int) dx, (int) dy);
            } else {
                this.isSimplePath = false;
            }
            nOffset(this.mNativePath, dx, dy);
        }
    }

    public void setLastPoint(float dx, float dy) {
        this.isSimplePath = false;
        nSetLastPoint(this.mNativePath, dx, dy);
    }

    public void transform(Matrix matrix, Path dst) {
        long dstNative = 0;
        if (dst != null) {
            dst.isSimplePath = false;
            dstNative = dst.mNativePath;
        }
        nTransform(this.mNativePath, matrix.native_instance, dstNative);
    }

    public void transform(Matrix matrix) {
        this.isSimplePath = false;
        nTransform(this.mNativePath, matrix.native_instance);
    }

    protected void finalize() throws Throwable {
        try {
            nFinalize(this.mNativePath);
            this.mNativePath = 0;
        } finally {
            super.finalize();
        }
    }

    public final long readOnlyNI() {
        return this.mNativePath;
    }

    final long mutateNI() {
        this.isSimplePath = false;
        return this.mNativePath;
    }

    public float[] approximate(float acceptableError) {
        if (acceptableError >= TonemapCurve.LEVEL_BLACK) {
            return nApproximate(this.mNativePath, acceptableError);
        }
        throw new IllegalArgumentException("AcceptableError must be greater than or equal to 0");
    }
}
