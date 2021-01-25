package com.huawei.zxing.oned.rss.expanded;

import com.huawei.zxing.common.BitArray;
import java.util.List;

final class BitArrayBuilder {
    private BitArrayBuilder() {
    }

    static BitArray buildBitArray(List<ExpandedPair> pairs) {
        int charNumber = (pairs.size() << 1) - 1;
        if (pairs.get(pairs.size() - 1).getRightChar() == null) {
            charNumber--;
        }
        BitArray binary = new BitArray(charNumber * 12);
        int accPos = 0;
        int firstValue = pairs.get(0).getRightChar().getValue();
        for (int i = 11; i >= 0; i--) {
            if (((1 << i) & firstValue) != 0) {
                binary.set(accPos);
            }
            accPos++;
        }
        for (int i2 = 1; i2 < pairs.size(); i2++) {
            ExpandedPair currentPair = pairs.get(i2);
            int leftValue = currentPair.getLeftChar().getValue();
            for (int j = 11; j >= 0; j--) {
                if (((1 << j) & leftValue) != 0) {
                    binary.set(accPos);
                }
                accPos++;
            }
            if (currentPair.getRightChar() != null) {
                int rightValue = currentPair.getRightChar().getValue();
                for (int j2 = 11; j2 >= 0; j2--) {
                    if (((1 << j2) & rightValue) != 0) {
                        binary.set(accPos);
                    }
                    accPos++;
                }
            }
        }
        return binary;
    }
}
