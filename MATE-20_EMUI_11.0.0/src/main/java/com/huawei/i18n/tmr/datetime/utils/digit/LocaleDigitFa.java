package com.huawei.i18n.tmr.datetime.utils.digit;

import java.util.HashMap;
import java.util.Map;

public class LocaleDigitFa extends LocaleDigit {
    public LocaleDigitFa() {
        this.pattern = "[۰۱۲۳۴۵۶۷۸۹]+";
    }

    @Override // com.huawei.i18n.tmr.datetime.utils.digit.LocaleDigit
    public String convert(String str) {
        HashMap<Character, Integer> numMap = new HashMap<>();
        numMap.put((char) 1777, 1);
        numMap.put((char) 1778, 2);
        numMap.put((char) 1779, 3);
        numMap.put((char) 1780, 4);
        numMap.put((char) 1781, 5);
        numMap.put((char) 1782, 6);
        numMap.put((char) 1783, 7);
        numMap.put((char) 1784, 8);
        numMap.put((char) 1785, 9);
        numMap.put((char) 1776, 0);
        String inStr = str;
        for (Map.Entry<Character, Integer> entry : numMap.entrySet()) {
            inStr = inStr.replaceAll(entry.getKey().toString(), entry.getValue().toString());
        }
        return inStr;
    }
}
