package com.huawei.internal.telephony.msim;

import com.huawei.android.util.NoExtAPIException;

public class SubscriptionDataEx {
    public SubscriptionEx[] subscriptionEx;

    public SubscriptionDataEx(int numSub) {
        throw new NoExtAPIException("method not supported.");
    }

    public int getLength() {
        throw new NoExtAPIException("method not supported.");
    }

    public SubscriptionDataEx copyFrom(SubscriptionDataEx from) {
        throw new NoExtAPIException("method not supported.");
    }
}
