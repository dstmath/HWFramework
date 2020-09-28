package com.huawei.internal.telephony;

import com.android.internal.telephony.MccTable;

public class MccTableExt {
    public static String countryCodeForMcc(int mcc) {
        return MccTable.countryCodeForMcc(mcc);
    }
}
