package huawei.android.security.facerecognition.base;

import huawei.android.security.facerecognition.base.HwSecurityTaskBase;

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
