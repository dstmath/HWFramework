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
        if (this.mHwSubscriptionManager == null) {
            return false;
        }
        return this.mHwSubscriptionManager.setSubscription(slotId, activate, onCompleteMsg);
    }

    public void setUserPrefDataSlotId(int slotId) {
        if (this.mHwSubscriptionManager != null) {
            this.mHwSubscriptionManager.setUserPrefDataSlotId(slotId);
        }
    }

    public void setUserPrefDefaultSlotId(int slotId) {
        if (this.mHwSubscriptionManager != null) {
            this.mHwSubscriptionManager.setUserPrefDefaultSlotId(slotId);
        }
    }
}
