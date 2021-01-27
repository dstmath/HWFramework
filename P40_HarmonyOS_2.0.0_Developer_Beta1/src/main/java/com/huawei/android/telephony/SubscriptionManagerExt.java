package com.huawei.android.telephony;

import android.telephony.SubscriptionManager;

public class SubscriptionManagerExt {
    public static int getDefaultSubId() {
        return SubscriptionManager.getDefaultSubId();
    }
}
