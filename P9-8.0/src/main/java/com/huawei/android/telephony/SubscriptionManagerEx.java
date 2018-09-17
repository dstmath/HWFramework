package com.huawei.android.telephony;

import android.content.Intent;
import android.telephony.SubscriptionManager;

public class SubscriptionManagerEx {
    public static int[] getSubId(int slotIndex) {
        return SubscriptionManager.getSubId(slotIndex);
    }

    public static void putPhoneIdAndSubIdExtra(Intent intent, int phoneId) {
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, phoneId);
    }

    public static int getSlotIndex(int subId) {
        return SubscriptionManager.getSlotIndex(subId);
    }

    public static int getPhoneId(int subId) {
        return SubscriptionManager.getPhoneId(subId);
    }
}
