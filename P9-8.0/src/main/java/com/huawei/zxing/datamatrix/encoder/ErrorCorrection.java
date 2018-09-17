package com.huawei.zxing.datamatrix.encoder;

import android.telephony.HwCarrierConfigManager;
import com.huawei.android.util.JlogConstantsEx;
import com.huawei.internal.telephony.SmsConstantsEx;
import com.huawei.internal.telephony.uicc.IccConstantsEx;
import com.huawei.lcagent.client.MetricConstant;
import com.huawei.motiondetection.MotionTypeApps;

public final class ErrorCorrection {
    private static final int[] ALOG = new int[255];
    private static final int[][] FACTORS = new int[][]{new int[]{228, 48, 15, 111, 62}, new int[]{23, 68, 144, SmsConstantsEx.MAX_USER_DATA_BYTES_WITH_HEADER, 240, 92, 254}, new int[]{28, 24, 185, 166, 223, 248, JlogConstantsEx.JLID_CONTACT_BIND_EDITOR_FOR_NEW, 255, 110, 61}, new int[]{175, JlogConstantsEx.JLID_MMS_MESSAGES_DELETE, 205, 12, 194, 168, 39, 245, 60, 97, JlogConstantsEx.JLID_DIALPAD_ONTOUCH_NOT_FIRST_DOWN}, new int[]{41, 153, 158, 91, 61, 42, 142, 213, 97, 178, 100, 242}, new int[]{156, 97, HwCarrierConfigManager.HD_ICON_MASK_DIALER, 252, 95, 9, 157, JlogConstantsEx.JLID_CONTACT_MULTISELECT_BIND_VIEW, JlogConstantsEx.JLID_MMS_MESSAGES_DELETE, 45, 18, 186, 83, 185}, new int[]{83, 195, 100, 39, 188, 75, 66, 61, 241, 213, MetricConstant.SCREEN_METRIC_ID_EX, 129, 94, 254, 225, 48, 90, 188}, new int[]{15, 195, 244, 9, 233, 71, 168, 2, 188, SmsConstantsEx.MAX_USER_DATA_SEPTETS, 153, 145, 253, 79, MetricConstant.GPS_METRIC_ID_EX, 82, 27, 174, 186, 172}, new int[]{52, 190, 88, 205, MetricConstant.SCREEN_METRIC_ID_EX, 39, IccConstantsEx.SMS_RECORD_LENGTH, 21, 155, 197, 251, 223, 155, 21, 5, 172, 254, JlogConstantsEx.JLID_NEW_CONTACT_SAVE_CLICK, 12, 181, 184, 96, 50, 193}, new int[]{211, 231, 43, 97, 71, 96, 103, 174, 37, 151, 170, 53, 75, 34, 249, JlogConstantsEx.JLID_DIALPAD_ADAPTER_GET_VIEW, 17, JlogConstantsEx.JLID_MMS_MESSAGES_DELETE, 110, 213, JlogConstantsEx.JLID_MMS_MATCHED_CONTACTS_SEARCH, 136, JlogConstantsEx.JLID_DIALPAD_ONTOUCH_NOT_FIRST_DOWN, 151, 233, 168, 93, 255}, new int[]{245, 127, 242, 218, 130, 250, 162, 181, 102, JlogConstantsEx.JLID_DIALPAD_ONTOUCH_NOT_FIRST_DOWN, 84, 179, 220, 251, 80, 182, 229, 18, 2, 4, 68, 33, 101, JlogConstantsEx.JLID_MMS_CONVERSATIONS_DELETE, 95, JlogConstantsEx.JLID_CONTACT_MULTISELECT_BIND_VIEW, JlogConstantsEx.JLID_CONTACT_DETAIL_BIND_VIEW, 44, 175, 184, 59, 25, 225, 98, 81, 112}, new int[]{77, 193, JlogConstantsEx.JLID_MMS_CONVERSATIONS_DELETE, 31, 19, 38, 22, 153, 247, 105, JlogConstantsEx.JLID_DEF_CONTACT_ITEM_CLICK, 2, 245, 133, 242, 8, 175, 95, 100, 9, 167, 105, 214, 111, 57, JlogConstantsEx.JLID_DIALPAD_ADAPTER_GET_VIEW, 21, 1, 253, 57, 54, 101, 248, MotionTypeApps.TYPE_FLIP_MUTE_CLOCK, 69, 50, 150, 177, 226, 5, 9, 5}, new int[]{245, JlogConstantsEx.JLID_EDIT_CONTACT_END, 172, 223, 96, 32, JlogConstantsEx.JLID_DIALPAD_AFTER_TEXT_CHANGE, 22, 238, 133, 238, 231, 205, 188, 237, 87, 191, MetricConstant.WIFI_METRIC_ID_EX, 16, 147, JlogConstantsEx.JLID_CONTACT_MULTISELECT_ACTIVITY_ONCREATE, 23, 37, 90, 170, 205, 131, 88, JlogConstantsEx.JLID_DIALPAD_ONTOUCH_NOT_FIRST_DOWN, 100, 66, JlogConstantsEx.JLID_MMS_MESSAGES_DELETE, 186, 240, 82, 44, IccConstantsEx.SMS_RECORD_LENGTH, 87, 187, 147, SmsConstantsEx.MAX_USER_DATA_SEPTETS, 175, 69, 213, 92, 253, 225, 19}, new int[]{175, 9, 223, 238, 12, 17, 220, 208, 100, 29, 175, 170, 230, HwCarrierConfigManager.HD_ICON_MASK_DIALER, 215, 235, 150, 159, 36, 223, 38, 200, JlogConstantsEx.JLID_EDIT_CONTACT_END, 54, 228, 146, 218, 234, JlogConstantsEx.JLID_DIALPAD_AFTER_TEXT_CHANGE, MotionTypeApps.TYPE_FLIP_MUTE_AOD, 29, 232, 144, 238, 22, 150, MotionTypeApps.TYPE_FLIP_MUTE_CALL, JlogConstantsEx.JLID_DIALPAD_AFTER_TEXT_CHANGE, 62, 207, 164, 13, JlogConstantsEx.JLID_MMS_CONVERSATIONS_DELETE, 245, 127, 67, 247, 28, 155, 43, MotionTypeApps.TYPE_FLIP_MUTE_AOD, MetricConstant.BLUETOOTH_METRIC_ID_EX, 233, 53, 143, 46}, new int[]{242, 93, 169, 50, 144, 210, 39, JlogConstantsEx.JLID_CONTACT_MULTISELECT_ACTIVITY_ONCREATE, MotionTypeApps.TYPE_FLIP_MUTE_CLOCK, 188, MotionTypeApps.TYPE_FLIP_MUTE_CALL, 189, 143, MetricConstant.GPS_METRIC_ID_EX, 196, 37, 185, 112, SmsConstantsEx.MAX_USER_DATA_BYTES_WITH_HEADER, 230, 245, 63, 197, 190, 250, MetricConstant.WIFI_METRIC_ID_EX, 185, 221, 175, 64, 114, 71, 161, 44, 147, 6, 27, 218, 51, 63, 87, 10, 40, 130, 188, 17, 163, 31, IccConstantsEx.SMS_RECORD_LENGTH, 170, 4, MetricConstant.BLUETOOTH_METRIC_ID_EX, 232, 7, 94, 166, 224, JlogConstantsEx.JLID_NEW_CONTACT_SAVE_CLICK, 86, 47, 11, 204}, new int[]{220, 228, 173, 89, 251, 149, 159, 56, 89, 33, 147, 244, 154, 36, 73, 127, 213, 136, 248, 180, 234, 197, 158, 177, 68, JlogConstantsEx.JLID_DEF_CONTACT_ITEM_CLICK, 93, 213, 15, SmsConstantsEx.MAX_USER_DATA_SEPTETS, 227, 236, 66, JlogConstantsEx.JLID_MMS_MESSAGE_SEARCH, 153, 185, MotionTypeApps.TYPE_FLIP_MUTE_CLOCK, 167, 179, 25, 220, 232, 96, 210, 231, 136, 223, 239, 181, 241, 59, 52, 172, 25, 49, 232, 211, 189, 64, 54, MetricConstant.GPS_METRIC_ID_EX, 153, JlogConstantsEx.JLID_EDIT_CONTACT_END, 63, 96, 103, 82, 186}};
    private static final int[] FACTOR_SETS = new int[]{5, 7, 10, 11, 12, 14, 18, 20, 24, 28, 36, 42, 48, 56, 62, 68};
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
        if (codewords.length() != symbolInfo.getDataCapacity()) {
            throw new IllegalArgumentException("The number of codewords does not match the selected symbol");
        }
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
                    int pos2 = pos + 1;
                    sb.setCharAt(symbolInfo.getDataCapacity() + e, ecc.charAt(pos));
                    e += blockCount;
                    pos = pos2;
                }
            }
        }
        return sb.toString();
    }

    private static String createECCBlock(CharSequence codewords, int numECWords) {
        return createECCBlock(codewords, 0, codewords.length(), numECWords);
    }

    private static String createECCBlock(CharSequence codewords, int start, int len, int numECWords) {
        int i;
        int table = -1;
        for (i = 0; i < FACTOR_SETS.length; i++) {
            if (FACTOR_SETS[i] == numECWords) {
                table = i;
                break;
            }
        }
        if (table < 0) {
            throw new IllegalArgumentException("Illegal number of error correction codewords specified: " + numECWords);
        }
        int[] poly = FACTORS[table];
        char[] ecc = new char[numECWords];
        for (i = 0; i < numECWords; i++) {
            ecc[i] = 0;
        }
        for (i = start; i < start + len; i++) {
            int m = ecc[numECWords - 1] ^ codewords.charAt(i);
            int k = numECWords - 1;
            while (k > 0) {
                if (m == 0 || poly[k] == 0) {
                    ecc[k] = ecc[k - 1];
                } else {
                    ecc[k] = (char) (ecc[k - 1] ^ ALOG[(LOG[m] + LOG[poly[k]]) % 255]);
                }
                k--;
            }
            if (m == 0 || poly[0] == 0) {
                ecc[0] = 0;
            } else {
                ecc[0] = (char) ALOG[(LOG[m] + LOG[poly[0]]) % 255];
            }
        }
        char[] eccReversed = new char[numECWords];
        for (i = 0; i < numECWords; i++) {
            eccReversed[i] = ecc[(numECWords - i) - 1];
        }
        return String.valueOf(eccReversed);
    }
}
