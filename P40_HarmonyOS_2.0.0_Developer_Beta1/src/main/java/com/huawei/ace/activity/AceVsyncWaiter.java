package com.huawei.ace.activity;

import android.util.Log;
import android.view.Choreographer;
import android.view.WindowManager;

public class AceVsyncWaiter {
    private static final Object LOCK = new Object();
    private static final String TAG = "AceVsyncWaiter";
    private static AceVsyncWaiter instance;
    private static WindowManager windowManager;

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native void nativeOnVsync(long j, long j2, long j3);

    public static AceVsyncWaiter getInstance(WindowManager windowManager2) {
        AceVsyncWaiter aceVsyncWaiter;
        synchronized (LOCK) {
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
                if (AceVsyncWaiter.windowManager == null) {
                    Log.e(AceVsyncWaiter.TAG, "windowManager is null");
                } else {
                    AceVsyncWaiter.getInstance(AceVsyncWaiter.windowManager).nativeOnVsync(j, j + ((long) (1.0E9d / ((double) AceVsyncWaiter.windowManager.getDefaultDisplay().getRefreshRate()))), j);
                }
            }
        });
    }

    private AceVsyncWaiter() {
    }
}
