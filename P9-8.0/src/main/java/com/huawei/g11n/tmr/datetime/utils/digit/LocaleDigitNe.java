package com.huawei.g11n.tmr.datetime.utils.digit;

import java.util.HashMap;
import java.util.Map.Entry;

public class LocaleDigitNe extends LocaleDigit {
    public LocaleDigitNe() {
        this.pattern = "[०१२३४५६७८९]+";
    }

    public String convert(String inStr) {
        HashMap<Character, Integer> numMap = new HashMap();
        numMap.put(Character.valueOf(2407), Integer.valueOf(1));
        numMap.put(Character.valueOf(2408), Integer.valueOf(2));
        numMap.put(Character.valueOf(2409), Integer.valueOf(3));
        numMap.put(Character.valueOf(2410), Integer.valueOf(4));
        numMap.put(Character.valueOf(2411), Integer.valueOf(5));
        numMap.put(Character.valueOf(2412), Integer.valueOf(6));
        numMap.put(Character.valueOf(2413), Integer.valueOf(7));
        numMap.put(Character.valueOf(2414), Integer.valueOf(8));
        numMap.put(Character.valueOf(2415), Integer.valueOf(9));
        numMap.put(Character.valueOf(2406), Integer.valueOf(0));
        for (Entry<Character, Integer> entry : numMap.entrySet()) {
            inStr = inStr.replaceAll(((Character) entry.getKey()).toString(), ((Integer) entry.getValue()).toString());
        }
        return inStr;
    }
}
