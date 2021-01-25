package com.huawei.android.telephony;

import android.telephony.CellSignalStrength;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class CellSignalStrengthEx {
    public static final int INVALID_PHONEID = -1;

    public static int getPhoneId(CellSignalStrength cellSignalStrength) {
        if (cellSignalStrength != null) {
            return cellSignalStrength.getPhoneId();
        }
        return -1;
    }
}
