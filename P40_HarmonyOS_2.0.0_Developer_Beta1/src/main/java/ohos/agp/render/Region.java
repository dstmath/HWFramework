package ohos.agp.render;

import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.NativeMemoryCleanerHelper;
import ohos.agp.utils.Rect;

public class Region {
    private static final int UNION_OP_TYPE = 2;
    private long mNativeRegionHandle = nativeGetRegionHandle();

    private native boolean nativeGetBoundaryPath(long j, long j2);

    private native boolean nativeGetBounds(long j, Rect rect);

    private native long nativeGetRegionHandle();

    private native void nativeSetRect(long j, int i, int i2, int i3, int i4);

    private native void nativeSetRegion(long j, long j2);

    private native boolean opWithRect(long j, int i, int i2, int i3, int i4, int i5);

    public Region() {
        MemoryCleanerRegistry.getInstance().register(this, new RegionCleaner(this.mNativeRegionHandle));
    }

    public Region(Region region) {
        if (region != null) {
            nativeSetRegion(this.mNativeRegionHandle, region.getNativeHandle());
        }
        MemoryCleanerRegistry.getInstance().register(this, new RegionCleaner(this.mNativeRegionHandle));
    }

    public Region(Rect rect) {
        if (rect != null) {
            nativeSetRect(this.mNativeRegionHandle, rect.left, rect.top, rect.right, rect.bottom);
        }
        MemoryCleanerRegistry.getInstance().register(this, new RegionCleaner(this.mNativeRegionHandle));
    }

    public final boolean union(Rect rect) {
        return opWithRect(this.mNativeRegionHandle, rect.left, rect.top, rect.right, rect.bottom, 2);
    }

    public void clear() {
        nativeSetRect(this.mNativeRegionHandle, 0, 0, 0, 0);
    }

    public Rect getBounds() {
        Rect rect = new Rect();
        nativeGetBounds(this.mNativeRegionHandle, rect);
        return rect;
    }

    public boolean getBounds(Rect rect) {
        if (rect == null) {
            return false;
        }
        return nativeGetBounds(this.mNativeRegionHandle, rect);
    }

    public Path getBoundaryPath() {
        Path path = new Path();
        nativeGetBoundaryPath(this.mNativeRegionHandle, path.getNativeHandle());
        return path;
    }

    public boolean getBoundaryPath(Path path) {
        return nativeGetBoundaryPath(this.mNativeRegionHandle, path.getNativeHandle());
    }

    protected static class RegionCleaner extends NativeMemoryCleanerHelper {
        private native void nativeRegionRelease(long j);

        public RegionCleaner(long j) {
            super(j);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.utils.NativeMemoryCleanerHelper
        public void releaseNativeMemory(long j) {
            if (j != 0) {
                nativeRegionRelease(j);
            }
        }
    }

    public long getNativeHandle() {
        return this.mNativeRegionHandle;
    }
}
