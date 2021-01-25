package ohos.agp.utils;

public class Matrix {
    private static final float DEGREES_TO_RADIAN = 0.017453292f;
    private static final float IDENTITY = 1.0f;
    private static final int MATRIX_SIZE = 9;
    private static final int MAX_COLUMN = 3;
    private static final int MAX_ROW = 3;
    private static final int PERSP_0 = 6;
    private static final int PERSP_1 = 7;
    private static final int PERSP_2 = 8;
    private static final int SCALE_X = 0;
    private static final int SCALE_Y = 3;
    private static final int SKEW_X = 1;
    private static final int SKEW_Y = 4;
    private static final int TRANS_X = 2;
    private static final int TRANS_Y = 5;
    private long mNativeMatrixHandle;

    private native void nativeGetMatrixData(long j, float[] fArr);

    private native long nativeGetMatrixHandle();

    private native long nativeGetMatrixHandleWithArray(float[] fArr);

    private native long nativeGetMatrixHandleWithMatrix(long j);

    private native boolean nativeInvert(long j, long j2);

    private native boolean nativeIsIdentity(long j);

    private native void nativeMapPoints(long j, float[] fArr, int i, float[] fArr2, int i2, int i3);

    private native float nativeMapRadius(long j, float f);

    private native boolean nativeMapRect(long j, RectFloat rectFloat, RectFloat rectFloat2);

    private native void nativeMultiply(long j, long j2);

    private native boolean nativePostConcat(long j, long j2);

    private native boolean nativePostRotate(long j, float f, float f2, float f3);

    private native boolean nativePostScale(long j, float f, float f2);

    private native boolean nativePostScaleWithPoint(long j, float f, float f2, float f3, float f4);

    private native boolean nativePostTranslate(long j, float f, float f2);

    private native boolean nativePreConcat(long j, long j2);

    private native boolean nativePreRotate(long j, float f);

    private native boolean nativePreRotateWithPoint(long j, float f, float f2, float f3);

    private native boolean nativePreScale(long j, float f, float f2);

    private native boolean nativePreScaleWithPoint(long j, float f, float f2, float f3, float f4);

    private native boolean nativePreSkew(long j, float f, float f2);

    private native boolean nativePreTranslate(long j, float f, float f2);

    private native void nativeReset(long j);

    private native void nativeSetMatrix(long j, long j2);

    private native void nativeSetMatrixData(long j, float[] fArr);

    private native void nativeSetMatrixValue(long j, int i, float f);

    private native boolean nativeSetRectToRect(long j, RectFloat rectFloat, RectFloat rectFloat2, int i);

    private native void nativeSetRotate(long j, float f, float f2, float f3);

    private native void nativeSetScale(long j, float f, float f2);

    private native void nativeSetScaleWithPoint(long j, float f, float f2, float f3, float f4);

    private native void nativeSetTranslate(long j, float f, float f2);

    public int index(int i, int i2) {
        return (i2 * 3) + i;
    }

    public Matrix() {
        this.mNativeMatrixHandle = nativeGetMatrixHandle();
        MemoryCleanerRegistry.getInstance().register(this, new MatrixCleaner(this.mNativeMatrixHandle));
    }

    public Matrix(float[] fArr) {
        if (fArr == null) {
            this.mNativeMatrixHandle = nativeGetMatrixHandle();
        } else {
            this.mNativeMatrixHandle = nativeGetMatrixHandleWithArray(fArr);
        }
        MemoryCleanerRegistry.getInstance().register(this, new MatrixCleaner(this.mNativeMatrixHandle));
    }

    public Matrix(Matrix matrix) {
        if (matrix == null) {
            this.mNativeMatrixHandle = nativeGetMatrixHandle();
        } else {
            this.mNativeMatrixHandle = nativeGetMatrixHandleWithMatrix(matrix.getNativeHandle());
        }
        MemoryCleanerRegistry.getInstance().register(this, new MatrixCleaner(this.mNativeMatrixHandle));
    }

    public float[] getData() {
        float[] fArr = new float[9];
        nativeGetMatrixData(this.mNativeMatrixHandle, fArr);
        return fArr;
    }

