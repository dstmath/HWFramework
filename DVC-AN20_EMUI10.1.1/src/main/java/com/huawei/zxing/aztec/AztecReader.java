package com.huawei.zxing.aztec;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.BinaryBitmap;
import com.huawei.zxing.DecodeHintType;
import com.huawei.zxing.FormatException;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.Reader;
import com.huawei.zxing.Result;
import com.huawei.zxing.ResultMetadataType;
import com.huawei.zxing.ResultPoint;
import com.huawei.zxing.ResultPointCallback;
import com.huawei.zxing.aztec.decoder.Decoder;
import com.huawei.zxing.aztec.detector.Detector;
import com.huawei.zxing.common.DecoderResult;
import java.util.List;
import java.util.Map;

public final class AztecReader implements Reader {
    @Override // com.huawei.zxing.Reader
    public Result decode(BinaryBitmap image) throws NotFoundException, FormatException {
        return decode(image, null);
    }

    @Override // com.huawei.zxing.Reader
    public Result decode(BinaryBitmap image, Map<DecodeHintType, ?> hints) throws NotFoundException, FormatException {
        ResultPointCallback rpcb;
        NotFoundException notFoundException = null;
        FormatException formatException = null;
        Detector detector = new Detector(image.getBlackMatrix());
        ResultPoint[] points = null;
        DecoderResult decoderResult = null;
        try {
            AztecDetectorResult detectorResult = detector.detect(false);
            points = detectorResult.getPoints();
            decoderResult = new Decoder().decode(detectorResult);
        } catch (NotFoundException e) {
            notFoundException = e;
        } catch (FormatException e2) {
            formatException = e2;
        }
        if (decoderResult == null) {
            try {
                AztecDetectorResult detectorResult2 = detector.detect(true);
                points = detectorResult2.getPoints();
                decoderResult = new Decoder().decode(detectorResult2);
            } catch (NotFoundException e3) {
                if (notFoundException != null) {
                    throw notFoundException;
                } else if (formatException != null) {
                    throw formatException;
                } else {
                    throw e3;
                }
            } catch (FormatException e4) {
                if (notFoundException != null) {
                    throw notFoundException;
                } else if (formatException != null) {
                    throw formatException;
                } else {
                    throw e4;
                }
            }
        }
        if (!(hints == null || (rpcb = (ResultPointCallback) hints.get(DecodeHintType.NEED_RESULT_POINT_CALLBACK)) == null)) {
            for (ResultPoint point : points) {
                rpcb.foundPossibleResultPoint(point);
            }
        }
        Result result = new Result(decoderResult.getText(), decoderResult.getRawBytes(), points, BarcodeFormat.AZTEC);
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
}
