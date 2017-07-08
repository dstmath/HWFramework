package com.huawei.g11n.tmr.datetime.utils.digit;

import java.util.HashMap;
import java.util.Map.Entry;

public class LocaleDigitBn extends LocaleDigit {
    public LocaleDigitBn() {
        this.pattern = "[\u09e6\u09e7\u09e8\u09e9\u09ea\u09eb\u09ec\u09ed\u09ee\u09ef]+";
    }

    public String convert(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put(Character.valueOf('\u09e7'), Integer.valueOf(1));
        hashMap.put(Character.valueOf('\u09e8'), Integer.valueOf(2));
        hashMap.put(Character.valueOf('\u09e9'), Integer.valueOf(3));
        hashMap.put(Character.valueOf('\u09ea'), Integer.valueOf(4));
        hashMap.put(Character.valueOf('\u09eb'), Integer.valueOf(5));
        hashMap.put(Character.valueOf('\u09ec'), Integer.valueOf(6));
        hashMap.put(Character.valueOf('\u09ed'), Integer.valueOf(7));
        hashMap.put(Character.valueOf('\u09ee'), Integer.valueOf(8));
        hashMap.put(Character.valueOf('\u09ef'), Integer.valueOf(9));
        hashMap.put(Character.valueOf('\u09e6'), Integer.valueOf(0));
        for (Entry entry : hashMap.entrySet()) {
            str = str.replaceAll(((Character) entry.getKey()).toString(), ((Integer) entry.getValue()).toString());
        }
        return str;
    }
}
