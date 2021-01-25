package com.huawei.zxing.client.result;

import com.huawei.zxing.Result;
import java.util.ArrayList;
import java.util.List;

public final class BizcardResultParser extends AbstractDoCoMoResultParser {
    @Override // com.huawei.zxing.client.result.ResultParser
    public AddressBookParsedResult parse(Result result) {
        String rawText = getMassagedText(result);
        if (!rawText.startsWith("BIZCARD:")) {
            return null;
        }
        String fullName = buildName(matchSingleDoCoMoPrefixedField("N:", rawText, true), matchSingleDoCoMoPrefixedField("X:", rawText, true));
        String title = matchSingleDoCoMoPrefixedField("T:", rawText, true);
        String org = matchSingleDoCoMoPrefixedField("C:", rawText, true);
        return new AddressBookParsedResult(maybeWrap(fullName), null, null, buildPhoneNumbers(matchSingleDoCoMoPrefixedField("B:", rawText, true), matchSingleDoCoMoPrefixedField("M:", rawText, true), matchSingleDoCoMoPrefixedField("F:", rawText, true)), null, maybeWrap(matchSingleDoCoMoPrefixedField("E:", rawText, true)), null, null, null, matchDoCoMoPrefixedField("A:", rawText, true), null, org, null, title, null, null);
    }

    private static String[] buildPhoneNumbers(String number1, String number2, String number3) {
        List<String> numbers = new ArrayList<>(3);
        if (number1 != null) {
            numbers.add(number1);
        }
        if (number2 != null) {
            numbers.add(number2);
        }
        if (number3 != null) {
            numbers.add(number3);
        }
        int size = numbers.size();
        if (size == 0) {
            return null;
        }
        return (String[]) numbers.toArray(new String[size]);
    }

    private static String buildName(String firstName, String lastName) {
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + ' ' + lastName;
    }
}
