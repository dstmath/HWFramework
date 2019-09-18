package com.huawei.g11n.tmr.datetime.utils.digit;

import java.util.HashMap;
import java.util.Map;

public class LocaleDigitFa extends LocaleDigit {
    public LocaleDigitFa() {
        this.pattern = "[۰۱۲۳۴۵۶۷۸۹]+";
    }

    public String convert(String inStr) {
        HashMap<Character, Integer> numMap = new HashMap<>();
        numMap.put(1777, 1);
        numMap.put(1778, 2);
        numMap.put(1779, 3);
        numMap.put(1780, 4);
        numMap.put(1781, 5);
        numMap.put(1782, 6);
        numMap.put(1783, 7);
        numMap.put(1784, 8);
        numMap.put(1785, 9);
        numMap.put(1776, 0);
        for (Map.Entry<Character, Integer> entry : numMap.entrySet()) {
            inStr = inStr.replaceAll(entry.getKey().toString(), entry.getValue().toString());
        }
        return inStr;
    }
}
