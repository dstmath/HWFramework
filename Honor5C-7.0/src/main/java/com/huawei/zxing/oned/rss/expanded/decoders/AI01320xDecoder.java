package com.huawei.zxing.oned.rss.expanded.decoders;

import com.huawei.android.os.BuildEx.VERSION_CODES;
import com.huawei.zxing.common.BitArray;

final class AI01320xDecoder extends AI013x0xDecoder {
    AI01320xDecoder(BitArray information) {
        super(information);
    }

    protected void addWeightCode(StringBuilder buf, int weight) {
        if (weight < VERSION_CODES.CUR_DEVELOPMENT) {
            buf.append("(3202)");
        } else {
            buf.append("(3203)");
        }
    }

    protected int checkWeight(int weight) {
        if (weight < VERSION_CODES.CUR_DEVELOPMENT) {
            return weight;
        }
        return weight - 10000;
    }
}
