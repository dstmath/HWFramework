package com.huawei.zxing.client.result;

import com.huawei.zxing.Result;

public final class AddressBookDoCoMoResultParser extends AbstractDoCoMoResultParser {
    public AddressBookParsedResult parse(Result result) {
        String rawText = ResultParser.getMassagedText(result);
        if (!rawText.startsWith("MECARD:")) {
            return null;
        }
        String name = "";
        String[] rawName = AbstractDoCoMoResultParser.matchDoCoMoPrefixedField("N:", rawText, true);
        if (rawName != null) {
            name = parseName(rawName[0]);
        }
        String pronunciation = AbstractDoCoMoResultParser.matchSingleDoCoMoPrefixedField("SOUND:", rawText, true);
        String[] phoneNumbers = AbstractDoCoMoResultParser.matchDoCoMoPrefixedField("TEL:", rawText, true);
        String[] emails = AbstractDoCoMoResultParser.matchDoCoMoPrefixedField("EMAIL:", rawText, true);
        String note = AbstractDoCoMoResultParser.matchSingleDoCoMoPrefixedField("NOTE:", rawText, false);
        String[] addresses = AbstractDoCoMoResultParser.matchDoCoMoPrefixedField("ADR:", rawText, true);
        String birthday = AbstractDoCoMoResultParser.matchSingleDoCoMoPrefixedField("BDAY:", rawText, true);
        if (!(birthday == null || (ResultParser.isStringOfDigits(birthday, 8) ^ 1) == 0)) {
            birthday = null;
        }
        String[] urls = AbstractDoCoMoResultParser.matchDoCoMoPrefixedField("URL:", rawText, true);
        return new AddressBookParsedResult(ResultParser.maybeWrap(name), null, pronunciation, phoneNumbers, null, emails, null, null, note, addresses, null, AbstractDoCoMoResultParser.matchSingleDoCoMoPrefixedField("ORG:", rawText, true), birthday, AbstractDoCoMoResultParser.matchSingleDoCoMoPrefixedField("TIL:", rawText, true), urls, null);
    }

    private static String parseName(String name) {
        int comma = name.indexOf(44);
        return name;
    }
}
