package com.huawei.zxing.datamatrix.encoder;

import com.huawei.android.hardware.input.HwSideTouchManagerEx;
import com.huawei.android.hishow.AlarmInfoEx;
import com.huawei.internal.telephony.SmsConstantsEx;
import com.huawei.internal.telephony.uicc.IccConstantsEx;
import com.huawei.lcagent.client.MetricConstant;
import com.huawei.motiondetection.MotionTypeApps;

public final class ErrorCorrection {
    private static final int[] ALOG = new int[255];
    private static final int[][] FACTORS = {new int[]{228, 48, 15, 111, 62}, new int[]{23, 68, 144, SmsConstantsEx.MAX_USER_DATA_BYTES_WITH_HEADER, 240, 92, 254}, new int[]{28, 24, 185, 166, 223, 248, 116, 255, 110, 61}, new int[]{175, HwSideTouchManagerEx.VOLUME_MODE_STEP, 205, 12, 194, 168, 39, 245, 60, 97, 120}, new int[]{41, SmsConstantsEx.MAX_USER_DATA_SEPTETS_WITH_HEADER, 158, 91, 61, 42, 142, 213, 97, 178, 100, 242}, new int[]{156, 97, 192, 252, 95, 9, 157, 119, HwSideTouchManagerEx.VOLUME_MODE_STEP, 45, 18, 186, 83, 185}, new int[]{83, 195, 100, 39, 188, 75, 66, 61, 241, 213, 109, 129, 94, 254, 225, 48, 90, 188}, new int[]{15, 195, 244, 9, 233, 71, 168, 2, 188, SmsConstantsEx.MAX_USER_DATA_SEPTETS, SmsConstantsEx.MAX_USER_DATA_SEPTETS_WITH_HEADER, 145, 253, 79, MetricConstant.GPS_METRIC_ID_EX, 82, 27, 174, 186, 172}, new int[]{52, 190, 88, 205, 109, 39, IccConstantsEx.SMS_RECORD_LENGTH, 21, 155, 197, 251, 223, 155, 21, 5, 172, 254, 124, 12, 181, 184, 96, 50, 193}, new int[]{211, 231, 43, 97, 71, 96, 103, 174, 37, 151, 170, 53, 75, 34, 249, 121, 17, HwSideTouchManagerEx.VOLUME_MODE_STEP, 110, 213, 141, 136, 120, 151, 233, 168, 93, 255}, new int[]{245, AlarmInfoEx.EVERYDAY_CODE, 242, 218, 130, 250, 162, 181, 102, 120, 84, 179, 220, 251, 80, 182, 229, 18, 2, 4, 68, 33, 101, 137, 95, 119, 115, 44, 175, 184, 59, 25, 225, 98, 81, 112}, new int[]{77, 193, 137, 31, 19, 38, 22, SmsConstantsEx.MAX_USER_DATA_SEPTETS_WITH_HEADER, 247, 105, 122, 2, 245, 133, 242, 8, 175, 95, 100, 9, 167, 105, 214, 111, 57, 121, 21, 1, 253, 57, 54, 101, 248, 202, 69, 50, 150, 177, 226, 5, 9, 5}, new int[]{245, 132, 172, 223, 96, 32, 117, 22, 238, 133, 238, 231, 205, 188, 237, 87, 191, 106, 16, 147, 118, 23, 37, 90, 170, 205, 131, 88, 120, 100, 66, HwSideTouchManagerEx.VOLUME_MODE_STEP, 186, 240, 82, 44, IccConstantsEx.SMS_RECORD_LENGTH, 87, 187, 147, SmsConstantsEx.MAX_USER_DATA_SEPTETS, 175, 69, 213, 92, 253, 225, 19}, new int[]{175, 9, 223, 238, 12, 17, 220, 208, 100, 29, 175, 170, 230, 192, 215, 235, 150, 159, 36, 223, 38, 200, 132, 54, 228, 146, 218, 234, 117, MotionTypeApps.TYPE_FLIP_MUTE_AOD, 29, 232, 144, 238, 22, 150, MotionTypeApps.TYPE_FLIP_MUTE_CALL, 117, 62, 207, 164, 13, 137, 245, AlarmInfoEx.EVERYDAY_CODE, 67, 247, 28, 155, 43, MotionTypeApps.TYPE_FLIP_MUTE_AOD, MetricConstant.BLUETOOTH_METRIC_ID_EX, 233, 53, 143, 46}, new int[]{242, 93, 169, 50, 144, 210, 39, 118, 202, 188, MotionTypeApps.TYPE_FLIP_MUTE_CALL, 189, 143, MetricConstant.GPS_METRIC_ID_EX, 196, 37, 185, 112, SmsConstantsEx.MAX_USER_DATA_BYTES_WITH_HEADER, 230, 245, 63, 197, 190, 250, 106, 185, 221, 175, 64, 114, 71, 161, 44, 147, 6, 27, 218, 51, 63, 87, 10, 40, 130, 188, 17, 163, 31, IccConstantsEx.SMS_RECORD_LENGTH, 170, 4, MetricConstant.BLUETOOTH_METRIC_ID_EX, 232, 7, 94, 166, 224, 124, 86, 47, 11, HwSideTouchManagerEx.VOLUME_MODE_DOUBLE_SLIDE_MAPPING}, new int[]{220, 228, 173, 89, 251, 149, 159, 56, 89, 33, 147, 244, 154, 36, 73, AlarmInfoEx.EVERYDAY_CODE, 213, 136, 248, 180, 234, 197, 158, 177, 68, 122, 93, 213, 15, SmsConstantsEx.MAX_USER_DATA_SEPTETS, 227, 236, 66, 139, SmsConstantsEx.MAX_USER_DATA_SEPTETS_WITH_HEADER, 185, 202, 167, 179, 25, 220, 232, 96, 210, 231, 136, 223, 239, 181, 241, 59, 52, 172, 25, 49, 232, 211, 189, 64, 54, MetricConstant.GPS_METRIC_ID_EX, SmsConstantsEx.MAX_USER_DATA_SEPTETS_WITH_HEADER, 132, 63, 96, 103, 82, 186}};
    private static final int[] FACTOR_SETS = {5, 7, 10, 11, 12, 14, 18, 20, 24, 28, 36, 42, 48, 56, 62, 68};
    private static final int[] LOG = new int[256];
    private static final int MODULO_VALUE = 301;

