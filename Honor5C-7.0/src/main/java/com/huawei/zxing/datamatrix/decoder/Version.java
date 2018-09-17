package com.huawei.zxing.datamatrix.decoder;

import com.huawei.lcagent.client.MetricConstant;
import com.huawei.motiondetection.MotionTypeApps;
import com.huawei.zxing.FormatException;

public final class Version {
    private static final Version[] VERSIONS = null;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.datamatrix.decoder.Version.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.datamatrix.decoder.Version.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.datamatrix.decoder.Version.<clinit>():void");
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
        r9[2] = new Version(3, 14, 14, 12, 12, new ECBlocks(new ECB(8, null), null));
        r9[3] = new Version(4, 16, 16, 14, 14, new ECBlocks(new ECB(12, null), null));
        r9[4] = new Version(5, 18, 18, 16, 16, new ECBlocks(new ECB(18, null), null));
        r9[5] = new Version(6, 20, 20, 18, 18, new ECBlocks(new ECB(22, null), null));
        r9[6] = new Version(7, 22, 22, 20, 20, new ECBlocks(new ECB(30, null), null));
        r9[7] = new Version(8, 24, 24, 22, 22, new ECBlocks(new ECB(36, null), null));
        r9[8] = new Version(9, 26, 26, 24, 24, new ECBlocks(new ECB(44, null), null));
        r9[9] = new Version(10, 32, 32, 14, 14, new ECBlocks(new ECB(62, null), null));
        r9[10] = new Version(11, 36, 36, 16, 16, new ECBlocks(new ECB(86, null), null));
        r9[11] = new Version(12, 40, 40, 18, 18, new ECBlocks(new ECB(114, null), null));
        r9[12] = new Version(13, 44, 44, 20, 20, new ECBlocks(new ECB(144, null), null));
        r9[13] = new Version(14, 48, 48, 22, 22, new ECBlocks(new ECB(174, null), null));
        r9[14] = new Version(15, 52, 52, 24, 24, new ECBlocks(new ECB(MotionTypeApps.TYPE_PICKUP_REDUCE_CLOCK, null), null));
        r9[15] = new Version(16, 64, 64, 14, 14, new ECBlocks(new ECB(140, null), null));
        r9[16] = new Version(17, 72, 72, 16, 16, new ECBlocks(new ECB(92, null), null));
        r9[17] = new Version(18, 80, 80, 18, 18, new ECBlocks(new ECB(114, null), null));
        r9[18] = new Version(19, 88, 88, 20, 20, new ECBlocks(new ECB(144, null), null));
        r9[19] = new Version(20, 96, 96, 22, 22, new ECBlocks(new ECB(174, null), null));
        r9[20] = new Version(21, MetricConstant.CAMERA_METRIC_ID_EX, MetricConstant.CAMERA_METRIC_ID_EX, 24, 24, new ECBlocks(new ECB(136, null), null));
        r9[21] = new Version(22, 120, 120, 18, 18, new ECBlocks(new ECB(175, null), null));
        r9[22] = new Version(23, 132, 132, 20, 20, new ECBlocks(new ECB(163, null), null));
        r9[23] = new Version(24, 144, 144, 22, 22, new ECBlocks(new ECB(156, null), new ECB(155, null), null));
        r9[24] = new Version(25, 8, 18, 6, 16, new ECBlocks(new ECB(5, null), null));
        r9[25] = new Version(26, 8, 32, 6, 14, new ECBlocks(new ECB(10, null), null));
        r9[26] = new Version(27, 12, 26, 10, 24, new ECBlocks(new ECB(16, null), null));
        r9[27] = new Version(28, 12, 36, 10, 16, new ECBlocks(new ECB(22, null), null));
        r9[28] = new Version(29, 16, 36, 14, 16, new ECBlocks(new ECB(32, null), null));
        r9[29] = new Version(30, 16, 48, 14, 22, new ECBlocks(new ECB(49, null), null));
        return r9;
    }
}
