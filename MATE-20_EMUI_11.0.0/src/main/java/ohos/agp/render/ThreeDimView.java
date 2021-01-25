package ohos.agp.render;

import ohos.agp.utils.Matrix;
import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.NativeMemoryCleanerHelper;

public class ThreeDimView {
    private long mNative3DViewHandle;

    private native long nativeGet3DViewHandle();

    private native void nativeGetMatrix(long j, long j2);

    private native void nativeRotateX(long j, float f);

    private native void nativeRotateY(long j, float f);

    private native void nativeRotateZ(long j, float f);

    public ThreeDimView() {
        this.mNative3DViewHandle = 0;
        this.mNative3DViewHandle = nativeGet3DViewHandle();
        MemoryCleanerRegistry.getInstance().register(this, new ThreeDimViewCleaner(this.mNative3DViewHandle));
    }

    public void getMatrix(Matrix matrix) {
        nativeGetMatrix(this.mNative3DViewHandle, matrix.getNativeHandle());
    }

    public void rotateX(float f) {
        nativeRotateX(this.mNative3DViewHandle, f);
    }

    public void rotateY(float f) {
        nativeRotateY(this.mNative3DViewHandle, f);
    }

    public void rotateZ(float f) {
        nativeRotateZ(this.mNative3DViewHandle, f);
    }

    protected static class ThreeDimViewCleaner extends NativeMemoryCleanerHelper {
        private native void nativeThreeDimViewRelease(long j);

        public ThreeDimViewCleaner(long j) {
            super(j);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.utils.NativeMemoryCleanerHelper
        public void releaseNativeMemory(long j) {
            if (j != 0) {
                nativeThreeDimViewRelease(j);
            }
        }
    }

    /* access modifiers changed from: protected */
    public long getNativeHandle() {
        return this.mNative3DViewHandle;
    }
}
