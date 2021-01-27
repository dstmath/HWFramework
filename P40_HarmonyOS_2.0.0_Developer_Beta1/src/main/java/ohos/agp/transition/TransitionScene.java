package ohos.agp.transition;

import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.components.ComponentContainer;
import ohos.hiviewdfx.HiLogLabel;

public class TransitionScene {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_TRANSITION");
    protected final long mNativeTransitionScenePtr;

    private native long nativeGetTransitionSceneHandle(long j, long j2);

    public TransitionScene(ComponentContainer componentContainer, ComponentContainer componentContainer2) {
        this.mNativeTransitionScenePtr = nativeGetTransitionSceneHandle(componentContainer.getNativeViewPtr(), componentContainer2.getNativeViewPtr());
    }

    public long getNativeTransitionScenePtr() {
        return this.mNativeTransitionScenePtr;
    }
}
