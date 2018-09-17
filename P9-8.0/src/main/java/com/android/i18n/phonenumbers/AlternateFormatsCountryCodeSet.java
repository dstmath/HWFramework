package com.android.i18n.phonenumbers;

import java.util.HashSet;
import java.util.Set;
import javax.sip.message.Response;

public class AlternateFormatsCountryCodeSet {
    static Set<Integer> getCountryCodeSet() {
        Set<Integer> countryCodeSet = new HashSet(60);
        countryCodeSet.add(Integer.valueOf(7));
        countryCodeSet.add(Integer.valueOf(27));
        countryCodeSet.add(Integer.valueOf(30));
        countryCodeSet.add(Integer.valueOf(31));
        countryCodeSet.add(Integer.valueOf(34));
        countryCodeSet.add(Integer.valueOf(36));
        countryCodeSet.add(Integer.valueOf(39));
        countryCodeSet.add(Integer.valueOf(43));
        countryCodeSet.add(Integer.valueOf(44));
        countryCodeSet.add(Integer.valueOf(49));
        countryCodeSet.add(Integer.valueOf(52));
        countryCodeSet.add(Integer.valueOf(54));
        countryCodeSet.add(Integer.valueOf(55));
        countryCodeSet.add(Integer.valueOf(58));
        countryCodeSet.add(Integer.valueOf(61));
        countryCodeSet.add(Integer.valueOf(62));
        countryCodeSet.add(Integer.valueOf(63));
        countryCodeSet.add(Integer.valueOf(66));
        countryCodeSet.add(Integer.valueOf(81));
        countryCodeSet.add(Integer.valueOf(84));
        countryCodeSet.add(Integer.valueOf(90));
        countryCodeSet.add(Integer.valueOf(91));
        countryCodeSet.add(Integer.valueOf(94));
        countryCodeSet.add(Integer.valueOf(95));
        countryCodeSet.add(Integer.valueOf(255));
        countryCodeSet.add(Integer.valueOf(350));
        countryCodeSet.add(Integer.valueOf(351));
        countryCodeSet.add(Integer.valueOf(352));
        countryCodeSet.add(Integer.valueOf(358));
        countryCodeSet.add(Integer.valueOf(359));
        countryCodeSet.add(Integer.valueOf(372));
        countryCodeSet.add(Integer.valueOf(373));
        countryCodeSet.add(Integer.valueOf(Response.ALTERNATIVE_SERVICE));
        countryCodeSet.add(Integer.valueOf(381));
        countryCodeSet.add(Integer.valueOf(385));
        countryCodeSet.add(Integer.valueOf(Response.VERSION_NOT_SUPPORTED));
        countryCodeSet.add(Integer.valueOf(506));
        countryCodeSet.add(Integer.valueOf(595));
        countryCodeSet.add(Integer.valueOf(675));
        countryCodeSet.add(Integer.valueOf(676));
        countryCodeSet.add(Integer.valueOf(679));
        countryCodeSet.add(Integer.valueOf(855));
        countryCodeSet.add(Integer.valueOf(971));
        countryCodeSet.add(Integer.valueOf(972));
        countryCodeSet.add(Integer.valueOf(995));
        return countryCodeSet;
    }
}
