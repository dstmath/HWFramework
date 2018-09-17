package com.android.internal.telephony;

public class SubscriptionInfoUpdaterUtils {
    static final byte[] C3 = new byte[]{(byte) -9, (byte) -86, (byte) 60, (byte) -113, (byte) 122, (byte) -7, (byte) -55, (byte) 69, (byte) 23, (byte) 119, (byte) 87, (byte) -83, (byte) 89, (byte) -1, (byte) -113, (byte) 29};

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
