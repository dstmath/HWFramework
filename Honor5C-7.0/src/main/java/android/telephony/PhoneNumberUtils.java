package android.telephony;

import android.common.HwFrameworkFactory;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Country;
import android.location.CountryDetector;
import android.net.Uri;
import android.os.SystemProperties;
import android.telecom.PhoneAccount;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.TtsSpan;
import android.text.style.TtsSpan.TelephoneBuilder;
import android.util.PtmLog;
import android.util.SparseIntArray;
import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber.CountryCodeSource;
import com.android.i18n.phonenumbers.ShortNumberUtil;
import com.android.internal.R;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.RILConstants;
import com.android.internal.telephony.TelephonyProperties;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.indexsearch.IndexSearchConstants;
import com.huawei.pgmng.log.LogPower;
import com.huawei.pgmng.plug.PGSdk;
import huawei.cust.HwCfgFilePolicy;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumberUtils {
    private static final int CCC_LENGTH = 0;
    public static final int CCWA_NUMBER_INTERNATIONAL = 1;
    private static final String CLIR_OFF = "#31#";
    private static final String CLIR_ON = "*31#";
    private static final boolean[] COUNTRY_CALLING_CALL = null;
    private static final boolean DBG = false;
    public static final int FORMAT_JAPAN = 2;
    public static final int FORMAT_NANP = 1;
    public static final int FORMAT_UNKNOWN = 0;
    private static final Pattern GLOBAL_PHONE_NUMBER_PATTERN = null;
    private static final SparseIntArray KEYPAD_MAP = null;
    private static final String KOREA_ISO_COUNTRY_CODE = "KR";
    static final String LOG_TAG = "PhoneNumberUtils";
    static final int MIN_MATCH = 7;
    private static final String[] NANP_COUNTRIES = null;
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
    private static Country sCountryDetector;
    private static final ArrayList<MccNumberMatch> table = null;

    private static class CountryCallingCodeAndNewIndex {
        public final int countryCallingCode;
        public final int newIndex;

        public CountryCallingCodeAndNewIndex(int countryCode, int newIndex) {
            this.countryCallingCode = countryCode;
            this.newIndex = newIndex;
        }
    }

    static class MccNumberMatch {
        private String mCc;
        private String mIdd;
        private int mMcc;
        private String mNdd;
        private String[] mSpcs;

        MccNumberMatch(int mcc, String idd, String cc, String ndd) {
            this.mMcc = mcc;
            this.mIdd = idd;
            this.mCc = cc;
            this.mNdd = ndd;
        }

        MccNumberMatch(int mcc, String idd, String cc, String ndd, String spcList) {
            this.mMcc = mcc;
            this.mIdd = idd;
            this.mCc = cc;
            this.mNdd = ndd;
            if (spcList != null) {
                this.mSpcs = spcList.split(PtmLog.PAIRE_DELIMETER);
            }
        }

        public int getMcc() {
            return this.mMcc;
        }

        public String getIdd() {
            return this.mIdd;
        }

        public String getCc() {
            return this.mCc;
        }

        public String getNdd() {
            return this.mNdd;
        }

        public String[] getSpcs() {
            return this.mSpcs;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.telephony.PhoneNumberUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.telephony.PhoneNumberUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.telephony.PhoneNumberUtils.<clinit>():void");
    }

    private static ArrayList<MccNumberMatch> initMccMatchTable() {
        ArrayList<MccNumberMatch> tempTable = new ArrayList();
        tempTable.add(new MccNumberMatch(460, "00", "86", "0", "13,15,18,17,14,10649"));
        tempTable.add(new MccNumberMatch(MetricsEvent.RUNNING_SERVICES, "00", "91", "0", "99"));
        return tempTable;
    }

    private static MccNumberMatch getRecordByMcc(int mcc) {
        for (int i = FORMAT_UNKNOWN; i < table.size(); i += NANP_STATE_DIGIT) {
            if (mcc == ((MccNumberMatch) table.get(i)).getMcc()) {
                return (MccNumberMatch) table.get(i);
            }
        }
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String convertPlusByMcc(String number, int mcc) {
        MccNumberMatch record = getRecordByMcc(mcc);
        StringBuilder strBuilder = new StringBuilder();
        if (record == null || number == null || !number.startsWith(PLUS_SIGN_STRING) || !number.startsWith(record.getCc(), NANP_STATE_DIGIT)) {
            return number;
        }
        String realNum = number.substring(record.getCc().length() + NANP_STATE_DIGIT);
        if (!beginWith(realNum, record.getSpcs())) {
            strBuilder.append(record.getNdd());
        }
        strBuilder.append(realNum);
        return strBuilder.toString();
    }

    public static boolean isMobileNumber(String number, int mcc) {
        MccNumberMatch record = getRecordByMcc(mcc);
        if (record == null || number == null || number.length() == 0) {
            return DBG;
        }
        boolean isMobileNum = DBG;
        int spcIndex = FORMAT_UNKNOWN;
        if (number.startsWith(PLUS_SIGN_STRING) && number.startsWith(record.getCc(), NANP_STATE_DIGIT)) {
            spcIndex = record.getCc().length() + NANP_STATE_DIGIT;
        } else if (number.startsWith(record.getIdd()) && number.startsWith(record.getCc(), record.getIdd().length())) {
            spcIndex = record.getIdd().length() + record.getCc().length();
        }
        if (beginWith(number.substring(spcIndex), record.getSpcs())) {
            isMobileNum = true;
        }
        Rlog.d(LOG_TAG, "plus: isMobileNumber = " + isMobileNum);
        return isMobileNum;
    }

    private static boolean beginWith(String wholeStr, String[] subStrs) {
        if (wholeStr == null || subStrs == null || subStrs.length <= 0) {
            return DBG;
        }
        int i = FORMAT_UNKNOWN;
        while (i < subStrs.length) {
            if (subStrs[i].length() > 0 && wholeStr.startsWith(subStrs[i])) {
                return true;
            }
            i += NANP_STATE_DIGIT;
        }
        return DBG;
    }

    public static boolean isISODigit(char c) {
        return (c < '0' || c > '9') ? DBG : true;
    }

    public static final boolean is12Key(char c) {
        return ((c >= '0' && c <= '9') || c == '*' || c == '#') ? true : DBG;
    }

    public static final boolean isDialable(char c) {
        return ((c >= '0' && c <= '9') || c == '*' || c == '#' || c == PLUS_SIGN_CHAR || c == WILD) ? true : DBG;
    }

    public static final boolean isReallyDialable(char c) {
        return ((c >= '0' && c <= '9') || c == '*' || c == '#' || c == PLUS_SIGN_CHAR) ? true : DBG;
    }

    public static final boolean isNonSeparator(char c) {
        if ((c >= '0' && c <= '9') || c == '*' || c == '#' || c == PLUS_SIGN_CHAR || c == WILD || c == WAIT || c == PAUSE) {
            return true;
        }
        return DBG;
    }

    public static final boolean isStartsPostDial(char c) {
        return (c == PAUSE || c == WAIT) ? true : DBG;
    }

    private static boolean isPause(char c) {
        return (c == 'p' || c == 'P') ? true : DBG;
    }

    private static boolean isToneWait(char c) {
        return (c == 'w' || c == 'W') ? true : DBG;
    }

    private static boolean isSeparator(char ch) {
        boolean z = true;
        if (isDialable(ch)) {
            return DBG;
        }
        if (DateFormat.AM_PM <= ch && ch <= DateFormat.TIME_ZONE) {
            return DBG;
        }
        if (DateFormat.CAPITAL_AM_PM <= ch && ch <= 'Z') {
            z = DBG;
        }
        return z;
    }

    public static String getNumberFromIntent(Intent intent, Context context) {
        String str = null;
        Uri uri = intent.getData();
        if (uri == null) {
            return null;
        }
        String scheme = uri.getScheme();
        if (scheme.equals(PhoneAccount.SCHEME_TEL) || scheme.equals(PhoneAccount.SCHEME_SIP)) {
            return uri.getSchemeSpecificPart();
        }
        if (context == null) {
            return null;
        }
        String type = intent.resolveType(context);
        String phoneColumn = null;
        String authority = uri.getAuthority();
        if ("contacts".equals(authority)) {
            phoneColumn = SubscriptionManager.NUMBER;
        } else if ("com.android.contacts".equals(authority)) {
            phoneColumn = "data1";
        }
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            String[] strArr = new String[NANP_STATE_DIGIT];
            strArr[FORMAT_UNKNOWN] = phoneColumn;
            cursor = contentResolver.query(uri, strArr, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                str = cursor.getString(cursor.getColumnIndex(phoneColumn));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (RuntimeException e) {
            Rlog.e(LOG_TAG, "Error getting phone number.", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return str;
    }

    public static String extractNetworkPortion(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);
        for (int i = FORMAT_UNKNOWN; i < len; i += NANP_STATE_DIGIT) {
            char c = phoneNumber.charAt(i);
            int digit = Character.digit(c, NANP_LENGTH);
            if (digit != -1) {
                ret.append(digit);
            } else if (c == PLUS_SIGN_CHAR) {
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

    public static String extractNetworkPortionAlt(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);
        boolean haveSeenPlus = DBG;
        for (int i = FORMAT_UNKNOWN; i < len; i += NANP_STATE_DIGIT) {
            char c = phoneNumber.charAt(i);
            if (c == PLUS_SIGN_CHAR) {
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
        for (int i = FORMAT_UNKNOWN; i < len; i += NANP_STATE_DIGIT) {
            char c = phoneNumber.charAt(i);
            int digit = Character.digit(c, NANP_LENGTH);
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

    public static String convertPreDial(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int len = phoneNumber.length();
        StringBuilder ret = new StringBuilder(len);
        for (int i = FORMAT_UNKNOWN; i < len; i += NANP_STATE_DIGIT) {
            char c = phoneNumber.charAt(i);
            if (isPause(c)) {
                c = PAUSE;
            } else if (isToneWait(c)) {
                c = WAIT;
            }
            ret.append(c);
        }
        return ret.toString();
    }

    private static int minPositive(int a, int b) {
        if (a >= 0 && b >= 0) {
            if (a >= b) {
                a = b;
            }
            return a;
        } else if (a >= 0) {
            return a;
        } else {
            if (b >= 0) {
                return b;
            }
            return -1;
        }
    }

    private static void log(String msg) {
        Rlog.d(LOG_TAG, msg);
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
        for (int i = indexOfLastNetworkChar(phoneNumber) + NANP_STATE_DIGIT; i < s; i += NANP_STATE_DIGIT) {
            char c = phoneNumber.charAt(i);
            if (isNonSeparator(c)) {
                ret.append(c);
            }
        }
        return ret.toString();
    }

    public static boolean compare(String a, String b) {
        return compare(a, b, (boolean) DBG);
    }

    public static boolean compare(Context context, String a, String b) {
        return compare(a, b, context.getResources().getBoolean(R.bool.config_use_strict_phone_number_comparation));
    }

    public static boolean compare(String a, String b, boolean useStrictComparation) {
        return useStrictComparation ? compareStrictly(a, b) : compareLoosely(a, b);
    }

    public static boolean compareLoosely(String a, String b) {
        int numNonDialableCharsInA = FORMAT_UNKNOWN;
        int numNonDialableCharsInB = FORMAT_UNKNOWN;
        if (a == null || b == null) {
            return a == b ? true : DBG;
        } else if (a.length() == 0 || b.length() == 0) {
            return DBG;
        } else {
            int ia = indexOfLastNetworkChar(a);
            int ib = indexOfLastNetworkChar(b);
            int matched = FORMAT_UNKNOWN;
            while (ia >= 0 && ib >= 0) {
                boolean skipCmp = DBG;
                char ca = a.charAt(ia);
                if (!isDialable(ca)) {
                    ia--;
                    skipCmp = true;
                    numNonDialableCharsInA += NANP_STATE_DIGIT;
                }
                char cb = b.charAt(ib);
                if (!isDialable(cb)) {
                    ib--;
                    skipCmp = true;
                    numNonDialableCharsInB += NANP_STATE_DIGIT;
                }
                if (!skipCmp) {
                    if (cb != ca && ca != WILD && cb != WILD) {
                        break;
                    }
                    ia--;
                    ib--;
                    matched += NANP_STATE_DIGIT;
                }
            }
            if (matched < MIN_MATCH) {
                int effectiveALen = a.length() - numNonDialableCharsInA;
                if (effectiveALen == b.length() - numNonDialableCharsInB && effectiveALen == matched) {
                    return true;
                }
                return DBG;
            } else if (matched >= MIN_MATCH && (ia < 0 || ib < 0)) {
                return true;
            } else {
                if (matchIntlPrefix(a, ia + NANP_STATE_DIGIT) && matchIntlPrefix(b, ib + NANP_STATE_DIGIT)) {
                    return true;
                }
                if (matchTrunkPrefix(a, ia + NANP_STATE_DIGIT) && matchIntlPrefixAndCC(b, ib + NANP_STATE_DIGIT)) {
                    return true;
                }
                if (matchTrunkPrefix(b, ib + NANP_STATE_DIGIT) && matchIntlPrefixAndCC(a, ia + NANP_STATE_DIGIT)) {
                    return true;
                }
                return DBG;
            }
        }
    }

    public static boolean compareStrictly(String a, String b) {
        return compareStrictly(a, b, true);
    }

    public static boolean compareStrictly(String a, String b, boolean acceptInvalidCCCPrefix) {
        if (a == null || b == null) {
            return a == b ? true : DBG;
        } else if (a.length() == 0 && b.length() == 0) {
            return DBG;
        } else {
            char chA;
            char chB;
            int forwardIndexA = FORMAT_UNKNOWN;
            int forwardIndexB = FORMAT_UNKNOWN;
            CountryCallingCodeAndNewIndex cccA = tryGetCountryCallingCodeAndNewIndex(a, acceptInvalidCCCPrefix);
            CountryCallingCodeAndNewIndex cccB = tryGetCountryCallingCodeAndNewIndex(b, acceptInvalidCCCPrefix);
            boolean bothHasCountryCallingCode = DBG;
            boolean okToIgnorePrefix = true;
            boolean trunkPrefixIsOmittedA = DBG;
            boolean trunkPrefixIsOmittedB = DBG;
            if (cccA != null && cccB != null) {
                if (cccA.countryCallingCode != cccB.countryCallingCode) {
                    return DBG;
                }
                okToIgnorePrefix = DBG;
                bothHasCountryCallingCode = true;
                forwardIndexA = cccA.newIndex;
                forwardIndexB = cccB.newIndex;
            } else if (cccA == null && cccB == null) {
                okToIgnorePrefix = DBG;
            } else {
                int tmp;
                if (cccA != null) {
                    forwardIndexA = cccA.newIndex;
                } else {
                    tmp = tryGetTrunkPrefixOmittedIndex(b, FORMAT_UNKNOWN);
                    if (tmp >= 0) {
                        forwardIndexA = tmp;
                        trunkPrefixIsOmittedA = true;
                    }
                }
                if (cccB != null) {
                    forwardIndexB = cccB.newIndex;
                } else {
                    tmp = tryGetTrunkPrefixOmittedIndex(b, FORMAT_UNKNOWN);
                    if (tmp >= 0) {
                        forwardIndexB = tmp;
                        trunkPrefixIsOmittedB = true;
                    }
                }
            }
            int backwardIndexA = a.length() - 1;
            int backwardIndexB = b.length() - 1;
            while (backwardIndexA >= forwardIndexA && backwardIndexB >= forwardIndexB) {
                boolean skip_compare = DBG;
                chA = a.charAt(backwardIndexA);
                chB = b.charAt(backwardIndexB);
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
                        return DBG;
                    }
                    backwardIndexA--;
                    backwardIndexB--;
                }
            }
            if (!okToIgnorePrefix) {
                boolean maybeNamp = bothHasCountryCallingCode ? DBG : true;
                while (backwardIndexA >= forwardIndexA) {
                    chA = a.charAt(backwardIndexA);
                    if (isDialable(chA)) {
                        if (!maybeNamp || tryGetISODigit(chA) != NANP_STATE_DIGIT) {
                            return DBG;
                        }
                        maybeNamp = DBG;
                    }
                    backwardIndexA--;
                }
                while (backwardIndexB >= forwardIndexB) {
                    chB = b.charAt(backwardIndexB);
                    if (isDialable(chB)) {
                        if (!maybeNamp || tryGetISODigit(chB) != NANP_STATE_DIGIT) {
                            return DBG;
                        }
                        maybeNamp = DBG;
                    }
                    backwardIndexB--;
                }
            } else if ((!trunkPrefixIsOmittedA || forwardIndexA > backwardIndexA) && checkPrefixIsIgnorable(a, forwardIndexA, backwardIndexA)) {
                if ((trunkPrefixIsOmittedB && forwardIndexB <= backwardIndexB) || !checkPrefixIsIgnorable(b, forwardIndexA, backwardIndexB)) {
                    if (acceptInvalidCCCPrefix) {
                        return compare(a, b, DBG);
                    }
                    return DBG;
                }
            } else if (acceptInvalidCCCPrefix) {
                return compare(a, b, DBG);
            } else {
                return DBG;
            }
            return true;
        }
    }

    public static String toCallerIDMinMatch(String phoneNumber) {
        return internalGetStrippedReversed(extractNetworkPortionAlt(phoneNumber), MIN_MATCH);
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
        int s = length;
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
        if (TOA != TOA_International || s.length() <= 0 || s.charAt(FORMAT_UNKNOWN) == PLUS_SIGN_CHAR) {
            return s;
        }
        return PLUS_SIGN_STRING + s;
    }

    public static int getToaFromNumberType(int numType) {
        if (NANP_STATE_DIGIT == numType) {
            return TOA_International;
        }
        return TOA_Unknown;
    }

    public static int toaFromString(String s) {
        if (s == null || s.length() <= 0 || s.charAt(FORMAT_UNKNOWN) != PLUS_SIGN_CHAR) {
            return TOA_Unknown;
        }
        return TOA_International;
    }

    public static String calledPartyBCDToString(byte[] bytes, int offset, int length) {
        boolean prependPlus = DBG;
        StringBuilder ret = new StringBuilder((length * NANP_STATE_PLUS) + NANP_STATE_DIGIT);
        if (length < NANP_STATE_PLUS) {
            return "";
        }
        if ((bytes[offset] & IndexSearchConstants.INDEX_BUILD_FLAG_MASK) == LogPower.DISABLE_SENSOR) {
            prependPlus = true;
        }
        internalCalledPartyBCDFragmentToString(ret, bytes, offset + NANP_STATE_DIGIT, length - 1);
        if (prependPlus && ret.length() == 0) {
            return "";
        }
        if (prependPlus) {
            String retString = ret.toString();
            Matcher m = Pattern.compile("(^[#*])(.*)([#*])(.*)([*]{2})(.*)(#)$").matcher(retString);
            if (!m.matches()) {
                m = Pattern.compile("(^[#*])(.*)([#*])(.*)(#)$").matcher(retString);
                if (!m.matches()) {
                    m = Pattern.compile("(^[#*])(.*)([#*])(.*)").matcher(retString);
                    if (m.matches()) {
                        ret = new StringBuilder();
                        ret.append(m.group(NANP_STATE_DIGIT));
                        ret.append(m.group(NANP_STATE_PLUS));
                        ret.append(m.group(NANP_STATE_ONE));
                        ret.append(PLUS_SIGN_STRING);
                        ret.append(m.group(NANP_STATE_DASH));
                    } else {
                        ret = new StringBuilder();
                        ret.append(PLUS_SIGN_CHAR);
                        ret.append(retString);
                    }
                } else if ("".equals(m.group(NANP_STATE_PLUS))) {
                    ret = new StringBuilder();
                    ret.append(m.group(NANP_STATE_DIGIT));
                    ret.append(m.group(NANP_STATE_ONE));
                    ret.append(m.group(NANP_STATE_DASH));
                    ret.append(m.group(5));
                    ret.append(PLUS_SIGN_STRING);
                } else {
                    ret = new StringBuilder();
                    ret.append(m.group(NANP_STATE_DIGIT));
                    ret.append(m.group(NANP_STATE_PLUS));
                    ret.append(m.group(NANP_STATE_ONE));
                    ret.append(PLUS_SIGN_STRING);
                    ret.append(m.group(NANP_STATE_DASH));
                    ret.append(m.group(5));
                }
            } else if ("".equals(m.group(NANP_STATE_PLUS))) {
                ret = new StringBuilder();
                ret.append(m.group(NANP_STATE_DIGIT));
                ret.append(m.group(NANP_STATE_ONE));
                ret.append(m.group(NANP_STATE_DASH));
                ret.append(m.group(5));
                ret.append(m.group(6));
                ret.append(m.group(MIN_MATCH));
                ret.append(PLUS_SIGN_STRING);
            } else {
                ret = new StringBuilder();
                ret.append(m.group(NANP_STATE_DIGIT));
                ret.append(m.group(NANP_STATE_PLUS));
                ret.append(m.group(NANP_STATE_ONE));
                ret.append(PLUS_SIGN_STRING);
                ret.append(m.group(NANP_STATE_DASH));
                ret.append(m.group(5));
                ret.append(m.group(6));
                ret.append(m.group(MIN_MATCH));
            }
        }
        return ret.toString();
    }

    private static void internalCalledPartyBCDFragmentToString(StringBuilder sb, byte[] bytes, int offset, int length) {
        int i = offset;
        while (i < length + offset) {
            char c = bcdToChar((byte) (bytes[i] & 15));
            if (c != '\u0000') {
                sb.append(c);
                byte b = (byte) ((bytes[i] >> NANP_STATE_DASH) & 15);
                if (b == 15 && i + NANP_STATE_DIGIT == length + offset) {
                    break;
                }
                c = bcdToChar(b);
                if (c != '\u0000') {
                    sb.append(c);
                    i += NANP_STATE_DIGIT;
                } else {
                    return;
                }
            }
            return;
        }
    }

    public static String calledPartyBCDFragmentToString(byte[] bytes, int offset, int length) {
        StringBuilder ret = new StringBuilder(length * NANP_STATE_PLUS);
        internalCalledPartyBCDFragmentToString(ret, bytes, offset, length);
        return ret.toString();
    }

    private static char bcdToChar(byte b) {
        if (b < NANP_LENGTH) {
            return (char) (b + 48);
        }
        switch (b) {
            case NANP_LENGTH /*10*/:
                return '*';
            case PGSdk.TYPE_IM /*11*/:
                return '#';
            case PGSdk.TYPE_MUSIC /*12*/:
                return PAUSE;
            case HwPerformance.PERF_VAL_DEV_TYPE_MAX /*13*/:
                return WILD;
            default:
                return '\u0000';
        }
    }

    private static int charToBCD(char c) {
        if (c >= '0' && c <= '9') {
            return c - 48;
        }
        if (c == '*') {
            return NANP_LENGTH;
        }
        if (c == '#') {
            return 11;
        }
        if (c == PAUSE) {
            return 12;
        }
        if (c == WILD) {
            return 13;
        }
        if (c == WAIT) {
            return 14;
        }
        throw new RuntimeException("invalid char for BCD " + c);
    }

    public static boolean isWellFormedSmsAddress(String address) {
        boolean z;
        String networkPortion = extractNetworkPortion(address);
        if (networkPortion.equals(PLUS_SIGN_STRING)) {
            z = true;
        } else {
            z = TextUtils.isEmpty(networkPortion);
        }
        if (z) {
            return DBG;
        }
        return isDialable(networkPortion);
    }

    public static boolean isGlobalPhoneNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return DBG;
        }
        return GLOBAL_PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches();
    }

    private static boolean isDialable(String address) {
        int count = address.length();
        for (int i = FORMAT_UNKNOWN; i < count; i += NANP_STATE_DIGIT) {
            if (!isDialable(address.charAt(i))) {
                return DBG;
            }
        }
        return true;
    }

    private static boolean isNonSeparator(String address) {
        int count = address.length();
        for (int i = FORMAT_UNKNOWN; i < count; i += NANP_STATE_DIGIT) {
            if (!isNonSeparator(address.charAt(i))) {
                return DBG;
            }
        }
        return true;
    }

    public static byte[] networkPortionToCalledPartyBCD(String s) {
        return numberToCalledPartyBCDHelper(extractNetworkPortion(s), DBG);
    }

    public static byte[] networkPortionToCalledPartyBCDWithLength(String s) {
        return numberToCalledPartyBCDHelper(extractNetworkPortion(s), true);
    }

    public static byte[] numberToCalledPartyBCD(String number) {
        return numberToCalledPartyBCDHelper(number, DBG);
    }

    private static byte[] numberToCalledPartyBCDHelper(String number, boolean includeLength) {
        int numberLenReal = number.length();
        int numberLenEffective = numberLenReal;
        boolean hasPlus = number.indexOf(43) != -1 ? true : DBG;
        if (hasPlus) {
            numberLenEffective = numberLenReal - 1;
        }
        if (numberLenEffective == 0) {
            return null;
        }
        int resultLen = (numberLenEffective + NANP_STATE_DIGIT) / NANP_STATE_PLUS;
        int extraBytes = NANP_STATE_DIGIT;
        if (includeLength) {
            extraBytes = NANP_STATE_PLUS;
        }
        resultLen += extraBytes;
        byte[] result = new byte[resultLen];
        int digitCount = FORMAT_UNKNOWN;
        for (int i = FORMAT_UNKNOWN; i < numberLenReal; i += NANP_STATE_DIGIT) {
            char c = number.charAt(i);
            if (c != PLUS_SIGN_CHAR) {
                int i2 = (digitCount >> NANP_STATE_DIGIT) + extraBytes;
                result[i2] = (byte) (result[i2] | ((byte) ((charToBCD(c) & 15) << ((digitCount & NANP_STATE_DIGIT) == NANP_STATE_DIGIT ? NANP_STATE_DASH : FORMAT_UNKNOWN))));
                digitCount += NANP_STATE_DIGIT;
            }
        }
        if ((digitCount & NANP_STATE_DIGIT) == NANP_STATE_DIGIT) {
            i2 = (digitCount >> NANP_STATE_DIGIT) + extraBytes;
            result[i2] = (byte) (result[i2] | IndexSearchConstants.INDEX_BUILD_FLAG_MASK);
        }
        int offset = FORMAT_UNKNOWN;
        if (includeLength) {
            offset = NANP_STATE_DIGIT;
            result[FORMAT_UNKNOWN] = (byte) (resultLen - 1);
        }
        result[offset] = (byte) (hasPlus ? TOA_International : TOA_Unknown);
        return result;
    }

    @Deprecated
    public static String formatNumber(String source) {
        Editable text = new SpannableStringBuilder(source);
        formatNumber(text, getFormatTypeForLocale(Locale.getDefault()));
        return text.toString();
    }

    @Deprecated
    public static String formatNumber(String source, int defaultFormattingType) {
        Editable text = new SpannableStringBuilder(source);
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
        if (text.length() > NANP_STATE_PLUS && text.charAt(FORMAT_UNKNOWN) == PLUS_SIGN_CHAR) {
            formatType = text.charAt(NANP_STATE_DIGIT) == '1' ? NANP_STATE_DIGIT : (text.length() >= NANP_STATE_ONE && text.charAt(NANP_STATE_DIGIT) == '8' && text.charAt(NANP_STATE_PLUS) == '1') ? NANP_STATE_PLUS : FORMAT_UNKNOWN;
        }
        switch (formatType) {
            case FORMAT_UNKNOWN /*0*/:
                removeDashes(text);
            case NANP_STATE_DIGIT /*1*/:
                formatNanpNumber(text);
            case NANP_STATE_PLUS /*2*/:
                formatJapaneseNumber(text);
            default:
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @Deprecated
    public static void formatNanpNumber(Editable text) {
        int length = text.length();
        if (length <= "+1-nnn-nnn-nnnn".length() && length > 5) {
            int numDashes;
            CharSequence saved = text.subSequence(FORMAT_UNKNOWN, length);
            removeDashes(text);
            length = text.length();
            int[] dashPositions = new int[NANP_STATE_ONE];
            int state = NANP_STATE_DIGIT;
            int numDigits = FORMAT_UNKNOWN;
            int i = FORMAT_UNKNOWN;
            int numDashes2 = FORMAT_UNKNOWN;
            while (i < length) {
                switch (text.charAt(i)) {
                    case StatisticalConstant.TYPE_SINGLEHAND_ENTER_2S_EXIT /*43*/:
                        if (i != 0) {
                            break;
                        }
                        state = NANP_STATE_PLUS;
                        numDashes = numDashes2;
                        continue;
                    case RILConstants.RIL_REQUEST_QUERY_NETWORK_SELECTION_MODE /*45*/:
                        state = NANP_STATE_DASH;
                        numDashes = numDashes2;
                        continue;
                    case RILConstants.RIL_REQUEST_DTMF_START /*49*/:
                        if (numDigits == 0 || state == NANP_STATE_PLUS) {
                            state = NANP_STATE_ONE;
                            numDashes = numDashes2;
                            continue;
                        }
                    case IndexSearchConstants.INDEX_BUILD_FLAG_EXTERNAL_FILE /*48*/:
                    case StatisticalConstant.TYPE_SINGLEHAND_END /*50*/:
                    case StatisticalConstant.TYPE_WIFI_SURFING /*51*/:
                    case StatisticalConstant.TYPE_WIFI_OPERATION_INFO /*52*/:
                    case StatisticalConstant.TYPE_WIFI_DISCONNECT /*53*/:
                    case StatisticalConstant.TYPE_WIFI_CONNECTION_ACTION /*54*/:
                    case RILConstants.RIL_REQUEST_QUERY_CLIP /*55*/:
                    case RILConstants.RIL_REQUEST_LAST_DATA_CALL_FAIL_CAUSE /*56*/:
                    case RILConstants.RIL_REQUEST_DATA_CALL_LIST /*57*/:
                        if (state == NANP_STATE_PLUS) {
                            text.replace(FORMAT_UNKNOWN, length, saved);
                            return;
                        }
                        if (state == NANP_STATE_ONE) {
                            numDashes = numDashes2 + NANP_STATE_DIGIT;
                            dashPositions[numDashes2] = i;
                        } else if (state == NANP_STATE_DASH || !(numDigits == NANP_STATE_ONE || numDigits == 6)) {
                            numDashes = numDashes2;
                        } else {
                            numDashes = numDashes2 + NANP_STATE_DIGIT;
                            dashPositions[numDashes2] = i;
                        }
                        state = NANP_STATE_DIGIT;
                        numDigits += NANP_STATE_DIGIT;
                        continue;
                    default:
                        break;
                }
                text.replace(FORMAT_UNKNOWN, length, saved);
                return;
            }
            if (numDigits == MIN_MATCH) {
                numDashes = numDashes2 - 1;
            } else {
                numDashes = numDashes2;
            }
            for (i = FORMAT_UNKNOWN; i < numDashes; i += NANP_STATE_DIGIT) {
                int pos = dashPositions[i];
                text.replace(pos + i, pos + i, NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
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
        int p = FORMAT_UNKNOWN;
        while (p < text.length()) {
            if (text.charAt(p) == '-') {
                text.delete(p, p + NANP_STATE_DIGIT);
            } else {
                p += NANP_STATE_DIGIT;
            }
        }
    }

    public static String formatNumberToE164(String phoneNumber, String defaultCountryIso) {
        return formatNumberInternal(phoneNumber, defaultCountryIso, PhoneNumberFormat.E164);
    }

    public static String formatNumberToRFC3966(String phoneNumber, String defaultCountryIso) {
        return formatNumberInternal(phoneNumber, defaultCountryIso, PhoneNumberFormat.RFC3966);
    }

    private static String formatNumberInternal(String rawPhoneNumber, String defaultCountryIso, PhoneNumberFormat formatIdentifier) {
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        try {
            PhoneNumber phoneNumber = util.parse(rawPhoneNumber, defaultCountryIso);
            if (util.isValidNumber(phoneNumber)) {
                return util.format(phoneNumber, formatIdentifier);
            }
        } catch (NumberParseException e) {
        } catch (RuntimeException e2) {
            Rlog.d(LOG_TAG, "formatNumberInternal RuntimeException");
        }
        return null;
    }

    public static String formatNumber(String phoneNumber, String defaultCountryIso) {
        if (phoneNumber.startsWith("#") || phoneNumber.startsWith(PhoneConstants.APN_TYPE_ALL)) {
            return phoneNumber;
        }
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        String result = null;
        try {
            PhoneNumber pn = util.parseAndKeepRawInput(phoneNumber, defaultCountryIso);
            if (KOREA_ISO_COUNTRY_CODE.equals(defaultCountryIso) && pn.getCountryCode() == util.getCountryCodeForRegion(KOREA_ISO_COUNTRY_CODE) && pn.getCountryCodeSource() == CountryCodeSource.FROM_NUMBER_WITH_PLUS_SIGN) {
                result = util.format(pn, PhoneNumberFormat.NATIONAL);
                if (HwFrameworkFactory.getHwInnerTelephonyManager().isCustomProcess()) {
                    result = HwFrameworkFactory.getHwInnerTelephonyManager().stripBrackets(result);
                }
                if (HwFrameworkFactory.getHwInnerTelephonyManager().isCustRemoveSep()) {
                    result = HwFrameworkFactory.getHwInnerTelephonyManager().removeAllSeparate(result);
                }
                return result;
            }
            result = util.formatInOriginalFormat(pn, defaultCountryIso);
            if (HwFrameworkFactory.getHwInnerTelephonyManager().isCustomProcess()) {
                result = HwFrameworkFactory.getHwInnerTelephonyManager().stripBrackets(result);
            }
            if (HwFrameworkFactory.getHwInnerTelephonyManager().isCustRemoveSep()) {
                result = HwFrameworkFactory.getHwInnerTelephonyManager().removeAllSeparate(result);
            }
            return result;
        } catch (NumberParseException e) {
        } catch (RuntimeException e2) {
            Rlog.d(LOG_TAG, "formatNumber RuntimeException");
        }
    }

    public static String formatNumber(String phoneNumber, String phoneNumberE164, String defaultCountryIso) {
        int len = phoneNumber.length();
        for (int i = FORMAT_UNKNOWN; i < len; i += NANP_STATE_DIGIT) {
            if (!isDialable(phoneNumber.charAt(i))) {
                return phoneNumber;
            }
        }
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        if (phoneNumberE164 != null && phoneNumberE164.length() >= NANP_STATE_PLUS && phoneNumberE164.charAt(FORMAT_UNKNOWN) == PLUS_SIGN_CHAR) {
            try {
                String regionCode = util.getRegionCodeForNumber(util.parse(phoneNumberE164, "ZZ"));
                if (!TextUtils.isEmpty(regionCode) && normalizeNumber(phoneNumber).indexOf(phoneNumberE164.substring(NANP_STATE_DIGIT)) <= 0) {
                    defaultCountryIso = regionCode;
                }
            } catch (NumberParseException e) {
            } catch (RuntimeException e2) {
                Rlog.e(LOG_TAG, "parsed number RuntimeException.");
                return phoneNumber;
            }
        }
        String result = formatNumber(phoneNumber, defaultCountryIso);
        if (HwFrameworkFactory.getHwInnerTelephonyManager().isCustomProcess()) {
            result = HwFrameworkFactory.getHwInnerTelephonyManager().stripBrackets(result);
        }
        if (HwFrameworkFactory.getHwInnerTelephonyManager().isCustRemoveSep()) {
            result = HwFrameworkFactory.getHwInnerTelephonyManager().removeAllSeparate(result);
        }
        if (result == null) {
            result = phoneNumber;
        }
        return result;
    }

    public static String normalizeNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int len = phoneNumber.length();
        for (int i = FORMAT_UNKNOWN; i < len; i += NANP_STATE_DIGIT) {
            char c = phoneNumber.charAt(i);
            int digit = Character.digit(c, NANP_LENGTH);
            if (digit != -1) {
                sb.append(digit);
            } else if (sb.length() == 0 && c == PLUS_SIGN_CHAR) {
                sb.append(c);
            } else {
                if (c < DateFormat.AM_PM || c > DateFormat.TIME_ZONE) {
                    if (c >= DateFormat.CAPITAL_AM_PM && c <= 'Z') {
                    }
                }
                return normalizeNumber(convertKeypadLettersToDigits(phoneNumber));
            }
        }
        return sb.toString();
    }

    public static String replaceUnicodeDigits(String number) {
        StringBuilder normalizedDigits = new StringBuilder(number.length());
        char[] toCharArray = number.toCharArray();
        int length = toCharArray.length;
        for (int i = FORMAT_UNKNOWN; i < length; i += NANP_STATE_DIGIT) {
            char c = toCharArray[i];
            int digit = Character.digit(c, NANP_LENGTH);
            if (digit != -1) {
                normalizedDigits.append(digit);
            } else {
                normalizedDigits.append(c);
            }
        }
        return normalizedDigits.toString();
    }

    public static boolean isEmergencyNumber(String number) {
        return isEmergencyNumber(getDefaultVoiceSubId(), number);
    }

    public static boolean isEmergencyNumber(int subId, String number) {
        return isEmergencyNumberInternal(subId, number, true);
    }

    public static boolean isPotentialEmergencyNumber(String number) {
        return isPotentialEmergencyNumber(getDefaultVoiceSubId(), number);
    }

    public static boolean isPotentialEmergencyNumber(int subId, String number) {
        return isEmergencyNumberInternal(subId, number, (boolean) DBG);
    }

    private static boolean isEmergencyNumberInternal(String number, boolean useExactMatch) {
        return isEmergencyNumberInternal(getDefaultVoiceSubId(), number, useExactMatch);
    }

    private static boolean isEmergencyNumberInternal(int subId, String number, boolean useExactMatch) {
        return isEmergencyNumberInternal(subId, number, null, useExactMatch);
    }

    public static boolean isEmergencyNumber(String number, String defaultCountryIso) {
        return isEmergencyNumber(getDefaultVoiceSubId(), number, defaultCountryIso);
    }

    public static boolean isEmergencyNumber(int subId, String number, String defaultCountryIso) {
        return isEmergencyNumberInternal(subId, number, defaultCountryIso, true);
    }

    public static boolean isPotentialEmergencyNumber(String number, String defaultCountryIso) {
        return isPotentialEmergencyNumber(getDefaultVoiceSubId(), number, defaultCountryIso);
    }

    public static boolean isPotentialEmergencyNumber(int subId, String number, String defaultCountryIso) {
        return isEmergencyNumberInternal(subId, number, defaultCountryIso, DBG);
    }

    private static boolean isEmergencyNumberInternal(String number, String defaultCountryIso, boolean useExactMatch) {
        return isEmergencyNumberInternal(getDefaultVoiceSubId(), number, defaultCountryIso, useExactMatch);
    }

    private static boolean isEmergencyNumberInternal(int subId, String number, String defaultCountryIso, boolean useExactMatch) {
        if (number == null) {
            return DBG;
        }
        if (isUriNumber(number)) {
            return DBG;
        }
        number = extractNetworkPortionAlt(number);
        String emergencyNumbers = "";
        int slotId = SubscriptionManager.getSlotId(subId);
        emergencyNumbers = SystemProperties.get(slotId <= 0 ? "ril.ecclist" : "ril.ecclist" + slotId, "");
        Rlog.d(LOG_TAG, "slotId:" + slotId + " subId:" + subId + " country:" + defaultCountryIso + " emergencyNumbers: " + emergencyNumbers);
        if (TextUtils.isEmpty(emergencyNumbers)) {
            emergencyNumbers = SystemProperties.get("ro.ril.ecclist");
        }
        emergencyNumbers = HwFrameworkFactory.getHwInnerTelephonyManager().custExtraEmergencyNumbers((long) subId, emergencyNumbers);
        String[] split;
        int length;
        int i;
        String emergencyNum;
        if (!TextUtils.isEmpty(emergencyNumbers)) {
            split = emergencyNumbers.split(PtmLog.PAIRE_DELIMETER);
            length = split.length;
            for (i = FORMAT_UNKNOWN; i < length; i += NANP_STATE_DIGIT) {
                emergencyNum = split[i];
                if (useExactMatch || "BR".equalsIgnoreCase(defaultCountryIso)) {
                    if (number.equals(emergencyNum)) {
                        return true;
                    }
                } else if (number.startsWith(emergencyNum)) {
                    boolean isChinaRegion = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
                    if (isChinaRegion && number.equals(emergencyNum)) {
                        return true;
                    }
                    if (!isChinaRegion && number.startsWith(emergencyNum)) {
                        return true;
                    }
                } else {
                    continue;
                }
            }
            return DBG;
        } else if (HwFrameworkFactory.getHwInnerTelephonyManager().skipHardcodeEmergencyNumbers()) {
            return DBG;
        } else {
            Rlog.d(LOG_TAG, "System property doesn't provide any emergency numbers. Use embedded logic for determining ones.");
            split = (slotId < 0 ? "112,911,000,08,110,118,119,999" : "112,911").split(PtmLog.PAIRE_DELIMETER);
            length = split.length;
            for (i = FORMAT_UNKNOWN; i < length; i += NANP_STATE_DIGIT) {
                emergencyNum = split[i];
                if (useExactMatch) {
                    if (number.equals(emergencyNum)) {
                        return true;
                    }
                } else if (number.startsWith(emergencyNum)) {
                    return true;
                }
            }
            if (defaultCountryIso == null) {
                return DBG;
            }
            ShortNumberUtil util = new ShortNumberUtil();
            if (useExactMatch) {
                return util.isEmergencyNumber(number, defaultCountryIso);
            }
            return util.connectsToEmergencyNumber(number, defaultCountryIso);
        }
    }

    public static boolean isLocalEmergencyNumber(Context context, String number) {
        return isLocalEmergencyNumber(context, getDefaultVoiceSubId(), number);
    }

    public static boolean isLocalEmergencyNumber(Context context, int subId, String number) {
        return isLocalEmergencyNumberInternal(subId, number, context, true);
    }

    public static boolean isPotentialLocalEmergencyNumber(Context context, String number) {
        return isPotentialLocalEmergencyNumber(context, getDefaultVoiceSubId(), number);
    }

    public static boolean isPotentialLocalEmergencyNumber(Context context, int subId, String number) {
        return isLocalEmergencyNumberInternal(subId, number, context, DBG);
    }

    private static boolean isLocalEmergencyNumberInternal(String number, Context context, boolean useExactMatch) {
        return isLocalEmergencyNumberInternal(getDefaultVoiceSubId(), number, context, useExactMatch);
    }

    private static boolean isLocalEmergencyNumberInternal(int subId, String number, Context context, boolean useExactMatch) {
        String countryIso = getCountryIso(context);
        Rlog.w(LOG_TAG, "isLocalEmergencyNumberInternal" + countryIso);
        if (countryIso == null) {
            countryIso = context.getResources().getConfiguration().locale.getCountry();
            Rlog.w(LOG_TAG, "No CountryDetector; falling back to countryIso based on locale: " + countryIso);
        }
        if (HwFrameworkFactory.getHwInnerTelephonyManager().isHwCustNotEmergencyNumber(context, number)) {
            return DBG;
        }
        return isEmergencyNumberInternal(subId, number, countryIso, useExactMatch);
    }

    private static String getCountryIso(Context context) {
        Rlog.w(LOG_TAG, "getCountryIso " + sCountryDetector);
        if (sCountryDetector == null) {
            CountryDetector detector = (CountryDetector) context.getSystemService("country_detector");
            if (detector != null) {
                sCountryDetector = detector.detectCountry();
            }
        }
        if (sCountryDetector == null) {
            return null;
        }
        return sCountryDetector.getCountryIso();
    }

    public static String toLogSafePhoneNumber(String number) {
        if (number == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = FORMAT_UNKNOWN; i < number.length(); i += NANP_STATE_DIGIT) {
            char c = number.charAt(i);
            if (c == '-' || c == '@' || c == '.' || c == '[' || c == ']') {
                builder.append(c);
            } else {
                builder.append(StateProperty.TARGET_X);
            }
        }
        return builder.toString();
    }

    public static void resetCountryDetectorInfo() {
        sCountryDetector = null;
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

    public static boolean isVoiceMailNumber(Context context, int subId, String number) {
        boolean z = DBG;
        if (HwFrameworkFactory.getHwInnerTelephonyManager().isLongVoiceMailNumber(subId, number)) {
            return true;
        }
        TelephonyManager tm;
        if (context == null) {
            try {
                tm = TelephonyManager.getDefault();
            } catch (SecurityException e) {
                return DBG;
            }
        }
        tm = TelephonyManager.from(context);
        String vmNumber = tm.getVoiceMailNumber(subId);
        number = extractNetworkPortionAlt(number);
        if (!TextUtils.isEmpty(number)) {
            z = compare(number, vmNumber);
        }
        return z;
    }

    public static String convertKeypadLettersToDigits(String input) {
        if (input == null) {
            return input;
        }
        int len = input.length();
        if (len == 0) {
            return input;
        }
        char[] out = input.toCharArray();
        for (int i = FORMAT_UNKNOWN; i < len; i += NANP_STATE_DIGIT) {
            char c = out[i];
            out[i] = (char) KEYPAD_MAP.get(c, c);
        }
        return new String(out);
    }

    public static String cdmaCheckAndProcessPlusCode(String dialStr) {
        if (!TextUtils.isEmpty(dialStr) && isReallyDialable(dialStr.charAt(FORMAT_UNKNOWN)) && isNonSeparator(dialStr)) {
            String currIso = TelephonyManager.getDefault().getNetworkCountryIso();
            String defaultIso = TelephonyManager.getDefault().getSimCountryIso();
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                int subId = getCdmaSubId();
                currIso = TelephonyManager.getDefault().getNetworkCountryIso(subId);
                defaultIso = TelephonyManager.getDefault().getSimCountryIso(subId);
            }
            if (!(TextUtils.isEmpty(currIso) || TextUtils.isEmpty(defaultIso))) {
                return cdmaCheckAndProcessPlusCodeByNumberFormat(dialStr, getFormatTypeFromCountryCode(currIso), getFormatTypeFromCountryCode(defaultIso));
            }
        }
        return dialStr;
    }

    private static int getCdmaSubId() {
        int i = FORMAT_UNKNOWN;
        while (i < TelephonyManager.getDefault().getPhoneCount()) {
            int subId = SubscriptionManager.getSubId(i)[FORMAT_UNKNOWN];
            if (TelephonyManager.getDefault().getCurrentPhoneType(subId) == NANP_STATE_PLUS && TelephonyManager.getDefault().getSimState(i) == 5) {
                Rlog.d(LOG_TAG, "getCdmaSubId find cdma phone subId = " + subId);
                return subId;
            }
            i += NANP_STATE_DIGIT;
        }
        Rlog.d(LOG_TAG, "getCdmaSubId find none cdma phone return default 0 ");
        return FORMAT_UNKNOWN;
    }

    public static String cdmaCheckAndProcessPlusCodeForSms(String dialStr) {
        if (!TextUtils.isEmpty(dialStr) && isReallyDialable(dialStr.charAt(FORMAT_UNKNOWN)) && isNonSeparator(dialStr)) {
            String defaultIso = TelephonyManager.getDefault().getSimCountryIso();
            if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                defaultIso = TelephonyManager.getDefault().getSimCountryIso(getCdmaSubId());
            }
            if (!TextUtils.isEmpty(defaultIso)) {
                int format = getFormatTypeFromCountryCode(defaultIso);
                return cdmaCheckAndProcessPlusCodeByNumberFormat(dialStr, format, format);
            }
        }
        return dialStr;
    }

    public static String cdmaCheckAndProcessPlusCodeByNumberFormat(String dialStr, int currFormat, int defaultFormat) {
        String retStr = dialStr;
        boolean useNanp = (currFormat == defaultFormat && currFormat == NANP_STATE_DIGIT) ? true : DBG;
        if (!(dialStr == null || dialStr.lastIndexOf(PLUS_SIGN_STRING) == -1)) {
            String tempDialStr = dialStr;
            retStr = null;
            do {
                String networkDialStr;
                if (useNanp) {
                    networkDialStr = extractNetworkPortion(tempDialStr);
                } else {
                    networkDialStr = extractNetworkPortionAlt(tempDialStr);
                }
                networkDialStr = processPlusCode(networkDialStr, useNanp);
                if (!TextUtils.isEmpty(networkDialStr)) {
                    if (retStr == null) {
                        retStr = networkDialStr;
                    } else {
                        retStr = retStr.concat(networkDialStr);
                    }
                    String postDialStr = extractPostDialPortion(tempDialStr);
                    if (!TextUtils.isEmpty(postDialStr)) {
                        int dialableIndex = findDialableIndexFromPostDialStr(postDialStr);
                        if (dialableIndex >= NANP_STATE_DIGIT) {
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
                    Rlog.e("checkAndProcessPlusCode: null newDialStr", networkDialStr);
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
        Spannable spannable = Factory.getInstance().newSpannable(phoneNumber);
        addTtsSpan(spannable, FORMAT_UNKNOWN, spannable.length());
        return spannable;
    }

    public static void addTtsSpan(Spannable s, int start, int endExclusive) {
        s.setSpan(createTtsSpan(s.subSequence(start, endExclusive).toString()), start, endExclusive, 33);
    }

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
        PhoneNumber phoneNumber = null;
        try {
            phoneNumber = PhoneNumberUtil.getInstance().parse(phoneNumberString, null);
        } catch (NumberParseException e) {
        }
        TelephoneBuilder builder = new TelephoneBuilder();
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

    private static String splitAtNonNumerics(CharSequence number) {
        StringBuilder sb = new StringBuilder(number.length());
        for (int i = FORMAT_UNKNOWN; i < number.length(); i += NANP_STATE_DIGIT) {
            Object valueOf;
            if (isISODigit(number.charAt(i))) {
                valueOf = Character.valueOf(number.charAt(i));
            } else {
                valueOf = " ";
            }
            sb.append(valueOf);
        }
        return sb.toString().replaceAll(" +", " ").trim();
    }

    private static String getCurrentIdp(boolean useNanp) {
        if (useNanp) {
            return NANP_IDP_STRING;
        }
        String ps = SystemProperties.get(TelephonyProperties.PROPERTY_OPERATOR_IDP_STRING, PLUS_SIGN_STRING);
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            return TelephonyManager.getTelephonyProperty(getCdmaSubId(), TelephonyProperties.PROPERTY_OPERATOR_IDP_STRING, PLUS_SIGN_STRING);
        }
        return ps;
    }

    private static boolean isTwoToNine(char c) {
        if (c < '2' || c > '9') {
            return DBG;
        }
        return true;
    }

    private static int getFormatTypeFromCountryCode(String country) {
        int length = NANP_COUNTRIES.length;
        for (int i = FORMAT_UNKNOWN; i < length; i += NANP_STATE_DIGIT) {
            if (NANP_COUNTRIES[i].compareToIgnoreCase(country) == 0) {
                return NANP_STATE_DIGIT;
            }
        }
        if ("jp".compareToIgnoreCase(country) == 0) {
            return NANP_STATE_PLUS;
        }
        return FORMAT_UNKNOWN;
    }

    public static boolean isNanp(String dialStr) {
        if (dialStr == null) {
            Rlog.e("isNanp: null dialStr passed in", dialStr);
            return DBG;
        } else if (dialStr.length() != NANP_LENGTH || !isTwoToNine(dialStr.charAt(FORMAT_UNKNOWN)) || !isTwoToNine(dialStr.charAt(NANP_STATE_ONE))) {
            return DBG;
        } else {
            for (int i = NANP_STATE_DIGIT; i < NANP_LENGTH; i += NANP_STATE_DIGIT) {
                if (!isISODigit(dialStr.charAt(i))) {
                    return DBG;
                }
            }
            return true;
        }
    }

    private static boolean isOneNanp(String dialStr) {
        if (dialStr != null) {
            String newDialStr = dialStr.substring(NANP_STATE_DIGIT);
            if (dialStr.charAt(FORMAT_UNKNOWN) == '1' && isNanp(newDialStr)) {
                return true;
            }
            return DBG;
        }
        Rlog.e("isOneNanp: null dialStr passed in", dialStr);
        return DBG;
    }

    public static boolean isUriNumber(String number) {
        if (number != null) {
            return !number.contains("@") ? number.contains("%40") : true;
        } else {
            return DBG;
        }
    }

    public static String getUsernameFromUriNumber(String number) {
        int delimiterIndex = number.indexOf(64);
        if (delimiterIndex < 0) {
            delimiterIndex = number.indexOf("%40");
        }
        if (delimiterIndex < 0) {
            Rlog.w(LOG_TAG, "getUsernameFromUriNumber: no delimiter found in SIP addr '" + number + "'");
            delimiterIndex = number.length();
        }
        return number.substring(FORMAT_UNKNOWN, delimiterIndex);
    }

    private static String processPlusCode(String networkDialStr, boolean useNanp) {
        String retStr = networkDialStr;
        if (networkDialStr == null || networkDialStr.charAt(FORMAT_UNKNOWN) != PLUS_SIGN_CHAR || networkDialStr.length() <= NANP_STATE_DIGIT) {
            return retStr;
        }
        String newStr = networkDialStr.substring(NANP_STATE_DIGIT);
        if (useNanp && isOneNanp(newStr)) {
            return newStr;
        }
        return networkDialStr.replaceFirst("[+]", getCurrentIdp(useNanp));
    }

    private static int findDialableIndexFromPostDialStr(String postDialStr) {
        for (int index = FORMAT_UNKNOWN; index < postDialStr.length(); index += NANP_STATE_DIGIT) {
            if (isReallyDialable(postDialStr.charAt(index))) {
                return index;
            }
        }
        return -1;
    }

    private static String appendPwCharBackToOrigDialStr(int dialableIndex, String origStr, String dialStr) {
        if (dialableIndex == NANP_STATE_DIGIT) {
            return dialStr.charAt(FORMAT_UNKNOWN);
        }
        return origStr.concat(dialStr.substring(FORMAT_UNKNOWN, dialableIndex));
    }

    private static boolean matchIntlPrefix(String a, int len) {
        boolean z = true;
        int state = FORMAT_UNKNOWN;
        for (int i = FORMAT_UNKNOWN; i < len; i += NANP_STATE_DIGIT) {
            char c = a.charAt(i);
            switch (state) {
                case FORMAT_UNKNOWN /*0*/:
                    if (c != PLUS_SIGN_CHAR) {
                        if (c != '0') {
                            if (!isNonSeparator(c)) {
                                break;
                            }
                            return DBG;
                        }
                        state = NANP_STATE_PLUS;
                        break;
                    }
                    state = NANP_STATE_DIGIT;
                    break;
                case NANP_STATE_PLUS /*2*/:
                    if (c != '0') {
                        if (c != '1') {
                            if (!isNonSeparator(c)) {
                                break;
                            }
                            return DBG;
                        }
                        state = NANP_STATE_DASH;
                        break;
                    }
                    state = NANP_STATE_ONE;
                    break;
                case NANP_STATE_DASH /*4*/:
                    if (c != '1') {
                        if (!isNonSeparator(c)) {
                            break;
                        }
                        return DBG;
                    }
                    state = 5;
                    break;
                default:
                    if (!isNonSeparator(c)) {
                        break;
                    }
                    return DBG;
            }
        }
        if (!(state == NANP_STATE_DIGIT || state == NANP_STATE_ONE || state == 5)) {
            z = DBG;
        }
        return z;
    }

    private static boolean matchIntlPrefixAndCC(String a, int len) {
        boolean z = true;
        int state = FORMAT_UNKNOWN;
        for (int i = FORMAT_UNKNOWN; i < len; i += NANP_STATE_DIGIT) {
            char c = a.charAt(i);
            switch (state) {
                case FORMAT_UNKNOWN /*0*/:
                    if (c != PLUS_SIGN_CHAR) {
                        if (c != '0') {
                            if (!isNonSeparator(c)) {
                                break;
                            }
                            return DBG;
                        }
                        state = NANP_STATE_PLUS;
                        break;
                    }
                    state = NANP_STATE_DIGIT;
                    break;
                case NANP_STATE_DIGIT /*1*/:
                case NANP_STATE_ONE /*3*/:
                case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                    if (!isISODigit(c)) {
                        if (!isNonSeparator(c)) {
                            break;
                        }
                        return DBG;
                    }
                    state = 6;
                    break;
                case NANP_STATE_PLUS /*2*/:
                    if (c != '0') {
                        if (c != '1') {
                            if (!isNonSeparator(c)) {
                                break;
                            }
                            return DBG;
                        }
                        state = NANP_STATE_DASH;
                        break;
                    }
                    state = NANP_STATE_ONE;
                    break;
                case NANP_STATE_DASH /*4*/:
                    if (c != '1') {
                        if (!isNonSeparator(c)) {
                            break;
                        }
                        return DBG;
                    }
                    state = 5;
                    break;
                case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                case MIN_MATCH /*7*/:
                    if (!isISODigit(c)) {
                        if (!isNonSeparator(c)) {
                            break;
                        }
                        return DBG;
                    }
                    state += NANP_STATE_DIGIT;
                    break;
                default:
                    if (!isNonSeparator(c)) {
                        break;
                    }
                    return DBG;
            }
        }
        if (!(state == 6 || state == MIN_MATCH || state == 8)) {
            z = DBG;
        }
        return z;
    }

    private static boolean matchTrunkPrefix(String a, int len) {
        boolean found = DBG;
        for (int i = FORMAT_UNKNOWN; i < len; i += NANP_STATE_DIGIT) {
            char c = a.charAt(i);
            if (c == '0' && !found) {
                found = true;
            } else if (isNonSeparator(c)) {
                return DBG;
            }
        }
        return found;
    }

    private static boolean isCountryCallingCode(int countryCallingCodeCandidate) {
        if (countryCallingCodeCandidate <= 0 || countryCallingCodeCandidate >= CCC_LENGTH) {
            return DBG;
        }
        return COUNTRY_CALLING_CALL[countryCallingCodeCandidate];
    }

    private static int tryGetISODigit(char ch) {
        if ('0' > ch || ch > '9') {
            return -1;
        }
        return ch - 48;
    }

    private static CountryCallingCodeAndNewIndex tryGetCountryCallingCodeAndNewIndex(String str, boolean acceptThailandCase) {
        int state = FORMAT_UNKNOWN;
        int ccc = FORMAT_UNKNOWN;
        int length = str.length();
        for (int i = FORMAT_UNKNOWN; i < length; i += NANP_STATE_DIGIT) {
            char ch = str.charAt(i);
            switch (state) {
                case FORMAT_UNKNOWN /*0*/:
                    if (ch != PLUS_SIGN_CHAR) {
                        if (ch != '0') {
                            if (ch != '1') {
                                if (!isDialable(ch)) {
                                    break;
                                }
                                return null;
                            } else if (acceptThailandCase) {
                                state = 8;
                                break;
                            } else {
                                return null;
                            }
                        }
                        state = NANP_STATE_PLUS;
                        break;
                    }
                    state = NANP_STATE_DIGIT;
                    break;
                case NANP_STATE_DIGIT /*1*/:
                case NANP_STATE_ONE /*3*/:
                case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                case MIN_MATCH /*7*/:
                    int ret = tryGetISODigit(ch);
                    if (ret <= 0) {
                        if (!isDialable(ch)) {
                            break;
                        }
                        return null;
                    }
                    ccc = (ccc * NANP_LENGTH) + ret;
                    if (ccc < 100 && !isCountryCallingCode(ccc)) {
                        if (state != NANP_STATE_DIGIT && state != NANP_STATE_ONE && state != 5) {
                            state += NANP_STATE_DIGIT;
                            break;
                        }
                        state = 6;
                        break;
                    }
                    return new CountryCallingCodeAndNewIndex(ccc, i + NANP_STATE_DIGIT);
                    break;
                case NANP_STATE_PLUS /*2*/:
                    if (ch != '0') {
                        if (ch != '1') {
                            if (!isDialable(ch)) {
                                break;
                            }
                            return null;
                        }
                        state = NANP_STATE_DASH;
                        break;
                    }
                    state = NANP_STATE_ONE;
                    break;
                case NANP_STATE_DASH /*4*/:
                    if (ch != '1') {
                        if (!isDialable(ch)) {
                            break;
                        }
                        return null;
                    }
                    state = 5;
                    break;
                case PGSdk.TYPE_VIDEO /*8*/:
                    if (ch != '6') {
                        if (!isDialable(ch)) {
                            break;
                        }
                        return null;
                    }
                    state = 9;
                    break;
                case PGSdk.TYPE_SCRLOCK /*9*/:
                    if (ch == '6') {
                        return new CountryCallingCodeAndNewIndex(66, i + NANP_STATE_DIGIT);
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
        for (int i = currentIndex; i < length; i += NANP_STATE_DIGIT) {
            char ch = str.charAt(i);
            if (tryGetISODigit(ch) >= 0) {
                return i + NANP_STATE_DIGIT;
            }
            if (isDialable(ch)) {
                return -1;
            }
        }
        return -1;
    }

    private static boolean checkPrefixIsIgnorable(String str, int forwardIndex, int backwardIndex) {
        boolean trunk_prefix_was_read = DBG;
        while (backwardIndex >= forwardIndex) {
            if (tryGetISODigit(str.charAt(backwardIndex)) >= 0) {
                if (trunk_prefix_was_read) {
                    return DBG;
                }
                trunk_prefix_was_read = true;
            } else if (isDialable(str.charAt(backwardIndex))) {
                return DBG;
            }
            backwardIndex--;
        }
        return true;
    }

    private static int getDefaultVoiceSubId() {
        return SubscriptionManager.getDefaultVoiceSubscriptionId();
    }
}
