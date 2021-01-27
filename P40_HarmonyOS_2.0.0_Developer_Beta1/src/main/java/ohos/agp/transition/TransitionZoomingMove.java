package ohos.agp.transition;

import ohos.aafwk.utils.log.LogDomain;
import ohos.hiviewdfx.HiLogLabel;

public class TransitionZoomingMove extends Transition {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_TRANSITION");

    private native long nativeGetTransitionHandle(int i);

    private native void nativeSetTransitionDuration(long j, float f);

    /* access modifiers changed from: protected */
    @Override // ohos.agp.transition.Transition
    public void createTransitionNativePtr() {
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.transition.Transition
    public void registerTransitionCleaner() {
    }

    public TransitionZoomingMove() {
        this(1);
    }

    public TransitionZoomingMove(int i) {
        this.mNativeTransitionPtr = nativeGetTransitionHandle(i);
        super.registerTransitionCleaner();
    }

    @Override // ohos.agp.transition.Transition
    public void setDuration(float f) {
        nativeSetTransitionDuration(this.mNativeTransitionPtr, f);
    }
}
