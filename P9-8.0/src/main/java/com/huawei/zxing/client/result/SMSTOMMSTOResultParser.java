package com.huawei.zxing.client.result;

import com.huawei.zxing.Result;

public final class SMSTOMMSTOResultParser extends ResultParser {
    public SMSParsedResult parse(Result result) {
        boolean z;
        String rawText = ResultParser.getMassagedText(result);
        if (rawText.startsWith("smsto:") || rawText.startsWith("SMSTO:") || rawText.startsWith("mmsto:")) {
            z = true;
        } else {
            z = rawText.startsWith("MMSTO:");
        }
        if (!z) {
            return null;
        }
        String number = rawText.substring(6);
        String body = null;
        int bodyStart = number.indexOf(58);
        if (bodyStart >= 0) {
            body = number.substring(bodyStart + 1);
            number = number.substring(0, bodyStart);
        }
        return new SMSParsedResult(number, null, null, body);
    }
}
