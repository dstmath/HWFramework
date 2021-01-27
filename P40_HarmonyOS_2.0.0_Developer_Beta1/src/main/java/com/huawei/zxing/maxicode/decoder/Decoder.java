package com.huawei.zxing.maxicode.decoder;

import com.huawei.zxing.ChecksumException;
import com.huawei.zxing.DecodeHintType;
import com.huawei.zxing.FormatException;
import com.huawei.zxing.common.BitMatrix;
import com.huawei.zxing.common.DecoderResult;
import com.huawei.zxing.common.reedsolomon.GenericGF;
import com.huawei.zxing.common.reedsolomon.ReedSolomonDecoder;
import com.huawei.zxing.common.reedsolomon.ReedSolomonException;
import java.util.Map;

public final class Decoder {
    private static final int ALL = 0;
    private static final int EVEN = 1;
    private static final int ODD = 2;
    private final ReedSolomonDecoder rsDecoder = new ReedSolomonDecoder(GenericGF.MAXICODE_FIELD_64);

    public DecoderResult decode(BitMatrix bits) throws ChecksumException, FormatException {
        return decode(bits, null);
    }

    public DecoderResult decode(BitMatrix bits, Map<DecodeHintType, ?> map) throws FormatException, ChecksumException {
        byte[] datawords;
        byte[] codewords = new BitMatrixParser(bits).readCodewords();
        correctErrors(codewords, 0, 10, 10, 0);
        int mode = codewords[0] & 15;
        if (mode == 2 || mode == 3 || mode == 4) {
            correctErrors(codewords, 20, 84, 40, 1);
            correctErrors(codewords, 20, 84, 40, 2);
            datawords = new byte[94];
        } else if (mode == 5) {
            correctErrors(codewords, 20, 68, 56, 1);
            correctErrors(codewords, 20, 68, 56, 2);
            datawords = new byte[78];
        } else {
            throw FormatException.getFormatInstance();
        }
        System.arraycopy(codewords, 0, datawords, 0, 10);
        System.arraycopy(codewords, 20, datawords, 10, datawords.length - 10);
        return DecodedBitStreamParser.decode(datawords, mode);
    }

    private void correctErrors(byte[] codewordBytes, int start, int dataCodewords, int ecCodewords, int mode) throws ChecksumException {
        int codewords = dataCodewords + ecCodewords;
        int divisor = mode == 0 ? 1 : 2;
        int[] codewordsInts = new int[(codewords / divisor)];
        for (int i = 0; i < codewords; i++) {
            if (mode == 0 || i % 2 == mode - 1) {
                codewordsInts[i / divisor] = codewordBytes[i + start] & 255;
            }
        }
        try {
            this.rsDecoder.decode(codewordsInts, ecCodewords / divisor);
            for (int i2 = 0; i2 < dataCodewords; i2++) {
                if (mode == 0 || i2 % 2 == mode - 1) {
                    codewordBytes[i2 + start] = (byte) codewordsInts[i2 / divisor];
                }
            }
        } catch (ReedSolomonException e) {
            throw ChecksumException.getChecksumInstance();
        }
    }
}
