package com.huawei.g11n.tmr.datetime.utils.digit;

import java.util.HashMap;
import java.util.Map.Entry;

public class LocaleDigitBn extends LocaleDigit {
    public LocaleDigitBn() {
        this.pattern = "[০১২৩৪৫৬৭৮৯]+";
    }

    public String convert(String inStr) {
        HashMap<Character, Integer> numMap = new HashMap();
        numMap.put(Character.valueOf(2535), Integer.valueOf(1));
        numMap.put(Character.valueOf(2536), Integer.valueOf(2));
        numMap.put(Character.valueOf(2537), Integer.valueOf(3));
        numMap.put(Character.valueOf(2538), Integer.valueOf(4));
        numMap.put(Character.valueOf(2539), Integer.valueOf(5));
        numMap.put(Character.valueOf(2540), Integer.valueOf(6));
        numMap.put(Character.valueOf(2541), Integer.valueOf(7));
        numMap.put(Character.valueOf(2542), Integer.valueOf(8));
        numMap.put(Character.valueOf(2543), Integer.valueOf(9));
        numMap.put(Character.valueOf(2534), Integer.valueOf(0));
        for (Entry<Character, Integer> entry : numMap.entrySet()) {
            inStr = inStr.replaceAll(((Character) entry.getKey()).toString(), ((Integer) entry.getValue()).toString());
        }
        return inStr;
    }
}
