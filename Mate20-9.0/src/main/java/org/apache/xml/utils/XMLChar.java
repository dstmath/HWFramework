package org.apache.xml.utils;

public class XMLChar {
    private static final byte[] CHARS = new byte[65536];
    public static final int MASK_CONTENT = 32;
    public static final int MASK_NAME = 8;
    public static final int MASK_NAME_START = 4;
    public static final int MASK_NCNAME = 128;
    public static final int MASK_NCNAME_START = 64;
    public static final int MASK_PUBID = 16;
    public static final int MASK_SPACE = 2;
    public static final int MASK_VALID = 1;

    static {
        int[] pubidRange;
        int[] charRange = {9, 10, 13, 13, 32, 55295, 57344, 65533};
        int[] spaceChar = {32, 9, 13, 10};
        int[] nameChar = {45, 46};
        int[] nameStartChar = {58, 95};
        int[] pubidChar = {10, 13, 32, 33, 35, 36, 37, 61, 95};
        int[] pubidRange2 = {39, 59, 63, 90, 97, 122};
        int[] letterRange = {65, 90, 97, 122, 192, 214, 216, 246, 248, 305, 308, 318, 321, 328, 330, 382, 384, 451, 461, 496, 500, 501, 506, 535, 592, 680, 699, 705, 904, 906, 910, 929, 931, 974, 976, 982, 994, 1011, 1025, 1036, 1038, 1103, 1105, 1116, 1118, 1153, 1168, 1220, 1223, 1224, 1227, 1228, 1232, 1259, 1262, 1269, 1272, 1273, 1329, 1366, 1377, 1414, 1488, 1514, 1520, 1522, 1569, 1594, 1601, 1610, 1649, 1719, 1722, 1726, 1728, 1742, 1744, 1747, 1765, 1766, 2309, 2361, 2392, 2401, 2437, 2444, 2447, 2448, 2451, 2472, 2474, 2480, 2486, 2489, 2524, 2525, 2527, 2529, 2544, 2545, 2565, 2570, 2575, 2576, 2579, 2600, 2602, 2608, 2610, 2611, 2613, 2614, 2616, 2617, 2649, 2652, 2674, 2676, 2693, 2699, 2703, 2705, 2707, 2728, 2730, 2736, 2738, 2739, 2741, 2745, 2821, 2828, 2831, 2832, 2835, 2856, 2858, 2864, 2866, 2867, 2870, 2873, 2908, 2909, 2911, 2913, 2949, 2954, 2958, 2960, 2962, 2965, 2969, 2970, 2974, 2975, 2979, 2980, 2984, 2986, 2990, 2997, 2999, 3001, 3077, 3084, 3086, 3088, 3090, 3112, 3114, 3123, 3125, 3129, 3168, 3169, 3205, 3212, 3214, 3216, 3218, 3240, 3242, 3251, 3253, 3257, 3296, 3297, 3333, 3340, 3342, 3344, 3346, 3368, 3370, 3385, 3424, 3425, 3585, 3630, 3634, 3635, 3648, 3653, 3713, 3714, 3719, 3720, 3732, 3735, 3737, 3743, 3745, 3747, 3754, 3755, 3757, 3758, 3762, 3763, 3776, 3780, 3904, 3911, 3913, 3945, 4256, 4293, 4304, 4342, 4354, 4355, 4357, 4359, 4363, 4364, 4366, 4370, 4436, 4437, 4447, 4449, 4461, 4462, 4466, 4467, 4526, 4527, 4535, 4536, 4540, 4546, 7680, 7835, 7840, 7929, 7936, 7957, 7960, 7965, 7968, 8005, 8008, 8013, 8016, 8023, 8031, 8061, 8064, 8116, 8118, 8124, 8130, 8132, 8134, 8140, 8144, 8147, 8150, 8155, 8160, 8172, 8178, 8180, 8182, 8188, 8490, 8491, 8576, 8578, 12353, 12436, 12449, 12538, 12549, 12588, 44032, 55203, 12321, 12329, 19968, 40869};
        int[] letterChar = {902, 908, 986, 988, 990, 992, 1369, 1749, 2365, 2482, 2654, 2701, 2749, 2784, 2877, 2972, 3294, 3632, 3716, 3722, 3725, 3749, 3751, 3760, 3773, 4352, 4361, 4412, 4414, 4416, 4428, 4430, 4432, 4441, 4451, 4453, 4455, 4457, 4469, 4510, 4520, 4523, 4538, 4587, 4592, 4601, 8025, 8027, 8029, 8126, 8486, 8494, 12295};
        int[] combiningCharRange = {768, 837, 864, 865, 1155, 1158, 1425, 1441, 1443, 1465, 1467, 1469, 1473, 1474, 1611, 1618, 1750, 1756, 1757, 1759, 1760, 1764, 1767, 1768, 1770, 1773, 2305, 2307, 2366, 2380, 2385, 2388, 2402, 2403, 2433, 2435, 2496, 2500, 2503, 2504, 2507, 2509, 2530, 2531, 2624, 2626, 2631, 2632, 2635, 2637, 2672, 2673, 2689, 2691, 2750, 2757, 2759, 2761, 2763, 2765, 2817, 2819, 2878, 2883, 2887, 2888, 2891, 2893, 2902, 2903, 2946, 2947, 3006, 3010, 3014, 3016, 3018, 3021, 3073, 3075, 3134, 3140, 3142, 3144, 3146, 3149, 3157, 3158, 3202, 3203, 3262, 3268, 3270, 3272, 3274, 3277, 3285, 3286, 3330, 3331, 3390, 3395, 3398, 3400, 3402, 3405, 3636, 3642, 3655, 3662, 3764, 3769, 3771, 3772, 3784, 3789, 3864, 3865, 3953, 3972, 3974, 3979, 3984, 3989, 3993, 4013, 4017, 4023, 8400, 8412, 12330, 12335};
        int[] combiningCharChar = {1471, 1476, 1648, 2364, 2381, 2492, 2494, 2495, 2519, 2562, 2620, 2622, 2623, 2748, 2876, 3031, 3415, 3633, 3761, 3893, 3895, 3897, 3902, 3903, 3991, 4025, 8417, 12441, 12442};
        int[] digitRange = {48, 57, 1632, 1641, 1776, 1785, 2406, 2415, 2534, 2543, 2662, 2671, 2790, 2799, 2918, 2927, 3047, 3055, 3174, 3183, 3302, 3311, 3430, 3439, 3664, 3673, 3792, 3801, 3872, 3881};
        int[] extenderRange = {12337, 12341, 12445, 12446, 12540, 12542};
        int[] extenderChar = {183, 720, 721, 903, 1600, 3654, 3782, 12293};
        int[] specialChar = {60, 38, 10, 13, 93};
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= charRange.length) {
                break;
            }
            int j = charRange[i2];
            while (true) {
                pubidRange = pubidRange2;
                if (j > charRange[i2 + 1]) {
                    break;
                }
                byte[] bArr = CHARS;
                bArr[j] = (byte) (bArr[j] | 33);
                j++;
                pubidRange2 = pubidRange;
                charRange = charRange;
            }
            i = i2 + 2;
            pubidRange2 = pubidRange;
        }
        int[] pubidRange3 = pubidRange2;
        for (int i3 = 0; i3 < specialChar.length; i3++) {
            CHARS[specialChar[i3]] = (byte) (CHARS[specialChar[i3]] & -33);
        }
        for (int i4 : spaceChar) {
            byte[] bArr2 = CHARS;
            bArr2[i4] = (byte) (bArr2[i4] | 2);
        }
        for (int i5 : nameStartChar) {
            byte[] bArr3 = CHARS;
            bArr3[i5] = (byte) (bArr3[i5] | 204);
        }
        for (int i6 = 0; i6 < letterRange.length; i6 += 2) {
            for (int j2 = letterRange[i6]; j2 <= letterRange[i6 + 1]; j2++) {
                byte[] bArr4 = CHARS;
                bArr4[j2] = (byte) (bArr4[j2] | 204);
            }
        }
        for (int i7 : letterChar) {
            byte[] bArr5 = CHARS;
            bArr5[i7] = (byte) (bArr5[i7] | 204);
        }
        for (int i8 : nameChar) {
            byte[] bArr6 = CHARS;
            bArr6[i8] = (byte) (bArr6[i8] | 136);
        }
        for (int i9 = 0; i9 < digitRange.length; i9 += 2) {
            for (int j3 = digitRange[i9]; j3 <= digitRange[i9 + 1]; j3++) {
                byte[] bArr7 = CHARS;
                bArr7[j3] = (byte) (bArr7[j3] | 136);
            }
        }
        for (int i10 = 0; i10 < combiningCharRange.length; i10 += 2) {
            for (int j4 = combiningCharRange[i10]; j4 <= combiningCharRange[i10 + 1]; j4++) {
                byte[] bArr8 = CHARS;
                bArr8[j4] = (byte) (bArr8[j4] | 136);
            }
        }
        for (int i11 : combiningCharChar) {
            byte[] bArr9 = CHARS;
            bArr9[i11] = (byte) (bArr9[i11] | 136);
        }
        for (int i12 = 0; i12 < extenderRange.length; i12 += 2) {
            for (int j5 = extenderRange[i12]; j5 <= extenderRange[i12 + 1]; j5++) {
                byte[] bArr10 = CHARS;
                bArr10[j5] = (byte) (bArr10[j5] | 136);
            }
        }
        for (int i13 : extenderChar) {
            byte[] bArr11 = CHARS;
            bArr11[i13] = (byte) (bArr11[i13] | 136);
        }
        byte[] bArr12 = CHARS;
        bArr12[58] = (byte) (bArr12[58] & -193);
        for (int i14 : pubidChar) {
            byte[] bArr13 = CHARS;
            bArr13[i14] = (byte) (bArr13[i14] | 16);
        }
        int i15 = 0;
        while (true) {
            int i16 = i15;
            int[] pubidRange4 = pubidRange3;
            if (i16 < pubidRange4.length) {
                int j6 = pubidRange4[i16];
                while (j6 <= pubidRange4[i16 + 1]) {
                    byte[] bArr14 = CHARS;
                    bArr14[j6] = (byte) (bArr14[j6] | 16);
                    j6++;
                    extenderChar = extenderChar;
                }
                i15 = i16 + 2;
                pubidRange3 = pubidRange4;
            } else {
                return;
            }
        }
    }

    public static boolean isSupplemental(int c) {
        return c >= 65536 && c <= 1114111;
    }

    public static int supplemental(char h, char l) {
        return ((h - 55296) * 1024) + (l - 56320) + 65536;
    }

    public static char highSurrogate(int c) {
        return (char) (((c - 65536) >> 10) + 55296);
    }

    public static char lowSurrogate(int c) {
        return (char) (((c - 65536) & 1023) + 56320);
    }

    public static boolean isHighSurrogate(int c) {
        return 55296 <= c && c <= 56319;
    }

    public static boolean isLowSurrogate(int c) {
        return 56320 <= c && c <= 57343;
    }

    public static boolean isValid(int c) {
        if (c >= 65536 || (CHARS[c] & 1) == 0) {
            return 65536 <= c && c <= 1114111;
        }
        return true;
    }

    public static boolean isInvalid(int c) {
        return !isValid(c);
    }

    public static boolean isContent(int c) {
        return (c < 65536 && (CHARS[c] & 32) != 0) || (65536 <= c && c <= 1114111);
    }

    public static boolean isMarkup(int c) {
        return c == 60 || c == 38 || c == 37;
    }

    public static boolean isSpace(int c) {
        return c < 65536 && (CHARS[c] & 2) != 0;
    }

    public static boolean isNameStart(int c) {
        return c < 65536 && (CHARS[c] & 4) != 0;
    }

    public static boolean isName(int c) {
        return c < 65536 && (CHARS[c] & 8) != 0;
    }

    public static boolean isNCNameStart(int c) {
        return c < 65536 && (CHARS[c] & 64) != 0;
    }

    public static boolean isNCName(int c) {
        return c < 65536 && (CHARS[c] & 128) != 0;
    }

    public static boolean isPubid(int c) {
        return c < 65536 && (CHARS[c] & 16) != 0;
    }

    public static boolean isValidName(String name) {
        if (name.length() == 0) {
            return false;
        }
        char ch = name.charAt(0);
        if (!isNameStart(ch)) {
            return false;
        }
        char c = ch;
        for (int i = 1; i < name.length(); i++) {
            if (!isName(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidNCName(String ncName) {
        if (ncName.length() == 0) {
            return false;
        }
        char ch = ncName.charAt(0);
        if (!isNCNameStart(ch)) {
            return false;
        }
        char c = ch;
        for (int i = 1; i < ncName.length(); i++) {
            if (!isNCName(ncName.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidNmtoken(String nmtoken) {
        if (nmtoken.length() == 0) {
            return false;
        }
        for (int i = 0; i < nmtoken.length(); i++) {
            if (!isName(nmtoken.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidIANAEncoding(String ianaEncoding) {
        if (ianaEncoding != null) {
            int length = ianaEncoding.length();
            if (length > 0) {
                char c = ianaEncoding.charAt(0);
                if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
                    char c2 = c;
                    for (int i = 1; i < length; i++) {
                        char c3 = ianaEncoding.charAt(i);
                        if ((c3 < 'A' || c3 > 'Z') && ((c3 < 'a' || c3 > 'z') && ((c3 < '0' || c3 > '9') && c3 != '.' && c3 != '_' && c3 != '-'))) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isValidJavaEncoding(String javaEncoding) {
        if (javaEncoding != null) {
            int length = javaEncoding.length();
            if (length > 0) {
                for (int i = 1; i < length; i++) {
                    char c = javaEncoding.charAt(i);
                    if ((c < 'A' || c > 'Z') && ((c < 'a' || c > 'z') && ((c < '0' || c > '9') && c != '.' && c != '_' && c != '-'))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static boolean isValidQName(String str) {
        int colon = str.indexOf(58);
        boolean z = false;
        if (colon == 0 || colon == str.length() - 1) {
            return false;
        }
        if (colon <= 0) {
            return isValidNCName(str);
        }
        String prefix = str.substring(0, colon);
        String localPart = str.substring(colon + 1);
        if (isValidNCName(prefix) && isValidNCName(localPart)) {
            z = true;
        }
        return z;
    }
}
