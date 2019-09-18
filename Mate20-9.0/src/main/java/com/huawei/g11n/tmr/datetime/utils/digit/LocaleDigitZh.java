package com.huawei.g11n.tmr.datetime.utils.digit;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocaleDigitZh extends LocaleDigit {
    public LocaleDigitZh() {
        this.pattern = "[0-9零一二三四五六七八九十两整半科钟鍾兩]+";
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    public String convert(String inStr) {
        char c;
        String inStr2 = inStr.replaceAll("半", "30").replaceAll("钟", "00").replaceAll("鍾", "00").replaceAll("整", "00").replaceAll("一刻", "15").replaceAll("三刻", "45");
        HashMap<Character, Integer> numMap = new HashMap<>();
        int i = 1;
        numMap.put(19968, 1);
        int i2 = 2;
        numMap.put(20108, 2);
        int i3 = 3;
        numMap.put(19977, 3);
        numMap.put(22235, 4);
        numMap.put(20116, 5);
        numMap.put(20845, 6);
        numMap.put(19971, 7);
        numMap.put(20843, 8);
        numMap.put(20061, 9);
        char c2 = 38646;
        numMap.put(38646, 0);
        numMap.put(21313, 10);
        numMap.put(20004, 2);
        numMap.put(20841, 2);
        Matcher matcher = Pattern.compile("[零一二三四五六七八九十两兩]{1,10}").matcher(inStr2);
        StringBuffer strSB = new StringBuffer(inStr2);
        while (matcher.find()) {
            int res = 0;
            String hanzi = matcher.group();
            switch (hanzi.length()) {
                case 1:
                    c = c2;
                    res = numMap.get(Character.valueOf(hanzi.charAt(0))).intValue();
                    continue;
                case 2:
                    if (hanzi.charAt(0) != 21313) {
                        if (hanzi.charAt(i) != 21313) {
                            c = 38646;
                            res = (numMap.get(Character.valueOf(hanzi.charAt(0))).intValue() * 10) + numMap.get(Character.valueOf(hanzi.charAt(i))).intValue();
                            break;
                        } else {
                            c = 38646;
                            if (hanzi.charAt(0) == 38646) {
                                res = 10;
                                break;
                            } else {
                                res = numMap.get(Character.valueOf(hanzi.charAt(0))).intValue() * 10;
                                continue;
                            }
                        }
                    } else {
                        res = 10 + numMap.get(Character.valueOf(hanzi.charAt(i))).intValue();
                    }
                case 3:
                    res = (numMap.get(Character.valueOf(hanzi.charAt(0))).intValue() * 10) + numMap.get(Character.valueOf(hanzi.charAt(i2))).intValue();
                    c = 38646;
                    break;
                case 4:
                    res = (numMap.get(Character.valueOf(hanzi.charAt(0))).intValue() * 1000) + (numMap.get(Character.valueOf(hanzi.charAt(i))).intValue() * 100) + (numMap.get(Character.valueOf(hanzi.charAt(i2))).intValue() * 10) + numMap.get(Character.valueOf(hanzi.charAt(i3))).intValue();
                    c = 38646;
                    break;
                default:
                    c = c2;
                    continue;
            }
            c = 38646;
            strSB.replace(strSB.indexOf(hanzi), strSB.indexOf(hanzi) + hanzi.length(), "" + res);
            c2 = c;
            i = 1;
            i2 = 2;
            i3 = 3;
        }
        return strSB.toString();
    }
}
