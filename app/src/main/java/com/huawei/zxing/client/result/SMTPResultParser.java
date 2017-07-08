package com.huawei.zxing.client.result;

import com.huawei.zxing.Result;

public final class SMTPResultParser extends ResultParser {
    public EmailAddressParsedResult parse(Result result) {
        String rawText = ResultParser.getMassagedText(result);
        if (!(!rawText.startsWith("smtp:") ? rawText.startsWith("SMTP:") : true)) {
            return null;
        }
        String emailAddress = rawText.substring(5);
        String str = null;
        String str2 = null;
        int colon = emailAddress.indexOf(58);
        if (colon >= 0) {
            str = emailAddress.substring(colon + 1);
            emailAddress = emailAddress.substring(0, colon);
            colon = str.indexOf(58);
            if (colon >= 0) {
                str2 = str.substring(colon + 1);
                str = str.substring(0, colon);
            }
        }
        return new EmailAddressParsedResult(emailAddress, str, str2, "mailto:" + emailAddress);
    }
}
