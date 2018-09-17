package android.graphics;

import android.hardware.camera2.params.TonemapCurve;

public class PathMeasure {
    public static final int POSITION_MATRIX_FLAG = 1;
    public static final int TANGENT_MATRIX_FLAG = 2;
    private Path mPath;
    private long native_instance;

    private static native long native_create(long j, boolean z);

    private static native void native_destroy(long j);

    private static native float native_getLength(long j);

    private static native boolean native_getMatrix(long j, float f, long j2, int i);

    private static native boolean native_getPosTan(long j, float f, float[] fArr, float[] fArr2);

    private static native boolean native_getSegment(long j, float f, float f2, long j2, boolean z);

    private static native boolean native_isClosed(long j);

    private static native boolean native_nextContour(long j);

    private static native void native_setPath(long j, long j2, boolean z);

    public PathMeasure() {
        this.mPath = null;
        this.native_instance = native_create(0, false);
    }

    public PathMeasure(Path path, boolean forceClosed) {
        this.mPath = path;
        this.native_instance = native_create(path != null ? path.readOnlyNI() : 0, forceClosed);
    }

    public void setPath(Path path, boolean forceClosed) {
        this.mPath = path;
        native_setPath(this.native_instance, path != null ? path.readOnlyNI() : 0, forceClosed);
    }

    public float getLength() {
        return native_getLength(this.native_instance);
    }

    public boolean getPosTan(float distance, float[] pos, float[] tan) {
        if ((pos == null || pos.length >= 2) && (tan == null || tan.length >= 2)) {
            return native_getPosTan(this.native_instance, distance, pos, tan);
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public boolean getMatrix(float distance, Matrix matrix, int flags) {
        return native_getMatrix(this.native_instance, distance, matrix.native_instance, flags);
    }

    public boolean getSegment(float startD, float stopD, Path dst, boolean startWithMoveTo) {
        float length = getLength();
        if (startD < TonemapCurve.LEVEL_BLACK) {
            startD = TonemapCurve.LEVEL_BLACK;
        }
        if (stopD > length) {
            stopD = length;
        }
        if (startD >= stopD) {
            return false;
        }
        return native_getSegment(this.native_instance, startD, stopD, dst.mutateNI(), startWithMoveTo);
    }

    public boolean isClosed() {
        return native_isClosed(this.native_instance);
    }

    public boolean nextContour() {
        return native_nextContour(this.native_instance);
    }

    protected void finalize() throws Throwable {
        native_destroy(this.native_instance);
        this.native_instance = 0;
    }
}
