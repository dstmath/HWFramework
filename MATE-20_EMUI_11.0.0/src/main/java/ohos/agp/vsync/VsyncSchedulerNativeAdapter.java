package ohos.agp.vsync;

import ohos.agp.vsync.VsyncScheduler;

public class VsyncSchedulerNativeAdapter {
    /* access modifiers changed from: private */
    public static native void nativeOnVsync(long j, long j2);

    private static void requestVsync(final long j) {
        VsyncScheduler.getInstance().requestVsync(new VsyncScheduler.FrameCallback() {
            /* class ohos.agp.vsync.VsyncSchedulerNativeAdapter.AnonymousClass1 */

            @Override // ohos.agp.vsync.VsyncScheduler.FrameCallback
            public void doFrame(long j) {
                VsyncSchedulerNativeAdapter.nativeOnVsync(j, j);
            }
        });
    }
}
