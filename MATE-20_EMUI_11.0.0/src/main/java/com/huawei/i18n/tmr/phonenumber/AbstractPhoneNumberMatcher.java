package com.huawei.i18n.tmr.phonenumber;

public abstract class AbstractPhoneNumberMatcher {
    private String country;

    public abstract int[] getMatchedPhoneNumber(String str, String str2);

    AbstractPhoneNumberMatcher(String country2) {
        this.country = country2;
    }

    public String getCountry() {
        return this.country;
    }
}
