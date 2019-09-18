package com.android.internal.telephony;

import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;

public class HwCustSMSDispatcherImpl extends HwCustSMSDispatcher {
    private static final String TAG = "HwCustSMSDispatcherImpl";

    private void log(String message) {
        Rlog.d(TAG, message);
    }

    public boolean isBlockMsgSending(int subId) {
        return isBlockMsgSendingByNonAis(subId);
    }

    private boolean isBlockMsgSendingByNonAis(int subId) {
        if (!HwTelephonyManagerInner.getDefault().isCustomAis() || HwTelephonyManagerInner.getDefault().isAISCard(subId) || HwTelephonyManagerInner.getDefault().isAisCustomDisable()) {
            return false;
        }
        log("ais custom version but not ais card. block msg send.");
        return true;
    }
}
