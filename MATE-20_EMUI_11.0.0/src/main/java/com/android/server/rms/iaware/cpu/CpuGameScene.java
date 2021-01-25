package com.android.server.rms.iaware.cpu;

import android.iawareperf.UniPerf;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.cpu.CpuFeature;
import java.util.concurrent.atomic.AtomicBoolean;

public class CpuGameScene {
    private static final int GAME_SCENE_DELAYED = 3000;
    private static final Object SLOCK = new Object();
    private static final String TAG = "CpuGameScene";
    private static CpuGameScene sInstance;
    private CpuFeature.CpuFeatureHandler mCpuFeatureHandler;
    private AtomicBoolean mIsFeatureEnable = new AtomicBoolean(false);
    private AtomicBoolean mSetGameScene = new AtomicBoolean(false);

    private CpuGameScene() {
    }

    public static CpuGameScene getInstance() {
        CpuGameScene cpuGameScene;
        synchronized (SLOCK) {
            if (sInstance == null) {
                sInstance = new CpuGameScene();
            }
            cpuGameScene = sInstance;
        }
        return cpuGameScene;
    }

    public void enable(CpuFeature.CpuFeatureHandler handler) {
        if (this.mIsFeatureEnable.get()) {
            AwareLog.e(TAG, "CpuGameScene has already enable!");
            return;
        }
        this.mCpuFeatureHandler = handler;
        this.mIsFeatureEnable.set(true);
    }

    public void disable() {
        if (!this.mIsFeatureEnable.get()) {
            AwareLog.e(TAG, "CpuGameScene has already disable!");
        } else {
            this.mIsFeatureEnable.set(false);
        }
    }

    public void enterGameSceneMsg() {
        CpuFeature.CpuFeatureHandler cpuFeatureHandler;
        if (this.mIsFeatureEnable.get() && (cpuFeatureHandler = this.mCpuFeatureHandler) != null) {
            cpuFeatureHandler.removeMessages(CpuFeature.MSG_ENTER_GAME_SCENE);
            this.mCpuFeatureHandler.sendEmptyMessageDelayed(CpuFeature.MSG_ENTER_GAME_SCENE, 3000);
        }
    }

    public void exitGameSceneMsg() {
        CpuFeature.CpuFeatureHandler cpuFeatureHandler;
        if (this.mIsFeatureEnable.get() && (cpuFeatureHandler = this.mCpuFeatureHandler) != null) {
            cpuFeatureHandler.removeMessages(CpuFeature.MSG_EXIT_GAME_SCENE);
            if (this.mSetGameScene.get()) {
                this.mCpuFeatureHandler.sendEmptyMessageDelayed(CpuFeature.MSG_EXIT_GAME_SCENE, 0);
            } else {
                this.mCpuFeatureHandler.sendEmptyMessageDelayed(CpuFeature.MSG_EXIT_GAME_SCENE, 3000);
            }
        }
    }

    public void setGameScene() {
        UniPerf.getInstance().uniPerfEvent(4120, "", new int[]{0});
        this.mSetGameScene.set(true);
    }

    public void resetGameScene() {
        UniPerf.getInstance().uniPerfEvent(4120, "", new int[]{-1});
        this.mSetGameScene.set(false);
    }

    public void setScreenOffScene() {
        if (this.mIsFeatureEnable.get() && this.mSetGameScene.get()) {
            resetGameScene();
        }
    }
}
