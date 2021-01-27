package com.huawei.internal.telephony.vsim;

import android.content.Intent;
import com.android.internal.telephony.vsim.VSimUtilsInner;

public class VSimUtilsInnerEx {
    public static boolean isVSimOn() {
        return VSimUtilsInner.isVSimOn();
    }

    public static int getTopPrioritySubscriptionId() {
        return VSimUtilsInner.getTopPrioritySubscriptionId();
    }

    public static void putVSimExtraForIccStateChanged(Intent intent, int subId, String value) {
        VSimUtilsInner.putVSimExtraForIccStateChanged(intent, subId, value);
    }
}
