package com.huawei.zxing.qrcode.decoder;

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
    private final ReedSolomonDecoder rsDecoder = new ReedSolomonDecoder(GenericGF.QR_CODE_FIELD_256);

    public DecoderResult decode(boolean[][] image) throws ChecksumException, FormatException {
        return decode(image, null);
    }

    public DecoderResult decode(boolean[][] image, Map<DecodeHintType, ?> hints) throws ChecksumException, FormatException {
        int dimension = image.length;
        BitMatrix bits = new BitMatrix(dimension);
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                if (image[i][j]) {
                    bits.set(j, i);
                }
            }
        }
        return decode(bits, (Map) hints);
    }

    public DecoderResult decode(BitMatrix bits) throws ChecksumException, FormatException {
        return decode(bits, null);
    }

    public DecoderResult decode(BitMatrix bits, Map<DecodeHintType, ?> hints) throws FormatException, ChecksumException {
        BitMatrixParser parser = new BitMatrixParser(bits);
        FormatException fe = null;
        ChecksumException ce = null;
        try {
            return decode(parser, (Map) hints);
        } catch (FormatException e) {
            fe = e;
        } catch (ChecksumException e2) {
            ce = e2;
        }
        try {
            parser.remask();
            parser.setMirror(true);
            parser.readVersion();
            parser.readFormatInformation();
            parser.mirror();
            DecoderResult result = decode(parser, (Map) hints);
            result.setOther(new QRCodeDecoderMetaData(true));
            return result;
        } catch (FormatException e3) {
            if (fe != null) {
                throw fe;
            } else if (ce != null) {
                throw ce;
            } else {
                throw e3;
            }
        } catch (ChecksumException e22) {
            if (fe != null) {
                throw fe;
            } else if (ce != null) {
                throw ce;
            } else {
                throw e22;
            }
        }
    }

    private DecoderResult decode(BitMatrixParser parser, Map<DecodeHintType, ?> hints) throws FormatException, ChecksumException {
        DataBlock dataBlock;
        Version version = parser.readVersion();
        ErrorCorrectionLevel ecLevel = parser.readFormatInformation().getErrorCorrectionLevel();
        DataBlock[] dataBlocks = DataBlock.getDataBlocks(parser.readCodewords(), version, ecLevel);
        int totalBytes = 0;
        for (DataBlock dataBlock2 : dataBlocks) {
            totalBytes += dataBlock2.getNumDataCodewords();
        }
        byte[] resultBytes = new byte[totalBytes];
        int resultOffset = 0;
        int i = 0;
        int length = dataBlocks.length;
        while (i < length) {
            dataBlock2 = dataBlocks[i];
            byte[] codewordBytes = dataBlock2.getCodewords();
            int numDataCodewords = dataBlock2.getNumDataCodewords();
            correctErrors(codewordBytes, numDataCodewords);
            int i2 = 0;
            int resultOffset2 = resultOffset;
            while (i2 < numDataCodewords) {
                resultOffset = resultOffset2 + 1;
                resultBytes[resultOffset2] = codewordBytes[i2];
                i2++;
                resultOffset2 = resultOffset;
            }
            i++;
            resultOffset = resultOffset2;
        }
        return DecodedBitStreamParser.decode(resultBytes, version, ecLevel, hints);
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
