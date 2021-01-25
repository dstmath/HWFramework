package android.telephony;

import android.annotation.UnsupportedAppUsage;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.CountryDetector;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.provider.Contacts;
import android.provider.SettingsStringUtil;
import android.telecom.PhoneAccount;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.TtsSpan;
import android.util.SparseIntArray;
import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.Phonenumber;
import com.android.internal.R;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.midi.MidiConstants;
import com.android.internal.telephony.TelephonyProperties;
import com.android.internal.transition.EpicenterTranslateClipReveal;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumberUtils {
    private static final String BCD_CALLED_PARTY_EXTENDED = "*#abc";
    private static final String BCD_EF_ADN_EXTENDED = "*#,N;";
    public static final int BCD_EXTENDED_TYPE_CALLED_PARTY = 2;
    public static final int BCD_EXTENDED_TYPE_EF_ADN = 1;
    private static final int CCC_LENGTH = COUNTRY_CALLING_CALL.length;
    private static final String CLIR_OFF = "#31#";
    private static final String CLIR_ON = "*31#";
    private static final boolean[] COUNTRY_CALLING_CALL = {true, true, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, false, false, false, false, true, true, false, true, true, true, true, true, false, true, false, false, true, true, false, false, true, true, true, true, true, true, true, false, true, true, true, true, true, true, true, true, false, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, false, true, false, false, true, true, true, true, true, true, true, false, false, true, false};
    private static final boolean DBG = false;
    public static final int FORMAT_JAPAN = 2;
    public static final int FORMAT_NANP = 1;
    public static final int FORMAT_UNKNOWN = 0;
    private static final Pattern GLOBAL_PHONE_NUMBER_PATTERN = Pattern.compile("[\\+]?[0-9.-]+");
    private static final String JAPAN_ISO_COUNTRY_CODE = "JP";
    private static final SparseIntArray KEYPAD_MAP = new SparseIntArray();
    private static final String KOREA_ISO_COUNTRY_CODE = "KR";
    static final String LOG_TAG = "PhoneNumberUtils";
    @UnsupportedAppUsage
    static final int MIN_MATCH = 7;
    private static final String[] NANP_COUNTRIES = {"US", "CA", "AS", "AI", "AG", "BS", "BB", "BM", "VG", "KY", "DM", "DO", "GD", "GU", "JM", "PR", "MS", "MP", "KN", "LC", "VC", "TT", "TC", "VI"};
    private static final String NANP_IDP_STRING = "011";
    private static final int NANP_LENGTH = 10;
    private static final int NANP_STATE_DASH = 4;
    private static final int NANP_STATE_DIGIT = 1;
    private static final int NANP_STATE_ONE = 3;
    private static final int NANP_STATE_PLUS = 2;
    public static final char PAUSE = ',';
    private static final char PLUS_SIGN_CHAR = '+';
    private static final String PLUS_SIGN_STRING = "+";
    public static final int TOA_International = 145;
    public static final int TOA_Unknown = 129;
    public static final char WAIT = ';';
    public static final char WILD = 'N';
    private static String[] sConvertToEmergencyMap = null;

    @Retention(RetentionPolicy.SOURCE)
    public @interface BcdExtendType {
    }

    static {
        KEYPAD_MAP.put(97, 50);
        KEYPAD_MAP.put(98, 50);
        KEYPAD_MAP.put(99, 50);
        KEYPAD_MAP.put(65, 50);
        KEYPAD_MAP.put(66, 50);
        KEYPAD_MAP.put(67, 50);
        KEYPAD_MAP.put(100, 51);
        KEYPAD_MAP.put(101, 51);
        KEYPAD_MAP.put(102, 51);
        KEYPAD_MAP.put(68, 51);
        KEYPAD_MAP.put(69, 51);
        KEYPAD_MAP.put(70, 51);
        KEYPAD_MAP.put(103, 52);
        KEYPAD_MAP.put(104, 52);
        KEYPAD_MAP.put(105, 52);
        KEYPAD_MAP.put(71, 52);
        KEYPAD_MAP.put(72, 52);
        KEYPAD_MAP.put(73, 52);
        KEYPAD_MAP.put(106, 53);
        KEYPAD_MAP.put(107, 53);
        KEYPAD_MAP.put(108, 53);
        KEYPAD_MAP.put(74, 53);
        KEYPAD_MAP.put(75, 53);
        KEYPAD_MAP.put(76, 53);
        KEYPAD_MAP.put(109, 54);
        KEYPAD_MAP.put(110, 54);
        KEYPAD_MAP.put(111, 54);
        KEYPAD_MAP.put(77, 54);
        KEYPAD_MAP.put(78, 54);
        KEYPAD_MAP.put(79, 54);
        KEYPAD_MAP.put(112, 55);
        KEYPAD_MAP.put(113, 55);
        KEYPAD_MAP.put(114, 55);
        KEYPAD_MAP.put(115, 55);
        KEYPAD_MAP.put(80, 55);
        KEYPAD_MAP.put(81, 55);
        KEYPAD_MAP.put(82, 55);
        KEYPAD_MAP.put(83, 55);
        KEYPAD_MAP.put(116, 56);
        KEYPAD_MAP.put(117, 56);
        KEYPAD_MAP.put(118, 56);
        KEYPAD_MAP.put(84, 56);
        KEYPAD_MAP.put(85, 56);
        KEYPAD_MAP.put(86, 56);
        KEYPAD_MAP.put(119, 57);
        KEYPAD_MAP.put(120, 57);
        KEYPAD_MAP.put(121, 57);
        KEYPAD_MAP.put(122, 57);
        KEYPAD_MAP.put(87, 57);
        KEYPAD_MAP.put(88, 57);
        KEYPAD_MAP.put(89, 57);
        KEYPAD_MAP.put(90, 57);
    }

    public static String convertPlusByMcc(String number, int mcc) {
        return HwFrameworkFactory.getHwInnerTelephonyManager().convertPlusByMcc(number, mcc);
    }

    public static boolean isISODigit(char c) {
        return c >= '0' && c <= '9';
    }

    public static final boolean is12Key(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#';
    }

    public static final boolean isDialable(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == '+' || c == 'N';
    }

    public static final boolean isReallyDialable(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == '+';
    }

    public static final boolean isNonSeparator(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == '+' || c == 'N' || c == ';' || c == ',';
    }

    public static final boolean isStartsPostDial(char c) {
        return c == ',' || c == ';';
    }

    private static boolean isPause(char c) {
        return c == 'p' || c == 'P';
    }

    private static boolean isToneWait(char c) {
        return c == 'w' || c == 'W';
    }

    private static boolean isSeparator(char ch) {
        return !isDialable(ch) && ('a' > ch || ch > 'z') && ('A' > ch || ch > 'Z');
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x006c, code lost:
        if (r12 != null) goto L_0x006e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x006e, code lost:
        r12.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x007d, code lost:
        if (0 == 0) goto L_0x0080;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0080, code lost:
        return r0;
     */
    public static String getNumberFromIntent(Intent intent, Context context) {
        String phoneColumn;
        String number = null;
        Uri uri = intent.getData();
        if (uri == null) {
            return null;
        }
        String scheme = uri.getScheme();
        if (scheme.equals(PhoneAccount.SCHEME_TEL) || scheme.equals("sip")) {
            return uri.getSchemeSpecificPart();
        }
        if (context == null) {
            return null;
        }
        intent.resolveType(context);
        String authority = uri.getAuthority();
        if (Contacts.AUTHORITY.equals(authority)) {
            phoneColumn = "number";
        } else if ("com.android.contacts".equals(authority)) {
            phoneColumn = "data1";
        } else {
            phoneColumn = null;
        }
        Cursor c = null;
        try {
            c = context.getContentResolver().query(uri, new String[]{phoneColumn}, null, null, null);
            if (c != null && c.moveToFirst()) {
                number = c.getString(c.getColumnIndex(phoneColumn));
            }
        } catch (RuntimeException e) {
            Rlog.e(LOG_TAG, "Error getting phone number.", e);
        } catch (Throwable th) {
            if (0 != 0) {
                c.close();
            }
            throw th;
        }
    }

    public static String extractNetworkPortion(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            int digit = Character.digit(c, 10);
            if (digit != -1) {
                ret.append(digit);
            } else if (c == '+') {
                String prefix = ret.toString();
                if (prefix.length() == 0 || prefix.equals(CLIR_ON) || prefix.equals(CLIR_OFF)) {
                    ret.append(c);
                }
            } else if (isDialable(c)) {
                ret.append(c);
            } else if (isStartsPostDial(c)) {
                break;
            }
        }
        return ret.toString();
    }

    @UnsupportedAppUsage
    public static String extractNetworkPortionAlt(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);
        boolean haveSeenPlus = false;
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            if (c == '+') {
                if (haveSeenPlus) {
                    continue;
                } else {
                    haveSeenPlus = true;
                }
            }
            if (isDialable(c)) {
                ret.append(c);
            } else if (isStartsPostDial(c)) {
                break;
            }
        }
        return ret.toString();
    }

    public static String stripSeparators(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            int digit = Character.digit(c, 10);
            if (digit != -1) {
                ret.append(digit);
            } else if (isNonSeparator(c)) {
                ret.append(c);
            }
        }
        return ret.toString();
    }

    public static String convertAndStrip(String phoneNumber) {
        return stripSeparators(convertKeypadLettersToDigits(phoneNumber));
    }

    @UnsupportedAppUsage
    public static String convertPreDial(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            if (isPause(c)) {
                c = ',';
            } else if (isToneWait(c)) {
                c = ';';
            }
            ret.append(c);
        }
        return ret.toString();
    }

    private static int minPositive(int a, int b) {
        if (a >= 0 && b >= 0) {
            return a < b ? a : b;
        }
        if (a >= 0) {
            return a;
        }
        if (b >= 0) {
            return b;
        }
        return -1;
    }

    private static void log(String msg) {
        Rlog.i(LOG_TAG, msg);
    }

    private static int indexOfLastNetworkChar(String a) {
        int origLength = a.length();
        int trimIndex = minPositive(a.indexOf(44), a.indexOf(59));
        if (trimIndex < 0) {
            return origLength - 1;
        }
        return trimIndex - 1;
    }

    public static String extractPostDialPortion(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        StringBuilder ret = new StringBuilder();
        int s = phoneNumber.length();
        for (int i = indexOfLastNetworkChar(phoneNumber) + 1; i < s; i++) {
            char c = phoneNumber.charAt(i);
            if (isNonSeparator(c)) {
                ret.append(c);
            }
        }
        return ret.toString();
    }

    public static boolean compare(String a, String b) {
        return compare(a, b, false);
    }

    public static boolean compare(Context context, String a, String b) {
        return compare(a, b, context.getResources().getBoolean(R.bool.config_use_strict_phone_number_comparation));
    }

    @UnsupportedAppUsage
    public static boolean compare(String a, String b, boolean useStrictComparation) {
        return useStrictComparation ? compareStrictly(a, b) : compareLoosely(a, b);
    }

    @UnsupportedAppUsage
    public static boolean compareLoosely(String a, String b) {
        int numNonDialableCharsInA = 0;
        int numNonDialableCharsInB = 0;
        if (a == null || b == null) {
            return a == b;
        }
        if (a.length() == 0 || b.length() == 0) {
            return false;
        }
        int ia = indexOfLastNetworkChar(a);
        int ib = indexOfLastNetworkChar(b);
        int matched = 0;
        while (ia >= 0 && ib >= 0) {
            boolean skipCmp = false;
            char ca = a.charAt(ia);
            if (!isDialable(ca)) {
                ia--;
                skipCmp = true;
                numNonDialableCharsInA++;
            }
            char cb = b.charAt(ib);
            if (!isDialable(cb)) {
                ib--;
                skipCmp = true;
                numNonDialableCharsInB++;
            }
            if (!skipCmp) {
                if (cb != ca && ca != 'N' && cb != 'N') {
                    break;
                }
                ia--;
                ib--;
                matched++;
            }
        }
        if (matched < 7) {
            int effectiveALen = a.length() - numNonDialableCharsInA;
            return effectiveALen == b.length() - numNonDialableCharsInB && effectiveALen == matched;
        } else if (matched >= 7 && (ia < 0 || ib < 0)) {
            return true;
        } else {
            if (matchIntlPrefix(a, ia + 1) && matchIntlPrefix(b, ib + 1)) {
                return true;
            }
            if (!matchTrunkPrefix(a, ia + 1) || !matchIntlPrefixAndCC(b, ib + 1)) {
                return matchTrunkPrefix(b, ib + 1) && matchIntlPrefixAndCC(a, ia + 1);
            }
            return true;
        }
    }

    @UnsupportedAppUsage
    public static boolean compareStrictly(String a, String b) {
        return compareStrictly(a, b, true);
    }

    @UnsupportedAppUsage
    public static boolean compareStrictly(String a, String b, boolean acceptInvalidCCCPrefix) {
        boolean z;
        if (a == null) {
            z = true;
        } else if (b == null) {
            z = true;
        } else if (a.length() == 0 && b.length() == 0) {
            return false;
        } else {
            int forwardIndexA = 0;
            int forwardIndexB = 0;
            CountryCallingCodeAndNewIndex cccA = tryGetCountryCallingCodeAndNewIndex(a, acceptInvalidCCCPrefix);
            CountryCallingCodeAndNewIndex cccB = tryGetCountryCallingCodeAndNewIndex(b, acceptInvalidCCCPrefix);
            boolean bothHasCountryCallingCode = false;
            boolean okToIgnorePrefix = true;
            boolean trunkPrefixIsOmittedA = false;
            boolean trunkPrefixIsOmittedB = false;
            if (cccA == null || cccB == null) {
                if (cccA == null && cccB == null) {
                    okToIgnorePrefix = false;
                } else {
                    if (cccA != null) {
                        forwardIndexA = cccA.newIndex;
                    } else {
                        int tmp = tryGetTrunkPrefixOmittedIndex(b, 0);
                        if (tmp >= 0) {
                            forwardIndexA = tmp;
                            trunkPrefixIsOmittedA = true;
                        }
                    }
                    if (cccB != null) {
                        forwardIndexB = cccB.newIndex;
                    } else {
                        int tmp2 = tryGetTrunkPrefixOmittedIndex(b, 0);
                        if (tmp2 >= 0) {
                            forwardIndexB = tmp2;
                            trunkPrefixIsOmittedB = true;
                        }
                    }
                }
            } else if (cccA.countryCallingCode != cccB.countryCallingCode) {
                return false;
            } else {
                okToIgnorePrefix = false;
                bothHasCountryCallingCode = true;
                forwardIndexA = cccA.newIndex;
                forwardIndexB = cccB.newIndex;
            }
            int backwardIndexA = a.length() - 1;
            int backwardIndexB = b.length() - 1;
            while (backwardIndexA >= forwardIndexA && backwardIndexB >= forwardIndexB) {
                boolean skip_compare = false;
                char chA = a.charAt(backwardIndexA);
                char chB = b.charAt(backwardIndexB);
                if (isSeparator(chA)) {
                    backwardIndexA--;
                    skip_compare = true;
                }
                if (isSeparator(chB)) {
                    backwardIndexB--;
                    skip_compare = true;
                }
                if (!skip_compare) {
                    if (chA != chB) {
                        return false;
                    }
                    backwardIndexA--;
                    backwardIndexB--;
                }
            }
            if (!okToIgnorePrefix) {
                boolean maybeNamp = !bothHasCountryCallingCode;
                while (backwardIndexA >= forwardIndexA) {
                    char chA2 = a.charAt(backwardIndexA);
                    if (isDialable(chA2)) {
                        if (!maybeNamp || tryGetISODigit(chA2) != 1) {
                            return false;
                        }
                        maybeNamp = false;
                    }
                    backwardIndexA--;
                }
                while (backwardIndexB >= forwardIndexB) {
                    char chB2 = b.charAt(backwardIndexB);
                    if (isDialable(chB2)) {
                        if (!maybeNamp || tryGetISODigit(chB2) != 1) {
                            return false;
                        }
                        maybeNamp = false;
                    }
                    backwardIndexB--;
                }
                return true;
            } else if ((!trunkPrefixIsOmittedA || forwardIndexA > backwardIndexA) && checkPrefixIsIgnorable(a, forwardIndexA, backwardIndexA)) {
                if ((!trunkPrefixIsOmittedB || forwardIndexB > backwardIndexB) && checkPrefixIsIgnorable(b, forwardIndexA, backwardIndexB)) {
                    return true;
                }
                if (acceptInvalidCCCPrefix) {
                    return compare(a, b, false);
                }
                return false;
            } else if (acceptInvalidCCCPrefix) {
                return compare(a, b, false);
            } else {
                return false;
            }
        }
        if (a == b) {
            return z;
        }
        return false;
    }

    public static String toCallerIDMinMatch(String phoneNumber) {
        return internalGetStrippedReversed(extractNetworkPortionAlt(phoneNumber), 7);
    }

    public static String getStrippedReversed(String phoneNumber) {
        String np = extractNetworkPortionAlt(phoneNumber);
        if (np == null) {
            return null;
        }
        return internalGetStrippedReversed(np, np.length());
    }

    private static String internalGetStrippedReversed(String np, int numDigits) {
        if (np == null) {
            return null;
        }
        StringBuilder ret = new StringBuilder(numDigits);
        int length = np.length();
        int i = length - 1;
        while (i >= 0 && length - i <= numDigits) {
            ret.append(np.charAt(i));
            i--;
        }
        return ret.toString();
    }

    public static String stringFromStringAndTOA(String s, int TOA) {
        if (s == null) {
            return null;
        }
        if (TOA != 145 || s.length() <= 0 || s.charAt(0) == '+') {
            return s;
        }
        return "+" + s;
    }

    public static int toaFromString(String s) {
        if (s == null || s.length() <= 0 || s.charAt(0) != '+') {
            return 129;
        }
        return 145;
    }

    @Deprecated
    public static String calledPartyBCDToString(byte[] bytes, int offset, int length) {
        return calledPartyBCDToString(bytes, offset, length, 1);
    }

    public static String calledPartyBCDToString(byte[] bytes, int offset, int length, int bcdExtType) {
        boolean prependPlus = false;
        StringBuilder ret = new StringBuilder((length * 2) + 1);
        if (length < 2) {
            return "";
        }
        if ((bytes[offset] & 240) == 144) {
            prependPlus = true;
        }
        internalCalledPartyBCDFragmentToString(ret, bytes, offset + 1, length - 1, bcdExtType);
        if (prependPlus && ret.length() == 0) {
            return "";
        }
        if (prependPlus) {
            String retString = ret.toString();
            Matcher m = Pattern.compile("(^[#*])(.*)([#*])(.*)(#)$").matcher(retString);
            if (!m.matches()) {
                Matcher m2 = Pattern.compile("(^[#*])(.*)([#*])(.*)").matcher(retString);
                if (m2.matches()) {
                    ret = new StringBuilder();
                    ret.append(m2.group(1));
                    ret.append(m2.group(2));
                    ret.append(m2.group(3));
                    ret.append("+");
                    ret.append(m2.group(4));
                } else {
                    ret = new StringBuilder();
                    ret.append(PLUS_SIGN_CHAR);
                    ret.append(retString);
                }
            } else if ("".equals(m.group(2))) {
                ret = new StringBuilder();
                ret.append(m.group(1));
                ret.append(m.group(3));
                ret.append(m.group(4));
                ret.append(m.group(5));
                ret.append("+");
            } else {
                ret = new StringBuilder();
                ret.append(m.group(1));
                ret.append(m.group(2));
                ret.append(m.group(3));
                ret.append("+");
                ret.append(m.group(4));
                ret.append(m.group(5));
            }
        }
        return ret.toString();
    }

    private static void internalCalledPartyBCDFragmentToString(StringBuilder sb, byte[] bytes, int offset, int length, int bcdExtType) {
        char c;
        char c2;
        int i = offset;
        while (i < length + offset && (c = bcdToChar((byte) (bytes[i] & MidiConstants.STATUS_CHANNEL_MASK), bcdExtType)) != 0) {
            sb.append(c);
            byte b = (byte) ((bytes[i] >> 4) & 15);
            if ((b != 15 || i + 1 != length + offset) && (c2 = bcdToChar(b, bcdExtType)) != 0) {
                sb.append(c2);
                i++;
            } else {
                return;
            }
        }
    }

    @Deprecated
    public static String calledPartyBCDFragmentToString(byte[] bytes, int offset, int length) {
        return calledPartyBCDFragmentToString(bytes, offset, length, 1);
    }

    public static String calledPartyBCDFragmentToString(byte[] bytes, int offset, int length, int bcdExtType) {
        StringBuilder ret = new StringBuilder(length * 2);
        internalCalledPartyBCDFragmentToString(ret, bytes, offset, length, bcdExtType);
        return ret.toString();
    }

    private static char bcdToChar(byte b, int bcdExtType) {
        if (b < 10) {
            return (char) (b + 48);
        }
        String extended = null;
        if (1 == bcdExtType) {
            extended = BCD_EF_ADN_EXTENDED;
        } else if (2 == bcdExtType) {
            extended = BCD_CALLED_PARTY_EXTENDED;
        }
        if (extended == null || b - 10 >= extended.length()) {
            return 0;
        }
        return extended.charAt(b - 10);
    }

    private static int charToBCD(char c, int bcdExtType) {
        if ('0' <= c && c <= '9') {
            return c - '0';
        }
        String extended = null;
        if (1 == bcdExtType) {
            extended = BCD_EF_ADN_EXTENDED;
        } else if (2 == bcdExtType) {
            extended = BCD_CALLED_PARTY_EXTENDED;
        }
        if (extended != null && extended.indexOf(c) != -1) {
            return extended.indexOf(c) + 10;
        }
        throw new RuntimeException("invalid char for BCD " + c);
    }

    public static boolean isWellFormedSmsAddress(String address) {
        String networkPortion = extractNetworkPortion(address);
        return !networkPortion.equals("+") && !TextUtils.isEmpty(networkPortion) && isDialable(networkPortion);
    }

    public static boolean isGlobalPhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return false;
        }
        return GLOBAL_PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches();
    }

    private static boolean isDialable(String address) {
        int count = address.length();
        for (int i = 0; i < count; i++) {
            if (!isDialable(address.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isNonSeparator(String address) {
        int count = address.length();
        for (int i = 0; i < count; i++) {
            if (!isNonSeparator(address.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static byte[] networkPortionToCalledPartyBCD(String s) {
        return numberToCalledPartyBCDHelper(extractNetworkPortion(s), false, 1);
    }

    public static byte[] networkPortionToCalledPartyBCDWithLength(String s) {
        return numberToCalledPartyBCDHelper(extractNetworkPortion(s), true, 1);
    }

    @Deprecated
    public static byte[] numberToCalledPartyBCD(String number) {
        return numberToCalledPartyBCD(number, 1);
    }

    public static byte[] numberToCalledPartyBCD(String number, int bcdExtType) {
        return numberToCalledPartyBCDHelper(number, false, bcdExtType);
    }

    private static byte[] numberToCalledPartyBCDHelper(String number, boolean includeLength, int bcdExtType) {
        int numberLenReal = number.length();
        int numberLenEffective = numberLenReal;
        char c = PLUS_SIGN_CHAR;
        boolean hasPlus = number.indexOf(43) != -1;
        if (hasPlus) {
            numberLenEffective--;
        }
        if (numberLenEffective == 0) {
            return null;
        }
        int resultLen = (numberLenEffective + 1) / 2;
        int extraBytes = 1;
        if (includeLength) {
            extraBytes = 1 + 1;
        }
        int resultLen2 = resultLen + extraBytes;
        byte[] result = new byte[resultLen2];
        int digitCount = 0;
        int i = 0;
        while (i < numberLenReal) {
            char c2 = number.charAt(i);
            if (c2 != c) {
                int i2 = (digitCount >> 1) + extraBytes;
                result[i2] = (byte) (((byte) ((charToBCD(c2, bcdExtType) & 15) << ((digitCount & 1) == 1 ? 4 : 0))) | result[i2]);
                digitCount++;
            }
            i++;
            c = PLUS_SIGN_CHAR;
        }
        if ((digitCount & 1) == 1) {
            int i3 = (digitCount >> 1) + extraBytes;
            result[i3] = (byte) (result[i3] | 240);
        }
        int offset = 0;
        if (includeLength) {
            result[0] = (byte) (resultLen2 - 1);
            offset = 0 + 1;
        }
        result[offset] = (byte) (hasPlus ? 145 : 129);
        return result;
    }

    @Deprecated
    public static String formatNumber(String source) {
        SpannableStringBuilder text = new SpannableStringBuilder(source);
        formatNumber(text, getFormatTypeForLocale(Locale.getDefault()));
        return text.toString();
    }

    @UnsupportedAppUsage
    @Deprecated
    public static String formatNumber(String source, int defaultFormattingType) {
        SpannableStringBuilder text = new SpannableStringBuilder(source);
        formatNumber(text, defaultFormattingType);
        return text.toString();
    }

    @Deprecated
    public static int getFormatTypeForLocale(Locale locale) {
        return getFormatTypeFromCountryCode(locale.getCountry());
    }

    @Deprecated
    public static void formatNumber(Editable text, int defaultFormattingType) {
        int formatType = defaultFormattingType;
        if (text.length() > 2 && text.charAt(0) == '+') {
            formatType = text.charAt(1) == '1' ? 1 : (text.length() >= 3 && text.charAt(1) == '8' && text.charAt(2) == '1') ? 2 : 0;
        }
        if (formatType == 0) {
            removeDashes(text);
        } else if (formatType == 1) {
            formatNanpNumber(text);
        } else if (formatType == 2) {
            formatJapaneseNumber(text);
        }
    }

    @Deprecated
    public static void formatNanpNumber(Editable text) {
        int numDashes;
        int length = text.length();
        if (length <= "+1-nnn-nnn-nnnn".length() && length > 5) {
            CharSequence saved = text.subSequence(0, length);
            removeDashes(text);
            int length2 = text.length();
            int[] dashPositions = new int[3];
            int state = 0;
            int state2 = 1;
            int numDigits = 0;
            for (int i = 0; i < length2; i++) {
                char c = text.charAt(i);
                if (c != '+') {
                    if (c != '-') {
                        switch (c) {
                            case '1':
                                if (numDigits == 0 || state2 == 2) {
                                    state2 = 3;
                                    break;
                                }
                            case '0':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                if (state2 != 2) {
                                    if (state2 == 3) {
                                        numDashes = state + 1;
                                        dashPositions[state] = i;
                                    } else if (state2 == 4 || !(numDigits == 3 || numDigits == 6)) {
                                        numDashes = state;
                                    } else {
                                        numDashes = state + 1;
                                        dashPositions[state] = i;
                                    }
                                    numDigits++;
                                    state2 = 1;
                                    state = numDashes;
                                    break;
                                } else {
                                    text.replace(0, length2, saved);
                                    return;
                                }
                            default:
                                text.replace(0, length2, saved);
                                return;
                        }
                    } else {
                        state2 = 4;
                    }
                } else if (i == 0) {
                    state2 = 2;
                } else {
                    text.replace(0, length2, saved);
                    return;
                }
            }
            if (numDigits == 7) {
                state--;
            }
            for (int i2 = 0; i2 < state; i2++) {
                int pos = dashPositions[i2];
                text.replace(pos + i2, pos + i2, NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
            }
            int len = text.length();
            while (len > 0 && text.charAt(len - 1) == '-') {
                text.delete(len - 1, len);
                len--;
            }
        }
    }

    @Deprecated
    public static void formatJapaneseNumber(Editable text) {
        JapanesePhoneNumberFormatter.format(text);
    }

    private static void removeDashes(Editable text) {
        int p = 0;
        while (p < text.length()) {
            if (text.charAt(p) == '-') {
                text.delete(p, p + 1);
            } else {
                p++;
            }
        }
    }

    public static String formatNumberToE164(String phoneNumber, String defaultCountryIso) {
        return formatNumberInternal(phoneNumber, defaultCountryIso, PhoneNumberUtil.PhoneNumberFormat.E164);
    }

    public static String formatNumberToRFC3966(String phoneNumber, String defaultCountryIso) {
        return formatNumberInternal(phoneNumber, defaultCountryIso, PhoneNumberUtil.PhoneNumberFormat.RFC3966);
    }

    private static String formatNumberInternal(String rawPhoneNumber, String defaultCountryIso, PhoneNumberUtil.PhoneNumberFormat formatIdentifier) {
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber phoneNumber = util.parse(rawPhoneNumber, defaultCountryIso);
            if (util.isValidNumber(phoneNumber)) {
                return util.format(phoneNumber, formatIdentifier);
            }
            return null;
        } catch (NumberParseException e) {
            return null;
        }
    }

    public static boolean isInternationalNumber(String phoneNumber, String defaultCountryIso) {
        if (TextUtils.isEmpty(phoneNumber) || phoneNumber.startsWith("#") || phoneNumber.startsWith("*")) {
            return false;
        }
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        try {
            if (util.parseAndKeepRawInput(phoneNumber, defaultCountryIso).getCountryCode() != util.getCountryCodeForRegion(defaultCountryIso)) {
                return true;
            }
            return false;
        } catch (NumberParseException e) {
            return false;
        }
    }

    public static String formatNumber(String phoneNumber, String defaultCountryIso) {
        if (phoneNumber.startsWith("#") || phoneNumber.startsWith("*")) {
            return phoneNumber;
        }
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber pn = util.parseAndKeepRawInput(phoneNumber, defaultCountryIso);
            if (KOREA_ISO_COUNTRY_CODE.equalsIgnoreCase(defaultCountryIso) && pn.getCountryCode() == util.getCountryCodeForRegion(KOREA_ISO_COUNTRY_CODE) && pn.getCountryCodeSource() == Phonenumber.PhoneNumber.CountryCodeSource.FROM_NUMBER_WITH_PLUS_SIGN) {
                return util.format(pn, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
            }
            if (JAPAN_ISO_COUNTRY_CODE.equalsIgnoreCase(defaultCountryIso) && pn.getCountryCode() == util.getCountryCodeForRegion(JAPAN_ISO_COUNTRY_CODE) && pn.getCountryCodeSource() == Phonenumber.PhoneNumber.CountryCodeSource.FROM_NUMBER_WITH_PLUS_SIGN) {
                return util.format(pn, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
            }
            return util.formatInOriginalFormat(pn, defaultCountryIso);
        } catch (NumberParseException e) {
            return null;
        }
    }

    public static String formatNumber(String phoneNumber, String phoneNumberE164, String defaultCountryIso) {
        int len = phoneNumber.length();
        for (int i = 0; i < len; i++) {
            if (!isDialable(phoneNumber.charAt(i))) {
                return phoneNumber;
            }
        }
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        if (phoneNumberE164 != null && phoneNumberE164.length() >= 2 && phoneNumberE164.charAt(0) == '+') {
            try {
                String regionCode = util.getRegionCodeForNumber(util.parse(phoneNumberE164, "ZZ"));
                if (!TextUtils.isEmpty(regionCode) && normalizeNumber(phoneNumber).indexOf(phoneNumberE164.substring(1)) <= 0) {
                    defaultCountryIso = regionCode;
                }
            } catch (NumberParseException e) {
            }
        }
        String result = formatNumber(phoneNumber, defaultCountryIso);
        return result != null ? result : phoneNumber;
    }

    public static String normalizeNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int len = phoneNumber.length();
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            int digit = Character.digit(c, 10);
            if (digit != -1) {
                sb.append(digit);
            } else if (sb.length() == 0 && c == '+') {
                sb.append(c);
            } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                return normalizeNumber(convertKeypadLettersToDigits(phoneNumber));
            }
        }
        return sb.toString();
    }

    public static String replaceUnicodeDigits(String number) {
        StringBuilder normalizedDigits = new StringBuilder(number.length());
        char[] charArray = number.toCharArray();
        for (char c : charArray) {
            int digit = Character.digit(c, 10);
            if (digit != -1) {
                normalizedDigits.append(digit);
            } else {
                normalizedDigits.append(c);
            }
        }
        return normalizedDigits.toString();
    }

    @Deprecated
    public static boolean isEmergencyNumber(String number) {
        return isEmergencyNumber(getDefaultVoiceSubId(), number);
    }

    @UnsupportedAppUsage
    @Deprecated
    public static boolean isEmergencyNumber(int subId, String number) {
        return isEmergencyNumberInternal(subId, number, true);
    }

    @Deprecated
    public static boolean isPotentialEmergencyNumber(String number) {
        return isPotentialEmergencyNumber(getDefaultVoiceSubId(), number);
    }

    @UnsupportedAppUsage
    @Deprecated
    public static boolean isPotentialEmergencyNumber(int subId, String number) {
        return isEmergencyNumberInternal(subId, number, false);
    }

    private static boolean isEmergencyNumberInternal(String number, boolean useExactMatch) {
        return isEmergencyNumberInternal(getDefaultVoiceSubId(), number, useExactMatch);
    }

    private static boolean isEmergencyNumberInternal(int subId, String number, boolean useExactMatch) {
        return isEmergencyNumberInternal(subId, number, null, useExactMatch);
    }

    @UnsupportedAppUsage
    @Deprecated
    public static boolean isEmergencyNumber(String number, String defaultCountryIso) {
        return isEmergencyNumber(getDefaultVoiceSubId(), number, defaultCountryIso);
    }

    @Deprecated
    public static boolean isEmergencyNumber(int subId, String number, String defaultCountryIso) {
        return isEmergencyNumberInternal(subId, number, defaultCountryIso, true);
    }

    @Deprecated
    public static boolean isPotentialEmergencyNumber(String number, String defaultCountryIso) {
        return isPotentialEmergencyNumber(getDefaultVoiceSubId(), number, defaultCountryIso);
    }

    @Deprecated
    public static boolean isPotentialEmergencyNumber(int subId, String number, String defaultCountryIso) {
        return isEmergencyNumberInternal(subId, number, defaultCountryIso, false);
    }

    private static boolean isEmergencyNumberInternal(String number, String defaultCountryIso, boolean useExactMatch) {
        return isEmergencyNumberInternal(getDefaultVoiceSubId(), number, defaultCountryIso, useExactMatch);
    }

    private static boolean isEmergencyNumberInternal(int subId, String number, String defaultCountryIso, boolean useExactMatch) {
        if (!useExactMatch) {
            return TelephonyManager.getDefault().isPotentialEmergencyNumber(number, subId);
        }
        try {
            return TelephonyManager.getDefault().isEmergencyNumber(number, subId);
        } catch (RuntimeException ex) {
            Rlog.e(LOG_TAG, "isEmergencyNumberInternal: RuntimeException: " + ex);
            return false;
        }
    }

    @Deprecated
    public static boolean isLocalEmergencyNumber(Context context, String number) {
        return isLocalEmergencyNumber(context, getDefaultVoiceSubId(), number);
    }

    @UnsupportedAppUsage
    @Deprecated
    public static boolean isLocalEmergencyNumber(Context context, int subId, String number) {
        return isLocalEmergencyNumberInternal(subId, number, context, true);
    }

    @UnsupportedAppUsage
    @Deprecated
    public static boolean isPotentialLocalEmergencyNumber(Context context, String number) {
        return isPotentialLocalEmergencyNumber(context, getDefaultVoiceSubId(), number);
    }

    @UnsupportedAppUsage
    @Deprecated
    public static boolean isPotentialLocalEmergencyNumber(Context context, int subId, String number) {
        return isLocalEmergencyNumberInternal(subId, number, context, false);
    }

    private static boolean isLocalEmergencyNumberInternal(String number, Context context, boolean useExactMatch) {
        return isLocalEmergencyNumberInternal(getDefaultVoiceSubId(), number, context, useExactMatch);
    }

    private static boolean isLocalEmergencyNumberInternal(int subId, String number, Context context, boolean useExactMatch) {
        String countryIso;
        CountryDetector detector = (CountryDetector) context.getSystemService(Context.COUNTRY_DETECTOR);
        if (detector == null || detector.detectCountry() == null) {
            String countryIso2 = context.getResources().getConfiguration().locale.getCountry();
            Rlog.w(LOG_TAG, "No CountryDetector; falling back to countryIso based on locale: " + countryIso2);
            countryIso = countryIso2;
        } else {
            countryIso = detector.detectCountry().getCountryIso();
        }
        return isEmergencyNumberInternal(subId, number, countryIso, useExactMatch);
    }

    public static String toLogSafePhoneNumber(String number) {
        if (number == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < number.length(); i++) {
            char c = number.charAt(i);
            if (c == '-' || c == '@' || c == '.' || c == '[' || c == ']') {
                builder.append(c);
            } else {
                builder.append(EpicenterTranslateClipReveal.StateProperty.TARGET_X);
            }
        }
        return builder.toString();
    }

    public static boolean isVoiceMailNumber(String number) {
        if (HwFrameworkFactory.getHwInnerTelephonyManager().useVoiceMailNumberFeature()) {
            return HwFrameworkFactory.getHwInnerTelephonyManager().isVoiceMailNumber(number);
        }
        return isVoiceMailNumber(SubscriptionManager.getDefaultSubscriptionId(), number);
    }

    public static boolean isVoiceMailNumber(int subId, String number) {
        return isVoiceMailNumber(null, subId, number);
    }

    @UnsupportedAppUsage
    public static boolean isVoiceMailNumber(Context context, int subId, String number) {
        TelephonyManager tm;
        CarrierConfigManager configManager;
        PersistableBundle b;
        if (HwFrameworkFactory.getHwInnerTelephonyManager().isLongVoiceMailNumber(subId, number)) {
            return true;
        }
        if (context == null) {
            try {
                tm = TelephonyManager.getDefault();
            } catch (SecurityException e) {
                return false;
            }
        } else {
            tm = TelephonyManager.from(context);
        }
        String vmNumber = tm.getVoiceMailNumber(subId);
        String mdn = tm.getLine1Number(subId);
        String number2 = extractNetworkPortionAlt(number);
        if (TextUtils.isEmpty(number2)) {
            return false;
        }
        boolean compareWithMdn = false;
        if (!(context == null || (configManager = (CarrierConfigManager) context.getSystemService(Context.CARRIER_CONFIG_SERVICE)) == null || (b = configManager.getConfigForSubId(subId)) == null)) {
            compareWithMdn = b.getBoolean(CarrierConfigManager.KEY_MDN_IS_ADDITIONAL_VOICEMAIL_NUMBER_BOOL);
        }
        if (!compareWithMdn) {
            return compare(number2, vmNumber);
        }
        if (compare(number2, vmNumber) || compare(number2, mdn)) {
            return true;
        }
        return false;
    }

    public static String convertKeypadLettersToDigits(String input) {
        int len;
        if (input == null || (len = input.length()) == 0) {
            return input;
        }
        char[] out = input.toCharArray();
        for (int i = 0; i < len; i++) {
            char c = out[i];
            out[i] = (char) KEYPAD_MAP.get(c, c);
        }
        return new String(out);
    }

    @UnsupportedAppUsage
    public static String cdmaCheckAndProcessPlusCode(String dialStr) {
        if (!TextUtils.isEmpty(dialStr) && isReallyDialable(dialStr.charAt(0)) && isNonSeparator(dialStr)) {
            String currIso = TelephonyManager.getDefault().getNetworkCountryIso();
            String defaultIso = TelephonyManager.getDefault().getSimCountryIso();
            if (!TextUtils.isEmpty(currIso) && !TextUtils.isEmpty(defaultIso)) {
                return cdmaCheckAndProcessPlusCodeByNumberFormat(dialStr, getFormatTypeFromCountryCode(currIso), getFormatTypeFromCountryCode(defaultIso));
            }
        }
        return dialStr;
    }

    public static String cdmaCheckAndProcessPlusCodeForSms(String dialStr) {
        if (!TextUtils.isEmpty(dialStr) && isReallyDialable(dialStr.charAt(0)) && isNonSeparator(dialStr)) {
            String defaultIso = TelephonyManager.getDefault().getSimCountryIso();
            if (!TextUtils.isEmpty(defaultIso)) {
                int format = getFormatTypeFromCountryCode(defaultIso);
                return cdmaCheckAndProcessPlusCodeByNumberFormat(dialStr, format, format);
            }
        }
        return dialStr;
    }

    public static String cdmaCheckAndProcessPlusCodeByNumberFormat(String dialStr, int currFormat, int defaultFormat) {
        String networkDialStr;
        String retStr = dialStr;
        boolean useNanp = currFormat == defaultFormat && currFormat == 1;
        if (dialStr != null && dialStr.lastIndexOf("+") != -1) {
            String tempDialStr = dialStr;
            retStr = null;
            do {
                if (useNanp) {
                    networkDialStr = extractNetworkPortion(tempDialStr);
                } else {
                    networkDialStr = extractNetworkPortionAlt(tempDialStr);
                }
                String networkDialStr2 = processPlusCode(networkDialStr, useNanp);
                if (!TextUtils.isEmpty(networkDialStr2)) {
                    if (retStr == null) {
                        retStr = networkDialStr2;
                    } else {
                        retStr = retStr.concat(networkDialStr2);
                    }
                    String postDialStr = extractPostDialPortion(tempDialStr);
                    if (!TextUtils.isEmpty(postDialStr)) {
                        int dialableIndex = findDialableIndexFromPostDialStr(postDialStr);
                        if (dialableIndex >= 1) {
                            retStr = appendPwCharBackToOrigDialStr(dialableIndex, retStr, postDialStr);
                            tempDialStr = postDialStr.substring(dialableIndex);
                        } else {
                            if (dialableIndex < 0) {
                                postDialStr = "";
                            }
                            Rlog.e("wrong postDialStr=", postDialStr);
                        }
                    }
                    if (TextUtils.isEmpty(postDialStr)) {
                        break;
                    }
                } else {
                    Rlog.e("checkAndProcessPlusCode: null newDialStr", networkDialStr2);
                    return dialStr;
                }
            } while (!TextUtils.isEmpty(tempDialStr));
        }
        return retStr;
    }

    public static CharSequence createTtsSpannable(CharSequence phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        Spannable spannable = Spannable.Factory.getInstance().newSpannable(phoneNumber);
        addTtsSpan(spannable, 0, spannable.length());
        return spannable;
    }

    public static void addTtsSpan(Spannable s, int start, int endExclusive) {
        s.setSpan(createTtsSpan(s.subSequence(start, endExclusive).toString()), start, endExclusive, 33);
    }

    @UnsupportedAppUsage
    @Deprecated
    public static CharSequence ttsSpanAsPhoneNumber(CharSequence phoneNumber) {
        return createTtsSpannable(phoneNumber);
    }

    @Deprecated
    public static void ttsSpanAsPhoneNumber(Spannable s, int start, int end) {
        addTtsSpan(s, start, end);
    }

    public static TtsSpan createTtsSpan(String phoneNumberString) {
        if (phoneNumberString == null) {
            return null;
        }
        Phonenumber.PhoneNumber phoneNumber = null;
        try {
            phoneNumber = PhoneNumberUtil.getInstance().parse(phoneNumberString, (String) null);
        } catch (NumberParseException e) {
        }
        TtsSpan.TelephoneBuilder builder = new TtsSpan.TelephoneBuilder();
        if (phoneNumber == null) {
            builder.setNumberParts(splitAtNonNumerics(phoneNumberString));
        } else {
            if (phoneNumber.hasCountryCode()) {
                builder.setCountryCode(Integer.toString(phoneNumber.getCountryCode()));
            }
            builder.setNumberParts(Long.toString(phoneNumber.getNationalNumber()));
        }
        return builder.build();
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:11:0x0026 */
    /* JADX DEBUG: Multi-variable search result rejected for r1v3, resolved type: java.lang.String */
    /* JADX DEBUG: Multi-variable search result rejected for r3v1, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v0, types: [java.lang.String] */
    /* JADX WARN: Type inference failed for: r3v2, types: [java.lang.Character] */
    private static String splitAtNonNumerics(CharSequence number) {
        StringBuilder sb = new StringBuilder(number.length());
        int i = 0;
        while (true) {
            int length = number.length();
            Object obj = WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
            if (i >= length) {
                return sb.toString().replaceAll(" +", obj).trim();
            }
            if (is12Key(number.charAt(i))) {
                obj = Character.valueOf(number.charAt(i));
            }
            sb.append(obj);
            i++;
        }
    }

    private static String getCurrentIdp(boolean useNanp) {
        if (useNanp) {
            return NANP_IDP_STRING;
        }
        return SystemProperties.get(TelephonyProperties.PROPERTY_OPERATOR_IDP_STRING, "+");
    }

    private static boolean isTwoToNine(char c) {
        if (c < '2' || c > '9') {
            return false;
        }
        return true;
    }

    private static int getFormatTypeFromCountryCode(String country) {
        int length = NANP_COUNTRIES.length;
        for (int i = 0; i < length; i++) {
            if (NANP_COUNTRIES[i].compareToIgnoreCase(country) == 0) {
                return 1;
            }
        }
        if ("jp".compareToIgnoreCase(country) == 0) {
            return 2;
        }
        return 0;
    }

    @UnsupportedAppUsage
    public static boolean isNanp(String dialStr) {
        if (dialStr == null) {
            Rlog.e("isNanp: null dialStr passed in", dialStr);
            return false;
        } else if (dialStr.length() != 10 || !isTwoToNine(dialStr.charAt(0)) || !isTwoToNine(dialStr.charAt(3))) {
            return false;
        } else {
            for (int i = 1; i < 10; i++) {
                if (!isISODigit(dialStr.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
    }

    private static boolean isOneNanp(String dialStr) {
        if (dialStr != null) {
            String newDialStr = dialStr.substring(1);
            if (dialStr.charAt(0) != '1' || !isNanp(newDialStr)) {
                return false;
            }
            return true;
        }
        Rlog.e("isOneNanp: null dialStr passed in", dialStr);
        return false;
    }

    @UnsupportedAppUsage
    public static boolean isUriNumber(String number) {
        return number != null && (number.contains("@") || number.contains("%40"));
    }

    @UnsupportedAppUsage
    public static String getUsernameFromUriNumber(String number) {
        int delimiterIndex = number.indexOf(64);
        if (delimiterIndex < 0) {
            delimiterIndex = number.indexOf("%40");
        }
        if (delimiterIndex < 0) {
            Rlog.w(LOG_TAG, "getUsernameFromUriNumber: no delimiter found in SIP addr '" + number + "'");
            delimiterIndex = number.length();
        }
        return number.substring(0, delimiterIndex);
    }

    public static Uri convertSipUriToTelUri(Uri source) {
        if (!"sip".equals(source.getScheme())) {
            return source;
        }
        String[] numberParts = source.getSchemeSpecificPart().split("[@;:]");
        if (numberParts.length == 0) {
            return source;
        }
        return Uri.fromParts(PhoneAccount.SCHEME_TEL, numberParts[0], null);
    }

    private static String processPlusCode(String networkDialStr, boolean useNanp) {
        if (networkDialStr == null || networkDialStr.charAt(0) != '+' || networkDialStr.length() <= 1) {
            return networkDialStr;
        }
        String newStr = networkDialStr.substring(1);
        if (!useNanp || !isOneNanp(newStr)) {
            return networkDialStr.replaceFirst("[+]", getCurrentIdp(useNanp));
        }
        return newStr;
    }

    private static int findDialableIndexFromPostDialStr(String postDialStr) {
        for (int index = 0; index < postDialStr.length(); index++) {
            if (isReallyDialable(postDialStr.charAt(index))) {
                return index;
            }
        }
        return -1;
    }

    /* JADX INFO: Multiple debug info for r0v2 java.lang.String: [D('nonDigitStr' java.lang.String), D('retStr' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r0v5 java.lang.String: [D('ret' java.lang.StringBuilder), D('retStr' java.lang.String)] */
    private static String appendPwCharBackToOrigDialStr(int dialableIndex, String origStr, String dialStr) {
        if (dialableIndex == 1) {
            return origStr + dialStr.charAt(0);
        }
        return origStr.concat(dialStr.substring(0, dialableIndex));
    }

    private static boolean matchIntlPrefix(String a, int len) {
        int state = 0;
        for (int i = 0; i < len; i++) {
            char c = a.charAt(i);
            if (state != 0) {
                if (state != 2) {
                    if (state != 4) {
                        if (isNonSeparator(c)) {
                            return false;
                        }
                    } else if (c == '1') {
                        state = 5;
                    } else if (isNonSeparator(c)) {
                        return false;
                    }
                } else if (c == '0') {
                    state = 3;
                } else if (c == '1') {
                    state = 4;
                } else if (isNonSeparator(c)) {
                    return false;
                }
            } else if (c == '+') {
                state = 1;
            } else if (c == '0') {
                state = 2;
            } else if (isNonSeparator(c)) {
                return false;
            }
        }
        if (state == 1 || state == 3 || state == 5) {
            return true;
        }
        return false;
    }

    private static boolean matchIntlPrefixAndCC(String a, int len) {
        int state = 0;
        for (int i = 0; i < len; i++) {
            char c = a.charAt(i);
            switch (state) {
                case 0:
                    if (c != '+') {
                        if (c == '0') {
                            state = 2;
                            break;
                        } else if (!isNonSeparator(c)) {
                            break;
                        } else {
                            return false;
                        }
                    } else {
                        state = 1;
                        break;
                    }
                case 1:
                case 3:
                case 5:
                    if (isISODigit(c)) {
                        state = 6;
                        break;
                    } else if (!isNonSeparator(c)) {
                        break;
                    } else {
                        return false;
                    }
                case 2:
                    if (c != '0') {
                        if (c == '1') {
                            state = 4;
                            break;
                        } else if (!isNonSeparator(c)) {
                            break;
                        } else {
                            return false;
                        }
                    } else {
                        state = 3;
                        break;
                    }
                case 4:
                    if (c == '1') {
                        state = 5;
                        break;
                    } else if (!isNonSeparator(c)) {
                        break;
                    } else {
                        return false;
                    }
                case 6:
                case 7:
                    if (isISODigit(c)) {
                        state++;
                        break;
                    } else if (!isNonSeparator(c)) {
                        break;
                    } else {
                        return false;
                    }
                default:
                    if (!isNonSeparator(c)) {
                        break;
                    } else {
                        return false;
                    }
            }
        }
        if (state == 6 || state == 7 || state == 8) {
            return true;
        }
        return false;
    }

    private static boolean matchTrunkPrefix(String a, int len) {
        boolean found = false;
        for (int i = 0; i < len; i++) {
            char c = a.charAt(i);
            if (c == '0' && !found) {
                found = true;
            } else if (isNonSeparator(c)) {
                return false;
            }
        }
        return found;
    }

    private static boolean isCountryCallingCode(int countryCallingCodeCandidate) {
        return countryCallingCodeCandidate > 0 && countryCallingCodeCandidate < CCC_LENGTH && COUNTRY_CALLING_CALL[countryCallingCodeCandidate];
    }

    private static int tryGetISODigit(char ch) {
        if ('0' > ch || ch > '9') {
            return -1;
        }
        return ch - '0';
    }

    /* access modifiers changed from: private */
    public static class CountryCallingCodeAndNewIndex {
        public final int countryCallingCode;
        public final int newIndex;

        public CountryCallingCodeAndNewIndex(int countryCode, int newIndex2) {
            this.countryCallingCode = countryCode;
            this.newIndex = newIndex2;
        }
    }

    private static CountryCallingCodeAndNewIndex tryGetCountryCallingCodeAndNewIndex(String str, boolean acceptThailandCase) {
        int state = 0;
        int ccc = 0;
        int length = str.length();
        for (int i = 0; i < length; i++) {
            char ch = str.charAt(i);
            switch (state) {
                case 0:
                    if (ch != '+') {
                        if (ch == '0') {
                            state = 2;
                            break;
                        } else if (ch != '1') {
                            if (!isDialable(ch)) {
                                break;
                            } else {
                                return null;
                            }
                        } else if (acceptThailandCase) {
                            state = 8;
                            break;
                        } else {
                            return null;
                        }
                    } else {
                        state = 1;
                        break;
                    }
                case 1:
                case 3:
                case 5:
                case 6:
                case 7:
                    int ret = tryGetISODigit(ch);
                    if (ret > 0) {
                        ccc = (ccc * 10) + ret;
                        if (ccc < 100 && !isCountryCallingCode(ccc)) {
                            if (state != 1 && state != 3 && state != 5) {
                                state++;
                                break;
                            } else {
                                state = 6;
                                break;
                            }
                        } else {
                            return new CountryCallingCodeAndNewIndex(ccc, i + 1);
                        }
                    } else if (!isDialable(ch)) {
                        break;
                    } else {
                        return null;
                    }
                    break;
                case 2:
                    if (ch != '0') {
                        if (ch == '1') {
                            state = 4;
                            break;
                        } else if (!isDialable(ch)) {
                            break;
                        } else {
                            return null;
                        }
                    } else {
                        state = 3;
                        break;
                    }
                case 4:
                    if (ch == '1') {
                        state = 5;
                        break;
                    } else if (!isDialable(ch)) {
                        break;
                    } else {
                        return null;
                    }
                case 8:
                    if (ch == '6') {
                        state = 9;
                        break;
                    } else if (!isDialable(ch)) {
                        break;
                    } else {
                        return null;
                    }
                case 9:
                    if (ch == '6') {
                        return new CountryCallingCodeAndNewIndex(66, i + 1);
                    }
                    return null;
                default:
                    return null;
            }
        }
        return null;
    }

    private static int tryGetTrunkPrefixOmittedIndex(String str, int currentIndex) {
        int length = str.length();
        for (int i = currentIndex; i < length; i++) {
            char ch = str.charAt(i);
            if (tryGetISODigit(ch) >= 0) {
                return i + 1;
            }
            if (isDialable(ch)) {
                return -1;
            }
        }
        return -1;
    }

    private static boolean checkPrefixIsIgnorable(String str, int forwardIndex, int backwardIndex) {
        boolean trunk_prefix_was_read = false;
        while (backwardIndex >= forwardIndex) {
            if (tryGetISODigit(str.charAt(backwardIndex)) >= 0) {
                if (trunk_prefix_was_read) {
                    return false;
                }
                trunk_prefix_was_read = true;
            } else if (isDialable(str.charAt(backwardIndex))) {
                return false;
            }
            backwardIndex--;
        }
        return true;
    }

    private static int getDefaultVoiceSubId() {
        return SubscriptionManager.getDefaultVoiceSubscriptionId();
    }

    public static String convertToEmergencyNumber(Context context, String number) {
        if (context == null || TextUtils.isEmpty(number)) {
            return number;
        }
        String normalizedNumber = normalizeNumber(number);
        if (isEmergencyNumber(normalizedNumber)) {
            return number;
        }
        if (sConvertToEmergencyMap == null) {
            sConvertToEmergencyMap = context.getResources().getStringArray(R.array.config_convert_to_emergency_number_map);
        }
        String[] strArr = sConvertToEmergencyMap;
        if (strArr == null || strArr.length == 0) {
            return number;
        }
        for (String convertMap : strArr) {
            String[] entry = null;
            String[] filterNumbers = null;
            String convertedNumber = null;
            if (!TextUtils.isEmpty(convertMap)) {
                entry = convertMap.split(SettingsStringUtil.DELIMITER);
            }
            if (entry != null && entry.length == 2) {
                convertedNumber = entry[1];
                if (!TextUtils.isEmpty(entry[0])) {
                    filterNumbers = entry[0].split(SmsManager.REGEX_PREFIX_DELIMITER);
                }
            }
            if (!(TextUtils.isEmpty(convertedNumber) || filterNumbers == null || filterNumbers.length == 0)) {
                for (String filterNumber : filterNumbers) {
                    if (!TextUtils.isEmpty(filterNumber) && filterNumber.equals(normalizedNumber)) {
                        return convertedNumber;
                    }
                }
                continue;
            }
        }
        return number;
    }
}
