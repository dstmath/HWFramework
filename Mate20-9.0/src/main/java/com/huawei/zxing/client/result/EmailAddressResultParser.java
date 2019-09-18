package com.huawei.zxing.client.result;

import com.huawei.zxing.Result;
import java.util.Map;

public final class EmailAddressResultParser extends ResultParser {
    public EmailAddressParsedResult parse(Result result) {
        String rawText = getMassagedText(result);
        if (rawText.startsWith("mailto:") || rawText.startsWith("MAILTO:")) {
            String emailAddress = rawText.substring(7);
            int queryStart = emailAddress.indexOf(63);
            if (queryStart >= 0) {
                emailAddress = emailAddress.substring(0, queryStart);
            }
            String emailAddress2 = urlDecode(emailAddress);
            Map<String, String> nameValues = parseNameValuePairs(rawText);
            String subject = null;
            String body = null;
            if (nameValues != null) {
                if (emailAddress2.isEmpty()) {
                    emailAddress2 = nameValues.get("to");
                }
                subject = nameValues.get("subject");
                body = nameValues.get("body");
            }
            return new EmailAddressParsedResult(emailAddress2, subject, body, rawText);
        } else if (!EmailDoCoMoResultParser.isBasicallyValidEmailAddress(rawText)) {
            return null;
        } else {
            String emailAddress3 = rawText;
            return new EmailAddressParsedResult(emailAddress3, null, null, "mailto:" + emailAddress3);
        }
    }
}
