package com.huawei.internal.telephony;

import com.android.internal.telephony.HwUiccSmsManager;

public class ISmsEx {
    public static void setSingleShiftTable(int[] temp) {
        HwUiccSmsManager.setEnabledSingleShiftTables(temp);
    }

    public static void setSmsCodingNationalCode(String code) {
        HwUiccSmsManager.setSmsCodingNationalCode(code);
    }
}
