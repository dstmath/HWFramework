package ohos.agp.transition;

import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.utils.MemoryCleaner;
import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.hiviewdfx.HiLogLabel;

public class Transition {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_TRANSITION");
    protected long mNativeTransitionPtr;

    private native long nativeGetTransitionHandle();

    private native void nativeSetTransitionDuration(long j, float f);

    public Transition() {
        createTransitionNativePtr();
        registerTransitionCleaner();
    }

    /* access modifiers changed from: protected */
    public void createTransitionNativePtr() {
        if (this.mNativeTransitionPtr == 0) {
            this.mNativeTransitionPtr = nativeGetTransitionHandle();
        }
    }

    /* access modifiers changed from: protected */
    public void registerTransitionCleaner() {
        MemoryCleanerRegistry.getInstance().registerWithNativeBind(this, new TransitionCleaner(this.mNativeTransitionPtr), this.mNativeTransitionPtr);
    }

    public long getNativeTransitionPtr() {
        return this.mNativeTransitionPtr;
    }

    public void setDuration(float f) {
        nativeSetTransitionDuration(this.mNativeTransitionPtr, f);
    }

    /* access modifiers changed from: protected */
    public static class TransitionCleaner implements MemoryCleaner {
        private long mNativePtr;

        private native void nativeTransitionRelease(long j);

        public TransitionCleaner(long j) {
            this.mNativePtr = j;
        }

        @Override // ohos.agp.utils.MemoryCleaner
        public void run() {
            long j = this.mNativePtr;
            if (j != 0) {
                nativeTransitionRelease(j);
                this.mNativePtr = 0;
            }
        }
    }
}
