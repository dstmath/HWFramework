package com.android.internal.telephony.imsphone;

import android.content.Context;
import android.telephony.ims.ImsReasonInfo;
import com.android.ims.ImsCall;
import com.android.ims.ImsException;

public class HwCustImsPhoneCallTracker {
    public static final int BUSY_REJECT_CAUSE = 0;
    public static final int NO_REJECT_CAUSE = -1;
    public static final int USER_REJECT_CAUSE = 1;

    public HwCustImsPhoneCallTracker(Context context) {
    }

    public boolean checkImsRegistered() {
        return true;
    }

    public void addSipErrorPopup(ImsReasonInfo reasonInfo, Context context) {
    }

    public int getDisconnectCauseFromReasonInfo(ImsReasonInfo reasonInfo) {
        return 36;
    }

    public boolean isForkedCallLoggingEnabled() {
        return false;
    }

    public void handleCallDropErrors(ImsReasonInfo reasonInfo) {
    }

    public void rejectCallForCause(ImsCall imsCall) throws ImsException {
    }

    public int getRejectCallCause(ImsPhoneCall ringCall) {
        return -1;
    }

    public void markCallRejectCause(String telecomCallId, int cause) {
    }
}
