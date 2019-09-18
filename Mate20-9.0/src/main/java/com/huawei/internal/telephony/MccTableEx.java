package com.huawei.internal.telephony;

import com.android.internal.telephony.MccTable;

public class MccTableEx {
    public static int smallestDigitsMccForMnc(int mcc) {
        return MccTable.smallestDigitsMccForMnc(mcc);
    }
}
