package com.android.server.rms.iaware.cpu;

import android.iawareperf.UniPerf;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.cpu.CPUFeature.CPUFeatureHandler;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.util.concurrent.atomic.AtomicBoolean;

class CPUAppStartOnFire {
    private static final int RESET_ON_FIRE_DELAYED = 10000;
    private static final String TAG = "CPUAppStartOnFire";
    private static CPUAppStartOnFire sInstance;
    private static final Object syncObject = new Object();
    private CPUFeatureHandler mCPUFeatureHandler;
    private AtomicBoolean mIsFeatureEnable = new AtomicBoolean(false);

    private CPUAppStartOnFire() {
    }

    public static CPUAppStartOnFire getInstance() {
        CPUAppStartOnFire cPUAppStartOnFire;
        synchronized (syncObject) {
            if (sInstance == null) {
                sInstance = new CPUAppStartOnFire();
            }
            cPUAppStartOnFire = sInstance;
        }
        return cPUAppStartOnFire;
    }

    public void enable(CPUFeatureHandler handler) {
        if (this.mIsFeatureEnable.get()) {
            AwareLog.e(TAG, "CPUAppStartOnFire has already enable!");
            return;
        }
        this.mCPUFeatureHandler = handler;
        this.mIsFeatureEnable.set(true);
    }

    public void disable() {
        if (this.mIsFeatureEnable.get()) {
            this.mIsFeatureEnable.set(false);
        } else {
            AwareLog.e(TAG, "CPUAppStartOnFire has already disable!");
        }
    }

    public void setOnFire() {
        if (this.mIsFeatureEnable.get() && this.mCPUFeatureHandler != null) {
            UniPerf.getInstance().uniPerfEvent(4121, "", new int[]{0});
            this.mCPUFeatureHandler.removeMessages(CPUFeature.MSG_RESET_ON_FIRE);
            this.mCPUFeatureHandler.sendEmptyMessageDelayed(CPUFeature.MSG_RESET_ON_FIRE, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
        }
    }

    public void resetOnFire() {
        UniPerf.getInstance().uniPerfEvent(4121, "", new int[]{-1});
    }
}
