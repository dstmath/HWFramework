package com.huawei.g11n.tmr.datetime.utils.digit;

import java.util.HashMap;
import java.util.Map;

public class LocaleDigitNe extends LocaleDigit {
    public LocaleDigitNe() {
        this.pattern = "[०१२३४५६७८९]+";
    }

    @Override // com.huawei.g11n.tmr.datetime.utils.digit.LocaleDigit
    public String convert(String inStr) {
        HashMap<Character, Integer> numMap = new HashMap<>();
        numMap.put((char) 2407, 1);
        numMap.put((char) 2408, 2);
        numMap.put((char) 2409, 3);
        numMap.put((char) 2410, 4);
        numMap.put((char) 2411, 5);
        numMap.put((char) 2412, 6);
        numMap.put((char) 2413, 7);
        numMap.put((char) 2414, 8);
        numMap.put((char) 2415, 9);
        numMap.put((char) 2406, 0);
        for (Map.Entry<Character, Integer> entry : numMap.entrySet()) {
            inStr = inStr.replaceAll(entry.getKey().toString(), entry.getValue().toString());
        }
        return inStr;
    }
}
