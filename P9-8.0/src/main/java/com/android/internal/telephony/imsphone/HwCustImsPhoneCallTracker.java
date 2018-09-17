package com.android.internal.telephony.imsphone;

import android.content.Context;
import com.android.ims.ImsReasonInfo;

public class HwCustImsPhoneCallTracker {
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
}
