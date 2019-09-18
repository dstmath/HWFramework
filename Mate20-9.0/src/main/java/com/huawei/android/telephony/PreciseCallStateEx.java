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
        if (preciseCallStateEx == null) {
            return -1;
        }
        PreciseCallState callState = preciseCallStateEx.getCallState();
        if (callState != null) {
            return callState.getDisconnectCause();
        }
        return -1;
    }

    public static int getPreciseDisconnectCause(PreciseCallStateEx preciseCallStateEx) {
        if (preciseCallStateEx == null) {
            return -1;
        }
        PreciseCallState callState = preciseCallStateEx.getCallState();
        if (callState != null) {
            return callState.getPreciseDisconnectCause();
        }
        return -1;
    }

    private PreciseCallState getCallState() {
        return this.mCallState;
    }

    public static int getForegroundCallState(PreciseCallStateEx preciseCallStateEx) {
        if (preciseCallStateEx == null) {
            return -1;
        }
        PreciseCallState callState = preciseCallStateEx.getCallState();
        if (callState != null) {
            return callState.getForegroundCallState();
        }
        return -1;
    }

    public static int getRingingCallState(PreciseCallStateEx preciseCallStateEx) {
        if (preciseCallStateEx == null) {
            return -1;
        }
        PreciseCallState callState = preciseCallStateEx.getCallState();
        if (callState != null) {
            return callState.getRingingCallState();
        }
        return -1;
    }

    public static int getBackgroundCallState(PreciseCallStateEx preciseCallStateEx) {
        if (preciseCallStateEx == null) {
            return -1;
        }
        PreciseCallState callState = preciseCallStateEx.getCallState();
        if (callState != null) {
            return callState.getBackgroundCallState();
        }
        return -1;
    }
}
