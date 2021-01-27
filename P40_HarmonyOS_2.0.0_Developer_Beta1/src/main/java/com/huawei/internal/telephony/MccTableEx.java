package com.huawei.internal.telephony;

import android.content.Context;
import com.android.internal.telephony.HwCustMccTable;
import com.android.internal.telephony.MccTable;
import com.huawei.annotation.HwSystemApi;
import huawei.cust.HwCustUtils;
import java.util.Locale;

public class MccTableEx {
    public static int smallestDigitsMccForMnc(int mcc) {
        return MccTable.smallestDigitsMccForMnc(mcc);
    }

    @HwSystemApi
    public static void updateMccMncConfiguration(Context context, String mccmnc) {
        MccTable.updateMccMncConfiguration(context, mccmnc);
    }

    @HwSystemApi
    public static String countryCodeForMcc(String mcc) {
        return MccTable.countryCodeForMcc(mcc);
    }

    @HwSystemApi
    public static Locale getCustLocale(String imsi) {
        Locale custLocale;
        HwCustMccTable hwCustMccTable = (HwCustMccTable) HwCustUtils.createObj(HwCustMccTable.class, new Object[0]);
        if (hwCustMccTable == null || (custLocale = hwCustMccTable.getCustSpecialLocaleConfig(imsi)) == null) {
            return null;
        }
        return custLocale;
    }
}
