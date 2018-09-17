package com.huawei.zxing.multi.qrcode;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.BinaryBitmap;
import com.huawei.zxing.DecodeHintType;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.ReaderException;
import com.huawei.zxing.Result;
import com.huawei.zxing.ResultMetadataType;
import com.huawei.zxing.ResultPoint;
import com.huawei.zxing.common.DecoderResult;
import com.huawei.zxing.common.DetectorResult;
import com.huawei.zxing.multi.MultipleBarcodeReader;
import com.huawei.zxing.multi.qrcode.detector.MultiDetector;
import com.huawei.zxing.qrcode.QRCodeReader;
import com.huawei.zxing.qrcode.decoder.QRCodeDecoderMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class QRCodeMultiReader extends QRCodeReader implements MultipleBarcodeReader {
    private static final Result[] EMPTY_RESULT_ARRAY = new Result[0];

    public Result[] decodeMultiple(BinaryBitmap image) throws NotFoundException {
        return decodeMultiple(image, null);
    }

    public Result[] decodeMultiple(BinaryBitmap image, Map<DecodeHintType, ?> hints) throws NotFoundException {
        List<Result> results = new ArrayList();
        for (DetectorResult detectorResult : new MultiDetector(image.getBlackMatrix()).detectMulti(hints)) {
            try {
                DecoderResult decoderResult = getDecoder().decode(detectorResult.getBits(), (Map) hints);
                ResultPoint[] points = detectorResult.getPoints();
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
                results.add(result);
            } catch (ReaderException e) {
            }
        }
        if (results.isEmpty()) {
            return EMPTY_RESULT_ARRAY;
        }
        return (Result[]) results.toArray(new Result[results.size()]);
    }
}
