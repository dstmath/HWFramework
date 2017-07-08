package com.huawei.g11n.tmr.phonenumber;

public abstract class AbstractPhoneNumberMatcher {
    private String country;

    public abstract int[] getMatchedPhoneNumber(String str, String str2);

    public AbstractPhoneNumberMatcher(String str) {
        this.country = str;
    }

    public String getCountry() {
        return this.country;
    }
}
