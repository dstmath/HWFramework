package com.huawei.zxing.oned;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.Result;
import com.huawei.zxing.ResultMetadataType;
import com.huawei.zxing.ResultPoint;
import com.huawei.zxing.common.BitArray;
import java.util.EnumMap;
import java.util.Map;

final class UPCEANExtension5Support {
    private static final int[] CHECK_DIGIT_ENCODINGS = {24, 20, 18, 17, 12, 6, 3, 10, 9, 5};
    private final int[] decodeMiddleCounters = new int[4];
    private final StringBuilder decodeRowStringBuffer = new StringBuilder();

    UPCEANExtension5Support() {
    }

    /* access modifiers changed from: package-private */
    public Result decodeRow(int rowNumber, BitArray row, int[] extensionStartRange) throws NotFoundException {
        StringBuilder result = this.decodeRowStringBuffer;
        result.setLength(0);
        int end = decodeMiddle(row, extensionStartRange, result);
        String resultString = result.toString();
        Map<ResultMetadataType, Object> extensionData = parseExtensionString(resultString);
        Result extensionResult = new Result(resultString, null, new ResultPoint[]{new ResultPoint(((float) (extensionStartRange[0] + extensionStartRange[1])) / 2.0f, (float) rowNumber), new ResultPoint((float) end, (float) rowNumber)}, BarcodeFormat.UPC_EAN_EXTENSION);
        if (extensionData != null) {
            extensionResult.putAllMetadata(extensionData);
        }
        return extensionResult;
    }

    /* access modifiers changed from: package-private */
    public int decodeMiddle(BitArray row, int[] startRange, StringBuilder resultString) throws NotFoundException {
        int[] counters = this.decodeMiddleCounters;
        counters[0] = 0;
        counters[1] = 0;
        counters[2] = 0;
        counters[3] = 0;
        int end = row.getSize();
        int lgPatternFound = 0;
        int rowOffset = startRange[1];
        for (int x = 0; x < 5 && rowOffset < end; x++) {
            int bestMatch = UPCEANReader.decodeDigit(row, counters, rowOffset, UPCEANReader.L_AND_G_PATTERNS);
            resultString.append((char) (48 + (bestMatch % 10)));
            int rowOffset2 = rowOffset;
            for (int counter : counters) {
                rowOffset2 += counter;
            }
            if (bestMatch >= 10) {
                lgPatternFound |= 1 << (4 - x);
            }
            if (x != 4) {
                rowOffset = row.getNextUnset(row.getNextSet(rowOffset2));
            } else {
                rowOffset = rowOffset2;
            }
        }
        if (resultString.length() == 5) {
            if (extensionChecksum(resultString.toString()) == determineCheckDigit(lgPatternFound)) {
                return rowOffset;
            }
            throw NotFoundException.getNotFoundInstance();
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static int extensionChecksum(CharSequence s) {
        int length = s.length();
        int sum = 0;
        for (int i = length - 2; i >= 0; i -= 2) {
            sum += s.charAt(i) - '0';
        }
        int sum2 = sum * 3;
        for (int i2 = length - 1; i2 >= 0; i2 -= 2) {
            sum2 += s.charAt(i2) - '0';
        }
        return (sum2 * 3) % 10;
    }

    private static int determineCheckDigit(int lgPatternFound) throws NotFoundException {
        for (int d = 0; d < 10; d++) {
            if (lgPatternFound == CHECK_DIGIT_ENCODINGS[d]) {
                return d;
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static Map<ResultMetadataType, Object> parseExtensionString(String raw) {
        if (raw.length() != 5) {
            return null;
        }
        Object value = parseExtension5String(raw);
        if (value == null) {
            return null;
        }
        Map<ResultMetadataType, Object> result = new EnumMap<>(ResultMetadataType.class);
        result.put(ResultMetadataType.SUGGESTED_PRICE, value);
        return result;
    }

    private static String parseExtension5String(String raw) {
        String currency;
        String hundredthsString;
        char charAt = raw.charAt(0);
        if (charAt == '0') {
            currency = "£";
        } else if (charAt == '5') {
            currency = "$";
        } else if (charAt != '9') {
            currency = "";
        } else if ("90000".equals(raw)) {
            return null;
        } else {
            if ("99991".equals(raw)) {
                return "0.00";
            }
            if ("99990".equals(raw)) {
                return "Used";
            }
            currency = "";
        }
        int rawAmount = Integer.parseInt(raw.substring(1));
        String unitsString = String.valueOf(rawAmount / 100);
        int hundredths = rawAmount % 100;
        if (hundredths < 10) {
            hundredthsString = "0" + hundredths;
        } else {
            hundredthsString = String.valueOf(hundredths);
        }
        return currency + unitsString + '.' + hundredthsString;
    }
}
