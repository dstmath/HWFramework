package com.huawei.zxing.client.result;

import com.huawei.zxing.Result;
import java.util.ArrayList;
import java.util.List;

public final class BizcardResultParser extends AbstractDoCoMoResultParser {
    public AddressBookParsedResult parse(Result result) {
        String rawText = getMassagedText(result);
        if (!rawText.startsWith("BIZCARD:")) {
            return null;
        }
        String fullName = buildName(matchSingleDoCoMoPrefixedField("N:", rawText, true), matchSingleDoCoMoPrefixedField("X:", rawText, true));
        String title = matchSingleDoCoMoPrefixedField("T:", rawText, true);
        String org2 = matchSingleDoCoMoPrefixedField("C:", rawText, true);
        String[] addresses = matchDoCoMoPrefixedField("A:", rawText, true);
        String phoneNumber1 = matchSingleDoCoMoPrefixedField("B:", rawText, true);
        String phoneNumber2 = matchSingleDoCoMoPrefixedField("M:", rawText, true);
        String phoneNumber3 = matchSingleDoCoMoPrefixedField("F:", rawText, true);
        String str = phoneNumber3;
        String str2 = phoneNumber2;
        String str3 = phoneNumber1;
        AddressBookParsedResult addressBookParsedResult = new AddressBookParsedResult(maybeWrap(fullName), null, null, buildPhoneNumbers(phoneNumber1, phoneNumber2, phoneNumber3), null, maybeWrap(matchSingleDoCoMoPrefixedField("E:", rawText, true)), null, null, null, addresses, null, org2, null, title, null, null);
        return addressBookParsedResult;
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
        String str;
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            str = firstName;
        } else {
            str = firstName + ' ' + lastName;
        }
        return str;
    }
}
