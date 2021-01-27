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
    private final long mNativePtr = nativeGetHandle();

    private native void nativeAddMovement(long j, float f, float f2);

    private native void nativeCalculateCurrentVelocity(long j, int i, float f, float f2);

    private native void nativeClear(long j);

    private native long nativeGetHandle();

    private native float nativeGetXVelocity(long j, int i);

    private native float nativeGetYVelocity(long j, int i);

    public static VelocityDetector obtainInstance() {
        return new VelocityDetector();
    }

    private VelocityDetector() {
        MemoryCleanerRegistry.getInstance().register(this, new VelocityDetectorCleaner(this.mNativePtr));
    }

    protected static class VelocityDetectorCleaner implements MemoryCleaner {
        private long mNativePtr;

        private native void nativeDispose(long j);

        VelocityDetectorCleaner(long j) {
            this.mNativePtr = j;
        }

        @Override // ohos.agp.utils.MemoryCleaner
        public void run() {
            long j = this.mNativePtr;
            if (j != 0) {
                nativeDispose(j);
                this.mNativePtr = 0;
            }
        }
    }

    public void clear() {
        nativeClear(this.mNativePtr);
    }

    public void addEvent(TouchEvent touchEvent) {
        if (touchEvent == null) {
            HiLog.error(TAG, "event must not be null.", new Object[0]);
        } else {
            nativeAddMovement(this.mNativePtr, touchEvent.getPointerPosition(touchEvent.getIndex()).getX(), touchEvent.getPointerPosition(touchEvent.getIndex()).getY());
        }
    }

    public void calculateCurrentVelocity(int i) {
        nativeCalculateCurrentVelocity(this.mNativePtr, i, Float.MAX_VALUE, Float.MAX_VALUE);
    }

    public void calculateCurrentVelocity(int i, float f, float f2) {
        nativeCalculateCurrentVelocity(this.mNativePtr, i, f, f2);
    }

    public float getHorizontalVelocity() {
        return nativeGetXVelocity(this.mNativePtr, -1);
    }

    public float getVerticalVelocity() {
        return nativeGetYVelocity(this.mNativePtr, -1);
    }

    public float[] getVelocity() {
        return new float[]{nativeGetXVelocity(this.mNativePtr, -1), nativeGetYVelocity(this.mNativePtr, -1)};
    }
}
