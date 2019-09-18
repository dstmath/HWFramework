package com.huawei.zxing.client.result;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.Result;
import java.util.HashMap;
import java.util.Map;

public final class ExpandedProductResultParser extends ResultParser {
    public ExpandedProductParsedResult parse(Result result) {
        BarcodeFormat format = result.getBarcodeFormat();
        if (format != BarcodeFormat.RSS_EXPANDED) {
            return null;
        }
        String rawText = getMassagedText(result);
        Map<String, String> uncommonAIs = new HashMap<>();
        BarcodeFormat barcodeFormat = format;
        String productID = null;
        String sscc = null;
        String lotNumber = null;
        String productionDate = null;
        String packagingDate = null;
        String bestBeforeDate = null;
        String expirationDate = null;
        String weight = null;
        String weightType = null;
        String weightIncrement = null;
        String price = null;
        String priceIncrement = null;
        String priceCurrency = null;
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 < rawText.length()) {
                String ai = findAIvalue(i2, rawText);
                if (ai == null) {
                    return null;
                }
                int i3 = i2 + ai.length() + 2;
                String value = findValue(i3, rawText);
                int i4 = value.length() + i3;
                if ("00".equals(ai)) {
                    sscc = value;
                } else if ("01".equals(ai)) {
                    productID = value;
                } else if ("10".equals(ai)) {
                    lotNumber = value;
                } else if ("11".equals(ai)) {
                    productionDate = value;
                } else if ("13".equals(ai)) {
                    packagingDate = value;
                } else if ("15".equals(ai)) {
                    bestBeforeDate = value;
                } else if ("17".equals(ai)) {
                    expirationDate = value;
                } else if ("3100".equals(ai) || "3101".equals(ai) || "3102".equals(ai) || "3103".equals(ai) || "3104".equals(ai) || "3105".equals(ai) || "3106".equals(ai) || "3107".equals(ai) || "3108".equals(ai) || "3109".equals(ai)) {
                    weight = value;
                    weightType = ExpandedProductParsedResult.KILOGRAM;
                    weightIncrement = ai.substring(3);
                    i = i4;
                } else if ("3200".equals(ai) || "3201".equals(ai) || "3202".equals(ai) || "3203".equals(ai) || "3204".equals(ai) || "3205".equals(ai) || "3206".equals(ai) || "3207".equals(ai) || "3208".equals(ai) || "3209".equals(ai)) {
                    weight = value;
                    weightType = ExpandedProductParsedResult.POUND;
                    weightIncrement = ai.substring(3);
                    i = i4;
                } else if ("3920".equals(ai) || "3921".equals(ai) || "3922".equals(ai) || "3923".equals(ai)) {
                    price = value;
                    priceIncrement = ai.substring(3);
                    i = i4;
                } else if (!"3930".equals(ai) && !"3931".equals(ai) && !"3932".equals(ai) && !"3933".equals(ai)) {
                    uncommonAIs.put(ai, value);
                } else if (value.length() < 4) {
                    return null;
                } else {
                    price = value.substring(3);
                    priceCurrency = value.substring(0, 3);
                    priceIncrement = ai.substring(3);
                    i = i4;
                }
                i = i4;
            } else {
                int i5 = i2;
                ExpandedProductParsedResult expandedProductParsedResult = new ExpandedProductParsedResult(rawText, productID, sscc, lotNumber, productionDate, packagingDate, bestBeforeDate, expirationDate, weight, weightType, weightIncrement, price, priceIncrement, priceCurrency, uncommonAIs);
                return expandedProductParsedResult;
            }
        }
    }

    private static String findAIvalue(int i, String rawText) {
        if (rawText.charAt(i) != '(') {
            return null;
        }
        CharSequence rawTextAux = rawText.substring(i + 1);
        StringBuilder buf = new StringBuilder();
        for (int index = 0; index < rawTextAux.length(); index++) {
            char currentChar = rawTextAux.charAt(index);
            if (currentChar == ')') {
                return buf.toString();
            }
            if (currentChar < '0' || currentChar > '9') {
                return null;
            }
            buf.append(currentChar);
        }
        return buf.toString();
    }

    private static String findValue(int i, String rawText) {
        StringBuilder buf = new StringBuilder();
        String rawTextAux = rawText.substring(i);
        for (int index = 0; index < rawTextAux.length(); index++) {
            char c = rawTextAux.charAt(index);
            if (c == '(') {
                if (findAIvalue(index, rawTextAux) != null) {
                    break;
                }
                buf.append('(');
            } else {
                buf.append(c);
            }
        }
        return buf.toString();
    }
}
