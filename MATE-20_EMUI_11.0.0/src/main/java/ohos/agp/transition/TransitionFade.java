package ohos.agp.transition;

import ohos.aafwk.utils.log.LogDomain;
import ohos.hiviewdfx.HiLogLabel;

public class TransitionFade extends Transition {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_TRANSITION");

    private native long nativeGetTransitionHandle();

    private native void nativeSetTransitionDuration(long j, float f);

    public TransitionFade() {
        this.mNativeTransitionPtr = nativeGetTransitionHandle();
    }

    @Override // ohos.agp.transition.Transition
    public long getNativeTransitionPtr() {
        return this.mNativeTransitionPtr;
    }

    @Override // ohos.agp.transition.Transition
    public void setTransitionDuration(float f) {
        nativeSetTransitionDuration(this.mNativeTransitionPtr, f);
    }
}
