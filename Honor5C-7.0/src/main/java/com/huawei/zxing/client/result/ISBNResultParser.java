package com.huawei.zxing.client.result;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.Result;

public final class ISBNResultParser extends ResultParser {
    public ISBNParsedResult parse(Result result) {
        if (result.getBarcodeFormat() != BarcodeFormat.EAN_13) {
            return null;
        }
        String rawText = ResultParser.getMassagedText(result);
        if (rawText.length() != 13) {
            return null;
        }
        if (rawText.startsWith("978") || rawText.startsWith("979")) {
            return new ISBNParsedResult(rawText);
        }
        return null;
    }
}
