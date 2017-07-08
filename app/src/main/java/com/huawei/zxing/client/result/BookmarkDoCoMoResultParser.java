package com.huawei.zxing.client.result;

import com.huawei.zxing.Result;

public final class BookmarkDoCoMoResultParser extends AbstractDoCoMoResultParser {
    public URIParsedResult parse(Result result) {
        URIParsedResult uRIParsedResult = null;
        String rawText = result.getText();
        if (!rawText.startsWith("MEBKM:")) {
            return null;
        }
        String title = AbstractDoCoMoResultParser.matchSingleDoCoMoPrefixedField("TITLE:", rawText, true);
        String[] rawUri = AbstractDoCoMoResultParser.matchDoCoMoPrefixedField("URL:", rawText, true);
        if (rawUri == null) {
            return null;
        }
        String uri = rawUri[0];
        if (URIResultParser.isBasicallyValidURI(uri)) {
            uRIParsedResult = new URIParsedResult(uri, title);
        }
        return uRIParsedResult;
    }
}
