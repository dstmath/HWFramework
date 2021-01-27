package com.huawei.zxing.qrcode;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.BinaryBitmap;
import com.huawei.zxing.ChecksumException;
import com.huawei.zxing.DecodeHintType;
import com.huawei.zxing.FormatException;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.Reader;
import com.huawei.zxing.Result;
import com.huawei.zxing.ResultMetadataType;
import com.huawei.zxing.ResultPoint;
import com.huawei.zxing.common.BitMatrix;
import com.huawei.zxing.common.DecoderResult;
import com.huawei.zxing.common.DetectorResult;
import com.huawei.zxing.qrcode.decoder.Decoder;
import com.huawei.zxing.qrcode.decoder.QRCodeDecoderMetaData;
import com.huawei.zxing.qrcode.detector.Detector;
import java.util.List;
import java.util.Map;

public class QRCodeReader implements Reader {
    private static final ResultPoint[] NO_POINTS = new ResultPoint[0];
    private final Decoder decoder = new Decoder();

    /* access modifiers changed from: protected */
    public final Decoder getDecoder() {
        return this.decoder;
    }

    @Override // com.huawei.zxing.Reader
    public Result decode(BinaryBitmap image) throws NotFoundException, ChecksumException, FormatException {
        return decode(image, null);
    }

    @Override // com.huawei.zxing.Reader
    public final Result decode(BinaryBitmap image, Map<DecodeHintType, ?> hints) throws NotFoundException, ChecksumException, FormatException {
        DecoderResult decoderResult;
        ResultPoint[] points;
        if (hints == null || !hints.containsKey(DecodeHintType.PURE_BARCODE)) {
            DetectorResult detectorResult = new Detector(image.getBlackMatrix()).detect(hints);
            decoderResult = this.decoder.decode(detectorResult.getBits(), hints);
            points = detectorResult.getPoints();
        } else {
            decoderResult = this.decoder.decode(extractPureBits(image.getBlackMatrix()), hints);
            points = NO_POINTS;
        }
        if (decoderResult.getOther() instanceof QRCodeDecoderMetaData) {
            ((QRCodeDecoderMetaData) decoderResult.getOther()).applyMirroredCorrection(points);
        }
        Result result = new Result(decoderResult.getText(), decoderResult.getRawBytes(), points, BarcodeFormat.QR_CODE);
        List<byte[]> byteSegments = decoderResult.getByteSegments();
        if (byteSegments != null) {
            result.putMetadata(ResultMetadataType.BYTE_SEGMENTS, byteSegments);
        }
        String ecLevel = decoderResult.getECLevel();
        if (ecLevel != null) {
            result.putMetadata(ResultMetadataType.ERROR_CORRECTION_LEVEL, ecLevel);
        }
        return result;
    }

    @Override // com.huawei.zxing.Reader
    public void reset() {
    }

    private static BitMatrix extractPureBits(BitMatrix image) throws NotFoundException {
        int[] leftTopBlack = image.getTopLeftOnBit();
        int[] rightBottomBlack = image.getBottomRightOnBit();
        if (leftTopBlack == null || rightBottomBlack == null) {
            throw NotFoundException.getNotFoundInstance();
        }
        float moduleSize = moduleSize(leftTopBlack, image);
        int top = leftTopBlack[1];
        int bottom = rightBottomBlack[1];
        int left = leftTopBlack[0];
        int right = rightBottomBlack[0];
        if (left >= right || top >= bottom) {
            throw NotFoundException.getNotFoundInstance();
        }
        if (bottom - top != right - left) {
            right = left + (bottom - top);
        }
        int matrixWidth = Math.round(((float) ((right - left) + 1)) / moduleSize);
        int matrixHeight = Math.round(((float) ((bottom - top) + 1)) / moduleSize);
        if (matrixWidth <= 0 || matrixHeight <= 0) {
            throw NotFoundException.getNotFoundInstance();
        } else if (matrixHeight == matrixWidth) {
            int nudge = (int) (moduleSize / 2.0f);
            int top2 = top + nudge;
            int left2 = left + nudge;
            int nudgedTooFarRight = (((int) (((float) (matrixWidth - 1)) * moduleSize)) + left2) - (right - 1);
            if (nudgedTooFarRight > 0) {
                if (nudgedTooFarRight <= nudge) {
                    left2 -= nudgedTooFarRight;
                } else {
                    throw NotFoundException.getNotFoundInstance();
                }
            }
            int nudgedTooFarDown = (((int) (((float) (matrixHeight - 1)) * moduleSize)) + top2) - (bottom - 1);
            if (nudgedTooFarDown > 0) {
                if (nudgedTooFarDown <= nudge) {
                    top2 -= nudgedTooFarDown;
                } else {
                    throw NotFoundException.getNotFoundInstance();
                }
            }
            BitMatrix bits = new BitMatrix(matrixWidth, matrixHeight);
            int y = 0;
            while (y < matrixHeight) {
                int iOffset = ((int) (((float) y) * moduleSize)) + top2;
                int x = 0;
                while (x < matrixWidth) {
                    if (image.get(((int) (((float) x) * moduleSize)) + left2, iOffset)) {
                        bits.set(x, y);
                    }
                    x++;
                    rightBottomBlack = rightBottomBlack;
                }
                y++;
                leftTopBlack = leftTopBlack;
            }
            return bits;
        } else {
            throw NotFoundException.getNotFoundInstance();
        }
    }

    private static float moduleSize(int[] leftTopBlack, BitMatrix image) throws NotFoundException {
        int height = image.getHeight();
        int width = image.getWidth();
        int x = leftTopBlack[0];
        int y = leftTopBlack[1];
        boolean inBlack = true;
        int transitions = 0;
        while (x < width && y < height) {
            if (inBlack != image.get(x, y)) {
                transitions++;
                if (transitions == 5) {
                    break;
                }
                inBlack = !inBlack;
            }
            x++;
            y++;
        }
        if (x != width && y != height) {
            return ((float) (x - leftTopBlack[0])) / 7.0f;
        }
        throw NotFoundException.getNotFoundInstance();
    }
}
