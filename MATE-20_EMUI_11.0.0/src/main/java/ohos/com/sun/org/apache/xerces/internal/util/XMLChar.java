package ohos.com.sun.org.apache.xerces.internal.util;

import java.util.Arrays;
import ohos.agp.render.opengl.EGL;
import ohos.agp.render.opengl.GLES20;
import ohos.ai.engine.resultcode.HwHiAIResultCode;
import ohos.bluetooth.BluetoothDeviceClass;
import ohos.devtools.JLogConstants;
import ohos.global.icu.impl.Normalizer2Impl;
import ohos.global.icu.impl.UCharacterProperty;
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
        byte[] bArr = CHARS;
        bArr[9] = 35;
        bArr[10] = 19;
        bArr[13] = 19;
        bArr[32] = 51;
        bArr[33] = 49;
        bArr[34] = 33;
        Arrays.fill(bArr, 35, 38, (byte) 49);
        byte[] bArr2 = CHARS;
        bArr2[38] = 1;
        Arrays.fill(bArr2, 39, 45, (byte) 49);
        Arrays.fill(CHARS, 45, 47, (byte) -71);
        byte[] bArr3 = CHARS;
        bArr3[47] = 49;
        Arrays.fill(bArr3, 48, 58, (byte) -71);
        byte[] bArr4 = CHARS;
        bArr4[58] = 61;
        bArr4[59] = 49;
        bArr4[60] = 1;
        bArr4[61] = 49;
        bArr4[62] = 33;
        Arrays.fill(bArr4, 63, 65, (byte) 49);
        Arrays.fill(CHARS, 65, 91, (byte) -3);
        Arrays.fill(CHARS, 91, 93, (byte) 33);
        byte[] bArr5 = CHARS;
        bArr5[93] = 1;
        bArr5[94] = 33;
        bArr5[95] = -3;
        bArr5[96] = 33;
        Arrays.fill(bArr5, 97, 123, (byte) -3);
        Arrays.fill(CHARS, 123, 183, (byte) 33);
        byte[] bArr6 = CHARS;
        bArr6[183] = -87;
        Arrays.fill(bArr6, 184, 192, (byte) 33);
        Arrays.fill(CHARS, 192, 215, (byte) -19);
        byte[] bArr7 = CHARS;
        bArr7[215] = 33;
        Arrays.fill(bArr7, 216, 247, (byte) -19);
        byte[] bArr8 = CHARS;
        bArr8[247] = 33;
        Arrays.fill(bArr8, 248, 306, (byte) -19);
        Arrays.fill(CHARS, 306, 308, (byte) 33);
        Arrays.fill(CHARS, 308, (int) JLogConstants.JLID_CAMERAALGO_ALGOEXT1_END, (byte) -19);
        Arrays.fill(CHARS, (int) JLogConstants.JLID_CAMERAALGO_ALGOEXT1_END, (int) JLogConstants.JLID_CAMERAALGO_ALGOEXT2_END, (byte) 33);
        Arrays.fill(CHARS, (int) JLogConstants.JLID_CAMERAALGO_ALGOEXT2_END, (int) JLogConstants.JLID_CAMERA3_HAL_CAF_END, (byte) -19);
        byte[] bArr9 = CHARS;
        bArr9[329] = 33;
        Arrays.fill(bArr9, (int) JLogConstants.JLID_CAMERAAPP_SURFACE_AVALIABLE, 383, (byte) -19);
        byte[] bArr10 = CHARS;
        bArr10[383] = 33;
        Arrays.fill(bArr10, 384, (int) JLogConstants.JLID_REMOTE_DMS_CONTINUATION_OLD, (byte) -19);
        Arrays.fill(CHARS, (int) JLogConstants.JLID_REMOTE_DMS_CONTINUATION_OLD, (int) JLogConstants.JLID_CAMERA_COLD_START_BEGIN, (byte) 33);
        Arrays.fill(CHARS, (int) JLogConstants.JLID_CAMERA_COLD_START_BEGIN, 497, (byte) -19);
        Arrays.fill(CHARS, 497, 500, (byte) 33);
        Arrays.fill(CHARS, 500, 502, (byte) -19);
        Arrays.fill(CHARS, 502, 506, (byte) 33);
        Arrays.fill(CHARS, 506, 536, (byte) -19);
        Arrays.fill(CHARS, 536, 592, (byte) 33);
        Arrays.fill(CHARS, 592, 681, (byte) -19);
        Arrays.fill(CHARS, 681, 699, (byte) 33);
        Arrays.fill(CHARS, 699, 706, (byte) -19);
        Arrays.fill(CHARS, 706, 720, (byte) 33);
        Arrays.fill(CHARS, 720, 722, (byte) -87);
        Arrays.fill(CHARS, 722, 768, (byte) 33);
        Arrays.fill(CHARS, 768, 838, (byte) -87);
        Arrays.fill(CHARS, 838, 864, (byte) 33);
        Arrays.fill(CHARS, 864, 866, (byte) -87);
        Arrays.fill(CHARS, 866, 902, (byte) 33);
        byte[] bArr11 = CHARS;
        bArr11[902] = -19;
        bArr11[903] = -87;
        Arrays.fill(bArr11, 904, 907, (byte) -19);
        byte[] bArr12 = CHARS;
        bArr12[907] = 33;
        bArr12[908] = -19;
        bArr12[909] = 33;
        Arrays.fill(bArr12, 910, 930, (byte) -19);
        byte[] bArr13 = CHARS;
        bArr13[930] = 33;
        Arrays.fill(bArr13, 931, 975, (byte) -19);
        byte[] bArr14 = CHARS;
        bArr14[975] = 33;
        Arrays.fill(bArr14, 976, 983, (byte) -19);
        Arrays.fill(CHARS, 983, 986, (byte) 33);
        byte[] bArr15 = CHARS;
        bArr15[986] = -19;
        bArr15[987] = 33;
        bArr15[988] = -19;
        bArr15[989] = 33;
        bArr15[990] = -19;
        bArr15[991] = 33;
        bArr15[992] = -19;
        bArr15[993] = 33;
        Arrays.fill(bArr15, 994, 1012, (byte) -19);
        Arrays.fill(CHARS, 1012, 1025, (byte) 33);
        Arrays.fill(CHARS, 1025, 1037, (byte) -19);
        byte[] bArr16 = CHARS;
        bArr16[1037] = 33;
        Arrays.fill(bArr16, 1038, 1104, (byte) -19);
        byte[] bArr17 = CHARS;
        bArr17[1104] = 33;
        Arrays.fill(bArr17, 1105, 1117, (byte) -19);
        byte[] bArr18 = CHARS;
        bArr18[1117] = 33;
        Arrays.fill(bArr18, 1118, 1154, (byte) -19);
        byte[] bArr19 = CHARS;
        bArr19[1154] = 33;
        Arrays.fill(bArr19, 1155, 1159, (byte) -87);
        Arrays.fill(CHARS, 1159, 1168, (byte) 33);
        Arrays.fill(CHARS, 1168, 1221, (byte) -19);
        Arrays.fill(CHARS, 1221, 1223, (byte) 33);
        Arrays.fill(CHARS, 1223, 1225, (byte) -19);
        Arrays.fill(CHARS, 1225, 1227, (byte) 33);
        Arrays.fill(CHARS, 1227, 1229, (byte) -19);
        Arrays.fill(CHARS, 1229, 1232, (byte) 33);
        Arrays.fill(CHARS, 1232, 1260, (byte) -19);
        Arrays.fill(CHARS, 1260, 1262, (byte) 33);
        Arrays.fill(CHARS, 1262, 1270, (byte) -19);
        Arrays.fill(CHARS, 1270, 1272, (byte) 33);
        Arrays.fill(CHARS, 1272, 1274, (byte) -19);
        Arrays.fill(CHARS, 1274, 1329, (byte) 33);
        Arrays.fill(CHARS, 1329, 1367, (byte) -19);
        Arrays.fill(CHARS, 1367, 1369, (byte) 33);
        byte[] bArr20 = CHARS;
        bArr20[1369] = -19;
        Arrays.fill(bArr20, 1370, 1377, (byte) 33);
        Arrays.fill(CHARS, 1377, 1415, (byte) -19);
        Arrays.fill(CHARS, 1415, 1425, (byte) 33);
        Arrays.fill(CHARS, 1425, 1442, (byte) -87);
        byte[] bArr21 = CHARS;
        bArr21[1442] = 33;
        Arrays.fill(bArr21, 1443, 1466, (byte) -87);
        byte[] bArr22 = CHARS;
        bArr22[1466] = 33;
        Arrays.fill(bArr22, 1467, 1470, (byte) -87);
        byte[] bArr23 = CHARS;
        bArr23[1470] = 33;
        bArr23[1471] = -87;
        bArr23[1472] = 33;
        Arrays.fill(bArr23, 1473, 1475, (byte) -87);
        byte[] bArr24 = CHARS;
        bArr24[1475] = 33;
        bArr24[1476] = -87;
        Arrays.fill(bArr24, 1477, 1488, (byte) 33);
        Arrays.fill(CHARS, 1488, 1515, (byte) -19);
        Arrays.fill(CHARS, 1515, 1520, (byte) 33);
        Arrays.fill(CHARS, 1520, 1523, (byte) -19);
        Arrays.fill(CHARS, 1523, 1569, (byte) 33);
        Arrays.fill(CHARS, 1569, 1595, (byte) -19);
        Arrays.fill(CHARS, 1595, 1600, (byte) 33);
        byte[] bArr25 = CHARS;
        bArr25[1600] = -87;
        Arrays.fill(bArr25, 1601, 1611, (byte) -19);
        Arrays.fill(CHARS, 1611, 1619, (byte) -87);
        Arrays.fill(CHARS, 1619, 1632, (byte) 33);
        Arrays.fill(CHARS, 1632, 1642, (byte) -87);
        Arrays.fill(CHARS, 1642, 1648, (byte) 33);
        byte[] bArr26 = CHARS;
        bArr26[1648] = -87;
        Arrays.fill(bArr26, 1649, 1720, (byte) -19);
        Arrays.fill(CHARS, 1720, 1722, (byte) 33);
        Arrays.fill(CHARS, 1722, 1727, (byte) -19);
        byte[] bArr27 = CHARS;
        bArr27[1727] = 33;
        Arrays.fill(bArr27, 1728, 1743, (byte) -19);
        byte[] bArr28 = CHARS;
        bArr28[1743] = 33;
        Arrays.fill(bArr28, 1744, 1748, (byte) -19);
        byte[] bArr29 = CHARS;
        bArr29[1748] = 33;
        bArr29[1749] = -19;
        Arrays.fill(bArr29, 1750, 1765, (byte) -87);
        Arrays.fill(CHARS, 1765, 1767, (byte) -19);
        Arrays.fill(CHARS, 1767, 1769, (byte) -87);
        byte[] bArr30 = CHARS;
        bArr30[1769] = 33;
        Arrays.fill(bArr30, 1770, 1774, (byte) -87);
        Arrays.fill(CHARS, 1774, 1776, (byte) 33);
        Arrays.fill(CHARS, 1776, 1786, (byte) -87);
        Arrays.fill(CHARS, 1786, (int) GLES20.GL_CCW, (byte) 33);
        Arrays.fill(CHARS, (int) GLES20.GL_CCW, (int) BluetoothDeviceClass.MajorMinorClass.HEALTH_BLOOD_PRESSURE_MONITOR, (byte) -87);
        byte[] bArr31 = CHARS;
        bArr31[2308] = 33;
        Arrays.fill(bArr31, 2309, 2362, (byte) -19);
        Arrays.fill(CHARS, 2362, (int) BluetoothDeviceClass.MajorMinorClass.HEALTH_PERSONAL_MOBILITY_DEVICE, (byte) 33);
        byte[] bArr32 = CHARS;
        bArr32[2364] = -87;
        bArr32[2365] = -19;
        Arrays.fill(bArr32, 2366, 2382, (byte) -87);
        Arrays.fill(CHARS, 2382, 2385, (byte) 33);
        Arrays.fill(CHARS, 2385, 2389, (byte) -87);
        Arrays.fill(CHARS, 2389, 2392, (byte) 33);
        Arrays.fill(CHARS, 2392, 2402, (byte) -19);
        Arrays.fill(CHARS, 2402, 2404, (byte) -87);
        Arrays.fill(CHARS, 2404, 2406, (byte) 33);
        Arrays.fill(CHARS, 2406, 2416, (byte) -87);
        Arrays.fill(CHARS, 2416, 2433, (byte) 33);
        Arrays.fill(CHARS, 2433, 2436, (byte) -87);
        byte[] bArr33 = CHARS;
        bArr33[2436] = 33;
        Arrays.fill(bArr33, 2437, 2445, (byte) -19);
        Arrays.fill(CHARS, 2445, 2447, (byte) 33);
        Arrays.fill(CHARS, 2447, 2449, (byte) -19);
        Arrays.fill(CHARS, 2449, 2451, (byte) 33);
        Arrays.fill(CHARS, 2451, 2473, (byte) -19);
        byte[] bArr34 = CHARS;
        bArr34[2473] = 33;
        Arrays.fill(bArr34, 2474, 2481, (byte) -19);
        byte[] bArr35 = CHARS;
        bArr35[2481] = 33;
        bArr35[2482] = -19;
        Arrays.fill(bArr35, 2483, 2486, (byte) 33);
        Arrays.fill(CHARS, 2486, 2490, (byte) -19);
        Arrays.fill(CHARS, 2490, 2492, (byte) 33);
        byte[] bArr36 = CHARS;
        bArr36[2492] = -87;
        bArr36[2493] = 33;
        Arrays.fill(bArr36, 2494, 2501, (byte) -87);
        Arrays.fill(CHARS, 2501, 2503, (byte) 33);
        Arrays.fill(CHARS, 2503, 2505, (byte) -87);
        Arrays.fill(CHARS, 2505, 2507, (byte) 33);
        Arrays.fill(CHARS, 2507, 2510, (byte) -87);
        Arrays.fill(CHARS, 2510, 2519, (byte) 33);
        byte[] bArr37 = CHARS;
        bArr37[2519] = -87;
        Arrays.fill(bArr37, 2520, 2524, (byte) 33);
        Arrays.fill(CHARS, 2524, 2526, (byte) -19);
        byte[] bArr38 = CHARS;
        bArr38[2526] = 33;
        Arrays.fill(bArr38, 2527, 2530, (byte) -19);
        Arrays.fill(CHARS, 2530, 2532, (byte) -87);
        Arrays.fill(CHARS, 2532, 2534, (byte) 33);
        Arrays.fill(CHARS, 2534, 2544, (byte) -87);
        Arrays.fill(CHARS, 2544, 2546, (byte) -19);
        Arrays.fill(CHARS, 2546, 2562, (byte) 33);
        byte[] bArr39 = CHARS;
        bArr39[2562] = -87;
        Arrays.fill(bArr39, 2563, 2565, (byte) 33);
        Arrays.fill(CHARS, 2565, 2571, (byte) -19);
        Arrays.fill(CHARS, 2571, 2575, (byte) 33);
        Arrays.fill(CHARS, 2575, 2577, (byte) -19);
        Arrays.fill(CHARS, 2577, 2579, (byte) 33);
        Arrays.fill(CHARS, 2579, 2601, (byte) -19);
        byte[] bArr40 = CHARS;
        bArr40[2601] = 33;
        Arrays.fill(bArr40, 2602, 2609, (byte) -19);
        byte[] bArr41 = CHARS;
        bArr41[2609] = 33;
        Arrays.fill(bArr41, 2610, 2612, (byte) -19);
        byte[] bArr42 = CHARS;
        bArr42[2612] = 33;
        Arrays.fill(bArr42, 2613, 2615, (byte) -19);
        byte[] bArr43 = CHARS;
        bArr43[2615] = 33;
        Arrays.fill(bArr43, 2616, 2618, (byte) -19);
        Arrays.fill(CHARS, 2618, 2620, (byte) 33);
        byte[] bArr44 = CHARS;
        bArr44[2620] = -87;
        bArr44[2621] = 33;
        Arrays.fill(bArr44, 2622, 2627, (byte) -87);
        Arrays.fill(CHARS, 2627, 2631, (byte) 33);
        Arrays.fill(CHARS, 2631, 2633, (byte) -87);
        Arrays.fill(CHARS, 2633, 2635, (byte) 33);
        Arrays.fill(CHARS, 2635, 2638, (byte) -87);
        Arrays.fill(CHARS, 2638, 2649, (byte) 33);
        Arrays.fill(CHARS, 2649, 2653, (byte) -19);
        byte[] bArr45 = CHARS;
        bArr45[2653] = 33;
        bArr45[2654] = -19;
        Arrays.fill(bArr45, 2655, 2662, (byte) 33);
        Arrays.fill(CHARS, 2662, 2674, (byte) -87);
        Arrays.fill(CHARS, 2674, 2677, (byte) -19);
        Arrays.fill(CHARS, 2677, 2689, (byte) 33);
        Arrays.fill(CHARS, 2689, 2692, (byte) -87);
        byte[] bArr46 = CHARS;
        bArr46[2692] = 33;
        Arrays.fill(bArr46, 2693, 2700, (byte) -19);
        byte[] bArr47 = CHARS;
        bArr47[2700] = 33;
        bArr47[2701] = -19;
        bArr47[2702] = 33;
        Arrays.fill(bArr47, 2703, 2706, (byte) -19);
        byte[] bArr48 = CHARS;
        bArr48[2706] = 33;
        Arrays.fill(bArr48, 2707, 2729, (byte) -19);
        byte[] bArr49 = CHARS;
        bArr49[2729] = 33;
        Arrays.fill(bArr49, 2730, 2737, (byte) -19);
        byte[] bArr50 = CHARS;
        bArr50[2737] = 33;
        Arrays.fill(bArr50, 2738, 2740, (byte) -19);
        byte[] bArr51 = CHARS;
        bArr51[2740] = 33;
        Arrays.fill(bArr51, 2741, 2746, (byte) -19);
        Arrays.fill(CHARS, 2746, 2748, (byte) 33);
        byte[] bArr52 = CHARS;
        bArr52[2748] = -87;
        bArr52[2749] = -19;
        Arrays.fill(bArr52, 2750, 2758, (byte) -87);
        byte[] bArr53 = CHARS;
        bArr53[2758] = 33;
        Arrays.fill(bArr53, 2759, 2762, (byte) -87);
        byte[] bArr54 = CHARS;
        bArr54[2762] = 33;
        Arrays.fill(bArr54, 2763, 2766, (byte) -87);
        Arrays.fill(CHARS, 2766, 2784, (byte) 33);
        byte[] bArr55 = CHARS;
        bArr55[2784] = -19;
        Arrays.fill(bArr55, 2785, 2790, (byte) 33);
        Arrays.fill(CHARS, 2790, 2800, (byte) -87);
        Arrays.fill(CHARS, 2800, 2817, (byte) 33);
        Arrays.fill(CHARS, 2817, 2820, (byte) -87);
        byte[] bArr56 = CHARS;
        bArr56[2820] = 33;
        Arrays.fill(bArr56, 2821, 2829, (byte) -19);
        Arrays.fill(CHARS, 2829, 2831, (byte) 33);
        Arrays.fill(CHARS, 2831, 2833, (byte) -19);
        Arrays.fill(CHARS, 2833, 2835, (byte) 33);
        Arrays.fill(CHARS, 2835, 2857, (byte) -19);
        byte[] bArr57 = CHARS;
        bArr57[2857] = 33;
        Arrays.fill(bArr57, 2858, 2865, (byte) -19);
        byte[] bArr58 = CHARS;
        bArr58[2865] = 33;
        Arrays.fill(bArr58, 2866, 2868, (byte) -19);
        Arrays.fill(CHARS, 2868, 2870, (byte) 33);
        Arrays.fill(CHARS, 2870, 2874, (byte) -19);
        Arrays.fill(CHARS, 2874, 2876, (byte) 33);
        byte[] bArr59 = CHARS;
        bArr59[2876] = -87;
        bArr59[2877] = -19;
        Arrays.fill(bArr59, 2878, (int) GLES20.GL_CULL_FACE, (byte) -87);
        Arrays.fill(CHARS, (int) GLES20.GL_CULL_FACE, 2887, (byte) 33);
        Arrays.fill(CHARS, 2887, 2889, (byte) -87);
        Arrays.fill(CHARS, 2889, 2891, (byte) 33);
        Arrays.fill(CHARS, 2891, 2894, (byte) -87);
        Arrays.fill(CHARS, 2894, 2902, (byte) 33);
        Arrays.fill(CHARS, 2902, 2904, (byte) -87);
        Arrays.fill(CHARS, 2904, 2908, (byte) 33);
        Arrays.fill(CHARS, 2908, 2910, (byte) -19);
        byte[] bArr60 = CHARS;
        bArr60[2910] = 33;
        Arrays.fill(bArr60, 2911, 2914, (byte) -19);
        Arrays.fill(CHARS, 2914, 2918, (byte) 33);
        Arrays.fill(CHARS, 2918, 2928, (byte) -87);
        Arrays.fill(CHARS, 2928, 2946, (byte) 33);
        Arrays.fill(CHARS, 2946, 2948, (byte) -87);
        byte[] bArr61 = CHARS;
        bArr61[2948] = 33;
        Arrays.fill(bArr61, 2949, 2955, (byte) -19);
        Arrays.fill(CHARS, 2955, 2958, (byte) 33);
        Arrays.fill(CHARS, 2958, 2961, (byte) -19);
        byte[] bArr62 = CHARS;
        bArr62[2961] = 33;
        Arrays.fill(bArr62, 2962, 2966, (byte) -19);
        Arrays.fill(CHARS, 2966, 2969, (byte) 33);
        Arrays.fill(CHARS, 2969, 2971, (byte) -19);
        byte[] bArr63 = CHARS;
        bArr63[2971] = 33;
        bArr63[2972] = -19;
        bArr63[2973] = 33;
        Arrays.fill(bArr63, 2974, 2976, (byte) -19);
        Arrays.fill(CHARS, 2976, 2979, (byte) 33);
        Arrays.fill(CHARS, 2979, 2981, (byte) -19);
        Arrays.fill(CHARS, 2981, 2984, (byte) 33);
        Arrays.fill(CHARS, 2984, 2987, (byte) -19);
        Arrays.fill(CHARS, 2987, 2990, (byte) 33);
        Arrays.fill(CHARS, 2990, 2998, (byte) -19);
        byte[] bArr64 = CHARS;
        bArr64[2998] = 33;
        Arrays.fill(bArr64, 2999, (int) HwHiAIResultCode.AIRESULT_GET_CLOUD_RESULT_FAIL, (byte) -19);
        Arrays.fill(CHARS, (int) HwHiAIResultCode.AIRESULT_GET_CLOUD_RESULT_FAIL, 3006, (byte) 33);
        Arrays.fill(CHARS, 3006, 3011, (byte) -87);
        Arrays.fill(CHARS, 3011, 3014, (byte) 33);
        Arrays.fill(CHARS, 3014, 3017, (byte) -87);
        byte[] bArr65 = CHARS;
        bArr65[3017] = 33;
        Arrays.fill(bArr65, 3018, 3022, (byte) -87);
        Arrays.fill(CHARS, 3022, 3031, (byte) 33);
        byte[] bArr66 = CHARS;
        bArr66[3031] = -87;
        Arrays.fill(bArr66, 3032, 3047, (byte) 33);
        Arrays.fill(CHARS, 3047, 3056, (byte) -87);
        Arrays.fill(CHARS, 3056, 3073, (byte) 33);
        Arrays.fill(CHARS, 3073, 3076, (byte) -87);
        byte[] bArr67 = CHARS;
        bArr67[3076] = 33;
        Arrays.fill(bArr67, 3077, 3085, (byte) -19);
        byte[] bArr68 = CHARS;
        bArr68[3085] = 33;
        Arrays.fill(bArr68, 3086, 3089, (byte) -19);
        byte[] bArr69 = CHARS;
        bArr69[3089] = 33;
        Arrays.fill(bArr69, 3090, 3113, (byte) -19);
        byte[] bArr70 = CHARS;
        bArr70[3113] = 33;
        Arrays.fill(bArr70, 3114, 3124, (byte) -19);
        byte[] bArr71 = CHARS;
        bArr71[3124] = 33;
        Arrays.fill(bArr71, 3125, 3130, (byte) -19);
        Arrays.fill(CHARS, 3130, 3134, (byte) 33);
        Arrays.fill(CHARS, 3134, 3141, (byte) -87);
        byte[] bArr72 = CHARS;
        bArr72[3141] = 33;
        Arrays.fill(bArr72, 3142, 3145, (byte) -87);
        byte[] bArr73 = CHARS;
        bArr73[3145] = 33;
        Arrays.fill(bArr73, 3146, 3150, (byte) -87);
        Arrays.fill(CHARS, 3150, 3157, (byte) 33);
        Arrays.fill(CHARS, 3157, 3159, (byte) -87);
        Arrays.fill(CHARS, 3159, 3168, (byte) 33);
        Arrays.fill(CHARS, 3168, 3170, (byte) -19);
        Arrays.fill(CHARS, 3170, 3174, (byte) 33);
        Arrays.fill(CHARS, 3174, 3184, (byte) -87);
        Arrays.fill(CHARS, 3184, 3202, (byte) 33);
        Arrays.fill(CHARS, 3202, 3204, (byte) -87);
        byte[] bArr74 = CHARS;
        bArr74[3204] = 33;
        Arrays.fill(bArr74, 3205, 3213, (byte) -19);
        byte[] bArr75 = CHARS;
        bArr75[3213] = 33;
        Arrays.fill(bArr75, 3214, 3217, (byte) -19);
        byte[] bArr76 = CHARS;
        bArr76[3217] = 33;
        Arrays.fill(bArr76, 3218, 3241, (byte) -19);
        byte[] bArr77 = CHARS;
        bArr77[3241] = 33;
        Arrays.fill(bArr77, 3242, 3252, (byte) -19);
        byte[] bArr78 = CHARS;
        bArr78[3252] = 33;
        Arrays.fill(bArr78, 3253, 3258, (byte) -19);
        Arrays.fill(CHARS, 3258, 3262, (byte) 33);
        Arrays.fill(CHARS, 3262, 3269, (byte) -87);
        byte[] bArr79 = CHARS;
        bArr79[3269] = 33;
        Arrays.fill(bArr79, 3270, 3273, (byte) -87);
        byte[] bArr80 = CHARS;
        bArr80[3273] = 33;
        Arrays.fill(bArr80, 3274, 3278, (byte) -87);
        Arrays.fill(CHARS, 3278, 3285, (byte) 33);
        Arrays.fill(CHARS, 3285, 3287, (byte) -87);
        Arrays.fill(CHARS, 3287, 3294, (byte) 33);
        byte[] bArr81 = CHARS;
        bArr81[3294] = -19;
        bArr81[3295] = 33;
        Arrays.fill(bArr81, 3296, 3298, (byte) -19);
        Arrays.fill(CHARS, 3298, 3302, (byte) 33);
        Arrays.fill(CHARS, 3302, 3312, (byte) -87);
        Arrays.fill(CHARS, 3312, 3330, (byte) 33);
        Arrays.fill(CHARS, 3330, 3332, (byte) -87);
        byte[] bArr82 = CHARS;
        bArr82[3332] = 33;
        Arrays.fill(bArr82, 3333, 3341, (byte) -19);
        byte[] bArr83 = CHARS;
        bArr83[3341] = 33;
        Arrays.fill(bArr83, 3342, 3345, (byte) -19);
        byte[] bArr84 = CHARS;
        bArr84[3345] = 33;
        Arrays.fill(bArr84, 3346, 3369, (byte) -19);
        byte[] bArr85 = CHARS;
        bArr85[3369] = 33;
        Arrays.fill(bArr85, 3370, 3386, (byte) -19);
        Arrays.fill(CHARS, 3386, 3390, (byte) 33);
        Arrays.fill(CHARS, 3390, 3396, (byte) -87);
        Arrays.fill(CHARS, 3396, 3398, (byte) 33);
        Arrays.fill(CHARS, 3398, 3401, (byte) -87);
        byte[] bArr86 = CHARS;
        bArr86[3401] = 33;
        Arrays.fill(bArr86, 3402, 3406, (byte) -87);
        Arrays.fill(CHARS, 3406, 3415, (byte) 33);
        byte[] bArr87 = CHARS;
        bArr87[3415] = -87;
        Arrays.fill(bArr87, 3416, 3424, (byte) 33);
        Arrays.fill(CHARS, 3424, 3426, (byte) -19);
        Arrays.fill(CHARS, 3426, 3430, (byte) 33);
        Arrays.fill(CHARS, 3430, 3440, (byte) -87);
        Arrays.fill(CHARS, 3440, 3585, (byte) 33);
        Arrays.fill(CHARS, 3585, 3631, (byte) -19);
        byte[] bArr88 = CHARS;
        bArr88[3631] = 33;
        bArr88[3632] = -19;
        bArr88[3633] = -87;
        Arrays.fill(bArr88, 3634, 3636, (byte) -19);
        Arrays.fill(CHARS, 3636, 3643, (byte) -87);
        Arrays.fill(CHARS, 3643, 3648, (byte) 33);
        Arrays.fill(CHARS, 3648, 3654, (byte) -19);
        Arrays.fill(CHARS, 3654, 3663, (byte) -87);
        byte[] bArr89 = CHARS;
        bArr89[3663] = 33;
        Arrays.fill(bArr89, 3664, 3674, (byte) -87);
        Arrays.fill(CHARS, 3674, 3713, (byte) 33);
        Arrays.fill(CHARS, 3713, 3715, (byte) -19);
        byte[] bArr90 = CHARS;
        bArr90[3715] = 33;
        bArr90[3716] = -19;
        Arrays.fill(bArr90, 3717, 3719, (byte) 33);
        Arrays.fill(CHARS, 3719, 3721, (byte) -19);
        byte[] bArr91 = CHARS;
        bArr91[3721] = 33;
        bArr91[3722] = -19;
        Arrays.fill(bArr91, 3723, 3725, (byte) 33);
        byte[] bArr92 = CHARS;
        bArr92[3725] = -19;
        Arrays.fill(bArr92, 3726, 3732, (byte) 33);
        Arrays.fill(CHARS, 3732, 3736, (byte) -19);
        byte[] bArr93 = CHARS;
        bArr93[3736] = 33;
        Arrays.fill(bArr93, 3737, 3744, (byte) -19);
        byte[] bArr94 = CHARS;
        bArr94[3744] = 33;
        Arrays.fill(bArr94, 3745, 3748, (byte) -19);
        byte[] bArr95 = CHARS;
        bArr95[3748] = 33;
        bArr95[3749] = -19;
        bArr95[3750] = 33;
        bArr95[3751] = -19;
        Arrays.fill(bArr95, 3752, 3754, (byte) 33);
        Arrays.fill(CHARS, 3754, 3756, (byte) -19);
        byte[] bArr96 = CHARS;
        bArr96[3756] = 33;
        Arrays.fill(bArr96, 3757, 3759, (byte) -19);
        byte[] bArr97 = CHARS;
        bArr97[3759] = 33;
        bArr97[3760] = -19;
        bArr97[3761] = -87;
        Arrays.fill(bArr97, 3762, 3764, (byte) -19);
        Arrays.fill(CHARS, 3764, 3770, (byte) -87);
        byte[] bArr98 = CHARS;
        bArr98[3770] = 33;
        Arrays.fill(bArr98, 3771, 3773, (byte) -87);
        byte[] bArr99 = CHARS;
        bArr99[3773] = -19;
        Arrays.fill(bArr99, 3774, 3776, (byte) 33);
        Arrays.fill(CHARS, 3776, 3781, (byte) -19);
        byte[] bArr100 = CHARS;
        bArr100[3781] = 33;
        bArr100[3782] = -87;
        bArr100[3783] = 33;
        Arrays.fill(bArr100, 3784, 3790, (byte) -87);
        Arrays.fill(CHARS, 3790, 3792, (byte) 33);
        Arrays.fill(CHARS, 3792, 3802, (byte) -87);
        Arrays.fill(CHARS, 3802, 3864, (byte) 33);
        Arrays.fill(CHARS, 3864, 3866, (byte) -87);
        Arrays.fill(CHARS, 3866, 3872, (byte) 33);
        Arrays.fill(CHARS, 3872, 3882, (byte) -87);
        Arrays.fill(CHARS, 3882, 3893, (byte) 33);
        byte[] bArr101 = CHARS;
        bArr101[3893] = -87;
        bArr101[3894] = 33;
        bArr101[3895] = -87;
        bArr101[3896] = 33;
        bArr101[3897] = -87;
        Arrays.fill(bArr101, 3898, 3902, (byte) 33);
        Arrays.fill(CHARS, 3902, 3904, (byte) -87);
        Arrays.fill(CHARS, 3904, 3912, (byte) -19);
        byte[] bArr102 = CHARS;
        bArr102[3912] = 33;
        Arrays.fill(bArr102, 3913, 3946, (byte) -19);
        Arrays.fill(CHARS, 3946, 3953, (byte) 33);
        Arrays.fill(CHARS, 3953, 3973, (byte) -87);
        byte[] bArr103 = CHARS;
        bArr103[3973] = 33;
        Arrays.fill(bArr103, 3974, 3980, (byte) -87);
        Arrays.fill(CHARS, 3980, 3984, (byte) 33);
        Arrays.fill(CHARS, 3984, 3990, (byte) -87);
        byte[] bArr104 = CHARS;
        bArr104[3990] = 33;
        bArr104[3991] = -87;
        bArr104[3992] = 33;
        Arrays.fill(bArr104, 3993, 4014, (byte) -87);
        Arrays.fill(CHARS, 4014, 4017, (byte) 33);
        Arrays.fill(CHARS, 4017, 4024, (byte) -87);
        byte[] bArr105 = CHARS;
        bArr105[4024] = 33;
        bArr105[4025] = -87;
        Arrays.fill(bArr105, 4026, 4256, (byte) 33);
        Arrays.fill(CHARS, 4256, 4294, (byte) -19);
        Arrays.fill(CHARS, 4294, 4304, (byte) 33);
        Arrays.fill(CHARS, 4304, 4343, (byte) -19);
        Arrays.fill(CHARS, 4343, (int) Normalizer2Impl.Hangul.JAMO_L_BASE, (byte) 33);
        byte[] bArr106 = CHARS;
        bArr106[4352] = -19;
        bArr106[4353] = 33;
        Arrays.fill(bArr106, (int) GLES20.GL_NICEST, 4356, (byte) -19);
        byte[] bArr107 = CHARS;
        bArr107[4356] = 33;
        Arrays.fill(bArr107, 4357, 4360, (byte) -19);
        byte[] bArr108 = CHARS;
        bArr108[4360] = 33;
        bArr108[4361] = -19;
        bArr108[4362] = 33;
        Arrays.fill(bArr108, 4363, 4365, (byte) -19);
        byte[] bArr109 = CHARS;
        bArr109[4365] = 33;
        Arrays.fill(bArr109, 4366, (int) Normalizer2Impl.Hangul.JAMO_L_LIMIT, (byte) -19);
        Arrays.fill(CHARS, (int) Normalizer2Impl.Hangul.JAMO_L_LIMIT, 4412, (byte) 33);
        byte[] bArr110 = CHARS;
        bArr110[4412] = -19;
        bArr110[4413] = 33;
        bArr110[4414] = -19;
        bArr110[4415] = 33;
        bArr110[4416] = -19;
        Arrays.fill(bArr110, 4417, 4428, (byte) 33);
        byte[] bArr111 = CHARS;
        bArr111[4428] = -19;
        bArr111[4429] = 33;
        bArr111[4430] = -19;
        bArr111[4431] = 33;
        bArr111[4432] = -19;
        Arrays.fill(bArr111, 4433, 4436, (byte) 33);
        Arrays.fill(CHARS, 4436, 4438, (byte) -19);
        Arrays.fill(CHARS, 4438, 4441, (byte) 33);
        byte[] bArr112 = CHARS;
        bArr112[4441] = -19;
        Arrays.fill(bArr112, 4442, 4447, (byte) 33);
        Arrays.fill(CHARS, 4447, 4450, (byte) -19);
        byte[] bArr113 = CHARS;
        bArr113[4450] = 33;
        bArr113[4451] = -19;
        bArr113[4452] = 33;
        bArr113[4453] = -19;
        bArr113[4454] = 33;
        bArr113[4455] = -19;
        bArr113[4456] = 33;
        bArr113[4457] = -19;
        Arrays.fill(bArr113, 4458, 4461, (byte) 33);
        Arrays.fill(CHARS, 4461, 4463, (byte) -19);
        Arrays.fill(CHARS, 4463, 4466, (byte) 33);
        Arrays.fill(CHARS, 4466, 4468, (byte) -19);
        byte[] bArr114 = CHARS;
        bArr114[4468] = 33;
        bArr114[4469] = -19;
        Arrays.fill(bArr114, (int) Normalizer2Impl.Hangul.JAMO_V_LIMIT, 4510, (byte) 33);
        byte[] bArr115 = CHARS;
        bArr115[4510] = -19;
        Arrays.fill(bArr115, 4511, 4520, (byte) 33);
        byte[] bArr116 = CHARS;
        bArr116[4520] = -19;
        Arrays.fill(bArr116, 4521, 4523, (byte) 33);
        byte[] bArr117 = CHARS;
        bArr117[4523] = -19;
        Arrays.fill(bArr117, 4524, 4526, (byte) 33);
        Arrays.fill(CHARS, 4526, 4528, (byte) -19);
        Arrays.fill(CHARS, 4528, 4535, (byte) 33);
        Arrays.fill(CHARS, 4535, 4537, (byte) -19);
        byte[] bArr118 = CHARS;
        bArr118[4537] = 33;
        bArr118[4538] = -19;
        bArr118[4539] = 33;
        Arrays.fill(bArr118, 4540, 4547, (byte) -19);
        Arrays.fill(CHARS, 4547, 4587, (byte) 33);
        byte[] bArr119 = CHARS;
        bArr119[4587] = -19;
        Arrays.fill(bArr119, 4588, 4592, (byte) 33);
        byte[] bArr120 = CHARS;
        bArr120[4592] = -19;
        Arrays.fill(bArr120, 4593, 4601, (byte) 33);
        byte[] bArr121 = CHARS;
        bArr121[4601] = -19;
        Arrays.fill(bArr121, 4602, 7680, (byte) 33);
        Arrays.fill(CHARS, 7680, 7836, (byte) -19);
        Arrays.fill(CHARS, 7836, 7840, (byte) 33);
        Arrays.fill(CHARS, 7840, 7930, (byte) -19);
        Arrays.fill(CHARS, 7930, 7936, (byte) 33);
        Arrays.fill(CHARS, 7936, 7958, (byte) -19);
        Arrays.fill(CHARS, 7958, 7960, (byte) 33);
        Arrays.fill(CHARS, 7960, 7966, (byte) -19);
        Arrays.fill(CHARS, 7966, 7968, (byte) 33);
        Arrays.fill(CHARS, 7968, 8006, (byte) -19);
        Arrays.fill(CHARS, 8006, 8008, (byte) 33);
        Arrays.fill(CHARS, 8008, 8014, (byte) -19);
        Arrays.fill(CHARS, 8014, 8016, (byte) 33);
        Arrays.fill(CHARS, 8016, 8024, (byte) -19);
        byte[] bArr122 = CHARS;
        bArr122[8024] = 33;
        bArr122[8025] = -19;
        bArr122[8026] = 33;
        bArr122[8027] = -19;
        bArr122[8028] = 33;
        bArr122[8029] = -19;
        bArr122[8030] = 33;
        Arrays.fill(bArr122, 8031, 8062, (byte) -19);
        Arrays.fill(CHARS, 8062, 8064, (byte) 33);
        Arrays.fill(CHARS, 8064, 8117, (byte) -19);
        byte[] bArr123 = CHARS;
        bArr123[8117] = 33;
        Arrays.fill(bArr123, 8118, 8125, (byte) -19);
        byte[] bArr124 = CHARS;
        bArr124[8125] = 33;
        bArr124[8126] = -19;
        Arrays.fill(bArr124, 8127, 8130, (byte) 33);
        Arrays.fill(CHARS, 8130, 8133, (byte) -19);
        byte[] bArr125 = CHARS;
        bArr125[8133] = 33;
        Arrays.fill(bArr125, 8134, 8141, (byte) -19);
        Arrays.fill(CHARS, 8141, 8144, (byte) 33);
        Arrays.fill(CHARS, 8144, 8148, (byte) -19);
        Arrays.fill(CHARS, 8148, 8150, (byte) 33);
        Arrays.fill(CHARS, 8150, 8156, (byte) -19);
        Arrays.fill(CHARS, 8156, 8160, (byte) 33);
        Arrays.fill(CHARS, 8160, 8173, (byte) -19);
        Arrays.fill(CHARS, 8173, 8178, (byte) 33);
        Arrays.fill(CHARS, 8178, 8181, (byte) -19);
        byte[] bArr126 = CHARS;
        bArr126[8181] = 33;
        Arrays.fill(bArr126, 8182, 8189, (byte) -19);
        Arrays.fill(CHARS, 8189, 8400, (byte) 33);
        Arrays.fill(CHARS, 8400, 8413, (byte) -87);
        Arrays.fill(CHARS, 8413, 8417, (byte) 33);
        byte[] bArr127 = CHARS;
        bArr127[8417] = -87;
        Arrays.fill(bArr127, 8418, 8486, (byte) 33);
        byte[] bArr128 = CHARS;
        bArr128[8486] = -19;
        Arrays.fill(bArr128, 8487, 8490, (byte) 33);
        Arrays.fill(CHARS, 8490, 8492, (byte) -19);
        Arrays.fill(CHARS, 8492, 8494, (byte) 33);
        byte[] bArr129 = CHARS;
        bArr129[8494] = -19;
        Arrays.fill(bArr129, 8495, 8576, (byte) 33);
        Arrays.fill(CHARS, 8576, 8579, (byte) -19);
        Arrays.fill(CHARS, 8579, 12293, (byte) 33);
        byte[] bArr130 = CHARS;
        bArr130[12293] = -87;
        bArr130[12294] = 33;
        bArr130[12295] = -19;
        Arrays.fill(bArr130, 12296, (int) EGL.EGL_ALPHA_SIZE, (byte) 33);
        Arrays.fill(CHARS, (int) EGL.EGL_ALPHA_SIZE, 12330, (byte) -19);
        Arrays.fill(CHARS, 12330, 12336, (byte) -87);
        byte[] bArr131 = CHARS;
        bArr131[12336] = 33;
        Arrays.fill(bArr131, 12337, 12342, (byte) -87);
        Arrays.fill(CHARS, 12342, 12353, (byte) 33);
        Arrays.fill(CHARS, 12353, 12437, (byte) -19);
        Arrays.fill(CHARS, 12437, 12441, (byte) 33);
        Arrays.fill(CHARS, 12441, 12443, (byte) -87);
        Arrays.fill(CHARS, 12443, 12445, (byte) 33);
        Arrays.fill(CHARS, 12445, 12447, (byte) -87);
        Arrays.fill(CHARS, 12447, 12449, (byte) 33);
        Arrays.fill(CHARS, 12449, 12539, (byte) -19);
        byte[] bArr132 = CHARS;
        bArr132[12539] = 33;
        Arrays.fill(bArr132, 12540, 12543, (byte) -87);
        Arrays.fill(CHARS, 12543, 12549, (byte) 33);
        Arrays.fill(CHARS, 12549, 12589, (byte) -19);
        Arrays.fill(CHARS, 12589, 19968, (byte) 33);
        Arrays.fill(CHARS, 19968, 40870, (byte) -19);
        Arrays.fill(CHARS, 40870, (int) Normalizer2Impl.Hangul.HANGUL_BASE, (byte) 33);
        Arrays.fill(CHARS, (int) Normalizer2Impl.Hangul.HANGUL_BASE, (int) Normalizer2Impl.Hangul.HANGUL_LIMIT, (byte) -19);
        Arrays.fill(CHARS, (int) Normalizer2Impl.Hangul.HANGUL_LIMIT, 55296, (byte) 33);
        Arrays.fill(CHARS, 57344, 65534, (byte) 33);
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
        return i <= 32 && (CHARS[i] & 2) != 0;
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
        int length = str.length();
        if (length == 0 || !isNameStart(str.charAt(0))) {
            return false;
        }
        for (int i = 1; i < length; i++) {
            if (!isName(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidNCName(String str) {
        int length = str.length();
        if (length == 0 || !isNCNameStart(str.charAt(0))) {
            return false;
        }
        for (int i = 1; i < length; i++) {
            if (!isNCName(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidNmtoken(String str) {
        int length = str.length();
        if (length == 0) {
            return false;
        }
        for (int i = 0; i < length; i++) {
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

    public static String trim(String str) {
        int length = str.length() - 1;
        int i = 0;
        while (i <= length && isSpace(str.charAt(i))) {
            i++;
        }
        int i2 = length;
        while (i2 >= i && isSpace(str.charAt(i2))) {
            i2--;
        }
        if (i == 0 && i2 == length) {
            return str;
        }
        if (i > length) {
            return "";
        }
        return str.substring(i, i2 + 1);
    }
}
