package com.huawei.android.telephony;

import android.content.Context;
import android.content.Intent;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import java.util.ArrayList;
import java.util.List;

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

    public static int getAllSubscriptionInfoCount(Context context) {
        SubscriptionManager sm = SubscriptionManager.from(context);
        if (sm != null) {
            return sm.getAllSubscriptionInfoCount();
        }
        return 0;
    }

    public static List<SubscriptionInfo> getAllSubscriptionInfoList(Context context) {
        SubscriptionManager sm = SubscriptionManager.from(context);
        if (sm != null) {
            return sm.getAllSubscriptionInfoList();
        }
        return new ArrayList();
    }

    public static boolean isValidSubscriptionId(int subId) {
        return SubscriptionManager.isValidSubscriptionId(subId);
    }

    public static int getSimStateForSlotIndex(int slotIndex) {
        return SubscriptionManager.getSimStateForSlotIndex(slotIndex);
    }
}
