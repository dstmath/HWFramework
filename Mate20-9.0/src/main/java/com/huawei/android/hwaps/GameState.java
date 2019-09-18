package com.huawei.android.hwaps;

import android.app.HwApsInterface;

public class GameState {
    public void setGamePid(int pid) {
        HwApsInterface.nativeSetGamePid(pid);
    }

    public int getResultJudgedByFps() {
        return 0;
    }

    public int getGameRoundDuration() {
        return 0;
    }

    public void setNonplayFrame(int nonplayFps) {
    }
}
