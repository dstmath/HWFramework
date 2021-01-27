package com.huawei.internal.telephony;

import com.android.internal.telephony.HwTelephonyFactory;

public class HwDataServiceChrManagerEx {
    public static void sendIntentWhenSetDataSubFail(int subId) {
        HwTelephonyFactory.getHwDataServiceChrManager().sendIntentWhenSetDataSubFail(subId);
    }
}
