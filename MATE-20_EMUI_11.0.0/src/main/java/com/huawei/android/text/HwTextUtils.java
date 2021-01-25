package com.huawei.android.text;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.text.SpannableString;
import android.text.SpannedString;
import huawei.android.provider.HwSettings;
import java.util.HashMap;

public class HwTextUtils {
    private static HashMap<Character, CharSequence> SyrillicLatinMap = SyrillicToLatin();

    private static boolean isSyrillic(String chs, int len) {
        for (int i = 0; i < len; i++) {
            char ch = chs.charAt(i);
            if (ch > 1024 && ch < 1120) {
                return true;
            }
        }
        return false;
    }

    private static StringBuilder getLatinString(String chs, int len) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < len; i++) {
            char ch = chs.charAt(i);
            if (ch <= 1024 || ch >= 1120) {
                out.append(ch);
            } else {
                out.append(SyrillicLatinMap.get(Character.valueOf(ch)));
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
            if (st < 0) {
                st = 0;
            }
            if (en > end) {
                en = end;
            }
            newText.setSpan(spans[i], getLatinStringLen(text.subSequence(0, st), st), getLatinStringLen(text.subSequence(0, en), en), sp.getSpanFlags(spans[i]));
        }
        return newText;
    }

    private static HashMap<Character, CharSequence> SyrillicToLatin() {
        HashMap<Character, CharSequence> map = new HashMap<>();
        map.put((char) 1025, "Ё");
        map.put((char) 1026, "Đ");
        map.put((char) 1027, "Ѓ");
        map.put((char) 1028, "Є");
        map.put((char) 1029, "Ѕ");
        map.put((char) 1030, "І");
        map.put((char) 1031, "Ї");
        map.put((char) 1032, "J");
        map.put((char) 1033, "Lj");
        map.put((char) 1034, "Nj");
        map.put((char) 1035, "Ć");
        map.put((char) 1036, "Ќ");
        map.put((char) 1037, "Ѝ");
        map.put((char) 1038, "Ў");
        map.put((char) 1039, "Dž");
        map.put((char) 1040, "A");
        map.put((char) 1041, "B");
        map.put((char) 1042, "V");
        map.put((char) 1043, "G");
        map.put((char) 1044, "D");
        map.put((char) 1045, "E");
        map.put((char) 1046, "Ž");
        map.put((char) 1047, AppMngConstant.APP_START_ZFLAG_KEY);
        map.put((char) 1048, "I");
        map.put((char) 1049, "Й");
        map.put((char) 1050, "K");
        map.put((char) 1051, "L");
        map.put((char) 1052, "M");
        map.put((char) 1053, "N");
        map.put((char) 1054, "O");
        map.put((char) 1055, "P");
        map.put((char) 1056, "R");
        map.put((char) 1057, "S");
        map.put((char) 1058, "T");
        map.put((char) 1059, "U");
        map.put((char) 1060, "F");
        map.put((char) 1061, "H");
        map.put((char) 1062, "C");
        map.put((char) 1063, "Č");
        map.put((char) 1064, "Š");
        map.put((char) 1065, "Щ");
        map.put((char) 1066, "Ъ");
        map.put((char) 1067, "Ы");
        map.put((char) 1068, "Ь");
        map.put((char) 1069, "Э");
        map.put((char) 1070, "Ю");
        map.put((char) 1071, "Я");
        map.put((char) 1072, "a");
        map.put((char) 1073, "b");
        map.put((char) 1074, HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_V_SUFFIX);
        map.put((char) 1075, "g");
        map.put((char) 1076, "d");
        map.put((char) 1077, HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_E_SUFFIX);
        map.put((char) 1078, "ž");
        map.put((char) 1079, HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_Z_SUFFIX);
        map.put((char) 1080, "i");
        map.put((char) 1081, "й");
        map.put((char) 1082, "k");
        map.put((char) 1083, "l");
        map.put((char) 1084, HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_M_SUFFIX);
        map.put((char) 1085, "n");
        map.put((char) 1086, "o");
        map.put((char) 1087, "p");
        map.put((char) 1088, "r");
        map.put((char) 1089, HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_S_SUFFIX);
        map.put((char) 1090, "t");
        map.put((char) 1091, "u");
        map.put((char) 1092, "f");
        map.put((char) 1093, "h");
        map.put((char) 1094, HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_C_SUFFIX);
        map.put((char) 1095, "č");
        map.put((char) 1096, "š");
        map.put((char) 1097, "щ");
        map.put((char) 1098, "ъ");
        map.put((char) 1099, "ы");
        map.put((char) 1100, "ь");
        map.put((char) 1101, "э");
        map.put((char) 1102, "ю");
        map.put((char) 1103, "я");
        map.put((char) 1104, "ѐ");
        map.put((char) 1105, "ё");
        map.put((char) 1106, "đ");
        map.put((char) 1107, "ѓ");
        map.put((char) 1108, "є");
        map.put((char) 1109, "ѕ");
        map.put((char) 1110, "і");
        map.put((char) 1111, "ї");
        map.put((char) 1112, "j");
        map.put((char) 1113, "lj");
        map.put((char) 1114, "nj");
        map.put((char) 1115, "ć");
        map.put((char) 1116, "ќ");
        map.put((char) 1117, "ѝ");
        map.put((char) 1118, "ў");
        map.put((char) 1119, "dž");
        return map;
    }
}