    static {
        int p = 1;
        for (int i = 0; i < 255; i++) {
            ALOG[i] = p;
            LOG[p] = i;
            p <<= 1;
            if (p >= 256) {
                p ^= 301;
            }
        }
    }

    private ErrorCorrection() {
    }

    public static String encodeECC200(String codewords, SymbolInfo symbolInfo) {
        if (codewords.length() == symbolInfo.getDataCapacity()) {
            StringBuilder sb = new StringBuilder(symbolInfo.getDataCapacity() + symbolInfo.getErrorCodewords());
            sb.append(codewords);
            int blockCount = symbolInfo.getInterleavedBlockCount();
            if (blockCount == 1) {
                sb.append(createECCBlock(codewords, symbolInfo.getErrorCodewords()));
            } else {
                sb.setLength(sb.capacity());
                int[] dataSizes = new int[blockCount];
                int[] errorSizes = new int[blockCount];
                int[] startPos = new int[blockCount];
                for (int i = 0; i < blockCount; i++) {
                    dataSizes[i] = symbolInfo.getDataLengthForInterleavedBlock(i + 1);
                    errorSizes[i] = symbolInfo.getErrorLengthForInterleavedBlock(i + 1);
                    startPos[i] = 0;
                    if (i > 0) {
                        startPos[i] = startPos[i - 1] + dataSizes[i];
                    }
                }
                for (int block = 0; block < blockCount; block++) {
                    StringBuilder temp = new StringBuilder(dataSizes[block]);
                    for (int d = block; d < symbolInfo.getDataCapacity(); d += blockCount) {
                        temp.append(codewords.charAt(d));
                    }
                    String ecc = createECCBlock(temp.toString(), errorSizes[block]);
                    int pos = 0;
                    int e = block;
                    while (e < errorSizes[block] * blockCount) {
                        sb.setCharAt(symbolInfo.getDataCapacity() + e, ecc.charAt(pos));
                        e += blockCount;
                        pos++;
                    }
                }
            }
            return sb.toString();
        }
        throw new IllegalArgumentException("The number of codewords does not match the selected symbol");
    }

    private static String createECCBlock(CharSequence codewords, int numECWords) {
        return createECCBlock(codewords, 0, codewords.length(), numECWords);
    }

    /* JADX INFO: Multiple debug info for r3v6 char[]: [D('eccReversed' char[]), D('i' int)] */
    private static String createECCBlock(CharSequence codewords, int start, int len, int numECWords) {
        int table = -1;
        int i = 0;
        while (true) {
            int[] iArr = FACTOR_SETS;
            if (i >= iArr.length) {
                break;
            } else if (iArr[i] == numECWords) {
                table = i;
                break;
            } else {
                i++;
            }
        }
        if (table >= 0) {
            int[] poly = FACTORS[table];
            char[] ecc = new char[numECWords];
            for (int i2 = 0; i2 < numECWords; i2++) {
                ecc[i2] = 0;
            }
            for (int i3 = start; i3 < start + len; i3++) {
                int m = ecc[numECWords - 1] ^ codewords.charAt(i3);
                for (int k = numECWords - 1; k > 0; k--) {
                    if (m == 0 || poly[k] == 0) {
                        ecc[k] = ecc[k - 1];
                    } else {
                        char c = ecc[k - 1];
                        int[] iArr2 = ALOG;
                        int[] iArr3 = LOG;
                        ecc[k] = (char) (c ^ iArr2[(iArr3[m] + iArr3[poly[k]]) % 255]);
                    }
                }
                if (m == 0 || poly[0] == 0) {
                    ecc[0] = 0;
                } else {
                    int[] iArr4 = ALOG;
                    int[] iArr5 = LOG;
                    ecc[0] = (char) iArr4[(iArr5[m] + iArr5[poly[0]]) % 255];
                }
            }
            char[] eccReversed = new char[numECWords];
            for (int i4 = 0; i4 < numECWords; i4++) {
                eccReversed[i4] = ecc[(numECWords - i4) - 1];
            }
            return String.valueOf(eccReversed);
        }
        throw new IllegalArgumentException("Illegal number of error correction codewords specified: " + numECWords);
    }
}
