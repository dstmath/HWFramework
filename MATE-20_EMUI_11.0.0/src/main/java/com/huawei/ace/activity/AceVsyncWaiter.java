package com.huawei.ace.activity;

import android.view.Choreographer;
import android.view.WindowManager;

public class AceVsyncWaiter {
    private static AceVsyncWaiter instance;
    private static WindowManager windowManager;

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void nativeOnVsync(long j, long j2, long j3);

    public static AceVsyncWaiter getInstance(WindowManager windowManager2) {
        AceVsyncWaiter aceVsyncWaiter;
        synchronized (AceVsyncWaiter.class) {
            if (instance == null) {
                windowManager = windowManager2;
                instance = new AceVsyncWaiter();
            }
            aceVsyncWaiter = instance;
        }
        return aceVsyncWaiter;
    }

    public static void waitForVsync(final long j) {
        Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() {
            /* class com.huawei.ace.activity.AceVsyncWaiter.AnonymousClass1 */

            @Override // android.view.Choreographer.FrameCallback
            public void doFrame(long j) {
                AceVsyncWaiter.getInstance(AceVsyncWaiter.windowManager).nativeOnVsync(j, j + ((long) (1.0E9d / ((double) AceVsyncWaiter.windowManager.getDefaultDisplay().getRefreshRate()))), j);
            }
        });
    }

    private AceVsyncWaiter() {
    }
}
