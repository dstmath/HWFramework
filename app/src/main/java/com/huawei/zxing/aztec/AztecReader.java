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
    public Result decode(BinaryBitmap image) throws NotFoundException, FormatException {
        return decode(image, null);
    }

    public Result decode(BinaryBitmap image, Map<DecodeHintType, ?> hints) throws NotFoundException, FormatException {
        NotFoundException notFoundException = null;
        FormatException formatException = null;
        Detector detector = new Detector(image.getBlackMatrix());
        ResultPoint[] resultPointArr = null;
        DecoderResult decoderResult = null;
        try {
            AztecDetectorResult detectorResult = detector.detect(false);
            resultPointArr = detectorResult.getPoints();
            decoderResult = new Decoder().decode(detectorResult);
        } catch (NotFoundException e) {
            notFoundException = e;
        } catch (FormatException e2) {
            formatException = e2;
        }
        if (decoderResult == null) {
            try {
                detectorResult = detector.detect(true);
                resultPointArr = detectorResult.getPoints();
                decoderResult = new Decoder().decode(detectorResult);
            } catch (NotFoundException e3) {
                if (notFoundException != null) {
                    throw notFoundException;
                } else if (formatException != null) {
                    throw formatException;
                } else {
                    throw e3;
                }
            } catch (FormatException e22) {
                if (notFoundException != null) {
                    throw notFoundException;
                } else if (formatException != null) {
                    throw formatException;
                } else {
                    throw e22;
                }
            }
        }
        if (hints != null) {
            ResultPointCallback rpcb = (ResultPointCallback) hints.get(DecodeHintType.NEED_RESULT_POINT_CALLBACK);
            if (rpcb != null) {
                for (ResultPoint point : resultPointArr) {
                    rpcb.foundPossibleResultPoint(point);
                }
            }
        }
        Result result = new Result(decoderResult.getText(), decoderResult.getRawBytes(), resultPointArr, BarcodeFormat.AZTEC);
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
}
