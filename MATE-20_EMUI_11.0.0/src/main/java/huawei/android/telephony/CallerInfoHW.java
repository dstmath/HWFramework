package huawei.android.telephony;

import android.database.Cursor;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import com.android.i18n.phonenumbers.CountryCodeToRegionCodeMapUtils;
import com.android.internal.telephony.HwTelephonyProperties;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.hwparttelephony.BuildConfig;
import com.huawei.i18n.phonenumbers.PhoneNumberUtilExt;
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
    protected static final boolean DBG = false;
    private static final String FIXED_NUMBER_TOP2_TOKEN1 = "01";
    private static final String FIXED_NUMBER_TOP2_TOKEN2 = "02";
    private static final String[] INTERNATIONAL_PREFIX = {"+00", "+", "00"};
    private static final String[] IPHEAD = {"10193", "11808", "12593", "17900", "17901", "17908", "17909", "17910", "17911", "17931", "17950", "17951", "17960", "17968", "17969", "96435"};
    private static final int IPHEAD_LENTH = 5;
    private static final boolean IS_SUPPORT_DUAL_NUMBER = SystemPropertiesEx.getBoolean("ro.config.hw_dual_number", false);
    public static final int MIN_MATCH = 7;
    private static final String[] NORMAL_PREFIX_MCC = {"602", "722"};
    private static final String TAG = "CallerInfo";
    private static CallerInfoHW sCallerInfoHwInstance = null;
    private static PhoneNumberUtilExt sInstance = PhoneNumberUtilExt.getInstance();
    private boolean IS_CHINA_TELECOM;
    private boolean IS_MIIT_NUM_MATCH;
    private final int NUM_LONG_CUST;
    private final int NUM_SHORT_CUST;
    private final Map<Integer, ArrayList<String>> chineseFixNumberAreaCodeMap;
    private int configMatchNum = SystemPropertiesEx.getInt("ro.config.hwft_MatchNum", 7);
    private int configMatchNumShort;
    private final Map<Integer, List<String>> countryCallingCodeToRegionCodeMap;
    private int countryCodeforCN;
    private String mNetworkOperator;
    private int mSimNumLong;
    private int mSimNumShort;

    public CallerInfoHW() {
        int i = 7;
        int i2 = this.configMatchNum;
        this.NUM_LONG_CUST = i2 >= 7 ? i2 : i;
        this.configMatchNumShort = SystemPropertiesEx.getInt("ro.config.hwft_MatchNumShort", this.NUM_LONG_CUST);
        int i3 = this.configMatchNumShort;
        int i4 = this.NUM_LONG_CUST;
        this.NUM_SHORT_CUST = i3 >= i4 ? i4 : i3;
        this.mSimNumLong = this.NUM_LONG_CUST;
        this.mSimNumShort = this.NUM_SHORT_CUST;
        this.IS_CHINA_TELECOM = SystemPropertiesEx.get("ro.config.hw_opta", CHINA_AREACODE).equals("92") && SystemPropertiesEx.get("ro.config.hw_optb", CHINA_AREACODE).equals("156");
        this.IS_MIIT_NUM_MATCH = SystemPropertiesEx.getBoolean("ro.config.miit_number_match", false);
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
        int len;
        logd("getCountryIsoFromDbNumber(), number: " + number);
        if (!TextUtils.isEmpty(number) && (len = getIntlPrefixLength(number)) > 0) {
            String tmpNumber = number.substring(len);
            for (Integer num : this.countryCallingCodeToRegionCodeMap.keySet()) {
                int countrycode = num.intValue();
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

    /* JADX WARNING: Removed duplicated region for block: B:50:0x0159  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0188  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0192  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0199  */
    public boolean compareNums(String num1, String netIso1, String num2, String netIso2) {
        String num12;
        String num22;
        String num1AreaCode;
        boolean ret;
        String num2AreaCode;
        boolean isNum1CnMPN;
        String num1AreaCode2;
        boolean isNum2CnMPN;
        String num2AreaCode2;
        boolean ret2;
        boolean isNum2CnMPN2;
        String num23;
        String num13;
        String num14 = num1;
        String num24 = num2;
        String num1Prefix = null;
        String num2Prefix = null;
        int NUM_LONG = this.NUM_LONG_CUST;
        int NUM_SHORT = this.NUM_SHORT_CUST;
        if (num14 == null) {
            return false;
        }
        if (num24 == null) {
            return false;
        }
        logd("compareNums, num1 = " + num14 + ", netIso1 = " + netIso1 + ", num2 = " + num24 + ", netIso2 = " + netIso2);
        if (SystemPropertiesEx.getInt("ro.config.hwft_MatchNum", 0) == 0) {
            int numMatch = SystemPropertiesEx.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH, 7);
            int numMatchShort = SystemPropertiesEx.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT, numMatch);
            int i = numMatch < 7 ? 7 : numMatch;
            this.mSimNumLong = i;
            NUM_LONG = i;
            int i2 = numMatchShort >= NUM_LONG ? NUM_LONG : numMatchShort;
            this.mSimNumShort = i2;
            NUM_SHORT = i2;
            logd("compareNums, after setprop NUM_LONG = " + NUM_LONG + ", NUM_SHORT = " + NUM_SHORT);
        }
        if (num14.indexOf(64) < 0) {
            num14 = PhoneNumberUtils.stripSeparators(num1);
        }
        if (num24.indexOf(64) < 0) {
            num24 = PhoneNumberUtils.stripSeparators(num2);
        }
        String num15 = formatedForDualNumber(num14);
        String num25 = formatedForDualNumber(num24);
        if (this.IS_CHINA_TELECOM && num15.startsWith("**133") && num15.endsWith("#")) {
            num15 = num15.substring(0, num15.length() - 1);
            logd("compareNums, num1 startsWith **133 && endsWith #");
        }
        if (this.IS_CHINA_TELECOM && num25.startsWith("**133") && num25.endsWith("#")) {
            num25 = num25.substring(0, num25.length() - 1);
            logd("compareNums, num2 startsWith **133 && endsWith #");
        }
        if (num15.equals(num25)) {
            logd("compareNums, full compare returns true.");
            return true;
        }
        if (!TextUtils.isEmpty(netIso1)) {
            String formattedNum1 = PhoneNumberUtils.formatNumberToE164(num15, netIso1.toUpperCase(Locale.US));
            if (formattedNum1 != null) {
                logd("compareNums, formattedNum1: " + formattedNum1 + ", with netIso1: " + netIso1);
                num12 = formattedNum1;
                if (TextUtils.isEmpty(netIso2)) {
                    String formattedNum2 = PhoneNumberUtils.formatNumberToE164(num25, netIso2.toUpperCase(Locale.US));
                    if (formattedNum2 != null) {
                        logd("compareNums, formattedNum2: " + formattedNum2 + ", with netIso2: " + netIso2);
                        num22 = formattedNum2;
                        if (num12.equals(num22)) {
                            logd("compareNums, full compare for formatted number returns true.");
                            return true;
                        }
                        int countryCodeLen1 = getIntlPrefixAndCCLen(num12);
                        if (countryCodeLen1 > 0) {
                            num1Prefix = num12.substring(0, countryCodeLen1);
                            num12 = num12.substring(countryCodeLen1);
                            logd("compareNums, num1 after remove prefix: " + num12 + ", num1Prefix: " + num1Prefix);
                        }
                        int countryCodeLen2 = getIntlPrefixAndCCLen(num22);
                        if (countryCodeLen2 > 0) {
                            num2Prefix = num22.substring(0, countryCodeLen2);
                            num22 = num22.substring(countryCodeLen2);
                            logd("compareNums, num2 after remove prefix: " + num22 + ", num2Prefix: " + num2Prefix);
                        }
                        if (isRoamingCountryNumberByPrefix(num1Prefix, netIso1) || isRoamingCountryNumberByPrefix(num2Prefix, netIso2)) {
                            logd("compareNums, num1 or num2 belong to roaming country");
                            int numMatch2 = SystemPropertiesEx.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_ROAMING, 7);
                            int numMatchShort2 = SystemPropertiesEx.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT_ROAMING, numMatch2);
                            num1AreaCode = null;
                            NUM_LONG = numMatch2 < 7 ? 7 : numMatch2;
                            NUM_SHORT = numMatchShort2 >= NUM_LONG ? NUM_LONG : numMatchShort2;
                            logd("compareNums, roaming prop NUM_LONG = " + NUM_LONG + ", NUM_SHORT = " + NUM_SHORT);
                        } else {
                            num1AreaCode = null;
                        }
                        if (isEqualCountryCodePrefix(num1Prefix, netIso1, num2Prefix, netIso2)) {
                            boolean isNum1CnNumber = isChineseNumberByPrefix(num1Prefix, netIso1);
                            if (isNum1CnNumber) {
                                NUM_LONG = CN_NUM_MATCH;
                                num12 = deleteIPHead(num12);
                                boolean isNum1CnMPN2 = isChineseMobilePhoneNumber(num12);
                                if (!isNum1CnMPN2) {
                                    isNum1CnMPN = isNum1CnMPN2;
                                    int areaCodeLen = getChineseFixNumberAreaCodeLength(num12);
                                    if (areaCodeLen > 0) {
                                        num2AreaCode = null;
                                        num1AreaCode2 = num12.substring(0, areaCodeLen);
                                        num12 = num12.substring(areaCodeLen);
                                        logd("compareNums, CN num1 after remove area code: " + num12 + ", num1AreaCode: " + num1AreaCode2);
                                    } else {
                                        num2AreaCode = null;
                                        num1AreaCode2 = num1AreaCode;
                                    }
                                } else {
                                    isNum1CnMPN = isNum1CnMPN2;
                                    num2AreaCode = null;
                                    num1AreaCode2 = num1AreaCode;
                                }
                            } else {
                                num2AreaCode = null;
                                if ("PE".equalsIgnoreCase(netIso1)) {
                                    logd("compareNums, PE num1 start with 0 not remove it");
                                    isNum1CnMPN = false;
                                } else if (num12.length() >= 7) {
                                    isNum1CnMPN = false;
                                    if (CHINA_AREACODE.equals(num12.substring(0, 1)) && !CHINA_AREACODE.equals(num12.substring(1, 2))) {
                                        num12 = num12.substring(1);
                                        logd("compareNums, num1 remove 0 at beginning");
                                        num1AreaCode2 = num1AreaCode;
                                    }
                                } else {
                                    isNum1CnMPN = false;
                                }
                                num1AreaCode2 = num1AreaCode;
                            }
                            boolean isNum2CnNumber = isChineseNumberByPrefix(num2Prefix, netIso2);
                            if (isNum2CnNumber) {
                                NUM_LONG = CN_NUM_MATCH;
                                num22 = deleteIPHead(num22);
                                boolean isNum2CnMPN3 = isChineseMobilePhoneNumber(num22);
                                if (!isNum2CnMPN3) {
                                    int areaCodeLen2 = getChineseFixNumberAreaCodeLength(num22);
                                    if (areaCodeLen2 > 0) {
                                        isNum2CnMPN2 = isNum2CnMPN3;
                                        String num2AreaCode3 = num22.substring(0, areaCodeLen2);
                                        num22 = num22.substring(areaCodeLen2);
                                        logd("compareNums, CN num2 after remove area code: " + num22 + ", num2AreaCode: " + num2AreaCode3);
                                        num2AreaCode = num2AreaCode3;
                                    } else {
                                        isNum2CnMPN2 = isNum2CnMPN3;
                                    }
                                    isNum2CnMPN = isNum2CnMPN2;
                                    num2AreaCode2 = num2AreaCode;
                                } else {
                                    isNum2CnMPN = isNum2CnMPN3;
                                    num2AreaCode2 = num2AreaCode;
                                }
                            } else {
                                if ("PE".equalsIgnoreCase(netIso2)) {
                                    logd("compareNums, PE num2 start with 0 not remove it");
                                    isNum2CnMPN = false;
                                } else {
                                    isNum2CnMPN = false;
                                    if (num22.length() >= 7) {
                                        if (CHINA_AREACODE.equals(num22.substring(0, 1)) && !CHINA_AREACODE.equals(num22.substring(1, 2))) {
                                            num22 = num22.substring(1);
                                            logd("compareNums, num2 remove 0 at beginning");
                                            num2AreaCode2 = num2AreaCode;
                                        }
                                    }
                                }
                                num2AreaCode2 = num2AreaCode;
                            }
                            if ((!isNum1CnMPN || isNum2CnMPN) && (isNum1CnMPN || !isNum2CnMPN)) {
                                if (isNum1CnMPN && isNum2CnMPN) {
                                    logd("compareNums, num1 and num2 are both MPN, continue to compare");
                                } else if (isNum1CnNumber && isNum2CnNumber && !isEqualChineseFixNumberAreaCode(num1AreaCode2, num2AreaCode2)) {
                                    logd("compareNums, areacode prefix not same, return false");
                                    return false;
                                }
                                return compareNumsInternal(num12, num22, NUM_LONG, NUM_SHORT);
                            }
                            if (shouldDoNumberMatchAgainBySimMccmnc(num15, netIso1) || shouldDoNumberMatchAgainBySimMccmnc(num25, netIso2)) {
                                ret2 = compareNumsInternal(num15, num25, this.mSimNumLong, this.mSimNumShort);
                            } else {
                                ret2 = false;
                            }
                            logd("compareNums, num1 and num2 not both MPN, return " + ret2);
                            return ret2;
                        }
                        if (shouldDoNumberMatchAgainBySimMccmnc(num15, netIso1) || shouldDoNumberMatchAgainBySimMccmnc(num25, netIso2)) {
                            ret = compareNumsInternal(num15, num25, this.mSimNumLong, this.mSimNumShort);
                        } else {
                            ret = false;
                        }
                        logd("compareNums, countrycode prefix not same, return " + ret);
                        return ret;
                    }
                    num23 = num25;
                } else {
                    num23 = num25;
                }
                num22 = num23;
                if (num12.equals(num22)) {
                }
            } else {
                num13 = num15;
            }
        } else {
            num13 = num15;
        }
        num12 = num13;
        if (TextUtils.isEmpty(netIso2)) {
        }
        num22 = num23;
        if (num12.equals(num22)) {
        }
    }

    public boolean compareNums(String num1, String num2) {
        int NUM_LONG = this.NUM_LONG_CUST;
        int NUM_SHORT = this.NUM_SHORT_CUST;
        if (num1 == null || num2 == null) {
            return false;
        }
        logd("compareNums, num1 = " + num1 + ", num2 = " + num2);
        if (SystemPropertiesEx.getInt("ro.config.hwft_MatchNum", 0) == 0) {
            int i = 7;
            int numMatch = SystemPropertiesEx.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH, 7);
            int numMatchShort = SystemPropertiesEx.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT, numMatch);
            if (numMatch >= 7) {
                i = numMatch;
            }
            NUM_LONG = i;
            NUM_SHORT = numMatchShort >= NUM_LONG ? NUM_LONG : numMatchShort;
            logd("compareNums, after setprop NUM_LONG = " + NUM_LONG + ", NUM_SHORT = " + NUM_SHORT);
        }
        if (num1.indexOf(64) < 0) {
            num1 = PhoneNumberUtils.stripSeparators(num1);
        }
        if (num2.indexOf(64) < 0) {
            num2 = PhoneNumberUtils.stripSeparators(num2);
        }
        String num12 = formatedForDualNumber(num1);
        String num22 = formatedForDualNumber(num2);
        if (this.IS_CHINA_TELECOM && num12.startsWith("**133") && num12.endsWith("#")) {
            num12 = num12.substring(0, num12.length() - 1);
            logd("compareNums, num1 startsWith **133 && endsWith #");
        }
        if (this.IS_CHINA_TELECOM && num22.startsWith("**133") && num22.endsWith("#")) {
            num22 = num22.substring(0, num22.length() - 1);
            logd("compareNums, num2 startsWith **133 && endsWith #");
        }
        if (NUM_SHORT < NUM_LONG) {
            logd("compareNums, NUM_SHORT have been set! Only do full compare.");
            return num12.equals(num22);
        }
        int num1Len = num12.length();
        int num2Len = num22.length();
        if (num1Len > NUM_LONG) {
            num12 = num12.substring(num1Len - NUM_LONG);
        }
        if (num2Len > NUM_LONG) {
            num22 = num22.substring(num2Len - NUM_LONG);
        }
        logd("compareNums, new num1 = " + num12 + ", new num2 = " + num22);
        return num12.equals(num22);
    }

    @Override // huawei.android.telephony.TelephonyInterfacesHW
    public int getCallerIndex(Cursor cursor, String compNum) {
        return getCallerIndex(cursor, compNum, "number");
    }

    public int getCallerIndex(Cursor cursor, String compNum, String columnName) {
        return getCallerIndex(cursor, compNum, columnName, SystemPropertiesEx.get(HwTelephonyProperties.PROPERTY_NETWORK_COUNTRY_ISO, BuildConfig.FLAVOR));
    }

    /* JADX INFO: Multiple debug info for r1v1 int: [D('NUM_LONG' int), D('compNumLong' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r2v1 int: [D('tmpNum' java.lang.String), D('NUM_SHORT' int)] */
    /* JADX INFO: Multiple debug info for r9v36 'NUM_LONG'  int: [D('NUM_SHORT' int), D('NUM_LONG' int)] */
    /* JADX WARNING: Code restructure failed: missing block: B:356:0x0d66, code lost:
        return -1;
     */
    public int getCallerIndex(Cursor cursor, String compNum, String columnName, String countryIso) {
        int fixedIndex;
        String compNum2;
        int countryCodeLen;
        int NUM_SHORT;
        String origTmpNum;
        boolean isCompNumCnMPN;
        int NUM_LONG;
        String origTmpNum2;
        int NUM_SHORT2;
        String compNumAreaCode;
        String tmpCompNum;
        int NUM_SHORT3;
        int NUM_SHORT4;
        String str;
        String tmpNumShort;
        int countryCodeLen2;
        String tmpNumFormat;
        String tmpNum;
        int fixedIndex2;
        String tmpNum2;
        String tmpNumPrefix;
        String tmpNumAreaCode;
        int data4ColumnIndex;
        String tmpNumAreaCode2;
        String tmpNumPrefix2;
        String compNum3;
        String tmpNumPrefix3;
        String tmpNum3;
        String tmpNumAreaCode3;
        String compNumShort;
        String tmpNumAreaCode4;
        String tmpNumPrefix4;
        String origTmpNum3;
        int NUM_LONG2;
        String tmpNumShort2;
        String tmpNum4;
        String compNumShort2;
        int numShortID;
        String origTmpNum4;
        String tmpNumFormat2;
        String tmpNum5;
        String compNumShort3;
        String tmpNumAreaCode5;
        String tmpNumPrefix5;
        int NUM_LONG3;
        String tmpNumPrefix6;
        String tmpNum6;
        String compNumAreaCode2;
        String tmpNumAreaCode6;
        String tmpNumFormat3;
        String tmpNumAreaCode7;
        String tmpNumPrefix7;
        String origTmpNum5;
        String tmpNumShort3;
        int countryCodeLen3;
        int numLongID;
        String origTmpNum6;
        String tmpNumFormat4;
        String tmpNum7;
        String tmpNumAreaCode8;
        String tmpNumPrefix8;
        String tmpNumPrefix9;
        String tmpNumAreaCode9;
        String tmpNumAreaCode10;
        String compNumAreaCode3;
        String compNumPrefix = null;
        String compNumAreaCode4 = null;
        int fixedIndex3 = -1;
        int NUM_LONG4 = this.NUM_LONG_CUST;
        int NUM_SHORT5 = this.NUM_SHORT_CUST;
        if (TextUtils.isEmpty(compNum)) {
            if (cursor != null && cursor.getCount() > 0) {
                fixedIndex3 = 0;
            }
            Log.e(TAG, "CallerInfoHW(),null == compNum! fixedIndex = " + fixedIndex3);
            return fixedIndex3;
        }
        String tmpNumShort4 = null;
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                int fixedIndex4 = getFullMatchIndex(cursor, compNum, columnName);
                if (IS_SUPPORT_DUAL_NUMBER && -1 == fixedIndex4) {
                    fixedIndex4 = getFullMatchIndex(cursor, formatedForDualNumber(PhoneNumberUtils.stripSeparators(compNum)), columnName);
                }
                if (-1 != fixedIndex4) {
                    return fixedIndex4;
                }
                logd("getCallerIndex(), not full match proceed to check..");
                logd("getCallerIndex(), NUM_LONG = " + NUM_LONG4 + ",NUM_SHORT = " + NUM_SHORT5);
                if (SystemPropertiesEx.getInt("ro.config.hwft_MatchNum", 0) == 0) {
                    int numMatch = SystemPropertiesEx.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH, 7);
                    int numMatchShort = SystemPropertiesEx.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT, numMatch);
                    fixedIndex = fixedIndex4;
                    int i = numMatch < 7 ? 7 : numMatch;
                    this.mSimNumLong = i;
                    NUM_LONG4 = i;
                    int i2 = numMatchShort >= NUM_LONG4 ? NUM_LONG4 : numMatchShort;
                    this.mSimNumShort = i2;
                    NUM_SHORT5 = i2;
                    logd("getCallerIndex(), after setprop NUM_LONG = " + NUM_LONG4 + ", NUM_SHORT = " + NUM_SHORT5);
                } else {
                    fixedIndex = fixedIndex4;
                }
                String compNum4 = formatedForDualNumber(PhoneNumberUtils.stripSeparators(compNum));
                compNum4.length();
                StringBuilder sb = new StringBuilder();
                int NUM_LONG5 = NUM_LONG4;
                sb.append("compNum: ");
                sb.append(compNum4);
                sb.append(", countryIso: ");
                sb.append(countryIso);
                logd(sb.toString());
                if (this.IS_CHINA_TELECOM && compNum4.startsWith("**133") && compNum4.endsWith("#")) {
                    compNum4 = compNum4.substring(0, compNum4.length() - 1);
                    logd("compNum startsWith **133 && endsWith #");
                }
                this.mNetworkOperator = SystemPropertiesEx.get(HwTelephonyProperties.PROPERTY_NETWORK_OPERATOR, BuildConfig.FLAVOR);
                if (!TextUtils.isEmpty(countryIso)) {
                    String formattedCompNum = PhoneNumberUtils.formatNumberToE164(compNum4, countryIso.toUpperCase(Locale.US));
                    if (formattedCompNum != null) {
                        logd("formattedCompNum: " + formattedCompNum + ", with countryIso: " + countryIso);
                        compNum4 = formattedCompNum;
                        compNum2 = formattedCompNum;
                    } else {
                        compNum2 = formattedCompNum;
                    }
                } else {
                    compNum2 = null;
                }
                int countryCodeLen4 = getIntlPrefixAndCCLen(compNum4);
                if (countryCodeLen4 > 0) {
                    compNumPrefix = compNum4.substring(0, countryCodeLen4);
                    compNum4 = compNum4.substring(countryCodeLen4);
                    StringBuilder sb2 = new StringBuilder();
                    countryCodeLen = countryCodeLen4;
                    sb2.append("compNum after remove prefix: ");
                    sb2.append(compNum4);
                    sb2.append(", compNumLen: ");
                    sb2.append(compNum4.length());
                    sb2.append(", compNumPrefix: ");
                    sb2.append(compNumPrefix);
                    logd(sb2.toString());
                } else {
                    countryCodeLen = countryCodeLen4;
                }
                String tmpCompNum2 = !TextUtils.isEmpty(compNum2) ? compNum2 : compNum4;
                if (isRoamingCountryNumberByPrefix(compNumPrefix, countryIso)) {
                    logd("compNum belongs to roaming country");
                    int numMatch2 = SystemPropertiesEx.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_ROAMING, 7);
                    int numMatchShort2 = SystemPropertiesEx.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT_ROAMING, numMatch2);
                    int NUM_LONG6 = numMatch2 < 7 ? 7 : numMatch2;
                    NUM_SHORT = numMatchShort2 >= NUM_LONG6 ? NUM_LONG6 : numMatchShort2;
                    logd("getCallerIndex(), roaming prop NUM_LONG = " + NUM_LONG6 + ", NUM_SHORT = " + NUM_SHORT);
                    NUM_LONG5 = NUM_LONG6;
                } else {
                    NUM_SHORT = NUM_SHORT5;
                }
                boolean isCnNumber = isChineseNumberByPrefix(compNumPrefix, countryIso);
                if (isCnNumber) {
                    String compNum5 = deleteIPHead(compNum4);
                    boolean isCompNumCnMPN2 = isChineseMobilePhoneNumber(compNum5);
                    if (!isCompNumCnMPN2) {
                        int areaCodeLen = getChineseFixNumberAreaCodeLength(compNum5);
                        if (areaCodeLen > 0) {
                            isCompNumCnMPN = isCompNumCnMPN2;
                            compNumAreaCode4 = compNum5.substring(0, areaCodeLen);
                            compNum5 = compNum5.substring(areaCodeLen);
                            logd("CN compNum after remove area code: " + compNum5 + ", compNumLen: " + compNum5.length() + ", compNumAreaCode: " + compNumAreaCode4);
                        } else {
                            isCompNumCnMPN = isCompNumCnMPN2;
                        }
                        origTmpNum = null;
                        compNumAreaCode = compNumAreaCode4;
                        NUM_LONG = CN_NUM_MATCH;
                        NUM_SHORT2 = CN_NUM_MATCH;
                        origTmpNum2 = compNum5;
                    } else {
                        isCompNumCnMPN = isCompNumCnMPN2;
                        origTmpNum = null;
                        compNumAreaCode = null;
                        NUM_LONG = CN_NUM_MATCH;
                        NUM_SHORT2 = CN_NUM_MATCH;
                        origTmpNum2 = compNum5;
                    }
                } else {
                    if ("PE".equalsIgnoreCase(countryIso)) {
                        logd("PE compNum start with 0 not remove it");
                        isCompNumCnMPN = false;
                        origTmpNum = null;
                    } else {
                        isCompNumCnMPN = false;
                        if (compNum4.length() >= 7) {
                            origTmpNum = null;
                            if (CHINA_AREACODE.equals(compNum4.substring(0, 1)) && !CHINA_AREACODE.equals(compNum4.substring(1, 2))) {
                                origTmpNum2 = compNum4.substring(1);
                                compNumAreaCode = null;
                                NUM_LONG = NUM_LONG5;
                                NUM_SHORT2 = NUM_SHORT;
                            }
                        } else {
                            origTmpNum = null;
                        }
                    }
                    origTmpNum2 = compNum4;
                    compNumAreaCode = null;
                    NUM_LONG = NUM_LONG5;
                    NUM_SHORT2 = NUM_SHORT;
                }
                int compNumLen = origTmpNum2.length();
                String tmpNumAreaCode11 = null;
                String tmpNumPrefix10 = null;
                String tmpNumFormat5 = null;
                String str2 = CHINA_AREACODE;
                String compNumAreaCode5 = compNumAreaCode;
                if (compNumLen >= NUM_LONG) {
                    String compNumLong = origTmpNum2.substring(compNumLen - NUM_LONG);
                    int NUM_LONG7 = NUM_LONG;
                    String compNumShort4 = origTmpNum2.substring(compNumLen - NUM_SHORT2);
                    logd("11:, compNumLong = " + compNumLong + ",compNumShort = " + compNumShort4);
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(columnName);
                        int formatColumnIndex = cursor.getColumnIndex("normalized_number");
                        int data4ColumnIndex2 = cursor.getColumnIndex("data4");
                        logd("11: columnIndex: " + columnIndex + ", formatColumnIndex: " + formatColumnIndex + ", data4ColumnIndex: " + data4ColumnIndex2);
                        if (columnIndex != -1) {
                            int numShortID2 = -1;
                            int numLongID2 = -1;
                            while (true) {
                                String tmpNum8 = cursor.getString(columnIndex);
                                if (tmpNum8 == null) {
                                    return -1;
                                }
                                if (tmpNum8.indexOf(64) >= 0) {
                                    return -1;
                                }
                                origTmpNum6 = PhoneNumberUtils.stripSeparators(tmpNum8);
                                logd("origTmpNum: " + origTmpNum6);
                                if (-1 != formatColumnIndex) {
                                    tmpNumFormat4 = cursor.getString(formatColumnIndex);
                                    tmpNum7 = isValidData4Number(origTmpNum6, tmpNumFormat4) ? tmpNumFormat4 : origTmpNum6;
                                } else if (-1 != data4ColumnIndex2) {
                                    tmpNumFormat4 = cursor.getString(data4ColumnIndex2);
                                    tmpNum7 = isValidData4Number(origTmpNum6, tmpNumFormat4) ? tmpNumFormat4 : origTmpNum6;
                                } else {
                                    tmpNum7 = origTmpNum6;
                                    tmpNumFormat4 = tmpNumFormat5;
                                }
                                logd("11: tmpNumFormat: " + tmpNumFormat4);
                                tmpNum7.length();
                                logd("11: tmpNum = " + tmpNum7 + ", tmpNum.length11: " + tmpNum7.length() + ",ID = " + cursor.getPosition());
                                if (tmpNum7.equals(tmpCompNum2)) {
                                    numLongID = cursor.getPosition();
                                    logd("11: > NUM_LONG numLongID = " + numLongID + ", formattedNum full match!");
                                    tmpNumFormat5 = tmpNumFormat4;
                                    break;
                                }
                                int countryCodeLen5 = getIntlPrefixAndCCLen(tmpNum7);
                                if (countryCodeLen5 > 0) {
                                    tmpNumAreaCode8 = null;
                                    tmpNumFormat5 = tmpNumFormat4;
                                    tmpNumPrefix8 = tmpNum7.substring(0, countryCodeLen5);
                                    tmpNum7 = tmpNum7.substring(countryCodeLen5);
                                    StringBuilder sb3 = new StringBuilder();
                                    countryCodeLen = countryCodeLen5;
                                    sb3.append("11: tmpNum after remove prefix: ");
                                    sb3.append(tmpNum7);
                                    sb3.append(", tmpNum.length11: ");
                                    sb3.append(tmpNum7.length());
                                    sb3.append(", tmpNumPrefix: ");
                                    sb3.append(tmpNumPrefix8);
                                    logd(sb3.toString());
                                } else {
                                    countryCodeLen = countryCodeLen5;
                                    tmpNumAreaCode8 = null;
                                    tmpNumFormat5 = tmpNumFormat4;
                                    tmpNumPrefix8 = null;
                                }
                                if (isEqualCountryCodePrefix(compNumPrefix, countryIso, tmpNumPrefix8, null)) {
                                    if (isCnNumber) {
                                        String tmpNum9 = deleteIPHead(tmpNum7);
                                        boolean isTmpNumCnMPN = isChineseMobilePhoneNumber(tmpNum9);
                                        if ((!isCompNumCnMPN || isTmpNumCnMPN) && (isCompNumCnMPN || !isTmpNumCnMPN)) {
                                            if (!isCompNumCnMPN || !isTmpNumCnMPN) {
                                                int areaCodeLen2 = getChineseFixNumberAreaCodeLength(tmpNum9);
                                                if (areaCodeLen2 > 0) {
                                                    tmpNumPrefix9 = tmpNumPrefix8;
                                                    String tmpNumAreaCode12 = tmpNum9.substring(0, areaCodeLen2);
                                                    tmpNum9 = tmpNum9.substring(areaCodeLen2);
                                                    logd("11: CN tmpNum after remove area code: " + tmpNum9 + ", tmpNum.length11: " + tmpNum9.length() + ", tmpNumAreaCode: " + tmpNumAreaCode12);
                                                    tmpNumAreaCode10 = tmpNumAreaCode12;
                                                } else {
                                                    tmpNumPrefix9 = tmpNumPrefix8;
                                                    tmpNumAreaCode10 = tmpNumAreaCode8;
                                                }
                                                compNumAreaCode3 = compNumAreaCode5;
                                                if (!isEqualChineseFixNumberAreaCode(compNumAreaCode3, tmpNumAreaCode10)) {
                                                    logd("11: areacode prefix not same, continue");
                                                    compNumAreaCode5 = compNumAreaCode3;
                                                    tmpNumAreaCode8 = tmpNumAreaCode10;
                                                } else {
                                                    tmpNum7 = tmpNum9;
                                                }
                                            } else {
                                                logd("11: compNum and tmpNum are both MPN, continue to match by mccmnc");
                                                tmpNum7 = tmpNum9;
                                                tmpNumPrefix9 = tmpNumPrefix8;
                                                tmpNumAreaCode10 = tmpNumAreaCode8;
                                                compNumAreaCode3 = compNumAreaCode5;
                                            }
                                            compNumAreaCode5 = compNumAreaCode3;
                                            tmpNumAreaCode8 = tmpNumAreaCode10;
                                            tmpNumAreaCode9 = str2;
                                        } else {
                                            logd("11: compNum and tmpNum not both MPN, continue");
                                            tmpNumPrefix9 = tmpNumPrefix8;
                                        }
                                    } else {
                                        tmpNumPrefix9 = tmpNumPrefix8;
                                        if (tmpNum7.length() >= 7) {
                                            tmpNumAreaCode9 = str2;
                                            if (tmpNumAreaCode9.equals(tmpNum7.substring(0, 1))) {
                                                compNumAreaCode5 = compNumAreaCode5;
                                                if (!tmpNumAreaCode9.equals(tmpNum7.substring(1, 2))) {
                                                    tmpNum7 = tmpNum7.substring(1);
                                                    logd("11: tmpNum remove 0 at beginning");
                                                }
                                            } else {
                                                compNumAreaCode5 = compNumAreaCode5;
                                            }
                                        } else {
                                            compNumAreaCode5 = compNumAreaCode5;
                                            tmpNumAreaCode9 = str2;
                                        }
                                    }
                                    int tmpNumLen = tmpNum7.length();
                                    if (tmpNumLen >= NUM_LONG7) {
                                        String tmpNumLong = tmpNum7.substring(tmpNumLen - NUM_LONG7);
                                        NUM_LONG7 = NUM_LONG7;
                                        if (-1 == numLongID2 && compNumLong.compareTo(tmpNumLong) == 0) {
                                            int numLongID3 = cursor.getPosition();
                                            StringBuilder sb4 = new StringBuilder();
                                            str2 = tmpNumAreaCode9;
                                            sb4.append("11: > NUM_LONG numLongID = ");
                                            sb4.append(numLongID3);
                                            logd(sb4.toString());
                                            numLongID2 = numLongID3;
                                        } else {
                                            str2 = tmpNumAreaCode9;
                                            logd("11: >=NUM_LONG, and !=,  tmpNumLong = " + tmpNumLong + ", numLongID:" + numLongID2);
                                        }
                                    } else {
                                        NUM_LONG7 = NUM_LONG7;
                                        str2 = tmpNumAreaCode9;
                                        if (tmpNumLen >= NUM_SHORT2) {
                                            String tmpNumShort5 = tmpNum7.substring(tmpNumLen - NUM_SHORT2);
                                            if (-1 == numShortID2 && compNumShort4.compareTo(tmpNumShort5) == 0) {
                                                numShortID2 = cursor.getPosition();
                                            }
                                            logd("11: >=NUM_SHORT, tmpNumShort = " + tmpNumShort5 + ", numShortID:" + numShortID2);
                                            tmpNumShort4 = tmpNumShort5;
                                        } else {
                                            logd("tmpNum11, continue");
                                        }
                                    }
                                } else {
                                    tmpNumPrefix9 = tmpNumPrefix8;
                                    logd("11: countrycode prefix not same, continue");
                                }
                                if (!cursor.moveToNext()) {
                                    numLongID = numLongID2;
                                    tmpNumPrefix10 = tmpNumAreaCode8;
                                    tmpNumAreaCode11 = tmpNumPrefix9;
                                    break;
                                }
                                columnIndex = columnIndex;
                                formatColumnIndex = formatColumnIndex;
                                data4ColumnIndex2 = data4ColumnIndex2;
                                tmpNumPrefix10 = tmpNumAreaCode8;
                                tmpNumAreaCode11 = tmpNumPrefix9;
                            }
                            logd("11:  numLongID = " + numLongID + ",numShortID = " + numShortID2);
                            if (-1 != numLongID) {
                                origTmpNum5 = origTmpNum6;
                                tmpNumShort3 = tmpNumShort4;
                                tmpNumPrefix7 = tmpNumAreaCode11;
                                countryCodeLen3 = countryCodeLen;
                                tmpNumAreaCode7 = tmpNumPrefix10;
                                tmpNumFormat3 = tmpNumFormat5;
                            } else if (-1 != numShortID2) {
                                numLongID = numShortID2;
                                origTmpNum5 = origTmpNum6;
                                tmpNumShort3 = tmpNumShort4;
                                tmpNumPrefix7 = tmpNumAreaCode11;
                                countryCodeLen3 = countryCodeLen;
                                tmpNumAreaCode7 = tmpNumPrefix10;
                                tmpNumFormat3 = tmpNumFormat5;
                            } else {
                                numLongID = -1;
                                origTmpNum5 = origTmpNum6;
                                tmpNumShort3 = tmpNumShort4;
                                tmpNumPrefix7 = tmpNumAreaCode11;
                                countryCodeLen3 = countryCodeLen;
                                tmpNumAreaCode7 = tmpNumPrefix10;
                                tmpNumFormat3 = tmpNumFormat5;
                            }
                        } else {
                            tmpNumShort3 = null;
                            numLongID = fixedIndex;
                            tmpNumPrefix7 = null;
                            countryCodeLen3 = countryCodeLen;
                            tmpNumAreaCode7 = null;
                            tmpNumFormat3 = null;
                            origTmpNum5 = origTmpNum;
                        }
                        str = countryIso;
                        NUM_SHORT3 = NUM_SHORT2;
                        tmpCompNum = tmpCompNum2;
                        tmpNumShort = origTmpNum2;
                        countryCodeLen2 = compNumLen;
                        NUM_SHORT4 = numLongID;
                    } else {
                        str = countryIso;
                        NUM_SHORT3 = NUM_SHORT2;
                        tmpCompNum = tmpCompNum2;
                        NUM_SHORT4 = fixedIndex;
                        tmpNumShort = origTmpNum2;
                        countryCodeLen2 = compNumLen;
                    }
                } else {
                    str = countryIso;
                    String compNum6 = origTmpNum2;
                    int NUM_SHORT6 = NUM_LONG;
                    String str3 = str2;
                    if (compNumLen >= NUM_SHORT2) {
                        String compNumShort5 = compNum6.substring(compNumLen - NUM_SHORT2);
                        StringBuilder sb5 = new StringBuilder();
                        NUM_SHORT3 = NUM_SHORT2;
                        sb5.append("7:  compNumShort = ");
                        sb5.append(compNumShort5);
                        logd(sb5.toString());
                        if (cursor.moveToFirst()) {
                            int columnIndex2 = cursor.getColumnIndex(columnName);
                            int formatColumnIndex2 = cursor.getColumnIndex("normalized_number");
                            int data4ColumnIndex3 = cursor.getColumnIndex("data4");
                            StringBuilder sb6 = new StringBuilder();
                            String compNumShort6 = compNumShort5;
                            sb6.append("7: columnIndex: ");
                            sb6.append(columnIndex2);
                            sb6.append(", formatColumnIndex: ");
                            sb6.append(formatColumnIndex2);
                            sb6.append(", data4ColumnIndex: ");
                            sb6.append(data4ColumnIndex3);
                            logd(sb6.toString());
                            if (columnIndex2 != -1) {
                                int numShortID3 = -1;
                                int numLongID4 = -1;
                                while (true) {
                                    String tmpNum10 = cursor.getString(columnIndex2);
                                    if (tmpNum10 == null) {
                                        return -1;
                                    }
                                    if (tmpNum10.indexOf(64) >= 0) {
                                        return -1;
                                    }
                                    origTmpNum4 = PhoneNumberUtils.stripSeparators(tmpNum10);
                                    logd("origTmpNum: " + origTmpNum4);
                                    if (-1 != formatColumnIndex2) {
                                        tmpNumFormat2 = cursor.getString(formatColumnIndex2);
                                        tmpNum5 = isValidData4Number(origTmpNum4, tmpNumFormat2) ? tmpNumFormat2 : origTmpNum4;
                                    } else if (-1 != data4ColumnIndex3) {
                                        tmpNumFormat2 = cursor.getString(data4ColumnIndex3);
                                        tmpNum5 = isValidData4Number(origTmpNum4, tmpNumFormat2) ? tmpNumFormat2 : origTmpNum4;
                                    } else {
                                        tmpNum5 = origTmpNum4;
                                        tmpNumFormat2 = tmpNumFormat5;
                                    }
                                    logd("7: tmpNumFormat: " + tmpNumFormat2);
                                    tmpNum5.length();
                                    logd("7: tmpNum = " + tmpNum5 + ", tmpNum.length7: " + tmpNum5.length() + ",ID = " + cursor.getPosition());
                                    if (tmpNum5.equals(tmpCompNum2)) {
                                        numShortID = cursor.getPosition();
                                        logd("7: >= NUM_SHORT numShortID = " + numShortID + ", formattedNum full match!");
                                        tmpNumFormat5 = tmpNumFormat2;
                                        compNumShort3 = compNumShort6;
                                        NUM_LONG2 = NUM_SHORT3;
                                        break;
                                    }
                                    int countryCodeLen6 = getIntlPrefixAndCCLen(tmpNum5);
                                    if (countryCodeLen6 > 0) {
                                        tmpNumAreaCode5 = null;
                                        tmpNumFormat5 = tmpNumFormat2;
                                        tmpNumPrefix5 = tmpNum5.substring(0, countryCodeLen6);
                                        tmpNum5 = tmpNum5.substring(countryCodeLen6);
                                        logd("7: tmpNum after remove prefix: " + tmpNum5 + ", tmpNum.length7: " + tmpNum5.length() + ", tmpNumPrefix: " + tmpNumPrefix5);
                                    } else {
                                        tmpNumAreaCode5 = null;
                                        tmpNumFormat5 = tmpNumFormat2;
                                        tmpNumPrefix5 = null;
                                    }
                                    if (isEqualCountryCodePrefix(compNumPrefix, str, tmpNumPrefix5, null)) {
                                        if (isCnNumber) {
                                            String tmpNum11 = deleteIPHead(tmpNum5);
                                            boolean isTmpNumCnMPN2 = isChineseMobilePhoneNumber(tmpNum11);
                                            if ((!isCompNumCnMPN || isTmpNumCnMPN2) && (isCompNumCnMPN || !isTmpNumCnMPN2)) {
                                                if (!isCompNumCnMPN || !isTmpNumCnMPN2) {
                                                    int areaCodeLen3 = getChineseFixNumberAreaCodeLength(tmpNum11);
                                                    if (areaCodeLen3 > 0) {
                                                        tmpNumPrefix6 = tmpNumPrefix5;
                                                        String tmpNumAreaCode13 = tmpNum11.substring(0, areaCodeLen3);
                                                        tmpNum11 = tmpNum11.substring(areaCodeLen3);
                                                        logd("7: CN tmpNum after remove area code: " + tmpNum11 + ", tmpNum.length7: " + tmpNum11.length() + ", tmpNumAreaCode: " + tmpNumAreaCode13);
                                                        tmpNumAreaCode6 = tmpNumAreaCode13;
                                                    } else {
                                                        tmpNumPrefix6 = tmpNumPrefix5;
                                                        tmpNumAreaCode6 = tmpNumAreaCode5;
                                                    }
                                                    compNumAreaCode2 = compNumAreaCode5;
                                                    if (!isEqualChineseFixNumberAreaCode(compNumAreaCode2, tmpNumAreaCode6)) {
                                                        logd("7: areacode prefix not same, continue");
                                                        compNumAreaCode5 = compNumAreaCode2;
                                                        tmpNum6 = tmpNum11;
                                                        tmpNumAreaCode5 = tmpNumAreaCode6;
                                                        NUM_LONG3 = NUM_SHORT6;
                                                        compNumShort3 = compNumShort6;
                                                        NUM_LONG2 = NUM_SHORT3;
                                                    } else {
                                                        tmpNum5 = tmpNum11;
                                                    }
                                                } else {
                                                    logd("7: compNum and tmpNum are both MPN, continue to match by mccmnc");
                                                    tmpNum5 = tmpNum11;
                                                    tmpNumPrefix6 = tmpNumPrefix5;
                                                    tmpNumAreaCode6 = tmpNumAreaCode5;
                                                    compNumAreaCode2 = compNumAreaCode5;
                                                }
                                                tmpNumAreaCode5 = tmpNumAreaCode6;
                                            } else {
                                                logd("7: compNum and tmpNum not both MPN, continue");
                                                tmpNum6 = tmpNum11;
                                                NUM_LONG3 = NUM_SHORT6;
                                                tmpNumPrefix6 = tmpNumPrefix5;
                                                compNumShort3 = compNumShort6;
                                                NUM_LONG2 = NUM_SHORT3;
                                            }
                                        } else {
                                            tmpNumPrefix6 = tmpNumPrefix5;
                                            compNumAreaCode2 = compNumAreaCode5;
                                            if (tmpNum5.length() >= 7 && str3.equals(tmpNum5.substring(0, 1)) && !str3.equals(tmpNum5.substring(1, 2))) {
                                                tmpNum5 = tmpNum5.substring(1);
                                                logd("7: tmpNum remove 0 at beginning");
                                            }
                                        }
                                        int tmpNumLen2 = tmpNum5.length();
                                        if (tmpNumLen2 >= NUM_SHORT6) {
                                            String tmpNumShort6 = tmpNum5.substring(tmpNumLen2 - NUM_SHORT3);
                                            if (-1 == numLongID4) {
                                                compNumShort3 = compNumShort6;
                                                if (compNumShort3.compareTo(tmpNumShort6) == 0) {
                                                    numLongID4 = cursor.getPosition();
                                                }
                                            } else {
                                                compNumShort3 = compNumShort6;
                                            }
                                            compNumAreaCode5 = compNumAreaCode2;
                                            StringBuilder sb7 = new StringBuilder();
                                            NUM_LONG3 = NUM_SHORT6;
                                            sb7.append("7: >=NUM_LONG, tmpNumShort = ");
                                            sb7.append(tmpNumShort6);
                                            sb7.append(", numLongID:");
                                            sb7.append(numLongID4);
                                            logd(sb7.toString());
                                            tmpNumShort4 = tmpNumShort6;
                                            tmpNum6 = tmpNum5;
                                            NUM_LONG2 = NUM_SHORT3;
                                        } else {
                                            compNumAreaCode5 = compNumAreaCode2;
                                            NUM_LONG3 = NUM_SHORT6;
                                            compNumShort3 = compNumShort6;
                                            NUM_LONG2 = NUM_SHORT3;
                                            if (tmpNumLen2 >= NUM_LONG2) {
                                                String tmpNumShort7 = tmpNum5.substring(tmpNumLen2 - NUM_LONG2);
                                                if (-1 == numShortID3 && compNumShort3.compareTo(tmpNumShort7) == 0) {
                                                    int numShortID4 = cursor.getPosition();
                                                    logd("7: >= NUM_SHORT numShortID = " + numShortID4);
                                                    tmpNumShort4 = tmpNumShort7;
                                                    numShortID3 = numShortID4;
                                                    tmpNum6 = tmpNum5;
                                                } else {
                                                    logd("7: >=NUM_SHORT, and !=, tmpNumShort = " + tmpNumShort7 + ", numShortID:" + numShortID3);
                                                    tmpNumShort4 = tmpNumShort7;
                                                    tmpNum6 = tmpNum5;
                                                }
                                            } else {
                                                logd("7: continue");
                                                tmpNum6 = tmpNum5;
                                            }
                                        }
                                    } else {
                                        NUM_LONG3 = NUM_SHORT6;
                                        tmpNumPrefix6 = tmpNumPrefix5;
                                        compNumShort3 = compNumShort6;
                                        NUM_LONG2 = NUM_SHORT3;
                                        logd("7: countrycode prefix not same, continue");
                                        tmpNum6 = tmpNum5;
                                    }
                                    if (!cursor.moveToNext()) {
                                        numShortID = numShortID3;
                                        tmpNum5 = tmpNum6;
                                        tmpNumPrefix10 = tmpNumAreaCode5;
                                        tmpNumAreaCode11 = tmpNumPrefix6;
                                        break;
                                    }
                                    NUM_SHORT3 = NUM_LONG2;
                                    compNumShort6 = compNumShort3;
                                    columnIndex2 = columnIndex2;
                                    formatColumnIndex2 = formatColumnIndex2;
                                    data4ColumnIndex3 = data4ColumnIndex3;
                                    NUM_SHORT6 = NUM_LONG3;
                                    tmpNumPrefix10 = tmpNumAreaCode5;
                                    tmpNumAreaCode11 = tmpNumPrefix6;
                                }
                                logd("7: numShortID = " + numShortID + ",numLongID = " + numLongID4);
                                if (-1 != numShortID) {
                                    compNumShort2 = compNumShort3;
                                    origTmpNum3 = origTmpNum4;
                                    tmpNumShort2 = tmpNumShort4;
                                    tmpNumPrefix4 = tmpNumAreaCode11;
                                    tmpNumAreaCode4 = tmpNumPrefix10;
                                    compNumShort = tmpNumFormat5;
                                    tmpNum4 = tmpNum5;
                                } else if (-1 != numLongID4) {
                                    tmpNum4 = tmpNum5;
                                    origTmpNum3 = origTmpNum4;
                                    tmpNumShort2 = tmpNumShort4;
                                    tmpNumPrefix4 = tmpNumAreaCode11;
                                    tmpNumAreaCode4 = tmpNumPrefix10;
                                    numShortID = numLongID4;
                                    compNumShort2 = compNumShort3;
                                    compNumShort = tmpNumFormat5;
                                } else {
                                    tmpNum4 = tmpNum5;
                                    origTmpNum3 = origTmpNum4;
                                    tmpNumShort2 = tmpNumShort4;
                                    tmpNumPrefix4 = tmpNumAreaCode11;
                                    tmpNumAreaCode4 = tmpNumPrefix10;
                                    numShortID = -1;
                                    compNumShort2 = compNumShort3;
                                    compNumShort = tmpNumFormat5;
                                }
                            } else {
                                compNumShort2 = compNumShort6;
                                NUM_LONG2 = NUM_SHORT3;
                                tmpNum4 = null;
                                tmpNumShort2 = null;
                                numShortID = fixedIndex;
                                tmpNumPrefix4 = null;
                                tmpNumAreaCode4 = null;
                                compNumShort = null;
                                origTmpNum3 = origTmpNum;
                            }
                            tmpCompNum = tmpCompNum2;
                            NUM_SHORT3 = NUM_LONG2;
                            tmpNumShort = compNum6;
                            countryCodeLen2 = compNumLen;
                            NUM_SHORT4 = numShortID;
                        } else {
                            tmpCompNum = tmpCompNum2;
                            NUM_SHORT4 = fixedIndex;
                            tmpNumShort = compNum6;
                            countryCodeLen2 = compNumLen;
                        }
                    } else {
                        int compNumLen2 = compNumLen;
                        if (cursor.moveToFirst()) {
                            int columnIndex3 = cursor.getColumnIndex(columnName);
                            int compNumLen3 = cursor.getColumnIndex("normalized_number");
                            int data4ColumnIndex4 = cursor.getColumnIndex("data4");
                            NUM_SHORT3 = NUM_SHORT2;
                            logd("5: columnIndex: " + columnIndex3 + ", formatColumnIndex: " + compNumLen3 + ", data4ColumnIndex: " + data4ColumnIndex4);
                            if (columnIndex3 != -1) {
                                int fixedIndex5 = fixedIndex;
                                while (true) {
                                    String tmpNum12 = cursor.getString(columnIndex3);
                                    if (tmpNum12 == null || tmpNum12.indexOf(64) >= 0) {
                                        break;
                                    }
                                    String origTmpNum7 = PhoneNumberUtils.stripSeparators(tmpNum12);
                                    logd("origTmpNum: " + origTmpNum7);
                                    if (-1 != compNumLen3) {
                                        tmpNumFormat = cursor.getString(compNumLen3);
                                        tmpNum = isValidData4Number(origTmpNum7, tmpNumFormat) ? tmpNumFormat : origTmpNum7;
                                    } else if (-1 != data4ColumnIndex4) {
                                        tmpNumFormat = cursor.getString(data4ColumnIndex4);
                                        tmpNum = isValidData4Number(origTmpNum7, tmpNumFormat) ? tmpNumFormat : origTmpNum7;
                                    } else {
                                        tmpNum = origTmpNum7;
                                        tmpNumFormat = tmpNumFormat5;
                                    }
                                    logd("5: tmpNumFormat: " + tmpNumFormat);
                                    tmpNum.length();
                                    logd("5: tmpNum = " + tmpNum + ", tmpNum.length: " + tmpNum.length() + ",ID = " + cursor.getPosition());
                                    if (tmpNum.equals(tmpCompNum2)) {
                                        fixedIndex2 = cursor.getPosition();
                                        logd("5: break! numLongID = " + fixedIndex2 + ", formattedNum full match!");
                                        tmpCompNum = tmpCompNum2;
                                        tmpNum2 = tmpNum;
                                        tmpNumPrefix = tmpNumAreaCode11;
                                        tmpNumAreaCode = tmpNumPrefix10;
                                        tmpNumShort = compNum6;
                                        countryCodeLen2 = compNumLen2;
                                        break;
                                    }
                                    int countryCodeLen7 = getIntlPrefixAndCCLen(tmpNum);
                                    if (countryCodeLen7 > 0) {
                                        tmpNumAreaCode2 = null;
                                        data4ColumnIndex = data4ColumnIndex4;
                                        tmpNumPrefix2 = tmpNum.substring(0, countryCodeLen7);
                                        tmpNum = tmpNum.substring(countryCodeLen7);
                                        logd("5: tmpNum after remove prefix: " + tmpNum + ", tmpNum.length5: " + tmpNum.length() + ", tmpNumPrefix: " + tmpNumPrefix2);
                                    } else {
                                        tmpNumAreaCode2 = null;
                                        data4ColumnIndex = data4ColumnIndex4;
                                        tmpNumPrefix2 = null;
                                    }
                                    if (isEqualCountryCodePrefix(compNumPrefix, str, tmpNumPrefix2, null)) {
                                        if (isCnNumber) {
                                            String tmpNum13 = deleteIPHead(tmpNum);
                                            boolean isTmpNumCnMPN3 = isChineseMobilePhoneNumber(tmpNum13);
                                            if ((!isCompNumCnMPN || isTmpNumCnMPN3) && (isCompNumCnMPN || !isTmpNumCnMPN3)) {
                                                if (!isCompNumCnMPN || !isTmpNumCnMPN3) {
                                                    int areaCodeLen4 = getChineseFixNumberAreaCodeLength(tmpNum13);
                                                    if (areaCodeLen4 > 0) {
                                                        tmpNumPrefix3 = tmpNumPrefix2;
                                                        String tmpNumAreaCode14 = tmpNum13.substring(0, areaCodeLen4);
                                                        tmpNum13 = tmpNum13.substring(areaCodeLen4);
                                                        StringBuilder sb8 = new StringBuilder();
                                                        tmpCompNum = tmpCompNum2;
                                                        sb8.append("5: CN tmpNum after remove area code: ");
                                                        sb8.append(tmpNum13);
                                                        sb8.append(", tmpNum.length5: ");
                                                        sb8.append(tmpNum13.length());
                                                        sb8.append(", tmpNumAreaCode: ");
                                                        sb8.append(tmpNumAreaCode14);
                                                        logd(sb8.toString());
                                                        tmpNumAreaCode3 = tmpNumAreaCode14;
                                                    } else {
                                                        tmpNumPrefix3 = tmpNumPrefix2;
                                                        tmpCompNum = tmpCompNum2;
                                                        tmpNumAreaCode3 = tmpNumAreaCode2;
                                                    }
                                                    if (!isEqualChineseFixNumberAreaCode(compNumAreaCode5, tmpNumAreaCode3)) {
                                                        logd("5: areacode prefix not same, continue");
                                                        tmpNum3 = tmpNum13;
                                                        tmpNumAreaCode2 = tmpNumAreaCode3;
                                                        tmpNumShort = compNum6;
                                                        countryCodeLen2 = compNumLen2;
                                                        compNum3 = str3;
                                                    } else {
                                                        tmpNum = tmpNum13;
                                                    }
                                                } else {
                                                    logd("5: compNum and tmpNum are both MPN, continue to match by mccmnc");
                                                    tmpNum = tmpNum13;
                                                    tmpNumPrefix3 = tmpNumPrefix2;
                                                    tmpCompNum = tmpCompNum2;
                                                    tmpNumAreaCode3 = tmpNumAreaCode2;
                                                }
                                                tmpNumAreaCode2 = tmpNumAreaCode3;
                                            } else {
                                                logd("5: compNum and tmpNum not both MPN, continue");
                                                tmpNum3 = tmpNum13;
                                                tmpNumPrefix3 = tmpNumPrefix2;
                                                tmpCompNum = tmpCompNum2;
                                                tmpNumShort = compNum6;
                                                countryCodeLen2 = compNumLen2;
                                                compNum3 = str3;
                                            }
                                        } else {
                                            tmpNumPrefix3 = tmpNumPrefix2;
                                            tmpCompNum = tmpCompNum2;
                                            if (tmpNum.length() >= 7) {
                                                if (str3.equals(tmpNum.substring(0, 1))) {
                                                    if (!str3.equals(tmpNum.substring(1, 2))) {
                                                        tmpNum = tmpNum.substring(1);
                                                        logd("5: tmpNum remove 0 at beginning");
                                                    }
                                                }
                                            }
                                        }
                                        countryCodeLen2 = compNumLen2;
                                        if (tmpNum.length() != countryCodeLen2) {
                                            tmpNumShort = compNum6;
                                            compNum3 = str3;
                                            logd("5: continue");
                                        } else if (-1 == fixedIndex5) {
                                            tmpNumShort = compNum6;
                                            if (tmpNumShort.compareTo(tmpNum) == 0) {
                                                fixedIndex5 = cursor.getPosition();
                                                StringBuilder sb9 = new StringBuilder();
                                                compNum3 = str3;
                                                sb9.append("5: break! numLongID = ");
                                                sb9.append(fixedIndex5);
                                                logd(sb9.toString());
                                                tmpNum3 = tmpNum;
                                            } else {
                                                compNum3 = str3;
                                            }
                                        } else {
                                            tmpNumShort = compNum6;
                                            compNum3 = str3;
                                        }
                                        tmpNum3 = tmpNum;
                                    } else {
                                        tmpNumPrefix3 = tmpNumPrefix2;
                                        tmpCompNum = tmpCompNum2;
                                        tmpNumShort = compNum6;
                                        countryCodeLen2 = compNumLen2;
                                        compNum3 = str3;
                                        logd("5: countrycode prefix not same, continue");
                                        tmpNum3 = tmpNum;
                                    }
                                    if (!cursor.moveToNext()) {
                                        fixedIndex2 = fixedIndex5;
                                        tmpNum2 = tmpNum3;
                                        tmpNumAreaCode = tmpNumAreaCode2;
                                        tmpNumPrefix = tmpNumPrefix3;
                                        break;
                                    }
                                    compNumLen2 = countryCodeLen2;
                                    tmpNumFormat5 = tmpNumFormat;
                                    compNumLen3 = compNumLen3;
                                    columnIndex3 = columnIndex3;
                                    str3 = compNum3;
                                    tmpCompNum2 = tmpCompNum;
                                    compNum6 = tmpNumShort;
                                    data4ColumnIndex4 = data4ColumnIndex;
                                    tmpNumPrefix10 = tmpNumAreaCode2;
                                    tmpNumAreaCode11 = tmpNumPrefix3;
                                }
                                logd("5: fixedIndex = " + fixedIndex2);
                                NUM_SHORT4 = fixedIndex2;
                            } else {
                                tmpCompNum = tmpCompNum2;
                                tmpNumShort = compNum6;
                                countryCodeLen2 = compNumLen2;
                            }
                        } else {
                            tmpCompNum = tmpCompNum2;
                            NUM_SHORT3 = NUM_SHORT2;
                            tmpNumShort = compNum6;
                            countryCodeLen2 = compNumLen2;
                        }
                        NUM_SHORT4 = fixedIndex;
                    }
                }
                logd("fixedIndex: " + NUM_SHORT4);
                if (-1 != NUM_SHORT4) {
                    return NUM_SHORT4;
                }
                if (shouldDoNumberMatchAgainBySimMccmnc(compNum4, str)) {
                    return getCallerIndexInternal(cursor, compNum4, columnName, this.mSimNumLong, this.mSimNumShort);
                }
                return NUM_SHORT4;
            }
        }
        Log.e(TAG, "CallerInfoHW(), cursor is empty! fixedIndex = -1");
        return -1;
    }

    public static boolean isfixedIndexValid(String cookie, Cursor cursor) {
        int fixedIndex;
        if (cookie == null || cursor == null || (fixedIndex = new CallerInfoHW().getCallerIndex(cursor, cookie, "number")) == -1 || !cursor.moveToPosition(fixedIndex)) {
            return false;
        }
        return true;
    }

    private static void logd(String msg) {
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
            for (Integer num : this.countryCallingCodeToRegionCodeMap.keySet()) {
                int countrycode = num.intValue();
                if (tmpNumber.startsWith(Integer.toString(countrycode))) {
                    logd("extractCountryCodeFromNumber(), find matched country code: " + countrycode);
                    return len + Integer.toString(countrycode).length();
                }
            }
            logd("extractCountryCodeFromNumber(), no matched country code");
            return 0;
        }
        logd("extractCountryCodeFromNumber(), no valid prefix in number: " + number);
        return len;
    }

    private boolean isChineseMobilePhoneNumber(String number) {
        if (TextUtils.isEmpty(number) || number.length() < CN_NUM_MATCH || !number.substring(number.length() - CN_NUM_MATCH).matches(CN_MPN_PATTERN)) {
            return false;
        }
        logd("isChineseMobilePhoneNumber(), return true for number: " + number);
        return true;
    }

    private int getChineseFixNumberAreaCodeLength(String number) {
        String tmpNumber = number;
        if (TextUtils.isEmpty(tmpNumber) || tmpNumber.length() < CN_FIXED_NUMBER_WITH_AREA_CODE_MIN_LEN) {
            return 0;
        }
        if (!tmpNumber.startsWith(CHINA_AREACODE)) {
            tmpNumber = CHINA_AREACODE + tmpNumber;
        }
        int len = 2;
        String top2String = tmpNumber.substring(0, 2);
        if (top2String.equals(FIXED_NUMBER_TOP2_TOKEN1) || top2String.equals(FIXED_NUMBER_TOP2_TOKEN2)) {
            String areaCodeString = tmpNumber.substring(0, 3);
            ArrayList<String> areaCodeArray = this.chineseFixNumberAreaCodeMap.get(1);
            int areaCodeArraySize = areaCodeArray.size();
            for (int i = 0; i < areaCodeArraySize; i++) {
                if (areaCodeString.equals(areaCodeArray.get(i))) {
                    if (tmpNumber.equals(number)) {
                        len = 3;
                    }
                    logd("getChineseFixNumberAreaCodeLength(), matched area code len: " + len + ", number: " + number);
                    return len;
                }
            }
            return 0;
        }
        int len2 = 4;
        String areaCodeString2 = tmpNumber.substring(0, 4);
        ArrayList<String> areaCodeArray2 = this.chineseFixNumberAreaCodeMap.get(2);
        int areaCodeArraySize2 = areaCodeArray2.size();
        for (int i2 = 0; i2 < areaCodeArraySize2; i2++) {
            if (areaCodeString2.equals(areaCodeArray2.get(i2))) {
                if (!tmpNumber.equals(number)) {
                    len2 = 3;
                }
                logd("getChineseFixNumberAreaCodeLength(), matched area code len: " + len2 + ", number: " + number);
                return len2;
            }
        }
        return 0;
    }

    private boolean isEqualChineseFixNumberAreaCode(String compNumAreaCode, String dbNumAreaCode) {
        if (TextUtils.isEmpty(compNumAreaCode) && TextUtils.isEmpty(dbNumAreaCode)) {
            return true;
        }
        if (!TextUtils.isEmpty(compNumAreaCode) || TextUtils.isEmpty(dbNumAreaCode)) {
            if (TextUtils.isEmpty(compNumAreaCode) || !TextUtils.isEmpty(dbNumAreaCode)) {
                if (!compNumAreaCode.startsWith(CHINA_AREACODE)) {
                    compNumAreaCode = CHINA_AREACODE + compNumAreaCode;
                }
                if (!dbNumAreaCode.startsWith(CHINA_AREACODE)) {
                    dbNumAreaCode = CHINA_AREACODE + dbNumAreaCode;
                }
                return compNumAreaCode.equals(dbNumAreaCode);
            } else if (this.IS_MIIT_NUM_MATCH) {
                return false;
            } else {
                return true;
            }
        } else if (this.IS_MIIT_NUM_MATCH) {
            return false;
        } else {
            return true;
        }
    }

    private String deleteIPHead(String number) {
        if (TextUtils.isEmpty(number)) {
            return number;
        }
        int numberLen = number.length();
        if (numberLen < 5) {
            logd("deleteIPHead() numberLen is short than 5!");
            return number;
        }
        if (Arrays.binarySearch(IPHEAD, number.substring(0, 5)) >= 0) {
            number = number.substring(5, numberLen);
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
        int countryCode;
        int countryCode2;
        if (TextUtils.isEmpty(num1Prefix) && TextUtils.isEmpty(num2Prefix)) {
            logd("isEqualCountryCodePrefix(), both have no country code, return true");
            return true;
        } else if (TextUtils.isEmpty(num1Prefix) && !TextUtils.isEmpty(num2Prefix)) {
            logd("isEqualCountryCodePrefix(), netIso1: " + netIso1 + ", netIso2: " + netIso2);
            if (TextUtils.isEmpty(netIso1)) {
                return true;
            }
            String netIso = netIso1.toUpperCase();
            if ("CN".equals(netIso)) {
                countryCode2 = this.countryCodeforCN;
            } else {
                countryCode2 = sInstance.getCountryCodeForRegion(netIso);
            }
            return num2Prefix.substring(getIntlPrefixLength(num2Prefix)).equals(Integer.toString(countryCode2));
        } else if (TextUtils.isEmpty(num1Prefix) || !TextUtils.isEmpty(num2Prefix)) {
            return num1Prefix.substring(getIntlPrefixLength(num1Prefix)).equals(num2Prefix.substring(getIntlPrefixLength(num2Prefix)));
        } else {
            logd("isEqualCountryCodePrefix(), netIso1: " + netIso1 + ", netIso2: " + netIso2);
            if (TextUtils.isEmpty(netIso2)) {
                return true;
            }
            String netIso3 = netIso2.toUpperCase();
            if ("CN".equals(netIso3)) {
                countryCode = this.countryCodeforCN;
            } else {
                countryCode = sInstance.getCountryCodeForRegion(netIso3);
            }
            return num1Prefix.substring(getIntlPrefixLength(num1Prefix)).equals(Integer.toString(countryCode));
        }
    }

    private int getFullMatchIndex(Cursor cursor, String compNum, String columnName) {
        int columnIndex;
        String compNum2 = PhoneNumberUtils.stripSeparators(compNum);
        if (this.IS_CHINA_TELECOM && compNum2.startsWith("**133") && compNum2.endsWith("#")) {
            compNum2 = compNum2.substring(0, compNum2.length() - 1);
            logd("full match check, compNum startsWith **133 && endsWith #");
        }
        logd("full match check, compNum: " + compNum2);
        if (cursor == null || !cursor.moveToFirst() || -1 == (columnIndex = cursor.getColumnIndex(columnName))) {
            return -1;
        }
        do {
            String tmpNum = cursor.getString(columnIndex);
            if (tmpNum != null && tmpNum.indexOf(64) < 0) {
                tmpNum = PhoneNumberUtils.stripSeparators(tmpNum);
            }
            logd("full match check, tmpNum: " + tmpNum);
            if (compNum2.equals(tmpNum)) {
                int fixedIndex = cursor.getPosition();
                logd("exact match: break! fixedIndex = " + fixedIndex);
                return fixedIndex;
            }
        } while (cursor.moveToNext());
        return -1;
    }

    private boolean shouldDoNumberMatchAgainBySimMccmnc(String number, String countryIso) {
        if (!SystemPropertiesEx.getBoolean(HwTelephonyProperties.PROPERTY_NETWORK_ISROAMING, false) || getFormatNumberByCountryISO(number, countryIso) != null) {
            return false;
        }
        return true;
    }

    private String getFormatNumberByCountryISO(String number, String countryIso) {
        if (TextUtils.isEmpty(number) || TextUtils.isEmpty(countryIso)) {
            return null;
        }
        return PhoneNumberUtils.formatNumberToE164(number, countryIso.toUpperCase(Locale.US));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:125:0x0454, code lost:
        return -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x034d, code lost:
        logd("7: numShortID = " + r10 + ", numLongID = " + r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x036a, code lost:
        if (-1 == r10) goto L_0x0372;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x036c, code lost:
        r12 = r10;
        r7 = r8;
        r8 = r18;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x0372, code lost:
        if (-1 == r11) goto L_0x037a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x0374, code lost:
        r12 = r11;
        r7 = r8;
        r8 = r18;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x037a, code lost:
        r12 = -1;
        r7 = r8;
        r8 = r18;
     */
    private int getCallerIndexInternal(Cursor cursor, String compNum, String columnName, int numLong, int numShort) {
        String compNumShort;
        String tmpNumLong;
        String tmpNumShort;
        String tmpNum;
        String tmpNumLong2;
        String compNumLong;
        String compNumLong2 = null;
        String tmpNumLong3 = null;
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
        int NUM_LONG = 7;
        if (numLong >= 7) {
            NUM_LONG = numLong;
        }
        int NUM_SHORT = numShort >= NUM_LONG ? NUM_LONG : numShort;
        logd("getCallerIndexInternal, after check NUM_LONG: " + NUM_LONG + ", NUM_SHORT: " + NUM_SHORT);
        if (cursor != null) {
            compNumShort = null;
            if (compNumLen >= NUM_LONG) {
                String compNumLong3 = compNum.substring(compNumLen - NUM_LONG);
                String compNumShort2 = compNum.substring(compNumLen - NUM_SHORT);
                StringBuilder sb = new StringBuilder();
                String tmpNumShort2 = null;
                sb.append("11: compNumLong = ");
                sb.append(compNumLong3);
                sb.append(", compNumShort = ");
                sb.append(compNumShort2);
                logd(sb.toString());
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(columnName);
                    if (columnIndex != -1) {
                        while (true) {
                            String tmpNum2 = cursor.getString(columnIndex);
                            if (tmpNum2 == null) {
                                return -1;
                            }
                            if (tmpNum2.indexOf(64) >= 0) {
                                return -1;
                            }
                            tmpNum = PhoneNumberUtils.stripSeparators(tmpNum2);
                            int tmpNumLen = tmpNum.length();
                            tmpNumLong2 = tmpNumLong3;
                            logd("11: tmpNum = " + tmpNum + ", tmpNum.length11: " + tmpNum.length() + ", ID = " + cursor.getPosition());
                            if (compNum.equals(tmpNum)) {
                                int numLongID2 = cursor.getPosition();
                                logd("exact match: break! numLongID = " + numLongID2);
                                numLongID = numLongID2;
                                break;
                            }
                            if (tmpNumLen >= NUM_LONG) {
                                tmpNumLong3 = tmpNum.substring(tmpNumLen - NUM_LONG);
                                if (-1 == numLongID && compNumLong3.compareTo(tmpNumLong3) == 0) {
                                    numLongID = cursor.getPosition();
                                    StringBuilder sb2 = new StringBuilder();
                                    compNumLong = compNumLong3;
                                    sb2.append("11: > NUM_LONG numLongID = ");
                                    sb2.append(numLongID);
                                    logd(sb2.toString());
                                } else {
                                    compNumLong = compNumLong3;
                                }
                                logd("11: >= NUM_LONG, and !=,  tmpNumLong = " + tmpNumLong3 + ", numLongID: " + numLongID);
                            } else {
                                compNumLong = compNumLong3;
                                if (tmpNumLen >= NUM_SHORT) {
                                    String tmpNumShort3 = tmpNum.substring(tmpNumLen - NUM_SHORT);
                                    if (-1 == numShortID && compNumShort2.compareTo(tmpNumShort3) == 0) {
                                        numShortID = cursor.getPosition();
                                    }
                                    logd("11: >= NUM_SHORT, tmpNumShort = " + tmpNumShort3 + ", numShortID:" + numShortID);
                                    tmpNumShort2 = tmpNumShort3;
                                    tmpNumLong3 = tmpNumLong2;
                                } else {
                                    logd("tmpNum11, continue");
                                    tmpNumLong3 = tmpNumLong2;
                                }
                            }
                            if (!cursor.moveToNext()) {
                                tmpNumLong2 = tmpNumLong3;
                                break;
                            }
                            columnIndex = columnIndex;
                            fixedIndex = fixedIndex;
                            compNumLong3 = compNumLong;
                        }
                        logd("11: numLongID = " + numLongID + ", numShortID = " + numShortID);
                        if (-1 != numLongID) {
                            fixedIndex = numLongID;
                            tmpNumLong3 = tmpNumLong2;
                            tmpNumShort = tmpNumShort2;
                        } else if (-1 != numShortID) {
                            fixedIndex = numShortID;
                            tmpNumLong3 = tmpNumLong2;
                            tmpNumShort = tmpNumShort2;
                        } else {
                            fixedIndex = -1;
                            tmpNumLong3 = tmpNumLong2;
                            tmpNumShort = tmpNumShort2;
                        }
                    } else {
                        tmpNum = null;
                        tmpNumShort = null;
                    }
                }
            } else {
                String tmpNumShort4 = null;
                int fixedIndex2 = -1;
                if (compNumLen >= NUM_SHORT) {
                    String compNumShort3 = compNum.substring(compNumLen - NUM_SHORT);
                    logd("7: compNumShort = " + compNumShort3);
                    if (cursor.moveToFirst()) {
                        int columnIndex2 = cursor.getColumnIndex(columnName);
                        if (columnIndex2 != -1) {
                            while (true) {
                                String tmpNum3 = cursor.getString(columnIndex2);
                                if (tmpNum3 == null || tmpNum3.indexOf(64) >= 0) {
                                    break;
                                }
                                String tmpNum4 = PhoneNumberUtils.stripSeparators(tmpNum3);
                                int tmpNumLen2 = tmpNum4.length();
                                logd("7: tmpNum = " + tmpNum4 + ", tmpNum.length7: " + tmpNum4.length() + ", ID = " + cursor.getPosition());
                                if (compNum.equals(tmpNum4)) {
                                    int numShortID2 = cursor.getPosition();
                                    logd("exact match numShortID = " + numShortID2);
                                    numShortID = numShortID2;
                                    break;
                                }
                                if (tmpNumLen2 >= NUM_LONG) {
                                    String tmpNumShort5 = tmpNum4.substring(tmpNumLen2 - NUM_SHORT);
                                    if (-1 == numLongID && compNumShort3.compareTo(tmpNumShort5) == 0) {
                                        numLongID = cursor.getPosition();
                                    }
                                    StringBuilder sb3 = new StringBuilder();
                                    tmpNumLong = tmpNumLong3;
                                    sb3.append("7: >= NUM_LONG, tmpNumShort = ");
                                    sb3.append(tmpNumShort5);
                                    sb3.append(", numLongID:");
                                    sb3.append(numLongID);
                                    logd(sb3.toString());
                                    tmpNumShort4 = tmpNumShort5;
                                } else {
                                    tmpNumLong = tmpNumLong3;
                                    if (tmpNumLen2 >= NUM_SHORT) {
                                        String tmpNumShort6 = tmpNum4.substring(tmpNumLen2 - NUM_SHORT);
                                        if (-1 == numShortID && compNumShort3.compareTo(tmpNumShort6) == 0) {
                                            numShortID = cursor.getPosition();
                                            logd("7: >= NUM_SHORT numShortID = " + numShortID);
                                        }
                                        logd("7: >= NUM_SHORT, and !=, tmpNumShort = " + tmpNumShort6 + ", numShortID:" + numShortID);
                                        tmpNumShort4 = tmpNumShort6;
                                    } else {
                                        logd("7: continue");
                                    }
                                }
                                if (!cursor.moveToNext()) {
                                    break;
                                }
                                columnIndex2 = columnIndex2;
                                compNumLong2 = compNumLong2;
                                tmpNumLong3 = tmpNumLong;
                            }
                            return -1;
                        }
                        String tmpNum5 = null;
                        String tmpNum6 = null;
                        fixedIndex = -1;
                    } else {
                        fixedIndex = -1;
                    }
                } else if (cursor.moveToFirst()) {
                    int columnIndex3 = cursor.getColumnIndex(columnName);
                    if (columnIndex3 != -1) {
                        while (true) {
                            String tmpNum7 = cursor.getString(columnIndex3);
                            if (tmpNum7 == null || tmpNum7.indexOf(64) >= 0) {
                                break;
                            }
                            String tmpNum8 = PhoneNumberUtils.stripSeparators(tmpNum7);
                            int tmpNumLen3 = tmpNum8.length();
                            logd("5: tmpNum = " + tmpNum8 + ", tmpNum.length: " + tmpNum8.length() + ", ID = " + cursor.getPosition());
                            if (tmpNumLen3 == compNumLen) {
                                fixedIndex = fixedIndex2;
                                if (-1 == fixedIndex && compNum.compareTo(tmpNum8) == 0) {
                                    int fixedIndex3 = cursor.getPosition();
                                    logd("5: break! numLongID = " + fixedIndex3);
                                    fixedIndex = fixedIndex3;
                                    break;
                                }
                            } else {
                                fixedIndex = fixedIndex2;
                                logd("5: continue");
                            }
                            if (!cursor.moveToNext()) {
                                break;
                            }
                            fixedIndex2 = fixedIndex;
                        }
                        logd("5: fixedIndex = " + fixedIndex);
                    } else {
                        fixedIndex = -1;
                    }
                } else {
                    fixedIndex = -1;
                }
            }
            logd("getCallerIndexInternal, fixedIndex: " + fixedIndex);
            return fixedIndex;
        }
        compNumShort = null;
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
        int NUM_LONG = 7;
        if (numLong >= 7) {
            NUM_LONG = numLong;
        }
        int NUM_SHORT = numShort >= NUM_LONG ? NUM_LONG : numShort;
        logd("compareNumsInternal, after check NUM_LONG: " + NUM_LONG + ", NUM_SHORT: " + NUM_SHORT);
        if (num1Len >= NUM_LONG) {
            String num1Long = num1.substring(num1Len - NUM_LONG);
            String num1Short = num1.substring(num1Len - NUM_SHORT);
            logd("compareNumsInternal, 11: num1Long = " + num1Long + ", num1Short = " + num1Short);
            if (num2Len >= NUM_LONG) {
                if (num1Long.compareTo(num2.substring(num2Len - NUM_LONG)) == 0) {
                    logd("compareNumsInternal, 11: >= NUM_LONG return true");
                    return true;
                }
            } else if (num2Len >= NUM_SHORT && num1Short.compareTo(num2.substring(num2Len - NUM_SHORT)) == 0) {
                logd("compareNumsInternal, 11: >= NUM_SHORT return true");
                return true;
            }
        } else if (num1Len >= NUM_SHORT) {
            String num1Short2 = num1.substring(num1Len - NUM_SHORT);
            logd("compareNumsInternal, 7: num1Short = " + num1Short2);
            if (num2Len >= NUM_SHORT && num1Short2.compareTo(num2.substring(num2Len - NUM_SHORT)) == 0) {
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
        if (SystemPropertiesEx.getBoolean(HwTelephonyProperties.PROPERTY_NETWORK_ISROAMING, false)) {
            if (TextUtils.isEmpty(numberPrefix)) {
                return true;
            }
            String numberPrefix2 = numberPrefix.substring(getIntlPrefixLength(numberPrefix));
            if (!TextUtils.isEmpty(numberPrefix2) && !TextUtils.isEmpty(netIso)) {
                return numberPrefix2.equals(Integer.toString(sInstance.getCountryCodeForRegion(netIso.toUpperCase(Locale.US))));
            }
        }
        return false;
    }

    private boolean isValidData4Number(String data1Num, String data4Num) {
        logd("isValidData4Number, data1Num: " + data1Num + ", data4Num: " + data4Num);
        if (TextUtils.isEmpty(data1Num) || TextUtils.isEmpty(data4Num) || !data4Num.startsWith("+")) {
            return false;
        }
        int countryCodeLen = getIntlPrefixAndCCLen(data4Num);
        if (countryCodeLen > 0) {
            data4Num = data4Num.substring(countryCodeLen);
        }
        logd("isValidData4Number, data4Num after remove prefix: " + data4Num);
        if (data4Num.length() > data1Num.length() || !data4Num.equals(data1Num.substring(data1Num.length() - data4Num.length()))) {
            return false;
        }
        return true;
    }

    private boolean isNormalPrefix(String number) {
        String sMcc = " ";
        int operatorCount = NORMAL_PREFIX_MCC.length;
        String str = this.mNetworkOperator;
        if (str != null && str.length() > 3) {
            sMcc = this.mNetworkOperator.substring(0, 3);
        }
        if (number.startsWith("011")) {
            for (int i = 0; i < operatorCount; i++) {
                if (sMcc.equals(NORMAL_PREFIX_MCC[i]) && number.length() == CN_NUM_MATCH) {
                    logd("those operator 011 are normal prefix");
                    return true;
                }
            }
        }
        return false;
    }

    private static String formatedForDualNumber(String compNum) {
        if (!IS_SUPPORT_DUAL_NUMBER) {
            return compNum;
        }
        if (isVirtualNum(compNum)) {
            compNum = compNum.substring(0, compNum.length() - 1);
        }
        if (compNum.startsWith("*230#")) {
            return compNum.substring(5, compNum.length());
        }
        if (compNum.startsWith("*23#")) {
            return compNum.substring(4, compNum.length());
        }
        return compNum;
    }

    private static boolean isVirtualNum(String dialString) {
        if (!dialString.endsWith("#")) {
            return false;
        }
        String tempstring = dialString.substring(0, dialString.length() - 1).replace(" ", BuildConfig.FLAVOR).replace("+", BuildConfig.FLAVOR).replace("-", BuildConfig.FLAVOR);
        if (tempstring.startsWith("*230#")) {
            tempstring = tempstring.substring(5, tempstring.length());
        } else if (tempstring.startsWith("*23#")) {
            tempstring = tempstring.substring(4, tempstring.length());
        }
        if (!tempstring.matches("[0-9]+")) {
            return false;
        }
        return true;
    }
}
