package com.android.server.rms.iaware.memory.utils;

import android.rms.iaware.AwareLog;
import com.android.internal.annotations.GuardedBy;
import com.android.server.rms.collector.ResourceCollector;

public final class PressureDetector {
    public static final int PRESSURE_HIGH = 3;
    public static final int PRESSURE_LOW = 1;
    public static final int PRESSURE_MEDIUM = 2;
    public static final int PRESSURE_NONE = 0;
    public static final int PRESSURE_NUM = 3;
    public static final int PSI_FULL = 1;
    public static final int PSI_RES_CPU = 2;
    public static final int PSI_RES_IO = 0;
    public static final int PSI_RES_MEM = 1;
    public static final int PSI_RES_TOTAL = 3;
    public static final int PSI_SOME = 0;
    public static final int PSI_THRESHOLD_MAX = 1000000;
    public static final int RES_SWAP = 3;
    private static final String TAG = "PressureDetector";
    private volatile boolean mAvailable = false;
    private long mDetector = 0;
    private int[] mPressureState = {0, 0, 0, 0};
    private PressureThread mPressureThread = null;
    @GuardedBy({"mPsiStateLock"})
    private int[] mPsiState = {0, 0, 0};
    private final Object mPsiStateLock = new Object();

    /* access modifiers changed from: private */
    public static class PressureDetectorHolder {
        private static final PressureDetector SINGLE_INSTANCE = new PressureDetector();

        private PressureDetectorHolder() {
        }
    }

    public static PressureDetector getInstance() {
        return PressureDetectorHolder.SINGLE_INSTANCE;
    }

    public boolean isAvailable() {
        return this.mAvailable;
    }

    public boolean isPressureHigh(int res) {
        return this.mPressureState[res] >= 2;
    }

    private boolean createPressureDetector() {
        this.mDetector = ResourceCollector.getPressureDetector();
        return this.mDetector != 0;
    }

    public int registPressure(int res, int type, int threshold, int level) {
        if (this.mDetector == 0 && !createPressureDetector()) {
            return -1;
        }
        if (!this.mAvailable) {
            return ResourceCollector.registPressureThreshold(this.mDetector, new int[]{res, type, threshold, level});
        }
        return 1;
    }

    public void startPressureDetect() {
        if (this.mDetector != 0 || createPressureDetector()) {
            PressureThread pressureThread = this.mPressureThread;
            if (pressureThread == null || !pressureThread.isAlive()) {
                this.mAvailable = true;
                this.mPressureThread = new PressureThread();
                this.mPressureThread.start();
                AwareLog.i(TAG, "startPressureDetect, set mAvailable as true.");
            }
        }
    }

    private int getSwapPressure() {
        if (MemoryReader.getInstance().updateSwapInfo() != 0) {
            AwareLog.e(TAG, "updateSwapInfo fail, return high");
            return 3;
        }
        long swapFreeMB = MemoryReader.getInstance().getSwapFree() / 1024;
        if (MemoryReader.getInstance().getAnonSize() / 1024 <= ((long) MemoryConstant.getKernCompressAnonTarget()) || swapFreeMB <= MemoryConstant.getSwapHighThreshold()) {
            return 3;
        }
        if (swapFreeMB <= MemoryConstant.getSwapMediumThreshold()) {
            return 2;
        }
        if (swapFreeMB <= MemoryConstant.getSwapLowThreshold()) {
            return 1;
        }
        return 0;
    }

    public void updateSystemPressure() {
        this.mPressureState[3] = getSwapPressure();
        synchronized (this.mPsiStateLock) {
            System.arraycopy(this.mPsiState, 0, this.mPressureState, 0, this.mPsiState.length);
        }
        AwareLog.d(TAG, "mMemPressure = " + this.mPressureState[1] + ", mCpuPressure = " + this.mPressureState[2] + ", mIoPressure = " + this.mPressureState[0] + ", mSwapPressure = " + this.mPressureState[3]);
    }

    /* access modifiers changed from: private */
    public final class PressureThread extends Thread {
        public PressureThread() {
            super.setName("PressureThread");
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            while (true) {
                int[] newPsiState = ResourceCollector.waitForPressure(PressureDetector.this.mDetector);
                if (newPsiState == null) {
                    AwareLog.e(PressureDetector.TAG, "waitForPressure error!!");
                    synchronized (PressureDetector.this.mPsiStateLock) {
                        PressureDetector.this.mPsiState = new int[]{0, 0, 0};
                    }
                    PressureDetector.this.mAvailable = false;
                    return;
                }
                synchronized (PressureDetector.this.mPsiStateLock) {
                    System.arraycopy(newPsiState, 0, PressureDetector.this.mPsiState, 0, PressureDetector.this.mPsiState.length);
                }
            }
        }
    }
}
