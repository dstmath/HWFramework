package com.android.internal.telephony;

import android.os.Message;

public class HwCustGsmCdmaCallTracker {
    public static final int BUSY_REJECT_CAUSE = 0;
    public static final int NO_REJECT_CAUSE = -1;
    public static final int USER_REJECT_CAUSE = 1;

    public void rejectCallForCause(CommandsInterface ci, GsmCdmaCall ringCall, Message message) {
    }

    public int getRejectCallCause(GsmCdmaCall ringCall) {
        return -1;
    }
}
