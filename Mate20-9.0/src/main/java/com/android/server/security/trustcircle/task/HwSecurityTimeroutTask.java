package com.android.server.security.trustcircle.task;

import com.android.server.security.trustcircle.task.HwSecurityTaskBase;

public class HwSecurityTimeroutTask extends HwSecurityTaskBase {
    private HwSecurityTaskBase.TimerOutProc mToProc;

    public HwSecurityTimeroutTask(HwSecurityTaskBase.TimerOutProc toProc) {
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
