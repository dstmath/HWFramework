package com.android.internal.telephony;

import com.huawei.android.telephony.TelephonyManagerEx;

public class HwPhoneServiceCommonUtils {
    private static final int SIM_NUM = TelephonyManagerEx.getDefault().getPhoneCount();

    public static boolean isValidSlotId(int slotId) {
        return slotId >= 0 && slotId < SIM_NUM;
    }
}
