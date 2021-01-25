package com.android.internal.telephony;

import android.telephony.HwTelephonyManagerInner;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionManager;

public class HwCustCallTrackerImpl extends HwCustCallTracker {
    public boolean isBlockDialing(String dialString, int slotId) {
        return isBlockDialingByNonAis(dialString, slotId);
    }

    private boolean isBlockDialingByNonAis(String dialString, int slotId) {
        int[] subIds = SubscriptionManager.getSubId(slotId);
        if (subIds == null || subIds.length <= 0 || !PhoneNumberUtils.isEmergencyNumber(subIds[0], dialString)) {
            return HwTelephonyManagerInner.getDefault().isBlockNonAisSlot(slotId);
        }
        return false;
    }
}
