package com.android.i18n.phonenumbers;

import com.huawei.annotation.HwSystemApi;
import java.util.List;
import java.util.Map;

@HwSystemApi
public class CountryCodeToRegionCodeMapUtilsEx {
    public static Map<Integer, List<String>> getCountryCodeToRegionCodeMap() {
        return CountryCodeToRegionCodeMap.getCountryCodeToRegionCodeMap();
    }
}