    public void setMatrix(int i, int i2, float f) {
        nativeSetMatrixValue(this.mNativeMatrixHandle, (i2 * 3) + i, f);
    }

    public void setMatrix(Matrix matrix) {
        if (matrix == null) {
            reset();
        } else {
            nativeSetMatrix(this.mNativeMatrixHandle, matrix.getNativeHandle());
        }
    }

    public void setIdentity() {
        reset();
    }

    public void reset() {
        nativeReset(this.mNativeMatrixHandle);
    }

    public boolean isIdentity() {
        return nativeIsIdentity(this.mNativeMatrixHandle);
    }

    public boolean equalsMatrixValue(Matrix matrix) {
        if (matrix == null) {
            return false;
        }
        float[] data = getData();
        float[] data2 = matrix.getData();
        for (int i = 0; i < 9; i++) {
            if (Math.abs(data[i] - data2[i]) > 1.0E-6f) {
                return false;
            }
        }
        return true;
    }

    public void multiply(Matrix matrix) {
        if (matrix != null) {
            nativeMultiply(this.mNativeMatrixHandle, matrix.getNativeHandle());
        }
    }

    public void rotate(float f) {
        float[] fArr = {1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f};
        double d = (double) (f * DEGREES_TO_RADIAN);
        float cos = (float) Math.cos(d);
        float sin = (float) Math.sin(d);
        fArr[0] = cos;
        fArr[1] = -sin;
        fArr[3] = sin;
        fArr[4] = cos;
        multiply(new Matrix(fArr));
    }

    public void scale(float f, float f2) {
        float[] fArr = {1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f};
        fArr[0] = f;
        fArr[4] = f2;
        multiply(new Matrix(fArr));
    }

    public void translate(float f, float f2) {
        float[] fArr = {1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f};
        fArr[2] = f;
        fArr[5] = f2;
        multiply(new Matrix(fArr));
    }

    public void transpose() {
        float[] data = getData();
        int i = 0;
        while (i < 2) {
            int i2 = i + 1;
            for (int i3 = i2; i3 < 3; i3++) {
                int i4 = (i * 3) + i3;
                float f = data[i4];
                int i5 = (i3 * 3) + i;
                data[i4] = data[i5];
                data[i5] = f;
            }
            i = i2;
        }
        setElements(data);
    }

    public void setRotate(float f) {
        setRotate(f, 0.0f, 0.0f);
    }

    public void setRotate(float f, float f2, float f3) {
        nativeSetRotate(this.mNativeMatrixHandle, f, f2, f3);
    }

    public void getElements(float[] fArr) {
        if (fArr == null || fArr.length < 9) {
            throw new IllegalArgumentException("Illegal input array!");
        }
        nativeGetMatrixData(this.mNativeMatrixHandle, fArr);
    }

    public void setElements(float[] fArr) {
        if (fArr == null || fArr.length < 9) {
            throw new IllegalArgumentException("Illegal input array!");
        }
        nativeSetMatrixData(this.mNativeMatrixHandle, fArr);
    }

    public boolean invert(Matrix matrix) {
        if (matrix != null) {
            return nativeInvert(this.mNativeMatrixHandle, matrix.getNativeHandle());
        }
        return false;
    }

    public void mapPoints(float[] fArr) {
        if (fArr != null) {
            mapPoints(fArr, 0, fArr, 0, fArr.length / 2);
        }
    }

    public void mapPoints(float[] fArr, float[] fArr2) {
        if (fArr != null && fArr2 != null && fArr.length == fArr2.length) {
            mapPoints(fArr, 0, fArr2, 0, fArr.length / 2);
        }
    }

    public void mapPoints(float[] fArr, int i, float[] fArr2, int i2, int i3) {
        if (!isPointArrayIllegal(fArr2, i2, fArr, i, i3)) {
            nativeMapPoints(this.mNativeMatrixHandle, fArr, i, fArr2, i2, i3);
            return;
        }
        throw new IllegalArgumentException("Illegal points array");
    }

    public boolean postConcat(Matrix matrix) {
        if (matrix != null) {
            return nativePostConcat(this.mNativeMatrixHandle, matrix.getNativeHandle());
        }
        return false;
    }

