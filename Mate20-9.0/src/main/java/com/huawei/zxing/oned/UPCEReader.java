package com.huawei.zxing.oned;

import com.huawei.android.app.AppOpsManagerEx;
import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.ChecksumException;
import com.huawei.zxing.FormatException;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.common.BitArray;

public final class UPCEReader extends UPCEANReader {
    private static final int[] MIDDLE_END_PATTERN = {1, 1, 1, 1, 1, 1};
    private static final int[][] NUMSYS_AND_CHECK_DIGIT_PATTERNS = {new int[]{56, 52, 50, 49, 44, 38, 35, 42, 41, 37}, new int[]{7, 11, 13, 14, 19, 25, 28, 21, 22, 26}};
    private final int[] decodeMiddleCounters = new int[4];

    /* access modifiers changed from: protected */
    public int decodeMiddle(BitArray row, int[] startRange, StringBuilder result) throws NotFoundException {
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
            int bestMatch = decodeDigit(row, counters, lgPatternFound2, L_AND_G_PATTERNS);
            result.append((char) (48 + (bestMatch % 10)));
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
        determineNumSysAndCheckDigit(result, lgPatternFound);
        return lgPatternFound2;
    }

    /* access modifiers changed from: protected */
    public int[] decodeEnd(BitArray row, int endStart) throws NotFoundException {
        return findGuardPattern(row, endStart, true, MIDDLE_END_PATTERN);
    }

    /* access modifiers changed from: protected */
    public boolean checkChecksum(String s) throws FormatException, ChecksumException {
        return super.checkChecksum(convertUPCEtoUPCA(s));
    }

    private static void determineNumSysAndCheckDigit(StringBuilder resultString, int lgPatternFound) throws NotFoundException {
        for (int numSys = 0; numSys <= 1; numSys++) {
            for (int d = 0; d < 10; d++) {
                if (lgPatternFound == NUMSYS_AND_CHECK_DIGIT_PATTERNS[numSys][d]) {
                    resultString.insert(0, (char) (48 + numSys));
                    resultString.append((char) (48 + d));
                    return;
                }
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    /* access modifiers changed from: package-private */
    public BarcodeFormat getBarcodeFormat() {
        return BarcodeFormat.UPC_E;
    }

    public static String convertUPCEtoUPCA(String upce) {
        char[] upceChars = new char[6];
        upce.getChars(1, 7, upceChars, 0);
        StringBuilder result = new StringBuilder(12);
        result.append(upce.charAt(0));
        char lastChar = upceChars[5];
        switch (lastChar) {
            case '0':
            case '1':
            case '2':
                result.append(upceChars, 0, 2);
                result.append(lastChar);
                result.append("0000");
                result.append(upceChars, 2, 3);
                break;
            case '3':
                result.append(upceChars, 0, 3);
                result.append("00000");
                result.append(upceChars, 3, 2);
                break;
            case AppOpsManagerEx.OP_ADD_VOICEMAIL:
                result.append(upceChars, 0, 4);
                result.append("00000");
                result.append(upceChars[4]);
                break;
            default:
                result.append(upceChars, 0, 5);
                result.append("0000");
                result.append(lastChar);
                break;
        }
        result.append(upce.charAt(7));
        return result.toString();
    }
}
