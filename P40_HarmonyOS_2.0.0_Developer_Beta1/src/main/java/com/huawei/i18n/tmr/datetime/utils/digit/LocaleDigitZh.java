package com.huawei.i18n.tmr.datetime.utils.digit;

import com.huawei.android.os.storage.StorageManagerExt;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocaleDigitZh extends LocaleDigit {
    public LocaleDigitZh() {
        this.pattern = "[0-9零一二三四五六七八九十两整半科钟鍾兩]+";
    }

    @Override // com.huawei.i18n.tmr.datetime.utils.digit.LocaleDigit
    public String convert(String str) {
        String inStr = str.replaceAll("半", "30").replaceAll("钟", "00").replaceAll("鍾", "00").replaceAll("整", "00").replaceAll("一刻", "15").replaceAll("三刻", "45");
        return getConvertString(inStr, getNumMap(), Pattern.compile("[零一二三四五六七八九十两兩]{1,10}").matcher(inStr));
    }

    private String getConvertString(String inStr, HashMap<Character, Integer> numMap, Matcher matcher) {
        StringBuffer stringBuffer = new StringBuffer(inStr);
        while (matcher.find()) {
            int res = 0;
            String hanzi = matcher.group();
            int length = hanzi.length();
            if (length == 1) {
                res = numMap.get(Character.valueOf(hanzi.charAt(0))).intValue();
            } else if (length != 2) {
                if (length == 3) {
                    res = (numMap.get(Character.valueOf(hanzi.charAt(0))).intValue() * 10) + numMap.get(Character.valueOf(hanzi.charAt(2))).intValue();
                } else if (length == 4) {
                    res = (numMap.get(Character.valueOf(hanzi.charAt(0))).intValue() * 1000) + (numMap.get(Character.valueOf(hanzi.charAt(1))).intValue() * 100) + (numMap.get(Character.valueOf(hanzi.charAt(2))).intValue() * 10) + numMap.get(Character.valueOf(hanzi.charAt(3))).intValue();
                }
            } else if (hanzi.charAt(0) == 21313) {
                res = numMap.get(Character.valueOf(hanzi.charAt(1))).intValue() + 10;
            } else if (hanzi.charAt(1) != 21313) {
                res = (numMap.get(Character.valueOf(hanzi.charAt(0))).intValue() * 10) + numMap.get(Character.valueOf(hanzi.charAt(1))).intValue();
            } else if (hanzi.charAt(0) != 38646) {
                res = numMap.get(Character.valueOf(hanzi.charAt(0))).intValue() * 10;
            } else {
                res = 10;
            }
            int indexOf = stringBuffer.indexOf(hanzi);
            int indexOf2 = stringBuffer.indexOf(hanzi) + hanzi.length();
            stringBuffer.replace(indexOf, indexOf2, StorageManagerExt.INVALID_KEY_DESC + res);
        }
        return stringBuffer.toString();
    }

    private HashMap<Character, Integer> getNumMap() {
        HashMap<Character, Integer> numMap = new HashMap<>();
        numMap.put((char) 19968, 1);
        numMap.put((char) 20108, 2);
        numMap.put((char) 19977, 3);
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
        return numMap;
    }
}
