package com.huawei.zxing.client.result;

import com.huawei.zxing.Result;

public final class URLTOResultParser extends ResultParser {
    public URIParsedResult parse(Result result) {
        String rawText = getMassagedText(result);
        String title = null;
        if (!rawText.startsWith("urlto:") && !rawText.startsWith("URLTO:")) {
            return null;
        }
        int titleEnd = rawText.indexOf(58, 6);
        if (titleEnd < 0) {
            return null;
        }
        if (titleEnd > 6) {
            title = rawText.substring(6, titleEnd);
        }
        return new URIParsedResult(rawText.substring(titleEnd + 1), title);
    }
}
