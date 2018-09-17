package com.android.server.rms.iaware.cpu;

import android.iawareperf.UniPerf;
import android.os.Bundle;
import android.rms.iaware.AwareLog;
import java.util.concurrent.atomic.AtomicBoolean;

class CPUGesturePointerBoost {
    private static final String ITEM_MOVE_BOOST_DIF = "move_boost_dif";
    private static final String TAG = "CPUGesturePointerBoost";
    private static final int UNIPERF_EVENT_GESTURE_POINTER_BOOST = 4117;
    private static CPUGesturePointerBoost sInstance;
    private static Object syncObject = new Object();
    private AtomicBoolean mIsFeatureEnable = new AtomicBoolean(false);
    private AtomicBoolean mIsGameScene = new AtomicBoolean(false);
    private int mMoveBoostDif = 10;

    private CPUGesturePointerBoost() {
        initMoveBoostDif();
    }

    private void initMoveBoostDif() {
        int moveBoostDif = new GesturePointerBoostConfig().getIntXmlValue(ITEM_MOVE_BOOST_DIF);
        if (moveBoostDif != -1) {
            this.mMoveBoostDif = moveBoostDif;
        }
    }

    public static CPUGesturePointerBoost getInstance() {
        CPUGesturePointerBoost cPUGesturePointerBoost;
        synchronized (syncObject) {
            if (sInstance == null) {
                sInstance = new CPUGesturePointerBoost();
            }
            cPUGesturePointerBoost = sInstance;
        }
        return cPUGesturePointerBoost;
    }

    public void enable() {
        if (this.mIsFeatureEnable.get()) {
            AwareLog.e(TAG, "CPUGesturePointerBoost has already enable!");
        } else {
            this.mIsFeatureEnable.set(true);
        }
    }

    public void disable() {
        if (this.mIsFeatureEnable.get()) {
            this.mIsFeatureEnable.set(false);
        } else {
            AwareLog.e(TAG, "CPUGesturePointerBoost has already disable!");
        }
    }

    public void enterGameScene() {
        if (this.mIsFeatureEnable.get() && (this.mIsGameScene.get() ^ 1) != 0) {
            this.mIsGameScene.set(true);
        }
    }

    public void exitGameScene() {
        if (this.mIsFeatureEnable.get() && this.mIsGameScene.get()) {
            this.mIsGameScene.set(false);
        }
    }

    public void doGesturePointerBoost(Bundle bundleArgs) {
        if (this.mIsFeatureEnable.get() && bundleArgs != null) {
            int moveDif = bundleArgs.getInt("movedif", 0);
            if (!this.mIsGameScene.get() && moveDif <= this.mMoveBoostDif) {
                UniPerf.getInstance().uniPerfEvent(UNIPERF_EVENT_GESTURE_POINTER_BOOST, "", new int[0]);
            }
        }
    }
}
