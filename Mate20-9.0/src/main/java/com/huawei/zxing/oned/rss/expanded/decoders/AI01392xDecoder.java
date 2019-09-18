package com.huawei.zxing.oned.rss.expanded.decoders;

import com.huawei.zxing.FormatException;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.common.BitArray;

final class AI01392xDecoder extends AI01decoder {
    private static final int HEADER_SIZE = 8;
    private static final int LAST_DIGIT_SIZE = 2;

    AI01392xDecoder(BitArray information) {
        super(information);
    }

    public String parseInformation() throws NotFoundException, FormatException {
        if (getInformation().getSize() >= 48) {
            StringBuilder buf = new StringBuilder();
            encodeCompressedGtin(buf, 8);
            int lastAIdigit = getGeneralDecoder().extractNumericValueFromBitArray(48, 2);
            buf.append("(392");
            buf.append(lastAIdigit);
            buf.append(')');
            buf.append(getGeneralDecoder().decodeGeneralPurposeField(50, null).getNewString());
            return buf.toString();
        }
        throw NotFoundException.getNotFoundInstance();
    }
}
