package com.huawei.zxing.client.result;

import com.huawei.zxing.BarcodeFormat;
import com.huawei.zxing.Result;
import java.util.HashMap;
import java.util.Map;

public final class ExpandedProductResultParser extends ResultParser {
    @Override // com.huawei.zxing.client.result.ResultParser
    public ExpandedProductParsedResult parse(Result result) {
        Map<String, String> uncommonAIs;
        Map<String, String> uncommonAIs2;
        Map<String, String> uncommonAIs3;
        Map<String, String> uncommonAIs4;
        if (result.getBarcodeFormat() != BarcodeFormat.RSS_EXPANDED) {
            return null;
        }
        String rawText = getMassagedText(result);
        Map<String, String> uncommonAIs5 = new HashMap<>();
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
        while (i < rawText.length()) {
            String ai = findAIvalue(i, rawText);
            if (ai == null) {
                return null;
            }
            int i2 = i + ai.length() + 2;
            String value = findValue(i2, rawText);
            i = i2 + value.length();
            if ("00".equals(ai)) {
                sscc = value;
                uncommonAIs = uncommonAIs5;
            } else if ("01".equals(ai)) {
                productID = value;
                uncommonAIs = uncommonAIs5;
            } else if ("10".equals(ai)) {
                lotNumber = value;
                uncommonAIs = uncommonAIs5;
            } else if ("11".equals(ai)) {
                productionDate = value;
                uncommonAIs = uncommonAIs5;
            } else if ("13".equals(ai)) {
                packagingDate = value;
                uncommonAIs = uncommonAIs5;
            } else if ("15".equals(ai)) {
                bestBeforeDate = value;
                uncommonAIs = uncommonAIs5;
            } else if ("17".equals(ai)) {
                expirationDate = value;
                uncommonAIs = uncommonAIs5;
            } else {
                if ("3100".equals(ai) || "3101".equals(ai)) {
                    uncommonAIs2 = uncommonAIs5;
                } else if ("3102".equals(ai) || "3103".equals(ai)) {
                    uncommonAIs2 = uncommonAIs5;
                } else if ("3104".equals(ai) || "3105".equals(ai)) {
                    uncommonAIs2 = uncommonAIs5;
                } else if ("3106".equals(ai) || "3107".equals(ai)) {
                    uncommonAIs2 = uncommonAIs5;
                } else if ("3108".equals(ai)) {
                    uncommonAIs2 = uncommonAIs5;
                } else if ("3109".equals(ai)) {
                    uncommonAIs2 = uncommonAIs5;
                } else {
                    if ("3200".equals(ai) || "3201".equals(ai)) {
                        uncommonAIs3 = uncommonAIs5;
                    } else if ("3202".equals(ai) || "3203".equals(ai)) {
                        uncommonAIs3 = uncommonAIs5;
                    } else if ("3204".equals(ai) || "3205".equals(ai)) {
                        uncommonAIs3 = uncommonAIs5;
                    } else if ("3206".equals(ai) || "3207".equals(ai)) {
                        uncommonAIs3 = uncommonAIs5;
                    } else if ("3208".equals(ai)) {
                        uncommonAIs3 = uncommonAIs5;
                    } else if ("3209".equals(ai)) {
                        uncommonAIs3 = uncommonAIs5;
                    } else {
                        if ("3920".equals(ai) || "3921".equals(ai)) {
                            uncommonAIs4 = uncommonAIs5;
                        } else if ("3922".equals(ai)) {
                            uncommonAIs4 = uncommonAIs5;
                        } else if ("3923".equals(ai)) {
                            uncommonAIs4 = uncommonAIs5;
                        } else {
                            if ("3930".equals(ai) || "3931".equals(ai)) {
                                uncommonAIs = uncommonAIs5;
                            } else if ("3932".equals(ai)) {
                                uncommonAIs = uncommonAIs5;
                            } else if ("3933".equals(ai)) {
                                uncommonAIs = uncommonAIs5;
                            } else {
                                uncommonAIs = uncommonAIs5;
                                uncommonAIs.put(ai, value);
                            }
                            if (value.length() < 4) {
                                return null;
                            }
                            price = value.substring(3);
                            priceCurrency = value.substring(0, 3);
                            priceIncrement = ai.substring(3);
                        }
                        price = value;
                        priceIncrement = ai.substring(3);
                    }
                    weight = value;
                    weightType = ExpandedProductParsedResult.POUND;
                    weightIncrement = ai.substring(3);
                }
                weight = value;
                weightType = ExpandedProductParsedResult.KILOGRAM;
                weightIncrement = ai.substring(3);
            }
            uncommonAIs5 = uncommonAIs;
        }
        return new ExpandedProductParsedResult(rawText, productID, sscc, lotNumber, productionDate, packagingDate, bestBeforeDate, expirationDate, weight, weightType, weightIncrement, price, priceIncrement, priceCurrency, uncommonAIs5);
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
