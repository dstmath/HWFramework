package com.huawei.zxing.oned;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.common.BitArray;

public final class EAN13Reader extends UPCEANReader {
    static final int[] FIRST_DIGIT_ENCODINGS = {0, 11, 13, 14, 19, 25, 28, 21, 22, 26};
    private final int[] decodeMiddleCounters = new int[4];

    /* access modifiers changed from: protected */
    public int decodeMiddle(BitArray row, int[] startRange, StringBuilder resultString) throws NotFoundException {
        BitArray bitArray = row;
        StringBuilder sb = resultString;
        int[] counters = this.decodeMiddleCounters;
        counters[0] = 0;
        counters[1] = 0;
        counters[2] = 0;
        counters[3] = 0;
        int end = row.getSize();
        int lgPatternFound = 0;
        int lgPatternFound2 = startRange[1];
        int x = 0;
        while (x < 6 && lgPatternFound2 < end) {
            int bestMatch = decodeDigit(bitArray, counters, lgPatternFound2, L_AND_G_PATTERNS);
            sb.append((char) (48 + (bestMatch % 10)));
            int rowOffset = lgPatternFound2;
            for (int counter : counters) {
                rowOffset += counter;
            }
            if (bestMatch >= 10) {
                lgPatternFound = (1 << (5 - x)) | lgPatternFound;
            }
            x++;
            lgPatternFound2 = rowOffset;
        }
        determineFirstDigit(sb, lgPatternFound);
        int rowOffset2 = findGuardPattern(bitArray, lgPatternFound2, true, MIDDLE_PATTERN)[1];
        int x2 = 0;
        while (x2 < 6 && rowOffset2 < end) {
            sb.append((char) (48 + decodeDigit(bitArray, counters, rowOffset2, L_PATTERNS)));
            int rowOffset3 = rowOffset2;
            for (int counter2 : counters) {
                rowOffset3 += counter2;
            }
            x2++;
            rowOffset2 = rowOffset3;
        }
        return rowOffset2;
    }

    /* access modifiers changed from: package-private */
    public BarcodeFormat getBarcodeFormat() {
        return BarcodeFormat.EAN_13;
    }

    private static void determineFirstDigit(StringBuilder resultString, int lgPatternFound) throws NotFoundException {
        for (int d = 0; d < 10; d++) {
            if (lgPatternFound == FIRST_DIGIT_ENCODINGS[d]) {
                resultString.insert(0, (char) (48 + d));
                return;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }
}
