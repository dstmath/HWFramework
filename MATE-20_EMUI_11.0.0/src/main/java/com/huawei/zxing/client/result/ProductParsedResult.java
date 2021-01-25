package com.huawei.zxing.client.result;

public final class ProductParsedResult extends ParsedResult {
    private final String normalizedProductID;
    private final String productID;

    ProductParsedResult(String productID2) {
        this(productID2, productID2);
    }

    ProductParsedResult(String productID2, String normalizedProductID2) {
        super(ParsedResultType.PRODUCT);
        this.productID = productID2;
        this.normalizedProductID = normalizedProductID2;
    }

    public String getProductID() {
        return this.productID;
    }

    public String getNormalizedProductID() {
        return this.normalizedProductID;
    }

    @Override // com.huawei.zxing.client.result.ParsedResult
    public String getDisplayResult() {
        return this.productID;
    }
}
