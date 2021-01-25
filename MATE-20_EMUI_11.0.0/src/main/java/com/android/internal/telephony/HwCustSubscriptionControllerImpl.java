package com.android.internal.telephony;

import android.telephony.HwTelephonyManagerInner;

public class HwCustSubscriptionControllerImpl extends HwCustSubscriptionController {
    private static final int DATA_SERVICES = 0;

    public boolean isBlockSetDataSlot(int slotId) {
        if (HwTelephonyManagerInner.getDefault().isBlockNonAisSlot(slotId) || HwTelephonyManagerInner.getDefault().isBlockNonSmartSlot(slotId) || HwTelephonyManagerInner.getDefault().isBlockNonCustomSlot(slotId, 0)) {
            return true;
        }
        return false;
    }
}
