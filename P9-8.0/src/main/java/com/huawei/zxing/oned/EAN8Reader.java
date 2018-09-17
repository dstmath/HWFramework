package com.huawei.zxing.oned;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.common.BitArray;

public final class EAN8Reader extends UPCEANReader {
    private final int[] decodeMiddleCounters = new int[4];

    protected int decodeMiddle(BitArray row, int[] startRange, StringBuilder result) throws NotFoundException {
        int x;
        int[] counters = this.decodeMiddleCounters;
        counters[0] = 0;
        counters[1] = 0;
        counters[2] = 0;
        counters[3] = 0;
        int end = row.getSize();
        int rowOffset = startRange[1];
        for (x = 0; x < 4 && rowOffset < end; x++) {
            result.append((char) (UPCEANReader.decodeDigit(row, counters, rowOffset, L_PATTERNS) + 48));
            for (int counter : counters) {
                rowOffset += counter;
            }
        }
        rowOffset = UPCEANReader.findGuardPattern(row, rowOffset, true, MIDDLE_PATTERN)[1];
        for (x = 0; x < 4 && rowOffset < end; x++) {
            result.append((char) (UPCEANReader.decodeDigit(row, counters, rowOffset, L_PATTERNS) + 48));
            for (int counter2 : counters) {
                rowOffset += counter2;
            }
        }
        return rowOffset;
    }

    BarcodeFormat getBarcodeFormat() {
        return BarcodeFormat.EAN_8;
    }
}
