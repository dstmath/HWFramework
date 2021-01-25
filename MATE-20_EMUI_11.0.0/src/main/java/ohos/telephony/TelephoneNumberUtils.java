package ohos.telephony;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ohos.annotation.SystemApi;
import ohos.data.resultset.ResultSet;
import ohos.global.icu.util.ULocale;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class TelephoneNumberUtils {
    private static final char[] BCD_EXT_CALLED_PARTY_ARRAY = {'*', '#', 'a', 'b', 'c'};
    private static final char[] BCD_EXT_EF_ADN_ARRAY = {'*', '#', PAUSE, WILD, WAIT};
    private static final int BCD_EXT_FIRST_CHAR_VALUE = 10;
    public static final int BCD_EXT_TYPE_CALLED_PARTY = 2;
    public static final int BCD_EXT_TYPE_EF_ADN = 1;
    private static final int BEGIN_INDEX = 0;
    private static final String CLIR_PREFIX_OFF = "#31#";
    private static final String CLIR_PREFIX_ON = "*31#";
    private static final int DEFAULT_LOOSLY_COMPARE_NUMBER = 7;
    private static final String EMPTY_STRING = "";
    private static final int END_INDEX = 1;
    private static final Pattern INTERNATIONAL_TELEPHONE_NUMBER_PATTERN = Pattern.compile("[\\+]?[0-9.-]+");
    public static final int INTERNATIONAL_TON_VALUE = 9;
    private static final int INVALID_BCD_VALUE = 15;
    private static final int INVALID_INDEX = -1;
    private static final HashMap<Character, Character> L2D_MAP = new HashMap<>(52);
    private static final int MIN_MATCH_NUM = 7;
    private static final Pattern MMI_PATTERN = Pattern.compile("(^[*#])(.*)([*#])(.*)(#)$");
    public static final char PAUSE = ',';
    private static final int PREFIX_SIMILAR_TYPE_NUMBER = 3;
    private static final int PTL_LOW_4_BIT_MASK = 15;
    private static final int PTL_SEPARATION_LOOP = 2;
    private static final HiLogLabel TAG = new HiLogLabel(3, TelephonyUtils.LOG_ID_TELEPHONY, "TelephoneNumberUtils");
    private static final TelephonyProxy TELEPHONY_PROXY = TelephonyProxy.getInstance();
    public static final int UNKNOWN_TON_VALUE = 8;
    private static final Pattern UNUSUAL_PATTERN = Pattern.compile("(^[*#])(.*)([*#])(.*)");
    public static final char WAIT = ';';
    public static final char WILD = 'N';

    private static boolean isBeginPostDialKey(char c) {
        return c == ',' || c == ';';
    }

    public static boolean isDialTwelveKey(char c) {
        return c == '*' || c == '#' || (c >= '0' && c <= '9');
    }

    public static boolean isDialableKey(char c) {
        return c == '*' || c == '#' || c == '+' || c == 'N' || (c >= '0' && c <= '9');
    }

    public static boolean isLatinDigit(char c) {
        return c >= '0' && c <= '9';
    }

    public static boolean isNotSeparator(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == '+' || c == 'N' || c == ';' || c == ',';
    }

    private static boolean isPause(char c) {
        return c == 'p' || c == 'P';
    }

    public static boolean isReallyDialableKey(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == '+';
    }

    private static boolean isToneWait(char c) {
        return c == 'w' || c == 'W';
    }

    static {
        L2D_MAP.put('a', '2');
        L2D_MAP.put('b', '2');
        L2D_MAP.put('c', '2');
        L2D_MAP.put('A', '2');
        L2D_MAP.put('B', '2');
        L2D_MAP.put('C', '2');
        L2D_MAP.put('d', '3');
        L2D_MAP.put('e', '3');
        L2D_MAP.put('f', '3');
        L2D_MAP.put('D', '3');
        L2D_MAP.put('E', '3');
        L2D_MAP.put('F', '3');
        L2D_MAP.put('g', '4');
        L2D_MAP.put('h', '4');
        L2D_MAP.put('i', '4');
        L2D_MAP.put('G', '4');
        L2D_MAP.put('H', '4');
        L2D_MAP.put('I', '4');
        L2D_MAP.put('j', '5');
        L2D_MAP.put('k', '5');
        L2D_MAP.put('l', '5');
        L2D_MAP.put('J', '5');
        L2D_MAP.put('K', '5');
        L2D_MAP.put('L', '5');
        L2D_MAP.put('m', '6');
        L2D_MAP.put('n', '6');
        L2D_MAP.put('o', '6');
        L2D_MAP.put('M', '6');
        L2D_MAP.put(Character.valueOf(WILD), '6');
        L2D_MAP.put('O', '6');
        L2D_MAP.put('p', '7');
        L2D_MAP.put('q', '7');
        L2D_MAP.put('r', '7');
        L2D_MAP.put('s', '7');
        L2D_MAP.put('P', '7');
        L2D_MAP.put('Q', '7');
        L2D_MAP.put('R', '7');
        L2D_MAP.put('S', '7');
        L2D_MAP.put('t', '8');
        L2D_MAP.put(Character.valueOf(ULocale.UNICODE_LOCALE_EXTENSION), '8');
        L2D_MAP.put('v', '8');
        L2D_MAP.put('T', '8');
        L2D_MAP.put('U', '8');
        L2D_MAP.put('V', '8');
        L2D_MAP.put('w', '9');
        L2D_MAP.put(Character.valueOf(ULocale.PRIVATE_USE_EXTENSION), '9');
        L2D_MAP.put('y', '9');
        L2D_MAP.put('z', '9');
        L2D_MAP.put('W', '9');
        L2D_MAP.put('X', '9');
        L2D_MAP.put('Y', '9');
        L2D_MAP.put('Z', '9');
    }

    /* access modifiers changed from: private */
    public static class PrefixPosition {
        int beginIndex;
        int endIndex;
        String name;

        private PrefixPosition() {
        }
    }

    public static boolean isUriPhoneNumber(String str) {
        return str != null && (str.contains("@") || str.contains("%40"));
    }

    public static boolean isInternationalPhoneNumber(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        return INTERNATIONAL_TELEPHONE_NUMBER_PATTERN.matcher(str).matches();
    }

    private static char convertBcdToChar(byte b, int i) {
        if (b >= 0 && b < 10) {
            return (char) (b + 48);
        }
        if (i == 1) {
            int i2 = b - 10;
            char[] cArr = BCD_EXT_EF_ADN_ARRAY;
            if (i2 >= cArr.length) {
                return 0;
            }
            return cArr[i2];
        } else if (i == 2) {
            int i3 = b - 10;
            char[] cArr2 = BCD_EXT_CALLED_PARTY_ARRAY;
            if (i3 >= cArr2.length) {
                return 0;
            }
            return cArr2[i3];
        } else {
            HiLog.error(TAG, "convertBcdToChar input type = %{public}d error", Integer.valueOf(i));
            return 0;
        }
    }

    private static void basicCalledPartyBCDToString(StringBuilder sb, byte[] bArr, int i, int i2, int i3) {
        int i4;
        if (bArr == null || bArr.length == 0 || i < 0 || i2 < 0) {
            HiLog.error(TAG, "basicCalledPartyBCDToString data error", new Object[0]);
        } else if (i >= i2 || i >= bArr.length || i2 >= bArr.length) {
            HiLog.error(TAG, "Bcd to string error beginIdx %{public}d, endIdx %{public}d", Integer.valueOf(i), Integer.valueOf(i2));
        } else {
            while (i <= i2) {
                char c = 0;
                byte b = 15;
                for (int i5 = 0; i5 < 2; i5++) {
                    if (i5 == 0) {
                        i4 = bArr[i];
                    } else {
                        i4 = bArr[i] >> 4;
                    }
                    b = (byte) (i4 & 15);
                    if (b == 15 || (c = convertBcdToChar(b, i3)) == 0) {
                        break;
                    }
                    sb.append(c);
                }
                if (b == 15 || c == 0) {
                    HiLog.error(TAG, "basic convert loop break i %{public}d, convertData %{public}c", Integer.valueOf(i), Character.valueOf(c));
                    return;
                }
                i++;
            }
        }
    }

    private static void addPlusForCalledPartyBCD(String str, StringBuilder sb) {
        if (!"".equals(str)) {
            Matcher matcher = MMI_PATTERN.matcher(str);
            if (!matcher.matches()) {
                Matcher matcher2 = UNUSUAL_PATTERN.matcher(str);
                if (matcher2.matches()) {
                    sb.append(matcher2.group(1));
                    sb.append(matcher2.group(2));
                    sb.append(matcher2.group(3));
                    sb.append("+");
                    sb.append(matcher2.group(4));
                    return;
                }
                sb.append("+");
                sb.append(str);
            } else if ("".equals(matcher.group(2))) {
                sb.append(str);
                sb.append("+");
            } else {
                sb.append(matcher.group(1));
                sb.append(matcher.group(2));
                sb.append(matcher.group(3));
                sb.append("+");
                sb.append(matcher.group(4));
                sb.append(matcher.group(5));
            }
        }
    }

    public static String convertCalledPartyBCDToStringWithTOA(byte[] bArr, int i, int i2, int i3) {
        if (bArr == null || bArr.length == 0) {
            HiLog.error(TAG, "convertCalledPartyBCDToStringWithTOA input data error", new Object[0]);
            return "";
        } else if (i2 < 1 || i < 0 || i >= bArr.length) {
            HiLog.error(TAG, "convertCalledPartyBCDToStringWithTOA length or offset error", new Object[0]);
            return "";
        } else if (i3 == 1 || i3 == 2) {
            int i4 = (i2 * 2) + 1;
            StringBuilder sb = new StringBuilder(i4);
            basicCalledPartyBCDToString(sb, bArr, i + 1, i2 + i, i3);
            if (sb.length() == 0) {
                HiLog.error(TAG, "basicCalledPartyBCDToString output length error", new Object[0]);
                return "";
            }
            StringBuilder sb2 = new StringBuilder(i4);
            if (((bArr[i] >> 4) & 15) != 9) {
                return sb.toString();
            }
            addPlusForCalledPartyBCD(sb.toString(), sb2);
            return sb2.toString();
        } else {
            HiLog.error(TAG, "convertCalledPartyBCDToStringWithTOA bcdExtendedType error", new Object[0]);
            return "";
        }
    }

    public static String convertCalledPartyBCDToStringWithoutTOA(byte[] bArr, int i, int i2, int i3) {
        if (bArr == null || bArr.length == 0) {
            HiLog.error(TAG, "convertCalledPartyBCDToStringWithoutTOA input data error", new Object[0]);
            return "";
        } else if (i2 < 1 || i < 0 || i >= bArr.length) {
            HiLog.error(TAG, "convertCalledPartyBCDToStringWithoutTOA length or offset error", new Object[0]);
            return "";
        } else if (i3 == 1 || i3 == 2) {
            StringBuilder sb = new StringBuilder(i2 * 2);
            basicCalledPartyBCDToString(sb, bArr, i, (i2 + i) - 1, i3);
            return sb.toString();
        } else {
            HiLog.error(TAG, "convertCalledPartyBCDToStringWithTOA bcdExtendedType error", new Object[0]);
            return "";
        }
    }

    public static boolean isEmergencyPhoneNumber(String str) {
        if (str == null) {
            return false;
        }
        return TELEPHONY_PROXY.isEmergencyPhoneNumber(str);
    }

    public static boolean isLocalEmergencyPhoneNumber(String str) {
        if (str == null) {
            return false;
        }
        return isEmergencyPhoneNumber(str);
    }

    public static String formatPhoneNumber(String str) {
        return str == null ? "" : formatPhoneNumber(str, "CN");
    }

    public static String formatPhoneNumber(String str, String str2) {
        return TelephonyAdapt.formatPhoneNumber(str, str2);
    }

    public static String formatPhoneNumber(String str, String str2, String str3) {
        return TelephonyAdapt.formatPhoneNumber(str, str2, str3);
    }

    public static String formatPhoneNumberToE164(String str, String str2) {
        return TelephonyAdapt.formatPhoneNumberToE164(str, str2);
    }

    private static String extractNetworkAddressPortionAlt(String str) {
        if (str == null) {
            return "";
        }
        int length = str.length();
        StringBuilder sb = new StringBuilder(length);
        boolean z = false;
        for (int i = 0; i < length; i++) {
            char charAt = str.charAt(i);
            if (charAt == '+') {
                if (z) {
                    continue;
                } else {
                    z = true;
                }
            }
            if (!isDialableKey(charAt)) {
                if (charAt == ';' || charAt == ',') {
                    break;
                }
                HiLog.warn(TAG, "other perticular input", new Object[0]);
            } else {
                sb.append(charAt);
            }
        }
        return sb.toString();
    }

    public static boolean isVoiceMailNumber(String str, int i) {
        if (str == null) {
            return false;
        }
        String voiceMailNumber = TELEPHONY_PROXY.getVoiceMailNumber(i);
        String extractNetworkAddressPortionAlt = extractNetworkAddressPortionAlt(str);
        if (extractNetworkAddressPortionAlt.length() == 0) {
            return false;
        }
        return comparePhoneNumbers(extractNetworkAddressPortionAlt, voiceMailNumber);
    }

    public static String removeSeparators(String str) {
        if (str == null) {
            return "";
        }
        int length = str.length();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char charAt = str.charAt(i);
            int digit = Character.digit(charAt, 10);
            if (digit != -1) {
                sb.append(digit);
            } else if (isNotSeparator(charAt)) {
                sb.append(charAt);
            } else {
                HiLog.warn(TAG, "other perticular input", new Object[0]);
            }
        }
        return sb.toString();
    }

    private static boolean isDialableAddress(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!isDialableKey(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isSuitableSmsAddress(String str) {
        String extractNetworkAddressPortion = extractNetworkAddressPortion(str);
        return extractNetworkAddressPortion.length() != 0 && !"+".equals(extractNetworkAddressPortion) && isDialableAddress(extractNetworkAddressPortion);
    }

    public static String normalizePhoneNumber(String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int length = str.length();
        for (int i = 0; i < length; i++) {
            char charAt = str.charAt(i);
            int digit = Character.digit(charAt, 10);
            if (digit != -1) {
                sb.append(digit);
            } else if (sb.length() == 0 && charAt == '+') {
                sb.append(charAt);
            } else if ((charAt >= 'A' && charAt <= 'Z') || (charAt >= 'a' && charAt <= 'z')) {
                return normalizePhoneNumber(changeKeypadLettersToDigits(str));
            } else {
                HiLog.warn(TAG, "other perticular input", new Object[0]);
            }
        }
        return sb.toString();
    }

    private static int getFinalNetworkCharIdx(String str) {
        if (str == null || str.length() == 0) {
            return -1;
        }
        int indexOf = str.indexOf(44);
        int indexOf2 = str.indexOf(59);
        if (indexOf > 0 && indexOf2 > 0) {
            return indexOf < indexOf2 ? indexOf - 1 : indexOf2 - 1;
        }
        if (indexOf > 0) {
            return indexOf - 1;
        }
        if (indexOf2 > 0) {
            return indexOf2 - 1;
        }
        return str.length() - 1;
    }

    private static int getNextNonDialableCharIdx(int i, String str) {
        if (!(i <= 0 || str == null || str.length() == 0)) {
            while (i > 0) {
                i--;
                if (isDialableKey(str.charAt(i))) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static int getDialablePhoneLength(String str) {
        if (str == null || str.length() == 0) {
            return 0;
        }
        int length = str.length();
        int i = 0;
        for (int i2 = 0; i2 < length; i2++) {
            if (isDialableKey(str.charAt(i2))) {
                i++;
            }
        }
        return i;
    }

    private static List<PrefixPosition> initInternationalPrefixList(String str, int i) {
        if (str == null || str.length() == 0 || i == 0) {
            return Collections.emptyList();
        }
        LinkedList linkedList = new LinkedList();
        String[] strArr = {"+", "00", "011"};
        for (int i2 = 0; i2 < 3; i2++) {
            PrefixPosition prefixPosition = new PrefixPosition();
            prefixPosition.name = strArr[i2];
            prefixPosition.beginIndex = str.indexOf(prefixPosition.name);
            if (prefixPosition.beginIndex >= 0) {
                prefixPosition.endIndex = (prefixPosition.beginIndex + strArr[i2].length()) - 1;
                linkedList.add(prefixPosition);
            }
        }
        return linkedList;
    }

    private static boolean matchInternationalPrefix(String str, int i) {
        boolean z;
        if (!(str == null || str.length() == 0 || i == 0)) {
            for (PrefixPosition prefixPosition : initInternationalPrefixList(str, i)) {
                if (prefixPosition.beginIndex >= 0 && prefixPosition.endIndex + 1 <= i) {
                    int i2 = prefixPosition.endIndex + 1;
                    while (true) {
                        if (i2 >= i) {
                            z = false;
                            break;
                        } else if (isNotSeparator(str.charAt(i2))) {
                            z = true;
                            break;
                        } else {
                            i2++;
                        }
                    }
                    if (!z) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean matchInternationalPrefixAndCC(String str, int i) {
        boolean z;
        if (!(str == null || str.length() == 0 || i == 0)) {
            for (PrefixPosition prefixPosition : initInternationalPrefixList(str, i)) {
                if (prefixPosition.beginIndex >= 0 && prefixPosition.endIndex + 1 <= i) {
                    int i2 = prefixPosition.beginIndex + 1;
                    int i3 = 0;
                    while (true) {
                        if (i2 >= i) {
                            z = false;
                            break;
                        }
                        char charAt = str.charAt(i2);
                        if (!isLatinDigit(charAt) || i2 <= prefixPosition.endIndex) {
                            if (isNotSeparator(charAt) && i3 != 0) {
                                z = true;
                                break;
                            }
                            HiLog.warn(TAG, "other perticular input", new Object[0]);
                        } else {
                            i3++;
                        }
                        i2++;
                    }
                    if (!z && i3 == 2) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean matchTrunkCode(String str, int i) {
        boolean z = false;
        for (int i2 = 0; i2 < i; i2++) {
            char charAt = str.charAt(i2);
            if (charAt == '0' && !z) {
                z = true;
            } else if (isNotSeparator(charAt)) {
                return false;
            } else {
                HiLog.warn(TAG, "other perticular input", new Object[0]);
            }
        }
        return z;
    }

    private static boolean matchPrefixInPhoneNumber(String str, int i, String str2, int i2) {
        int i3 = i + 1;
        if (matchInternationalPrefix(str, i3) && matchInternationalPrefix(str2, i2 + 1)) {
            return true;
        }
        if (matchTrunkCode(str, i3) && matchInternationalPrefixAndCC(str2, i2 + 1)) {
            return true;
        }
        if (!matchTrunkCode(str2, i2 + 1) || !matchInternationalPrefixAndCC(str, i3)) {
            return false;
        }
        return true;
    }

    private static boolean comparePhoneNumbers(String str, String str2, int i) {
        if (!(str == null || str2 == null || str.length() == 0 || str2.length() == 0)) {
            int length = str.length();
            int length2 = str2.length();
            int finalNetworkCharIdx = getFinalNetworkCharIdx(str);
            int finalNetworkCharIdx2 = getFinalNetworkCharIdx(str2);
            int i2 = 0;
            while (finalNetworkCharIdx >= 0 && finalNetworkCharIdx2 >= 0 && finalNetworkCharIdx < length && finalNetworkCharIdx2 < length2) {
                char charAt = str.charAt(finalNetworkCharIdx);
                char charAt2 = str2.charAt(finalNetworkCharIdx2);
                if (charAt != charAt2 && charAt != 'N' && charAt2 != 'N') {
                    break;
                }
                i2++;
                finalNetworkCharIdx = getNextNonDialableCharIdx(finalNetworkCharIdx, str);
                finalNetworkCharIdx2 = getNextNonDialableCharIdx(finalNetworkCharIdx2, str2);
            }
            if (i2 < i) {
                int dialablePhoneLength = getDialablePhoneLength(str);
                if (dialablePhoneLength == getDialablePhoneLength(str2) && dialablePhoneLength == i2) {
                    return true;
                }
                return false;
            } else if ((i2 >= i && (finalNetworkCharIdx < 0 || finalNetworkCharIdx2 < 0)) || matchPrefixInPhoneNumber(str, finalNetworkCharIdx, str2, finalNetworkCharIdx2)) {
                return true;
            }
        }
        return false;
    }

    public static boolean comparePhoneNumbers(String str, String str2) {
        return comparePhoneNumbers(str, str2, 7);
    }

    public static String changeKeypadLettersToDigits(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        char[] cArr = new char[str.length()];
        for (int i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            if (L2D_MAP.get(Character.valueOf(charAt)) != null) {
                cArr[i] = L2D_MAP.get(Character.valueOf(charAt)).charValue();
            } else {
                cArr[i] = charAt;
            }
        }
        return new String(cArr);
    }

    public static String extractNetworkAddressPortion(String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        char[] charArray = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            int digit = Character.digit(c, 10);
            if (digit != -1) {
                sb.append(digit);
            } else if (c == '+') {
                String str2 = new String(sb);
                if (i == 0 || str2.equals(CLIR_PREFIX_ON) || str2.equals(CLIR_PREFIX_OFF)) {
                    sb.append(c);
                }
            } else if (isDialableKey(c)) {
                sb.append(c);
            } else if (isBeginPostDialKey(c)) {
                break;
            } else {
                HiLog.warn(TAG, "other perticular input", new Object[0]);
            }
        }
        return new String(sb);
    }

    public static String extractNetworkAddressPortionPlus(String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        char[] charArray = str.toCharArray();
        int length = charArray.length;
        StringBuilder sb = new StringBuilder(length);
        boolean z = false;
        for (char c : charArray) {
            if (c == '+') {
                if (z) {
                    continue;
                } else {
                    z = true;
                }
            }
            if (isDialableKey(c)) {
                sb.append(c);
            }
            if (isBeginPostDialKey(c)) {
                break;
            }
        }
        return sb.toString();
    }

    public static String extractPostDialPortion(String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        int finalNetworkCharIdx = getFinalNetworkCharIdx(str);
        StringBuilder sb = new StringBuilder();
        while (true) {
            finalNetworkCharIdx++;
            if (finalNetworkCharIdx >= str.length()) {
                return sb.toString();
            }
            char charAt = str.charAt(finalNetworkCharIdx);
            if (isNotSeparator(charAt)) {
                sb.append(charAt);
            }
        }
    }

    public static String convertPhoneNumber(String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        char[] charArray = str.toCharArray();
        int length = charArray.length;
        StringBuilder sb = new StringBuilder(length);
        for (char c : charArray) {
            if (isPause(c)) {
                c = PAUSE;
            } else if (isToneWait(c)) {
                c = WAIT;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public static String getCountryIsoFromDbNumber(String str) {
        return TelephonyAdapt.getCountryIsoFromDbNumber(str);
    }

    @SystemApi
    public static int getCallerIndex(ResultSet resultSet, String str) {
        return TelephonyAdapt.getCallerIndex(resultSet, str);
    }
}
