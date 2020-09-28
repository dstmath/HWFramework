package com.huawei.i18n.phonenumbers;

import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class PhoneNumberUtilExt {
    private PhoneNumberUtil mPhoneNumberUtil;

    public void setPhoneNumberUtil(PhoneNumberUtil phoneNumberUtil) {
        this.mPhoneNumberUtil = phoneNumberUtil;
    }

    public static PhoneNumberUtilExt getInstance() {
        PhoneNumberUtil instance = PhoneNumberUtil.getInstance();
        if (instance == null) {
            return null;
        }
        PhoneNumberUtilExt utilEx = new PhoneNumberUtilExt();
        utilEx.setPhoneNumberUtil(instance);
        return utilEx;
    }

    public int getCountryCodeForRegion(String regionCode) {
        return this.mPhoneNumberUtil.getCountryCodeForRegion(regionCode);
    }

    public String getRegionCodeForCountryCode(int countryCallingCode) {
        return this.mPhoneNumberUtil.getRegionCodeForCountryCode(countryCallingCode);
    }
}
