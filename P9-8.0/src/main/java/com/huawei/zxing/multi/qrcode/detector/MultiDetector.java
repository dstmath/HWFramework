package com.huawei.zxing.multi.qrcode.detector;

import com.huawei.zxing.DecodeHintType;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.ReaderException;
import com.huawei.zxing.ResultPointCallback;
import com.huawei.zxing.common.BitMatrix;
import com.huawei.zxing.common.DetectorResult;
import com.huawei.zxing.qrcode.detector.Detector;
import com.huawei.zxing.qrcode.detector.FinderPatternInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MultiDetector extends Detector {
    private static final DetectorResult[] EMPTY_DETECTOR_RESULTS = new DetectorResult[0];

    public MultiDetector(BitMatrix image) {
        super(image);
    }

    public DetectorResult[] detectMulti(Map<DecodeHintType, ?> hints) throws NotFoundException {
        FinderPatternInfo[] infos = new MultiFinderPatternFinder(getImage(), hints == null ? null : (ResultPointCallback) hints.get(DecodeHintType.NEED_RESULT_POINT_CALLBACK)).findMulti(hints);
        if (infos.length == 0) {
            throw NotFoundException.getNotFoundInstance();
        }
        List<DetectorResult> result = new ArrayList();
        for (FinderPatternInfo info : infos) {
            try {
                result.add(processFinderPatternInfo(info));
            } catch (ReaderException e) {
            }
        }
        if (result.isEmpty()) {
            return EMPTY_DETECTOR_RESULTS;
        }
        return (DetectorResult[]) result.toArray(new DetectorResult[result.size()]);
    }
}
