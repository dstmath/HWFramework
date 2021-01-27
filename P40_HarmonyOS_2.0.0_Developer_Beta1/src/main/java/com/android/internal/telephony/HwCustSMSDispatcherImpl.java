package com.android.internal.telephony;

import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;

public class HwCustSMSDispatcherImpl extends HwCustSMSDispatcher {
    private static final String TAG = "HwCustSMSDispatcherImpl";

    private void log(String message) {
        Rlog.d(TAG, message);
    }

    public boolean isBlockMsgSending(int slotId) {
        return HwTelephonyManagerInner.getDefault().isBlockNonAisSlot(slotId);
    }
}
