package ohos.agp.components;

import ohos.agp.utils.MemoryCleaner;
import ohos.agp.utils.MemoryCleanerRegistry;

public class ScrollHelper {
    private long mNativePtr = nativeCreateOverScroller();

    private native void nativeAbortAnimation(long j);

    private native boolean nativeComputeScrollOffset(long j);

    private native long nativeCreateOverScroller();

    private native void nativeFling(long j, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8);

    private native int nativeGetCurrX(long j);

    private native int nativeGetCurrY(long j);

    private native float nativeGetCurrentVelocity(long j);

    private native float nativeGetFriction(long j);

    private native boolean nativeIsFinished(long j);

    private native boolean nativeIsOverScrolled(long j);

    private native void nativeSetFriction(long j, float f);

    private native void nativeStartScroll(long j, int i, int i2, int i3, int i4);

    public static class OverScrollerCleaner implements MemoryCleaner {
        private long mNativePtr;

        private native void nativeRelease(long j);

        OverScrollerCleaner(long j) {
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

    public ScrollHelper() {
        MemoryCleanerRegistry.getInstance().register(this, new OverScrollerCleaner(this.mNativePtr));
    }

    public void fling(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        nativeFling(this.mNativePtr, i, i2, i3, i4, i5, i6, i7, i8);
    }

    public void startScroll(int i, int i2, int i3, int i4) {
        nativeStartScroll(this.mNativePtr, i, i2, i3, i4);
    }

    public int getCurrX() {
        return nativeGetCurrX(this.mNativePtr);
    }

    public int getCurrY() {
        return nativeGetCurrY(this.mNativePtr);
    }

    public boolean isFinished() {
        return nativeIsFinished(this.mNativePtr);
    }

    public void setFriction(float f) {
        nativeSetFriction(this.mNativePtr, f);
    }

    public float getFriction() {
        return nativeGetFriction(this.mNativePtr);
    }

    public void abortAnimation() {
        nativeAbortAnimation(this.mNativePtr);
    }

    public boolean computeScrollOffset() {
        return nativeComputeScrollOffset(this.mNativePtr);
    }

    public float getCurrVelocity() {
        return nativeGetCurrentVelocity(this.mNativePtr);
    }

    public boolean isOverScrolled() {
        return nativeIsOverScrolled(this.mNativePtr);
    }
}
