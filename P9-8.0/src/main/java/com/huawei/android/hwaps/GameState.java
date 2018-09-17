package com.huawei.android.hwaps;

import android.app.HwApsInterface;

public class GameState {
    public void setGamePid(int pid) {
        HwApsInterface.nativeSetGamePid(pid);
    }

    public void setTouchState(int state) {
        HwApsInterface.nativeSetTouchState(state);
    }

    public int getOpenglGameType() {
        return HwApsInterface.nativeGetOpenglGameType();
    }

    public void setOpenglGameType(int type) {
        HwApsInterface.nativeSetOpenglGameType(type);
    }

    public void setTouchGameType(int type) {
        HwApsInterface.nativeSetTouchGameType(type);
    }

    public void setApsVersion(String version) {
        HwApsInterface.nativeSetApsVersion(version);
    }

    public int getResultJudgedByFps() {
        return HwApsInterface.nativeGetResultJudgedByFps();
    }

    public int getGameRoundDuration() {
        return HwApsInterface.nativeGetGameRoundDuration();
    }

    public void setNonplayFrame(int nonplayFps) {
        HwApsInterface.nativeSetNonplayFrame(nonplayFps);
    }

    public boolean isDepthGame() {
        return 1 == HwApsInterface.nativeIsDepthGame();
    }

    public void setSceneFps(String name, int id, int fps) {
        HwApsInterface.nativeSetSceneFps(name, id, fps);
    }

    public void setSceneRatio(String name, int id, double ratio) {
        HwApsInterface.nativeSetSceneRatio(name, id, ratio);
    }

    public void setCtrlBattery(String name, int ctrlBattery) {
        HwApsInterface.nativeSetCtrlBattery(name, ctrlBattery);
    }

    public void setSceneFixed(String name, boolean isSceneFixed) {
        HwApsInterface.nativeSetSceneFixed(name, isSceneFixed ? 1 : 0);
    }

    public void setPowerKitFrame(int powerkitFrame) {
        HwApsInterface.nativeSetPowerKitFrame(powerkitFrame);
    }
}
