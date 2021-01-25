package ohos.agp.components;

import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.utils.MemoryCleaner;
import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.multimodalinput.event.TouchEvent;

public final class VelocityDetector {
    private static final int ACTIVE_POINTER_ID = -1;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_COMPONENT");
    private long mNativePtr = nativeGetVelocityDetectorHandle();

    private native long nativeGetVelocityDetectorHandle();

    private native void nativeVelocityDetectorAddMovement(long j, float f, float f2);

    private native void nativeVelocityDetectorCalculateCurrentVelocity(long j, int i, float f, float f2);

    private native void nativeVelocityDetectorClear(long j);

    private native float nativeVelocityDetectorGetXVelocity(long j, int i);

    private native float nativeVelocityDetectorGetYVelocity(long j, int i);

    public static VelocityDetector obtain() {
        return new VelocityDetector();
    }

    private VelocityDetector() {
        MemoryCleanerRegistry.getInstance().register(this, new VelocityDetectorCleaner(this.mNativePtr));
    }

    protected static class VelocityDetectorCleaner implements MemoryCleaner {
        private long mNativePtr;

        private native void nativeVelocityDetectorDispose(long j);

        VelocityDetectorCleaner(long j) {
            this.mNativePtr = j;
        }

        @Override // ohos.agp.utils.MemoryCleaner
        public void run() {
            long j = this.mNativePtr;
            if (j != 0) {
                nativeVelocityDetectorDispose(j);
                this.mNativePtr = 0;
            }
        }
    }

    public void clear() {
        nativeVelocityDetectorClear(this.mNativePtr);
    }

    public void addMovement(TouchEvent touchEvent) {
        if (touchEvent == null) {
            HiLog.error(TAG, "event must not be null.", new Object[0]);
        } else {
            nativeVelocityDetectorAddMovement(this.mNativePtr, touchEvent.getPointerPosition(touchEvent.getIndex()).getX(), touchEvent.getPointerPosition(touchEvent.getIndex()).getY());
        }
    }

    public void calculateCurrentVelocity(int i) {
        nativeVelocityDetectorCalculateCurrentVelocity(this.mNativePtr, i, Float.MAX_VALUE, Float.MAX_VALUE);
    }

    public void calculateCurrentVelocity(int i, float f, float f2) {
        nativeVelocityDetectorCalculateCurrentVelocity(this.mNativePtr, i, f, f2);
    }

    public float getXVelocity() {
        return nativeVelocityDetectorGetXVelocity(this.mNativePtr, -1);
    }

    public float getYVelocity() {
        return nativeVelocityDetectorGetYVelocity(this.mNativePtr, -1);
    }
}
