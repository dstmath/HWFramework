package com.huawei.zxing.client.result;

import com.huawei.zxing.Result;

public final class TelResultParser extends ResultParser {
    @Override // com.huawei.zxing.client.result.ResultParser
    public TelParsedResult parse(Result result) {
        String telURI;
        String rawText = getMassagedText(result);
        if (!rawText.startsWith("tel:") && !rawText.startsWith("TEL:")) {
            return null;
        }
        if (rawText.startsWith("TEL:")) {
            telURI = "tel:" + rawText.substring(4);
        } else {
            telURI = rawText;
        }
        int queryStart = rawText.indexOf(63, 4);
        return new TelParsedResult(queryStart < 0 ? rawText.substring(4) : rawText.substring(4, queryStart), telURI, null);
    }
}
