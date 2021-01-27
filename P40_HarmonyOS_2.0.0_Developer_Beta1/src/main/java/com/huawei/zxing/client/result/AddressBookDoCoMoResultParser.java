package com.huawei.zxing.client.result;

import com.huawei.zxing.Result;

public final class AddressBookDoCoMoResultParser extends AbstractDoCoMoResultParser {
    @Override // com.huawei.zxing.client.result.ResultParser
    public AddressBookParsedResult parse(Result result) {
        String birthday;
        String rawText = getMassagedText(result);
        if (!rawText.startsWith("MECARD:")) {
            return null;
        }
        String name = "";
        String[] rawName = matchDoCoMoPrefixedField("N:", rawText, true);
        if (rawName != null) {
            name = parseName(rawName[0]);
        }
        String pronunciation = matchSingleDoCoMoPrefixedField("SOUND:", rawText, true);
        String[] phoneNumbers = matchDoCoMoPrefixedField("TEL:", rawText, true);
        String[] emails = matchDoCoMoPrefixedField("EMAIL:", rawText, true);
        String note = matchSingleDoCoMoPrefixedField("NOTE:", rawText, false);
        String[] addresses = matchDoCoMoPrefixedField("ADR:", rawText, true);
        String birthday2 = matchSingleDoCoMoPrefixedField("BDAY:", rawText, true);
        if (birthday2 == null || isStringOfDigits(birthday2, 8)) {
            birthday = birthday2;
        } else {
            birthday = null;
        }
        return new AddressBookParsedResult(maybeWrap(name), null, pronunciation, phoneNumbers, null, emails, null, null, note, addresses, null, matchSingleDoCoMoPrefixedField("ORG:", rawText, true), birthday, matchSingleDoCoMoPrefixedField("TIL:", rawText, true), matchDoCoMoPrefixedField("URL:", rawText, true), null);
    }

    private static String parseName(String name) {
        name.indexOf(44);
        return name;
    }
}
