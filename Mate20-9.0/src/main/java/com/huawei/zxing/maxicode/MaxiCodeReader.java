package com.huawei.zxing.maxicode;

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
import com.huawei.zxing.maxicode.decoder.Decoder;
import java.util.Map;

public final class MaxiCodeReader implements Reader {
    private static final int MATRIX_HEIGHT = 33;
    private static final int MATRIX_WIDTH = 30;
    private static final ResultPoint[] NO_POINTS = new ResultPoint[0];
    private final Decoder decoder = new Decoder();

    public Result decode(BinaryBitmap image) throws NotFoundException, ChecksumException, FormatException {
        return decode(image, null);
    }

    public Result decode(BinaryBitmap image, Map<DecodeHintType, ?> hints) throws NotFoundException, ChecksumException, FormatException {
        if (hints == null || !hints.containsKey(DecodeHintType.PURE_BARCODE)) {
            throw NotFoundException.getNotFoundInstance();
        }
        DecoderResult decoderResult = this.decoder.decode(extractPureBits(image.getBlackMatrix()), hints);
        Result result = new Result(decoderResult.getText(), decoderResult.getRawBytes(), NO_POINTS, BarcodeFormat.MAXICODE);
        String ecLevel = decoderResult.getECLevel();
        if (ecLevel != null) {
            result.putMetadata(ResultMetadataType.ERROR_CORRECTION_LEVEL, ecLevel);
        }
        return result;
    }

    public void reset() {
    }

    private static BitMatrix extractPureBits(BitMatrix image) throws NotFoundException {
        int[] enclosingRectangle = image.getEnclosingRectangle();
        if (enclosingRectangle != null) {
            int left = enclosingRectangle[0];
            int top = enclosingRectangle[1];
            int width = enclosingRectangle[2];
            int height = enclosingRectangle[3];
            BitMatrix bits = new BitMatrix(30, 33);
            for (int y = 0; y < 33; y++) {
                int iy = (((y * height) + (height / 2)) / 33) + top;
                for (int x = 0; x < 30; x++) {
                    if (image.get(((((x * width) + (width / 2)) + (((y & 1) * width) / 2)) / 30) + left, iy)) {
                        bits.set(x, y);
                    }
                }
            }
            return bits;
        }
        throw NotFoundException.getNotFoundInstance();
    }
}
