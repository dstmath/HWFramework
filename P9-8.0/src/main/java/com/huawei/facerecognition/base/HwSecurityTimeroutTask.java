package com.huawei.facerecognition.base;

import com.huawei.facerecognition.base.HwSecurityTaskBase.TimerOutProc;

public class HwSecurityTimeroutTask extends HwSecurityTaskBase {
    private TimerOutProc mToProc;

    public HwSecurityTimeroutTask(TimerOutProc toProc) {
        super(null, null);
        this.mToProc = toProc;
    }

    public int doAction() {
        if (this.mToProc != null) {
            this.mToProc.onTimerOut();
        }
        return 0;
    }
}
