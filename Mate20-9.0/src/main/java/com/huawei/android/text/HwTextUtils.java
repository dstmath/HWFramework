package com.huawei.android.text;

import android.text.SpannableString;
import android.text.SpannedString;
import huawei.android.provider.HwSettings;
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
                out.append(SyrillicLatinMap.get(Character.valueOf(c)));
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
        if (!isSyrillic(text, len)) {
            return text;
        }
        return getLatinString(text, len).toString();
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
        HashMap<Character, CharSequence> map = new HashMap<>();
        map.put(1025, "Ё");
        map.put(1026, "Đ");
        map.put(1027, "Ѓ");
        map.put(1028, "Є");
        map.put(1029, "Ѕ");
        map.put(1030, "І");
        map.put(1031, "Ї");
        map.put(1032, "J");
        map.put(1033, "Lj");
        map.put(1034, "Nj");
        map.put(1035, "Ć");
        map.put(1036, "Ќ");
        map.put(1037, "Ѝ");
        map.put(1038, "Ў");
        map.put(1039, "Dž");
        map.put(1040, "A");
        map.put(1041, "B");
        map.put(1042, "V");
        map.put(1043, "G");
        map.put(1044, "D");
        map.put(1045, "E");
        map.put(1046, "Ž");
        map.put(1047, "Z");
        map.put(1048, "I");
        map.put(1049, "Й");
        map.put(1050, "K");
        map.put(1051, "L");
        map.put(1052, "M");
        map.put(1053, "N");
        map.put(1054, "O");
        map.put(1055, "P");
        map.put(1056, "R");
        map.put(1057, "S");
        map.put(1058, "T");
        map.put(1059, "U");
        map.put(1060, "F");
        map.put(1061, "H");
        map.put(1062, "C");
        map.put(1063, "Č");
        map.put(1064, "Š");
        map.put(1065, "Щ");
        map.put(1066, "Ъ");
        map.put(1067, "Ы");
        map.put(1068, "Ь");
        map.put(1069, "Э");
        map.put(1070, "Ю");
        map.put(1071, "Я");
        map.put(1072, "a");
        map.put(1073, "b");
        map.put(1074, HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_V_SUFFIX);
        map.put(1075, "g");
        map.put(1076, "d");
        map.put(1077, HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_E_SUFFIX);
        map.put(1078, "ž");
        map.put(1079, HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_Z_SUFFIX);
        map.put(1080, "i");
        map.put(1081, "й");
        map.put(1082, "k");
        map.put(1083, "l");
        map.put(1084, HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_M_SUFFIX);
        map.put(1085, "n");
        map.put(1086, "o");
        map.put(1087, "p");
        map.put(1088, "r");
        map.put(1089, HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_S_SUFFIX);
        map.put(1090, "t");
        map.put(1091, "u");
        map.put(1092, "f");
        map.put(1093, "h");
        map.put(1094, HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_C_SUFFIX);
        map.put(1095, "č");
        map.put(1096, "š");
        map.put(1097, "щ");
        map.put(1098, "ъ");
        map.put(1099, "ы");
        map.put(1100, "ь");
        map.put(1101, "э");
        map.put(1102, "ю");
        map.put(1103, "я");
        map.put(1104, "ѐ");
        map.put(1105, "ё");
        map.put(1106, "đ");
        map.put(1107, "ѓ");
        map.put(1108, "є");
        map.put(1109, "ѕ");
        map.put(1110, "і");
        map.put(1111, "ї");
        map.put(1112, "j");
        map.put(1113, "lj");
        map.put(1114, "nj");
        map.put(1115, "ć");
        map.put(1116, "ќ");
        map.put(1117, "ѝ");
        map.put(1118, "ў");
        map.put(1119, "dž");
        return map;
    }
}
