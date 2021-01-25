package ohos.agp.render;

import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.NativeMemoryCleanerHelper;

public class PathEffect {
    private PathEffect mInnerPathEffect;
    private long mNativePathEffectHandle = 0;
    private PathEffect mOuterPathEffect;
    private Path mShape;

    private native long nativeGet1DPathEffectHandle(long j, float f, float f2, int i);

    private native long nativeGetComposePathEffectHandle(long j, long j2);

    private native long nativeGetCornerPathEffectHandle(float f);

    private native long nativeGetDashPathEffectHandle(float[] fArr, int i, float f);

    private native long nativeGetDiscretePathEffectHandle(float f, float f2);

    public enum Style {
        TRANSLATE(0),
        ROTATE(1),
        MORPH(2);
        
        final int enumInt;

        private Style(int i) {
            this.enumInt = i;
        }

        public int value() {
            return this.enumInt;
        }
    }

    public PathEffect(float[] fArr, float f) {
        if (fArr.length % 2 == 0 && fArr.length >= 2) {
            this.mNativePathEffectHandle = nativeGetDashPathEffectHandle(fArr, fArr.length, f);
            MemoryCleanerRegistry.getInstance().register(this, new PathEffectCleaner(this.mNativePathEffectHandle));
        }
    }

    public PathEffect(float f) {
        this.mNativePathEffectHandle = nativeGetCornerPathEffectHandle(f);
        MemoryCleanerRegistry.getInstance().register(this, new PathEffectCleaner(this.mNativePathEffectHandle));
    }

    public PathEffect(Path path, float f, float f2, Style style) {
        if (this.mShape != path) {
            this.mShape = path;
        }
        this.mNativePathEffectHandle = nativeGet1DPathEffectHandle(path.getNativeHandle(), f, f2, style.value());
        MemoryCleanerRegistry.getInstance().register(this, new PathEffectCleaner(this.mNativePathEffectHandle));
    }

    public PathEffect(float f, float f2) {
        this.mNativePathEffectHandle = nativeGetDiscretePathEffectHandle(f, f2);
        MemoryCleanerRegistry.getInstance().register(this, new PathEffectCleaner(this.mNativePathEffectHandle));
    }

    public PathEffect(PathEffect pathEffect, PathEffect pathEffect2) {
        if (this.mInnerPathEffect != pathEffect2) {
            this.mInnerPathEffect = pathEffect2;
        }
        if (this.mOuterPathEffect != pathEffect) {
            this.mOuterPathEffect = pathEffect;
        }
        this.mNativePathEffectHandle = nativeGetComposePathEffectHandle(pathEffect.getNativeHandle(), pathEffect2.getNativeHandle());
        MemoryCleanerRegistry.getInstance().register(this, new PathEffectCleaner(this.mNativePathEffectHandle));
    }

    protected static class PathEffectCleaner extends NativeMemoryCleanerHelper {
        private native void nativePathEffectRelease(long j);

        public PathEffectCleaner(long j) {
            super(j);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.utils.NativeMemoryCleanerHelper
        public void releaseNativeMemory(long j) {
            if (j != 0) {
                nativePathEffectRelease(j);
            }
        }
    }

    /* access modifiers changed from: protected */
    public long getNativeHandle() {
        return this.mNativePathEffectHandle;
    }
}
