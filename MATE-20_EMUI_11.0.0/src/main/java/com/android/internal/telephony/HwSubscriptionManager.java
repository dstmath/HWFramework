package com.android.internal.telephony;

import android.content.Context;
import android.os.Message;
import android.util.Log;
import com.huawei.internal.telephony.CommandsInterfaceEx;

public class HwSubscriptionManager {
    private static final String TAG = "HwSubscriptionManager";
    private static HwSubscriptionManager sInstance;
    private DefaultHwSubscriptionManager mHwSubscriptionManager = HwPartOptTelephonyFactory.getTelephonyFactory().getFullnetworkFactory().getHwSubscriptionManager();

    private HwSubscriptionManager() {
        Log.d(TAG, "add " + this.mHwSubscriptionManager.getClass().getCanonicalName() + " to memory");
    }

    public static HwSubscriptionManager getInstance() {
        if (sInstance == null) {
            sInstance = new HwSubscriptionManager();
        }
        return sInstance;
    }

    public void init(Context context, CommandsInterfaceEx[] commandsInterfaceExes) {
        this.mHwSubscriptionManager.init(context, commandsInterfaceExes);
    }

    public boolean setSubscription(int slotId, boolean isActivate, Message onCompleteMsg) {
        return this.mHwSubscriptionManager.setSubscription(slotId, isActivate, onCompleteMsg);
    }

    public void setUserPrefDataSlotId(int slotId) {
        this.mHwSubscriptionManager.setUserPrefDataSlotId(slotId);
    }

    public void setDefaultDataSubIdToDbBySlotId(int slotId) {
        this.mHwSubscriptionManager.setDefaultDataSubIdToDbBySlotId(slotId);
    }

    public void setDefaultDataSubIdBySlotId(int slotId) {
        this.mHwSubscriptionManager.setDefaultDataSubIdBySlotId(slotId);
    }

    public void setUserPrefDefaultSlotId(int slotId) {
        this.mHwSubscriptionManager.setUserPrefDefaultSlotId(slotId);
    }

    public void updateDataSlot() {
        this.mHwSubscriptionManager.updateDataSlot();
    }

    public void updateUserPreferences(boolean isSetDds) {
        this.mHwSubscriptionManager.updateUserPreferences(isSetDds);
    }
}
