package com.android.internal.telephony;

import android.telephony.HwTelephonyManagerInner;
import android.telephony.PhoneNumberUtils;

public class HwCustCallTrackerImpl extends HwCustCallTracker {
    public boolean isBlockDialing(String dialString, int subId) {
        return isBlockDialingByNonAis(dialString, subId);
    }

    private boolean isBlockDialingByNonAis(String dialString, int subId) {
        if (!HwTelephonyManagerInner.getDefault().isCustomAis() || HwTelephonyManagerInner.getDefault().isAISCard(subId) || HwTelephonyManagerInner.getDefault().isAisCustomDisable() || PhoneNumberUtils.isEmergencyNumber(subId, dialString)) {
            return false;
        }
        return true;
    }
}