    public boolean postScale(float f, float f2) {
        return nativePostScale(this.mNativeMatrixHandle, f, f2);
    }

    public boolean postScale(float f, float f2, float f3, float f4) {
        return nativePostScaleWithPoint(this.mNativeMatrixHandle, f, f2, f3, f4);
    }

    public boolean postTranslate(float f, float f2) {
        return nativePostTranslate(this.mNativeMatrixHandle, f, f2);
    }

    public boolean preConcat(Matrix matrix) {
        if (matrix != null) {
            return nativePreConcat(this.mNativeMatrixHandle, matrix.getNativeHandle());
        }
        return false;
    }

    public boolean preRotate(float f) {
        return nativePreRotate(this.mNativeMatrixHandle, f);
    }

    public boolean preRotate(float f, float f2, float f3) {
        return nativePreRotateWithPoint(this.mNativeMatrixHandle, f, f2, f3);
    }

    public void setScale(float f, float f2) {
        nativeSetScale(this.mNativeMatrixHandle, f, f2);
    }

    public void setScale(float f, float f2, float f3, float f4) {
        nativeSetScaleWithPoint(this.mNativeMatrixHandle, f, f2, f3, f4);
    }

    public boolean preTranslate(float f, float f2) {
        return nativePreTranslate(this.mNativeMatrixHandle, f, f2);
    }

    public void setTranslate(float f, float f2) {
        nativeSetTranslate(this.mNativeMatrixHandle, f, f2);
    }

    public boolean preScale(float f, float f2) {
        return nativePreScale(this.mNativeMatrixHandle, f, f2);
    }

    public boolean preScale(float f, float f2, float f3, float f4) {
        return nativePreScaleWithPoint(this.mNativeMatrixHandle, f, f2, f3, f4);
    }

    public boolean preSkew(float f, float f2) {
        return nativePreSkew(this.mNativeMatrixHandle, f, f2);
    }

    public float mapRadius(float f) {
        return nativeMapRadius(this.mNativeMatrixHandle, f);
    }

    public boolean postRotate(float f, float f2, float f3) {
        return nativePostRotate(this.mNativeMatrixHandle, f, f2, f3);
    }

    public boolean mapRect(RectFloat rectFloat) {
        return mapRect(rectFloat, rectFloat);
    }

    public boolean mapRect(RectFloat rectFloat, RectFloat rectFloat2) {
        if (rectFloat != null && rectFloat2 != null) {
            return nativeMapRect(this.mNativeMatrixHandle, rectFloat, rectFloat2);
        }
        throw new IllegalArgumentException("RectFloat is null");
    }

    public boolean setRectToRect(RectFloat rectFloat, RectFloat rectFloat2, ScaleToFit scaleToFit) {
        if (rectFloat != null && rectFloat2 != null) {
            return nativeSetRectToRect(this.mNativeMatrixHandle, rectFloat, rectFloat2, scaleToFit.value());
        }
        throw new IllegalArgumentException("RectFloat is null");
    }

    protected static class MatrixCleaner extends NativeMemoryCleanerHelper {
        private native void nativeMatrixRelease(long j);

        public MatrixCleaner(long j) {
            super(j);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.utils.NativeMemoryCleanerHelper
        public void releaseNativeMemory(long j) {
            if (j != 0) {
                nativeMatrixRelease(j);
            }
        }
    }

    public long getNativeHandle() {
        return this.mNativeMatrixHandle;
    }

    public enum ScaleToFit {
        FILL(0),
        START(1),
        CENTER(2),
        END(3);
        
        final int enumInt;

        private ScaleToFit(int i) {
            this.enumInt = i;
        }

        public int value() {
            return this.enumInt;
        }
    }

    private boolean isPointArrayIllegal(float[] fArr, int i, float[] fArr2, int i2, int i3) {
        if (!(fArr == null || fArr2 == null)) {
            int i4 = i3 * 2;
            int i5 = i + i4;
            int i6 = i4 + i2;
            if (!(((((i | i3) | i2) | i5) | i6) < 0) && i5 <= fArr.length && i6 <= fArr2.length) {
                return false;
            }
        }
        return true;
    }
}
