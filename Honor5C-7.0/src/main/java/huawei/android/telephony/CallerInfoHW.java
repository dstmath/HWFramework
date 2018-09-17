package huawei.android.telephony;

import android.database.Cursor;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import com.android.i18n.phonenumbers.CountryCodeToRegionCodeMapUtils;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.internal.telephony.HwTelephonyProperties;
import huawei.android.provider.HanziToPinyin.Token;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CallerInfoHW implements TelephonyInterfacesHW {
    private static final String CHINA_AREACODE = "0";
    private static final String CHINA_OPERATOR_MCC = "460";
    private static final int CN_FIXED_NUMBER_WITH_AREA_CODE_MIN_LEN = 9;
    private static final String CN_MPN_PATTERN = "^(1)\\d{10}$";
    private static final int CN_NUM_MATCH = 11;
    private static final String FIXED_NUMBER_TOP2_TOKEN1 = "01";
    private static final String FIXED_NUMBER_TOP2_TOKEN2 = "02";
    private static final String[] INTERNATIONAL_PREFIX = null;
    private static final String[] IPHEAD = null;
    private static final int IPHEAD_LENTH = 5;
    public static final int MIN_MATCH = 7;
    private static final String[] NORMAL_PREFIX_MCC = null;
    private static final String TAG = "CallerInfo";
    private static CallerInfoHW sCallerInfoHwInstance;
    private static PhoneNumberUtil sInstance;
    private boolean IS_CHINA_TELECOM;
    private boolean IS_MIIT_NUM_MATCH;
    private int NUM_LONG;
    private int NUM_SHORT;
    private final Map<Integer, ArrayList<String>> chineseFixNumberAreaCodeMap;
    private int configMatchNum;
    private int configMatchNumShort;
    private final Map<Integer, List<String>> countryCallingCodeToRegionCodeMap;
    private int countryCodeforCN;
    private String mNetworkOperator;
    private int mSimNumLong;
    private int mSimNumShort;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.telephony.CallerInfoHW.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.telephony.CallerInfoHW.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.telephony.CallerInfoHW.<clinit>():void");
    }

    public CallerInfoHW() {
        boolean equals;
        int i = MIN_MATCH;
        this.configMatchNum = SystemProperties.getInt("ro.config.hwft_MatchNum", MIN_MATCH);
        if (this.configMatchNum >= MIN_MATCH) {
            i = this.configMatchNum;
        }
        this.NUM_LONG = i;
        this.configMatchNumShort = SystemProperties.getInt("ro.config.hwft_MatchNumShort", this.NUM_LONG);
        this.NUM_SHORT = this.configMatchNumShort >= this.NUM_LONG ? this.NUM_LONG : this.configMatchNumShort;
        this.mSimNumLong = 0;
        this.mSimNumShort = 0;
        if (SystemProperties.get("ro.config.hw_opta", CHINA_AREACODE).equals("92")) {
            equals = SystemProperties.get("ro.config.hw_optb", CHINA_AREACODE).equals("156");
        } else {
            equals = false;
        }
        this.IS_CHINA_TELECOM = equals;
        this.IS_MIIT_NUM_MATCH = SystemProperties.getBoolean("ro.config.miit_number_match", false);
        this.mNetworkOperator = null;
        this.countryCodeforCN = sInstance.getCountryCodeForRegion("CN");
        this.countryCallingCodeToRegionCodeMap = CountryCodeToRegionCodeMapUtils.getCountryCodeToRegionCodeMap();
        this.chineseFixNumberAreaCodeMap = ChineseFixNumberAreaCodeMap.getChineseFixNumberAreaCodeMap();
    }

    public static synchronized CallerInfoHW getInstance() {
        CallerInfoHW callerInfoHW;
        synchronized (CallerInfoHW.class) {
            if (sCallerInfoHwInstance == null) {
                sCallerInfoHwInstance = new CallerInfoHW();
            }
            callerInfoHW = sCallerInfoHwInstance;
        }
        return callerInfoHW;
    }

    public String getCountryIsoFromDbNumber(String number) {
        logd("getCountryIsoFromDbNumber(), number: " + number);
        if (TextUtils.isEmpty(number)) {
            return null;
        }
        int len = getIntlPrefixLength(number);
        if (len > 0) {
            String tmpNumber = number.substring(len);
            for (Integer intValue : this.countryCallingCodeToRegionCodeMap.keySet()) {
                int countrycode = intValue.intValue();
                if (tmpNumber.startsWith(Integer.toString(countrycode))) {
                    String countryIso = sInstance.getRegionCodeForCountryCode(countrycode);
                    logd("getCountryIsoFromDbNumber(), find matched country code: " + countrycode + ", and country iso: " + countryIso);
                    return countryIso;
                }
            }
            logd("getCountryIsoFromDbNumber(), no matched country code, returns null");
        }
        return null;
    }

    public boolean compareNums(String num1, String netIso1, String num2, String netIso2) {
        boolean z = false;
        String str = null;
        String str2 = null;
        String str3 = null;
        String str4 = null;
        if (num1 == null || num2 == null) {
            return false;
        }
        int numMatch;
        int numMatchShort;
        logd("compareNums, num1 = " + num1 + ", netIso1 = " + netIso1 + ", num2 = " + num2 + ", netIso2 = " + netIso2);
        if (SystemProperties.getInt("ro.config.hwft_MatchNum", 0) == 0) {
            numMatch = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH, MIN_MATCH);
            numMatchShort = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT, numMatch);
            if (numMatch < MIN_MATCH) {
                numMatch = MIN_MATCH;
            }
            this.mSimNumLong = numMatch;
            this.NUM_LONG = numMatch;
            if (numMatchShort >= this.NUM_LONG) {
                numMatchShort = this.NUM_LONG;
            }
            this.mSimNumShort = numMatchShort;
            this.NUM_SHORT = numMatchShort;
            int i = this.NUM_LONG;
            logd("compareNums, after setprop NUM_LONG = " + r0 + ", NUM_SHORT = " + this.NUM_SHORT);
        }
        if (num1.indexOf(64) < 0) {
            num1 = PhoneNumberUtils.stripSeparators(num1);
        }
        if (num2.indexOf(64) < 0) {
            num2 = PhoneNumberUtils.stripSeparators(num2);
        }
        if (this.IS_CHINA_TELECOM) {
            if (num1.startsWith("**133")) {
                if (num1.endsWith("#")) {
                    num1 = num1.substring(0, num1.length() - 1);
                    logd("compareNums, num1 startsWith **133 && endsWith #");
                }
            }
        }
        if (this.IS_CHINA_TELECOM) {
            if (num2.startsWith("**133")) {
                if (num2.endsWith("#")) {
                    num2 = num2.substring(0, num2.length() - 1);
                    logd("compareNums, num2 startsWith **133 && endsWith #");
                }
            }
        }
        if (num1.equals(num2)) {
            logd("compareNums, full compare returns true.");
            return true;
        }
        String origNum1 = num1;
        String origNum2 = num2;
        if (!TextUtils.isEmpty(netIso1)) {
            String formattedNum1 = PhoneNumberUtils.formatNumberToE164(num1, netIso1.toUpperCase(Locale.US));
            if (formattedNum1 != null) {
                logd("compareNums, formattedNum1: " + formattedNum1 + ", with netIso1: " + netIso1);
                num1 = formattedNum1;
            }
        }
        if (!TextUtils.isEmpty(netIso2)) {
            String formattedNum2 = PhoneNumberUtils.formatNumberToE164(num2, netIso2.toUpperCase(Locale.US));
            if (formattedNum2 != null) {
                logd("compareNums, formattedNum2: " + formattedNum2 + ", with netIso2: " + netIso2);
                num2 = formattedNum2;
            }
        }
        if (num1.equals(num2)) {
            logd("compareNums, full compare for formatted number returns true.");
            return true;
        }
        int countryCodeLen1 = getIntlPrefixAndCCLen(num1);
        if (countryCodeLen1 > 0) {
            str = num1.substring(0, countryCodeLen1);
            num1 = num1.substring(countryCodeLen1);
            logd("compareNums, num1 after remove prefix: " + num1 + ", num1Prefix: " + str);
        }
        int countryCodeLen2 = getIntlPrefixAndCCLen(num2);
        if (countryCodeLen2 > 0) {
            str2 = num2.substring(0, countryCodeLen2);
            num2 = num2.substring(countryCodeLen2);
            logd("compareNums, num2 after remove prefix: " + num2 + ", num2Prefix: " + str2);
        }
        if (isRoamingCountryNumberByPrefix(str, netIso1) || isRoamingCountryNumberByPrefix(str2, netIso2)) {
            logd("compareNums, num1 or num2 belong to roaming country");
            numMatch = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_ROAMING, MIN_MATCH);
            numMatchShort = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT_ROAMING, numMatch);
            if (numMatch < MIN_MATCH) {
                numMatch = MIN_MATCH;
            }
            this.NUM_LONG = numMatch;
            if (numMatchShort >= this.NUM_LONG) {
                numMatchShort = this.NUM_LONG;
            }
            this.NUM_SHORT = numMatchShort;
            i = this.NUM_LONG;
            logd("compareNums, roaming prop NUM_LONG = " + r0 + ", NUM_SHORT = " + this.NUM_SHORT);
        }
        if (isEqualCountryCodePrefix(str, netIso1, str2, netIso2)) {
            int areaCodeLen;
            boolean isNum1CnNumber = isChineseNumberByPrefix(str, netIso1);
            boolean z2 = false;
            if (isNum1CnNumber) {
                this.NUM_LONG = CN_NUM_MATCH;
                if (num1 != null) {
                    if (num1.startsWith("86")) {
                        num1 = num1.substring(2);
                    }
                }
                num1 = deleteIPHead(num1);
                z2 = isChineseMobilePhoneNumber(num1);
                if (!z2) {
                    areaCodeLen = getChineseFixNumberAreaCodeLength(num1);
                    if (areaCodeLen > 0) {
                        str3 = num1.substring(0, areaCodeLen);
                        num1 = num1.substring(areaCodeLen);
                        logd("compareNums, CN num1 after remove area code: " + num1 + ", num1AreaCode: " + str3);
                    }
                }
            } else {
                if ("PE".equalsIgnoreCase(netIso1)) {
                    logd("compareNums, PE num1 start with 0 not remove it");
                } else if (num1.length() >= MIN_MATCH && CHINA_AREACODE.equals(num1.substring(0, 1)) && !CHINA_AREACODE.equals(num1.substring(1, 2))) {
                    num1 = num1.substring(1);
                    logd("compareNums, num1 remove 0 at beginning");
                }
            }
            boolean isNum2CnNumber = isChineseNumberByPrefix(str2, netIso2);
            boolean z3 = false;
            if (isNum2CnNumber) {
                this.NUM_LONG = CN_NUM_MATCH;
                if (num2 != null) {
                    if (num2.startsWith("86")) {
                        num2 = num2.substring(2);
                    }
                }
                num2 = deleteIPHead(num2);
                z3 = isChineseMobilePhoneNumber(num2);
                if (!z3) {
                    areaCodeLen = getChineseFixNumberAreaCodeLength(num2);
                    if (areaCodeLen > 0) {
                        str4 = num2.substring(0, areaCodeLen);
                        num2 = num2.substring(areaCodeLen);
                        logd("compareNums, CN num2 after remove area code: " + num2 + ", num2AreaCode: " + str4);
                    }
                }
            } else {
                if ("PE".equalsIgnoreCase(netIso2)) {
                    logd("compareNums, PE num2 start with 0 not remove it");
                } else if (num2.length() >= MIN_MATCH && CHINA_AREACODE.equals(num2.substring(0, 1)) && !CHINA_AREACODE.equals(num2.substring(1, 2))) {
                    num2 = num2.substring(1);
                    logd("compareNums, num2 remove 0 at beginning");
                }
            }
            if ((!z2 || r12) && (z2 || !r12)) {
                if (z2 && r12) {
                    logd("compareNums, num1 and num2 are both MPN, continue to compare");
                } else if (isNum1CnNumber && isNum2CnNumber && !isEqualChineseFixNumberAreaCode(r14, r17)) {
                    logd("compareNums, areacode prefix not same, return false");
                    return false;
                }
                return compareNumsInternal(num1, num2, this.NUM_LONG, this.NUM_SHORT);
            }
            if (shouldDoNumberMatchAgainBySimMccmnc(origNum1, netIso1) || shouldDoNumberMatchAgainBySimMccmnc(origNum2, netIso2)) {
                z = compareNumsInternal(origNum1, origNum2, this.mSimNumLong, this.mSimNumShort);
            }
            logd("compareNums, num1 and num2 not both MPN, return " + z);
            return z;
        }
        if (shouldDoNumberMatchAgainBySimMccmnc(origNum1, netIso1) || shouldDoNumberMatchAgainBySimMccmnc(origNum2, netIso2)) {
            z = compareNumsInternal(origNum1, origNum2, this.mSimNumLong, this.mSimNumShort);
        }
        logd("compareNums, countrycode prefix not same, return " + z);
        return z;
    }

    public boolean compareNums(String num1, String num2) {
        if (num1 == null || num2 == null) {
            return false;
        }
        logd("compareNums, num1 = " + num1 + ", num2 = " + num2);
        if (SystemProperties.getInt("ro.config.hwft_MatchNum", 0) == 0) {
            int numMatch = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH, MIN_MATCH);
            int numMatchShort = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT, numMatch);
            if (numMatch < MIN_MATCH) {
                numMatch = MIN_MATCH;
            }
            this.NUM_LONG = numMatch;
            if (numMatchShort >= this.NUM_LONG) {
                numMatchShort = this.NUM_LONG;
            }
            this.NUM_SHORT = numMatchShort;
            logd("compareNums, after setprop NUM_LONG = " + this.NUM_LONG + ", NUM_SHORT = " + this.NUM_SHORT);
        }
        if (num1.indexOf(64) < 0) {
            num1 = PhoneNumberUtils.stripSeparators(num1);
        }
        if (num2.indexOf(64) < 0) {
            num2 = PhoneNumberUtils.stripSeparators(num2);
        }
        if (this.IS_CHINA_TELECOM && num1.startsWith("**133") && num1.endsWith("#")) {
            num1 = num1.substring(0, num1.length() - 1);
            logd("compareNums, num1 startsWith **133 && endsWith #");
        }
        if (this.IS_CHINA_TELECOM && num2.startsWith("**133") && num2.endsWith("#")) {
            num2 = num2.substring(0, num2.length() - 1);
            logd("compareNums, num2 startsWith **133 && endsWith #");
        }
        if (this.NUM_SHORT < this.NUM_LONG) {
            logd("compareNums, NUM_SHORT have been set! Only do full compare.");
            return num1.equals(num2);
        }
        int num1Len = num1.length();
        int num2Len = num2.length();
        if (num1Len > this.NUM_LONG) {
            num1 = num1.substring(num1Len - this.NUM_LONG);
        }
        if (num2Len > this.NUM_LONG) {
            num2 = num2.substring(num2Len - this.NUM_LONG);
        }
        logd("compareNums, new num1 = " + num1 + ", new num2 = " + num2);
        return num1.equals(num2);
    }

    public int getCallerIndex(Cursor cursor, String compNum) {
        return getCallerIndex(cursor, compNum, "number");
    }

    public int getCallerIndex(Cursor cursor, String compNum, String columnName) {
        return getCallerIndex(cursor, compNum, columnName, SystemProperties.get(HwTelephonyProperties.PROPERTY_NETWORK_COUNTRY_ISO, ""));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getCallerIndex(Cursor cursor, String compNum, String columnName, String countryIso) {
        String str = null;
        String str2 = null;
        String tmpNumFormat = null;
        int numShortID = -1;
        int numLongID = -1;
        int fixedIndex = -1;
        if (TextUtils.isEmpty(compNum)) {
            if (cursor != null && cursor.getCount() > 0) {
                fixedIndex = 0;
            }
            Log.e(TAG, "CallerInfoHW(),null == compNum! fixedIndex = " + fixedIndex);
            return fixedIndex;
        } else if (cursor == null || cursor.getCount() <= 0) {
            Log.e(TAG, "CallerInfoHW(), cursor is empty! fixedIndex = " + -1);
            return -1;
        } else {
            fixedIndex = getFullMatchIndex(cursor, compNum, columnName);
            if (-1 != fixedIndex) {
                return fixedIndex;
            }
            int numMatch;
            int numMatchShort;
            int areaCodeLen;
            logd("getCallerIndex(), not full match proceed to check..");
            logd("getCallerIndex(), NUM_LONG = " + this.NUM_LONG + ",NUM_SHORT = " + this.NUM_SHORT);
            if (SystemProperties.getInt("ro.config.hwft_MatchNum", 0) == 0) {
                numMatch = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH, MIN_MATCH);
                numMatchShort = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT, numMatch);
                if (numMatch < MIN_MATCH) {
                    numMatch = MIN_MATCH;
                }
                this.mSimNumLong = numMatch;
                this.NUM_LONG = numMatch;
                if (numMatchShort >= this.NUM_LONG) {
                    numMatchShort = this.NUM_LONG;
                }
                this.mSimNumShort = numMatchShort;
                this.NUM_SHORT = numMatchShort;
                logd("getCallerIndex(), after setprop NUM_LONG = " + this.NUM_LONG + ", NUM_SHORT = " + this.NUM_SHORT);
            }
            compNum = PhoneNumberUtils.stripSeparators(compNum);
            int compNumLen = compNum.length();
            logd("compNum: " + compNum + ", countryIso: " + countryIso);
            if (this.IS_CHINA_TELECOM) {
                if (compNum.startsWith("**133")) {
                    if (compNum.endsWith("#")) {
                        compNum = compNum.substring(0, compNum.length() - 1);
                        logd("compNum startsWith **133 && endsWith #");
                    }
                }
            }
            String origCompNum = compNum;
            this.mNetworkOperator = SystemProperties.get(HwTelephonyProperties.PROPERTY_NETWORK_OPERATOR, "");
            CharSequence charSequence = null;
            if (!TextUtils.isEmpty(countryIso)) {
                charSequence = PhoneNumberUtils.formatNumberToE164(compNum, countryIso.toUpperCase(Locale.US));
                if (charSequence != null) {
                    logd("formattedCompNum: " + charSequence + ", with countryIso: " + countryIso);
                    compNum = charSequence;
                }
            }
            int countryCodeLen = getIntlPrefixAndCCLen(compNum);
            if (countryCodeLen > 0) {
                str = compNum.substring(0, countryCodeLen);
                compNum = compNum.substring(countryCodeLen);
                logd("compNum after remove prefix: " + compNum + ", compNumLen: " + compNum.length() + ", compNumPrefix: " + str);
            }
            String tmpCompNum = !TextUtils.isEmpty(charSequence) ? charSequence : origCompNum;
            if (isRoamingCountryNumberByPrefix(str, countryIso)) {
                logd("compNum belongs to roaming country");
                numMatch = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_ROAMING, MIN_MATCH);
                numMatchShort = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT_ROAMING, numMatch);
                if (numMatch < MIN_MATCH) {
                    numMatch = MIN_MATCH;
                }
                this.NUM_LONG = numMatch;
                if (numMatchShort >= this.NUM_LONG) {
                    numMatchShort = this.NUM_LONG;
                }
                this.NUM_SHORT = numMatchShort;
                logd("getCallerIndex(), roaming prop NUM_LONG = " + this.NUM_LONG + ", NUM_SHORT = " + this.NUM_SHORT);
            }
            boolean isCnNumber = isChineseNumberByPrefix(str, countryIso);
            boolean z = false;
            if (isCnNumber) {
                this.NUM_SHORT = CN_NUM_MATCH;
                this.NUM_LONG = CN_NUM_MATCH;
                if (compNum != null) {
                    if (compNum.startsWith("86")) {
                        compNum = compNum.substring(2);
                    }
                }
                compNum = deleteIPHead(compNum);
                z = isChineseMobilePhoneNumber(compNum);
                if (!z) {
                    areaCodeLen = getChineseFixNumberAreaCodeLength(compNum);
                    if (areaCodeLen > 0) {
                        str2 = compNum.substring(0, areaCodeLen);
                        compNum = compNum.substring(areaCodeLen);
                        logd("CN compNum after remove area code: " + compNum + ", compNumLen: " + compNum.length() + ", compNumAreaCode: " + str2);
                    }
                }
            } else if ("PE".equalsIgnoreCase(countryIso)) {
                logd("PE compNum start with 0 not remove it");
            } else if (compNum.length() >= MIN_MATCH && CHINA_AREACODE.equals(compNum.substring(0, 1)) && !CHINA_AREACODE.equals(compNum.substring(1, 2))) {
                compNum = compNum.substring(1);
            }
            compNumLen = compNum.length();
            if (cursor != null) {
                String compNumShort;
                int columnIndex;
                int formatColumnIndex;
                int data4ColumnIndex;
                String tmpNum;
                String origTmpNum;
                int tmpNumLen;
                String tmpNumPrefix;
                String tmpNumAreaCode;
                boolean isTmpNumCnMPN;
                String tmpNumShort;
                if (compNumLen >= this.NUM_LONG) {
                    String compNumLong = compNum.substring(compNumLen - this.NUM_LONG);
                    compNumShort = compNum.substring(compNumLen - this.NUM_SHORT);
                    logd("11:, compNumLong = " + compNumLong + ",compNumShort = " + compNumShort);
                    if (cursor.moveToFirst()) {
                        columnIndex = cursor.getColumnIndex(columnName);
                        formatColumnIndex = cursor.getColumnIndex("normalized_number");
                        data4ColumnIndex = cursor.getColumnIndex("data4");
                        logd("11: columnIndex: " + columnIndex + ", formatColumnIndex: " + formatColumnIndex + ", data4ColumnIndex: " + data4ColumnIndex);
                        if (columnIndex != -1) {
                            while (true) {
                                tmpNum = cursor.getString(columnIndex);
                                origTmpNum = tmpNum;
                                if (tmpNum != null && tmpNum.indexOf(64) < 0) {
                                    origTmpNum = PhoneNumberUtils.stripSeparators(tmpNum);
                                    logd("origTmpNum: " + origTmpNum);
                                    if (-1 != formatColumnIndex) {
                                        tmpNumFormat = cursor.getString(formatColumnIndex);
                                        tmpNum = isValidData4Number(origTmpNum, tmpNumFormat) ? tmpNumFormat : origTmpNum;
                                    } else if (-1 != data4ColumnIndex) {
                                        tmpNumFormat = cursor.getString(data4ColumnIndex);
                                        tmpNum = isValidData4Number(origTmpNum, tmpNumFormat) ? tmpNumFormat : origTmpNum;
                                    } else {
                                        tmpNum = origTmpNum;
                                    }
                                    logd("11: tmpNumFormat: " + tmpNumFormat);
                                    tmpNumLen = tmpNum.length();
                                    logd("11: tmpNum = " + tmpNum + ", tmpNum.length11: " + tmpNum.length() + ",ID = " + cursor.getPosition());
                                    if (!tmpNum.equals(tmpCompNum)) {
                                        tmpNumPrefix = null;
                                        tmpNumAreaCode = null;
                                        countryCodeLen = getIntlPrefixAndCCLen(tmpNum);
                                        if (countryCodeLen > 0) {
                                            tmpNumPrefix = tmpNum.substring(0, countryCodeLen);
                                            tmpNum = tmpNum.substring(countryCodeLen);
                                            logd("11: tmpNum after remove prefix: " + tmpNum + ", tmpNum.length11: " + tmpNum.length() + ", tmpNumPrefix: " + tmpNumPrefix);
                                        }
                                        if (isEqualCountryCodePrefix(str, countryIso, tmpNumPrefix, null)) {
                                            if (isCnNumber) {
                                                tmpNum = deleteIPHead(tmpNum);
                                                isTmpNumCnMPN = isChineseMobilePhoneNumber(tmpNum);
                                                if ((z && !isTmpNumCnMPN) || (!z && isTmpNumCnMPN)) {
                                                    logd("11: compNum and tmpNum not both MPN, continue");
                                                } else if (z && isTmpNumCnMPN) {
                                                    logd("11: compNum and tmpNum are both MPN, continue to match by mccmnc");
                                                } else {
                                                    areaCodeLen = getChineseFixNumberAreaCodeLength(tmpNum);
                                                    if (areaCodeLen > 0) {
                                                        tmpNumAreaCode = tmpNum.substring(0, areaCodeLen);
                                                        tmpNum = tmpNum.substring(areaCodeLen);
                                                        logd("11: CN tmpNum after remove area code: " + tmpNum + ", tmpNum.length11: " + tmpNum.length() + ", tmpNumAreaCode: " + tmpNumAreaCode);
                                                    }
                                                    if (!isEqualChineseFixNumberAreaCode(str2, tmpNumAreaCode)) {
                                                        logd("11: areacode prefix not same, continue");
                                                    }
                                                }
                                            } else if (tmpNum.length() >= MIN_MATCH && CHINA_AREACODE.equals(tmpNum.substring(0, 1)) && !CHINA_AREACODE.equals(tmpNum.substring(1, 2))) {
                                                tmpNum = tmpNum.substring(1);
                                                logd("11: tmpNum remove 0 at beginning");
                                            }
                                            tmpNumLen = tmpNum.length();
                                            if (tmpNumLen >= this.NUM_LONG) {
                                                String tmpNumLong = tmpNum.substring(tmpNumLen - this.NUM_LONG);
                                                if (-1 == numLongID && compNumLong.compareTo(tmpNumLong) == 0) {
                                                    numLongID = cursor.getPosition();
                                                    logd("11: > NUM_LONG numLongID = " + numLongID);
                                                } else {
                                                    logd("11: >=NUM_LONG, and !=,  tmpNumLong = " + tmpNumLong + ", numLongID:" + numLongID);
                                                }
                                            } else if (tmpNumLen >= this.NUM_SHORT) {
                                                tmpNumShort = tmpNum.substring(tmpNumLen - this.NUM_SHORT);
                                                if (-1 == numShortID && compNumShort.compareTo(tmpNumShort) == 0) {
                                                    numShortID = cursor.getPosition();
                                                }
                                                logd("11: >=NUM_SHORT, tmpNumShort = " + tmpNumShort + ", numShortID:" + numShortID);
                                            } else {
                                                logd("tmpNum11, continue");
                                            }
                                        } else {
                                            logd("11: countrycode prefix not same, continue");
                                        }
                                        if (!cursor.moveToNext()) {
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            }
                            logd("11:  numLongID = " + numLongID + ",numShortID = " + numShortID);
                            fixedIndex = -1 != numLongID ? numLongID : -1 != numShortID ? numShortID : -1;
                        }
                    }
                } else if (compNumLen >= this.NUM_SHORT) {
                    compNumShort = compNum.substring(compNumLen - this.NUM_SHORT);
                    logd("7:  compNumShort = " + compNumShort);
                    if (cursor.moveToFirst()) {
                        columnIndex = cursor.getColumnIndex(columnName);
                        formatColumnIndex = cursor.getColumnIndex("normalized_number");
                        data4ColumnIndex = cursor.getColumnIndex("data4");
                        logd("7: columnIndex: " + columnIndex + ", formatColumnIndex: " + formatColumnIndex + ", data4ColumnIndex: " + data4ColumnIndex);
                        if (columnIndex != -1) {
                            while (true) {
                                tmpNum = cursor.getString(columnIndex);
                                origTmpNum = tmpNum;
                                if (tmpNum != null && tmpNum.indexOf(64) < 0) {
                                    origTmpNum = PhoneNumberUtils.stripSeparators(tmpNum);
                                    logd("origTmpNum: " + origTmpNum);
                                    if (-1 != formatColumnIndex) {
                                        tmpNumFormat = cursor.getString(formatColumnIndex);
                                        tmpNum = isValidData4Number(origTmpNum, tmpNumFormat) ? tmpNumFormat : origTmpNum;
                                    } else if (-1 != data4ColumnIndex) {
                                        tmpNumFormat = cursor.getString(data4ColumnIndex);
                                        tmpNum = isValidData4Number(origTmpNum, tmpNumFormat) ? tmpNumFormat : origTmpNum;
                                    } else {
                                        tmpNum = origTmpNum;
                                    }
                                    logd("7: tmpNumFormat: " + tmpNumFormat);
                                    tmpNumLen = tmpNum.length();
                                    logd("7: tmpNum = " + tmpNum + ", tmpNum.length7: " + tmpNum.length() + ",ID = " + cursor.getPosition());
                                    if (!tmpNum.equals(tmpCompNum)) {
                                        tmpNumPrefix = null;
                                        tmpNumAreaCode = null;
                                        countryCodeLen = getIntlPrefixAndCCLen(tmpNum);
                                        if (countryCodeLen > 0) {
                                            tmpNumPrefix = tmpNum.substring(0, countryCodeLen);
                                            tmpNum = tmpNum.substring(countryCodeLen);
                                            logd("7: tmpNum after remove prefix: " + tmpNum + ", tmpNum.length7: " + tmpNum.length() + ", tmpNumPrefix: " + tmpNumPrefix);
                                        }
                                        if (isEqualCountryCodePrefix(str, countryIso, tmpNumPrefix, null)) {
                                            if (isCnNumber) {
                                                tmpNum = deleteIPHead(tmpNum);
                                                isTmpNumCnMPN = isChineseMobilePhoneNumber(tmpNum);
                                                if ((z && !isTmpNumCnMPN) || (!z && isTmpNumCnMPN)) {
                                                    logd("7: compNum and tmpNum not both MPN, continue");
                                                } else if (z && isTmpNumCnMPN) {
                                                    logd("7: compNum and tmpNum are both MPN, continue to match by mccmnc");
                                                } else {
                                                    areaCodeLen = getChineseFixNumberAreaCodeLength(tmpNum);
                                                    if (areaCodeLen > 0) {
                                                        tmpNumAreaCode = tmpNum.substring(0, areaCodeLen);
                                                        tmpNum = tmpNum.substring(areaCodeLen);
                                                        logd("7: CN tmpNum after remove area code: " + tmpNum + ", tmpNum.length7: " + tmpNum.length() + ", tmpNumAreaCode: " + tmpNumAreaCode);
                                                    }
                                                    if (!isEqualChineseFixNumberAreaCode(str2, tmpNumAreaCode)) {
                                                        logd("7: areacode prefix not same, continue");
                                                    }
                                                }
                                            } else if (tmpNum.length() >= MIN_MATCH && CHINA_AREACODE.equals(tmpNum.substring(0, 1)) && !CHINA_AREACODE.equals(tmpNum.substring(1, 2))) {
                                                tmpNum = tmpNum.substring(1);
                                                logd("7: tmpNum remove 0 at beginning");
                                            }
                                            tmpNumLen = tmpNum.length();
                                            if (tmpNumLen >= this.NUM_LONG) {
                                                tmpNumShort = tmpNum.substring(tmpNumLen - this.NUM_SHORT);
                                                if (-1 == numLongID && compNumShort.compareTo(tmpNumShort) == 0) {
                                                    numLongID = cursor.getPosition();
                                                }
                                                logd("7: >=NUM_LONG, tmpNumShort = " + tmpNumShort + ", numLongID:" + numLongID);
                                            } else if (tmpNumLen >= this.NUM_SHORT) {
                                                tmpNumShort = tmpNum.substring(tmpNumLen - this.NUM_SHORT);
                                                if (-1 == numShortID && compNumShort.compareTo(tmpNumShort) == 0) {
                                                    numShortID = cursor.getPosition();
                                                    logd("7: >= NUM_SHORT numShortID = " + numShortID);
                                                } else {
                                                    logd("7: >=NUM_SHORT, and !=, tmpNumShort = " + tmpNumShort + ", numShortID:" + numShortID);
                                                }
                                            } else {
                                                logd("7: continue");
                                            }
                                        } else {
                                            logd("7: countrycode prefix not same, continue");
                                        }
                                        if (!cursor.moveToNext()) {
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            }
                            logd("7: numShortID = " + numShortID + ",numLongID = " + numLongID);
                            fixedIndex = -1 != numShortID ? numShortID : -1 != numLongID ? numLongID : -1;
                        }
                    }
                } else if (cursor.moveToFirst()) {
                    columnIndex = cursor.getColumnIndex(columnName);
                    formatColumnIndex = cursor.getColumnIndex("normalized_number");
                    data4ColumnIndex = cursor.getColumnIndex("data4");
                    logd("5: columnIndex: " + columnIndex + ", formatColumnIndex: " + formatColumnIndex + ", data4ColumnIndex: " + data4ColumnIndex);
                    if (columnIndex != -1) {
                        while (true) {
                            tmpNum = cursor.getString(columnIndex);
                            origTmpNum = tmpNum;
                            if (tmpNum != null && tmpNum.indexOf(64) < 0) {
                                origTmpNum = PhoneNumberUtils.stripSeparators(tmpNum);
                                logd("origTmpNum: " + origTmpNum);
                                if (-1 != formatColumnIndex) {
                                    tmpNumFormat = cursor.getString(formatColumnIndex);
                                    tmpNum = isValidData4Number(origTmpNum, tmpNumFormat) ? tmpNumFormat : origTmpNum;
                                } else if (-1 != data4ColumnIndex) {
                                    tmpNumFormat = cursor.getString(data4ColumnIndex);
                                    tmpNum = isValidData4Number(origTmpNum, tmpNumFormat) ? tmpNumFormat : origTmpNum;
                                } else {
                                    tmpNum = origTmpNum;
                                }
                                logd("5: tmpNumFormat: " + tmpNumFormat);
                                tmpNumLen = tmpNum.length();
                                logd("5: tmpNum = " + tmpNum + ", tmpNum.length: " + tmpNum.length() + ",ID = " + cursor.getPosition());
                                if (!tmpNum.equals(tmpCompNum)) {
                                    tmpNumPrefix = null;
                                    tmpNumAreaCode = null;
                                    countryCodeLen = getIntlPrefixAndCCLen(tmpNum);
                                    if (countryCodeLen > 0) {
                                        tmpNumPrefix = tmpNum.substring(0, countryCodeLen);
                                        tmpNum = tmpNum.substring(countryCodeLen);
                                        logd("5: tmpNum after remove prefix: " + tmpNum + ", tmpNum.length5: " + tmpNum.length() + ", tmpNumPrefix: " + tmpNumPrefix);
                                    }
                                    if (isEqualCountryCodePrefix(str, countryIso, tmpNumPrefix, null)) {
                                        if (isCnNumber) {
                                            tmpNum = deleteIPHead(tmpNum);
                                            isTmpNumCnMPN = isChineseMobilePhoneNumber(tmpNum);
                                            if ((z && !isTmpNumCnMPN) || (!z && isTmpNumCnMPN)) {
                                                logd("5: compNum and tmpNum not both MPN, continue");
                                            } else if (z && isTmpNumCnMPN) {
                                                logd("5: compNum and tmpNum are both MPN, continue to match by mccmnc");
                                            } else {
                                                areaCodeLen = getChineseFixNumberAreaCodeLength(tmpNum);
                                                if (areaCodeLen > 0) {
                                                    tmpNumAreaCode = tmpNum.substring(0, areaCodeLen);
                                                    tmpNum = tmpNum.substring(areaCodeLen);
                                                    logd("5: CN tmpNum after remove area code: " + tmpNum + ", tmpNum.length5: " + tmpNum.length() + ", tmpNumAreaCode: " + tmpNumAreaCode);
                                                }
                                                if (!isEqualChineseFixNumberAreaCode(str2, tmpNumAreaCode)) {
                                                    logd("5: areacode prefix not same, continue");
                                                }
                                            }
                                        } else if (tmpNum.length() >= MIN_MATCH && CHINA_AREACODE.equals(tmpNum.substring(0, 1)) && !CHINA_AREACODE.equals(tmpNum.substring(1, 2))) {
                                            tmpNum = tmpNum.substring(1);
                                            logd("5: tmpNum remove 0 at beginning");
                                        }
                                        if (tmpNum.length() != compNumLen) {
                                            logd("5: continue");
                                        } else if (-1 == fixedIndex && compNum.compareTo(tmpNum) == 0) {
                                            fixedIndex = cursor.getPosition();
                                            logd("5: break! numLongID = " + fixedIndex);
                                        }
                                    } else {
                                        logd("5: countrycode prefix not same, continue");
                                    }
                                    if (!cursor.moveToNext()) {
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            }
                        }
                        fixedIndex = cursor.getPosition();
                        logd("5: break! numLongID = " + fixedIndex + ", formattedNum full match!");
                        logd("5: fixedIndex = " + fixedIndex);
                    }
                }
            }
            logd("fixedIndex: " + fixedIndex);
            if (-1 == fixedIndex && shouldDoNumberMatchAgainBySimMccmnc(origCompNum, countryIso)) {
                fixedIndex = getCallerIndexInternal(cursor, origCompNum, columnName, this.mSimNumLong, this.mSimNumShort);
            }
            return fixedIndex;
        }
    }

    public static boolean isfixedIndexValid(String cookie, Cursor cursor) {
        int fixedIndex = new CallerInfoHW().getCallerIndex(cursor, cookie, "number");
        return fixedIndex != -1 ? cursor.moveToPosition(fixedIndex) : false;
    }

    private static void logd(String msg) {
        if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, msg);
        }
    }

    private int getIntlPrefixLength(String number) {
        if (TextUtils.isEmpty(number) || isNormalPrefix(number)) {
            return 0;
        }
        int len = INTERNATIONAL_PREFIX.length;
        for (int i = 0; i < len; i++) {
            if (number.startsWith(INTERNATIONAL_PREFIX[i])) {
                return INTERNATIONAL_PREFIX[i].length();
            }
        }
        return 0;
    }

    public int getIntlPrefixAndCCLen(String number) {
        if (TextUtils.isEmpty(number)) {
            return 0;
        }
        int len = getIntlPrefixLength(number);
        if (len > 0) {
            String tmpNumber = number.substring(len);
            for (Integer intValue : this.countryCallingCodeToRegionCodeMap.keySet()) {
                int countrycode = intValue.intValue();
                if (tmpNumber.startsWith(Integer.toString(countrycode))) {
                    logd("extractCountryCodeFromNumber(), find matched country code: " + countrycode);
                    return len + Integer.toString(countrycode).length();
                }
            }
            logd("extractCountryCodeFromNumber(), no matched country code");
            len = 0;
        } else {
            logd("extractCountryCodeFromNumber(), no valid prefix in number: " + number);
        }
        return len;
    }

    private boolean isChineseMobilePhoneNumber(String number) {
        if (TextUtils.isEmpty(number) || number.length() < CN_NUM_MATCH || !number.substring(number.length() - 11).matches(CN_MPN_PATTERN)) {
            return false;
        }
        logd("isChineseMobilePhoneNumber(), return true for number: " + number);
        return true;
    }

    private int getChineseFixNumberAreaCodeLength(String number) {
        int len = 0;
        String tmpNumber = number;
        if (TextUtils.isEmpty(number) || number.length() < CN_FIXED_NUMBER_WITH_AREA_CODE_MIN_LEN) {
            return 0;
        }
        if (!number.startsWith(CHINA_AREACODE)) {
            tmpNumber = CHINA_AREACODE + number;
        }
        String top2String = tmpNumber.substring(0, 2);
        String areaCodeString;
        ArrayList<String> areaCodeArray;
        int i;
        if (top2String.equals(FIXED_NUMBER_TOP2_TOKEN1) || top2String.equals(FIXED_NUMBER_TOP2_TOKEN2)) {
            areaCodeString = tmpNumber.substring(0, 3);
            areaCodeArray = (ArrayList) this.chineseFixNumberAreaCodeMap.get(Integer.valueOf(1));
            i = 0;
            while (i < areaCodeArray.size()) {
                if (areaCodeString.equals(areaCodeArray.get(i))) {
                    len = tmpNumber.equals(number) ? 3 : 2;
                    logd("getChineseFixNumberAreaCodeLength(), matched area code len: " + len + ", number: " + number);
                } else {
                    i++;
                }
            }
        } else {
            areaCodeString = tmpNumber.substring(0, 4);
            areaCodeArray = (ArrayList) this.chineseFixNumberAreaCodeMap.get(Integer.valueOf(2));
            i = 0;
            while (i < areaCodeArray.size()) {
                if (areaCodeString.equals(areaCodeArray.get(i))) {
                    len = tmpNumber.equals(number) ? 4 : 3;
                    logd("getChineseFixNumberAreaCodeLength(), matched area code len: " + len + ", number: " + number);
                } else {
                    i++;
                }
            }
        }
        return len;
    }

    private boolean isEqualChineseFixNumberAreaCode(String compNumAreaCode, String dbNumAreaCode) {
        if (TextUtils.isEmpty(compNumAreaCode) && TextUtils.isEmpty(dbNumAreaCode)) {
            return true;
        }
        if (TextUtils.isEmpty(compNumAreaCode) && !TextUtils.isEmpty(dbNumAreaCode)) {
            return !this.IS_MIIT_NUM_MATCH;
        } else {
            if (!TextUtils.isEmpty(compNumAreaCode) && TextUtils.isEmpty(dbNumAreaCode)) {
                return !this.IS_MIIT_NUM_MATCH;
            } else {
                if (!compNumAreaCode.startsWith(CHINA_AREACODE)) {
                    compNumAreaCode = CHINA_AREACODE + compNumAreaCode;
                }
                if (!dbNumAreaCode.startsWith(CHINA_AREACODE)) {
                    dbNumAreaCode = CHINA_AREACODE + dbNumAreaCode;
                }
                return compNumAreaCode.equals(dbNumAreaCode);
            }
        }
    }

    private String deleteIPHead(String number) {
        if (TextUtils.isEmpty(number)) {
            return number;
        }
        int numberLen = number.length();
        if (numberLen < IPHEAD_LENTH) {
            logd("deleteIPHead() numberLen is short than 5!");
            return number;
        }
        if (Arrays.binarySearch(IPHEAD, number.substring(0, IPHEAD_LENTH)) >= 0) {
            number = number.substring(IPHEAD_LENTH, numberLen);
        }
        logd("deleteIPHead() new Number: " + number);
        return number;
    }

    private boolean isChineseNumberByPrefix(String numberPrefix, String netIso) {
        if (TextUtils.isEmpty(numberPrefix)) {
            logd("isChineseNumberByPrefix(), networkCountryIso: " + netIso);
            if (netIso == null || !"CN".equals(netIso.toUpperCase())) {
                return false;
            }
            return true;
        }
        return Integer.toString(this.countryCodeforCN).equals(numberPrefix.substring(getIntlPrefixLength(numberPrefix)));
    }

    private boolean isEqualCountryCodePrefix(String num1Prefix, String netIso1, String num2Prefix, String netIso2) {
        if (TextUtils.isEmpty(num1Prefix) && TextUtils.isEmpty(num2Prefix)) {
            logd("isEqualCountryCodePrefix(), both have no country code, return true");
            return true;
        }
        boolean ret;
        String netIso;
        int countryCode;
        if (TextUtils.isEmpty(num1Prefix) && !TextUtils.isEmpty(num2Prefix)) {
            logd("isEqualCountryCodePrefix(), netIso1: " + netIso1 + ", netIso2: " + netIso2);
            if (TextUtils.isEmpty(netIso1)) {
                ret = true;
            } else {
                netIso = netIso1.toUpperCase();
                if ("CN".equals(netIso)) {
                    countryCode = this.countryCodeforCN;
                } else {
                    countryCode = sInstance.getCountryCodeForRegion(netIso);
                }
                ret = num2Prefix.substring(getIntlPrefixLength(num2Prefix)).equals(Integer.toString(countryCode));
            }
        } else if (TextUtils.isEmpty(num1Prefix) || !TextUtils.isEmpty(num2Prefix)) {
            ret = num1Prefix.substring(getIntlPrefixLength(num1Prefix)).equals(num2Prefix.substring(getIntlPrefixLength(num2Prefix)));
        } else {
            logd("isEqualCountryCodePrefix(), netIso1: " + netIso1 + ", netIso2: " + netIso2);
            if (TextUtils.isEmpty(netIso2)) {
                ret = true;
            } else {
                netIso = netIso2.toUpperCase();
                if ("CN".equals(netIso)) {
                    countryCode = this.countryCodeforCN;
                } else {
                    countryCode = sInstance.getCountryCodeForRegion(netIso);
                }
                ret = num1Prefix.substring(getIntlPrefixLength(num1Prefix)).equals(Integer.toString(countryCode));
            }
        }
        return ret;
    }

    private int getFullMatchIndex(Cursor cursor, String compNum, String columnName) {
        compNum = PhoneNumberUtils.stripSeparators(compNum);
        if (this.IS_CHINA_TELECOM && compNum.startsWith("**133") && compNum.endsWith("#")) {
            compNum = compNum.substring(0, compNum.length() - 1);
            logd("full match check, compNum startsWith **133 && endsWith #");
        }
        logd("full match check, compNum: " + compNum);
        if (cursor == null || !cursor.moveToFirst()) {
            return -1;
        }
        int columnIndex = cursor.getColumnIndex(columnName);
        if (-1 == columnIndex) {
            return -1;
        }
        while (true) {
            String tmpNum = cursor.getString(columnIndex);
            if (tmpNum != null && tmpNum.indexOf(64) < 0) {
                tmpNum = PhoneNumberUtils.stripSeparators(tmpNum);
            }
            logd("full match check, tmpNum: " + tmpNum);
            if (compNum.equals(tmpNum)) {
                int fixedIndex = cursor.getPosition();
                logd("exact match: break! fixedIndex = " + fixedIndex);
                return fixedIndex;
            } else if (!cursor.moveToNext()) {
                return -1;
            }
        }
    }

    private boolean shouldDoNumberMatchAgainBySimMccmnc(String number, String countryIso) {
        if (SystemProperties.getBoolean(HwTelephonyProperties.PROPERTY_NETWORK_ISROAMING, false) && getFormatNumberByCountryISO(number, countryIso) == null) {
            return true;
        }
        return false;
    }

    private String getFormatNumberByCountryISO(String number, String countryIso) {
        if (TextUtils.isEmpty(number) || TextUtils.isEmpty(countryIso)) {
            return null;
        }
        return PhoneNumberUtils.formatNumberToE164(number, countryIso.toUpperCase(Locale.US));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getCallerIndexInternal(Cursor cursor, String compNum, String columnName, int numLong, int numShort) {
        int numShortID = -1;
        int numLongID = -1;
        int fixedIndex = -1;
        logd("getCallerIndexInternal, compNum: " + compNum + ", numLong: " + numLong + ", numShort: " + numShort);
        if (TextUtils.isEmpty(compNum)) {
            if (cursor != null && cursor.getCount() > 0) {
                fixedIndex = 0;
            }
            Log.e(TAG, "getCallerIndexInternal(),null == compNum! fixedIndex = " + fixedIndex);
            return fixedIndex;
        }
        int compNumLen = compNum.length();
        this.NUM_LONG = numLong;
        this.NUM_SHORT = numShort;
        if (cursor != null) {
            String compNumShort;
            int columnIndex;
            String tmpNum;
            int tmpNumLen;
            String tmpNumShort;
            if (compNumLen >= this.NUM_LONG) {
                String compNumLong = compNum.substring(compNumLen - this.NUM_LONG);
                compNumShort = compNum.substring(compNumLen - this.NUM_SHORT);
                logd("11: compNumLong = " + compNumLong + ", compNumShort = " + compNumShort);
                if (cursor.moveToFirst()) {
                    columnIndex = cursor.getColumnIndex(columnName);
                    if (columnIndex != -1) {
                        while (true) {
                            tmpNum = cursor.getString(columnIndex);
                            if (tmpNum != null && tmpNum.indexOf(64) < 0) {
                                tmpNum = PhoneNumberUtils.stripSeparators(tmpNum);
                                tmpNumLen = tmpNum.length();
                                logd("11: tmpNum = " + tmpNum + ", tmpNum.length11: " + tmpNum.length() + ", ID = " + cursor.getPosition());
                                if (!compNum.equals(tmpNum)) {
                                    if (tmpNumLen >= this.NUM_LONG) {
                                        String tmpNumLong = tmpNum.substring(tmpNumLen - this.NUM_LONG);
                                        if (-1 == numLongID && compNumLong.compareTo(tmpNumLong) == 0) {
                                            numLongID = cursor.getPosition();
                                            logd("11: > NUM_LONG numLongID = " + numLongID);
                                        }
                                        logd("11: >= NUM_LONG, and !=,  tmpNumLong = " + tmpNumLong + ", numLongID: " + numLongID);
                                    } else if (tmpNumLen >= this.NUM_SHORT) {
                                        tmpNumShort = tmpNum.substring(tmpNumLen - this.NUM_SHORT);
                                        if (-1 == numShortID && compNumShort.compareTo(tmpNumShort) == 0) {
                                            numShortID = cursor.getPosition();
                                        }
                                        logd("11: >= NUM_SHORT, tmpNumShort = " + tmpNumShort + ", numShortID:" + numShortID);
                                    } else {
                                        logd("tmpNum11, continue");
                                    }
                                    if (!cursor.moveToNext()) {
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            }
                        }
                        numLongID = cursor.getPosition();
                        logd("exact match: break! numLongID = " + numLongID);
                        logd("11: numLongID = " + numLongID + ", numShortID = " + numShortID);
                        fixedIndex = -1 != numLongID ? numLongID : -1 != numShortID ? numShortID : -1;
                    }
                }
            } else if (compNumLen >= this.NUM_SHORT) {
                compNumShort = compNum.substring(compNumLen - this.NUM_SHORT);
                logd("7: compNumShort = " + compNumShort);
                if (cursor.moveToFirst()) {
                    columnIndex = cursor.getColumnIndex(columnName);
                    if (columnIndex != -1) {
                        while (true) {
                            tmpNum = cursor.getString(columnIndex);
                            if (tmpNum != null && tmpNum.indexOf(64) < 0) {
                                tmpNum = PhoneNumberUtils.stripSeparators(tmpNum);
                                tmpNumLen = tmpNum.length();
                                logd("7: tmpNum = " + tmpNum + ", tmpNum.length7: " + tmpNum.length() + ", ID = " + cursor.getPosition());
                                if (!compNum.equals(tmpNum)) {
                                    if (tmpNumLen >= this.NUM_LONG) {
                                        tmpNumShort = tmpNum.substring(tmpNumLen - this.NUM_SHORT);
                                        if (-1 == numLongID && compNumShort.compareTo(tmpNumShort) == 0) {
                                            numLongID = cursor.getPosition();
                                        }
                                        logd("7: >= NUM_LONG, tmpNumShort = " + tmpNumShort + ", numLongID:" + numLongID);
                                    } else if (tmpNumLen >= this.NUM_SHORT) {
                                        tmpNumShort = tmpNum.substring(tmpNumLen - this.NUM_SHORT);
                                        if (-1 == numShortID && compNumShort.compareTo(tmpNumShort) == 0) {
                                            numShortID = cursor.getPosition();
                                            logd("7: >= NUM_SHORT numShortID = " + numShortID);
                                        }
                                        logd("7: >= NUM_SHORT, and !=, tmpNumShort = " + tmpNumShort + ", numShortID:" + numShortID);
                                    } else {
                                        logd("7: continue");
                                    }
                                    if (!cursor.moveToNext()) {
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            }
                        }
                        logd("7: numShortID = " + numShortID + ", numLongID = " + numLongID);
                        fixedIndex = -1 != numShortID ? numShortID : -1 != numLongID ? numLongID : -1;
                    }
                }
            } else if (cursor.moveToFirst()) {
                columnIndex = cursor.getColumnIndex(columnName);
                if (columnIndex != -1) {
                    while (true) {
                        tmpNum = cursor.getString(columnIndex);
                        if (tmpNum != null && tmpNum.indexOf(64) < 0) {
                            tmpNum = PhoneNumberUtils.stripSeparators(tmpNum);
                            tmpNumLen = tmpNum.length();
                            logd("5: tmpNum = " + tmpNum + ", tmpNum.length: " + tmpNum.length() + ", ID = " + cursor.getPosition());
                            if (tmpNumLen == compNumLen) {
                                if (compNum.compareTo(tmpNum) == 0) {
                                    break;
                                }
                                if (cursor.moveToNext()) {
                                    break;
                                }
                            } else {
                                logd("5: continue");
                                if (cursor.moveToNext()) {
                                    break;
                                }
                            }
                        }
                    }
                    return -1;
                }
            }
        }
        logd("getCallerIndexInternal, fixedIndex: " + fixedIndex);
        return fixedIndex;
    }

    private boolean compareNumsInternal(String num1, String num2, int numLong, int numShort) {
        logd("compareNumsInternal, num1: " + num1 + ", num2: " + num2 + ", numLong: " + numLong + ", numShort: " + numShort);
        if (TextUtils.isEmpty(num1) || TextUtils.isEmpty(num2)) {
            return false;
        }
        int num1Len = num1.length();
        int num2Len = num2.length();
        this.NUM_LONG = numLong;
        this.NUM_SHORT = numShort;
        String num1Short;
        if (num1Len >= this.NUM_LONG) {
            String num1Long = num1.substring(num1Len - this.NUM_LONG);
            num1Short = num1.substring(num1Len - this.NUM_SHORT);
            logd("compareNumsInternal, 11: num1Long = " + num1Long + ", num1Short = " + num1Short);
            if (num2Len >= this.NUM_LONG) {
                if (num1Long.compareTo(num2.substring(num2Len - this.NUM_LONG)) == 0) {
                    logd("compareNumsInternal, 11: >= NUM_LONG return true");
                    return true;
                }
            } else if (num2Len >= this.NUM_SHORT && num1Short.compareTo(num2.substring(num2Len - this.NUM_SHORT)) == 0) {
                logd("compareNumsInternal, 11: >= NUM_SHORT return true");
                return true;
            }
        } else if (num1Len >= this.NUM_SHORT) {
            num1Short = num1.substring(num1Len - this.NUM_SHORT);
            logd("compareNumsInternal, 7: num1Short = " + num1Short);
            if (num2Len >= this.NUM_SHORT && num1Short.compareTo(num2.substring(num2Len - this.NUM_SHORT)) == 0) {
                logd("compareNumsInternal, 7: >= NUM_SHORT return true");
                return true;
            }
        } else {
            logd("compareNumsInternal, 5: do full compare");
            return num1.equals(num2);
        }
        return false;
    }

    private boolean isRoamingCountryNumberByPrefix(String numberPrefix, String netIso) {
        if (SystemProperties.getBoolean(HwTelephonyProperties.PROPERTY_NETWORK_ISROAMING, false)) {
            if (TextUtils.isEmpty(numberPrefix)) {
                return true;
            }
            numberPrefix = numberPrefix.substring(getIntlPrefixLength(numberPrefix));
            if (!(TextUtils.isEmpty(numberPrefix) || TextUtils.isEmpty(netIso))) {
                return numberPrefix.equals(Integer.toString(sInstance.getCountryCodeForRegion(netIso.toUpperCase(Locale.US))));
            }
        }
        return false;
    }

    private boolean isValidData4Number(String data1Num, String data4Num) {
        logd("isValidData4Number, data1Num: " + data1Num + ", data4Num: " + data4Num);
        if (!(TextUtils.isEmpty(data1Num) || TextUtils.isEmpty(data4Num) || !data4Num.startsWith("+"))) {
            int countryCodeLen = getIntlPrefixAndCCLen(data4Num);
            if (countryCodeLen > 0) {
                data4Num = data4Num.substring(countryCodeLen);
            }
            logd("isValidData4Number, data4Num after remove prefix: " + data4Num);
            if (data4Num.length() <= data1Num.length() && data4Num.equals(data1Num.substring(data1Num.length() - data4Num.length()))) {
                return true;
            }
        }
        return false;
    }

    private boolean isNormalPrefix(String number) {
        String sMcc = Token.SEPARATOR;
        if (this.mNetworkOperator != null && this.mNetworkOperator.length() > 3) {
            sMcc = this.mNetworkOperator.substring(0, 3);
        }
        if (number.startsWith("011")) {
            for (Object equals : NORMAL_PREFIX_MCC) {
                if (sMcc.equals(equals) && number.length() == CN_NUM_MATCH) {
                    logd("those operator 011 are normal prefix");
                    return true;
                }
            }
        }
        return false;
    }
}
