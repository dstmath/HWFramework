package com.huawei.zxing.client.result;

import com.huawei.zxing.Result;
import java.util.Map;

public final class EmailAddressResultParser extends ResultParser {
    public EmailAddressParsedResult parse(Result result) {
        String rawText = ResultParser.getMassagedText(result);
        String emailAddress;
        if (rawText.startsWith("mailto:") || rawText.startsWith("MAILTO:")) {
            emailAddress = rawText.substring(7);
            int queryStart = emailAddress.indexOf(63);
            if (queryStart >= 0) {
                emailAddress = emailAddress.substring(0, queryStart);
            }
            emailAddress = ResultParser.urlDecode(emailAddress);
            Map<String, String> nameValues = ResultParser.parseNameValuePairs(rawText);
            String str = null;
            String str2 = null;
            if (nameValues != null) {
                if (emailAddress.isEmpty()) {
                    emailAddress = (String) nameValues.get("to");
                }
                str = (String) nameValues.get("subject");
                str2 = (String) nameValues.get("body");
            }
            return new EmailAddressParsedResult(emailAddress, str, str2, rawText);
        } else if (!EmailDoCoMoResultParser.isBasicallyValidEmailAddress(rawText)) {
            return null;
        } else {
            emailAddress = rawText;
            return new EmailAddressParsedResult(rawText, null, null, "mailto:" + rawText);
        }
    }
}
