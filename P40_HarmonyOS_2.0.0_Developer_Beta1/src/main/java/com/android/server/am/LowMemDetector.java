package com.android.server.am;

import com.android.internal.annotations.GuardedBy;

public final class LowMemDetector {
    public static final int MEM_PRESSURE_HIGH = 3;
    public static final int MEM_PRESSURE_LOW = 1;
    public static final int MEM_PRESSURE_MEDIUM = 2;
    public static final int MEM_PRESSURE_NONE = 0;
    private static final String TAG = "LowMemDetector";
    private final ActivityManagerService mAm;
    private boolean mAvailable;
    private final LowMemThread mLowMemThread;
    @GuardedBy({"mPressureStateLock"})
    private int mPressureState = 0;
    private final Object mPressureStateLock = new Object();

    private native int init();

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private native int waitForPressure();

    LowMemDetector(ActivityManagerService am) {
        this.mAm = am;
        this.mLowMemThread = new LowMemThread();
        if (init() != 0) {
            this.mAvailable = false;
            return;
        }
        this.mAvailable = true;
        this.mLowMemThread.start();
    }

    public boolean isAvailable() {
        return this.mAvailable;
    }

    public int getMemFactor() {
        int i;
        synchronized (this.mPressureStateLock) {
            i = this.mPressureState;
        }
        return i;
    }

    private final class LowMemThread extends Thread {
        private LowMemThread() {
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (true) {
                int newPressureState = LowMemDetector.this.waitForPressure();
                if (newPressureState == -1) {
                    LowMemDetector.this.mAvailable = false;
                    return;
                }
                synchronized (LowMemDetector.this.mPressureStateLock) {
                    LowMemDetector.this.mPressureState = newPressureState;
                }
            }
        }
    }
}
