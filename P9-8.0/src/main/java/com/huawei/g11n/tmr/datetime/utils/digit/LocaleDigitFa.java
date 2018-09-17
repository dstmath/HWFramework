package com.huawei.g11n.tmr.datetime.utils.digit;

import java.util.HashMap;
import java.util.Map.Entry;

public class LocaleDigitFa extends LocaleDigit {
    public LocaleDigitFa() {
        this.pattern = "[۰۱۲۳۴۵۶۷۸۹]+";
    }

    public String convert(String inStr) {
        HashMap<Character, Integer> numMap = new HashMap();
        numMap.put(Character.valueOf(1777), Integer.valueOf(1));
        numMap.put(Character.valueOf(1778), Integer.valueOf(2));
        numMap.put(Character.valueOf(1779), Integer.valueOf(3));
        numMap.put(Character.valueOf(1780), Integer.valueOf(4));
        numMap.put(Character.valueOf(1781), Integer.valueOf(5));
        numMap.put(Character.valueOf(1782), Integer.valueOf(6));
        numMap.put(Character.valueOf(1783), Integer.valueOf(7));
        numMap.put(Character.valueOf(1784), Integer.valueOf(8));
        numMap.put(Character.valueOf(1785), Integer.valueOf(9));
        numMap.put(Character.valueOf(1776), Integer.valueOf(0));
        for (Entry<Character, Integer> entry : numMap.entrySet()) {
            inStr = inStr.replaceAll(((Character) entry.getKey()).toString(), ((Integer) entry.getValue()).toString());
        }
        return inStr;
    }
}
