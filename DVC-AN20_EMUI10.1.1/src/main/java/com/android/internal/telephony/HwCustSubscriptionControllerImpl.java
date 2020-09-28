package com.android.internal.telephony;

import android.telephony.HwTelephonyManagerInner;

public class HwCustSubscriptionControllerImpl extends HwCustSubscriptionController {
    public boolean isBlockSetDataSlot(int slotId) {
        return HwTelephonyManagerInner.getDefault().isBlockNonAisSlot(slotId);
    }
}
