package com.huawei.zxing.client.result;

import com.huawei.zxing.Result;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class URIResultParser extends ResultParser {
    private static final Pattern URL_WITHOUT_PROTOCOL_PATTERN = Pattern.compile("([a-zA-Z0-9\\-]+\\.)+[a-zA-Z]{2,}(:\\d{1,5})?(/|\\?|$)");
    private static final Pattern URL_WITH_PROTOCOL_PATTERN = Pattern.compile("[a-zA-Z0-9]{2,}:");

    public URIParsedResult parse(Result result) {
        String rawText = ResultParser.getMassagedText(result);
        if (rawText.startsWith("URL:") || rawText.startsWith("URI:")) {
            return new URIParsedResult(rawText.substring(4).trim(), null);
        }
        rawText = rawText.trim();
        return isBasicallyValidURI(rawText) ? new URIParsedResult(rawText, null) : null;
    }

    static boolean isBasicallyValidURI(String uri) {
        boolean z = true;
        if (uri.contains(" ")) {
            return false;
        }
        Matcher m = URL_WITH_PROTOCOL_PATTERN.matcher(uri);
        if (m.find() && m.start() == 0) {
            return true;
        }
        m = URL_WITHOUT_PROTOCOL_PATTERN.matcher(uri);
        if (!(m.find() && m.start() == 0)) {
            z = false;
        }
        return z;
    }
}
