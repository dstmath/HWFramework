package com.android.internal.telephony.msim;

import com.huawei.android.util.NoExtAPIException;

public class SubscriptionData {
    public Subscription[] subscription;

    public SubscriptionData(int numSub) {
        throw new NoExtAPIException("method not supported.");
    }

    public int getLength() {
        throw new NoExtAPIException("method not supported.");
    }

    public SubscriptionData copyFrom(SubscriptionData from) {
        throw new NoExtAPIException("method not supported.");
    }

    public String getIccId() {
        throw new NoExtAPIException("method not supported.");
    }

    public boolean hasSubscription(Subscription sub) {
        throw new NoExtAPIException("method not supported.");
    }

    public Subscription getSubscription(Subscription sub) {
        throw new NoExtAPIException("method not supported.");
    }

    public String toString() {
        throw new NoExtAPIException("method not supported.");
    }
}
