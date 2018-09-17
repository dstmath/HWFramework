package com.huawei.zxing.datamatrix.decoder;

import com.huawei.zxing.ChecksumException;
import com.huawei.zxing.FormatException;
import com.huawei.zxing.common.BitMatrix;
import com.huawei.zxing.common.DecoderResult;
import com.huawei.zxing.common.reedsolomon.GenericGF;
import com.huawei.zxing.common.reedsolomon.ReedSolomonDecoder;
import com.huawei.zxing.common.reedsolomon.ReedSolomonException;

public final class Decoder {
    private final ReedSolomonDecoder rsDecoder = new ReedSolomonDecoder(GenericGF.DATA_MATRIX_FIELD_256);

    public DecoderResult decode(boolean[][] image) throws FormatException, ChecksumException {
        int dimension = image.length;
        BitMatrix bits = new BitMatrix(dimension);
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                if (image[i][j]) {
                    bits.set(j, i);
                }
            }
        }
        return decode(bits);
    }

    public DecoderResult decode(BitMatrix bits) throws FormatException, ChecksumException {
        BitMatrixParser parser = new BitMatrixParser(bits);
        DataBlock[] dataBlocks = DataBlock.getDataBlocks(parser.readCodewords(), parser.getVersion());
        int dataBlocksCount = dataBlocks.length;
        int totalBytes = 0;
        for (DataBlock db : dataBlocks) {
            totalBytes += db.getNumDataCodewords();
        }
        byte[] resultBytes = new byte[totalBytes];
        for (int j = 0; j < dataBlocksCount; j++) {
            DataBlock dataBlock = dataBlocks[j];
            byte[] codewordBytes = dataBlock.getCodewords();
            int numDataCodewords = dataBlock.getNumDataCodewords();
            correctErrors(codewordBytes, numDataCodewords);
            for (int i = 0; i < numDataCodewords; i++) {
                resultBytes[(i * dataBlocksCount) + j] = codewordBytes[i];
            }
        }
        return DecodedBitStreamParser.decode(resultBytes);
    }

    private void correctErrors(byte[] codewordBytes, int numDataCodewords) throws ChecksumException {
        int i;
        int numCodewords = codewordBytes.length;
        int[] codewordsInts = new int[numCodewords];
        for (i = 0; i < numCodewords; i++) {
            codewordsInts[i] = codewordBytes[i] & 255;
        }
        try {
            this.rsDecoder.decode(codewordsInts, codewordBytes.length - numDataCodewords);
            for (i = 0; i < numDataCodewords; i++) {
                codewordBytes[i] = (byte) codewordsInts[i];
            }
        } catch (ReedSolomonException e) {
            throw ChecksumException.getChecksumInstance();
        }
    }
}
