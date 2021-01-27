package com.huawei.zxing.client.result;

import com.huawei.zxing.Result;
import java.util.regex.Pattern;

public final class EmailDoCoMoResultParser extends AbstractDoCoMoResultParser {
    private static final Pattern ATEXT_ALPHANUMERIC = Pattern.compile("[a-zA-Z0-9@.!#$%&'*+\\-/=?^_`{|}~]+");

    @Override // com.huawei.zxing.client.result.ResultParser
    public EmailAddressParsedResult parse(Result result) {
        String[] rawTo;
        String rawText = getMassagedText(result);
        if (!rawText.startsWith("MATMSG:") || (rawTo = matchDoCoMoPrefixedField("TO:", rawText, true)) == null) {
            return null;
        }
        String to = rawTo[0];
        if (!isBasicallyValidEmailAddress(to)) {
            return null;
        }
        String subject = matchSingleDoCoMoPrefixedField("SUB:", rawText, false);
        String body = matchSingleDoCoMoPrefixedField("BODY:", rawText, false);
        return new EmailAddressParsedResult(to, subject, body, "mailto:" + to);
    }

    static boolean isBasicallyValidEmailAddress(String email) {
        return email != null && ATEXT_ALPHANUMERIC.matcher(email).matches() && email.indexOf(64) >= 0;
    }
}
