package ohos.agp.components;

import ohos.agp.animation.AnimatorProperty;
import ohos.agp.utils.MemoryCleaner;
import ohos.agp.utils.MemoryCleanerRegistry;

public class ComponentTransition {
    public static final int CHANGING = 4;
    public static final int OTHERS_GONE = 3;
    public static final int OTHERS_SHOW = 2;
    public static final int SELF_GONE = 1;
    public static final int SELF_SHOW = 0;
    private final AnimatorProperty[] mAnimatorProperty = {null, null, null, null, null};
    protected long mNativePtr = 0;

    private native void nativeDisableTransition(long j, int i);

    private native void nativeEnableTransition(long j, int i);

    private native long nativeGetComponentTransitionHandle();

    private native boolean nativeIsTransitionEnabled(long j, int i);

    private native void nativeSetDuration(long j, int i, int i2);

    private native void nativeSetViewPropertyAnimator(long j, int i, AnimatorProperty animatorProperty);

    public void release() {
    }

    protected static class ComponentTransitionCleaner implements MemoryCleaner {
        private long mNativePtr;

        private native void nativeRelease(long j);

        ComponentTransitionCleaner(long j) {
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

    public ComponentTransition() {
        createNativePtr();
        MemoryCleanerRegistry.getInstance().register(this, new ComponentTransitionCleaner(this.mNativePtr));
    }

    /* access modifiers changed from: protected */
    public void createNativePtr() {
        if (this.mNativePtr == 0) {
            this.mNativePtr = nativeGetComponentTransitionHandle();
        }
    }

    public void addTransitionType(int i) {
        nativeEnableTransition(this.mNativePtr, i);
    }

    public void removeTransitionType(int i) {
        nativeDisableTransition(this.mNativePtr, i);
    }

    public boolean hasTransitionType(int i) {
        return nativeIsTransitionEnabled(this.mNativePtr, i);
    }

    public void setTransitionTypeDuration(int i, int i2) {
        nativeSetDuration(this.mNativePtr, i, i2);
    }

    public void setAnimatorProperty(int i, AnimatorProperty animatorProperty) {
        if (i == 2 || i == 3 || i == 0 || i == 1 || i == 4) {
            this.mAnimatorProperty[i] = animatorProperty;
            nativeSetViewPropertyAnimator(this.mNativePtr, i, animatorProperty);
            return;
        }
        throw new IllegalArgumentException("The type " + i + " is invalid");
    }

    public AnimatorProperty getAnimatorProperty(int i) {
        if (i == 2 || i == 3 || i == 0 || i == 1 || i == 4) {
            return this.mAnimatorProperty[i];
        }
        throw new IllegalArgumentException("The type " + i + " is invalid");
    }

    public long getNativePtr() {
        return this.mNativePtr;
    }
}
