package ohos.agp.transition;

import ohos.aafwk.utils.log.LogDomain;
import ohos.hiviewdfx.HiLogLabel;

public class TransitionSlide extends Transition {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_TRANSITION");

    private native long nativeGetTransitionHandle();

    private native long nativeGetTransitionSlideHandle(int i);

    private native void nativeSetTransitionDuration(long j, float f);

    /* access modifiers changed from: protected */
    @Override // ohos.agp.transition.Transition
    public void createTransitionNativePtr() {
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.transition.Transition
    public void registerTransitionCleaner() {
    }

    public TransitionSlide() {
        this.mNativeTransitionPtr = nativeGetTransitionHandle();
        super.registerTransitionCleaner();
    }

    public TransitionSlide(int i) {
        this.mNativeTransitionPtr = nativeGetTransitionSlideHandle(i);
        super.registerTransitionCleaner();
    }

    @Override // ohos.agp.transition.Transition
    public long getNativeTransitionPtr() {
        return this.mNativeTransitionPtr;
    }

    @Override // ohos.agp.transition.Transition
    public void setDuration(float f) {
        nativeSetTransitionDuration(this.mNativeTransitionPtr, f);
    }
}
