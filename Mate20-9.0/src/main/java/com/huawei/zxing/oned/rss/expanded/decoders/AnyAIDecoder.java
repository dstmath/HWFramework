package com.huawei.zxing.oned.rss.expanded.decoders;

import com.huawei.zxing.FormatException;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.common.BitArray;

final class AnyAIDecoder extends AbstractExpandedDecoder {
    private static final int HEADER_SIZE = 5;

    AnyAIDecoder(BitArray information) {
        super(information);
    }

    public String parseInformation() throws NotFoundException, FormatException {
        return getGeneralDecoder().decodeAllCodes(new StringBuilder(), 5);
    }
}
