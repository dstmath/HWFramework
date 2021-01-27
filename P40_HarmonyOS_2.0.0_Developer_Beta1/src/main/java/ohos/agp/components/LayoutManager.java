package ohos.agp.components;

import ohos.agp.utils.MemoryCleaner;
import ohos.agp.utils.MemoryCleanerRegistry;

public abstract class LayoutManager {
    protected long mNativePtr = 0;

    private native int nativeGetOrientation(long j);

    private native void nativeSetOrientation(long j, int i);

    /* access modifiers changed from: protected */
    public abstract void createNativePtr();

    /* access modifiers changed from: protected */
    public static class LayoutManagerCleaner implements MemoryCleaner {
        private long mNativePtr;

        private native void nativeRelease(long j);

        LayoutManagerCleaner(long j) {
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

    public LayoutManager() {
        createNativePtr();
        registerCleaner();
    }

    public void setOrientation(int i) {
        nativeSetOrientation(this.mNativePtr, i);
    }

    public int getOrientation() {
        return nativeGetOrientation(this.mNativePtr);
    }

    public long getNativePtr() {
        return this.mNativePtr;
    }

    private void registerCleaner() {
        if (this.mNativePtr != 0) {
            MemoryCleanerRegistry.getInstance().register(this, new LayoutManagerCleaner(this.mNativePtr));
        }
    }
}
