package ohos.agp.render;

import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.NativeMemoryCleanerHelper;

public class ColorFilter {
    private long mNativeColorFilterHandle = 0;

    private native long nativeGetBlendModeColorFilterHandle(int i, int i2);

    public ColorFilter(int i, BlendMode blendMode) {
        if (blendMode != null) {
            this.mNativeColorFilterHandle = nativeGetBlendModeColorFilterHandle(i, blendMode.value());
            MemoryCleanerRegistry.getInstance().register(this, new ColorFilterCleaner(this.mNativeColorFilterHandle));
        }
    }

    protected static class ColorFilterCleaner extends NativeMemoryCleanerHelper {
        private native void nativeColorFilterRelease(long j);

        public ColorFilterCleaner(long j) {
            super(j);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.utils.NativeMemoryCleanerHelper
        public void releaseNativeMemory(long j) {
            if (j != 0) {
                nativeColorFilterRelease(j);
            }
        }
    }

    /* access modifiers changed from: protected */
    public long getNativeHandle() {
        return this.mNativeColorFilterHandle;
    }
}
