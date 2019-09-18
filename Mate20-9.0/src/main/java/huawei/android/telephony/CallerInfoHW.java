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
    private static final String[] INTERNATIONAL_PREFIX = {"+00", "+", "00"};
    private static final String[] IPHEAD = {"10193", "11808", "12593", "17900", "17901", "17908", "17909", "17910", "17911", "17931", "17950", "17951", "17960", "17968", "17969", "96435"};
    private static final int IPHEAD_LENTH = 5;
    private static final boolean IS_SUPPORT_DUAL_NUMBER = SystemProperties.getBoolean("ro.config.hw_dual_number", false);
    public static final int MIN_MATCH = 7;
    private static final String[] NORMAL_PREFIX_MCC = {"602", "722"};
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
        int i = 7;
        this.NUM_LONG_CUST = this.configMatchNum >= 7 ? this.configMatchNum : i;
        this.configMatchNumShort = SystemProperties.getInt("ro.config.hwft_MatchNumShort", this.NUM_LONG_CUST);
        this.NUM_SHORT_CUST = this.configMatchNumShort >= this.NUM_LONG_CUST ? this.NUM_LONG_CUST : this.configMatchNumShort;
        this.mSimNumLong = this.NUM_LONG_CUST;
        this.mSimNumShort = this.NUM_SHORT_CUST;
        this.IS_CHINA_TELECOM = SystemProperties.get("ro.config.hw_opta", "0").equals("92") && SystemProperties.get("ro.config.hw_optb", "0").equals("156");
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
        boolean ret;
        boolean ret2;
        boolean isNum1CnMPN;
        boolean isNum2CnMPN;
        boolean ret3;
        String str;
        boolean isNum2CnMPN2;
        String str2;
        boolean isNum1CnMPN2;
        String num12 = num1;
        String str3 = netIso1;
        String num22 = num2;
        String str4 = netIso2;
        String num1Prefix = null;
        String num2Prefix = null;
        String num1AreaCode = null;
        String num2AreaCode = null;
        int NUM_LONG = this.NUM_LONG_CUST;
        int NUM_SHORT = this.NUM_SHORT_CUST;
        if (num12 != null) {
            if (num22 != null) {
                logd("compareNums, num1 = " + num12 + ", netIso1 = " + str3 + ", num2 = " + num22 + ", netIso2 = " + str4);
                if (SystemProperties.getInt("ro.config.hwft_MatchNum", 0) == 0) {
                    int numMatch = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH, 7);
                    int numMatchShort = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT, numMatch);
                    ret = false;
                    int i = numMatch < 7 ? 7 : numMatch;
                    this.mSimNumLong = i;
                    NUM_LONG = i;
                    int i2 = numMatchShort >= NUM_LONG ? NUM_LONG : numMatchShort;
                    this.mSimNumShort = i2;
                    NUM_SHORT = i2;
                    StringBuilder sb = new StringBuilder();
                    int i3 = numMatch;
                    sb.append("compareNums, after setprop NUM_LONG = ");
                    sb.append(NUM_LONG);
                    sb.append(", NUM_SHORT = ");
                    sb.append(NUM_SHORT);
                    logd(sb.toString());
                } else {
                    ret = false;
                }
                if (num12.indexOf(64) < 0) {
                    num12 = PhoneNumberUtils.stripSeparators(num1);
                }
                if (num22.indexOf(64) < 0) {
                    num22 = PhoneNumberUtils.stripSeparators(num2);
                }
                String num13 = formatedForDualNumber(num12);
                String num23 = formatedForDualNumber(num22);
                if (this.IS_CHINA_TELECOM && num13.startsWith("**133") && num13.endsWith("#")) {
                    num13 = num13.substring(0, num13.length() - 1);
                    logd("compareNums, num1 startsWith **133 && endsWith #");
                }
                if (this.IS_CHINA_TELECOM && num23.startsWith("**133") && num23.endsWith("#")) {
                    num23 = num23.substring(0, num23.length() - 1);
                    logd("compareNums, num2 startsWith **133 && endsWith #");
                }
                if (num13.equals(num23)) {
                    logd("compareNums, full compare returns true.");
                    return true;
                }
                String origNum1 = num13;
                String origNum2 = num23;
                if (!TextUtils.isEmpty(netIso1)) {
                    String formattedNum1 = PhoneNumberUtils.formatNumberToE164(num13, str3.toUpperCase(Locale.US));
                    if (formattedNum1 != null) {
                        logd("compareNums, formattedNum1: " + formattedNum1 + ", with netIso1: " + str3);
                        num13 = formattedNum1;
                    }
                }
                if (!TextUtils.isEmpty(netIso2)) {
                    String formattedNum2 = PhoneNumberUtils.formatNumberToE164(num23, str4.toUpperCase(Locale.US));
                    if (formattedNum2 != null) {
                        logd("compareNums, formattedNum2: " + formattedNum2 + ", with netIso2: " + str4);
                        num23 = formattedNum2;
                    }
                }
                if (num13.equals(num23)) {
                    logd("compareNums, full compare for formatted number returns true.");
                    return true;
                }
                int countryCodeLen1 = getIntlPrefixAndCCLen(num13);
                if (countryCodeLen1 > 0) {
                    num1Prefix = num13.substring(0, countryCodeLen1);
                    num13 = num13.substring(countryCodeLen1);
                    logd("compareNums, num1 after remove prefix: " + num13 + ", num1Prefix: " + num1Prefix);
                }
                int countryCodeLen2 = getIntlPrefixAndCCLen(num23);
                if (countryCodeLen2 > 0) {
                    num2Prefix = num23.substring(0, countryCodeLen2);
                    num23 = num23.substring(countryCodeLen2);
                    StringBuilder sb2 = new StringBuilder();
                    int i4 = countryCodeLen1;
                    sb2.append("compareNums, num2 after remove prefix: ");
                    sb2.append(num23);
                    sb2.append(", num2Prefix: ");
                    sb2.append(num2Prefix);
                    logd(sb2.toString());
                }
                if (isRoamingCountryNumberByPrefix(num1Prefix, str3) != 0 || isRoamingCountryNumberByPrefix(num2Prefix, str4)) {
                    logd("compareNums, num1 or num2 belong to roaming country");
                    int numMatch2 = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_ROAMING, 7);
                    int numMatchShort2 = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT_ROAMING, numMatch2);
                    int i5 = countryCodeLen2;
                    NUM_LONG = numMatch2 < 7 ? 7 : numMatch2;
                    NUM_SHORT = numMatchShort2 >= NUM_LONG ? NUM_LONG : numMatchShort2;
                    StringBuilder sb3 = new StringBuilder();
                    int i6 = numMatch2;
                    sb3.append("compareNums, roaming prop NUM_LONG = ");
                    sb3.append(NUM_LONG);
                    sb3.append(", NUM_SHORT = ");
                    sb3.append(NUM_SHORT);
                    logd(sb3.toString());
                } else {
                    int i7 = countryCodeLen2;
                }
                if (isEqualCountryCodePrefix(num1Prefix, str3, num2Prefix, str4)) {
                    boolean isNum1CnNumber = isChineseNumberByPrefix(num1Prefix, str3);
                    if (isNum1CnNumber) {
                        NUM_LONG = 11;
                        num13 = deleteIPHead(num13);
                        boolean isNum1CnMPN3 = isChineseMobilePhoneNumber(num13);
                        if (!isNum1CnMPN3) {
                            int areaCodeLen = getChineseFixNumberAreaCodeLength(num13);
                            if (areaCodeLen > 0) {
                                isNum1CnMPN = isNum1CnMPN3;
                                num1AreaCode = num13.substring(0, areaCodeLen);
                                num13 = num13.substring(areaCodeLen);
                                StringBuilder sb4 = new StringBuilder();
                                int i8 = areaCodeLen;
                                sb4.append("compareNums, CN num1 after remove area code: ");
                                sb4.append(num13);
                                sb4.append(", num1AreaCode: ");
                                sb4.append(num1AreaCode);
                                logd(sb4.toString());
                            } else {
                                isNum1CnMPN = isNum1CnMPN3;
                            }
                            String str5 = num1Prefix;
                        } else {
                            isNum1CnMPN = isNum1CnMPN3;
                            String str6 = num1Prefix;
                        }
                    } else {
                        if ("PE".equalsIgnoreCase(str3)) {
                            logd("compareNums, PE num1 start with 0 not remove it");
                            isNum1CnMPN2 = false;
                            String str7 = num1Prefix;
                            str2 = null;
                        } else {
                            isNum1CnMPN2 = false;
                            if (num13.length() >= 7) {
                                String str8 = num1Prefix;
                                str2 = null;
                                if ("0".equals(num13.substring(0, 1)) && !"0".equals(num13.substring(1, 2))) {
                                    num13 = num13.substring(1);
                                    logd("compareNums, num1 remove 0 at beginning");
                                    isNum1CnMPN = false;
                                    num1AreaCode = null;
                                }
                            } else {
                                str2 = null;
                            }
                        }
                        isNum1CnMPN = isNum1CnMPN2;
                        num1AreaCode = str2;
                    }
                    boolean isNum2CnNumber = isChineseNumberByPrefix(num2Prefix, str4);
                    if (isNum2CnNumber) {
                        NUM_LONG = 11;
                        num23 = deleteIPHead(num23);
                        boolean isNum2CnMPN3 = isChineseMobilePhoneNumber(num23);
                        if (!isNum2CnMPN3) {
                            int areaCodeLen2 = getChineseFixNumberAreaCodeLength(num23);
                            if (areaCodeLen2 > 0) {
                                isNum2CnMPN = isNum2CnMPN3;
                                num2AreaCode = num23.substring(0, areaCodeLen2);
                                num23 = num23.substring(areaCodeLen2);
                                StringBuilder sb5 = new StringBuilder();
                                int i9 = areaCodeLen2;
                                sb5.append("compareNums, CN num2 after remove area code: ");
                                sb5.append(num23);
                                sb5.append(", num2AreaCode: ");
                                sb5.append(num2AreaCode);
                                logd(sb5.toString());
                            } else {
                                isNum2CnMPN = isNum2CnMPN3;
                            }
                            String str9 = num2Prefix;
                        } else {
                            isNum2CnMPN = isNum2CnMPN3;
                            String str10 = num2Prefix;
                        }
                    } else {
                        if ("PE".equalsIgnoreCase(str4)) {
                            logd("compareNums, PE num2 start with 0 not remove it");
                            isNum2CnMPN2 = false;
                            String str11 = num2Prefix;
                            str = null;
                        } else {
                            isNum2CnMPN2 = false;
                            if (num23.length() >= 7) {
                                String str12 = num2Prefix;
                                str = null;
                                if ("0".equals(num23.substring(0, 1)) && !"0".equals(num23.substring(1, 2))) {
                                    num23 = num23.substring(1);
                                    logd("compareNums, num2 remove 0 at beginning");
                                    isNum2CnMPN = false;
                                    num2AreaCode = null;
                                }
                            } else {
                                str = null;
                            }
                        }
                        isNum2CnMPN = isNum2CnMPN2;
                        num2AreaCode = str;
                    }
                    if ((!isNum1CnMPN || isNum2CnMPN) && (isNum1CnMPN || !isNum2CnMPN)) {
                        if (isNum1CnMPN && isNum2CnMPN) {
                            logd("compareNums, num1 and num2 are both MPN, continue to compare");
                        } else if (isNum1CnNumber && isNum2CnNumber && !isEqualChineseFixNumberAreaCode(num1AreaCode, num2AreaCode)) {
                            logd("compareNums, areacode prefix not same, return false");
                            return false;
                        }
                        return compareNumsInternal(num13, num23, NUM_LONG, NUM_SHORT);
                    }
                    if (shouldDoNumberMatchAgainBySimMccmnc(origNum1, str3) || shouldDoNumberMatchAgainBySimMccmnc(origNum2, str4)) {
                        ret3 = compareNumsInternal(origNum1, origNum2, this.mSimNumLong, this.mSimNumShort);
                    } else {
                        ret3 = ret;
                    }
                    logd("compareNums, num1 and num2 not both MPN, return " + ret3);
                    return ret3;
                }
                String str13 = num2Prefix;
                if (shouldDoNumberMatchAgainBySimMccmnc(origNum1, str3) || shouldDoNumberMatchAgainBySimMccmnc(origNum2, str4)) {
                    ret2 = compareNumsInternal(origNum1, origNum2, this.mSimNumLong, this.mSimNumShort);
                } else {
                    ret2 = ret;
                }
                logd("compareNums, countrycode prefix not same, return " + ret2);
                return ret2;
            }
        }
        return false;
    }

    public boolean compareNums(String num1, String num2) {
        int NUM_LONG = this.NUM_LONG_CUST;
        int NUM_SHORT = this.NUM_SHORT_CUST;
        if (num1 == null || num2 == null) {
            return false;
        }
        logd("compareNums, num1 = " + num1 + ", num2 = " + num2);
        if (SystemProperties.getInt("ro.config.hwft_MatchNum", 0) == 0) {
            int i = 7;
            int numMatch = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH, 7);
            int numMatchShort = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT, numMatch);
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

    public int getCallerIndex(Cursor cursor, String compNum) {
        return getCallerIndex(cursor, compNum, "number");
    }

    public int getCallerIndex(Cursor cursor, String compNum, String columnName) {
        return getCallerIndex(cursor, compNum, columnName, SystemProperties.get(HwTelephonyProperties.PROPERTY_NETWORK_COUNTRY_ISO, ""));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:179:0x063f, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:266:0x095d, code lost:
        logd("7: numShortID = " + r1 + ",numLongID = " + r15);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:267:0x097a, code lost:
        if (-1 == r1) goto L_0x098a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:268:0x097c, code lost:
        r5 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:269:0x097d, code lost:
        r16 = r1;
        r17 = r15;
        r12 = r41;
        r14 = r42;
        r15 = r70;
        r1 = r72;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:270:0x098a, code lost:
        if (-1 == r15) goto L_0x098e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:271:0x098c, code lost:
        r5 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:272:0x098e, code lost:
        r5 = -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:359:0x0ca0, code lost:
        return -1;
     */
    /* JADX WARNING: Removed duplicated region for block: B:147:0x053b  */
    /* JADX WARNING: Removed duplicated region for block: B:154:0x0587  */
    /* JADX WARNING: Removed duplicated region for block: B:176:0x061a A[LOOP:0: B:91:0x034a->B:176:0x061a, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:183:0x0675  */
    /* JADX WARNING: Removed duplicated region for block: B:273:0x0990 A[LOOP:1: B:190:0x06e4->B:273:0x0990, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:344:0x0c0d  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0c36  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c7d A[LOOP:2: B:285:0x0a2e->B:357:0x0c7d, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:365:0x0ccf  */
    /* JADX WARNING: Removed duplicated region for block: B:369:0x0d01  */
    /* JADX WARNING: Removed duplicated region for block: B:376:0x05e4 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:379:0x0958 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:385:0x0c58 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x017e  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x01b4  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x01be  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x01c0  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x01c8  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0214  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x021f  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0280  */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x02d7  */
    public int getCallerIndex(Cursor cursor, String compNum, String columnName, String countryIso) {
        int fixedIndex;
        String formattedCompNum;
        int countryCodeLen;
        int countryCodeLen2;
        int NUM_SHORT;
        String formattedCompNum2;
        boolean isCnNumber;
        String origTmpNum;
        boolean isCompNumCnMPN;
        int NUM_LONG;
        String origTmpNum2;
        int NUM_SHORT2;
        String compNumAreaCode;
        int compNumLen;
        String compNumAreaCode2;
        String tmpCompNum;
        String origCompNum;
        int fixedIndex2;
        int compNumLen2;
        String compNum2;
        String tmpCompNum2;
        String compNumAreaCode3;
        String compNum3;
        int compNumLen3;
        String origTmpNum3;
        String tmpNum;
        String tmpNumFormat;
        String tmpNumFormat2;
        int fixedIndex3;
        int countryCodeLen3;
        String tmpNumPrefix;
        String tmpCompNum3;
        String tmpNum2;
        int tmpNumLen;
        String tmpNum3;
        String origTmpNum4;
        int compNumLen4;
        String tmpNumFormat3;
        String tmpNumAreaCode;
        String tmpNumPrefix2;
        String tmpNum4;
        int fixedIndex4;
        int countryCodeLen4;
        int formatColumnIndex;
        String tmpNumFormat4;
        int numShortID;
        String tmpNumFormat5;
        int countryCodeLen5;
        String tmpNumAreaCode2;
        String tmpNumAreaCode3;
        int data4ColumnIndex;
        String tmpNumPrefix3;
        String tmpNum5;
        String tmpNumShort;
        String tmpNumPrefix4;
        String tmpNum6;
        String compNumLong;
        int compNumLen5;
        String tmpNumFormat6;
        String tmpNum7;
        String origTmpNum5;
        int countryCodeLen6;
        int fixedIndex5;
        int i;
        String tmpNumFormat7;
        String origTmpNum6;
        int numLongID;
        String tmpNumFormat8;
        int countryCodeLen7;
        int countryCodeLen8;
        String tmpNumAreaCode4;
        String tmpNumFormat9;
        int formatColumnIndex2;
        String tmpNumPrefix5;
        String tmpNum8;
        String compNumLong2;
        int tmpNumLen2;
        String tmpNumAreaCode5;
        String tmpNumAreaCode6;
        String tmpNum9;
        boolean isCompNumCnMPN2;
        Cursor cursor2 = cursor;
        String compNum4 = columnName;
        String str = countryIso;
        String compNumPrefix = null;
        String compNumAreaCode4 = null;
        int fixedIndex6 = -1;
        int NUM_LONG2 = this.NUM_LONG_CUST;
        int NUM_SHORT3 = this.NUM_SHORT_CUST;
        if (TextUtils.isEmpty(compNum)) {
            if (cursor2 != null && cursor.getCount() > 0) {
                fixedIndex6 = 0;
            }
            Log.e(TAG, "CallerInfoHW(),null == compNum! fixedIndex = " + fixedIndex6);
            return fixedIndex6;
        }
        if (cursor2 != null) {
            if (cursor.getCount() > 0) {
                int fixedIndex7 = getFullMatchIndex(cursor, compNum, columnName);
                if (IS_SUPPORT_DUAL_NUMBER && -1 == fixedIndex7) {
                    fixedIndex7 = getFullMatchIndex(cursor2, formatedForDualNumber(PhoneNumberUtils.stripSeparators(compNum)), compNum4);
                }
                if (-1 != fixedIndex7) {
                    return fixedIndex7;
                }
                logd("getCallerIndex(), not full match proceed to check..");
                logd("getCallerIndex(), NUM_LONG = " + NUM_LONG2 + ",NUM_SHORT = " + NUM_SHORT3);
                int i2 = 7;
                if (SystemProperties.getInt("ro.config.hwft_MatchNum", 0) == 0) {
                    int numMatch = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH, 7);
                    int numMatchShort = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT, numMatch);
                    if (numMatch >= 7) {
                        i2 = numMatch;
                    }
                    this.mSimNumLong = i2;
                    NUM_LONG2 = i2;
                    int i3 = numMatchShort >= NUM_LONG2 ? NUM_LONG2 : numMatchShort;
                    this.mSimNumShort = i3;
                    NUM_SHORT3 = i3;
                    StringBuilder sb = new StringBuilder();
                    fixedIndex = fixedIndex7;
                    sb.append("getCallerIndex(), after setprop NUM_LONG = ");
                    sb.append(NUM_LONG2);
                    sb.append(", NUM_SHORT = ");
                    sb.append(NUM_SHORT3);
                    logd(sb.toString());
                } else {
                    fixedIndex = fixedIndex7;
                }
                String compNum5 = formatedForDualNumber(PhoneNumberUtils.stripSeparators(compNum));
                int compNumLen6 = compNum5.length();
                logd("compNum: " + compNum5 + ", countryIso: " + str);
                if (this.IS_CHINA_TELECOM && compNum5.startsWith("**133") && compNum5.endsWith("#")) {
                    compNum5 = compNum5.substring(0, compNum5.length() - 1);
                    logd("compNum startsWith **133 && endsWith #");
                }
                String origCompNum2 = compNum5;
                int NUM_LONG3 = NUM_LONG2;
                this.mNetworkOperator = SystemProperties.get(HwTelephonyProperties.PROPERTY_NETWORK_OPERATOR, "");
                String formattedCompNum3 = null;
                if (!TextUtils.isEmpty(countryIso)) {
                    formattedCompNum3 = PhoneNumberUtils.formatNumberToE164(compNum5, str.toUpperCase(Locale.US));
                    if (formattedCompNum3 != null) {
                        StringBuilder sb2 = new StringBuilder();
                        String str2 = compNum5;
                        sb2.append("formattedCompNum: ");
                        sb2.append(formattedCompNum3);
                        sb2.append(", with countryIso: ");
                        sb2.append(str);
                        logd(sb2.toString());
                        compNum5 = formattedCompNum3;
                        formattedCompNum = formattedCompNum3;
                        countryCodeLen = getIntlPrefixAndCCLen(compNum5);
                        if (countryCodeLen <= 0) {
                            NUM_SHORT = NUM_SHORT3;
                            compNumPrefix = compNum5.substring(0, countryCodeLen);
                            compNum5 = compNum5.substring(countryCodeLen);
                            StringBuilder sb3 = new StringBuilder();
                            countryCodeLen2 = countryCodeLen;
                            sb3.append("compNum after remove prefix: ");
                            sb3.append(compNum5);
                            sb3.append(", compNumLen: ");
                            sb3.append(compNum5.length());
                            sb3.append(", compNumPrefix: ");
                            sb3.append(compNumPrefix);
                            logd(sb3.toString());
                        } else {
                            countryCodeLen2 = countryCodeLen;
                            NUM_SHORT = NUM_SHORT3;
                        }
                        String tmpCompNum4 = TextUtils.isEmpty(formattedCompNum) != 0 ? formattedCompNum : origCompNum2;
                        if (!isRoamingCountryNumberByPrefix(compNumPrefix, str)) {
                            logd("compNum belongs to roaming country");
                            int i4 = compNumLen6;
                            int numMatch2 = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_ROAMING, 7);
                            int numMatchShort2 = SystemProperties.getInt(HwTelephonyProperties.PROPERTY_GLOBAL_VERSION_NUM_MATCH_SHORT_ROAMING, numMatch2);
                            formattedCompNum2 = formattedCompNum;
                            int NUM_LONG4 = numMatch2 < 7 ? 7 : numMatch2;
                            int NUM_SHORT4 = numMatchShort2 >= NUM_LONG4 ? NUM_LONG4 : numMatchShort2;
                            int i5 = numMatch2;
                            StringBuilder sb4 = new StringBuilder();
                            int i6 = numMatchShort2;
                            sb4.append("getCallerIndex(), roaming prop NUM_LONG = ");
                            sb4.append(NUM_LONG4);
                            sb4.append(", NUM_SHORT = ");
                            int NUM_SHORT5 = NUM_SHORT4;
                            sb4.append(NUM_SHORT5);
                            logd(sb4.toString());
                            NUM_SHORT = NUM_SHORT5;
                            NUM_LONG3 = NUM_LONG4;
                        } else {
                            formattedCompNum2 = formattedCompNum;
                        }
                        isCnNumber = isChineseNumberByPrefix(compNumPrefix, str);
                        if (!isCnNumber) {
                            String compNum6 = deleteIPHead(compNum5);
                            boolean isCompNumCnMPN3 = isChineseMobilePhoneNumber(compNum6);
                            if (!isCompNumCnMPN3) {
                                int areaCodeLen = getChineseFixNumberAreaCodeLength(compNum6);
                                if (areaCodeLen > 0) {
                                    compNumAreaCode4 = compNum6.substring(0, areaCodeLen);
                                    compNum6 = compNum6.substring(areaCodeLen);
                                    StringBuilder sb5 = new StringBuilder();
                                    isCompNumCnMPN = isCompNumCnMPN3;
                                    sb5.append("CN compNum after remove area code: ");
                                    sb5.append(compNum6);
                                    sb5.append(", compNumLen: ");
                                    sb5.append(compNum6.length());
                                    sb5.append(", compNumAreaCode: ");
                                    sb5.append(compNumAreaCode4);
                                    logd(sb5.toString());
                                } else {
                                    isCompNumCnMPN = isCompNumCnMPN3;
                                }
                                origTmpNum = null;
                                compNumAreaCode = compNumAreaCode4;
                                NUM_LONG = 11;
                                NUM_SHORT2 = 11;
                                origTmpNum2 = compNum6;
                            } else {
                                isCompNumCnMPN = isCompNumCnMPN3;
                                origTmpNum = null;
                                compNumAreaCode = null;
                                NUM_LONG = 11;
                                NUM_SHORT2 = 11;
                                origTmpNum2 = compNum6;
                            }
                        } else {
                            if ("PE".equalsIgnoreCase(str)) {
                                logd("PE compNum start with 0 not remove it");
                                isCompNumCnMPN2 = false;
                                origTmpNum = null;
                            } else if (compNum5.length() >= 7) {
                                isCompNumCnMPN2 = false;
                                origTmpNum = null;
                                if ("0".equals(compNum5.substring(0, 1)) && !"0".equals(compNum5.substring(1, 2))) {
                                    origTmpNum2 = compNum5.substring(1);
                                    compNumAreaCode = null;
                                    NUM_LONG = NUM_LONG3;
                                    NUM_SHORT2 = NUM_SHORT;
                                    isCompNumCnMPN = false;
                                }
                            } else {
                                isCompNumCnMPN2 = false;
                                origTmpNum = null;
                            }
                            origTmpNum2 = compNum5;
                            compNumAreaCode = null;
                            NUM_LONG = NUM_LONG3;
                            NUM_SHORT2 = NUM_SHORT;
                            isCompNumCnMPN = isCompNumCnMPN2;
                        }
                        compNumLen = origTmpNum2.length();
                        if (compNumLen < NUM_LONG) {
                            String compNumLong3 = origTmpNum2.substring(compNumLen - NUM_LONG);
                            String tmpNumAreaCode7 = origTmpNum2.substring(compNumLen - NUM_SHORT2);
                            StringBuilder sb6 = new StringBuilder();
                            String tmpNumFormat10 = null;
                            sb6.append("11:, compNumLong = ");
                            sb6.append(compNumLong3);
                            sb6.append(",compNumShort = ");
                            sb6.append(tmpNumAreaCode7);
                            logd(sb6.toString());
                            if (cursor.moveToFirst()) {
                                int columnIndex = cursor2.getColumnIndex(compNum4);
                                int formatColumnIndex3 = cursor2.getColumnIndex("normalized_number");
                                origCompNum = origCompNum2;
                                int data4ColumnIndex2 = cursor2.getColumnIndex("data4");
                                StringBuilder sb7 = new StringBuilder();
                                String compNum7 = origTmpNum2;
                                sb7.append("11: columnIndex: ");
                                sb7.append(columnIndex);
                                sb7.append(", formatColumnIndex: ");
                                sb7.append(formatColumnIndex3);
                                sb7.append(", data4ColumnIndex: ");
                                sb7.append(data4ColumnIndex2);
                                logd(sb7.toString());
                                if (columnIndex != -1) {
                                    int numShortID2 = -1;
                                    int numLongID2 = -1;
                                    while (true) {
                                        int columnIndex2 = columnIndex;
                                        String origTmpNum7 = cursor2.getString(columnIndex);
                                        if (origTmpNum7 == null) {
                                            int i7 = compNumLen;
                                            int i8 = data4ColumnIndex2;
                                            int i9 = formatColumnIndex3;
                                            i = -1;
                                            break;
                                        }
                                        compNumLen5 = compNumLen;
                                        if (origTmpNum7.indexOf(64) >= 0) {
                                            int i10 = data4ColumnIndex2;
                                            int i11 = formatColumnIndex3;
                                            i = -1;
                                            break;
                                        }
                                        String origTmpNum8 = PhoneNumberUtils.stripSeparators(origTmpNum7);
                                        StringBuilder sb8 = new StringBuilder();
                                        String compNumShort = tmpNumAreaCode7;
                                        sb8.append("origTmpNum: ");
                                        sb8.append(origTmpNum8);
                                        logd(sb8.toString());
                                        if (-1 != formatColumnIndex3) {
                                            tmpNumFormat7 = cursor2.getString(formatColumnIndex3);
                                            tmpNum7 = isValidData4Number(origTmpNum8, tmpNumFormat7) ? tmpNumFormat7 : origTmpNum8;
                                        } else if (-1 != data4ColumnIndex2) {
                                            tmpNumFormat7 = cursor2.getString(data4ColumnIndex2);
                                            tmpNum7 = isValidData4Number(origTmpNum8, tmpNumFormat7) ? tmpNumFormat7 : origTmpNum8;
                                        } else {
                                            tmpNum7 = origTmpNum8;
                                            tmpNumFormat7 = tmpNumFormat10;
                                        }
                                        origTmpNum6 = origTmpNum8;
                                        StringBuilder sb9 = new StringBuilder();
                                        int data4ColumnIndex3 = data4ColumnIndex2;
                                        sb9.append("11: tmpNumFormat: ");
                                        sb9.append(tmpNumFormat7);
                                        logd(sb9.toString());
                                        int tmpNumLen3 = tmpNum7.length();
                                        StringBuilder sb10 = new StringBuilder();
                                        int i12 = tmpNumLen3;
                                        sb10.append("11: tmpNum = ");
                                        sb10.append(tmpNum7);
                                        sb10.append(", tmpNum.length11: ");
                                        sb10.append(tmpNum7.length());
                                        sb10.append(",ID = ");
                                        sb10.append(cursor.getPosition());
                                        logd(sb10.toString());
                                        if (tmpNum7.equals(tmpCompNum4)) {
                                            logd("11: > NUM_LONG numLongID = " + numLongID + ", formattedNum full match!");
                                            compNumLong = compNumLong3;
                                            numLongID2 = numLongID;
                                            tmpNumFormat8 = tmpNumFormat7;
                                            int i13 = formatColumnIndex3;
                                            countryCodeLen7 = countryCodeLen2;
                                            String tmpNumFormat11 = compNumShort;
                                            break;
                                        }
                                        int countryCodeLen9 = getIntlPrefixAndCCLen(tmpNum7);
                                        if (countryCodeLen9 > 0) {
                                            tmpNumAreaCode4 = null;
                                            tmpNumFormat8 = tmpNumFormat7;
                                            tmpNumFormat9 = tmpNum7.substring(0, countryCodeLen9);
                                            tmpNum7 = tmpNum7.substring(countryCodeLen9);
                                            StringBuilder sb11 = new StringBuilder();
                                            countryCodeLen7 = countryCodeLen9;
                                            sb11.append("11: tmpNum after remove prefix: ");
                                            sb11.append(tmpNum7);
                                            sb11.append(", tmpNum.length11: ");
                                            sb11.append(tmpNum7.length());
                                            sb11.append(", tmpNumPrefix: ");
                                            sb11.append(tmpNumFormat9);
                                            logd(sb11.toString());
                                        } else {
                                            countryCodeLen7 = countryCodeLen9;
                                            tmpNumAreaCode4 = null;
                                            tmpNumFormat8 = tmpNumFormat7;
                                            tmpNumFormat9 = null;
                                        }
                                        if (isEqualCountryCodePrefix(compNumPrefix, str, tmpNumFormat9, null)) {
                                            if (isCnNumber) {
                                                String tmpNum10 = deleteIPHead(tmpNum7);
                                                boolean isTmpNumCnMPN = isChineseMobilePhoneNumber(tmpNum10);
                                                if ((!isCompNumCnMPN || isTmpNumCnMPN) && (isCompNumCnMPN || !isTmpNumCnMPN)) {
                                                    if (!isCompNumCnMPN || !isTmpNumCnMPN) {
                                                        int areaCodeLen2 = getChineseFixNumberAreaCodeLength(tmpNum10);
                                                        if (areaCodeLen2 > 0) {
                                                            boolean z = isTmpNumCnMPN;
                                                            tmpNumPrefix5 = tmpNumFormat9;
                                                            tmpNumAreaCode6 = tmpNum10.substring(0, areaCodeLen2);
                                                            tmpNum10 = tmpNum10.substring(areaCodeLen2);
                                                            StringBuilder sb12 = new StringBuilder();
                                                            int i14 = areaCodeLen2;
                                                            sb12.append("11: CN tmpNum after remove area code: ");
                                                            sb12.append(tmpNum10);
                                                            sb12.append(", tmpNum.length11: ");
                                                            sb12.append(tmpNum10.length());
                                                            sb12.append(", tmpNumAreaCode: ");
                                                            sb12.append(tmpNumAreaCode6);
                                                            logd(sb12.toString());
                                                        } else {
                                                            tmpNumPrefix5 = tmpNumFormat9;
                                                            int i15 = areaCodeLen2;
                                                            tmpNumAreaCode6 = tmpNumAreaCode4;
                                                        }
                                                        if (!isEqualChineseFixNumberAreaCode(compNumAreaCode, tmpNumAreaCode6)) {
                                                            logd("11: areacode prefix not same, continue");
                                                            compNumLong = compNumLong3;
                                                            tmpNum8 = tmpNum10;
                                                            String str3 = tmpNumAreaCode6;
                                                            formatColumnIndex2 = formatColumnIndex3;
                                                            tmpNumAreaCode7 = compNumShort;
                                                            if (!cursor.moveToNext()) {
                                                                tmpNum7 = tmpNum8;
                                                                String str4 = tmpNumPrefix5;
                                                                break;
                                                            }
                                                            columnIndex = columnIndex2;
                                                            compNumLen = compNumLen5;
                                                            String str5 = origTmpNum6;
                                                            data4ColumnIndex2 = data4ColumnIndex3;
                                                            tmpNumFormat10 = tmpNumFormat8;
                                                            countryCodeLen2 = countryCodeLen7;
                                                            String str6 = tmpNumPrefix5;
                                                            formatColumnIndex3 = formatColumnIndex2;
                                                            compNumLong3 = compNumLong;
                                                        } else {
                                                            tmpNum9 = tmpNum10;
                                                            tmpNumAreaCode5 = tmpNumAreaCode6;
                                                        }
                                                    } else {
                                                        logd("11: compNum and tmpNum are both MPN, continue to match by mccmnc");
                                                        tmpNum9 = tmpNum10;
                                                        tmpNumPrefix5 = tmpNumFormat9;
                                                        tmpNumAreaCode5 = tmpNumAreaCode4;
                                                    }
                                                    tmpNumAreaCode4 = tmpNumAreaCode5;
                                                    formatColumnIndex2 = formatColumnIndex3;
                                                    tmpNumLen2 = tmpNum7.length();
                                                    if (tmpNumLen2 < NUM_LONG) {
                                                        String tmpNumLong = tmpNum7.substring(tmpNumLen2 - NUM_LONG);
                                                        if (-1 == numLongID2 && compNumLong3.compareTo(tmpNumLong) == 0) {
                                                            numLongID2 = cursor.getPosition();
                                                            logd("11: > NUM_LONG numLongID = " + numLongID2);
                                                        } else {
                                                            logd("11: >=NUM_LONG, and !=,  tmpNumLong = " + tmpNumLong + ", numLongID:" + numLongID2);
                                                        }
                                                        compNumLong = compNumLong3;
                                                        tmpNum8 = tmpNum7;
                                                    } else if (tmpNumLen2 >= NUM_SHORT2) {
                                                        String tmpNumShort2 = tmpNum7.substring(tmpNumLen2 - NUM_SHORT2);
                                                        if (-1 == numShortID2) {
                                                            tmpNumAreaCode7 = compNumShort;
                                                            if (tmpNumAreaCode7.compareTo(tmpNumShort2) == 0) {
                                                                numShortID2 = cursor.getPosition();
                                                            }
                                                        } else {
                                                            tmpNumAreaCode7 = compNumShort;
                                                        }
                                                        StringBuilder sb13 = new StringBuilder();
                                                        compNumLong2 = compNumLong3;
                                                        sb13.append("11: >=NUM_SHORT, tmpNumShort = ");
                                                        sb13.append(tmpNumShort2);
                                                        sb13.append(", numShortID:");
                                                        sb13.append(numShortID2);
                                                        logd(sb13.toString());
                                                        String str7 = tmpNumShort2;
                                                    } else {
                                                        compNumLong2 = compNumLong3;
                                                        tmpNumAreaCode7 = compNumShort;
                                                        logd("tmpNum11, continue");
                                                    }
                                                } else {
                                                    logd("11: compNum and tmpNum not both MPN, continue");
                                                    compNumLong = compNumLong3;
                                                    tmpNum8 = tmpNum10;
                                                    tmpNumPrefix5 = tmpNumFormat9;
                                                    formatColumnIndex2 = formatColumnIndex3;
                                                }
                                            } else {
                                                tmpNumPrefix5 = tmpNumFormat9;
                                                if (tmpNum7.length() >= 7) {
                                                    formatColumnIndex2 = formatColumnIndex3;
                                                    if ("0".equals(tmpNum7.substring(0, 1)) && !"0".equals(tmpNum7.substring(1, 2))) {
                                                        tmpNum7 = tmpNum7.substring(1);
                                                        logd("11: tmpNum remove 0 at beginning");
                                                    }
                                                } else {
                                                    formatColumnIndex2 = formatColumnIndex3;
                                                }
                                                tmpNumLen2 = tmpNum7.length();
                                                if (tmpNumLen2 < NUM_LONG) {
                                                }
                                            }
                                            tmpNumAreaCode7 = compNumShort;
                                            if (!cursor.moveToNext()) {
                                            }
                                        } else {
                                            compNumLong2 = compNumLong3;
                                            tmpNumPrefix5 = tmpNumFormat9;
                                            formatColumnIndex2 = formatColumnIndex3;
                                            tmpNumAreaCode7 = compNumShort;
                                            logd("11: countrycode prefix not same, continue");
                                        }
                                        tmpNum8 = tmpNum7;
                                        if (!cursor.moveToNext()) {
                                        }
                                    }
                                    logd("11:  numLongID = " + numLongID2 + ",numShortID = " + numShortID2);
                                    if (-1 != numLongID2) {
                                        countryCodeLen8 = numLongID2;
                                    } else if (-1 != numShortID2) {
                                        countryCodeLen8 = numShortID2;
                                    } else {
                                        countryCodeLen8 = -1;
                                    }
                                    fixedIndex5 = countryCodeLen8;
                                    origTmpNum5 = origTmpNum6;
                                    tmpNumFormat6 = tmpNumFormat8;
                                    countryCodeLen6 = countryCodeLen7;
                                } else {
                                    compNumLong = compNumLong3;
                                    compNumLen5 = compNumLen;
                                    tmpNum7 = null;
                                    fixedIndex5 = fixedIndex;
                                    countryCodeLen6 = countryCodeLen2;
                                    origTmpNum5 = origTmpNum;
                                    tmpNumFormat6 = null;
                                }
                                tmpCompNum = tmpCompNum4;
                                compNumAreaCode2 = compNumAreaCode;
                                String str8 = origTmpNum5;
                                String str9 = tmpNum7;
                                String str10 = tmpNumFormat6;
                                compNum2 = compNum7;
                                compNumLen2 = compNumLen5;
                                String str11 = compNumLong;
                                compNum4 = columnName;
                                fixedIndex2 = fixedIndex5;
                            } else {
                                origCompNum = origCompNum2;
                                tmpCompNum = tmpCompNum4;
                                compNumAreaCode2 = compNumAreaCode;
                                String str12 = compNumLong3;
                                compNumLen2 = compNumLen;
                                compNum2 = origTmpNum2;
                                fixedIndex2 = fixedIndex;
                            }
                        } else {
                            origCompNum = origCompNum2;
                            String compNum8 = origTmpNum2;
                            String tmpNumPrefix6 = null;
                            String tmpNumAreaCode8 = null;
                            String tmpNumFormat12 = null;
                            if (compNumLen >= NUM_SHORT2) {
                                String compNum9 = compNum8;
                                String compNumShort2 = compNum9.substring(compNumLen - NUM_SHORT2);
                                logd("7:  compNumShort = " + compNumShort2);
                                if (cursor.moveToFirst()) {
                                    int fixedIndex8 = cursor2.getColumnIndex(columnName);
                                    int formatColumnIndex4 = cursor2.getColumnIndex("normalized_number");
                                    int data4ColumnIndex4 = cursor2.getColumnIndex("data4");
                                    StringBuilder sb14 = new StringBuilder();
                                    String compNum10 = compNum9;
                                    sb14.append("7: columnIndex: ");
                                    sb14.append(fixedIndex8);
                                    sb14.append(", formatColumnIndex: ");
                                    sb14.append(formatColumnIndex4);
                                    sb14.append(", data4ColumnIndex: ");
                                    sb14.append(data4ColumnIndex4);
                                    logd(sb14.toString());
                                    if (fixedIndex8 != -1) {
                                        int numShortID3 = -1;
                                        int numLongID3 = -1;
                                        while (true) {
                                            int columnIndex3 = fixedIndex8;
                                            String origTmpNum9 = cursor2.getString(fixedIndex8);
                                            if (origTmpNum9 == null) {
                                                int i16 = formatColumnIndex4;
                                                int i17 = data4ColumnIndex4;
                                                formatColumnIndex = -1;
                                                break;
                                            }
                                            compNumLen4 = compNumLen;
                                            if (origTmpNum9.indexOf(64) >= 0) {
                                                int i18 = data4ColumnIndex4;
                                                formatColumnIndex = -1;
                                                break;
                                            }
                                            String origTmpNum10 = PhoneNumberUtils.stripSeparators(origTmpNum9);
                                            logd("origTmpNum: " + origTmpNum10);
                                            if (-1 != formatColumnIndex4) {
                                                tmpNumFormat4 = cursor2.getString(formatColumnIndex4);
                                                tmpNum4 = isValidData4Number(origTmpNum10, tmpNumFormat4) ? tmpNumFormat4 : origTmpNum10;
                                            } else if (-1 != data4ColumnIndex4) {
                                                tmpNumFormat4 = cursor2.getString(data4ColumnIndex4);
                                                tmpNum4 = isValidData4Number(origTmpNum10, tmpNumFormat4) ? tmpNumFormat4 : origTmpNum10;
                                            } else {
                                                tmpNum4 = origTmpNum10;
                                                tmpNumFormat4 = tmpNumFormat12;
                                            }
                                            origTmpNum4 = origTmpNum10;
                                            StringBuilder sb15 = new StringBuilder();
                                            int formatColumnIndex5 = formatColumnIndex4;
                                            sb15.append("7: tmpNumFormat: ");
                                            sb15.append(tmpNumFormat4);
                                            logd(sb15.toString());
                                            int tmpNumLen4 = tmpNum4.length();
                                            StringBuilder sb16 = new StringBuilder();
                                            int i19 = tmpNumLen4;
                                            sb16.append("7: tmpNum = ");
                                            sb16.append(tmpNum4);
                                            sb16.append(", tmpNum.length7: ");
                                            sb16.append(tmpNum4.length());
                                            sb16.append(",ID = ");
                                            sb16.append(cursor.getPosition());
                                            logd(sb16.toString());
                                            if (tmpNum4.equals(tmpCompNum4)) {
                                                numShortID = cursor.getPosition();
                                                logd("7: >= NUM_SHORT numShortID = " + numShortID + ", formattedNum full match!");
                                                tmpNumFormat5 = tmpNumFormat4;
                                                int i20 = data4ColumnIndex4;
                                                countryCodeLen5 = countryCodeLen2;
                                                break;
                                            }
                                            int countryCodeLen10 = getIntlPrefixAndCCLen(tmpNum4);
                                            if (countryCodeLen10 > 0) {
                                                tmpNumFormat5 = tmpNumFormat4;
                                                tmpNumAreaCode2 = null;
                                                tmpNumAreaCode3 = tmpNum4.substring(0, countryCodeLen10);
                                                tmpNum4 = tmpNum4.substring(countryCodeLen10);
                                                StringBuilder sb17 = new StringBuilder();
                                                countryCodeLen5 = countryCodeLen10;
                                                sb17.append("7: tmpNum after remove prefix: ");
                                                sb17.append(tmpNum4);
                                                sb17.append(", tmpNum.length7: ");
                                                sb17.append(tmpNum4.length());
                                                sb17.append(", tmpNumPrefix: ");
                                                sb17.append(tmpNumAreaCode3);
                                                logd(sb17.toString());
                                            } else {
                                                countryCodeLen5 = countryCodeLen10;
                                                tmpNumFormat5 = tmpNumFormat4;
                                                tmpNumAreaCode2 = null;
                                                tmpNumAreaCode3 = null;
                                            }
                                            if (isEqualCountryCodePrefix(compNumPrefix, str, tmpNumAreaCode3, null)) {
                                                if (isCnNumber) {
                                                    String tmpNum11 = deleteIPHead(tmpNum4);
                                                    boolean isTmpNumCnMPN2 = isChineseMobilePhoneNumber(tmpNum11);
                                                    if ((!isCompNumCnMPN || isTmpNumCnMPN2) && (isCompNumCnMPN || !isTmpNumCnMPN2)) {
                                                        if (!isCompNumCnMPN || !isTmpNumCnMPN2) {
                                                            int areaCodeLen3 = getChineseFixNumberAreaCodeLength(tmpNum11);
                                                            if (areaCodeLen3 > 0) {
                                                                boolean z2 = isTmpNumCnMPN2;
                                                                tmpNumPrefix3 = tmpNumAreaCode3;
                                                                tmpNumPrefix4 = tmpNum11.substring(0, areaCodeLen3);
                                                                tmpNum11 = tmpNum11.substring(areaCodeLen3);
                                                                StringBuilder sb18 = new StringBuilder();
                                                                int i21 = areaCodeLen3;
                                                                sb18.append("7: CN tmpNum after remove area code: ");
                                                                sb18.append(tmpNum11);
                                                                sb18.append(", tmpNum.length7: ");
                                                                sb18.append(tmpNum11.length());
                                                                sb18.append(", tmpNumAreaCode: ");
                                                                sb18.append(tmpNumPrefix4);
                                                                logd(sb18.toString());
                                                            } else {
                                                                int i22 = areaCodeLen3;
                                                                tmpNumPrefix3 = tmpNumAreaCode3;
                                                                tmpNumPrefix4 = tmpNumAreaCode2;
                                                            }
                                                            if (!isEqualChineseFixNumberAreaCode(compNumAreaCode, tmpNumPrefix4)) {
                                                                logd("7: areacode prefix not same, continue");
                                                                tmpNum5 = tmpNum11;
                                                                tmpNumAreaCode8 = tmpNumPrefix4;
                                                                data4ColumnIndex = data4ColumnIndex4;
                                                                if (!cursor.moveToNext()) {
                                                                    numShortID = numShortID3;
                                                                    tmpNum4 = tmpNum5;
                                                                    tmpNumPrefix6 = tmpNumPrefix3;
                                                                    break;
                                                                }
                                                                fixedIndex8 = columnIndex3;
                                                                compNumLen = compNumLen4;
                                                                String str13 = origTmpNum4;
                                                                formatColumnIndex4 = formatColumnIndex5;
                                                                tmpNumFormat12 = tmpNumFormat5;
                                                                countryCodeLen2 = countryCodeLen5;
                                                                tmpNumPrefix6 = tmpNumPrefix3;
                                                                data4ColumnIndex4 = data4ColumnIndex;
                                                                String str14 = columnName;
                                                            } else {
                                                                tmpNum6 = tmpNum11;
                                                            }
                                                        } else {
                                                            logd("7: compNum and tmpNum are both MPN, continue to match by mccmnc");
                                                            tmpNum6 = tmpNum11;
                                                            tmpNumPrefix3 = tmpNumAreaCode3;
                                                            tmpNumPrefix4 = tmpNumAreaCode2;
                                                        }
                                                        tmpNumAreaCode2 = tmpNumPrefix4;
                                                        data4ColumnIndex = data4ColumnIndex4;
                                                    } else {
                                                        logd("7: compNum and tmpNum not both MPN, continue");
                                                        tmpNum5 = tmpNum11;
                                                        tmpNumPrefix3 = tmpNumAreaCode3;
                                                        data4ColumnIndex = data4ColumnIndex4;
                                                        tmpNumAreaCode8 = tmpNumAreaCode2;
                                                        if (!cursor.moveToNext()) {
                                                        }
                                                    }
                                                } else {
                                                    tmpNumPrefix3 = tmpNumAreaCode3;
                                                    if (tmpNum4.length() >= 7) {
                                                        data4ColumnIndex = data4ColumnIndex4;
                                                        if ("0".equals(tmpNum4.substring(0, 1)) && !"0".equals(tmpNum4.substring(1, 2))) {
                                                            tmpNum4 = tmpNum4.substring(1);
                                                            logd("7: tmpNum remove 0 at beginning");
                                                        }
                                                    } else {
                                                        data4ColumnIndex = data4ColumnIndex4;
                                                    }
                                                }
                                                int tmpNumLen5 = tmpNum4.length();
                                                if (tmpNumLen5 >= NUM_LONG) {
                                                    tmpNumShort = tmpNum4.substring(tmpNumLen5 - NUM_SHORT2);
                                                    if (-1 == numLongID3 && compNumShort2.compareTo(tmpNumShort) == 0) {
                                                        numLongID3 = cursor.getPosition();
                                                    }
                                                    logd("7: >=NUM_LONG, tmpNumShort = " + tmpNumShort + ", numLongID:" + numLongID3);
                                                } else if (tmpNumLen5 >= NUM_SHORT2) {
                                                    tmpNumShort = tmpNum4.substring(tmpNumLen5 - NUM_SHORT2);
                                                    if (-1 == numShortID3 && compNumShort2.compareTo(tmpNumShort) == 0) {
                                                        numShortID3 = cursor.getPosition();
                                                        logd("7: >= NUM_SHORT numShortID = " + numShortID3);
                                                    } else {
                                                        logd("7: >=NUM_SHORT, and !=, tmpNumShort = " + tmpNumShort + ", numShortID:" + numShortID3);
                                                    }
                                                } else {
                                                    logd("7: continue");
                                                }
                                            } else {
                                                tmpNumPrefix3 = tmpNumAreaCode3;
                                                data4ColumnIndex = data4ColumnIndex4;
                                                logd("7: countrycode prefix not same, continue");
                                            }
                                            tmpNum5 = tmpNum4;
                                            tmpNumAreaCode8 = tmpNumAreaCode2;
                                            if (!cursor.moveToNext()) {
                                            }
                                        }
                                        return formatColumnIndex;
                                    }
                                    compNumLen4 = compNumLen;
                                    tmpNum4 = null;
                                    fixedIndex4 = fixedIndex;
                                    countryCodeLen4 = countryCodeLen2;
                                    origTmpNum4 = origTmpNum;
                                    tmpNumPrefix2 = null;
                                    tmpNumAreaCode = null;
                                    tmpNumFormat3 = null;
                                    tmpCompNum = tmpCompNum4;
                                    compNumAreaCode2 = compNumAreaCode;
                                    String str15 = tmpNum4;
                                    String str16 = tmpNumPrefix2;
                                    String str17 = tmpNumAreaCode;
                                    String str18 = tmpNumFormat3;
                                    compNum2 = compNum10;
                                    compNumLen2 = compNumLen4;
                                    String str19 = origTmpNum4;
                                    compNum4 = columnName;
                                    String tmpNumPrefix7 = compNumShort2;
                                    fixedIndex2 = fixedIndex4;
                                } else {
                                    String tmpNumPrefix8 = compNumShort2;
                                    tmpCompNum = tmpCompNum4;
                                    compNumAreaCode2 = compNumAreaCode;
                                    fixedIndex2 = fixedIndex;
                                    compNumLen2 = compNumLen;
                                    compNum2 = compNum9;
                                    compNum4 = columnName;
                                }
                            } else {
                                int compNumLen7 = compNumLen;
                                String compNum11 = compNum8;
                                if (cursor.moveToFirst()) {
                                    compNum4 = columnName;
                                    int columnIndex4 = cursor2.getColumnIndex(compNum4);
                                    int formatColumnIndex6 = cursor2.getColumnIndex("normalized_number");
                                    int data4ColumnIndex5 = cursor2.getColumnIndex("data4");
                                    logd("5: columnIndex: " + columnIndex4 + ", formatColumnIndex: " + formatColumnIndex6 + ", data4ColumnIndex: " + data4ColumnIndex5);
                                    if (columnIndex4 != -1) {
                                        int fixedIndex9 = fixedIndex;
                                        while (true) {
                                            String origTmpNum11 = cursor2.getString(columnIndex4);
                                            if (origTmpNum11 == null || origTmpNum11.indexOf(64) >= 0) {
                                                int i23 = formatColumnIndex6;
                                                String str20 = tmpCompNum4;
                                                String str21 = compNumAreaCode;
                                                String str22 = compNum11;
                                                int i24 = compNumLen7;
                                            } else {
                                                origTmpNum3 = PhoneNumberUtils.stripSeparators(origTmpNum11);
                                                StringBuilder sb19 = new StringBuilder();
                                                int columnIndex5 = columnIndex4;
                                                sb19.append("origTmpNum: ");
                                                sb19.append(origTmpNum3);
                                                logd(sb19.toString());
                                                if (-1 != formatColumnIndex6) {
                                                    tmpNumFormat = cursor2.getString(formatColumnIndex6);
                                                    tmpNum = isValidData4Number(origTmpNum3, tmpNumFormat) ? tmpNumFormat : origTmpNum3;
                                                } else if (-1 != data4ColumnIndex5) {
                                                    tmpNumFormat = cursor2.getString(data4ColumnIndex5);
                                                    tmpNum = isValidData4Number(origTmpNum3, tmpNumFormat) ? tmpNumFormat : origTmpNum3;
                                                } else {
                                                    tmpNum = origTmpNum3;
                                                    tmpNumFormat = tmpNumFormat12;
                                                }
                                                StringBuilder sb20 = new StringBuilder();
                                                int formatColumnIndex7 = formatColumnIndex6;
                                                sb20.append("5: tmpNumFormat: ");
                                                sb20.append(tmpNumFormat);
                                                logd(sb20.toString());
                                                int tmpNumLen6 = tmpNum.length();
                                                StringBuilder sb21 = new StringBuilder();
                                                tmpNumFormat2 = tmpNumFormat;
                                                sb21.append("5: tmpNum = ");
                                                sb21.append(tmpNum);
                                                sb21.append(", tmpNum.length: ");
                                                sb21.append(tmpNum.length());
                                                sb21.append(",ID = ");
                                                sb21.append(cursor.getPosition());
                                                logd(sb21.toString());
                                                if (tmpNum.equals(tmpCompNum4)) {
                                                    fixedIndex3 = cursor.getPosition();
                                                    logd("5: break! numLongID = " + fixedIndex3 + ", formattedNum full match!");
                                                    tmpCompNum = tmpCompNum4;
                                                    compNumAreaCode2 = compNumAreaCode;
                                                    countryCodeLen3 = countryCodeLen2;
                                                    tmpNumPrefix = tmpNumPrefix6;
                                                    compNum2 = compNum11;
                                                    compNumLen2 = compNumLen7;
                                                    break;
                                                }
                                                String tmpNumAreaCode9 = null;
                                                int countryCodeLen11 = getIntlPrefixAndCCLen(tmpNum);
                                                if (countryCodeLen11 > 0) {
                                                    int i25 = tmpNumLen6;
                                                    tmpCompNum = tmpCompNum4;
                                                    tmpCompNum3 = tmpNum.substring(0, countryCodeLen11);
                                                    tmpNum = tmpNum.substring(countryCodeLen11);
                                                    StringBuilder sb22 = new StringBuilder();
                                                    countryCodeLen3 = countryCodeLen11;
                                                    sb22.append("5: tmpNum after remove prefix: ");
                                                    sb22.append(tmpNum);
                                                    sb22.append(", tmpNum.length5: ");
                                                    sb22.append(tmpNum.length());
                                                    sb22.append(", tmpNumPrefix: ");
                                                    sb22.append(tmpCompNum3);
                                                    logd(sb22.toString());
                                                } else {
                                                    countryCodeLen3 = countryCodeLen11;
                                                    int i26 = tmpNumLen6;
                                                    tmpCompNum = tmpCompNum4;
                                                    tmpCompNum3 = null;
                                                }
                                                if (isEqualCountryCodePrefix(compNumPrefix, str, tmpCompNum3, null)) {
                                                    if (isCnNumber) {
                                                        String tmpNum12 = deleteIPHead(tmpNum);
                                                        boolean isTmpNumCnMPN3 = isChineseMobilePhoneNumber(tmpNum12);
                                                        if ((!isCompNumCnMPN || isTmpNumCnMPN3) && (isCompNumCnMPN || !isTmpNumCnMPN3)) {
                                                            if (!isCompNumCnMPN || !isTmpNumCnMPN3) {
                                                                int areaCodeLen4 = getChineseFixNumberAreaCodeLength(tmpNum12);
                                                                if (areaCodeLen4 > 0) {
                                                                    tmpNumPrefix = tmpCompNum3;
                                                                    tmpNumAreaCode9 = tmpNum12.substring(0, areaCodeLen4);
                                                                    tmpNum12 = tmpNum12.substring(areaCodeLen4);
                                                                    StringBuilder sb23 = new StringBuilder();
                                                                    int i27 = areaCodeLen4;
                                                                    sb23.append("5: CN tmpNum after remove area code: ");
                                                                    sb23.append(tmpNum12);
                                                                    sb23.append(", tmpNum.length5: ");
                                                                    sb23.append(tmpNum12.length());
                                                                    sb23.append(", tmpNumAreaCode: ");
                                                                    sb23.append(tmpNumAreaCode9);
                                                                    logd(sb23.toString());
                                                                } else {
                                                                    tmpNumPrefix = tmpCompNum3;
                                                                }
                                                                if (isEqualChineseFixNumberAreaCode(compNumAreaCode, tmpNumAreaCode9) == 0) {
                                                                    logd("5: areacode prefix not same, continue");
                                                                    tmpNum2 = tmpNum12;
                                                                } else {
                                                                    tmpNum3 = tmpNum12;
                                                                }
                                                            } else {
                                                                logd("5: compNum and tmpNum are both MPN, continue to match by mccmnc");
                                                                tmpNum3 = tmpNum12;
                                                                tmpNumPrefix = tmpCompNum3;
                                                            }
                                                            compNumAreaCode2 = compNumAreaCode;
                                                        } else {
                                                            logd("5: compNum and tmpNum not both MPN, continue");
                                                            tmpNum2 = tmpNum12;
                                                            tmpNumPrefix = tmpCompNum3;
                                                        }
                                                        compNumAreaCode2 = compNumAreaCode;
                                                        compNum2 = compNum11;
                                                        compNumLen2 = compNumLen7;
                                                        if (cursor.moveToNext()) {
                                                            fixedIndex3 = fixedIndex9;
                                                            tmpNum = tmpNum2;
                                                            break;
                                                        }
                                                        compNum11 = compNum2;
                                                        compNumLen7 = compNumLen2;
                                                        String str23 = origTmpNum3;
                                                        columnIndex4 = columnIndex5;
                                                        formatColumnIndex6 = formatColumnIndex7;
                                                        tmpNumFormat12 = tmpNumFormat2;
                                                        tmpCompNum4 = tmpCompNum;
                                                        countryCodeLen2 = countryCodeLen3;
                                                        tmpNumPrefix6 = tmpNumPrefix;
                                                        compNumAreaCode = compNumAreaCode2;
                                                    } else {
                                                        tmpNumPrefix = tmpCompNum3;
                                                        if (tmpNum.length() >= 7) {
                                                            compNumAreaCode2 = compNumAreaCode;
                                                            if ("0".equals(tmpNum.substring(0, 1))) {
                                                                if (!"0".equals(tmpNum.substring(1, 2))) {
                                                                    tmpNum = tmpNum.substring(1);
                                                                    logd("5: tmpNum remove 0 at beginning");
                                                                }
                                                            }
                                                        } else {
                                                            compNumAreaCode2 = compNumAreaCode;
                                                        }
                                                        tmpNumLen = tmpNum.length();
                                                        compNumLen2 = compNumLen7;
                                                        if (tmpNumLen == compNumLen2) {
                                                            int i28 = tmpNumLen;
                                                            compNum2 = compNum11;
                                                            logd("5: continue");
                                                        } else if (-1 == fixedIndex9) {
                                                            compNum2 = compNum11;
                                                            if (compNum2.compareTo(tmpNum) == 0) {
                                                                fixedIndex9 = cursor.getPosition();
                                                                StringBuilder sb24 = new StringBuilder();
                                                                int i29 = tmpNumLen;
                                                                sb24.append("5: break! numLongID = ");
                                                                sb24.append(fixedIndex9);
                                                                logd(sb24.toString());
                                                            }
                                                        } else {
                                                            compNum2 = compNum11;
                                                        }
                                                    }
                                                    tmpNumLen = tmpNum.length();
                                                    compNumLen2 = compNumLen7;
                                                    if (tmpNumLen == compNumLen2) {
                                                    }
                                                } else {
                                                    tmpNumPrefix = tmpCompNum3;
                                                    compNumAreaCode2 = compNumAreaCode;
                                                    compNum2 = compNum11;
                                                    compNumLen2 = compNumLen7;
                                                    logd("5: countrycode prefix not same, continue");
                                                }
                                                tmpNum2 = tmpNum;
                                                if (cursor.moveToNext()) {
                                                }
                                            }
                                        }
                                        logd("5: fixedIndex = " + fixedIndex3);
                                        fixedIndex2 = fixedIndex3;
                                        String str24 = tmpNum;
                                        String str25 = origTmpNum3;
                                        String str26 = tmpNumFormat2;
                                        int i30 = countryCodeLen3;
                                        String str27 = tmpNumPrefix;
                                    } else {
                                        tmpCompNum2 = tmpCompNum4;
                                        compNumAreaCode3 = compNumAreaCode;
                                        compNum3 = compNum11;
                                        compNumLen3 = compNumLen7;
                                    }
                                } else {
                                    tmpCompNum2 = tmpCompNum4;
                                    compNumAreaCode3 = compNumAreaCode;
                                    compNum3 = compNum11;
                                    compNumLen3 = compNumLen7;
                                    compNum4 = columnName;
                                }
                                fixedIndex2 = fixedIndex;
                            }
                        }
                        logd("fixedIndex: " + fixedIndex2);
                        if (-1 != fixedIndex2) {
                            String origCompNum3 = origCompNum;
                            if (shouldDoNumberMatchAgainBySimMccmnc(origCompNum3, str)) {
                                String str28 = compNum2;
                                int i31 = compNumLen2;
                                String str29 = tmpCompNum;
                                String str30 = compNumAreaCode2;
                                int i32 = NUM_SHORT2;
                                String str31 = formattedCompNum2;
                                String str32 = origCompNum3;
                                fixedIndex2 = getCallerIndexInternal(cursor2, origCompNum3, compNum4, this.mSimNumLong, this.mSimNumShort);
                            } else {
                                int i33 = compNumLen2;
                                int i34 = NUM_SHORT2;
                                String str33 = origCompNum3;
                                String str34 = formattedCompNum2;
                                String str35 = tmpCompNum;
                                String str36 = compNumAreaCode2;
                            }
                        } else {
                            int i35 = compNumLen2;
                            int i36 = NUM_SHORT2;
                            String str37 = formattedCompNum2;
                            String str38 = origCompNum;
                            String str39 = tmpCompNum;
                            String str40 = compNumAreaCode2;
                        }
                        return fixedIndex2;
                    }
                }
                String str41 = compNum5;
                formattedCompNum = formattedCompNum3;
                countryCodeLen = getIntlPrefixAndCCLen(compNum5);
                if (countryCodeLen <= 0) {
                }
                String tmpCompNum42 = TextUtils.isEmpty(formattedCompNum) != 0 ? formattedCompNum : origCompNum2;
                if (!isRoamingCountryNumberByPrefix(compNumPrefix, str)) {
                }
                isCnNumber = isChineseNumberByPrefix(compNumPrefix, str);
                if (!isCnNumber) {
                }
                compNumLen = origTmpNum2.length();
                if (compNumLen < NUM_LONG) {
                }
                logd("fixedIndex: " + fixedIndex2);
                if (-1 != fixedIndex2) {
                }
                return fixedIndex2;
            }
        }
        Log.e(TAG, "CallerInfoHW(), cursor is empty! fixedIndex = " + -1);
        return -1;
    }

    public static boolean isfixedIndexValid(String cookie, Cursor cursor) {
        int fixedIndex = new CallerInfoHW().getCallerIndex(cursor, cookie, "number");
        return fixedIndex != -1 && cursor.moveToPosition(fixedIndex);
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
        int i = 0;
        if (TextUtils.isEmpty(tmpNumber) || tmpNumber.length() < 9) {
            return 0;
        }
        if (!tmpNumber.startsWith("0")) {
            tmpNumber = "0" + tmpNumber;
        }
        int i2 = 2;
        String top2String = tmpNumber.substring(0, 2);
        if (top2String.equals(FIXED_NUMBER_TOP2_TOKEN1) || top2String.equals(FIXED_NUMBER_TOP2_TOKEN2)) {
            String areaCodeString = tmpNumber.substring(0, 3);
            ArrayList<String> areaCodeArray = this.chineseFixNumberAreaCodeMap.get(1);
            int areaCodeArraySize = areaCodeArray.size();
            while (true) {
                if (i >= areaCodeArraySize) {
                    break;
                } else if (areaCodeString.equals(areaCodeArray.get(i))) {
                    if (tmpNumber.equals(number)) {
                        i2 = 3;
                    }
                    len = i2;
                    logd("getChineseFixNumberAreaCodeLength(), matched area code len: " + len + ", number: " + number);
                } else {
                    i++;
                }
            }
        } else {
            int i3 = 4;
            String areaCodeString2 = tmpNumber.substring(0, 4);
            ArrayList<String> areaCodeArray2 = this.chineseFixNumberAreaCodeMap.get(2);
            int areaCodeArraySize2 = areaCodeArray2.size();
            while (true) {
                if (i >= areaCodeArraySize2) {
                    break;
                } else if (areaCodeString2.equals(areaCodeArray2.get(i))) {
                    if (!tmpNumber.equals(number)) {
                        i3 = 3;
                    }
                    len = i3;
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
        if (!TextUtils.isEmpty(compNumAreaCode) || TextUtils.isEmpty(dbNumAreaCode)) {
            if (TextUtils.isEmpty(compNumAreaCode) || !TextUtils.isEmpty(dbNumAreaCode)) {
                if (!compNumAreaCode.startsWith("0")) {
                    compNumAreaCode = "0" + compNumAreaCode;
                }
                if (!dbNumAreaCode.startsWith("0")) {
                    dbNumAreaCode = "0" + dbNumAreaCode;
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
        boolean ret;
        int countryCode;
        int countryCode2;
        if (TextUtils.isEmpty(num1Prefix) && TextUtils.isEmpty(num2Prefix)) {
            logd("isEqualCountryCodePrefix(), both have no country code, return true");
            return true;
        }
        if (TextUtils.isEmpty(num1Prefix) && !TextUtils.isEmpty(num2Prefix)) {
            logd("isEqualCountryCodePrefix(), netIso1: " + netIso1 + ", netIso2: " + netIso2);
            if (!TextUtils.isEmpty(netIso1)) {
                String netIso = netIso1.toUpperCase();
                if ("CN".equals(netIso)) {
                    countryCode2 = this.countryCodeforCN;
                } else {
                    countryCode2 = sInstance.getCountryCodeForRegion(netIso);
                }
                ret = num2Prefix.substring(getIntlPrefixLength(num2Prefix)).equals(Integer.toString(countryCode2));
            } else {
                ret = true;
            }
        } else if (TextUtils.isEmpty(num1Prefix) || !TextUtils.isEmpty(num2Prefix)) {
            ret = num1Prefix.substring(getIntlPrefixLength(num1Prefix)).equals(num2Prefix.substring(getIntlPrefixLength(num2Prefix)));
        } else {
            logd("isEqualCountryCodePrefix(), netIso1: " + netIso1 + ", netIso2: " + netIso2);
            if (!TextUtils.isEmpty(netIso2)) {
                String netIso3 = netIso2.toUpperCase();
                if ("CN".equals(netIso3)) {
                    countryCode = this.countryCodeforCN;
                } else {
                    countryCode = sInstance.getCountryCodeForRegion(netIso3);
                }
                ret = num1Prefix.substring(getIntlPrefixLength(num1Prefix)).equals(Integer.toString(countryCode));
            } else {
                ret = true;
            }
        }
        return ret;
    }

    private int getFullMatchIndex(Cursor cursor, String compNum, String columnName) {
        String compNum2 = PhoneNumberUtils.stripSeparators(compNum);
        if (this.IS_CHINA_TELECOM && compNum2.startsWith("**133") && compNum2.endsWith("#")) {
            compNum2 = compNum2.substring(0, compNum2.length() - 1);
            logd("full match check, compNum startsWith **133 && endsWith #");
        }
        logd("full match check, compNum: " + compNum2);
        if (cursor == null || !cursor.moveToFirst()) {
            return -1;
        }
        int columnIndex = cursor.getColumnIndex(columnName);
        if (-1 == columnIndex) {
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
        if (!SystemProperties.getBoolean(HwTelephonyProperties.PROPERTY_NETWORK_ISROAMING, false) || getFormatNumberByCountryISO(number, countryIso) != null) {
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

    /* JADX WARNING: Code restructure failed: missing block: B:120:0x03d9, code lost:
        return -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x01e6, code lost:
        return -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x0302, code lost:
        logd("7: numShortID = " + r10 + ", numLongID = " + r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x031f, code lost:
        if (-1 == r10) goto L_0x0324;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x0321, code lost:
        r3 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x0322, code lost:
        r12 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x0324, code lost:
        if (-1 == r11) goto L_0x0328;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x0326, code lost:
        r3 = r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x0328, code lost:
        r3 = -1;
     */
    /* JADX WARNING: Removed duplicated region for block: B:126:0x01b5 A[EDGE_INSN: B:126:0x01b5->B:46:0x01b5 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:127:0x0302 A[EDGE_INSN: B:127:0x0302->B:87:0x0302 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x01dd A[LOOP:0: B:21:0x00bf->B:53:0x01dd, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x032a A[LOOP:1: B:62:0x0215->B:94:0x032a, LOOP_END] */
    private int getCallerIndexInternal(Cursor cursor, String compNum, String columnName, int numLong, int numShort) {
        String tmpNumShort;
        int i;
        Cursor cursor2 = cursor;
        String str = compNum;
        String str2 = columnName;
        int i2 = numLong;
        int i3 = numShort;
        String compNumLong = null;
        String tmpNumShort2 = null;
        int numShortID = -1;
        int numLongID = -1;
        int fixedIndex = -1;
        logd("getCallerIndexInternal, compNum: " + str + ", numLong: " + i2 + ", numShort: " + i3);
        if (TextUtils.isEmpty(compNum)) {
            if (cursor2 != null && cursor.getCount() > 0) {
                fixedIndex = 0;
            }
            Log.e(TAG, "getCallerIndexInternal(),null == compNum! fixedIndex = " + fixedIndex);
            return fixedIndex;
        }
        int compNumLen = compNum.length();
        int NUM_LONG = 7;
        if (i2 >= 7) {
            NUM_LONG = i2;
        }
        int NUM_SHORT = i3 >= NUM_LONG ? NUM_LONG : i3;
        logd("getCallerIndexInternal, after check NUM_LONG: " + NUM_LONG + ", NUM_SHORT: " + NUM_SHORT);
        if (cursor2 != null) {
            if (compNumLen >= NUM_LONG) {
                String compNumLong2 = str.substring(compNumLen - NUM_LONG);
                String compNumShort = str.substring(compNumLen - NUM_SHORT);
                logd("11: compNumLong = " + compNumLong2 + ", compNumShort = " + compNumShort);
                if (cursor.moveToFirst()) {
                    int fixedIndex2 = cursor2.getColumnIndex(str2);
                    if (fixedIndex2 != -1) {
                        while (true) {
                            String tmpNum = cursor2.getString(fixedIndex2);
                            if (tmpNum == null || tmpNum.indexOf(64) >= 0) {
                                int columnIndex = fixedIndex2;
                                String str3 = tmpNumShort2;
                            } else {
                                String tmpNum2 = PhoneNumberUtils.stripSeparators(tmpNum);
                                int tmpNumLen = tmpNum2.length();
                                int columnIndex2 = fixedIndex2;
                                StringBuilder sb = new StringBuilder();
                                String tmpNumShort3 = tmpNumShort2;
                                sb.append("11: tmpNum = ");
                                sb.append(tmpNum2);
                                sb.append(", tmpNum.length11: ");
                                sb.append(tmpNum2.length());
                                sb.append(", ID = ");
                                sb.append(cursor.getPosition());
                                logd(sb.toString());
                                if (str.equals(tmpNum2)) {
                                    int numLongID2 = cursor.getPosition();
                                    logd("exact match: break! numLongID = " + numLongID2);
                                    numLongID = numLongID2;
                                    String str4 = tmpNumShort3;
                                    break;
                                }
                                if (tmpNumLen >= NUM_LONG) {
                                    String tmpNumLong = tmpNum2.substring(tmpNumLen - NUM_LONG);
                                    if (-1 == numLongID && compNumLong2.compareTo(tmpNumLong) == 0) {
                                        numLongID = cursor.getPosition();
                                        logd("11: > NUM_LONG numLongID = " + numLongID);
                                    }
                                    logd("11: >= NUM_LONG, and !=,  tmpNumLong = " + tmpNumLong + ", numLongID: " + numLongID);
                                    String str5 = tmpNumLong;
                                } else if (tmpNumLen >= NUM_SHORT) {
                                    String tmpNumShort4 = tmpNum2.substring(tmpNumLen - NUM_SHORT);
                                    if (-1 == numShortID && compNumShort.compareTo(tmpNumShort4) == 0) {
                                        numShortID = cursor.getPosition();
                                    }
                                    StringBuilder sb2 = new StringBuilder();
                                    int i4 = tmpNumLen;
                                    sb2.append("11: >= NUM_SHORT, tmpNumShort = ");
                                    sb2.append(tmpNumShort4);
                                    sb2.append(", numShortID:");
                                    sb2.append(numShortID);
                                    logd(sb2.toString());
                                    tmpNumShort2 = tmpNumShort4;
                                    if (cursor.moveToNext()) {
                                        break;
                                    }
                                    fixedIndex2 = columnIndex2;
                                } else {
                                    logd("tmpNum11, continue");
                                }
                                tmpNumShort2 = tmpNumShort3;
                                if (cursor.moveToNext()) {
                                }
                            }
                        }
                        logd("11: numLongID = " + numLongID + ", numShortID = " + numShortID);
                        if (-1 != numLongID) {
                            i = numLongID;
                        } else if (-1 != numShortID) {
                            i = numShortID;
                        } else {
                            i = -1;
                        }
                        fixedIndex = i;
                    }
                }
            } else if (compNumLen >= NUM_SHORT) {
                String compNumShort2 = str.substring(compNumLen - NUM_SHORT);
                logd("7: compNumShort = " + compNumShort2);
                if (cursor.moveToFirst()) {
                    int fixedIndex3 = cursor2.getColumnIndex(str2);
                    if (fixedIndex3 != -1) {
                        while (true) {
                            String tmpNum3 = cursor2.getString(fixedIndex3);
                            if (tmpNum3 == null || tmpNum3.indexOf(64) >= 0) {
                                int columnIndex3 = fixedIndex3;
                                String str6 = compNumLong;
                            } else {
                                String tmpNum4 = PhoneNumberUtils.stripSeparators(tmpNum3);
                                int tmpNumLen2 = tmpNum4.length();
                                int columnIndex4 = fixedIndex3;
                                StringBuilder sb3 = new StringBuilder();
                                String compNumLong3 = compNumLong;
                                sb3.append("7: tmpNum = ");
                                sb3.append(tmpNum4);
                                sb3.append(", tmpNum.length7: ");
                                sb3.append(tmpNum4.length());
                                sb3.append(", ID = ");
                                sb3.append(cursor.getPosition());
                                logd(sb3.toString());
                                if (str.equals(tmpNum4)) {
                                    int numShortID2 = cursor.getPosition();
                                    logd("exact match numShortID = " + numShortID2);
                                    numShortID = numShortID2;
                                    break;
                                }
                                if (tmpNumLen2 >= NUM_LONG) {
                                    tmpNumShort = tmpNum4.substring(tmpNumLen2 - NUM_SHORT);
                                    if (-1 == numLongID && compNumShort2.compareTo(tmpNumShort) == 0) {
                                        numLongID = cursor.getPosition();
                                    }
                                    logd("7: >= NUM_LONG, tmpNumShort = " + tmpNumShort + ", numLongID:" + numLongID);
                                } else if (tmpNumLen2 >= NUM_SHORT) {
                                    tmpNumShort = tmpNum4.substring(tmpNumLen2 - NUM_SHORT);
                                    if (-1 == numShortID && compNumShort2.compareTo(tmpNumShort) == 0) {
                                        numShortID = cursor.getPosition();
                                        logd("7: >= NUM_SHORT numShortID = " + numShortID);
                                    }
                                    logd("7: >= NUM_SHORT, and !=, tmpNumShort = " + tmpNumShort + ", numShortID:" + numShortID);
                                } else {
                                    logd("7: continue");
                                    if (cursor.moveToNext()) {
                                        break;
                                    }
                                    fixedIndex3 = columnIndex4;
                                    compNumLong = compNumLong3;
                                }
                                if (cursor.moveToNext()) {
                                }
                            }
                        }
                        int columnIndex32 = fixedIndex3;
                        String str62 = compNumLong;
                        return -1;
                    }
                }
            } else if (cursor.moveToFirst()) {
                int columnIndex5 = cursor2.getColumnIndex(str2);
                if (columnIndex5 != -1) {
                    while (true) {
                        String tmpNum5 = cursor2.getString(columnIndex5);
                        if (tmpNum5 != null && tmpNum5.indexOf(64) < 0) {
                            String tmpNum6 = PhoneNumberUtils.stripSeparators(tmpNum5);
                            int tmpNumLen3 = tmpNum6.length();
                            logd("5: tmpNum = " + tmpNum6 + ", tmpNum.length: " + tmpNum6.length() + ", ID = " + cursor.getPosition());
                            if (tmpNumLen3 == compNumLen) {
                                if (-1 == -1 && str.compareTo(tmpNum6) == 0) {
                                    fixedIndex = cursor.getPosition();
                                    logd("5: break! numLongID = " + fixedIndex);
                                    break;
                                }
                            } else {
                                logd("5: continue");
                            }
                            if (!cursor.moveToNext()) {
                                break;
                            }
                            String str7 = columnName;
                        }
                    }
                    logd("5: fixedIndex = " + fixedIndex);
                }
            }
            logd("getCallerIndexInternal, fixedIndex: " + fixedIndex);
            return fixedIndex;
        }
        logd("getCallerIndexInternal, fixedIndex: " + fixedIndex);
        return fixedIndex;
    }

    private boolean compareNumsInternal(String num1, String num2, int numLong, int numShort) {
        String str = num1;
        String str2 = num2;
        int i = numLong;
        int i2 = numShort;
        logd("compareNumsInternal, num1: " + str + ", num2: " + str2 + ", numLong: " + i + ", numShort: " + i2);
        if (TextUtils.isEmpty(num1) || TextUtils.isEmpty(num2)) {
            return false;
        }
        int num1Len = num1.length();
        int num2Len = num2.length();
        int NUM_LONG = 7;
        if (i >= 7) {
            NUM_LONG = i;
        }
        int NUM_SHORT = i2 >= NUM_LONG ? NUM_LONG : i2;
        logd("compareNumsInternal, after check NUM_LONG: " + NUM_LONG + ", NUM_SHORT: " + NUM_SHORT);
        if (num1Len >= NUM_LONG) {
            String num1Long = str.substring(num1Len - NUM_LONG);
            String num1Short = str.substring(num1Len - NUM_SHORT);
            logd("compareNumsInternal, 11: num1Long = " + num1Long + ", num1Short = " + num1Short);
            if (num2Len >= NUM_LONG) {
                if (num1Long.compareTo(str2.substring(num2Len - NUM_LONG)) == 0) {
                    logd("compareNumsInternal, 11: >= NUM_LONG return true");
                    return true;
                }
            } else if (num2Len >= NUM_SHORT && num1Short.compareTo(str2.substring(num2Len - NUM_SHORT)) == 0) {
                logd("compareNumsInternal, 11: >= NUM_SHORT return true");
                return true;
            }
        } else if (num1Len >= NUM_SHORT) {
            String num1Short2 = str.substring(num1Len - NUM_SHORT);
            logd("compareNumsInternal, 7: num1Short = " + num1Short2);
            if (num2Len >= NUM_SHORT && num1Short2.compareTo(str2.substring(num2Len - NUM_SHORT)) == 0) {
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
            String numberPrefix2 = numberPrefix.substring(getIntlPrefixLength(numberPrefix));
            if (!TextUtils.isEmpty(numberPrefix2) && !TextUtils.isEmpty(netIso)) {
                return numberPrefix2.equals(Integer.toString(sInstance.getCountryCodeForRegion(netIso.toUpperCase(Locale.US))));
            }
        }
        return false;
    }

    private boolean isValidData4Number(String data1Num, String data4Num) {
        logd("isValidData4Number, data1Num: " + data1Num + ", data4Num: " + data4Num);
        if (!TextUtils.isEmpty(data1Num) && !TextUtils.isEmpty(data4Num) && data4Num.startsWith("+")) {
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
        int operatorCount = NORMAL_PREFIX_MCC.length;
        if (this.mNetworkOperator != null && this.mNetworkOperator.length() > 3) {
            sMcc = this.mNetworkOperator.substring(0, 3);
        }
        if (number.startsWith("011")) {
            int i = 0;
            while (i < operatorCount) {
                if (!sMcc.equals(NORMAL_PREFIX_MCC[i]) || number.length() != 11) {
                    i++;
                } else {
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
        if (!tempstring.matches("[0-9]+")) {
            return false;
        }
        return true;
    }
}
