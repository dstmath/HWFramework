package ohos.agp.render;

import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.NativeMemoryCleanerHelper;

public class PathMeasure {
    protected long mNativePathMeasureHandle = 0;
    private Path mPath;

    private native long nativeCreatePathMeasureHandle(long j, boolean z);

    private native float nativeGetLength(long j);

    private native boolean nativeGetPosTan(long j, float f, float[] fArr, float[] fArr2);

    private native boolean nativeGetSegment(long j, float f, float f2, long j2, boolean z);

    private native void nativeSetPath(long j, long j2, boolean z);

    public PathMeasure(Path path, boolean z) {
        long j = 0;
        if (path != null) {
            j = path.getNativeHandle();
            this.mPath = path;
        }
        this.mNativePathMeasureHandle = nativeCreatePathMeasureHandle(j, z);
        MemoryCleanerRegistry.getInstance().register(this, new PathMeasureCleaner(this.mNativePathMeasureHandle));
    }

    protected static class PathMeasureCleaner extends NativeMemoryCleanerHelper {
        private native void nativePathMeasureRelease(long j);

        public PathMeasureCleaner(long j) {
            super(j);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.utils.NativeMemoryCleanerHelper
        public void releaseNativeMemory(long j) {
            if (j != 0) {
                nativePathMeasureRelease(j);
            }
        }
    }

    public long getNativeHandle() {
        return this.mNativePathMeasureHandle;
    }

    public float getLength() {
        return nativeGetLength(this.mNativePathMeasureHandle);
    }

    public boolean getPosTan(float f, float[] fArr, float[] fArr2) {
        return nativeGetPosTan(this.mNativePathMeasureHandle, f, fArr, fArr2);
    }

    public void setPath(Path path, boolean z) {
        this.mPath = path;
        nativeSetPath(this.mNativePathMeasureHandle, path != null ? path.getNativeHandle() : 0, z);
    }

    public boolean getSegment(float f, float f2, Path path, boolean z) {
        float length = getLength();
        float f3 = f < 0.0f ? 0.0f : f;
        float f4 = f2 > length ? length : f2;
        if (f3 >= f4) {
            return false;
        }
        return nativeGetSegment(this.mNativePathMeasureHandle, f3, f4, path.getNativeHandle(), z);
    }
}
