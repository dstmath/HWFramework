package com.huawei.zxing.client.result;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.Result;
import com.huawei.zxing.oned.UPCEReader;

public final class ProductResultParser extends ResultParser {
    public ProductParsedResult parse(Result result) {
        BarcodeFormat format = result.getBarcodeFormat();
        if (format != BarcodeFormat.UPC_A && format != BarcodeFormat.UPC_E && format != BarcodeFormat.EAN_8 && format != BarcodeFormat.EAN_13) {
            return null;
        }
        String normalizedProductID;
        String rawText = ResultParser.getMassagedText(result);
        int length = rawText.length();
        for (int x = 0; x < length; x++) {
            char c = rawText.charAt(x);
            if (c < '0' || c > '9') {
                return null;
            }
        }
        if (format == BarcodeFormat.UPC_E) {
            normalizedProductID = UPCEReader.convertUPCEtoUPCA(rawText);
        } else {
            normalizedProductID = rawText;
        }
        return new ProductParsedResult(rawText, normalizedProductID);
    }
}
