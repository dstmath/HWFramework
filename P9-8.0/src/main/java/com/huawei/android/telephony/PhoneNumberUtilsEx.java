package com.huawei.android.telephony;

import android.telephony.PhoneNumberUtils;

public class PhoneNumberUtilsEx {
    public static boolean isEmergencyNumber(int subId, String number) {
        return PhoneNumberUtils.isEmergencyNumber(subId, number);
    }

    public static boolean isUriNumber(String number) {
        return PhoneNumberUtils.isUriNumber(number);
    }

    public static String convertPreDial(String phoneNumber) {
        return PhoneNumberUtils.convertPreDial(phoneNumber);
    }

    public static String extractNetworkPortionAlt(String phoneNumber) {
        return PhoneNumberUtils.extractNetworkPortionAlt(phoneNumber);
    }

    public static boolean compareLoosely(String a, String b) {
        return PhoneNumberUtils.compareLoosely(a, b);
    }
}
