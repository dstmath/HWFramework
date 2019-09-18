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

        private ECB(int count2, int dataCodewords2) {
            this.count = count2;
            this.dataCodewords = dataCodewords2;
        }

        /* access modifiers changed from: package-private */
        public int getCount() {
            return this.count;
        }

        /* access modifiers changed from: package-private */
        public int getDataCodewords() {
            return this.dataCodewords;
        }
    }

    static final class ECBlocks {
        private final ECB[] ecBlocks;
        private final int ecCodewords;

        private ECBlocks(int ecCodewords2, ECB ecBlocks2) {
            this.ecCodewords = ecCodewords2;
            this.ecBlocks = new ECB[]{ecBlocks2};
        }

        private ECBlocks(int ecCodewords2, ECB ecBlocks1, ECB ecBlocks2) {
            this.ecCodewords = ecCodewords2;
            this.ecBlocks = new ECB[]{ecBlocks1, ecBlocks2};
        }

        /* access modifiers changed from: package-private */
        public int getECCodewords() {
            return this.ecCodewords;
        }

        /* access modifiers changed from: package-private */
        public ECB[] getECBlocks() {
            return this.ecBlocks;
        }
    }

    private Version(int versionNumber2, int symbolSizeRows2, int symbolSizeColumns2, int dataRegionSizeRows2, int dataRegionSizeColumns2, ECBlocks ecBlocks2) {
        this.versionNumber = versionNumber2;
        this.symbolSizeRows = symbolSizeRows2;
        this.symbolSizeColumns = symbolSizeColumns2;
        this.dataRegionSizeRows = dataRegionSizeRows2;
        this.dataRegionSizeColumns = dataRegionSizeColumns2;
        this.ecBlocks = ecBlocks2;
        int total = 0;
        int ecCodewords = ecBlocks2.getECCodewords();
        for (ECB ecBlock : ecBlocks2.getECBlocks()) {
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

    /* access modifiers changed from: package-private */
    public ECBlocks getECBlocks() {
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
        Version version = new Version(1, 10, 10, 8, 8, new ECBlocks(5, new ECB(1, 3)));
        Version version2 = new Version(2, 12, 12, 10, 10, new ECBlocks(7, new ECB(1, 5)));
        Version version3 = new Version(3, 14, 14, 12, 12, new ECBlocks(10, new ECB(1, 8)));
        Version version4 = new Version(4, 16, 16, 14, 14, new ECBlocks(12, new ECB(1, 12)));
        Version version5 = new Version(5, 18, 18, 16, 16, new ECBlocks(14, new ECB(1, 18)));
        Version version6 = new Version(6, 20, 20, 18, 18, new ECBlocks(18, new ECB(1, 22)));
        Version version7 = new Version(7, 22, 22, 20, 20, new ECBlocks(20, new ECB(1, 30)));
        Version version8 = new Version(8, 24, 24, 22, 22, new ECBlocks(24, new ECB(1, 36)));
        Version version9 = new Version(9, 26, 26, 24, 24, new ECBlocks(28, new ECB(1, 44)));
        Version version10 = new Version(10, 32, 32, 14, 14, new ECBlocks(36, new ECB(1, 62)));
        Version version11 = new Version(11, 36, 36, 16, 16, new ECBlocks(42, new ECB(1, 86)));
        Version version12 = new Version(12, 40, 40, 18, 18, new ECBlocks(48, new ECB(1, 114)));
        Version version13 = new Version(13, 44, 44, 20, 20, new ECBlocks(56, new ECB(1, 144)));
        Version version14 = new Version(14, 48, 48, 22, 22, new ECBlocks(68, new ECB(1, 174)));
        Version version15 = new Version(15, 52, 52, 24, 24, new ECBlocks(42, new ECB(2, 102)));
        Version version16 = new Version(16, 64, 64, 14, 14, new ECBlocks(56, new ECB(2, 140)));
        Version version17 = new Version(17, 72, 72, 16, 16, new ECBlocks(36, new ECB(4, 92)));
        Version version18 = new Version(18, 80, 80, 18, 18, new ECBlocks(48, new ECB(4, 114)));
        Version version19 = new Version(19, 88, 88, 20, 20, new ECBlocks(56, new ECB(4, 144)));
        Version version20 = new Version(20, 96, 96, 22, 22, new ECBlocks(68, new ECB(4, 174)));
        Version version21 = new Version(21, 104, 104, 24, 24, new ECBlocks(56, new ECB(6, 136)));
        Version version22 = new Version(22, JlogConstantsEx.JLID_DIALPAD_ONTOUCH_NOT_FIRST_DOWN, JlogConstantsEx.JLID_DIALPAD_ONTOUCH_NOT_FIRST_DOWN, 18, 18, new ECBlocks(68, new ECB(6, 175)));
        Version version23 = new Version(23, JlogConstantsEx.JLID_EDIT_CONTACT_END, JlogConstantsEx.JLID_EDIT_CONTACT_END, 20, 20, new ECBlocks(62, new ECB(8, 163)));
        Version version24 = new Version(24, 144, 144, 22, 22, new ECBlocks(62, new ECB(8, 156), new ECB(2, 155)));
        Version version25 = new Version(25, 8, 18, 6, 16, new ECBlocks(7, new ECB(1, 5)));
        Version version26 = new Version(26, 8, 32, 6, 14, new ECBlocks(11, new ECB(1, 10)));
        Version version27 = new Version(27, 12, 26, 10, 24, new ECBlocks(14, new ECB(1, 16)));
        Version version28 = new Version(28, 12, 36, 10, 16, new ECBlocks(18, new ECB(1, 22)));
        Version version29 = new Version(29, 16, 36, 14, 16, new ECBlocks(24, new ECB(1, 32)));
        Version version30 = new Version(30, 16, 48, 14, 22, new ECBlocks(28, new ECB(1, 49)));
        return new Version[]{version, version2, version3, version4, version5, version6, version7, version8, version9, version10, version11, version12, version13, version14, version15, version16, version17, version18, version19, version20, version21, version22, version23, version24, version25, version26, version27, version28, version29, version30};
    }
}
