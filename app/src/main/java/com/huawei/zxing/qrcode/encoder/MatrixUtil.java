package com.huawei.zxing.qrcode.encoder;

import com.huawei.zxing.WriterException;
import com.huawei.zxing.common.BitArray;
import com.huawei.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.huawei.zxing.qrcode.decoder.Version;

final class MatrixUtil {
    private static final int[][] POSITION_ADJUSTMENT_PATTERN = null;
    private static final int[][] POSITION_ADJUSTMENT_PATTERN_COORDINATE_TABLE = null;
    private static final int[][] POSITION_DETECTION_PATTERN = null;
    private static final int[][] TYPE_INFO_COORDINATES = null;
    private static final int TYPE_INFO_MASK_PATTERN = 21522;
    private static final int TYPE_INFO_POLY = 1335;
    private static final int VERSION_INFO_POLY = 7973;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.qrcode.encoder.MatrixUtil.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.qrcode.encoder.MatrixUtil.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.qrcode.encoder.MatrixUtil.<clinit>():void");
    }

    private MatrixUtil() {
    }

    static void clearMatrix(ByteMatrix matrix) {
        matrix.clear((byte) -1);
    }

    static void buildMatrix(BitArray dataBits, ErrorCorrectionLevel ecLevel, Version version, int maskPattern, ByteMatrix matrix) throws WriterException {
        clearMatrix(matrix);
        embedBasicPatterns(version, matrix);
        embedTypeInfo(ecLevel, maskPattern, matrix);
        maybeEmbedVersionInfo(version, matrix);
        embedDataBits(dataBits, maskPattern, matrix);
    }

    static void embedBasicPatterns(Version version, ByteMatrix matrix) throws WriterException {
        embedPositionDetectionPatternsAndSeparators(matrix);
        embedDarkDotAtLeftBottomCorner(matrix);
        maybeEmbedPositionAdjustmentPatterns(version, matrix);
        embedTimingPatterns(matrix);
    }

    static void embedTypeInfo(ErrorCorrectionLevel ecLevel, int maskPattern, ByteMatrix matrix) throws WriterException {
        BitArray typeInfoBits = new BitArray();
        makeTypeInfoBits(ecLevel, maskPattern, typeInfoBits);
        for (int i = 0; i < typeInfoBits.getSize(); i++) {
            boolean bit = typeInfoBits.get((typeInfoBits.getSize() - 1) - i);
            matrix.set(TYPE_INFO_COORDINATES[i][0], TYPE_INFO_COORDINATES[i][1], bit);
            if (i < 8) {
                matrix.set((matrix.getWidth() - i) - 1, 8, bit);
            } else {
                matrix.set(8, (matrix.getHeight() - 7) + (i - 8), bit);
            }
        }
    }

    static void maybeEmbedVersionInfo(Version version, ByteMatrix matrix) throws WriterException {
        if (version.getVersionNumber() >= 7) {
            BitArray versionInfoBits = new BitArray();
            makeVersionInfoBits(version, versionInfoBits);
            int bitIndex = 17;
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 3; j++) {
                    boolean bit = versionInfoBits.get(bitIndex);
                    bitIndex--;
                    matrix.set(i, (matrix.getHeight() - 11) + j, bit);
                    matrix.set((matrix.getHeight() - 11) + j, i, bit);
                }
            }
        }
    }

    static void embedDataBits(BitArray dataBits, int maskPattern, ByteMatrix matrix) throws WriterException {
        int bitIndex = 0;
        int direction = -1;
        int x = matrix.getWidth() - 1;
        int y = matrix.getHeight() - 1;
        while (x > 0) {
            if (x == 6) {
                x--;
            }
            while (y >= 0 && y < matrix.getHeight()) {
                for (int i = 0; i < 2; i++) {
                    int xx = x - i;
                    if (isEmpty(matrix.get(xx, y))) {
                        boolean bit;
                        if (bitIndex < dataBits.getSize()) {
                            bit = dataBits.get(bitIndex);
                            bitIndex++;
                        } else {
                            bit = false;
                        }
                        if (maskPattern != -1 && MaskUtil.getDataMaskBit(maskPattern, xx, y)) {
                            bit = !bit;
                        }
                        matrix.set(xx, y, bit);
                    }
                }
                y += direction;
            }
            direction = -direction;
            y += direction;
            x -= 2;
        }
        if (bitIndex != dataBits.getSize()) {
            throw new WriterException("Not all bits consumed: " + bitIndex + '/' + dataBits.getSize());
        }
    }

    static int findMSBSet(int value) {
        int numDigits = 0;
        while (value != 0) {
            value >>>= 1;
            numDigits++;
        }
        return numDigits;
    }

    static int calculateBCHCode(int value, int poly) {
        int msbSetInPoly = findMSBSet(poly);
        value <<= msbSetInPoly - 1;
        while (findMSBSet(value) >= msbSetInPoly) {
            value ^= poly << (findMSBSet(value) - msbSetInPoly);
        }
        return value;
    }

    static void makeTypeInfoBits(ErrorCorrectionLevel ecLevel, int maskPattern, BitArray bits) throws WriterException {
        if (QRCode.isValidMaskPattern(maskPattern)) {
            int typeInfo = (ecLevel.getBits() << 3) | maskPattern;
            bits.appendBits(typeInfo, 5);
            bits.appendBits(calculateBCHCode(typeInfo, TYPE_INFO_POLY), 10);
            BitArray maskBits = new BitArray();
            maskBits.appendBits(TYPE_INFO_MASK_PATTERN, 15);
            bits.xor(maskBits);
            if (bits.getSize() != 15) {
                throw new WriterException("should not happen but we got: " + bits.getSize());
            }
            return;
        }
        throw new WriterException("Invalid mask pattern");
    }

    static void makeVersionInfoBits(Version version, BitArray bits) throws WriterException {
        bits.appendBits(version.getVersionNumber(), 6);
        bits.appendBits(calculateBCHCode(version.getVersionNumber(), VERSION_INFO_POLY), 12);
        if (bits.getSize() != 18) {
            throw new WriterException("should not happen but we got: " + bits.getSize());
        }
    }

    private static boolean isEmpty(int value) {
        return value == -1;
    }

    private static void embedTimingPatterns(ByteMatrix matrix) {
        for (int i = 8; i < matrix.getWidth() - 8; i++) {
            int bit = (i + 1) % 2;
            if (isEmpty(matrix.get(i, 6))) {
                matrix.set(i, 6, bit);
            }
            if (isEmpty(matrix.get(6, i))) {
                matrix.set(6, i, bit);
            }
        }
    }

    private static void embedDarkDotAtLeftBottomCorner(ByteMatrix matrix) throws WriterException {
        if (matrix.get(8, matrix.getHeight() - 8) == null) {
            throw new WriterException();
        }
        matrix.set(8, matrix.getHeight() - 8, 1);
    }

    private static void embedHorizontalSeparationPattern(int xStart, int yStart, ByteMatrix matrix) throws WriterException {
        int x = 0;
        while (x < 8) {
            if (isEmpty(matrix.get(xStart + x, yStart))) {
                matrix.set(xStart + x, yStart, 0);
                x++;
            } else {
                throw new WriterException();
            }
        }
    }

    private static void embedVerticalSeparationPattern(int xStart, int yStart, ByteMatrix matrix) throws WriterException {
        int y = 0;
        while (y < 7) {
            if (isEmpty(matrix.get(xStart, yStart + y))) {
                matrix.set(xStart, yStart + y, 0);
                y++;
            } else {
                throw new WriterException();
            }
        }
    }

    private static void embedPositionAdjustmentPattern(int xStart, int yStart, ByteMatrix matrix) {
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                matrix.set(xStart + x, yStart + y, POSITION_ADJUSTMENT_PATTERN[y][x]);
            }
        }
    }

    private static void embedPositionDetectionPattern(int xStart, int yStart, ByteMatrix matrix) {
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                matrix.set(xStart + x, yStart + y, POSITION_DETECTION_PATTERN[y][x]);
            }
        }
    }

    private static void embedPositionDetectionPatternsAndSeparators(ByteMatrix matrix) throws WriterException {
        int pdpWidth = POSITION_DETECTION_PATTERN[0].length;
        embedPositionDetectionPattern(0, 0, matrix);
        embedPositionDetectionPattern(matrix.getWidth() - pdpWidth, 0, matrix);
        embedPositionDetectionPattern(0, matrix.getWidth() - pdpWidth, matrix);
        embedHorizontalSeparationPattern(0, 7, matrix);
        embedHorizontalSeparationPattern(matrix.getWidth() - 8, 7, matrix);
        embedHorizontalSeparationPattern(0, matrix.getWidth() - 8, matrix);
        embedVerticalSeparationPattern(7, 0, matrix);
        embedVerticalSeparationPattern((matrix.getHeight() - 7) - 1, 0, matrix);
        embedVerticalSeparationPattern(7, matrix.getHeight() - 7, matrix);
    }

    private static void maybeEmbedPositionAdjustmentPatterns(Version version, ByteMatrix matrix) {
        if (version.getVersionNumber() >= 2) {
            int index = version.getVersionNumber() - 1;
            int[] coordinates = POSITION_ADJUSTMENT_PATTERN_COORDINATE_TABLE[index];
            int numCoordinates = POSITION_ADJUSTMENT_PATTERN_COORDINATE_TABLE[index].length;
            for (int i = 0; i < numCoordinates; i++) {
                for (int j = 0; j < numCoordinates; j++) {
                    int y = coordinates[i];
                    int x = coordinates[j];
                    if (!(x == -1 || y == -1 || !isEmpty(matrix.get(x, y)))) {
                        embedPositionAdjustmentPattern(x - 2, y - 2, matrix);
                    }
                }
            }
        }
    }
}
