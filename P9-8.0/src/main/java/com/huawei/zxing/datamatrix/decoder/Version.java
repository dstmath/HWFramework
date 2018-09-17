package com.huawei.zxing.datamatrix.decoder;

import com.huawei.android.util.JlogConstantsEx;
import com.huawei.zxing.FormatException;

public final class Version {
    private static final Version[] VERSIONS = buildVersions();
    private final int dataRegionSizeColumns;
    private final int dataRegionSizeRows;
    private final ECBlocks ecBlocks;
    private final int symbolSizeColumns;
    private final int symbolSizeRows;
    private final int totalCodewords;
    private final int versionNumber;

    static final class ECB {
        private final int count;
        private final int dataCodewords;

        /* synthetic */ ECB(int count, int dataCodewords, ECB -this2) {
            this(count, dataCodewords);
        }

        private ECB(int count, int dataCodewords) {
            this.count = count;
            this.dataCodewords = dataCodewords;
        }

        int getCount() {
            return this.count;
        }

        int getDataCodewords() {
            return this.dataCodewords;
        }
    }

    static final class ECBlocks {
        private final ECB[] ecBlocks;
        private final int ecCodewords;

        /* synthetic */ ECBlocks(int ecCodewords, ECB ecBlocks1, ECB ecBlocks2, ECBlocks -this3) {
            this(ecCodewords, ecBlocks1, ecBlocks2);
        }

        private ECBlocks(int ecCodewords, ECB ecBlocks) {
            this.ecCodewords = ecCodewords;
            this.ecBlocks = new ECB[]{ecBlocks};
        }

        private ECBlocks(int ecCodewords, ECB ecBlocks1, ECB ecBlocks2) {
            this.ecCodewords = ecCodewords;
            this.ecBlocks = new ECB[]{ecBlocks1, ecBlocks2};
        }

        int getECCodewords() {
            return this.ecCodewords;
        }

        ECB[] getECBlocks() {
            return this.ecBlocks;
        }
    }

    private Version(int versionNumber, int symbolSizeRows, int symbolSizeColumns, int dataRegionSizeRows, int dataRegionSizeColumns, ECBlocks ecBlocks) {
        this.versionNumber = versionNumber;
        this.symbolSizeRows = symbolSizeRows;
        this.symbolSizeColumns = symbolSizeColumns;
        this.dataRegionSizeRows = dataRegionSizeRows;
        this.dataRegionSizeColumns = dataRegionSizeColumns;
        this.ecBlocks = ecBlocks;
        int total = 0;
        int ecCodewords = ecBlocks.getECCodewords();
        for (ECB ecBlock : ecBlocks.getECBlocks()) {
            total += ecBlock.getCount() * (ecBlock.getDataCodewords() + ecCodewords);
        }
        this.totalCodewords = total;
    }

    public int getVersionNumber() {
        return this.versionNumber;
    }

    public int getSymbolSizeRows() {
        return this.symbolSizeRows;
    }

    public int getSymbolSizeColumns() {
        return this.symbolSizeColumns;
    }

    public int getDataRegionSizeRows() {
        return this.dataRegionSizeRows;
    }

    public int getDataRegionSizeColumns() {
        return this.dataRegionSizeColumns;
    }

    public int getTotalCodewords() {
        return this.totalCodewords;
    }

    ECBlocks getECBlocks() {
        return this.ecBlocks;
    }

    public static Version getVersionForDimensions(int numRows, int numColumns) throws FormatException {
        if ((numRows & 1) == 0 && (numColumns & 1) == 0) {
            for (Version version : VERSIONS) {
                if (version.symbolSizeRows == numRows && version.symbolSizeColumns == numColumns) {
                    return version;
                }
            }
            throw FormatException.getFormatInstance();
        }
        throw FormatException.getFormatInstance();
    }

    public String toString() {
        return String.valueOf(this.versionNumber);
    }

