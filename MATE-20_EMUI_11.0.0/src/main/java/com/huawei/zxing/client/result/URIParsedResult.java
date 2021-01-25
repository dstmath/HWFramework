package com.huawei.zxing.client.result;

import java.util.regex.Pattern;

public final class URIParsedResult extends ParsedResult {
    private static final Pattern USER_IN_HOST = Pattern.compile(":/*([^/@]+)@[^/]+");
    private final String title;
    private final String uri;

    public URIParsedResult(String uri2, String title2) {
        super(ParsedResultType.URI);
        this.uri = massageURI(uri2);
        this.title = title2;
    }

    public String getURI() {
        return this.uri;
    }

    public String getTitle() {
        return this.title;
    }

    public boolean isPossiblyMaliciousURI() {
        return USER_IN_HOST.matcher(this.uri).find();
    }

    @Override // com.huawei.zxing.client.result.ParsedResult
    public String getDisplayResult() {
        StringBuilder result = new StringBuilder(30);
        maybeAppend(this.title, result);
        maybeAppend(this.uri, result);
        return result.toString();
    }

    private static String massageURI(String uri2) {
        String uri3 = uri2.trim();
        int protocolEnd = uri3.indexOf(58);
        if (protocolEnd < 0) {
            return "http://" + uri3;
        } else if (!isColonFollowedByPortNumber(uri3, protocolEnd)) {
            return uri3;
        } else {
            return "http://" + uri3;
        }
    }

    private static boolean isColonFollowedByPortNumber(String uri2, int protocolEnd) {
        int nextSlash = uri2.indexOf(47, protocolEnd + 1);
        if (nextSlash < 0) {
            nextSlash = uri2.length();
        }
        if (nextSlash <= protocolEnd + 1) {
            return false;
        }
        for (int x = protocolEnd + 1; x < nextSlash; x++) {
            if (uri2.charAt(x) < '0' || uri2.charAt(x) > '9') {
                return false;
            }
        }
        return true;
    }
}
