package ohos.com.sun.org.apache.xml.internal.utils;

import ohos.agp.render.opengl.EGL;
import ohos.agp.render.opengl.GLES20;
import ohos.ai.engine.resultcode.HwHiAIResultCode;
import ohos.bluetooth.BluetoothDeviceClass;
import ohos.devtools.JLogConstants;
import ohos.global.icu.impl.Normalizer2Impl;
import ohos.global.icu.impl.UCharacterProperty;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.text.Bidi;
import ohos.global.icu.text.UTF16;

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

    public static char highSurrogate(int i) {
        return (char) (((i - 65536) >> 10) + 55296);
    }

    public static boolean isHighSurrogate(int i) {
        return 55296 <= i && i <= 56319;
    }

    public static boolean isLowSurrogate(int i) {
        return 56320 <= i && i <= 57343;
    }

    public static boolean isMarkup(int i) {
        return i == 60 || i == 38 || i == 37;
    }

    public static boolean isSupplemental(int i) {
        return i >= 65536 && i <= 1114111;
    }

    public static char lowSurrogate(int i) {
        return (char) (((i - 65536) & UCharacterProperty.MAX_SCRIPT) + UTF16.TRAIL_SURROGATE_MIN_VALUE);
    }

    public static int supplemental(char c, char c2) {
        return ((c - 55296) * 1024) + (c2 - UTF16.TRAIL_SURROGATE_MIN_VALUE) + 65536;
    }

    static {
        int[] iArr = {9, 10, 13, 13, 32, 55295, 57344, UCharacter.REPLACEMENT_CHAR};
        int[] iArr2 = {32, 9, 13, 10};
        int[] iArr3 = {45, 46};
        int[] iArr4 = {58, 95};
        int[] iArr5 = {10, 13, 32, 33, 35, 36, 37, 61, 95};
        int[] iArr6 = {39, 59, 63, 90, 97, 122};
        int[] iArr7 = {65, 90, 97, 122, 192, 214, 216, 246, 248, 305, 308, JLogConstants.JLID_CAMERAALGO_ALGOEXT1_BEGIN, JLogConstants.JLID_CAMERAALGO_ALGOEXT2_END, JLogConstants.JLID_CAMERA3_HAL_CAF_BEGIN, JLogConstants.JLID_CAMERAAPP_SURFACE_AVALIABLE, JLogConstants.JLID_HOME_KEY_PRESS, 384, JLogConstants.JLID_LOCAL_DMS_CONTINUATION_OLD, JLogConstants.JLID_CAMERA_COLD_START_BEGIN, 496, 500, 501, 506, 535, 592, 680, 699, 705, 904, 906, 910, 929, 931, 974, 976, 982, 994, 1011, 1025, 1036, 1038, 1103, 1105, 1116, 1118, 1153, 1168, 1220, 1223, 1224, 1227, 1228, 1232, 1259, 1262, 1269, 1272, 1273, 1329, 1366, 1377, 1414, 1488, 1514, 1520, 1522, 1569, 1594, 1601, 1610, 1649, 1719, 1722, 1726, 1728, 1742, 1744, 1747, 1765, 1766, 2309, 2361, 2392, 2401, 2437, 2444, 2447, 2448, 2451, 2472, 2474, 2480, 2486, 2489, 2524, 2525, 2527, 2529, 2544, 2545, 2565, 2570, 2575, 2576, 2579, 2600, 2602, 2608, 2610, 2611, 2613, 2614, 2616, 2617, 2649, 2652, 2674, 2676, 2693, 2699, 2703, 2705, 2707, 2728, 2730, 2736, 2738, 2739, 2741, 2745, 2821, 2828, 2831, 2832, 2835, 2856, 2858, 2864, 2866, 2867, 2870, 2873, 2908, 2909, 2911, 2913, 2949, 2954, 2958, 2960, 2962, 2965, 2969, 2970, 2974, 2975, 2979, 2980, 2984, 2986, 2990, 2997, 2999, HwHiAIResultCode.AIRESULT_INTERRUPT_BY_HIGH_PRIORITY, 3077, 3084, 3086, 3088, 3090, 3112, 3114, 3123, 3125, 3129, 3168, 3169, 3205, 3212, 3214, 3216, 3218, 3240, 3242, 3251, 3253, 3257, 3296, 3297, 3333, 3340, 3342, 3344, 3346, 3368, 3370, 3385, 3424, 3425, 3585, 3630, 3634, 3635, 3648, 3653, 3713, 3714, 3719, 3720, 3732, 3735, 3737, 3743, 3745, 3747, 3754, 3755, 3757, 3758, 3762, 3763, 3776, 3780, 3904, 3911, 3913, 3945, 4256, 4293, 4304, 4342, GLES20.GL_NICEST, 4355, 4357, 4359, 4363, 4364, 4366, Normalizer2Impl.Hangul.JAMO_L_END, 4436, 4437, 4447, Normalizer2Impl.Hangul.JAMO_V_BASE, 4461, 4462, 4466, 4467, 4526, 4527, 4535, 4536, 4540, Normalizer2Impl.Hangul.JAMO_T_END, 7680, 7835, 7840, 7929, 7936, 7957, 7960, 7965, 7968, 8005, 8008, 8013, 8016, 8023, 8031, 8061, 8064, 8116, 8118, 8124, 8130, 8132, 8134, 8140, 8144, 8147, 8150, 8155, 8160, 8172, 8178, 8180, 8182, 8188, 8490, 8491, 8576, 8578, 12353, 12436, 12449, 12538, 12549, 12588, Normalizer2Impl.Hangul.HANGUL_BASE, Normalizer2Impl.Hangul.HANGUL_END, EGL.EGL_ALPHA_SIZE, EGL.EGL_LEVEL, 19968, 40869};
        int[] iArr8 = {902, 908, 986, 988, 990, 992, 1369, 1749, 2365, 2482, 2654, 2701, 2749, 2784, 2877, 2972, 3294, 3632, 3716, 3722, 3725, 3749, 3751, 3760, 3773, Normalizer2Impl.Hangul.JAMO_L_BASE, 4361, 4412, 4414, 4416, 4428, 4430, 4432, 4441, 4451, 4453, 4455, 4457, Normalizer2Impl.Hangul.JAMO_V_END, 4510, 4520, 4523, 4538, 4587, 4592, 4601, 8025, 8027, 8029, 8126, 8486, 8494, 12295};
        int[] iArr9 = {768, 837, 864, 865, 1155, 1158, 1425, 1441, 1443, 1465, 1467, 1469, 1473, 1474, 1611, 1618, 1750, 1756, 1757, 1759, 1760, 1764, 1767, 1768, 1770, 1773, GLES20.GL_CCW, 2307, 2366, 2380, 2385, 2388, 2402, 2403, 2433, 2435, 2496, 2500, 2503, 2504, 2507, 2509, 2530, 2531, 2624, 2626, 2631, 2632, 2635, 2637, 2672, 2673, 2689, 2691, 2750, 2757, 2759, 2761, 2763, 2765, 2817, 2819, 2878, 2883, 2887, 2888, 2891, 2893, 2902, 2903, 2946, 2947, 3006, 3010, 3014, 3016, 3018, 3021, 3073, 3075, 3134, 3140, 3142, 3144, 3146, 3149, 3157, 3158, 3202, 3203, 3262, 3268, 3270, 3272, 3274, 3277, 3285, 3286, 3330, 3331, 3390, 3395, 3398, 3400, 3402, 3405, 3636, 3642, 3655, 3662, 3764, 3769, 3771, 3772, 3784, 3789, 3864, 3865, 3953, 3972, 3974, 3979, 3984, 3989, 3993, 4013, 4017, 4023, 8400, 8412, 12330, 12335};
        int[] iArr10 = {1471, 1476, 1648, BluetoothDeviceClass.MajorMinorClass.HEALTH_PERSONAL_MOBILITY_DEVICE, 2381, 2492, 2494, 2495, 2519, 2562, 2620, 2622, 2623, 2748, 2876, 3031, 3415, 3633, 3761, 3893, 3895, 3897, 3902, 3903, 3991, 4025, 8417, 12441, 12442};
        int[] iArr11 = {48, 57, 1632, 1641, 1776, 1785, 2406, 2415, 2534, 2543, 2662, 2671, 2790, 2799, 2918, 2927, 3047, 3055, 3174, 3183, 3302, 3311, 3430, 3439, 3664, 3673, 3792, 3801, 3872, 3881};
        int[] iArr12 = {12337, 12341, 12445, 12446, 12540, 12542};
        int[] iArr13 = {183, 720, 721, 903, 1600, 3654, 3782, 12293};
        int[] iArr14 = {60, 38, 10, 13, 93};
        int i = 0;
        while (i < iArr.length) {
            int i2 = iArr[i];
            while (i2 <= iArr[i + 1]) {
                byte[] bArr = CHARS;
                bArr[i2] = (byte) (bArr[i2] | 33);
                i2++;
                iArr6 = iArr6;
                iArr = iArr;
            }
            i += 2;
            iArr6 = iArr6;
        }
        for (int i3 = 0; i3 < iArr14.length; i3++) {
            byte[] bArr2 = CHARS;
            bArr2[iArr14[i3]] = (byte) (bArr2[iArr14[i3]] & -33);
        }
        for (int i4 : iArr2) {
            byte[] bArr3 = CHARS;
            bArr3[i4] = (byte) (bArr3[i4] | 2);
        }
        for (int i5 : iArr4) {
            byte[] bArr4 = CHARS;
            bArr4[i5] = (byte) (bArr4[i5] | 204);
        }
        for (int i6 = 0; i6 < iArr7.length; i6 += 2) {
            for (int i7 = iArr7[i6]; i7 <= iArr7[i6 + 1]; i7++) {
                byte[] bArr5 = CHARS;
                bArr5[i7] = (byte) (bArr5[i7] | 204);
            }
        }
        for (int i8 : iArr8) {
            byte[] bArr6 = CHARS;
            bArr6[i8] = (byte) (bArr6[i8] | 204);
        }
        for (int i9 : iArr3) {
            byte[] bArr7 = CHARS;
            bArr7[i9] = (byte) (bArr7[i9] | 136);
        }
        for (int i10 = 0; i10 < iArr11.length; i10 += 2) {
            for (int i11 = iArr11[i10]; i11 <= iArr11[i10 + 1]; i11++) {
                byte[] bArr8 = CHARS;
                bArr8[i11] = (byte) (bArr8[i11] | 136);
            }
        }
        for (int i12 = 0; i12 < iArr9.length; i12 += 2) {
            for (int i13 = iArr9[i12]; i13 <= iArr9[i12 + 1]; i13++) {
                byte[] bArr9 = CHARS;
                bArr9[i13] = (byte) (bArr9[i13] | 136);
            }
        }
        for (int i14 : iArr10) {
            byte[] bArr10 = CHARS;
            bArr10[i14] = (byte) (bArr10[i14] | 136);
        }
        for (int i15 = 0; i15 < iArr12.length; i15 += 2) {
            for (int i16 = iArr12[i15]; i16 <= iArr12[i15 + 1]; i16++) {
                byte[] bArr11 = CHARS;
                bArr11[i16] = (byte) (bArr11[i16] | 136);
            }
        }
        for (int i17 : iArr13) {
            byte[] bArr12 = CHARS;
            bArr12[i17] = (byte) (bArr12[i17] | 136);
        }
        byte[] bArr13 = CHARS;
        bArr13[58] = (byte) (bArr13[58] & -193);
        for (int i18 : iArr5) {
            byte[] bArr14 = CHARS;
            bArr14[i18] = (byte) (bArr14[i18] | 16);
        }
        for (int i19 = 0; i19 < iArr6.length; i19 += 2) {
            for (int i20 = iArr6[i19]; i20 <= iArr6[i19 + 1]; i20++) {
                byte[] bArr15 = CHARS;
                bArr15[i20] = (byte) (bArr15[i20] | 16);
            }
        }
    }

    public static boolean isValid(int i) {
        if (i >= 65536 || (CHARS[i] & 1) == 0) {
            return 65536 <= i && i <= 1114111;
        }
        return true;
    }

    public static boolean isInvalid(int i) {
        return !isValid(i);
    }

    public static boolean isContent(int i) {
        return (i < 65536 && (CHARS[i] & 32) != 0) || (65536 <= i && i <= 1114111);
    }

    public static boolean isSpace(int i) {
        return i < 65536 && (CHARS[i] & 2) != 0;
    }

    public static boolean isNameStart(int i) {
        return i < 65536 && (CHARS[i] & 4) != 0;
    }

    public static boolean isName(int i) {
        return i < 65536 && (CHARS[i] & 8) != 0;
    }

    public static boolean isNCNameStart(int i) {
        return i < 65536 && (CHARS[i] & 64) != 0;
    }

    public static boolean isNCName(int i) {
        return i < 65536 && (CHARS[i] & Bidi.LEVEL_OVERRIDE) != 0;
    }

    public static boolean isPubid(int i) {
        return i < 65536 && (CHARS[i] & 16) != 0;
    }

    public static boolean isValidName(String str) {
        if (str.length() == 0 || !isNameStart(str.charAt(0))) {
            return false;
        }
        for (int i = 1; i < str.length(); i++) {
            if (!isName(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidNCName(String str) {
        if (str.length() == 0 || !isNCNameStart(str.charAt(0))) {
            return false;
        }
        for (int i = 1; i < str.length(); i++) {
            if (!isNCName(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidNmtoken(String str) {
        if (str.length() == 0) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!isName(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidIANAEncoding(String str) {
        int length;
        char charAt;
        if (str == null || (length = str.length()) <= 0 || (((charAt = str.charAt(0)) < 'A' || charAt > 'Z') && (charAt < 'a' || charAt > 'z'))) {
            return false;
        }
        for (int i = 1; i < length; i++) {
            char charAt2 = str.charAt(i);
            if ((charAt2 < 'A' || charAt2 > 'Z') && ((charAt2 < 'a' || charAt2 > 'z') && !((charAt2 >= '0' && charAt2 <= '9') || charAt2 == '.' || charAt2 == '_' || charAt2 == '-'))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidJavaEncoding(String str) {
        int length;
        if (str == null || (length = str.length()) <= 0) {
            return false;
        }
        for (int i = 1; i < length; i++) {
            char charAt = str.charAt(i);
            if ((charAt < 'A' || charAt > 'Z') && ((charAt < 'a' || charAt > 'z') && !((charAt >= '0' && charAt <= '9') || charAt == '.' || charAt == '_' || charAt == '-'))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidQName(String str) {
        int indexOf = str.indexOf(58);
        if (indexOf == 0 || indexOf == str.length() - 1) {
            return false;
        }
        if (indexOf <= 0) {
            return isValidNCName(str);
        }
        String substring = str.substring(0, indexOf);
        String substring2 = str.substring(indexOf + 1);
        if (!isValidNCName(substring) || !isValidNCName(substring2)) {
            return false;
        }
        return true;
    }
}
