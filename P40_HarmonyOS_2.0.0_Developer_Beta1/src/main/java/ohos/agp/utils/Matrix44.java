package ohos.agp.utils;

public class Matrix44 {
    private long mNativeMatrixHandle;

    private native double nativeDeterminant(long j);

    private native float nativeGet(long j, int i, int i2);

    private native double nativeGetDouble(long j, int i, int i2);

    private native float nativeGetFloat(long j, int i, int i2);

    private native long nativeGetMatrix44Handle();

    private native long nativeGetMatrix44HandleWithSrcMatrix(long j);

    private native long nativeGetMatrix44HandleWithTwoMatrix(long j, long j2);

    private native boolean nativeHasPerspective(long j);

    private native boolean nativeInvert(long j, long j2);

    private native boolean nativeIsIdentity(long j);

    private native boolean nativeIsScale(long j);

    private native boolean nativeIsScaleTranslate(long j);

    private native boolean nativeIsTranslate(long j);

    private native void nativePostConcat(long j, long j2);

    private native void nativePostScale(long j, float f, float f2, float f3);

    private native void nativePostScaleWithScale(long j, float f);

    private native void nativePostTranslate(long j, float f, float f2, float f3);

    private native void nativePreConcat(long j, long j2);

    private native void nativePreScale(long j, float f, float f2, float f3);

    private native void nativePreScaleWithScale(long j, float f);

    private native void nativePreTranslate(long j, float f, float f2, float f3);

    private native void nativeReset(long j);

    private native void nativeSet(long j, int i, int i2, float f);

    private native void nativeSetConcat(long j, long j2, long j3);

    private native void nativeSetDouble(long j, int i, int i2, double d);

    private native void nativeSetFloat(long j, int i, int i2, float f);

    private native void nativeSetIdentity(long j);

    private native void nativeSetRotateAbout(long j, float f, float f2, float f3, float f4);

    private native void nativeSetRotateAboutUnit(long j, float f, float f2, float f3, float f4);

    private native void nativeSetRotateDegreesAbout(long j, float f, float f2, float f3, float f4);

    private native void nativeSetScale(long j, float f, float f2, float f3);

    private native void nativeSetScaleWithScale(long j, float f);

    private native void nativeSetTranslate(long j, float f, float f2, float f3);

    private native void nativeTranspose(long j);

    public Matrix44() {
        this.mNativeMatrixHandle = nativeGetMatrix44Handle();
        MemoryCleanerRegistry.getInstance().register(this, new Matrix44Cleaner(this.mNativeMatrixHandle));
    }

    public Matrix44(Matrix44 matrix44) {
        this.mNativeMatrixHandle = nativeGetMatrix44HandleWithSrcMatrix(matrix44.getNativeHandle());
        MemoryCleanerRegistry.getInstance().register(this, new Matrix44Cleaner(this.mNativeMatrixHandle));
    }

    public Matrix44(Matrix44 matrix44, Matrix44 matrix442) {
        this.mNativeMatrixHandle = nativeGetMatrix44HandleWithTwoMatrix(matrix44.getNativeHandle(), matrix442.getNativeHandle());
        MemoryCleanerRegistry.getInstance().register(this, new Matrix44Cleaner(this.mNativeMatrixHandle));
    }

    public boolean isIdentity() {
        return nativeIsIdentity(this.mNativeMatrixHandle);
    }

    public boolean isTranslate() {
        return nativeIsTranslate(this.mNativeMatrixHandle);
    }

    public boolean isScaleTranslate() {
        return nativeIsScaleTranslate(this.mNativeMatrixHandle);
    }

    public boolean isScale() {
        return nativeIsScale(this.mNativeMatrixHandle);
    }

    public boolean hasPerspective() {
        return nativeHasPerspective(this.mNativeMatrixHandle);
    }

    public void setIdentity() {
        nativeSetIdentity(this.mNativeMatrixHandle);
    }

    public void reset() {
        nativeReset(this.mNativeMatrixHandle);
    }

    public float get(int i, int i2) {
        return nativeGet(this.mNativeMatrixHandle, i, i2);
    }

