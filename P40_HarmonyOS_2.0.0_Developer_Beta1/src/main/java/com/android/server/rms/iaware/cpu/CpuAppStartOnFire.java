package com.android.server.rms.iaware.cpu;

import android.iawareperf.UniPerf;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.cpu.CpuFeature;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.util.concurrent.atomic.AtomicBoolean;

public class CpuAppStartOnFire {
    private static final Object LOCK = new Object();
    private static final int RESET_ON_FIRE_DELAYED = 10000;
    private static final String TAG = "CpuAppStartOnFire";
    private static CpuAppStartOnFire sInstance;
    private CpuFeature.CpuFeatureHandler mCpuFeatureHandler;
    private AtomicBoolean mIsFeatureEnable = new AtomicBoolean(false);

    private CpuAppStartOnFire() {
    }

    public static CpuAppStartOnFire getInstance() {
        CpuAppStartOnFire cpuAppStartOnFire;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new CpuAppStartOnFire();
            }
            cpuAppStartOnFire = sInstance;
        }
        return cpuAppStartOnFire;
    }

    public void enable(CpuFeature.CpuFeatureHandler handler) {
        if (this.mIsFeatureEnable.get()) {
            AwareLog.e(TAG, "CpuAppStartOnFire has already enable!");
            return;
        }
        this.mCpuFeatureHandler = handler;
        this.mIsFeatureEnable.set(true);
    }

    public void disable() {
        if (!this.mIsFeatureEnable.get()) {
            AwareLog.e(TAG, "CpuAppStartOnFire has already disable!");
        } else {
            this.mIsFeatureEnable.set(false);
        }
    }

    public void setOnFire() {
        if (this.mIsFeatureEnable.get() && this.mCpuFeatureHandler != null) {
            UniPerf.getInstance().uniPerfEvent(4121, "", new int[]{0});
            this.mCpuFeatureHandler.removeMessages(CpuFeature.MSG_RESET_ON_FIRE);
            this.mCpuFeatureHandler.sendEmptyMessageDelayed(CpuFeature.MSG_RESET_ON_FIRE, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
        }
    }

    public void resetOnFire() {
        UniPerf.getInstance().uniPerfEvent(4121, "", new int[]{-1});
    }
}
