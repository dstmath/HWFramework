package com.huawei.zxing.client.result;

public final class ISBNParsedResult extends ParsedResult {
    private final String isbn;

    ISBNParsedResult(String isbn2) {
        super(ParsedResultType.ISBN);
        this.isbn = isbn2;
    }

    public String getISBN() {
        return this.isbn;
    }

    @Override // com.huawei.zxing.client.result.ParsedResult
    public String getDisplayResult() {
        return this.isbn;
    }
}