    public void set(int i, int i2, float f) {
        nativeSet(this.mNativeMatrixHandle, i, i2, f);
    }

    public double getDouble(int i, int i2) {
        return nativeGetDouble(this.mNativeMatrixHandle, i, i2);
    }

    public void setDouble(int i, int i2, double d) {
        nativeSetDouble(this.mNativeMatrixHandle, i, i2, d);
    }

    public float getFloat(int i, int i2) {
        return nativeGetFloat(this.mNativeMatrixHandle, i, i2);
    }

    public void setFloat(int i, int i2, float f) {
        nativeSetFloat(this.mNativeMatrixHandle, i, i2, f);
    }

    public void setTranslate(float f, float f2, float f3) {
        nativeSetTranslate(this.mNativeMatrixHandle, f, f2, f3);
    }

    public void preTranslate(float f, float f2, float f3) {
        nativePreTranslate(this.mNativeMatrixHandle, f, f2, f3);
    }

    public void postTranslate(float f, float f2, float f3) {
        nativePostTranslate(this.mNativeMatrixHandle, f, f2, f3);
    }

    public void setScale(float f, float f2, float f3) {
        nativeSetScale(this.mNativeMatrixHandle, f, f2, f3);
    }

    public void preScale(float f, float f2, float f3) {
        nativePreScale(this.mNativeMatrixHandle, f, f2, f3);
    }

    public void postScale(float f, float f2, float f3) {
        nativePostScale(this.mNativeMatrixHandle, f, f2, f3);
    }

    public void setScale(float f) {
        nativeSetScaleWithScale(this.mNativeMatrixHandle, f);
    }

    public void preScale(float f) {
        nativePreScaleWithScale(this.mNativeMatrixHandle, f);
    }

    public void postScale(float f) {
        nativePostScaleWithScale(this.mNativeMatrixHandle, f);
    }

    public void setRotateDegreesAbout(float f, float f2, float f3, float f4) {
        nativeSetRotateDegreesAbout(this.mNativeMatrixHandle, f, f2, f3, f4);
    }

    public void setRotateAbout(float f, float f2, float f3, float f4) {
        nativeSetRotateAbout(this.mNativeMatrixHandle, f, f2, f3, f4);
    }

    public void setRotateAboutUnit(float f, float f2, float f3, float f4) {
        nativeSetRotateAboutUnit(this.mNativeMatrixHandle, f, f2, f3, f4);
    }

    public void setConcat(Matrix44 matrix44, Matrix44 matrix442) {
        if (matrix44 == null || matrix442 == null) {
            throw new IllegalArgumentException("Matrix is null");
        }
        nativeSetConcat(this.mNativeMatrixHandle, matrix44.getNativeHandle(), matrix442.getNativeHandle());
    }

    public void preConcat(Matrix44 matrix44) {
        if (matrix44 != null) {
            nativePreConcat(this.mNativeMatrixHandle, matrix44.getNativeHandle());
            return;
        }
        throw new IllegalArgumentException("Matrix is null");
    }

    public void postConcat(Matrix44 matrix44) {
        if (matrix44 != null) {
            nativePostConcat(this.mNativeMatrixHandle, matrix44.getNativeHandle());
            return;
        }
        throw new IllegalArgumentException("Matrix is null");
    }

    public boolean invert(Matrix44 matrix44) {
        if (matrix44 != null) {
            return nativeInvert(this.mNativeMatrixHandle, matrix44.getNativeHandle());
        }
        throw new IllegalArgumentException("Matrix is null");
    }

    public void transpose() {
        nativeTranspose(this.mNativeMatrixHandle);
    }

    public double determinant() {
        return nativeDeterminant(this.mNativeMatrixHandle);
    }

    public long getNativeHandle() {
        return this.mNativeMatrixHandle;
    }

    protected static class Matrix44Cleaner extends NativeMemoryCleanerHelper {
        private native void nativeMatrix44Release(long j);

        public Matrix44Cleaner(long j) {
            super(j);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.utils.NativeMemoryCleanerHelper
        public void releaseNativeMemory(long j) {
            if (j != 0) {
                nativeMatrix44Release(j);
            }
        }
    }
}
