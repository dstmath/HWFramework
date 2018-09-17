package com.huawei.zxing.oned;

import android.telephony.HwCarrierConfigManager;
import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.NotFoundException;
import com.huawei.zxing.Result;
import com.huawei.zxing.ResultMetadataType;
import com.huawei.zxing.ResultPoint;
import com.huawei.zxing.common.BitArray;
import java.util.EnumMap;
import java.util.Map;

final class UPCEANExtension5Support {
    private static final int[] CHECK_DIGIT_ENCODINGS = new int[]{24, 20, 18, 17, 12, 6, 3, 10, 9, 5};
    private final int[] decodeMiddleCounters = new int[4];
    private final StringBuilder decodeRowStringBuffer = new StringBuilder();

    UPCEANExtension5Support() {
    }

    Result decodeRow(int rowNumber, BitArray row, int[] extensionStartRange) throws NotFoundException {
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

    int decodeMiddle(BitArray row, int[] startRange, StringBuilder resultString) throws NotFoundException {
        int[] counters = this.decodeMiddleCounters;
        counters[0] = 0;
        counters[1] = 0;
        counters[2] = 0;
        counters[3] = 0;
        int end = row.getSize();
        int rowOffset = startRange[1];
        int lgPatternFound = 0;
        for (int x = 0; x < 5 && rowOffset < end; x++) {
            int bestMatch = UPCEANReader.decodeDigit(row, counters, rowOffset, UPCEANReader.L_AND_G_PATTERNS);
            resultString.append((char) ((bestMatch % 10) + 48));
            for (int counter : counters) {
                rowOffset += counter;
            }
            if (bestMatch >= 10) {
                lgPatternFound |= 1 << (4 - x);
            }
            if (x != 4) {
                rowOffset = row.getNextUnset(row.getNextSet(rowOffset));
            }
        }
        if (resultString.length() != 5) {
            throw NotFoundException.getNotFoundInstance();
        }
        if (extensionChecksum(resultString.toString()) == determineCheckDigit(lgPatternFound)) {
            return rowOffset;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static int extensionChecksum(CharSequence s) {
        int i;
        int length = s.length();
        int sum = 0;
        for (i = length - 2; i >= 0; i -= 2) {
            sum += s.charAt(i) - 48;
        }
        sum *= 3;
        for (i = length - 1; i >= 0; i -= 2) {
            sum += s.charAt(i) - 48;
        }
        return (sum * 3) % 10;
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
        Map<ResultMetadataType, Object> result = new EnumMap(ResultMetadataType.class);
        result.put(ResultMetadataType.SUGGESTED_PRICE, value);
        return result;
    }

    private static String parseExtension5String(String raw) {
        String currency;
        switch (raw.charAt(0)) {
            case HwCarrierConfigManager.HD_ICON_MASK_STATUS_BAR /*48*/:
                currency = "£";
                break;
            case '5':
                currency = "$";
                break;
            case '9':
                if (!"90000".equals(raw)) {
                    if (!"99991".equals(raw)) {
                        if (!"99990".equals(raw)) {
                            currency = "";
                            break;
                        }
                        return "Used";
                    }
                    return "0.00";
                }
                return null;
            default:
                currency = "";
                break;
        }
        int rawAmount = Integer.parseInt(raw.substring(1));
        int hundredths = rawAmount % 100;
        return currency + String.valueOf(rawAmount / 100) + '.' + (hundredths < 10 ? "0" + hundredths : String.valueOf(hundredths));
    }
}
