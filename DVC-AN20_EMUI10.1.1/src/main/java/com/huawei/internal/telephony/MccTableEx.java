package com.huawei.internal.telephony;

import android.content.Context;
import com.android.internal.telephony.MccTable;
import com.huawei.annotation.HwSystemApi;

public class MccTableEx {
    public static int smallestDigitsMccForMnc(int mcc) {
        return MccTable.smallestDigitsMccForMnc(mcc);
    }

    @HwSystemApi
    public static void updateMccMncConfiguration(Context context, String mccmnc) {
        MccTable.updateMccMncConfiguration(context, mccmnc);
    }
}
