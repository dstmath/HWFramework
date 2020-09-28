package com.huawei.internal.telephony.msim;

import com.huawei.android.util.NoExtAPIException;

public class SubscriptionEx {
    public static final int SUBSCRIPTION_INDEX_INVALID = -1;
    public String appId;
    public String appLabel;
    public String appType;
    public String iccId;
    public int m3gpp2Index;
    public int m3gppIndex;
    public int slotId;
    public int subId;
    public SubscriptionStatusEx subStatusEx;

    public enum SubscriptionStatusEx {
        SUB_DEACTIVATE,
        SUB_ACTIVATE,
        SUB_ACTIVATED,
        SUB_DEACTIVATED,
        SUB_INVALID
    }

    public SubscriptionEx() {
        clear();
    }

    public void clear() {
        this.slotId = -1;
        this.m3gppIndex = -1;
        this.m3gpp2Index = -1;
        this.subId = -1;
        this.subStatusEx = SubscriptionStatusEx.SUB_INVALID;
        this.appId = null;
        this.appLabel = null;
        this.appType = null;
        this.iccId = null;
    }

    public SubscriptionEx copyFrom(SubscriptionEx from) {
        throw new NoExtAPIException("method not supported.");
    }

    public int getAppIndex() {
        throw new NoExtAPIException("method not supported.");
    }
}
