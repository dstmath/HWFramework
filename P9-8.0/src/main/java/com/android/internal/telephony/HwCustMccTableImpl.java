package com.android.internal.telephony;

import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import java.util.Locale;

public class HwCustMccTableImpl extends HwCustMccTable {
    private static final boolean IS_CUST_LOCALE_CONFIG = SystemProperties.getBoolean("ro.hw_tp.mexico_locale", false);
    private static final String LOG_TAG = "MccTable";
    private static final String[] MEXICO_MCC = new String[]{"334", "222", "206"};

    public Locale getCustSpecialLocaleConfig(String imsi) {
        if (!IS_CUST_LOCALE_CONFIG || TextUtils.isEmpty(imsi)) {
            return null;
        }
        for (String startsWith : MEXICO_MCC) {
            if (imsi.startsWith(startsWith)) {
                Log.d(LOG_TAG, "Mexico special locale config, set default language to es_mx");
                return new Locale("es", "mx");
            }
        }
        return null;
    }
}
