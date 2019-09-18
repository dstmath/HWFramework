package com.huawei.zxing.qrcode.decoder;

import com.huawei.android.app.AppOpsManagerEx;
import com.huawei.android.util.JlogConstantsEx;
import com.huawei.internal.telephony.SmsConstantsEx;
import com.huawei.lcagent.client.MetricConstant;
import com.huawei.zxing.FormatException;
import com.huawei.zxing.common.BitMatrix;

public final class Version {
    private static final Version[] VERSIONS = buildVersions();
    private static final int[] VERSION_DECODE_INFO = {31892, 34236, 39577, 42195, 48118, 51042, 55367, 58893, 63784, 68472, 70749, 76311, 79154, 84390, 87683, 92361, 96236, 102084, 102881, 110507, 110734, 117786, 119615, 126325, 127568, 133589, 136944, 141498, 145311, 150283, 152622, 158308, 161089, 167017};
    private final int[] alignmentPatternCenters;
    private final ECBlocks[] ecBlocks;
    private final int totalCodewords;
    private final int versionNumber;

    public static final class ECB {
        private final int count;
        private final int dataCodewords;

        ECB(int count2, int dataCodewords2) {
            this.count = count2;
            this.dataCodewords = dataCodewords2;
        }

        public int getCount() {
            return this.count;
        }

        public int getDataCodewords() {
            return this.dataCodewords;
        }
    }

    public static final class ECBlocks {
        private final ECB[] ecBlocks;
        private final int ecCodewordsPerBlock;

        ECBlocks(int ecCodewordsPerBlock2, ECB... ecBlocks2) {
            this.ecCodewordsPerBlock = ecCodewordsPerBlock2;
            this.ecBlocks = ecBlocks2;
        }

        public int getECCodewordsPerBlock() {
            return this.ecCodewordsPerBlock;
        }

        public int getNumBlocks() {
            int total = 0;
            for (ECB ecBlock : this.ecBlocks) {
                total += ecBlock.getCount();
            }
            return total;
        }

        public int getTotalECCodewords() {
            return this.ecCodewordsPerBlock * getNumBlocks();
        }

        public ECB[] getECBlocks() {
            return this.ecBlocks;
        }
    }

    private Version(int versionNumber2, int[] alignmentPatternCenters2, ECBlocks... ecBlocks2) {
        this.versionNumber = versionNumber2;
        this.alignmentPatternCenters = alignmentPatternCenters2;
        this.ecBlocks = ecBlocks2;
        int total = 0;
        int ecCodewords = ecBlocks2[0].getECCodewordsPerBlock();
        for (ECB ecBlock : ecBlocks2[0].getECBlocks()) {
            total += ecBlock.getCount() * (ecBlock.getDataCodewords() + ecCodewords);
        }
        this.totalCodewords = total;
    }

    public int getVersionNumber() {
        return this.versionNumber;
    }

    public int[] getAlignmentPatternCenters() {
        return this.alignmentPatternCenters;
    }

    public int getTotalCodewords() {
        return this.totalCodewords;
    }

    public int getDimensionForVersion() {
        return 17 + (4 * this.versionNumber);
    }

    public ECBlocks getECBlocksForLevel(ErrorCorrectionLevel ecLevel) {
        return this.ecBlocks[ecLevel.ordinal()];
    }

    public static Version getProvisionalVersionForDimension(int dimension) throws FormatException {
        if (dimension % 4 == 1) {
            try {
                return getVersionForNumber((dimension - 17) >> 2);
            } catch (IllegalArgumentException e) {
                throw FormatException.getFormatInstance();
            }
        } else {
            throw FormatException.getFormatInstance();
        }
    }

    public static Version getVersionForNumber(int versionNumber2) {
        if (versionNumber2 >= 1 && versionNumber2 <= 40) {
            return VERSIONS[versionNumber2 - 1];
        }
        throw new IllegalArgumentException();
    }

