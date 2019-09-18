package com.android.i18n.phonenumbers;

import java.util.HashSet;
import java.util.Set;
import javax.sip.message.Response;

public class AlternateFormatsCountryCodeSet {
    static Set<Integer> getCountryCodeSet() {
        Set<Integer> countryCodeSet = new HashSet<>(62);
        countryCodeSet.add(7);
        countryCodeSet.add(27);
        countryCodeSet.add(30);
        countryCodeSet.add(31);
        countryCodeSet.add(34);
        countryCodeSet.add(36);
        countryCodeSet.add(39);
        countryCodeSet.add(43);
        countryCodeSet.add(44);
        countryCodeSet.add(49);
        countryCodeSet.add(52);
        countryCodeSet.add(54);
        countryCodeSet.add(55);
        countryCodeSet.add(58);
        countryCodeSet.add(61);
        countryCodeSet.add(62);
        countryCodeSet.add(63);
        countryCodeSet.add(64);
        countryCodeSet.add(66);
        countryCodeSet.add(81);
        countryCodeSet.add(84);
        countryCodeSet.add(90);
        countryCodeSet.add(91);
        countryCodeSet.add(94);
        countryCodeSet.add(95);
        countryCodeSet.add(255);
        countryCodeSet.add(350);
        countryCodeSet.add(351);
        countryCodeSet.add(352);
        countryCodeSet.add(358);
        countryCodeSet.add(359);
        countryCodeSet.add(372);
        countryCodeSet.add(373);
        countryCodeSet.add(Integer.valueOf(Response.ALTERNATIVE_SERVICE));
        countryCodeSet.add(381);
        countryCodeSet.add(385);
        countryCodeSet.add(Integer.valueOf(Response.VERSION_NOT_SUPPORTED));
        countryCodeSet.add(506);
        countryCodeSet.add(595);
        countryCodeSet.add(675);
        countryCodeSet.add(676);
        countryCodeSet.add(679);
        countryCodeSet.add(855);
        countryCodeSet.add(856);
        countryCodeSet.add(971);
        countryCodeSet.add(972);
        countryCodeSet.add(995);
        return countryCodeSet;
    }
}
