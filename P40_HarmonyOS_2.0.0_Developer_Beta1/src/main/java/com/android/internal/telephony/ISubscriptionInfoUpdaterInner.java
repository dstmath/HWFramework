package com.android.internal.telephony;

import java.util.List;

public interface ISubscriptionInfoUpdaterInner {
    String[] getIccIdHw();

    boolean isAllIccIdQueryDoneHw();

    void resetIccid(int i);

    void updateEmbeddedSubscriptionsHw(List<Integer> list);

    void updateSubIdForNV(int i);

    void updateSubscriptionInfoByIccIdHw(int i, boolean z);
}