    static Version decodeVersionInformation(int versionBits) {
        int bestDifference = Integer.MAX_VALUE;
        int bestVersion = 0;
        for (int i = 0; i < VERSION_DECODE_INFO.length; i++) {
            int targetVersion = VERSION_DECODE_INFO[i];
            if (targetVersion == versionBits) {
                return getVersionForNumber(i + 7);
            }
            int bitsDifference = FormatInformation.numBitsDiffering(versionBits, targetVersion);
            if (bitsDifference < bestDifference) {
                bestVersion = i + 7;
                bestDifference = bitsDifference;
            }
        }
        if (bestDifference <= 3) {
            return getVersionForNumber(bestVersion);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public BitMatrix buildFunctionPattern() {
        BitMatrix bitMatrix = new BitMatrix(getDimensionForVersion());
        bitMatrix.setRegion(0, 0, 9, 9);
        bitMatrix.setRegion(dimension - 8, 0, 8, 9);
        bitMatrix.setRegion(0, dimension - 8, 9, 8);
        int max = this.alignmentPatternCenters.length;
        for (int x = 0; x < max; x++) {
            int i = this.alignmentPatternCenters[x] - 2;
            for (int y = 0; y < max; y++) {
                if (!((x == 0 && (y == 0 || y == max - 1)) || (x == max - 1 && y == 0))) {
                    bitMatrix.setRegion(this.alignmentPatternCenters[y] - 2, i, 5, 5);
                }
            }
        }
        bitMatrix.setRegion(6, 9, 1, dimension - 17);
        bitMatrix.setRegion(9, 6, dimension - 17, 1);
        if (this.versionNumber > 6) {
            bitMatrix.setRegion(dimension - 11, 0, 3, 6);
            bitMatrix.setRegion(0, dimension - 11, 6, 3);
        }
        return bitMatrix;
    }

    public String toString() {
        return String.valueOf(this.versionNumber);
    }

    private static Version[] buildVersions() {
        return new Version[]{new Version(1, new int[0], new ECBlocks(7, new ECB(1, 19)), new ECBlocks(10, new ECB(1, 16)), new ECBlocks(13, new ECB(1, 13)), new ECBlocks(17, new ECB(1, 9))), new Version(2, new int[]{6, 18}, new ECBlocks(10, new ECB(1, 34)), new ECBlocks(16, new ECB(1, 28)), new ECBlocks(22, new ECB(1, 22)), new ECBlocks(28, new ECB(1, 16))), new Version(3, new int[]{6, 22}, new ECBlocks(15, new ECB(1, 55)), new ECBlocks(26, new ECB(1, 44)), new ECBlocks(18, new ECB(2, 17)), new ECBlocks(22, new ECB(2, 13))), new Version(4, new int[]{6, 26}, new ECBlocks(20, new ECB(1, 80)), new ECBlocks(18, new ECB(2, 32)), new ECBlocks(26, new ECB(2, 24)), new ECBlocks(16, new ECB(4, 9))), new Version(5, new int[]{6, 30}, new ECBlocks(26, new ECB(1, MetricConstant.GPS_METRIC_ID_EX)), new ECBlocks(24, new ECB(2, 43)), new ECBlocks(18, new ECB(2, 15), new ECB(2, 16)), new ECBlocks(22, new ECB(2, 11), new ECB(2, 12))), new Version(6, new int[]{6, 34}, new ECBlocks(18, new ECB(2, 68)), new ECBlocks(16, new ECB(4, 27)), new ECBlocks(24, new ECB(4, 19)), new ECBlocks(28, new ECB(4, 15))), new Version(7, new int[]{6, 22, 38}, new ECBlocks(20, new ECB(2, 78)), new ECBlocks(18, new ECB(4, 31)), new ECBlocks(18, new ECB(2, 14), new ECB(4, 15)), new ECBlocks(26, new ECB(4, 13), new ECB(1, 14))), new Version(8, new int[]{6, 24, 42}, new ECBlocks(24, new ECB(2, 97)), new ECBlocks(22, new ECB(2, 38), new ECB(2, 39)), new ECBlocks(22, new ECB(4, 18), new ECB(2, 19)), new ECBlocks(26, new ECB(4, 14), new ECB(2, 15))), new Version(9, new int[]{6, 26, 46}, new ECBlocks(30, new ECB(2, JlogConstantsEx.JLID_CONTACT_BIND_EDITOR_FOR_NEW)), new ECBlocks(22, new ECB(3, 36), new ECB(2, 37)), new ECBlocks(20, new ECB(4, 16), new ECB(4, 17)), new ECBlocks(24, new ECB(4, 12), new ECB(4, 13))), new Version(10, new int[]{6, 28, 50}, new ECBlocks(18, new ECB(2, 68), new ECB(2, 69)), new ECBlocks(26, new ECB(4, 43), new ECB(1, 44)), new ECBlocks(24, new ECB(6, 19), new ECB(2, 20)), new ECBlocks(28, new ECB(6, 15), new ECB(2, 16))), new Version(11, new int[]{6, 30, 54}, new ECBlocks(20, new ECB(4, 81)), new ECBlocks(30, new ECB(1, 50), new ECB(4, 51)), new ECBlocks(28, new ECB(4, 22), new ECB(4, 23)), new ECBlocks(24, new ECB(3, 12), new ECB(8, 13))), new Version(12, new int[]{6, 32, 58}, new ECBlocks(24, new ECB(2, 92), new ECB(2, 93)), new ECBlocks(22, new ECB(6, 36), new ECB(2, 37)), new ECBlocks(26, new ECB(4, 20), new ECB(6, 21)), new ECBlocks(28, new ECB(7, 14), new ECB(4, 15))), new Version(13, new int[]{6, 34, 62}, new ECBlocks(26, new ECB(4, MetricConstant.BLUETOOTH_METRIC_ID_EX)), new ECBlocks(22, new ECB(8, 37), new ECB(1, 38)), new ECBlocks(24, new ECB(8, 20), new ECB(4, 21)), new ECBlocks(22, new ECB(12, 11), new ECB(4, 12))), new Version(14, new int[]{6, 26, 46, 66}, new ECBlocks(30, new ECB(3, JlogConstantsEx.JLID_CONTACT_DETAIL_BIND_VIEW), new ECB(1, JlogConstantsEx.JLID_CONTACT_BIND_EDITOR_FOR_NEW)), new ECBlocks(24, new ECB(4, 40), new ECB(5, 41)), new ECBlocks(20, new ECB(11, 16), new ECB(5, 17)), new ECBlocks(24, new ECB(11, 12), new ECB(5, 13))), new Version(15, new int[]{6, 26, 48, 70}, new ECBlocks(22, new ECB(5, 87), new ECB(1, 88)), new ECBlocks(24, new ECB(5, 41), new ECB(5, 42)), new ECBlocks(30, new ECB(5, 24), new ECB(7, 25)), new ECBlocks(24, new ECB(11, 12), new ECB(7, 13))), new Version(16, new int[]{6, 26, 50, 74}, new ECBlocks(24, new ECB(5, 98), new ECB(1, 99)), new ECBlocks(28, new ECB(7, 45), new ECB(3, 46)), new ECBlocks(24, new ECB(15, 19), new ECB(2, 20)), new ECBlocks(30, new ECB(3, 15), new ECB(13, 16))), new Version(17, new int[]{6, 30, 54, 78}, new ECBlocks(28, new ECB(1, MetricConstant.BLUETOOTH_METRIC_ID_EX), new ECB(5, MetricConstant.GPS_METRIC_ID_EX)), new ECBlocks(28, new ECB(10, 46), new ECB(1, 47)), new ECBlocks(28, new ECB(1, 22), new ECB(15, 23)), new ECBlocks(28, new ECB(2, 14), new ECB(17, 15))), new Version(18, new int[]{6, 30, 56, 82}, new ECBlocks(30, new ECB(5, JlogConstantsEx.JLID_DIALPAD_ONTOUCH_NOT_FIRST_DOWN), new ECB(1, JlogConstantsEx.JLID_DIALPAD_ADAPTER_GET_VIEW)), new ECBlocks(26, new ECB(9, 43), new ECB(4, 44)), new ECBlocks(28, new ECB(17, 22), new ECB(1, 23)), new ECBlocks(28, new ECB(2, 14), new ECB(19, 15))), new Version(19, new int[]{6, 30, 58, 86}, new ECBlocks(28, new ECB(3, 113), new ECB(4, 114)), new ECBlocks(26, new ECB(3, 44), new ECB(11, 45)), new ECBlocks(26, new ECB(17, 21), new ECB(4, 22)), new ECBlocks(26, new ECB(9, 13), new ECB(16, 14))), new Version(20, new int[]{6, 34, 62, 90}, new ECBlocks(28, new ECB(3, MetricConstant.BLUETOOTH_METRIC_ID_EX), new ECB(5, MetricConstant.GPS_METRIC_ID_EX)), new ECBlocks(26, new ECB(3, 41), new ECB(13, 42)), new ECBlocks(30, new ECB(15, 24), new ECB(5, 25)), new ECBlocks(28, new ECB(15, 15), new ECB(10, 16))), new Version(21, new int[]{6, 28, 50, 72, 94}, new ECBlocks(28, new ECB(4, JlogConstantsEx.JLID_CONTACT_BIND_EDITOR_FOR_NEW), new ECB(4, JlogConstantsEx.JLID_DIALPAD_AFTER_TEXT_CHANGE)), new ECBlocks(26, new ECB(17, 42)), new ECBlocks(28, new ECB(17, 22), new ECB(6, 23)), new ECBlocks(30, new ECB(19, 16), new ECB(6, 17))), new Version(22, new int[]{6, 26, 50, 74, 98}, new ECBlocks(28, new ECB(2, 111), new ECB(7, 112)), new ECBlocks(28, new ECB(17, 46)), new ECBlocks(30, new ECB(7, 24), new ECB(16, 25)), new ECBlocks(24, new ECB(34, 13))), new Version(23, new int[]{6, 30, 54, 78, 102}, new ECBlocks(30, new ECB(4, JlogConstantsEx.JLID_DIALPAD_ADAPTER_GET_VIEW), new ECB(5, JlogConstantsEx.JLID_DEF_CONTACT_ITEM_CLICK)), new ECBlocks(28, new ECB(4, 47), new ECB(14, 48)), new ECBlocks(30, new ECB(11, 24), new ECB(14, 25)), new ECBlocks(30, new ECB(16, 15), new ECB(14, 16))), new Version(24, new int[]{6, 28, 54, 80, 106}, new ECBlocks(30, new ECB(6, JlogConstantsEx.JLID_DIALPAD_AFTER_TEXT_CHANGE), new ECB(4, JlogConstantsEx.JLID_CONTACT_MULTISELECT_ACTIVITY_ONCREATE)), new ECBlocks(28, new ECB(6, 45), new ECB(14, 46)), new ECBlocks(30, new ECB(11, 24), new ECB(16, 25)), new ECBlocks(30, new ECB(30, 16), new ECB(2, 17))), new Version(25, new int[]{6, 32, 58, 84, 110}, new ECBlocks(26, new ECB(8, 106), new ECB(4, MetricConstant.BLUETOOTH_METRIC_ID_EX)), new ECBlocks(28, new ECB(8, 47), new ECB(13, 48)), new ECBlocks(30, new ECB(7, 24), new ECB(22, 25)), new ECBlocks(30, new ECB(22, 15), new ECB(13, 16))), new Version(26, new int[]{6, 30, 58, 86, 114}, new ECBlocks(28, new ECB(10, 114), new ECB(2, JlogConstantsEx.JLID_CONTACT_DETAIL_BIND_VIEW)), new ECBlocks(28, new ECB(19, 46), new ECB(4, 47)), new ECBlocks(28, new ECB(28, 22), new ECB(6, 23)), new ECBlocks(30, new ECB(33, 16), new ECB(4, 17))), new Version(27, new int[]{6, 34, 62, 90, JlogConstantsEx.JLID_CONTACT_MULTISELECT_ACTIVITY_ONCREATE}, new ECBlocks(30, new ECB(8, JlogConstantsEx.JLID_DEF_CONTACT_ITEM_CLICK), new ECB(4, JlogConstantsEx.JLID_NEW_CONTACT_CLICK)), new ECBlocks(28, new ECB(22, 45), new ECB(3, 46)), new ECBlocks(30, new ECB(8, 23), new ECB(26, 24)), new ECBlocks(30, new ECB(12, 15), new ECB(28, 16))), new Version(28, new int[]{6, 26, 50, 74, 98, JlogConstantsEx.JLID_DEF_CONTACT_ITEM_CLICK}, new ECBlocks(30, new ECB(3, JlogConstantsEx.JLID_DIALPAD_AFTER_TEXT_CHANGE), new ECB(10, JlogConstantsEx.JLID_CONTACT_MULTISELECT_ACTIVITY_ONCREATE)), new ECBlocks(28, new ECB(3, 45), new ECB(23, 46)), new ECBlocks(30, new ECB(4, 24), new ECB(31, 25)), new ECBlocks(30, new ECB(11, 15), new ECB(31, 16))), new Version(29, new int[]{6, 30, 54, 78, 102, JlogConstantsEx.JLID_NEW_CONTACT_SELECT_ACCOUNT}, new ECBlocks(30, new ECB(7, JlogConstantsEx.JLID_CONTACT_BIND_EDITOR_FOR_NEW), new ECB(7, JlogConstantsEx.JLID_DIALPAD_AFTER_TEXT_CHANGE)), new ECBlocks(28, new ECB(21, 45), new ECB(7, 46)), new ECBlocks(30, new ECB(1, 23), new ECB(37, 24)), new ECBlocks(30, new ECB(19, 15), new ECB(26, 16))), new Version(30, new int[]{6, 26, 52, 78, 104, 130}, new ECBlocks(30, new ECB(5, JlogConstantsEx.JLID_CONTACT_DETAIL_BIND_VIEW), new ECB(10, JlogConstantsEx.JLID_CONTACT_BIND_EDITOR_FOR_NEW)), new ECBlocks(28, new ECB(19, 47), new ECB(10, 48)), new ECBlocks(30, new ECB(15, 24), new ECB(25, 25)), new ECBlocks(30, new ECB(23, 15), new ECB(25, 16))), new Version(31, new int[]{6, 30, 56, 82, MetricConstant.GPS_METRIC_ID_EX, SmsConstantsEx.MAX_USER_DATA_BYTES_WITH_HEADER}, new ECBlocks(30, new ECB(13, JlogConstantsEx.JLID_CONTACT_DETAIL_BIND_VIEW), new ECB(3, JlogConstantsEx.JLID_CONTACT_BIND_EDITOR_FOR_NEW)), new ECBlocks(28, new ECB(2, 46), new ECB(29, 47)), new ECBlocks(30, new ECB(42, 24), new ECB(1, 25)), new ECBlocks(30, new ECB(23, 15), new ECB(28, 16))), new Version(32, new int[]{6, 34, 60, 86, 112, JlogConstantsEx.JLID_MMS_MESSAGES_DELETE}, new ECBlocks(30, new ECB(17, JlogConstantsEx.JLID_CONTACT_DETAIL_BIND_VIEW)), new ECBlocks(28, new ECB(10, 46), new ECB(23, 47)), new ECBlocks(30, new ECB(10, 24), new ECB(35, 25)), new ECBlocks(30, new ECB(19, 15), new ECB(35, 16))), new Version(33, new int[]{6, 30, 58, 86, 114, 142}, new ECBlocks(30, new ECB(17, JlogConstantsEx.JLID_CONTACT_DETAIL_BIND_VIEW), new ECB(1, JlogConstantsEx.JLID_CONTACT_BIND_EDITOR_FOR_NEW)), new ECBlocks(28, new ECB(14, 46), new ECB(21, 47)), new ECBlocks(30, new ECB(29, 24), new ECB(19, 25)), new ECBlocks(30, new ECB(11, 15), new ECB(46, 16))), new Version(34, new int[]{6, 34, 62, 90, JlogConstantsEx.JLID_CONTACT_MULTISELECT_ACTIVITY_ONCREATE, 146}, new ECBlocks(30, new ECB(13, JlogConstantsEx.JLID_CONTACT_DETAIL_BIND_VIEW), new ECB(6, JlogConstantsEx.JLID_CONTACT_BIND_EDITOR_FOR_NEW)), new ECBlocks(28, new ECB(14, 46), new ECB(23, 47)), new ECBlocks(30, new ECB(44, 24), new ECB(7, 25)), new ECBlocks(30, new ECB(59, 16), new ECB(1, 17))), new Version(35, new int[]{6, 30, 54, 78, 102, JlogConstantsEx.JLID_NEW_CONTACT_SELECT_ACCOUNT, 150}, new ECBlocks(30, new ECB(12, JlogConstantsEx.JLID_DIALPAD_ADAPTER_GET_VIEW), new ECB(7, JlogConstantsEx.JLID_DEF_CONTACT_ITEM_CLICK)), new ECBlocks(28, new ECB(12, 47), new ECB(26, 48)), new ECBlocks(30, new ECB(39, 24), new ECB(14, 25)), new ECBlocks(30, new ECB(22, 15), new ECB(41, 16))), new Version(36, new int[]{6, 24, 50, 76, 102, AppOpsManagerEx.TYPE_MICROPHONE, 154}, new ECBlocks(30, new ECB(6, JlogConstantsEx.JLID_DIALPAD_ADAPTER_GET_VIEW), new ECB(14, JlogConstantsEx.JLID_DEF_CONTACT_ITEM_CLICK)), new ECBlocks(28, new ECB(6, 47), new ECB(34, 48)), new ECBlocks(30, new ECB(46, 24), new ECB(10, 25)), new ECBlocks(30, new ECB(2, 15), new ECB(64, 16))), new Version(37, new int[]{6, 28, 54, 80, 106, JlogConstantsEx.JLID_EDIT_CONTACT_END, 158}, new ECBlocks(30, new ECB(17, JlogConstantsEx.JLID_DEF_CONTACT_ITEM_CLICK), new ECB(4, JlogConstantsEx.JLID_NEW_CONTACT_CLICK)), new ECBlocks(28, new ECB(29, 46), new ECB(14, 47)), new ECBlocks(30, new ECB(49, 24), new ECB(10, 25)), new ECBlocks(30, new ECB(24, 15), new ECB(46, 16))), new Version(38, new int[]{6, 32, 58, 84, 110, 136, 162}, new ECBlocks(30, new ECB(4, JlogConstantsEx.JLID_DEF_CONTACT_ITEM_CLICK), new ECB(18, JlogConstantsEx.JLID_NEW_CONTACT_CLICK)), new ECBlocks(28, new ECB(13, 46), new ECB(32, 47)), new ECBlocks(30, new ECB(48, 24), new ECB(14, 25)), new ECBlocks(30, new ECB(42, 15), new ECB(32, 16))), new Version(39, new int[]{6, 26, 54, 82, 110, JlogConstantsEx.JLID_MMS_MESSAGES_DELETE, 166}, new ECBlocks(30, new ECB(20, JlogConstantsEx.JLID_DIALPAD_AFTER_TEXT_CHANGE), new ECB(4, JlogConstantsEx.JLID_CONTACT_MULTISELECT_ACTIVITY_ONCREATE)), new ECBlocks(28, new ECB(40, 47), new ECB(7, 48)), new ECBlocks(30, new ECB(43, 24), new ECB(22, 25)), new ECBlocks(30, new ECB(10, 15), new ECB(67, 16))), new Version(40, new int[]{6, 30, 58, 86, 114, 142, 170}, new ECBlocks(30, new ECB(19, JlogConstantsEx.JLID_CONTACT_MULTISELECT_ACTIVITY_ONCREATE), new ECB(6, JlogConstantsEx.JLID_CONTACT_MULTISELECT_BIND_VIEW)), new ECBlocks(28, new ECB(18, 47), new ECB(31, 48)), new ECBlocks(30, new ECB(34, 24), new ECB(34, 25)), new ECBlocks(30, new ECB(20, 15), new ECB(61, 16)))};
    }
}
