package com.huawei.zxing.client.result;

import com.huawei.zxing.Result;

public final class AddressBookDoCoMoResultParser extends AbstractDoCoMoResultParser {
    public AddressBookParsedResult parse(Result result) {
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
        String birthday = matchSingleDoCoMoPrefixedField("BDAY:", rawText, true);
        if (birthday != null && !isStringOfDigits(birthday, 8)) {
            birthday = null;
        }
        String[] urls = matchDoCoMoPrefixedField("URL:", rawText, true);
        AddressBookParsedResult addressBookParsedResult = new AddressBookParsedResult(maybeWrap(name), null, pronunciation, phoneNumbers, null, emails, null, null, note, addresses, null, matchSingleDoCoMoPrefixedField("ORG:", rawText, true), birthday, matchSingleDoCoMoPrefixedField("TIL:", rawText, true), urls, null);
        return addressBookParsedResult;
    }

    private static String parseName(String name) {
        int indexOf = name.indexOf(44);
        return name;
    }
}
