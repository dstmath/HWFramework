package com.huawei.zxing.client.result;

import java.util.Map;

public final class ExpandedProductParsedResult extends ParsedResult {
    public static final String KILOGRAM = "KG";
    public static final String POUND = "LB";
    private final String bestBeforeDate;
    private final String expirationDate;
    private final String lotNumber;
    private final String packagingDate;
    private final String price;
    private final String priceCurrency;
    private final String priceIncrement;
    private final String productID;
    private final String productionDate;
    private final String rawText;
    private final String sscc;
    private final Map<String, String> uncommonAIs;
    private final String weight;
    private final String weightIncrement;
    private final String weightType;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ExpandedProductParsedResult(String rawText2, String productID2, String sscc2, String lotNumber2, String productionDate2, String packagingDate2, String bestBeforeDate2, String expirationDate2, String weight2, String weightType2, String weightIncrement2, String price2, String priceIncrement2, String priceCurrency2, Map<String, String> uncommonAIs2) {
        super(ParsedResultType.PRODUCT);
        this.rawText = rawText2;
        this.productID = productID2;
        this.sscc = sscc2;
        this.lotNumber = lotNumber2;
        this.productionDate = productionDate2;
        this.packagingDate = packagingDate2;
        this.bestBeforeDate = bestBeforeDate2;
        this.expirationDate = expirationDate2;
        this.weight = weight2;
        this.weightType = weightType2;
        this.weightIncrement = weightIncrement2;
        this.price = price2;
        this.priceIncrement = priceIncrement2;
        this.priceCurrency = priceCurrency2;
        this.uncommonAIs = uncommonAIs2;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof ExpandedProductParsedResult)) {
            return false;
        }
        ExpandedProductParsedResult other = (ExpandedProductParsedResult) o;
        if (equalsOrNull(this.productID, other.productID) && equalsOrNull(this.sscc, other.sscc) && equalsOrNull(this.lotNumber, other.lotNumber) && equalsOrNull(this.productionDate, other.productionDate) && equalsOrNull(this.bestBeforeDate, other.bestBeforeDate) && equalsOrNull(this.expirationDate, other.expirationDate) && equalsOrNull(this.weight, other.weight) && equalsOrNull(this.weightType, other.weightType) && equalsOrNull(this.weightIncrement, other.weightIncrement) && equalsOrNull(this.price, other.price) && equalsOrNull(this.priceIncrement, other.priceIncrement) && equalsOrNull(this.priceCurrency, other.priceCurrency) && equalsOrNull(this.uncommonAIs, other.uncommonAIs)) {
            z = true;
        }
        return z;
    }

    private static boolean equalsOrNull(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        }
        return o1.equals(o2);
    }

    public int hashCode() {
        return ((((((((((((0 ^ hashNotNull(this.productID)) ^ hashNotNull(this.sscc)) ^ hashNotNull(this.lotNumber)) ^ hashNotNull(this.productionDate)) ^ hashNotNull(this.bestBeforeDate)) ^ hashNotNull(this.expirationDate)) ^ hashNotNull(this.weight)) ^ hashNotNull(this.weightType)) ^ hashNotNull(this.weightIncrement)) ^ hashNotNull(this.price)) ^ hashNotNull(this.priceIncrement)) ^ hashNotNull(this.priceCurrency)) ^ hashNotNull(this.uncommonAIs);
    }

    private static int hashNotNull(Object o) {
        if (o == null) {
            return 0;
        }
        return o.hashCode();
    }

    public String getRawText() {
        return this.rawText;
    }

    public String getProductID() {
        return this.productID;
    }

    public String getSscc() {
        return this.sscc;
    }

    public String getLotNumber() {
        return this.lotNumber;
    }

    public String getProductionDate() {
        return this.productionDate;
    }

    public String getPackagingDate() {
        return this.packagingDate;
    }

    public String getBestBeforeDate() {
        return this.bestBeforeDate;
    }

    public String getExpirationDate() {
        return this.expirationDate;
    }

    public String getWeight() {
        return this.weight;
    }

    public String getWeightType() {
        return this.weightType;
    }

    public String getWeightIncrement() {
        return this.weightIncrement;
    }

    public String getPrice() {
        return this.price;
    }

    public String getPriceIncrement() {
        return this.priceIncrement;
    }

    public String getPriceCurrency() {
        return this.priceCurrency;
    }

    public Map<String, String> getUncommonAIs() {
        return this.uncommonAIs;
    }

    public String getDisplayResult() {
        return String.valueOf(this.rawText);
    }
}
