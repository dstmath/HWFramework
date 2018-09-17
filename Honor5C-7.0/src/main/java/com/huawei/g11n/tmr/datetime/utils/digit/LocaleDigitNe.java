package com.huawei.g11n.tmr.datetime.utils.digit;

import java.util.HashMap;
import java.util.Map.Entry;

public class LocaleDigitNe extends LocaleDigit {
    public LocaleDigitNe() {
        this.pattern = "[\u0966\u0967\u0968\u0969\u096a\u096b\u096c\u096d\u096e\u096f]+";
    }

    public String convert(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put(Character.valueOf('\u0967'), Integer.valueOf(1));
        hashMap.put(Character.valueOf('\u0968'), Integer.valueOf(2));
        hashMap.put(Character.valueOf('\u0969'), Integer.valueOf(3));
        hashMap.put(Character.valueOf('\u096a'), Integer.valueOf(4));
        hashMap.put(Character.valueOf('\u096b'), Integer.valueOf(5));
        hashMap.put(Character.valueOf('\u096c'), Integer.valueOf(6));
        hashMap.put(Character.valueOf('\u096d'), Integer.valueOf(7));
        hashMap.put(Character.valueOf('\u096e'), Integer.valueOf(8));
        hashMap.put(Character.valueOf('\u096f'), Integer.valueOf(9));
        hashMap.put(Character.valueOf('\u0966'), Integer.valueOf(0));
        for (Entry entry : hashMap.entrySet()) {
            str = str.replaceAll(((Character) entry.getKey()).toString(), ((Integer) entry.getValue()).toString());
        }
        return str;
    }
}
