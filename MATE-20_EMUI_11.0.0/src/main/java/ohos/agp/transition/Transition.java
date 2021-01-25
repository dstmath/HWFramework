package ohos.agp.transition;

import ohos.aafwk.utils.log.LogDomain;
import ohos.hiviewdfx.HiLogLabel;

public class Transition {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_TRANSITION");
    protected long mNativeTransitionPtr;

    private native long nativeGetTransitionHandle();

    private native void nativeSetTransitionDuration(long j, float f);

    public Transition() {
        this.mNativeTransitionPtr = 0;
        this.mNativeTransitionPtr = nativeGetTransitionHandle();
    }

    public long getNativeTransitionPtr() {
        return this.mNativeTransitionPtr;
    }

    public void setTransitionDuration(float f) {
        nativeSetTransitionDuration(this.mNativeTransitionPtr, f);
    }
}
