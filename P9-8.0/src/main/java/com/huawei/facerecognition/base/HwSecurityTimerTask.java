package com.huawei.facerecognition.base;

import com.huawei.facerecognition.base.HwSecurityTaskBase.TimerOutProc;
import java.util.TimerTask;

public class HwSecurityTimerTask extends TimerTask {
    private TimerOutProc mToProc;

    public boolean setTimeout(long delay, TimerOutProc toProc) {
        this.mToProc = toProc;
        HwSecurityTimer timer = HwSecurityTimer.getInstance();
        if (timer == null) {
            return false;
        }
        timer.schedule(this, delay);
        return true;
    }

    public void run() {
        HwSecurityTaskThread.staticPushTask(new HwSecurityTimeroutTask(this.mToProc), 1);
    }
}
