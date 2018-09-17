package com.android.server.rms.iaware.cpu;

import android.iawareperf.UniPerf;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.cpu.CPUFeature.CPUFeatureHandler;
import java.util.concurrent.atomic.AtomicBoolean;

class CPUGameScene {
    private static final int GAME_SCENE_DELAYED = 3000;
    private static final String TAG = "CPUGameScene";
    private static CPUGameScene sInstance;
    private static Object syncObject = new Object();
    private CPUFeatureHandler mCPUFeatureHandler;
    private AtomicBoolean mIsFeatureEnable = new AtomicBoolean(false);
    private AtomicBoolean mSetGameScene = new AtomicBoolean(false);

    private CPUGameScene() {
    }

    public static CPUGameScene getInstance() {
        CPUGameScene cPUGameScene;
        synchronized (syncObject) {
            if (sInstance == null) {
                sInstance = new CPUGameScene();
            }
            cPUGameScene = sInstance;
        }
        return cPUGameScene;
    }

    public void enable(CPUFeatureHandler handler) {
        if (this.mIsFeatureEnable.get()) {
            AwareLog.e(TAG, "CPUGameScene has already enable!");
            return;
        }
        this.mCPUFeatureHandler = handler;
        this.mIsFeatureEnable.set(true);
    }

    public void disable() {
        if (this.mIsFeatureEnable.get()) {
            this.mIsFeatureEnable.set(false);
        } else {
            AwareLog.e(TAG, "CPUGameScene has already disable!");
        }
    }

    public void enterGameSceneMsg() {
        if (this.mIsFeatureEnable.get() && this.mCPUFeatureHandler != null) {
            this.mCPUFeatureHandler.removeMessages(CPUFeature.MSG_ENTER_GAME_SCENE);
            this.mCPUFeatureHandler.sendEmptyMessageDelayed(CPUFeature.MSG_ENTER_GAME_SCENE, 3000);
        }
    }

    public void exitGameSceneMsg() {
        if (this.mIsFeatureEnable.get() && this.mCPUFeatureHandler != null) {
            this.mCPUFeatureHandler.removeMessages(CPUFeature.MSG_EXIT_GAME_SCENE);
            if (this.mSetGameScene.get()) {
                this.mCPUFeatureHandler.sendEmptyMessageDelayed(CPUFeature.MSG_EXIT_GAME_SCENE, 0);
            } else {
                this.mCPUFeatureHandler.sendEmptyMessageDelayed(CPUFeature.MSG_EXIT_GAME_SCENE, 3000);
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

    public boolean isGameScene() {
        return this.mSetGameScene.get();
    }

    public void setScreenOffScene() {
        if (this.mIsFeatureEnable.get() && this.mSetGameScene.get()) {
            resetGameScene();
        }
    }
}
