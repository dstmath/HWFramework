package com.huawei.zxing.client.result;

public final class TelParsedResult extends ParsedResult {
    private final String number;
    private final String telURI;
    private final String title;

    public TelParsedResult(String number2, String telURI2, String title2) {
        super(ParsedResultType.TEL);
        this.number = number2;
        this.telURI = telURI2;
        this.title = title2;
    }

    public String getNumber() {
        return this.number;
    }

    public String getTelURI() {
        return this.telURI;
    }

    public String getTitle() {
        return this.title;
    }

    @Override // com.huawei.zxing.client.result.ParsedResult
    public String getDisplayResult() {
        StringBuilder result = new StringBuilder(20);
        maybeAppend(this.number, result);
        maybeAppend(this.title, result);
        return result.toString();
    }
}
