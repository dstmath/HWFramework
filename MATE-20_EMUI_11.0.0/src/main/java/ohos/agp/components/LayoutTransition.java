package ohos.agp.components;

import ohos.agp.animation.AnimatorProperty;
import ohos.agp.utils.MemoryCleaner;
import ohos.agp.utils.MemoryCleanerRegistry;

public class LayoutTransition {
    public static final int APPEARING = 2;
    public static final int CHANGE_APPEARING = 0;
    public static final int CHANGE_DISAPPEARING = 1;
    public static final int CHANGING = 4;
    public static final int DISAPPEARING = 3;
    private final AnimatorProperty[] mAnimatorProperty = {null, null, null, null, null};
    protected long mNativePtr = 0;

    private native void nativeDisableTransition(long j, int i);

    private native void nativeEnableTransition(long j, int i);

    private native long nativeGetLayoutTransitionHandle();

    private native boolean nativeIsTransitionEnabled(long j, int i);

    private native void nativeSetDuration(long j, int i, int i2);

    private native void nativeSetViewPropertyAnimator(long j, int i, AnimatorProperty animatorProperty);

    public void release() {
    }

    protected static class LayoutTransitionCleaner implements MemoryCleaner {
        private long mNativePtr;

        private native void nativeRelease(long j);

        LayoutTransitionCleaner(long j) {
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

    public LayoutTransition() {
        createNativePtr();
        MemoryCleanerRegistry.getInstance().register(this, new LayoutTransitionCleaner(this.mNativePtr));
    }

    /* access modifiers changed from: protected */
    public void createNativePtr() {
        if (this.mNativePtr == 0) {
            this.mNativePtr = nativeGetLayoutTransitionHandle();
        }
    }

    public void enableTransitionType(int i) {
        nativeEnableTransition(this.mNativePtr, i);
    }

    public void disableTransitionType(int i) {
        nativeDisableTransition(this.mNativePtr, i);
    }

    public boolean isEnabledTransitionType(int i) {
        return nativeIsTransitionEnabled(this.mNativePtr, i);
    }

    public void setTransitionTypeDuration(int i, int i2) {
        nativeSetDuration(this.mNativePtr, i, i2);
    }

    public void setAnimatorProperty(int i, AnimatorProperty animatorProperty) {
        if (i == 0 || i == 1 || i == 2 || i == 3 || i == 4) {
            this.mAnimatorProperty[i] = animatorProperty;
            nativeSetViewPropertyAnimator(this.mNativePtr, i, animatorProperty);
            return;
        }
        throw new IllegalArgumentException("The type " + i + " is invalid");
    }

    public AnimatorProperty getAnimatorProperty(int i) {
        if (i == 0 || i == 1 || i == 2 || i == 3 || i == 4) {
            return this.mAnimatorProperty[i];
        }
        throw new IllegalArgumentException("The type " + i + " is invalid");
    }

    public long getNativePtr() {
        return this.mNativePtr;
    }
}
