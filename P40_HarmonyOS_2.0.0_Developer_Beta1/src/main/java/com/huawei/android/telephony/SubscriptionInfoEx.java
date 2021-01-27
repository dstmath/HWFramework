package com.huawei.android.telephony;

import android.telephony.SubscriptionInfo;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class SubscriptionInfoEx {
    public static int getProfileClass(SubscriptionInfo subscriptionInfo) {
        if (subscriptionInfo != null) {
            return subscriptionInfo.getProfileClass();
        }
        return -1;
    }

    public static String givePrintableIccid(String iccId) {
        return SubscriptionInfo.givePrintableIccid(iccId);
    }

    public static int getNwMode(SubscriptionInfo subscriptionInfo) {
        if (subscriptionInfo != null) {
            return subscriptionInfo.getNwMode();
        }
        return -1;
    }

    public static int getSubStatus(SubscriptionInfo subscriptionInfo) {
        if (subscriptionInfo != null) {
            return subscriptionInfo.getStatus();
        }
        return 1;
    }
}
