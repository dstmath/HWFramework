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

    public Result decode(BinaryBitmap image) throws NotFoundException, ChecksumException, FormatException {
        return decode(image, null);
    }

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

    public void reset() {
    }

    private static BitMatrix extractPureBits(BitMatrix image) throws NotFoundException {
        int[] leftTopBlack;
        BitMatrix bitMatrix = image;
        int[] leftTopBlack2 = image.getTopLeftOnBit();
        int[] rightBottomBlack = image.getBottomRightOnBit();
        if (leftTopBlack2 == null || rightBottomBlack == null) {
            int[] iArr = rightBottomBlack;
            throw NotFoundException.getNotFoundInstance();
        }
        float moduleSize = moduleSize(leftTopBlack2, bitMatrix);
        int top = leftTopBlack2[1];
        int bottom = rightBottomBlack[1];
        int left = leftTopBlack2[0];
        int right = rightBottomBlack[0];
        if (left >= right || top >= bottom) {
            int[] iArr2 = rightBottomBlack;
            throw NotFoundException.getNotFoundInstance();
        }
        if (bottom - top != right - left) {
            right = left + (bottom - top);
        }
        int matrixWidth = Math.round(((float) ((right - left) + 1)) / moduleSize);
        int matrixHeight = Math.round(((float) ((bottom - top) + 1)) / moduleSize);
        if (matrixWidth <= 0 || matrixHeight <= 0) {
            int[] iArr3 = rightBottomBlack;
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
                while (true) {
                    leftTopBlack = leftTopBlack2;
                    int x2 = x;
                    if (x2 >= matrixWidth) {
                        break;
                    }
                    int[] rightBottomBlack2 = rightBottomBlack;
                    if (bitMatrix.get(((int) (((float) x2) * moduleSize)) + left2, iOffset)) {
                        bits.set(x2, y);
                    }
                    x = x2 + 1;
                    leftTopBlack2 = leftTopBlack;
                    rightBottomBlack = rightBottomBlack2;
                }
                y++;
                leftTopBlack2 = leftTopBlack;
            }
            int[] iArr4 = rightBottomBlack;
            return bits;
        } else {
            int[] iArr5 = rightBottomBlack;
            throw NotFoundException.getNotFoundInstance();
        }
    }

    private static float moduleSize(int[] leftTopBlack, BitMatrix image) throws NotFoundException {
        int height = image.getHeight();
        int width = image.getWidth();
        int x = leftTopBlack[0];
        boolean inBlack = true;
        int y = leftTopBlack[1];
        int x2 = x;
        int transitions = 0;
        while (x2 < width && y < height) {
            if (inBlack != image.get(x2, y)) {
                transitions++;
                if (transitions == 5) {
                    break;
                }
                inBlack = !inBlack;
            }
            x2++;
            y++;
        }
        if (x2 != width && y != height) {
            return ((float) (x2 - leftTopBlack[0])) / 7.0f;
        }
        throw NotFoundException.getNotFoundInstance();
    }
}
