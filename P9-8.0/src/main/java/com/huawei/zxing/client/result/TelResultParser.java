package com.huawei.zxing.client.result;

import com.huawei.zxing.Result;

public final class TelResultParser extends ResultParser {
    public TelParsedResult parse(Result result) {
        String rawText = ResultParser.getMassagedText(result);
        if (!rawText.startsWith("tel:") && (rawText.startsWith("TEL:") ^ 1) != 0) {
            return null;
        }
        String telURI = rawText.startsWith("TEL:") ? "tel:" + rawText.substring(4) : rawText;
        int queryStart = rawText.indexOf(63, 4);
        return new TelParsedResult(queryStart < 0 ? rawText.substring(4) : rawText.substring(4, queryStart), telURI, null);
    }
}
