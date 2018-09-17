package com.huawei.android.text;

import android.text.SpannableString;
import android.text.SpannedString;
import huawei.android.provider.HwSettings.System;
import java.util.HashMap;

public class HwTextUtils {
    private static HashMap<Character, CharSequence> SyrillicLatinMap = SyrillicToLatin();

    private static boolean isSyrillic(String chs, int len) {
        for (int i = 0; i < len; i++) {
            char c = chs.charAt(i);
            if (c > 1024 && c < 1120) {
                return true;
            }
        }
        return false;
    }

    private static StringBuilder getLatinString(String chs, int len) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < len; i++) {
            char c = chs.charAt(i);
            if (c <= 1024 || c >= 1120) {
                out.append(c);
            } else {
                out.append((CharSequence) SyrillicLatinMap.get(Character.valueOf(c)));
            }
        }
        return out;
    }

    private static int getLatinStringLen(CharSequence chs, int len) {
        return getLatinString(chs.toString(), len).length();
    }

    public static String serbianSyrillic2Latin(String text) {
        if (text == null) {
            return null;
        }
        int len = text.length();
        if (isSyrillic(text, len)) {
            return getLatinString(text, len).toString();
        }
        return text;
    }

    public static CharSequence serbianSyrillic2Latin(CharSequence text) {
        if (text == null) {
            return null;
        }
        if (text instanceof String) {
            return serbianSyrillic2Latin((String) text);
        }
        if (!(text instanceof SpannedString)) {
            return text;
        }
        int len = text.length();
        if (!isSyrillic(text.toString(), len)) {
            return text;
        }
        SpannableString newText = new SpannableString(getLatinString(text.toString(), len));
        SpannedString sp = (SpannedString) text;
        int end = sp.length();
        Object[] spans = sp.getSpans(0, end, Object.class);
        for (int i = 0; i < spans.length; i++) {
            int st = sp.getSpanStart(spans[i]);
            int en = sp.getSpanEnd(spans[i]);
            int fl = sp.getSpanFlags(spans[i]);
            if (st < 0) {
                st = 0;
            }
            if (en > end) {
                en = end;
            }
            newText.setSpan(spans[i], getLatinStringLen(text.subSequence(0, st), st), getLatinStringLen(text.subSequence(0, en), en), fl);
        }
        return newText;
    }

    private static HashMap<Character, CharSequence> SyrillicToLatin() {
        HashMap<Character, CharSequence> map = new HashMap();
        map.put(Character.valueOf(1025), "Ё");
        map.put(Character.valueOf(1026), "Đ");
        map.put(Character.valueOf(1027), "Ѓ");
        map.put(Character.valueOf(1028), "Є");
        map.put(Character.valueOf(1029), "Ѕ");
        map.put(Character.valueOf(1030), "І");
        map.put(Character.valueOf(1031), "Ї");
        map.put(Character.valueOf(1032), "J");
        map.put(Character.valueOf(1033), "Lj");
        map.put(Character.valueOf(1034), "Nj");
        map.put(Character.valueOf(1035), "Ć");
        map.put(Character.valueOf(1036), "Ќ");
        map.put(Character.valueOf(1037), "Ѝ");
        map.put(Character.valueOf(1038), "Ў");
        map.put(Character.valueOf(1039), "Dž");
        map.put(Character.valueOf(1040), "A");
        map.put(Character.valueOf(1041), "B");
        map.put(Character.valueOf(1042), "V");
        map.put(Character.valueOf(1043), "G");
        map.put(Character.valueOf(1044), "D");
        map.put(Character.valueOf(1045), "E");
        map.put(Character.valueOf(1046), "Ž");
        map.put(Character.valueOf(1047), "Z");
        map.put(Character.valueOf(1048), "I");
        map.put(Character.valueOf(1049), "Й");
        map.put(Character.valueOf(1050), "K");
        map.put(Character.valueOf(1051), "L");
        map.put(Character.valueOf(1052), "M");
        map.put(Character.valueOf(1053), "N");
        map.put(Character.valueOf(1054), "O");
        map.put(Character.valueOf(1055), "P");
        map.put(Character.valueOf(1056), "R");
        map.put(Character.valueOf(1057), "S");
        map.put(Character.valueOf(1058), "T");
        map.put(Character.valueOf(1059), "U");
        map.put(Character.valueOf(1060), "F");
        map.put(Character.valueOf(1061), "H");
        map.put(Character.valueOf(1062), "C");
        map.put(Character.valueOf(1063), "Č");
        map.put(Character.valueOf(1064), "Š");
        map.put(Character.valueOf(1065), "Щ");
        map.put(Character.valueOf(1066), "Ъ");
        map.put(Character.valueOf(1067), "Ы");
        map.put(Character.valueOf(1068), "Ь");
        map.put(Character.valueOf(1069), "Э");
        map.put(Character.valueOf(1070), "Ю");
        map.put(Character.valueOf(1071), "Я");
        map.put(Character.valueOf(1072), "a");
        map.put(Character.valueOf(1073), "b");
        map.put(Character.valueOf(1074), System.FINGERSENSE_KNUCKLE_GESTURE_V_SUFFIX);
        map.put(Character.valueOf(1075), "g");
        map.put(Character.valueOf(1076), "d");
        map.put(Character.valueOf(1077), System.FINGERSENSE_KNUCKLE_GESTURE_E_SUFFIX);
        map.put(Character.valueOf(1078), "ž");
        map.put(Character.valueOf(1079), System.FINGERSENSE_KNUCKLE_GESTURE_Z_SUFFIX);
        map.put(Character.valueOf(1080), "i");
        map.put(Character.valueOf(1081), "й");
        map.put(Character.valueOf(1082), "k");
        map.put(Character.valueOf(1083), "l");
        map.put(Character.valueOf(1084), System.FINGERSENSE_KNUCKLE_GESTURE_M_SUFFIX);
        map.put(Character.valueOf(1085), "n");
        map.put(Character.valueOf(1086), "o");
        map.put(Character.valueOf(1087), "p");
        map.put(Character.valueOf(1088), "r");
        map.put(Character.valueOf(1089), System.FINGERSENSE_KNUCKLE_GESTURE_S_SUFFIX);
        map.put(Character.valueOf(1090), "t");
        map.put(Character.valueOf(1091), "u");
        map.put(Character.valueOf(1092), "f");
        map.put(Character.valueOf(1093), "h");
        map.put(Character.valueOf(1094), System.FINGERSENSE_KNUCKLE_GESTURE_C_SUFFIX);
        map.put(Character.valueOf(1095), "č");
        map.put(Character.valueOf(1096), "š");
        map.put(Character.valueOf(1097), "щ");
        map.put(Character.valueOf(1098), "ъ");
        map.put(Character.valueOf(1099), "ы");
        map.put(Character.valueOf(1100), "ь");
        map.put(Character.valueOf(1101), "э");
        map.put(Character.valueOf(1102), "ю");
        map.put(Character.valueOf(1103), "я");
        map.put(Character.valueOf(1104), "ѐ");
        map.put(Character.valueOf(1105), "ё");
        map.put(Character.valueOf(1106), "đ");
        map.put(Character.valueOf(1107), "ѓ");
        map.put(Character.valueOf(1108), "є");
        map.put(Character.valueOf(1109), "ѕ");
        map.put(Character.valueOf(1110), "і");
        map.put(Character.valueOf(1111), "ї");
        map.put(Character.valueOf(1112), "j");
        map.put(Character.valueOf(1113), "lj");
        map.put(Character.valueOf(1114), "nj");
        map.put(Character.valueOf(1115), "ć");
        map.put(Character.valueOf(1116), "ќ");
        map.put(Character.valueOf(1117), "ѝ");
        map.put(Character.valueOf(1118), "ў");
        map.put(Character.valueOf(1119), "dž");
        return map;
    }
}
