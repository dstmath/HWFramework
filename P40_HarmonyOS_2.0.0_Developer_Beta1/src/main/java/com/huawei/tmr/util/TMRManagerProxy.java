package com.huawei.tmr.util;

import android.util.Log;
import com.huawei.i18n.tmr.address.AddressTmr;
import com.huawei.i18n.tmr.datetime.DateTmr;
import com.huawei.i18n.tmr.phonenumber.SearchPhoneNumber;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class TMRManagerProxy {
    private static final String TAG = "TMRManager";

    public static int[] getAddress(String msgContent) {
        try {
            if (isAddressLocale(Locale.getDefault().getLanguage())) {
                return AddressTmr.getAddress(msgContent);
            }
            return TMRManager.getAddr(msgContent);
        } catch (Exception e) {
            Log.e(TAG, "getAddress, getAddr has an error");
            return null;
        }
    }

    private static boolean isAddressLocale(String locale) {
        return Arrays.asList("en", "fr", "es", "it", "pt", "de").contains(locale);
    }

    public static Date[] convertDate(String dateString, long defaultDate) {
        Date[] result = {new Date(defaultDate)};
        if (dateString == null) {
            return result;
        }
        return DateTmr.convertDate(dateString, defaultDate);
    }

    public static int[] getTime(String msg) {
        int[] result = {0};
        if (msg == null) {
            return result;
        }
        return DateTmr.getTime(msg);
    }

    public static int[] getMatchedPhoneNumber(String msg, String country) {
        int[] result = {0};
        if (msg == null) {
            return result;
        }
        try {
            return SearchPhoneNumber.getMatchedPhoneNumber(msg, country);
        } catch (Exception e) {
            Log.e(TAG, "getMatchedPhoneNumber, getPhoneNumber has an error");
            return result;
        }
    }
}
