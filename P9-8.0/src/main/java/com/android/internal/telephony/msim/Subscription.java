package com.android.internal.telephony.msim;

import com.huawei.android.util.NoExtAPIException;

public final class Subscription {
    public static final int SUBSCRIPTION_INDEX_INVALID = -1;
    public String appId;
    public String appLabel;
    public String appType;
    public String iccId;
    public int m3gpp2Index;
    public int m3gppIndex;
    public int slotId;
    public int subId;
    public SubscriptionStatus subStatus;

    public enum SubscriptionStatus {
        SUB_DEACTIVATE,
        SUB_ACTIVATE,
        SUB_ACTIVATED,
        SUB_DEACTIVATED,
        SUB_INVALID
    }

    public Subscription() {
        throw new NoExtAPIException("method not supported.");
    }

    public String toString() {
        throw new NoExtAPIException("method not supported.");
    }

    public boolean equals(Subscription sub) {
        throw new NoExtAPIException("method not supported.");
    }

    public boolean isSame(Subscription sub) {
        throw new NoExtAPIException("method not supported.");
    }

    public void clear() {
        throw new NoExtAPIException("method not supported.");
    }

    public Subscription copyFrom(Subscription from) {
        throw new NoExtAPIException("method not supported.");
    }

    public int getAppIndex() {
        throw new NoExtAPIException("method not supported.");
    }

    public int getLength() {
        throw new NoExtAPIException("method not supported.");
    }
}
