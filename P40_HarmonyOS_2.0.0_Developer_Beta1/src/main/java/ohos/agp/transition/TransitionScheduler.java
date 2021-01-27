package ohos.agp.transition;

import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.components.ComponentContainer;
import ohos.agp.utils.MemoryCleaner;
import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.hiviewdfx.HiLogLabel;

public class TransitionScheduler {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_TRANSITION");
    private ITransitionEndListener mListener;
    protected final long mNativeTransitionSchedulerPtr = nativeGetTransitionSchedulerHandle();
    private Transition transition;

    public interface ITransitionEndListener {
        void onTransitionEnd();
    }

    private native long nativeGetTransitionSchedulerHandle();

    private native void nativeSetTransition(long j, long j2);

    private native void nativeSetTransitionEndListener(long j, ITransitionEndListener iTransitionEndListener);

    private native void nativeStartNewRootTransition(long j, long j2, long j3);

    private native void nativeStartTransition(long j, long j2);

    public TransitionScheduler() {
        MemoryCleanerRegistry.getInstance().registerWithNativeBind(this, new SchedulerCleaner(this.mNativeTransitionSchedulerPtr), this.mNativeTransitionSchedulerPtr);
    }

    public static class SchedulerCleaner implements MemoryCleaner {
        private long mNativePtr;

        private native void nativeSchedulerRelease(long j);

        SchedulerCleaner(long j) {
            this.mNativePtr = j;
        }

        @Override // ohos.agp.utils.MemoryCleaner
        public void run() {
            long j = this.mNativePtr;
            if (j != 0) {
                nativeSchedulerRelease(j);
                this.mNativePtr = 0;
            }
        }
    }

    public void setTransition(Transition transition2) {
        this.transition = transition2;
        nativeSetTransition(this.mNativeTransitionSchedulerPtr, transition2.getNativeTransitionPtr());
    }

    public void startTransition(TransitionComponents transitionComponents) {
        nativeStartTransition(this.mNativeTransitionSchedulerPtr, transitionComponents.getNativeTransitionViewsPtr());
    }

    public void startNewRootTransition(ComponentContainer componentContainer, ComponentContainer componentContainer2) {
        nativeStartNewRootTransition(this.mNativeTransitionSchedulerPtr, componentContainer.getNativeViewPtr(), componentContainer2.getNativeViewPtr());
    }

    public void setTransitionEndListener(ITransitionEndListener iTransitionEndListener) {
        this.mListener = iTransitionEndListener;
        nativeSetTransitionEndListener(this.mNativeTransitionSchedulerPtr, this.mListener);
    }
}
