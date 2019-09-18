package com.android.internal.telephony;

public class SubscriptionInfoUpdaterUtils {
    static final byte[] C3 = {-9, -86, 60, -113, 122, -7, -55, 69, 23, 119, 87, -83, 89, -1, -113, 29};

    public String[] getIccId(SubscriptionInfoUpdater subscriptionInfoUpdater) {
        return subscriptionInfoUpdater.getIccIdHw();
    }

    public int[] getInsertSimState(SubscriptionInfoUpdater subscriptionInfoUpdater) {
        return subscriptionInfoUpdater.getInsertSimStateHw();
    }

    public boolean isAllIccIdQueryDone(SubscriptionInfoUpdater subscriptionInfoUpdater) {
        return subscriptionInfoUpdater.isAllIccIdQueryDoneHw();
    }

    public void updateSubscriptionInfoByIccId(SubscriptionInfoUpdater subscriptionInfoUpdater) {
        subscriptionInfoUpdater.updateSubscriptionInfoByIccIdHw();
    }
}
