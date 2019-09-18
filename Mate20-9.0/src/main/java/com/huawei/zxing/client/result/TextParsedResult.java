package com.huawei.zxing.client.result;

public final class TextParsedResult extends ParsedResult {
    private final String language;
    private final String text;

    public TextParsedResult(String text2, String language2) {
        super(ParsedResultType.TEXT);
        this.text = text2;
        this.language = language2;
    }

    public String getText() {
        return this.text;
    }

    public String getLanguage() {
        return this.language;
    }

    public String getDisplayResult() {
        return this.text;
    }
}