    private static Version[] buildVersions() {
        r9 = new Version[30];
        r9[2] = new Version(3, 14, 14, 12, 12, new ECBlocks(10, new ECB(1, 8, null), null));
        r9[3] = new Version(4, 16, 16, 14, 14, new ECBlocks(12, new ECB(1, 12, null), null));
        r9[4] = new Version(5, 18, 18, 16, 16, new ECBlocks(14, new ECB(1, 18, null), null));
        r9[5] = new Version(6, 20, 20, 18, 18, new ECBlocks(18, new ECB(1, 22, null), null));
        r9[6] = new Version(7, 22, 22, 20, 20, new ECBlocks(20, new ECB(1, 30, null), null));
        r9[7] = new Version(8, 24, 24, 22, 22, new ECBlocks(24, new ECB(1, 36, null), null));
        r9[8] = new Version(9, 26, 26, 24, 24, new ECBlocks(28, new ECB(1, 44, null), null));
        r9[9] = new Version(10, 32, 32, 14, 14, new ECBlocks(36, new ECB(1, 62, null), null));
        r9[10] = new Version(11, 36, 36, 16, 16, new ECBlocks(42, new ECB(1, 86, null), null));
        r9[11] = new Version(12, 40, 40, 18, 18, new ECBlocks(48, new ECB(1, 114, null), null));
        r9[12] = new Version(13, 44, 44, 20, 20, new ECBlocks(56, new ECB(1, 144, null), null));
        r9[13] = new Version(14, 48, 48, 22, 22, new ECBlocks(68, new ECB(1, 174, null), null));
        r9[14] = new Version(15, 52, 52, 24, 24, new ECBlocks(42, new ECB(2, 102, null), null));
        r9[15] = new Version(16, 64, 64, 14, 14, new ECBlocks(56, new ECB(2, 140, null), null));
        r9[16] = new Version(17, 72, 72, 16, 16, new ECBlocks(36, new ECB(4, 92, null), null));
        r9[17] = new Version(18, 80, 80, 18, 18, new ECBlocks(48, new ECB(4, 114, null), null));
        r9[18] = new Version(19, 88, 88, 20, 20, new ECBlocks(56, new ECB(4, 144, null), null));
        r9[19] = new Version(20, 96, 96, 22, 22, new ECBlocks(68, new ECB(4, 174, null), null));
        r9[20] = new Version(21, 104, 104, 24, 24, new ECBlocks(56, new ECB(6, 136, null), null));
        r9[21] = new Version(22, JlogConstantsEx.JLID_DIALPAD_ONTOUCH_NOT_FIRST_DOWN, JlogConstantsEx.JLID_DIALPAD_ONTOUCH_NOT_FIRST_DOWN, 18, 18, new ECBlocks(68, new ECB(6, 175, null), null));
        r9[22] = new Version(23, JlogConstantsEx.JLID_EDIT_CONTACT_END, JlogConstantsEx.JLID_EDIT_CONTACT_END, 20, 20, new ECBlocks(62, new ECB(8, 163, null), null));
        r9[23] = new Version(24, 144, 144, 22, 22, new ECBlocks(62, new ECB(8, 156, null), new ECB(2, 155, null), null));
        r9[24] = new Version(25, 8, 18, 6, 16, new ECBlocks(7, new ECB(1, 5, null), null));
        r9[25] = new Version(26, 8, 32, 6, 14, new ECBlocks(11, new ECB(1, 10, null), null));
        r9[26] = new Version(27, 12, 26, 10, 24, new ECBlocks(14, new ECB(1, 16, null), null));
        r9[27] = new Version(28, 12, 36, 10, 16, new ECBlocks(18, new ECB(1, 22, null), null));
        r9[28] = new Version(29, 16, 36, 14, 16, new ECBlocks(24, new ECB(1, 32, null), null));
        r9[29] = new Version(30, 16, 48, 14, 22, new ECBlocks(28, new ECB(1, 49, null), null));
        return r9;
    }
}
