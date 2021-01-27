package com.huawei.android.telephony;

import android.telephony.PreciseCallState;

public class PreciseCallStateEx {
    public static final int PRECISE_CALL_STATE_ACTIVE = 1;
    public static final int PRECISE_CALL_STATE_ALERTING = 4;
    public static final int PRECISE_CALL_STATE_DIALING = 3;
    public static final int PRECISE_CALL_STATE_DISCONNECTED = 7;
    public static final int PRECISE_CALL_STATE_IDLE = 0;
    public static final int PRECISE_CALL_STATE_NOT_VALID = -1;
    public static final int PRECISE_CALL_STATE_WAITING = 6;
    private PreciseCallState mCallState;

    public PreciseCallStateEx(PreciseCallState callState) {
        this.mCallState = callState;
    }

    public static int getDisconnectCause(PreciseCallStateEx preciseCallStateEx) {
        PreciseCallState callState;
        if (preciseCallStateEx == null || (callState = preciseCallStateEx.getCallState()) == null) {
            return -1;
        }
        return callState.getDisconnectCause();
    }

    public static int getPreciseDisconnectCause(PreciseCallStateEx preciseCallStateEx) {
        PreciseCallState callState;
        if (preciseCallStateEx == null || (callState = preciseCallStateEx.getCallState()) == null) {
            return -1;
        }
        return callState.getPreciseDisconnectCause();
    }

    public static int getForegroundCallState(PreciseCallStateEx preciseCallStateEx) {
        PreciseCallState callState;
        if (preciseCallStateEx == null || (callState = preciseCallStateEx.getCallState()) == null) {
            return -1;
        }
        return callState.getForegroundCallState();
    }

    public static int getRingingCallState(PreciseCallStateEx preciseCallStateEx) {
        PreciseCallState callState;
        if (preciseCallStateEx == null || (callState = preciseCallStateEx.getCallState()) == null) {
            return -1;
        }
        return callState.getRingingCallState();
    }

    public static int getBackgroundCallState(PreciseCallStateEx preciseCallStateEx) {
        PreciseCallState callState;
        if (preciseCallStateEx == null || (callState = preciseCallStateEx.getCallState()) == null) {
            return -1;
        }
        return callState.getBackgroundCallState();
    }

    private PreciseCallState getCallState() {
        return this.mCallState;
    }
}
