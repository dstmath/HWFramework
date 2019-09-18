package com.android.internal.telephony;

import android.telephony.HwTelephonyManagerInner;

public class HwCustSubscriptionControllerImpl extends HwCustSubscriptionController {
    public boolean isBlockSetDataSub(int subId) {
        return isBlockSetDataSubByNonAis(subId);
    }

    private boolean isBlockSetDataSubByNonAis(int subId) {
        return HwTelephonyManagerInner.getDefault().isCustomAis() && !HwTelephonyManagerInner.getDefault().isAISCard(subId) && !HwTelephonyManagerInner.getDefault().isAisCustomDisable();
    }
}
