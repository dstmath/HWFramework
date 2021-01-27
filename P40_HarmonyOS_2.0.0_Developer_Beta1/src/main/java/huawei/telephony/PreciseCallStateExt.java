package huawei.telephony;

import android.telephony.PreciseCallState;

public class PreciseCallStateExt {
    public static final int PRECISE_CALL_STATE_ACTIVE = 1;
    public static final int PRECISE_CALL_STATE_NOT_VALID = -1;
    private PreciseCallState mPreciseCallState;

    public static PreciseCallStateExt getPreciseCallStateExt(Object preciseCallState) {
        PreciseCallStateExt preciseCallStateExt = new PreciseCallStateExt();
        if (preciseCallState instanceof PreciseCallState) {
            preciseCallStateExt.setPreciseCallState((PreciseCallState) preciseCallState);
        }
        return preciseCallStateExt;
    }

    public void setPreciseCallState(PreciseCallState preciseCallState) {
        this.mPreciseCallState = preciseCallState;
    }

    public int getForegroundCallState() {
        PreciseCallState preciseCallState = this.mPreciseCallState;
        if (preciseCallState == null) {
            return -1;
        }
        return preciseCallState.getForegroundCallState();
    }
}
