package com.huawei.zxing.client.result;

abstract class AbstractDoCoMoResultParser extends ResultParser {
    AbstractDoCoMoResultParser() {
    }

    static String[] matchDoCoMoPrefixedField(String prefix, String rawText, boolean trim) {
        return matchPrefixedField(prefix, rawText, ';', trim);
    }

    static String matchSingleDoCoMoPrefixedField(String prefix, String rawText, boolean trim) {
        return matchSinglePrefixedField(prefix, rawText, ';', trim);
    }
}
