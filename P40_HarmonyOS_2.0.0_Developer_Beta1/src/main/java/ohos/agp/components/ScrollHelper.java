package ohos.agp.components;

import ohos.agp.utils.MemoryCleaner;
import ohos.agp.utils.MemoryCleanerRegistry;

public class ScrollHelper {
    public static final int AXIS_X = 0;
    public static final int AXIS_Y = 1;
    private final long mNativePtr = nativeCreateOverScroller();

    private native void nativeAbortAnimation(long j);

    private native long nativeCreateOverScroller();

    private native void nativeDoFling(long j, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8);

    private native int nativeGetCurrX(long j);

    private native int nativeGetCurrY(long j);

    private native float nativeGetCurrentVelocity(long j);

    private native int nativeGetFlingDistanceX(long j, int i);

    private native int nativeGetFlingDistanceY(long j, int i);

    private native int nativeGetFlingVelocityX(long j);

    private native int nativeGetFlingVelocityY(long j);

    private native float nativeGetFriction(long j);

    private native int nativeGetHorizontalBeginPosition(long j);

    private native int nativeGetScrollDistanceX(long j);

    private native int nativeGetScrollDistanceY(long j);

    private native boolean nativeIsFinished(long j);

    private native boolean nativeIsOverScrolled(long j);

    private native void nativeSetFriction(long j, float f);

    private native void nativeStartScroll(long j, int i, int i2, int i3, int i4);

    private native boolean nativeUpdateScroll(long j);

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

    public void doFling(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        nativeDoFling(this.mNativePtr, i, i2, i3, i4, i5, i6, i7, i8);
    }

    public void doFlingX(int i, int i2, int i3, int i4) {
        long j = this.mNativePtr;
        nativeDoFling(j, i, nativeGetCurrY(j), i2, 0, i3, i4, 0, 0);
    }

    public void doFlingY(int i, int i2, int i3, int i4) {
        long j = this.mNativePtr;
        nativeDoFling(j, nativeGetCurrX(j), i, 0, i2, 0, 0, i3, i4);
    }

    public void startScrollX(int i, int i2) {
        long j = this.mNativePtr;
        nativeStartScroll(j, i, nativeGetCurrY(j), i2, 0);
    }

    public void startScrollY(int i, int i2) {
        long j = this.mNativePtr;
        nativeStartScroll(j, nativeGetCurrX(j), i, 0, i2);
    }

    public int getFlingDistanceX(int i) {
        return nativeGetFlingDistanceX(this.mNativePtr, i);
    }

    public int getFlingDistanceY(int i) {
        return nativeGetFlingDistanceY(this.mNativePtr, i);
    }

    public int getFlingVelocityX() {
        return nativeGetFlingVelocityX(this.mNativePtr);
    }

    public int getFlingVelocityY() {
        return nativeGetFlingVelocityY(this.mNativePtr);
    }

    public void startScroll(int i, int i2, int i3, int i4) {
        nativeStartScroll(this.mNativePtr, i, i2, i3, i4);
    }

    public int getHorizontalBeginPosition() {
        return nativeGetHorizontalBeginPosition(this.mNativePtr);
    }

    public int getScrollDistanceX() {
        return nativeGetScrollDistanceX(this.mNativePtr);
    }

    public int getScrollDistanceY() {
        return nativeGetScrollDistanceY(this.mNativePtr);
    }

    public int getCurrValue(int i) {
        if (i == 0) {
            return nativeGetCurrX(this.mNativePtr);
        }
        if (i == 1) {
            return nativeGetCurrY(this.mNativePtr);
        }
        return 0;
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

    public boolean updateScroll() {
        return nativeUpdateScroll(this.mNativePtr);
    }

    public float getCurrVelocity() {
        return nativeGetCurrentVelocity(this.mNativePtr);
    }

    public boolean isOverScrolled() {
        return nativeIsOverScrolled(this.mNativePtr);
    }
}
