package com.huawei.g11n.tmr.datetime.utils.digit;

import java.util.HashMap;
import java.util.Map;

public class LocaleDigitBn extends LocaleDigit {
    public LocaleDigitBn() {
        this.pattern = "[০১২৩৪৫৬৭৮৯]+";
    }

    public String convert(String inStr) {
        HashMap<Character, Integer> numMap = new HashMap<>();
        numMap.put(2535, 1);
        numMap.put(2536, 2);
        numMap.put(2537, 3);
        numMap.put(2538, 4);
        numMap.put(2539, 5);
        numMap.put(2540, 6);
        numMap.put(2541, 7);
        numMap.put(2542, 8);
        numMap.put(2543, 9);
        numMap.put(2534, 0);
        for (Map.Entry<Character, Integer> entry : numMap.entrySet()) {
            inStr = inStr.replaceAll(entry.getKey().toString(), entry.getValue().toString());
        }
        return inStr;
    }
}
