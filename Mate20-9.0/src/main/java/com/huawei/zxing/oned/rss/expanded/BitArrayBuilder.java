package com.huawei.zxing.oned.rss.expanded;

import com.huawei.zxing.common.BitArray;
import java.util.List;

final class BitArrayBuilder {
    private BitArrayBuilder() {
    }

    static BitArray buildBitArray(List<ExpandedPair> pairs) {
        int accPos;
        int charNumber = (pairs.size() << 1) - 1;
        if (pairs.get(pairs.size() - 1).getRightChar() == null) {
            charNumber--;
        }
        BitArray binary = new BitArray(12 * charNumber);
        int firstValue = pairs.get(0).getRightChar().getValue();
        int accPos2 = 0;
        for (int i = 11; i >= 0; i--) {
            if (((1 << i) & firstValue) != 0) {
                binary.set(accPos);
            }
            accPos2 = accPos + 1;
        }
        for (int i2 = 1; i2 < pairs.size(); i2++) {
            ExpandedPair currentPair = pairs.get(i2);
            int leftValue = currentPair.getLeftChar().getValue();
            int accPos3 = accPos;
            for (int j = 11; j >= 0; j--) {
                if (((1 << j) & leftValue) != 0) {
                    binary.set(accPos3);
                }
                accPos3++;
            }
            if (currentPair.getRightChar() != null) {
                int rightValue = currentPair.getRightChar().getValue();
                int accPos4 = accPos3;
                for (int j2 = 11; j2 >= 0; j2--) {
                    if (((1 << j2) & rightValue) != 0) {
                        binary.set(accPos4);
                    }
                    accPos4++;
                }
                accPos = accPos4;
            } else {
                accPos = accPos3;
            }
        }
        return binary;
    }
}
