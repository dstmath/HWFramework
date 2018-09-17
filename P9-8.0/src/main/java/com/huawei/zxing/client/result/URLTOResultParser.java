package com.huawei.zxing.client.result;

import com.huawei.zxing.Result;

public final class URLTOResultParser extends ResultParser {
    public URIParsedResult parse(Result result) {
        String rawText = ResultParser.getMassagedText(result);
        if (!rawText.startsWith("urlto:") && (rawText.startsWith("URLTO:") ^ 1) != 0) {
            return null;
        }
        int titleEnd = rawText.indexOf(58, 6);
        if (titleEnd < 0) {
            return null;
        }
        return new URIParsedResult(rawText.substring(titleEnd + 1), titleEnd <= 6 ? null : rawText.substring(6, titleEnd));
    }
}
