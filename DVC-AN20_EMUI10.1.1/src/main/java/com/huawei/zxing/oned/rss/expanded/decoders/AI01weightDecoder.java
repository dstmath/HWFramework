package com.huawei.zxing.oned.rss.expanded.decoders;

import com.huawei.android.os.UserHandleEx;
import com.huawei.zxing.common.BitArray;

/* access modifiers changed from: package-private */
public abstract class AI01weightDecoder extends AI01decoder {
    /* access modifiers changed from: protected */
    public abstract void addWeightCode(StringBuilder sb, int i);

    /* access modifiers changed from: protected */
    public abstract int checkWeight(int i);

    AI01weightDecoder(BitArray information) {
        super(information);
    }

    /* access modifiers changed from: protected */
    public final void encodeCompressedWeight(StringBuilder buf, int currentPos, int weightSize) {
        int originalWeightNumeric = getGeneralDecoder().extractNumericValueFromBitArray(currentPos, weightSize);
        addWeightCode(buf, originalWeightNumeric);
        int weightNumeric = checkWeight(originalWeightNumeric);
        int currentDivisor = UserHandleEx.PER_USER_RANGE;
        for (int i = 0; i < 5; i++) {
            if (weightNumeric / currentDivisor == 0) {
                buf.append('0');
            }
            currentDivisor /= 10;
        }
        buf.append(weightNumeric);
    }
}
