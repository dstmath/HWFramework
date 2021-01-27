package ohos.agp.render;

import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.NativeMemoryCleanerHelper;
import ohos.agp.utils.Rect;

public class RegionIterator {
    private long mNativeIteratorHandle;

    private native long nativeGetIteratorHandle(long j);

    private native boolean nativeNext(long j, Rect rect);

    public RegionIterator(Region region) {
        this.mNativeIteratorHandle = nativeGetIteratorHandle(region.getNativeHandle());
        MemoryCleanerRegistry.getInstance().register(this, new RegionIteratorCleaner(this.mNativeIteratorHandle));
    }

    public final boolean next(Rect rect) {
        if (rect != null) {
            return nativeNext(this.mNativeIteratorHandle, rect);
        }
        throw new IllegalArgumentException("Rect is null");
    }

    protected static class RegionIteratorCleaner extends NativeMemoryCleanerHelper {
        private native void nativeRegionIteratorRelease(long j);

        public RegionIteratorCleaner(long j) {
            super(j);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.utils.NativeMemoryCleanerHelper
        public void releaseNativeMemory(long j) {
            if (j != 0) {
                nativeRegionIteratorRelease(j);
            }
        }
    }
}
