package com.huawei.tmr.util;

import android.util.Log;
import com.huawei.g11n.tmr.DateTmr;
import com.huawei.g11n.tmr.address.AddressTmr;
import com.huawei.g11n.tmr.address.de.SerEngine;
import com.huawei.g11n.tmr.phonenumber.SearchPhoneNumber;
import java.util.Date;
import java.util.Locale;

public class TMRManagerProxy {
    private static final String TAG = "TMRManager";

    public static int[] getAddress(String msgContent) {
        try {
            String locale = Locale.getDefault().getLanguage();
            if (locale.equals("en")) {
                return AddressTmr.getAddr(msgContent);
            }
            if (locale.equals("de")) {
                return SerEngine.search(msgContent);
            }
            if (locale.equals("es")) {
                return com.huawei.g11n.tmr.address.es.SerEngine.search(msgContent);
            }
            if (locale.equals("it")) {
                return com.huawei.g11n.tmr.address.it.SerEngine.search(msgContent);
            }
            if (locale.equals("fr")) {
                return com.huawei.g11n.tmr.address.fr.SerEngine.search(msgContent);
            }
            if (locale.equals("pt")) {
                return com.huawei.g11n.tmr.address.pt.SerEngine.search(msgContent);
            }
            return TMRManager.getAddr(msgContent);
        } catch (Exception e) {
            Log.e(TAG, "getAddr has  an error  >>>> " + e);
            return null;
        }
    }

    public static Date[] convertDate(String dateString, long defaultDate) {
        Date[] result = new Date[]{new Date(defaultDate)};
        if (dateString == null) {
            return result;
        }
        return DateTmr.convertDate(dateString, defaultDate);
    }

    public static int[] getTime(String msg) {
        int[] result = new int[]{0};
        if (msg == null) {
            return result;
        }
        return DateTmr.getTime(msg);
    }

    public static int[] getMatchedPhoneNumber(String msg, String country) {
        int[] result = new int[]{0};
        if (msg == null) {
            return result;
        }
        try {
            result = SearchPhoneNumber.getMatchedPhoneNumber(msg, country);
        } catch (Exception e) {
            Log.e(TAG, "getPhoneNumber has  an error  >>>> " + e);
        }
        return result;
    }
}
