package com.huawei.zxing.oned;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.common.BitArray;

public final class EAN13Reader extends UPCEANReader {
    static final int[] FIRST_DIGIT_ENCODINGS = {0, 11, 13, 14, 19, 25, 28, 21, 22, 26};
    private final int[] decodeMiddleCounters = new int[4];

    /* access modifiers changed from: protected */
    @Override // com.huawei.zxing.oned.UPCEANReader
    public int decodeMiddle(BitArray row, int[] startRange, StringBuilder resultString) throws NotFoundException {
        int[] counters = this.decodeMiddleCounters;
        counters[0] = 0;
        counters[1] = 0;
        counters[2] = 0;
        counters[3] = 0;
        int end = row.getSize();
        int rowOffset = startRange[1];
        int lgPatternFound = 0;
        int x = 0;
        while (x < 6 && rowOffset < end) {
            int bestMatch = decodeDigit(row, counters, rowOffset, L_AND_G_PATTERNS);
            resultString.append((char) ((bestMatch % 10) + 48));
            int rowOffset2 = rowOffset;
            for (int counter : counters) {
                rowOffset2 += counter;
            }
            if (bestMatch >= 10) {
                lgPatternFound = (1 << (5 - x)) | lgPatternFound;
            }
            x++;
            rowOffset = rowOffset2;
        }
        determineFirstDigit(resultString, lgPatternFound);
        int rowOffset3 = findGuardPattern(row, rowOffset, true, MIDDLE_PATTERN)[1];
        int x2 = 0;
        while (x2 < 6 && rowOffset3 < end) {
            resultString.append((char) (decodeDigit(row, counters, rowOffset3, L_PATTERNS) + 48));
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
        return BarcodeFormat.EAN_13;
    }

    private static void determineFirstDigit(StringBuilder resultString, int lgPatternFound) throws NotFoundException {
        for (int d = 0; d < 10; d++) {
            if (lgPatternFound == FIRST_DIGIT_ENCODINGS[d]) {
                resultString.insert(0, (char) (d + 48));
                return;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }
}
