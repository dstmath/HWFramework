package com.huawei.g11n.tmr.datetime.utils.digit;

import java.util.HashMap;
import java.util.Map.Entry;

public class LocaleDigitFa extends LocaleDigit {
    public LocaleDigitFa() {
        this.pattern = "[\u06f0\u06f1\u06f2\u06f3\u06f4\u06f5\u06f6\u06f7\u06f8\u06f9]+";
    }

    public String convert(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put(Character.valueOf('\u06f1'), Integer.valueOf(1));
        hashMap.put(Character.valueOf('\u06f2'), Integer.valueOf(2));
        hashMap.put(Character.valueOf('\u06f3'), Integer.valueOf(3));
        hashMap.put(Character.valueOf('\u06f4'), Integer.valueOf(4));
        hashMap.put(Character.valueOf('\u06f5'), Integer.valueOf(5));
        hashMap.put(Character.valueOf('\u06f6'), Integer.valueOf(6));
        hashMap.put(Character.valueOf('\u06f7'), Integer.valueOf(7));
        hashMap.put(Character.valueOf('\u06f8'), Integer.valueOf(8));
        hashMap.put(Character.valueOf('\u06f9'), Integer.valueOf(9));
        hashMap.put(Character.valueOf('\u06f0'), Integer.valueOf(0));
        for (Entry entry : hashMap.entrySet()) {
            str = str.replaceAll(((Character) entry.getKey()).toString(), ((Integer) entry.getValue()).toString());
        }
        return str;
    }
}
