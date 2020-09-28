package com.huawei.zxing.oned;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.common.BitArray;

public final class EAN8Reader extends UPCEANReader {
    private final int[] decodeMiddleCounters = new int[4];

    /* access modifiers changed from: protected */
    @Override // com.huawei.zxing.oned.UPCEANReader
    public int decodeMiddle(BitArray row, int[] startRange, StringBuilder result) throws NotFoundException {
        int[] counters = this.decodeMiddleCounters;
        counters[0] = 0;
        counters[1] = 0;
        counters[2] = 0;
        counters[3] = 0;
        int end = row.getSize();
        int rowOffset = startRange[1];
        int x = 0;
        while (x < 4 && rowOffset < end) {
            result.append((char) (decodeDigit(row, counters, rowOffset, L_PATTERNS) + 48));
            int rowOffset2 = rowOffset;
            for (int counter : counters) {
                rowOffset2 += counter;
            }
            x++;
            rowOffset = rowOffset2;
        }
        int rowOffset3 = findGuardPattern(row, rowOffset, true, MIDDLE_PATTERN)[1];
        int x2 = 0;
        while (x2 < 4 && rowOffset3 < end) {
            result.append((char) (decodeDigit(row, counters, rowOffset3, L_PATTERNS) + 48));
            int rowOffset4 = rowOffset3;
            for (int counter2 : counters) {
                rowOffset4 += counter2;
            }
            x2++;
            rowOffset3 = rowOffset4;
        }
        return rowOffset3;
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.zxing.oned.UPCEANReader
    public BarcodeFormat getBarcodeFormat() {
        return BarcodeFormat.EAN_8;
    }
}
