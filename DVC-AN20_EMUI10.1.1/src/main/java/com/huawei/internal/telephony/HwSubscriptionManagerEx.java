package com.huawei.internal.telephony;

import android.os.Message;
import com.android.internal.telephony.HwSubscriptionManager;

public class HwSubscriptionManagerEx {
    private HwSubscriptionManager mHwSubscriptionManager;

    private HwSubscriptionManagerEx(HwSubscriptionManager obj) {
        this.mHwSubscriptionManager = obj;
    }

    public static HwSubscriptionManagerEx getInstance() {
        return new HwSubscriptionManagerEx(HwSubscriptionManager.getInstance());
    }

    public boolean setSubscription(int slotId, boolean activate, Message onCompleteMsg) {
        HwSubscriptionManager hwSubscriptionManager = this.mHwSubscriptionManager;
        if (hwSubscriptionManager == null) {
            return false;
        }
        return hwSubscriptionManager.setSubscription(slotId, activate, onCompleteMsg);
    }

    public void setUserPrefDataSlotId(int slotId) {
        HwSubscriptionManager hwSubscriptionManager = this.mHwSubscriptionManager;
        if (hwSubscriptionManager != null) {
            hwSubscriptionManager.setUserPrefDataSlotId(slotId);
        }
    }

    public void setUserPrefDefaultSlotId(int slotId) {
        HwSubscriptionManager hwSubscriptionManager = this.mHwSubscriptionManager;
        if (hwSubscriptionManager != null) {
            hwSubscriptionManager.setUserPrefDefaultSlotId(slotId);
        }
    }
}
