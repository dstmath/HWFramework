package com.huawei.g11n.tmr.phonenumber;

public class SearchPhoneNumber {
    private static volatile AbstractPhoneNumberMatcher instance = null;

    private static synchronized AbstractPhoneNumberMatcher getInstance(String country) {
        AbstractPhoneNumberMatcher abstractPhoneNumberMatcher;
        synchronized (SearchPhoneNumber.class) {
            if (instance == null) {
                instance = new PhoneNumberMatcher(country);
            } else if (!instance.getCountry().equals(country.trim())) {
                instance = new PhoneNumberMatcher(country);
            }
            abstractPhoneNumberMatcher = instance;
        }
        return abstractPhoneNumberMatcher;
    }

    public static int[] getMatchedPhoneNumber(String msg, String country) {
        return getInstance(country).getMatchedPhoneNumber(msg, country);
    }
}
