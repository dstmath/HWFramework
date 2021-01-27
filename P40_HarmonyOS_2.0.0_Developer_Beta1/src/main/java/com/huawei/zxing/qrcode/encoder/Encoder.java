package com.huawei.zxing.qrcode.encoder;

import com.huawei.android.telephony.SignalStrengthEx;
import com.huawei.zxing.EncodeHintType;
import com.huawei.zxing.WriterException;
import com.huawei.zxing.common.BitArray;
import com.huawei.zxing.common.CharacterSetECI;
import com.huawei.zxing.common.reedsolomon.GenericGF;
import com.huawei.zxing.common.reedsolomon.ReedSolomonEncoder;
import com.huawei.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.huawei.zxing.qrcode.decoder.Mode;
import com.huawei.zxing.qrcode.decoder.Version;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public final class Encoder {
    private static final int[] ALPHANUMERIC_TABLE = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 36, -1, -1, -1, 37, 38, -1, -1, -1, -1, 39, 40, -1, 41, 42, 43, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 44, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, -1, -1, -1, -1, -1};
    static final String DEFAULT_BYTE_MODE_ENCODING = "UTF-8";

    private Encoder() {
    }

    private static int calculateMaskPenalty(ByteMatrix matrix) {
        return MaskUtil.applyMaskPenaltyRule1(matrix) + MaskUtil.applyMaskPenaltyRule2(matrix) + MaskUtil.applyMaskPenaltyRule3(matrix) + MaskUtil.applyMaskPenaltyRule4(matrix);
    }

    public static QRCode encode(String content, ErrorCorrectionLevel ecLevel) throws WriterException {
        return encode(content, ecLevel, null);
    }

    public static QRCode encode(String content, ErrorCorrectionLevel ecLevel, Map<EncodeHintType, ?> hints) throws WriterException {
        CharacterSetECI eci;
        String encoding = hints == null ? null : (String) hints.get(EncodeHintType.CHARACTER_SET);
        if (encoding == null) {
            encoding = DEFAULT_BYTE_MODE_ENCODING;
        }
        Mode mode = chooseMode(content, encoding);
        BitArray headerBits = new BitArray();
        if (mode == Mode.BYTE && !DEFAULT_BYTE_MODE_ENCODING.equals(encoding) && (eci = CharacterSetECI.getCharacterSetECIByName(encoding)) != null) {
            appendECI(eci, headerBits);
        }
        appendModeInfo(mode, headerBits);
        BitArray dataBits = new BitArray();
        appendBytes(content, mode, dataBits, encoding);
        Version version = chooseVersion(headerBits.getSize() + mode.getCharacterCountBits(chooseVersion(headerBits.getSize() + mode.getCharacterCountBits(Version.getVersionForNumber(1)) + dataBits.getSize(), ecLevel)) + dataBits.getSize(), ecLevel);
        BitArray headerAndDataBits = new BitArray();
        headerAndDataBits.appendBitArray(headerBits);
        appendLengthInfo(mode == Mode.BYTE ? dataBits.getSizeInBytes() : content.length(), version, mode, headerAndDataBits);
        headerAndDataBits.appendBitArray(dataBits);
        Version.ECBlocks ecBlocks = version.getECBlocksForLevel(ecLevel);
        int numDataBytes = version.getTotalCodewords() - ecBlocks.getTotalECCodewords();
        terminateBits(numDataBytes, headerAndDataBits);
        BitArray finalBits = interleaveWithECBytes(headerAndDataBits, version.getTotalCodewords(), numDataBytes, ecBlocks.getNumBlocks());
        QRCode qrCode = new QRCode();
        qrCode.setECLevel(ecLevel);
        qrCode.setMode(mode);
        qrCode.setVersion(version);
        int dimension = version.getDimensionForVersion();
        ByteMatrix matrix = new ByteMatrix(dimension, dimension);
        int maskPattern = chooseMaskPattern(finalBits, ecLevel, version, matrix);
        qrCode.setMaskPattern(maskPattern);
        MatrixUtil.buildMatrix(finalBits, ecLevel, version, maskPattern, matrix);
        qrCode.setMatrix(matrix);
        return qrCode;
    }

    static int getAlphanumericCode(int code) {
        int[] iArr = ALPHANUMERIC_TABLE;
        if (code < iArr.length) {
            return iArr[code];
        }
        return -1;
    }

    public static Mode chooseMode(String content) {
        return chooseMode(content, null);
    }

    private static Mode chooseMode(String content, String encoding) {
        if ("Shift_JIS".equals(encoding)) {
            return isOnlyDoubleByteKanji(content) ? Mode.KANJI : Mode.BYTE;
        }
        boolean hasNumeric = false;
        boolean hasAlphanumeric = false;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c >= '0' && c <= '9') {
                hasNumeric = true;
            } else if (getAlphanumericCode(c) == -1) {
                return Mode.BYTE;
            } else {
                hasAlphanumeric = true;
            }
        }
        if (hasAlphanumeric) {
            return Mode.ALPHANUMERIC;
        }
        if (hasNumeric) {
            return Mode.NUMERIC;
        }
        return Mode.BYTE;
    }

    private static boolean isOnlyDoubleByteKanji(String content) {
        try {
            byte[] bytes = content.getBytes("Shift_JIS");
            int length = bytes.length;
            if (length % 2 != 0) {
                return false;
            }
            for (int i = 0; i < length; i += 2) {
                int byte1 = bytes[i] & 255;
                if ((byte1 < 129 || byte1 > 159) && (byte1 < 224 || byte1 > 235)) {
                    return false;
                }
            }
            return true;
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }

    private static int chooseMaskPattern(BitArray bits, ErrorCorrectionLevel ecLevel, Version version, ByteMatrix matrix) throws WriterException {
        int minPenalty = SignalStrengthEx.INVALID;
        int bestMaskPattern = -1;
        for (int maskPattern = 0; maskPattern < 8; maskPattern++) {
            MatrixUtil.buildMatrix(bits, ecLevel, version, maskPattern, matrix);
            int penalty = calculateMaskPenalty(matrix);
            if (penalty < minPenalty) {
                minPenalty = penalty;
                bestMaskPattern = maskPattern;
            }
        }
        return bestMaskPattern;
    }

    private static Version chooseVersion(int numInputBits, ErrorCorrectionLevel ecLevel) throws WriterException {
        for (int versionNum = 1; versionNum <= 40; versionNum++) {
            Version version = Version.getVersionForNumber(versionNum);
            if (version.getTotalCodewords() - version.getECBlocksForLevel(ecLevel).getTotalECCodewords() >= (numInputBits + 7) / 8) {
                return version;
            }
        }
        throw new WriterException("Data too big");
    }

    static void terminateBits(int numDataBytes, BitArray bits) throws WriterException {
        int capacity = numDataBytes << 3;
        if (bits.getSize() <= capacity) {
            for (int i = 0; i < 4 && bits.getSize() < capacity; i++) {
                bits.appendBit(false);
            }
            int numBitsInLastByte = bits.getSize() & 7;
            if (numBitsInLastByte > 0) {
                for (int i2 = numBitsInLastByte; i2 < 8; i2++) {
                    bits.appendBit(false);
                }
            }
            int numPaddingBytes = numDataBytes - bits.getSizeInBytes();
            for (int i3 = 0; i3 < numPaddingBytes; i3++) {
                bits.appendBits((i3 & 1) == 0 ? 236 : 17, 8);
            }
            if (bits.getSize() != capacity) {
                throw new WriterException("Bits size does not equal capacity");
            }
            return;
        }
        throw new WriterException("data bits cannot fit in the QR Code" + bits.getSize() + " > " + capacity);
    }

    static void getNumDataBytesAndNumECBytesForBlockID(int numTotalBytes, int numDataBytes, int numRSBlocks, int blockID, int[] numDataBytesInBlock, int[] numECBytesInBlock) throws WriterException {
        if (blockID < numRSBlocks) {
            int numRsBlocksInGroup2 = numTotalBytes % numRSBlocks;
            int numRsBlocksInGroup1 = numRSBlocks - numRsBlocksInGroup2;
            int numTotalBytesInGroup1 = numTotalBytes / numRSBlocks;
            int numDataBytesInGroup1 = numDataBytes / numRSBlocks;
            int numDataBytesInGroup2 = numDataBytesInGroup1 + 1;
            int numEcBytesInGroup1 = numTotalBytesInGroup1 - numDataBytesInGroup1;
            int numEcBytesInGroup2 = (numTotalBytesInGroup1 + 1) - numDataBytesInGroup2;
            if (numEcBytesInGroup1 != numEcBytesInGroup2) {
                throw new WriterException("EC bytes mismatch");
            } else if (numRSBlocks != numRsBlocksInGroup1 + numRsBlocksInGroup2) {
                throw new WriterException("RS blocks mismatch");
            } else if (numTotalBytes != ((numDataBytesInGroup1 + numEcBytesInGroup1) * numRsBlocksInGroup1) + ((numDataBytesInGroup2 + numEcBytesInGroup2) * numRsBlocksInGroup2)) {
                throw new WriterException("Total bytes mismatch");
            } else if (blockID < numRsBlocksInGroup1) {
                numDataBytesInBlock[0] = numDataBytesInGroup1;
                numECBytesInBlock[0] = numEcBytesInGroup1;
            } else {
                numDataBytesInBlock[0] = numDataBytesInGroup2;
                numECBytesInBlock[0] = numEcBytesInGroup2;
            }
        } else {
            throw new WriterException("Block ID too large");
        }
    }

    static BitArray interleaveWithECBytes(BitArray bits, int numTotalBytes, int numDataBytes, int numRSBlocks) throws WriterException {
        if (bits.getSizeInBytes() == numDataBytes) {
            Collection<BlockPair> blocks = new ArrayList<>(numRSBlocks);
            int dataBytesOffset = 0;
            int maxNumDataBytes = 0;
            int maxNumEcBytes = 0;
            for (int i = 0; i < numRSBlocks; i++) {
                int[] numDataBytesInBlock = new int[1];
                int[] numEcBytesInBlock = new int[1];
                getNumDataBytesAndNumECBytesForBlockID(numTotalBytes, numDataBytes, numRSBlocks, i, numDataBytesInBlock, numEcBytesInBlock);
                int size = numDataBytesInBlock[0];
                byte[] dataBytes = new byte[size];
                bits.toBytes(dataBytesOffset * 8, dataBytes, 0, size);
                byte[] ecBytes = generateECBytes(dataBytes, numEcBytesInBlock[0]);
                blocks.add(new BlockPair(dataBytes, ecBytes));
                maxNumDataBytes = Math.max(maxNumDataBytes, size);
                maxNumEcBytes = Math.max(maxNumEcBytes, ecBytes.length);
                dataBytesOffset += numDataBytesInBlock[0];
            }
            if (numDataBytes == dataBytesOffset) {
                BitArray result = new BitArray();
                for (int i2 = 0; i2 < maxNumDataBytes; i2++) {
                    for (BlockPair block : blocks) {
                        byte[] dataBytes2 = block.getDataBytes();
                        if (i2 < dataBytes2.length) {
                            result.appendBits(dataBytes2[i2], 8);
                        }
                    }
                }
                for (int i3 = 0; i3 < maxNumEcBytes; i3++) {
                    for (BlockPair block2 : blocks) {
                        byte[] ecBytes2 = block2.getErrorCorrectionBytes();
                        if (i3 < ecBytes2.length) {
                            result.appendBits(ecBytes2[i3], 8);
                        }
                    }
                }
                if (numTotalBytes == result.getSizeInBytes()) {
                    return result;
                }
                throw new WriterException("Interleaving error: " + numTotalBytes + " and " + result.getSizeInBytes() + " differ.");
            }
            throw new WriterException("Data bytes does not match offset");
        }
        throw new WriterException("Number of bits and data bytes does not match");
    }

    static byte[] generateECBytes(byte[] dataBytes, int numEcBytesInBlock) {
        int numDataBytes = dataBytes.length;
        int[] toEncode = new int[(numDataBytes + numEcBytesInBlock)];
        for (int i = 0; i < numDataBytes; i++) {
            toEncode[i] = dataBytes[i] & 255;
        }
        new ReedSolomonEncoder(GenericGF.QR_CODE_FIELD_256).encode(toEncode, numEcBytesInBlock);
        byte[] ecBytes = new byte[numEcBytesInBlock];
        for (int i2 = 0; i2 < numEcBytesInBlock; i2++) {
            ecBytes[i2] = (byte) toEncode[numDataBytes + i2];
        }
        return ecBytes;
    }

    static void appendModeInfo(Mode mode, BitArray bits) {
        bits.appendBits(mode.getBits(), 4);
    }

    static void appendLengthInfo(int numLetters, Version version, Mode mode, BitArray bits) throws WriterException {
        int numBits = mode.getCharacterCountBits(version);
        if (numLetters < (1 << numBits)) {
            bits.appendBits(numLetters, numBits);
            return;
        }
        throw new WriterException(numLetters + " is bigger than " + ((1 << numBits) - 1));
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.huawei.zxing.qrcode.encoder.Encoder$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$huawei$zxing$qrcode$decoder$Mode = new int[Mode.values().length];

        static {
            try {
                $SwitchMap$com$huawei$zxing$qrcode$decoder$Mode[Mode.NUMERIC.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$huawei$zxing$qrcode$decoder$Mode[Mode.ALPHANUMERIC.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$huawei$zxing$qrcode$decoder$Mode[Mode.BYTE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$huawei$zxing$qrcode$decoder$Mode[Mode.KANJI.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    static void appendBytes(String content, Mode mode, BitArray bits, String encoding) throws WriterException {
        int i = AnonymousClass1.$SwitchMap$com$huawei$zxing$qrcode$decoder$Mode[mode.ordinal()];
        if (i == 1) {
            appendNumericBytes(content, bits);
        } else if (i == 2) {
            appendAlphanumericBytes(content, bits);
        } else if (i == 3) {
            append8BitBytes(content, bits, encoding);
        } else if (i == 4) {
            appendKanjiBytes(content, bits);
        } else {
            throw new WriterException("Invalid mode: " + mode);
        }
    }

    static void appendNumericBytes(CharSequence content, BitArray bits) {
        int length = content.length();
        int i = 0;
        while (i < length) {
            int num1 = content.charAt(i) - '0';
            if (i + 2 < length) {
                bits.appendBits((num1 * 100) + ((content.charAt(i + 1) - '0') * 10) + (content.charAt(i + 2) - '0'), 10);
                i += 3;
            } else if (i + 1 < length) {
                bits.appendBits((num1 * 10) + (content.charAt(i + 1) - '0'), 7);
                i += 2;
            } else {
                bits.appendBits(num1, 4);
                i++;
            }
        }
    }

    static void appendAlphanumericBytes(CharSequence content, BitArray bits) throws WriterException {
        int length = content.length();
        int i = 0;
        while (i < length) {
            int code1 = getAlphanumericCode(content.charAt(i));
            if (code1 == -1) {
                throw new WriterException();
            } else if (i + 1 < length) {
                int code2 = getAlphanumericCode(content.charAt(i + 1));
                if (code2 != -1) {
                    bits.appendBits((code1 * 45) + code2, 11);
                    i += 2;
                } else {
                    throw new WriterException();
                }
            } else {
                bits.appendBits(code1, 6);
                i++;
            }
        }
    }

    static void append8BitBytes(String content, BitArray bits, String encoding) throws WriterException {
        try {
            for (byte b : content.getBytes(encoding)) {
                bits.appendBits(b, 8);
            }
        } catch (UnsupportedEncodingException uee) {
            throw new WriterException(uee);
        }
    }

    static void appendKanjiBytes(String content, BitArray bits) throws WriterException {
        try {
            byte[] bytes = content.getBytes("Shift_JIS");
            int length = bytes.length;
            for (int i = 0; i < length; i += 2) {
                int code = ((bytes[i] & 255) << 8) | (bytes[i + 1] & 255);
                int subtracted = -1;
                if (code >= 33088 && code <= 40956) {
                    subtracted = code - 33088;
                } else if (code >= 57408 && code <= 60351) {
                    subtracted = code - 49472;
                }
                if (subtracted != -1) {
                    bits.appendBits(((subtracted >> 8) * 192) + (subtracted & 255), 13);
                } else {
                    throw new WriterException("Invalid byte sequence");
                }
            }
        } catch (UnsupportedEncodingException uee) {
            throw new WriterException(uee);
        }
    }

    private static void appendECI(CharacterSetECI eci, BitArray bits) {
        bits.appendBits(Mode.ECI.getBits(), 4);
        bits.appendBits(eci.getValue(), 8);
    }
}
