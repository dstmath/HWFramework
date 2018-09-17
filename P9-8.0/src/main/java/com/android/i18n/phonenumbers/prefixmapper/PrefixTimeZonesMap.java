package com.android.i18n.phonenumbers.prefixmapper;

import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.StringTokenizer;

public class PrefixTimeZonesMap implements Externalizable {
    private static final String RAW_STRING_TIMEZONES_SEPARATOR = "&";
    private final PhonePrefixMap phonePrefixMap = new PhonePrefixMap();

    public void readPrefixTimeZonesMap(SortedMap<Integer, String> sortedPrefixTimeZoneMap) {
        this.phonePrefixMap.readPhonePrefixMap(sortedPrefixTimeZoneMap);
    }

    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        this.phonePrefixMap.writeExternal(objectOutput);
    }

    public void readExternal(ObjectInput objectInput) throws IOException {
        this.phonePrefixMap.readExternal(objectInput);
    }

    private List<String> lookupTimeZonesForNumber(long key) {
        String timezonesString = this.phonePrefixMap.lookup(key);
        if (timezonesString == null) {
            return new LinkedList();
        }
        return tokenizeRawOutputString(timezonesString);
    }

    public List<String> lookupTimeZonesForNumber(PhoneNumber number) {
        return lookupTimeZonesForNumber(Long.parseLong(number.getCountryCode() + PhoneNumberUtil.getInstance().getNationalSignificantNumber(number)));
    }

    public List<String> lookupCountryLevelTimeZonesForNumber(PhoneNumber number) {
        return lookupTimeZonesForNumber((long) number.getCountryCode());
    }

    private List<String> tokenizeRawOutputString(String timezonesString) {
        StringTokenizer tokenizer = new StringTokenizer(timezonesString, "&");
        LinkedList<String> timezonesList = new LinkedList();
        while (tokenizer.hasMoreTokens()) {
            timezonesList.add(tokenizer.nextToken());
        }
        return timezonesList;
    }

    public String toString() {
        return this.phonePrefixMap.toString();
    }
}
