package com.android.internal.telephony;

import android.content.Context;
import android.os.Message;
import com.huawei.internal.telephony.CommandsInterfaceEx;

public class HwSubscriptionManagerUtils {
    private static HwSubscriptionManagerUtils sInstance;

    public static synchronized HwSubscriptionManagerUtils getInstance() {
        HwSubscriptionManagerUtils hwSubscriptionManagerUtils;
        synchronized (HwSubscriptionManagerUtils.class) {
            if (sInstance == null) {
                sInstance = new HwSubscriptionManagerUtils();
            }
            hwSubscriptionManagerUtils = sInstance;
        }
        return hwSubscriptionManagerUtils;
    }

    public boolean setSubscription(int slotId, boolean isActivate, Message onCompleteMsg) {
        return HwSubscriptionManager.getInstance().setSubscription(slotId, isActivate, onCompleteMsg);
    }

    public void updateDataSlot() {
        HwSubscriptionManager.getInstance().updateDataSlot();
    }

    public void updateUserPreferences(boolean isSetDds) {
        HwSubscriptionManager.getInstance().updateUserPreferences(isSetDds);
    }

    public void init(Context context, CommandsInterfaceEx[] commandsInterfaceExes) {
        HwSubscriptionManager.getInstance().init(context, commandsInterfaceExes);
    }
}
