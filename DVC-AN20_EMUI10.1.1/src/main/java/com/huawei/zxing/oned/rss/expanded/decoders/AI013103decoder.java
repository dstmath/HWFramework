package com.huawei.zxing.oned.rss.expanded.decoders;

import com.huawei.zxing.common.BitArray;

final class AI013103decoder extends AI013x0xDecoder {
    AI013103decoder(BitArray information) {
        super(information);
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.zxing.oned.rss.expanded.decoders.AI01weightDecoder
    public void addWeightCode(StringBuilder buf, int weight) {
        buf.append("(3103)");
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.zxing.oned.rss.expanded.decoders.AI01weightDecoder
    public int checkWeight(int weight) {
        return weight;
    }
}
