package huawei.android.telephony;

import android.database.Cursor;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import com.android.i18n.phonenumbers.CountryCodeToRegionCodeMapUtils;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.internal.telephony.HwTelephonyProperties;
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
    private static final String[] INTERNATIONAL_PREFIX = new String[]{"+00", "+", "00"};
    private static final String[] IPHEAD = new String[]{"10193", "11808", "12593", "17900", "17901", "17908", "17909", "17910", "17911", "17931", "17950", "17951", "17960", "17968", "17969", "96435"};
    private static final int IPHEAD_LENTH = 5;
    private static final boolean IS_SUPPORT_DUAL_NUMBER = SystemProperties.getBoolean("ro.config.hw_dual_number", false);
    public static final int MIN_MATCH = 7;
    private static final String[] NORMAL_PREFIX_MCC = new String[]{"602", "722"};
    private static final String TAG = "CallerInfo";
    private static CallerInfoHW sCallerInfoHwInstance = null;
    private static PhoneNumberUtil sInstance = PhoneNumberUtil.getInstance();
    private boolean IS_CHINA_TELECOM;
    private boolean IS_MIIT_NUM_MATCH;
    private final int NUM_LONG_CUST;
    private final int NUM_SHORT_CUST;
    private final Map<Integer, ArrayList<String>> chineseFixNumberAreaCodeMap;
    private int configMatchNum = SystemProperties.getInt("ro.config.hwft_MatchNum", 7);
    private int configMatchNumShort;
    private final Map<Integer, List<String>> countryCallingCodeToRegionCodeMap;
    private int countryCodeforCN;
    private String mNetworkOperator;
    private int mSimNumLong;
    private int mSimNumShort;

    public CallerInfoHW() {
        boolean equals;
        int i = 7;
        if (this.configMatchNum >= 7) {
            i = this.configMatchNum;
        }
        this.NUM_LONG_CUST = i;
        this.configMatchNumShort = SystemProperties.getInt("ro.config.hwft_MatchNumShort", this.NUM_LONG_CUST);
        this.NUM_SHORT_CUST = this.configMatchNumShort >= this.NUM_LONG_CUST ? this.NUM_LONG_CUST : this.configMatchNumShort;
        this.mSimNumLong = this.NUM_LONG_CUST;
        this.mSimNumShort = this.NUM_SHORT_CUST;
        if (SystemProperties.get("ro.config.hw_opta", "0").equals("92")) {
            equals = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
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
        boolean ret = false;
        String num1Prefix = null;
        String num2Prefix = null;
        String num1AreaCode = null;
        String num2AreaCode = null;
        int NUM_LONG = this.NUM_LONG_CUST;
        int NUM_SHORT = this.NUM_SHORT_CUST;
        if (num1 == null || num2 == null) {
            return false;
        }
        int numMatch;
        int numMatchShort;
        logd("compareNums, num1 = " + num1 + ", netIso1 = " + netIso1 + ", num2 = " + num2 + ", netIso2 = " + netIso2);
        if (SystemProperties.getInt("ro.config.hwft_MatchNum", 0) == 0) {
            numMatch = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH, 7);
            numMatchShort = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT, numMatch);
            if (numMatch < 7) {
                NUM_LONG = 7;
            } else {
                NUM_LONG = numMatch;
            }
            this.mSimNumLong = NUM_LONG;
            if (numMatchShort >= NUM_LONG) {
                NUM_SHORT = NUM_LONG;
            } else {
                NUM_SHORT = numMatchShort;
            }
            this.mSimNumShort = NUM_SHORT;
            logd("compareNums, after setprop NUM_LONG = " + NUM_LONG + ", NUM_SHORT = " + NUM_SHORT);
        }
        if (num1.indexOf(64) < 0) {
            num1 = PhoneNumberUtils.stripSeparators(num1);
        }
        if (num2.indexOf(64) < 0) {
            num2 = PhoneNumberUtils.stripSeparators(num2);
        }
        num1 = formatedForDualNumber(num1);
        num2 = formatedForDualNumber(num2);
        if (this.IS_CHINA_TELECOM && num1.startsWith("**133") && num1.endsWith("#")) {
            num1 = num1.substring(0, num1.length() - 1);
            logd("compareNums, num1 startsWith **133 && endsWith #");
        }
        if (this.IS_CHINA_TELECOM && num2.startsWith("**133") && num2.endsWith("#")) {
            num2 = num2.substring(0, num2.length() - 1);
            logd("compareNums, num2 startsWith **133 && endsWith #");
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
            num1Prefix = num1.substring(0, countryCodeLen1);
            num1 = num1.substring(countryCodeLen1);
            logd("compareNums, num1 after remove prefix: " + num1 + ", num1Prefix: " + num1Prefix);
        }
        int countryCodeLen2 = getIntlPrefixAndCCLen(num2);
        if (countryCodeLen2 > 0) {
            num2Prefix = num2.substring(0, countryCodeLen2);
            num2 = num2.substring(countryCodeLen2);
            logd("compareNums, num2 after remove prefix: " + num2 + ", num2Prefix: " + num2Prefix);
        }
        if (isRoamingCountryNumberByPrefix(num1Prefix, netIso1) || isRoamingCountryNumberByPrefix(num2Prefix, netIso2)) {
            logd("compareNums, num1 or num2 belong to roaming country");
            numMatch = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_ROAMING, 7);
            numMatchShort = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT_ROAMING, numMatch);
            NUM_LONG = numMatch < 7 ? 7 : numMatch;
            NUM_SHORT = numMatchShort >= NUM_LONG ? NUM_LONG : numMatchShort;
            logd("compareNums, roaming prop NUM_LONG = " + NUM_LONG + ", NUM_SHORT = " + NUM_SHORT);
        }
        if (isEqualCountryCodePrefix(num1Prefix, netIso1, num2Prefix, netIso2)) {
            int areaCodeLen;
            boolean isNum1CnNumber = isChineseNumberByPrefix(num1Prefix, netIso1);
            boolean isNum1CnMPN = false;
            if (isNum1CnNumber) {
                NUM_LONG = 11;
                if (num1 != null && num1.startsWith("86")) {
                    num1 = num1.substring(2);
                }
                num1 = deleteIPHead(num1);
                isNum1CnMPN = isChineseMobilePhoneNumber(num1);
                if (!isNum1CnMPN) {
                    areaCodeLen = getChineseFixNumberAreaCodeLength(num1);
                    if (areaCodeLen > 0) {
                        num1AreaCode = num1.substring(0, areaCodeLen);
                        num1 = num1.substring(areaCodeLen);
                        logd("compareNums, CN num1 after remove area code: " + num1 + ", num1AreaCode: " + num1AreaCode);
                    }
                }
            } else if ("PE".equalsIgnoreCase(netIso1)) {
                logd("compareNums, PE num1 start with 0 not remove it");
            } else if (num1.length() >= 7 && "0".equals(num1.substring(0, 1)) && ("0".equals(num1.substring(1, 2)) ^ 1) != 0) {
                num1 = num1.substring(1);
                logd("compareNums, num1 remove 0 at beginning");
            }
            boolean isNum2CnNumber = isChineseNumberByPrefix(num2Prefix, netIso2);
            int isNum2CnMPN = 0;
            if (isNum2CnNumber) {
                NUM_LONG = 11;
                if (num2 != null && num2.startsWith("86")) {
                    num2 = num2.substring(2);
                }
                num2 = deleteIPHead(num2);
                isNum2CnMPN = isChineseMobilePhoneNumber(num2);
                if (isNum2CnMPN == 0) {
                    areaCodeLen = getChineseFixNumberAreaCodeLength(num2);
                    if (areaCodeLen > 0) {
                        num2AreaCode = num2.substring(0, areaCodeLen);
                        num2 = num2.substring(areaCodeLen);
                        logd("compareNums, CN num2 after remove area code: " + num2 + ", num2AreaCode: " + num2AreaCode);
                    }
                }
            } else if ("PE".equalsIgnoreCase(netIso2)) {
                logd("compareNums, PE num2 start with 0 not remove it");
            } else if (num2.length() >= 7 && "0".equals(num2.substring(0, 1)) && ("0".equals(num2.substring(1, 2)) ^ 1) != 0) {
                num2 = num2.substring(1);
                logd("compareNums, num2 remove 0 at beginning");
            }
            if ((!isNum1CnMPN || (isNum2CnMPN ^ 1) == 0) && (isNum1CnMPN || isNum2CnMPN == 0)) {
                if (isNum1CnMPN && isNum2CnMPN != 0) {
                    logd("compareNums, num1 and num2 are both MPN, continue to compare");
                } else if (isNum1CnNumber && isNum2CnNumber && !isEqualChineseFixNumberAreaCode(num1AreaCode, num2AreaCode)) {
                    logd("compareNums, areacode prefix not same, return false");
                    return false;
                }
                return compareNumsInternal(num1, num2, NUM_LONG, NUM_SHORT);
            }
            if (shouldDoNumberMatchAgainBySimMccmnc(origNum1, netIso1) || shouldDoNumberMatchAgainBySimMccmnc(origNum2, netIso2)) {
                ret = compareNumsInternal(origNum1, origNum2, this.mSimNumLong, this.mSimNumShort);
            }
            logd("compareNums, num1 and num2 not both MPN, return " + ret);
            return ret;
        }
        if (shouldDoNumberMatchAgainBySimMccmnc(origNum1, netIso1) || shouldDoNumberMatchAgainBySimMccmnc(origNum2, netIso2)) {
            ret = compareNumsInternal(origNum1, origNum2, this.mSimNumLong, this.mSimNumShort);
        }
        logd("compareNums, countrycode prefix not same, return " + ret);
        return ret;
    }

    public boolean compareNums(String num1, String num2) {
        int NUM_LONG = this.NUM_LONG_CUST;
        int NUM_SHORT = this.NUM_SHORT_CUST;
        if (num1 == null || num2 == null) {
            return false;
        }
        logd("compareNums, num1 = " + num1 + ", num2 = " + num2);
        if (SystemProperties.getInt("ro.config.hwft_MatchNum", 0) == 0) {
            int numMatch = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH, 7);
            int numMatchShort = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT, numMatch);
            NUM_LONG = numMatch < 7 ? 7 : numMatch;
            NUM_SHORT = numMatchShort >= NUM_LONG ? NUM_LONG : numMatchShort;
            logd("compareNums, after setprop NUM_LONG = " + NUM_LONG + ", NUM_SHORT = " + NUM_SHORT);
        }
        if (num1.indexOf(64) < 0) {
            num1 = PhoneNumberUtils.stripSeparators(num1);
        }
        if (num2.indexOf(64) < 0) {
            num2 = PhoneNumberUtils.stripSeparators(num2);
        }
        num1 = formatedForDualNumber(num1);
        num2 = formatedForDualNumber(num2);
        if (this.IS_CHINA_TELECOM && num1.startsWith("**133") && num1.endsWith("#")) {
            num1 = num1.substring(0, num1.length() - 1);
            logd("compareNums, num1 startsWith **133 && endsWith #");
        }
        if (this.IS_CHINA_TELECOM && num2.startsWith("**133") && num2.endsWith("#")) {
            num2 = num2.substring(0, num2.length() - 1);
            logd("compareNums, num2 startsWith **133 && endsWith #");
        }
        if (NUM_SHORT < NUM_LONG) {
            logd("compareNums, NUM_SHORT have been set! Only do full compare.");
            return num1.equals(num2);
        }
        int num1Len = num1.length();
        int num2Len = num2.length();
        if (num1Len > NUM_LONG) {
            num1 = num1.substring(num1Len - NUM_LONG);
        }
        if (num2Len > NUM_LONG) {
            num2 = num2.substring(num2Len - NUM_LONG);
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

    /* JADX WARNING: Missing block: B:112:0x0512, code:
            return -1;
     */
    /* JADX WARNING: Missing block: B:196:0x08a9, code:
            return -1;
     */
    /* JADX WARNING: Missing block: B:277:0x0c09, code:
            return -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getCallerIndex(Cursor cursor, String compNum, String columnName, String countryIso) {
        String compNumPrefix = null;
        String compNumAreaCode = null;
        String tmpNumFormat = null;
        int numShortID = -1;
        int numLongID = -1;
        int fixedIndex = -1;
        int NUM_LONG = this.NUM_LONG_CUST;
        int NUM_SHORT = this.NUM_SHORT_CUST;
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
            if (IS_SUPPORT_DUAL_NUMBER && -1 == fixedIndex) {
                fixedIndex = getFullMatchIndex(cursor, formatedForDualNumber(PhoneNumberUtils.stripSeparators(compNum)), columnName);
            }
            if (-1 != fixedIndex) {
                return fixedIndex;
            }
            int numMatch;
            int numMatchShort;
            int areaCodeLen;
            logd("getCallerIndex(), not full match proceed to check..");
            logd("getCallerIndex(), NUM_LONG = " + NUM_LONG + ",NUM_SHORT = " + NUM_SHORT);
            if (SystemProperties.getInt("ro.config.hwft_MatchNum", 0) == 0) {
                numMatch = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH, 7);
                numMatchShort = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT, numMatch);
                if (numMatch < 7) {
                    NUM_LONG = 7;
                } else {
                    NUM_LONG = numMatch;
                }
                this.mSimNumLong = NUM_LONG;
                if (numMatchShort >= NUM_LONG) {
                    NUM_SHORT = NUM_LONG;
                } else {
                    NUM_SHORT = numMatchShort;
                }
                this.mSimNumShort = NUM_SHORT;
                logd("getCallerIndex(), after setprop NUM_LONG = " + NUM_LONG + ", NUM_SHORT = " + NUM_SHORT);
            }
            compNum = formatedForDualNumber(PhoneNumberUtils.stripSeparators(compNum));
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
            CharSequence formattedCompNum = null;
            if (!TextUtils.isEmpty(countryIso)) {
                formattedCompNum = PhoneNumberUtils.formatNumberToE164(compNum, countryIso.toUpperCase(Locale.US));
                if (formattedCompNum != null) {
                    logd("formattedCompNum: " + formattedCompNum + ", with countryIso: " + countryIso);
                    compNum = formattedCompNum;
                }
            }
            int countryCodeLen = getIntlPrefixAndCCLen(compNum);
            if (countryCodeLen > 0) {
                compNumPrefix = compNum.substring(0, countryCodeLen);
                compNum = compNum.substring(countryCodeLen);
                logd("compNum after remove prefix: " + compNum + ", compNumLen: " + compNum.length() + ", compNumPrefix: " + compNumPrefix);
            }
            String tmpCompNum = !TextUtils.isEmpty(formattedCompNum) ? formattedCompNum : origCompNum;
            if (isRoamingCountryNumberByPrefix(compNumPrefix, countryIso)) {
                logd("compNum belongs to roaming country");
                numMatch = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_ROAMING, 7);
                numMatchShort = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT_ROAMING, numMatch);
                NUM_LONG = numMatch < 7 ? 7 : numMatch;
                NUM_SHORT = numMatchShort >= NUM_LONG ? NUM_LONG : numMatchShort;
                logd("getCallerIndex(), roaming prop NUM_LONG = " + NUM_LONG + ", NUM_SHORT = " + NUM_SHORT);
            }
            boolean isCnNumber = isChineseNumberByPrefix(compNumPrefix, countryIso);
            boolean isCompNumCnMPN = false;
            if (isCnNumber) {
                NUM_SHORT = 11;
                NUM_LONG = 11;
                if (compNum != null) {
                    if (compNum.startsWith("86")) {
                        compNum = compNum.substring(2);
                    }
                }
                compNum = deleteIPHead(compNum);
                isCompNumCnMPN = isChineseMobilePhoneNumber(compNum);
                if (!isCompNumCnMPN) {
                    areaCodeLen = getChineseFixNumberAreaCodeLength(compNum);
                    if (areaCodeLen > 0) {
                        compNumAreaCode = compNum.substring(0, areaCodeLen);
                        compNum = compNum.substring(areaCodeLen);
                        logd("CN compNum after remove area code: " + compNum + ", compNumLen: " + compNum.length() + ", compNumAreaCode: " + compNumAreaCode);
                    }
                }
            } else if ("PE".equalsIgnoreCase(countryIso)) {
                logd("PE compNum start with 0 not remove it");
            } else if (compNum.length() >= 7 && "0".equals(compNum.substring(0, 1)) && ("0".equals(compNum.substring(1, 2)) ^ 1) != 0) {
                compNum = compNum.substring(1);
            }
            compNumLen = compNum.length();
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
            if (compNumLen >= NUM_LONG) {
                String compNumLong = compNum.substring(compNumLen - NUM_LONG);
                compNumShort = compNum.substring(compNumLen - NUM_SHORT);
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
                                    if (isEqualCountryCodePrefix(compNumPrefix, countryIso, tmpNumPrefix, null)) {
                                        if (isCnNumber) {
                                            tmpNum = deleteIPHead(tmpNum);
                                            isTmpNumCnMPN = isChineseMobilePhoneNumber(tmpNum);
                                            if ((isCompNumCnMPN && (isTmpNumCnMPN ^ 1) != 0) || (!isCompNumCnMPN && isTmpNumCnMPN)) {
                                                logd("11: compNum and tmpNum not both MPN, continue");
                                            } else if (isCompNumCnMPN && isTmpNumCnMPN) {
                                                logd("11: compNum and tmpNum are both MPN, continue to match by mccmnc");
                                            } else {
                                                areaCodeLen = getChineseFixNumberAreaCodeLength(tmpNum);
                                                if (areaCodeLen > 0) {
                                                    tmpNumAreaCode = tmpNum.substring(0, areaCodeLen);
                                                    tmpNum = tmpNum.substring(areaCodeLen);
                                                    logd("11: CN tmpNum after remove area code: " + tmpNum + ", tmpNum.length11: " + tmpNum.length() + ", tmpNumAreaCode: " + tmpNumAreaCode);
                                                }
                                                if (!isEqualChineseFixNumberAreaCode(compNumAreaCode, tmpNumAreaCode)) {
                                                    logd("11: areacode prefix not same, continue");
                                                }
                                            }
                                        } else if (tmpNum.length() >= 7 && "0".equals(tmpNum.substring(0, 1)) && ("0".equals(tmpNum.substring(1, 2)) ^ 1) != 0) {
                                            tmpNum = tmpNum.substring(1);
                                            logd("11: tmpNum remove 0 at beginning");
                                        }
                                        tmpNumLen = tmpNum.length();
                                        if (tmpNumLen >= NUM_LONG) {
                                            String tmpNumLong = tmpNum.substring(tmpNumLen - NUM_LONG);
                                            if (-1 == numLongID && compNumLong.compareTo(tmpNumLong) == 0) {
                                                numLongID = cursor.getPosition();
                                                logd("11: > NUM_LONG numLongID = " + numLongID);
                                            } else {
                                                logd("11: >=NUM_LONG, and !=,  tmpNumLong = " + tmpNumLong + ", numLongID:" + numLongID);
                                            }
                                        } else if (tmpNumLen >= NUM_SHORT) {
                                            tmpNumShort = tmpNum.substring(tmpNumLen - NUM_SHORT);
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
                                    numLongID = cursor.getPosition();
                                    logd("11: > NUM_LONG numLongID = " + numLongID + ", formattedNum full match!");
                                    break;
                                }
                            }
                        }
                        logd("11:  numLongID = " + numLongID + ",numShortID = " + numShortID);
                        if (-1 != numLongID) {
                            fixedIndex = numLongID;
                        } else if (-1 != numShortID) {
                            fixedIndex = numShortID;
                        } else {
                            fixedIndex = -1;
                        }
                    }
                }
            } else if (compNumLen >= NUM_SHORT) {
                compNumShort = compNum.substring(compNumLen - NUM_SHORT);
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
                                    if (isEqualCountryCodePrefix(compNumPrefix, countryIso, tmpNumPrefix, null)) {
                                        if (isCnNumber) {
                                            tmpNum = deleteIPHead(tmpNum);
                                            isTmpNumCnMPN = isChineseMobilePhoneNumber(tmpNum);
                                            if ((isCompNumCnMPN && (isTmpNumCnMPN ^ 1) != 0) || (!isCompNumCnMPN && isTmpNumCnMPN)) {
                                                logd("7: compNum and tmpNum not both MPN, continue");
                                            } else if (isCompNumCnMPN && isTmpNumCnMPN) {
                                                logd("7: compNum and tmpNum are both MPN, continue to match by mccmnc");
                                            } else {
                                                areaCodeLen = getChineseFixNumberAreaCodeLength(tmpNum);
                                                if (areaCodeLen > 0) {
                                                    tmpNumAreaCode = tmpNum.substring(0, areaCodeLen);
                                                    tmpNum = tmpNum.substring(areaCodeLen);
                                                    logd("7: CN tmpNum after remove area code: " + tmpNum + ", tmpNum.length7: " + tmpNum.length() + ", tmpNumAreaCode: " + tmpNumAreaCode);
                                                }
                                                if (!isEqualChineseFixNumberAreaCode(compNumAreaCode, tmpNumAreaCode)) {
                                                    logd("7: areacode prefix not same, continue");
                                                }
                                            }
                                        } else if (tmpNum.length() >= 7 && "0".equals(tmpNum.substring(0, 1)) && ("0".equals(tmpNum.substring(1, 2)) ^ 1) != 0) {
                                            tmpNum = tmpNum.substring(1);
                                            logd("7: tmpNum remove 0 at beginning");
                                        }
                                        tmpNumLen = tmpNum.length();
                                        if (tmpNumLen >= NUM_LONG) {
                                            tmpNumShort = tmpNum.substring(tmpNumLen - NUM_SHORT);
                                            if (-1 == numLongID && compNumShort.compareTo(tmpNumShort) == 0) {
                                                numLongID = cursor.getPosition();
                                            }
                                            logd("7: >=NUM_LONG, tmpNumShort = " + tmpNumShort + ", numLongID:" + numLongID);
                                        } else if (tmpNumLen >= NUM_SHORT) {
                                            tmpNumShort = tmpNum.substring(tmpNumLen - NUM_SHORT);
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
                                    numShortID = cursor.getPosition();
                                    logd("7: >= NUM_SHORT numShortID = " + numShortID + ", formattedNum full match!");
                                    break;
                                }
                            }
                        }
                        logd("7: numShortID = " + numShortID + ",numLongID = " + numLongID);
                        if (-1 != numShortID) {
                            fixedIndex = numShortID;
                        } else if (-1 != numLongID) {
                            fixedIndex = numLongID;
                        } else {
                            fixedIndex = -1;
                        }
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
                                if (isEqualCountryCodePrefix(compNumPrefix, countryIso, tmpNumPrefix, null)) {
                                    if (isCnNumber) {
                                        tmpNum = deleteIPHead(tmpNum);
                                        isTmpNumCnMPN = isChineseMobilePhoneNumber(tmpNum);
                                        if ((isCompNumCnMPN && (isTmpNumCnMPN ^ 1) != 0) || (!isCompNumCnMPN && isTmpNumCnMPN)) {
                                            logd("5: compNum and tmpNum not both MPN, continue");
                                        } else if (isCompNumCnMPN && isTmpNumCnMPN) {
                                            logd("5: compNum and tmpNum are both MPN, continue to match by mccmnc");
                                        } else {
                                            areaCodeLen = getChineseFixNumberAreaCodeLength(tmpNum);
                                            if (areaCodeLen > 0) {
                                                tmpNumAreaCode = tmpNum.substring(0, areaCodeLen);
                                                tmpNum = tmpNum.substring(areaCodeLen);
                                                logd("5: CN tmpNum after remove area code: " + tmpNum + ", tmpNum.length5: " + tmpNum.length() + ", tmpNumAreaCode: " + tmpNumAreaCode);
                                            }
                                            if (!isEqualChineseFixNumberAreaCode(compNumAreaCode, tmpNumAreaCode)) {
                                                logd("5: areacode prefix not same, continue");
                                            }
                                        }
                                    } else if (tmpNum.length() >= 7 && "0".equals(tmpNum.substring(0, 1)) && ("0".equals(tmpNum.substring(1, 2)) ^ 1) != 0) {
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
                                fixedIndex = cursor.getPosition();
                                logd("5: break! numLongID = " + fixedIndex + ", formattedNum full match!");
                                break;
                            }
                        }
                    }
                    logd("5: fixedIndex = " + fixedIndex);
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
        if (TextUtils.isEmpty(number) || number.length() < 11 || !number.substring(number.length() - 11).matches(CN_MPN_PATTERN)) {
            return false;
        }
        logd("isChineseMobilePhoneNumber(), return true for number: " + number);
        return true;
    }

    private int getChineseFixNumberAreaCodeLength(String number) {
        int len = 0;
        String tmpNumber = number;
        if (TextUtils.isEmpty(number) || number.length() < 9) {
            return 0;
        }
        if (!number.startsWith("0")) {
            tmpNumber = "0" + number;
        }
        String top2String = tmpNumber.substring(0, 2);
        String areaCodeString;
        ArrayList<String> areaCodeArray;
        int areaCodeArraySize;
        int i;
        if (top2String.equals(FIXED_NUMBER_TOP2_TOKEN1) || top2String.equals(FIXED_NUMBER_TOP2_TOKEN2)) {
            areaCodeString = tmpNumber.substring(0, 3);
            areaCodeArray = (ArrayList) this.chineseFixNumberAreaCodeMap.get(Integer.valueOf(1));
            areaCodeArraySize = areaCodeArray.size();
            i = 0;
            while (i < areaCodeArraySize) {
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
            areaCodeArraySize = areaCodeArray.size();
            i = 0;
            while (i < areaCodeArraySize) {
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
        if (TextUtils.isEmpty(compNumAreaCode) && (TextUtils.isEmpty(dbNumAreaCode) ^ 1) != 0) {
            return !this.IS_MIIT_NUM_MATCH;
        } else {
            if (!TextUtils.isEmpty(compNumAreaCode) && TextUtils.isEmpty(dbNumAreaCode)) {
                return !this.IS_MIIT_NUM_MATCH;
            } else {
                Object dbNumAreaCode2;
                if (!compNumAreaCode.startsWith("0")) {
                    compNumAreaCode = "0" + compNumAreaCode;
                }
                if (!dbNumAreaCode2.startsWith("0")) {
                    dbNumAreaCode2 = "0" + dbNumAreaCode2;
                }
                return compNumAreaCode.equals(dbNumAreaCode2);
            }
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
        if (TextUtils.isEmpty(num1Prefix) ? TextUtils.isEmpty(num2Prefix) : false) {
            logd("isEqualCountryCodePrefix(), both have no country code, return true");
            return true;
        }
        boolean ret;
        String netIso;
        int countryCode;
        if (TextUtils.isEmpty(num1Prefix) && (TextUtils.isEmpty(num2Prefix) ^ 1) != 0) {
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

    /* JADX WARNING: Removed duplicated region for block: B:128:0x0422 A:{SYNTHETIC, EDGE_INSN: B:128:0x0422->B:111:0x0422 ?: BREAK  } */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x0449 A:{LOOP_END, LOOP:2: B:102:0x03b0->B:117:0x0449} */
    /* JADX WARNING: Missing block: B:36:0x0196, code:
            return -1;
     */
    /* JADX WARNING: Missing block: B:75:0x0300, code:
            return -1;
     */
    /* JADX WARNING: Missing block: B:111:0x0422, code:
            logd("5: fixedIndex = " + r8);
     */
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
        int NUM_LONG = numLong < 7 ? 7 : numLong;
        int NUM_SHORT = numShort >= NUM_LONG ? NUM_LONG : numShort;
        logd("getCallerIndexInternal, after check NUM_LONG: " + NUM_LONG + ", NUM_SHORT: " + NUM_SHORT);
        if (cursor != null) {
            String compNumShort;
            int columnIndex;
            String tmpNum;
            int tmpNumLen;
            String tmpNumShort;
            if (compNumLen >= NUM_LONG) {
                String compNumLong = compNum.substring(compNumLen - NUM_LONG);
                compNumShort = compNum.substring(compNumLen - NUM_SHORT);
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
                                    if (tmpNumLen >= NUM_LONG) {
                                        String tmpNumLong = tmpNum.substring(tmpNumLen - NUM_LONG);
                                        if (-1 == numLongID && compNumLong.compareTo(tmpNumLong) == 0) {
                                            numLongID = cursor.getPosition();
                                            logd("11: > NUM_LONG numLongID = " + numLongID);
                                        }
                                        logd("11: >= NUM_LONG, and !=,  tmpNumLong = " + tmpNumLong + ", numLongID: " + numLongID);
                                    } else if (tmpNumLen >= NUM_SHORT) {
                                        tmpNumShort = tmpNum.substring(tmpNumLen - NUM_SHORT);
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
                                    numLongID = cursor.getPosition();
                                    logd("exact match: break! numLongID = " + numLongID);
                                    break;
                                }
                            }
                        }
                        logd("11: numLongID = " + numLongID + ", numShortID = " + numShortID);
                        fixedIndex = -1 != numLongID ? numLongID : -1 != numShortID ? numShortID : -1;
                    }
                }
            } else if (compNumLen >= NUM_SHORT) {
                compNumShort = compNum.substring(compNumLen - NUM_SHORT);
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
                                    if (tmpNumLen >= NUM_LONG) {
                                        tmpNumShort = tmpNum.substring(tmpNumLen - NUM_SHORT);
                                        if (-1 == numLongID && compNumShort.compareTo(tmpNumShort) == 0) {
                                            numLongID = cursor.getPosition();
                                        }
                                        logd("7: >= NUM_LONG, tmpNumShort = " + tmpNumShort + ", numLongID:" + numLongID);
                                    } else if (tmpNumLen >= NUM_SHORT) {
                                        tmpNumShort = tmpNum.substring(tmpNumLen - NUM_SHORT);
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
                                    numShortID = cursor.getPosition();
                                    logd("exact match numShortID = " + numShortID);
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
                                    fixedIndex = cursor.getPosition();
                                    logd("5: break! numLongID = " + fixedIndex);
                                    break;
                                }
                                if (cursor.moveToNext()) {
                                    break;
                                }
                            } else {
                                logd("5: continue");
                                if (cursor.moveToNext()) {
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
        int NUM_LONG = numLong < 7 ? 7 : numLong;
        int NUM_SHORT = numShort >= NUM_LONG ? NUM_LONG : numShort;
        logd("compareNumsInternal, after check NUM_LONG: " + NUM_LONG + ", NUM_SHORT: " + NUM_SHORT);
        String num1Short;
        if (num1Len >= NUM_LONG) {
            String num1Long = num1.substring(num1Len - NUM_LONG);
            num1Short = num1.substring(num1Len - NUM_SHORT);
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
            num1Short = num1.substring(num1Len - NUM_SHORT);
            logd("compareNumsInternal, 7: num1Short = " + num1Short);
            if (num2Len >= NUM_SHORT && num1Short.compareTo(num2.substring(num2Len - NUM_SHORT)) == 0) {
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
            if (!(TextUtils.isEmpty(numberPrefix) || (TextUtils.isEmpty(netIso) ^ 1) == 0)) {
                return numberPrefix.equals(Integer.toString(sInstance.getCountryCodeForRegion(netIso.toUpperCase(Locale.US))));
            }
        }
        return false;
    }

    private boolean isValidData4Number(String data1Num, String data4Num) {
        logd("isValidData4Number, data1Num: " + data1Num + ", data4Num: " + data4Num);
        if (!(TextUtils.isEmpty(data1Num) || (TextUtils.isEmpty(data4Num) ^ 1) == 0 || !data4Num.startsWith("+"))) {
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
        String sMcc = " ";
        if (this.mNetworkOperator != null && this.mNetworkOperator.length() > 3) {
            sMcc = this.mNetworkOperator.substring(0, 3);
        }
        if (number.startsWith("011")) {
            for (Object equals : NORMAL_PREFIX_MCC) {
                if (sMcc.equals(equals) && number.length() == 11) {
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
            compNum = compNum.substring(5, compNum.length());
        } else if (compNum.startsWith("*23#")) {
            compNum = compNum.substring(4, compNum.length());
        }
        return compNum;
    }

    private static boolean isVirtualNum(String dialString) {
        if (!dialString.endsWith("#")) {
            return false;
        }
        String tempstring = dialString.substring(0, dialString.length() - 1).replace(" ", "").replace("+", "").replace("-", "");
        if (tempstring.startsWith("*230#")) {
            tempstring = tempstring.substring(5, tempstring.length());
        } else if (tempstring.startsWith("*23#")) {
            tempstring = tempstring.substring(4, tempstring.length());
        }
        if (tempstring.matches("[0-9]+")) {
            return true;
        }
        return false;
    }
}
