package ohos.agp.render;

import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.NativeMemoryCleanerHelper;
import ohos.media.image.PixelMap;

public class PixelMapHolder {
    private final long mNativePlatformBitmapPtr;
    private PixelMap mUserPixelMap;

    private native long nativeGetPlatformBitmapHandle(PixelMap pixelMap);

    private native void nativeResetPixelMap(long j, PixelMap pixelMap);

    public PixelMapHolder(PixelMap pixelMap) {
        this.mNativePlatformBitmapPtr = nativeGetPlatformBitmapHandle(pixelMap);
        this.mUserPixelMap = pixelMap;
        MemoryCleanerRegistry.getInstance().register(this, new PixelMapHolderCleaner(this.mNativePlatformBitmapPtr));
    }

    public long getNativeHolder() {
        return this.mNativePlatformBitmapPtr;
    }

    public PixelMap getPixelMap() {
        return this.mUserPixelMap;
    }

    public void resetPixelMap(PixelMap pixelMap) {
        nativeResetPixelMap(this.mNativePlatformBitmapPtr, pixelMap);
        this.mUserPixelMap = pixelMap;
    }

    public void release() {
        PixelMap pixelMap = this.mUserPixelMap;
        if (pixelMap != null) {
            pixelMap.release();
            this.mUserPixelMap = null;
        }
    }

    protected static class PixelMapHolderCleaner extends NativeMemoryCleanerHelper {
        private native void nativePixelMapHolderRelease(long j);

        public PixelMapHolderCleaner(long j) {
            super(j);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.utils.NativeMemoryCleanerHelper
        public void releaseNativeMemory(long j) {
            if (j != 0) {
                nativePixelMapHolderRelease(j);
            }
        }
    }
}
