package ohos.agp.view;

import ohos.agp.utils.MemoryCleaner;
import ohos.agp.utils.MemoryCleanerRegistry;

public class ColorStateList {
    protected long mNativePtr = 0;

    private native long nativeCreate(int[][] iArr, int[] iArr2);

    private native int nativeGetColorForState(long j, int[] iArr, int i);

    public void release() {
    }

    protected static class ColorStateListCleaner implements MemoryCleaner {
        private long mNativePtr;

        private native void nativeRelease(long j);

        ColorStateListCleaner(long j) {
            this.mNativePtr = j;
        }

        @Override // ohos.agp.utils.MemoryCleaner
        public void run() {
            long j = this.mNativePtr;
            if (j != 0) {
                nativeRelease(j);
                this.mNativePtr = 0;
            }
        }
    }

    public ColorStateList(int[][] iArr, int[] iArr2) {
        this.mNativePtr = nativeCreate(iArr, iArr2);
        MemoryCleanerRegistry.getInstance().register(this, new ColorStateListCleaner(this.mNativePtr));
    }

    public int getColorForState(int[] iArr, int i) {
        return nativeGetColorForState(this.mNativePtr, iArr, i);
    }
}
