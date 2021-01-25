package ohos.agp.render;

import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.NativeMemoryCleanerHelper;

public class MaskFilter {
    private static final int MIN_DIRECTION_SIZE = 3;
    private static final int MIN_TABLE_SIZE = 256;
    private long mNativeMaskFilterHandle = 0;

    private native long nativeGetBlurMaskFilterHandle(float f, int i);

    private native long nativeGetEmbossMaskFilterHandle(float[] fArr, float f, float f2, float f3);

    private native long nativeGetTableMaskFilterHandle(byte[] bArr);

    public enum Blur {
        NORMAL(0),
        SOLID(1),
        OUTER(2),
        INNER(3);
        
        final int enumInt;

        private Blur(int i) {
            this.enumInt = i;
        }

        public int value() {
            return this.enumInt;
        }
    }

    public MaskFilter(float f, Blur blur) {
        this.mNativeMaskFilterHandle = nativeGetBlurMaskFilterHandle(f, (blur == null ? Blur.NORMAL : blur).value());
        MemoryCleanerRegistry.getInstance().register(this, new MaskFilterCleaner(this.mNativeMaskFilterHandle));
    }

    public MaskFilter(float[] fArr, float f, float f2, float f3) {
        if (fArr != null && fArr.length >= 3) {
            this.mNativeMaskFilterHandle = nativeGetEmbossMaskFilterHandle(fArr, f, f2, f3);
            MemoryCleanerRegistry.getInstance().register(this, new MaskFilterCleaner(this.mNativeMaskFilterHandle));
        }
    }

    public MaskFilter(byte[] bArr) {
        if (bArr != null && bArr.length >= 256) {
            this.mNativeMaskFilterHandle = nativeGetTableMaskFilterHandle(bArr);
            MemoryCleanerRegistry.getInstance().register(this, new MaskFilterCleaner(this.mNativeMaskFilterHandle));
        }
    }

    protected static class MaskFilterCleaner extends NativeMemoryCleanerHelper {
        private native void nativeMaskFilterRelease(long j);

        public MaskFilterCleaner(long j) {
            super(j);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.utils.NativeMemoryCleanerHelper
        public void releaseNativeMemory(long j) {
            if (j != 0) {
                nativeMaskFilterRelease(j);
            }
        }
    }

    /* access modifiers changed from: protected */
    public long getNativeHandle() {
        return this.mNativeMaskFilterHandle;
    }
}
