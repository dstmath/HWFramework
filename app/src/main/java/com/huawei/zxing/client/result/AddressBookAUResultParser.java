package com.huawei.zxing.client.result;

import com.huawei.zxing.Result;
import java.util.ArrayList;
import java.util.List;

public final class AddressBookAUResultParser extends ResultParser {
    public AddressBookParsedResult parse(Result result) {
        String rawText = ResultParser.getMassagedText(result);
        if (rawText.contains("MEMORY")) {
            if (rawText.contains("\r\n")) {
                return new AddressBookParsedResult(ResultParser.maybeWrap(ResultParser.matchSinglePrefixedField("NAME1:", rawText, '\r', true)), null, ResultParser.matchSinglePrefixedField("NAME2:", rawText, '\r', true), matchMultipleValuePrefix("TEL", 3, rawText, true), null, matchMultipleValuePrefix("MAIL", 3, rawText, true), null, null, ResultParser.matchSinglePrefixedField("MEMORY:", rawText, '\r', false), ResultParser.matchSinglePrefixedField("ADD:", rawText, '\r', true) == null ? null : new String[]{ResultParser.matchSinglePrefixedField("ADD:", rawText, '\r', true)}, null, null, null, null, null, null);
            }
        }
        return null;
    }

    private static String[] matchMultipleValuePrefix(String prefix, int max, String rawText, boolean trim) {
        List values = null;
        for (int i = 1; i <= max; i++) {
            String value = ResultParser.matchSinglePrefixedField(prefix + i + ':', rawText, '\r', trim);
            if (value == null) {
                break;
            }
            if (values == null) {
                values = new ArrayList(max);
            }
            values.add(value);
        }
        if (values == null) {
            return null;
        }
        return (String[]) values.toArray(new String[values.size()]);
    }
}
