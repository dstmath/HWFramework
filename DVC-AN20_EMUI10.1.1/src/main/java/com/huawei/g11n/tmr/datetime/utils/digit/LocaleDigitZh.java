package com.huawei.g11n.tmr.datetime.utils.digit;

import com.huawei.uikit.effect.BuildConfig;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocaleDigitZh extends LocaleDigit {
    public LocaleDigitZh() {
        this.pattern = "[0-9零一二三四五六七八九十两整半科钟鍾兩]+";
    }

    @Override // com.huawei.g11n.tmr.datetime.utils.digit.LocaleDigit
    public String convert(String inStr) {
        char c;
        String inStr2 = inStr.replaceAll("半", "30").replaceAll("钟", "00").replaceAll("鍾", "00").replaceAll("整", "00").replaceAll("一刻", "15").replaceAll("三刻", "45");
        HashMap<Character, Integer> numMap = new HashMap<>();
        int i = 1;
        numMap.put((char) 19968, 1);
        int i2 = 2;
        numMap.put((char) 20108, 2);
        int i3 = 3;
        numMap.put((char) 19977, 3);
        int i4 = 4;
        numMap.put((char) 22235, 4);
        numMap.put((char) 20116, 5);
        numMap.put((char) 20845, 6);
        numMap.put((char) 19971, 7);
        numMap.put((char) 20843, 8);
        numMap.put((char) 20061, 9);
        numMap.put((char) 38646, 0);
        numMap.put((char) 21313, 10);
        numMap.put((char) 20004, 2);
        numMap.put((char) 20841, 2);
        Matcher matcher = Pattern.compile("[零一二三四五六七八九十两兩]{1,10}").matcher(inStr2);
        StringBuffer strSB = new StringBuffer(inStr2);
        while (matcher.find()) {
            int res = 0;
            String hanzi = matcher.group();
            int length = hanzi.length();
            if (length == i) {
                c = 38646;
                res = numMap.get(Character.valueOf(hanzi.charAt(0))).intValue();
            } else if (length != i2) {
                if (length == i3) {
                    res = (numMap.get(Character.valueOf(hanzi.charAt(0))).intValue() * 10) + numMap.get(Character.valueOf(hanzi.charAt(i2))).intValue();
                    c = 38646;
                } else if (length != i4) {
                    c = 38646;
                } else {
                    res = (numMap.get(Character.valueOf(hanzi.charAt(0))).intValue() * 1000) + (numMap.get(Character.valueOf(hanzi.charAt(i))).intValue() * 100) + (numMap.get(Character.valueOf(hanzi.charAt(i2))).intValue() * 10) + numMap.get(Character.valueOf(hanzi.charAt(i3))).intValue();
                    c = 38646;
                }
            } else if (hanzi.charAt(0) == 21313) {
                res = numMap.get(Character.valueOf(hanzi.charAt(i))).intValue() + 10;
                c = 38646;
            } else if (hanzi.charAt(i) == 21313) {
                c = 38646;
                if (hanzi.charAt(0) != 38646) {
                    res = numMap.get(Character.valueOf(hanzi.charAt(0))).intValue() * 10;
                } else {
                    res = 10;
                }
            } else {
                c = 38646;
                res = (numMap.get(Character.valueOf(hanzi.charAt(0))).intValue() * 10) + numMap.get(Character.valueOf(hanzi.charAt(i))).intValue();
            }
            strSB.replace(strSB.indexOf(hanzi), strSB.indexOf(hanzi) + hanzi.length(), BuildConfig.FLAVOR + res);
            i = 1;
            i2 = 2;
            i3 = 3;
            i4 = 4;
        }
        return strSB.toString();
    }
}
