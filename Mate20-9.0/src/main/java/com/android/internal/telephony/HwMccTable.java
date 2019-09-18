package com.android.internal.telephony;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstants;
import com.android.internal.telephony.vsim.HwVSimUtils;
import com.android.internal.util.XmlUtils;
import huawei.cust.HwCfgFilePolicy;
import huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwMccTable {
    private static final String[] BritianEnglish = {"IE", "GB", "BN", "MY", "PG", "NR", "WS", "FJ", "VU", "TO", "MT", "GI"};
    private static final String ICCID_IUCACELL_MEXICO_PREFIX = "8952050";
    private static final int LENGTH_MIN = 0;
    private static final String LOG_TAG = "HwMccTable";
    private static final String MCC_MNC_NOWAY_EX = "2400768";
    private static final String MCC_MNC_SWEDISH_EX = "24007";
    private static final String MCC_TABLE_V2_PROP_COUNTRY = "country";
    private static final String MCC_TABLE_V2_PROP_LANGUAGE = "language";
    private static final String MCC_TABLE_V2_PROP_MCC = "mcc";
    private static final String MCC_TABLE_V2_PROP_NODE_ENTRY = "mcc-entry";
    private static final String MCC_TABLE_V2_PROP_NODE_ROOT = "mcc-table";
    private static final String MCC_TABLE_V2_PROP_SCRIPT = "script";
    private static final String MCC_TABLE_V2_PROP_TIME_ZONE = "time-zone";
    private static final String MCC_TABLE_V2_SMALLEST_DIGIT = "smallest-digit";
    private static final String NAME_COUNTRY = "COUNTRY_CODES";
    private static final String NAME_IND = "IND_CODES";
    private static final String NAME_LANG = "LANG_STRINGS";
    private static final String NAME_MCC = "MCC_CODES";
    private static final String NAME_TZ = "TZ_STRINGS";
    private static final String PLMN_IUCACELL_MEXICO = "22201";
    private static String SIM_LANGUAGE = "sim_language";
    protected static final String TIMEZONE_PROPERTY = "persist.sys.timezone";
    private static String custFilePath = "/data/cust/xml/";
    private static String hwCfgPolicyPath = "hwCfgPolicyPath";
    static boolean isDefaultTimezone = false;
    private static boolean mEnableSimLang = SystemProperties.getBoolean("ro.config.simlang", false);
    private static final boolean mHB_defaultlanguage = SystemProperties.getBoolean("ro.config.hw.BH_defaultlanguage", false);
    private static final int mHB_mcc = 218;
    private static final int mHB_mncEronet = 3;
    private static final int mHB_mncMobile = 90;
    private static final int mHB_mncTel = 5;
    private static String mIccId = null;
    static String mImsi = null;
    private static int mMnc;
    static ArrayList<MccEntry> sTable = new ArrayList<>();
    private static String systemFilePath = "/system/etc/";

    static class MccEntry implements Comparable<MccEntry> {
        String mIso;
        String mLanguage;
        int mMcc;
        int mSmallestDigitsMnc;
        String script;
        String timeZone;

        MccEntry(int mnc, String iso, int smallestDigitsMCC) {
            this(mnc, iso, smallestDigitsMCC, null);
        }

        MccEntry(int mnc, String iso, int smallestDigitsMCC, String language) {
            this(mnc, iso, smallestDigitsMCC, language, null);
        }

        MccEntry(int mnc, String iso, int smallestDigitsMCC, String language, String timeZone2) {
            this(mnc, iso, smallestDigitsMCC, language, timeZone2, null);
        }

        MccEntry(int mnc, String iso, int smallestDigitsMCC, String language, String timeZone2, String script2) {
            this.mMcc = mnc;
            this.mIso = iso;
            this.mSmallestDigitsMnc = smallestDigitsMCC;
            this.mLanguage = language;
            this.timeZone = timeZone2;
            this.script = script2;
        }

        public int compareTo(MccEntry o) {
            return this.mMcc - o.mMcc;
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof MccEntry)) {
                return false;
            }
            if (this.mMcc - ((MccEntry) o).mMcc == 0) {
                z = true;
            }
            return z;
        }

        public int hashCode() {
            return this.mMcc;
        }
    }

    static {
        if (loadCustMccTableV2(hwCfgPolicyPath)) {
            Rlog.d(LOG_TAG, "loadCustMccTableV2 from hwCfgPolicyPath sucess");
        } else if (loadCustMccTableV2(custFilePath)) {
            Rlog.d(LOG_TAG, "loadCustMccTableV2 from cust sucess");
        } else if (loadCustMccTableV2(systemFilePath)) {
            Rlog.d(LOG_TAG, "loadCustMccTableV2 from system/etc sucess");
        } else {
            Rlog.d(LOG_TAG, "start loadCustMccTable sucess");
            loadCustMccTable();
        }
    }

    private static MccEntry entryForMcc(int mcc) {
        int index = Collections.binarySearch(sTable, new MccEntry(mcc, null, 0));
        if (index < 0) {
            return null;
        }
        return sTable.get(index);
    }

    public static String custTimeZoneForMcc(int mcc) {
        MccEntry entry = entryForMcc(mcc);
        if (entry == null || entry.mIso == null) {
            return null;
        }
        String timeZone = entry.timeZone;
        if (!TextUtils.isEmpty(timeZone)) {
            return timeZone;
        }
        return null;
    }

    public static String custCountryCodeForMcc(int mcc) {
        MccEntry entry = entryForMcc(mcc);
        if (entry == null) {
            return null;
        }
        String iso = entry.mIso;
        if (!TextUtils.isEmpty(iso)) {
            return iso;
        }
        return null;
    }

    public static String custLanguageForMcc(int mcc) {
        MccEntry entry = entryForMcc(mcc);
        if (entry == null) {
            return null;
        }
        String language = entry.mLanguage;
        if (!TextUtils.isEmpty(language)) {
            return language;
        }
        return null;
    }

    public static int custSmallestDigitsMccForMnc(int mcc) {
        MccEntry entry = entryForMcc(mcc);
        if (entry == null) {
            return -1;
        }
        return entry.mSmallestDigitsMnc;
    }

    public static boolean useAutoSimLan(Context context) {
        if (!mEnableSimLang || 1 != Settings.System.getInt(context.getContentResolver(), SIM_LANGUAGE, 0)) {
            return false;
        }
        return true;
    }

    public static Locale getImsiSpecialConfig() {
        if (mImsi == null || !mImsi.startsWith(MCC_MNC_SWEDISH_EX)) {
            if (mIccId == null || mImsi == null || !mImsi.startsWith(PLMN_IUCACELL_MEXICO) || !mIccId.startsWith(ICCID_IUCACELL_MEXICO_PREFIX)) {
                return null;
            }
            Rlog.d(LOG_TAG, "locale set to es_us ,IUCACELL_MEXICO");
            return new Locale("es", "mx");
        } else if (mImsi.startsWith(MCC_MNC_NOWAY_EX)) {
            Rlog.d(LOG_TAG, "locale set to nb_no");
            return new Locale("nb", "no");
        } else {
            Rlog.d(LOG_TAG, "locale set to sv_se");
            return new Locale("sv", "se");
        }
    }

    public static Locale getSpecialLoacleConfig(Context context, int mcc) {
        boolean alwaysPersist = false;
        if (Build.IS_DEBUGGABLE) {
            alwaysPersist = SystemProperties.getBoolean("persist.always.persist.locale", false);
        }
        String persistSysLocale = SystemProperties.get("persist.sys.locale", "");
        if (alwaysPersist || persistSysLocale.isEmpty()) {
            Locale result = getImsiSpecialConfig();
            if (result != null) {
                return result;
            }
            if (mHB_mcc == mcc && true == mHB_defaultlanguage) {
                if (3 == mMnc || mHB_mncMobile == mMnc) {
                    Rlog.d(LOG_TAG, "current mnc is " + mMnc + ", set language to hr and set country to hr");
                    return new Locale("hr", "ba");
                }
                Rlog.i(LOG_TAG, "current mcc 218 mnc is " + mMnc + ", set language and country to cust config");
            }
            HwCustMccTable hwCustMccTable = (HwCustMccTable) HwCustUtils.createObj(HwCustMccTable.class, new Object[0]);
            if (hwCustMccTable != null) {
                Locale custLocale = hwCustMccTable.getCustSpecialLocaleConfig(mImsi);
                if (custLocale != null) {
                    return custLocale;
                }
            }
            return null;
        }
        Rlog.d(LOG_TAG, "getSpecialLoacleConfig: skipping already persisted");
        return null;
    }

    private static boolean isHaveString(String[] strs, String s) {
        if (strs == null) {
            return false;
        }
        for (String indexOf : strs) {
            if (indexOf.indexOf(s) != -1) {
                return true;
            }
        }
        return false;
    }

    private static boolean isStartWithString(String[] strs, String s) {
        if (strs == null) {
            return false;
        }
        for (String startsWith : strs) {
            if (startsWith.startsWith(s)) {
                return true;
            }
        }
        return false;
    }

    public static String getBetterMatchLocale(Context context, String language, String country, String bestMatch) {
        String white_strings = Settings.System.getString(context.getContentResolver(), "white_languages");
        if (white_strings != null && !isStartWithString(white_strings.split(","), language)) {
            return "en_US";
        }
        if ((language + "_" + country).equalsIgnoreCase(bestMatch)) {
            return bestMatch;
        }
        Locale[] availablelocale = Locale.getAvailableLocales();
        int length = availablelocale.length;
        int i = 0;
        while (i < length) {
            Locale loc = availablelocale[i];
            if (loc.getLanguage().length() != 2 || !language.equalsIgnoreCase(loc.getLanguage()) || ((loc.getCountry() == null || !country.equalsIgnoreCase(loc.getCountry())) && (loc.getVariant() == null || !country.equalsIgnoreCase(loc.getVariant())))) {
                i++;
            } else {
                return language + "_" + country;
            }
        }
        if (!"en".equalsIgnoreCase(language)) {
            return bestMatch;
        }
        if (isHaveString(BritianEnglish, country)) {
            return "en_GB";
        }
        return "en_US";
    }

    public static Locale getBetterMatchLocale(Context context, String language, String script, String country, Locale bestMatch) {
        Locale custLocale;
        String white_strings = Settings.System.getString(context.getContentResolver(), "white_languages");
        if (white_strings != null && !isStartWithString(white_strings.split(","), language)) {
            return Locale.forLanguageTag("en_US".replace('_', '-'));
        }
        Rlog.d(LOG_TAG, "getBetterMatchLocale, custLocale: " + custLocale + ", bestMatch: " + bestMatch);
        if (bestMatch != null) {
            if (custLocale.toString().equalsIgnoreCase(bestMatch.toString())) {
                Rlog.d(LOG_TAG, "getBetterMatchLocale, got exact match with script!!");
                return bestMatch;
            }
            if ((language + "_" + country).equalsIgnoreCase(bestMatch.getLanguage() + "_" + bestMatch.getCountry())) {
                Rlog.d(LOG_TAG, "getBetterMatchLocale, got language and country match!!");
                return Locale.forLanguageTag((language + "_" + country).replace('_', '-'));
            }
        }
        Locale[] availablelocale = Locale.getAvailableLocales();
        int length = availablelocale.length;
        int i = 0;
        while (i < length) {
            Locale loc = availablelocale[i];
            if (!language.equalsIgnoreCase(loc.getLanguage()) || ((loc.getCountry() == null || !country.equalsIgnoreCase(loc.getCountry())) && (loc.getVariant() == null || !country.equalsIgnoreCase(loc.getVariant())))) {
                i++;
            } else {
                return Locale.forLanguageTag((language + "_" + country).replace('_', '-'));
            }
        }
        if (!"en".equalsIgnoreCase(language)) {
            return bestMatch;
        }
        if (isHaveString(BritianEnglish, country)) {
            return Locale.forLanguageTag("en_GB".replace('_', '-'));
        }
        return Locale.forLanguageTag("en_US".replace('_', '-'));
    }

    public static String custScriptForMcc(int mcc) {
        MccEntry entry = entryForMcc(mcc);
        if (entry == null) {
            return null;
        }
        String script = entry.script;
        if (!TextUtils.isEmpty(script)) {
            return script;
        }
        return null;
    }

    private static HashMap<String, String[]> parseString(XmlPullParser parser, HashMap<String, String[]> map) throws XmlPullParserException {
        if (map == null || parser == null) {
            return null;
        }
        String attName = parser.getAttributeName(0);
        if (NAME_TZ.equals(attName)) {
            String strValue = parser.getAttributeValue(null, NAME_TZ);
            if (strValue == null) {
                Rlog.e(LOG_TAG, "TimeZone get from cust file is null");
                return null;
            }
            String[] items = strValue.split(",");
            if (items.length <= 0) {
                Rlog.e(LOG_TAG, "number of TimeZone get from cust file is less than zero");
                return null;
            }
            map.put(NAME_TZ, items);
        } else if (NAME_LANG.equals(attName)) {
            String strValue2 = parser.getAttributeValue(null, NAME_LANG);
            if (strValue2 == null) {
                Rlog.e(LOG_TAG, "LanguageCode get from cust file is null");
                return null;
            }
            String[] items2 = strValue2.split(",");
            if (items2.length <= 0) {
                Rlog.e(LOG_TAG, "number of LanguageCode get from cust file is less than zero");
                return null;
            }
            map.put(NAME_LANG, items2);
        } else if (NAME_COUNTRY.equals(attName)) {
            String strValue3 = parser.getAttributeValue(null, NAME_COUNTRY);
            if (strValue3 == null) {
                Rlog.e(LOG_TAG, "CountryCode get from cust file is null");
                return null;
            }
            String[] items3 = strValue3.split(",");
            if (items3.length <= 0) {
                Rlog.e(LOG_TAG, "number of CountryCode get from cust file is less than zero");
                return null;
            }
            map.put(NAME_LANG, items3);
        }
        return map;
    }

    private static HashMap<String, String[]> parseShort(XmlPullParser parser, HashMap<String, String[]> map) throws XmlPullParserException {
        if (map == null || parser == null) {
            return null;
        }
        if (NAME_MCC.equals(parser.getAttributeName(0))) {
            String strValue = parser.getAttributeValue(null, NAME_MCC);
            if (strValue == null) {
                Rlog.e(LOG_TAG, "MCCCode get from cust file is null");
                return null;
            }
            String[] mcc = strValue.split(",");
            if (mcc.length <= 0) {
                Rlog.e(LOG_TAG, "number of MCCCode get from cust file is less than zero");
                return null;
            }
            map.put(NAME_MCC, mcc);
        }
        return map;
    }

    private static HashMap<String, String[]> parseInteger(XmlPullParser parser, HashMap<String, String[]> map) throws XmlPullParserException {
        if (map == null || parser == null) {
            return null;
        }
        if (NAME_IND.equals(parser.getAttributeName(0))) {
            String strValue = parser.getAttributeValue(null, NAME_IND);
            if (strValue == null) {
                Rlog.e(LOG_TAG, "IndCode get from cust file is null");
                return null;
            }
            String[] ind = strValue.split(",");
            if (ind.length <= 0) {
                Rlog.e(LOG_TAG, "number of IndCode get from cust file is less than zero");
                return null;
            }
            map.put(NAME_IND, ind);
        }
        return map;
    }

    private static HashMap<String, String[]> parseElement() {
        String str;
        StringBuilder sb;
        String str2;
        StringBuilder sb2;
        File mccTableFileCust = new File("/data/cust/", "xml/mccTableParse.xml");
        try {
            InputStreamReader mccTableReader = new InputStreamReader(new FileInputStream(mccTableFileCust), Charset.defaultCharset());
            HashMap<String, String[]> itemsMap = new HashMap<>();
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(mccTableReader);
                XmlUtils.beginDocument(parser, "mccTableParse");
                do {
                    XmlUtils.nextElement(parser);
                    String tagName = parser.getName();
                    if ("string".equals(tagName)) {
                        itemsMap = parseString(parser, itemsMap);
                    } else if ("short".equals(tagName)) {
                        itemsMap = parseShort(parser, itemsMap);
                    } else if ("integer".equals(tagName)) {
                        itemsMap = parseInteger(parser, itemsMap);
                    } else {
                        try {
                            mccTableReader.close();
                            return itemsMap;
                        } catch (IOException e) {
                            e2 = e;
                            str2 = LOG_TAG;
                            sb2 = new StringBuilder();
                        }
                    }
                } while (!(itemsMap == null || itemsMap.size() <= 0));
                try {
                    mccTableReader.close();
                    return null;
                } catch (IOException e2) {
                    Rlog.w(LOG_TAG, "Exception in mcctable parser " + e2);
                }
                sb.append("Exception in mcctable parser ");
                sb.append(e2);
                Rlog.w(str, sb.toString());
                return null;
                sb2.append("Exception in mcctable parser ");
                sb2.append(e2);
                Rlog.w(str2, sb2.toString());
                return null;
                return null;
            } catch (XmlPullParserException e3) {
                Rlog.w(LOG_TAG, "Exception in mcctable parser " + e3);
                try {
                    mccTableReader.close();
                    return null;
                } catch (IOException e4) {
                    e2 = e4;
                    str = LOG_TAG;
                    sb = new StringBuilder();
                }
            } catch (IOException e5) {
                Rlog.w(LOG_TAG, "Exception in mcctable parser " + e5);
                try {
                    mccTableReader.close();
                    return null;
                } catch (IOException e6) {
                    e2 = e6;
                    str = LOG_TAG;
                    sb = new StringBuilder();
                }
            } catch (Throwable th) {
                try {
                    mccTableReader.close();
                    throw th;
                } catch (IOException e7) {
                    e2 = e7;
                    str2 = LOG_TAG;
                    sb2 = new StringBuilder();
                }
            }
        } catch (FileNotFoundException e8) {
            Rlog.w(LOG_TAG, "can not open" + mccTableFileCust);
            return null;
        }
    }

    private static boolean loadCustMccTable() {
        HashMap<String, String[]> elementsMap = parseElement();
        if (elementsMap == null) {
            return false;
        }
        String[] tz = elementsMap.get(NAME_TZ);
        String[] lang = elementsMap.get(NAME_LANG);
        String[] country = elementsMap.get(NAME_COUNTRY);
        String[] mcc = elementsMap.get(NAME_MCC);
        String[] ind = elementsMap.get(NAME_IND);
        if (tz == null || lang == null || country == null || mcc == null || ind == null || tz.length <= 0 || lang.length <= 0 || country.length <= 0 || mcc.length <= 0 || ind.length <= 0 || mcc.length != ind.length) {
            Rlog.e(LOG_TAG, "some other exception occured");
            return false;
        }
        short[] mcc_codes = new short[mcc.length];
        for (int i = 0; i < mcc.length; i++) {
            mcc_codes[i] = Short.parseShort(mcc[i]);
        }
        int[] ind_codes = new int[ind.length];
        for (int i2 = 0; i2 < ind.length; i2++) {
            ind_codes[i2] = Integer.parseInt(ind[i2]);
        }
        try {
            sTable = new ArrayList<>(HwFullNetworkConstants.EVENT_CHECK_STATE_BASE);
            int i3 = 0;
            while (i3 < mcc.length) {
                short mccTemp = mcc_codes[i3];
                int indCode = ind_codes[i3];
                String tzTemp = tz[(indCode >>> 4) & 31];
                int countryInd = (indCode >>> 16) & HwSubscriptionManager.SUB_INIT_STATE;
                String countryCodeTemp = country[countryInd];
                String langTemp = lang[(indCode >>> 24) & HwSubscriptionManager.SUB_INIT_STATE];
                int smDigTemp = (indCode >>> 9) & 3;
                ArrayList<MccEntry> arrayList = sTable;
                int[] ind_codes2 = ind_codes;
                try {
                    int i4 = countryInd;
                    MccEntry mccEntry = new MccEntry(mccTemp, countryCodeTemp, smDigTemp, langTemp, tzTemp);
                    arrayList.add(mccEntry);
                    i3++;
                    ind_codes = ind_codes2;
                } catch (ArrayIndexOutOfBoundsException e) {
                    Rlog.w(LOG_TAG, "parse cust mcc table failed, ArrayIndexOutOfBoundsException occured.");
                    return false;
                }
            }
            Collections.sort(sTable);
            Rlog.i(LOG_TAG, "cust file is successfully load into the table");
            return true;
        } catch (ArrayIndexOutOfBoundsException e2) {
            int[] iArr = ind_codes;
            Rlog.w(LOG_TAG, "parse cust mcc table failed, ArrayIndexOutOfBoundsException occured.");
            return false;
        }
    }

    public static void setImsi(String imsi) {
        mImsi = imsi;
    }

    public static void setIccId(String iccid) {
        mIccId = iccid;
    }

    public static void setMnc(int mnc) {
        mMnc = mnc;
    }

    private static File parseFile(String mccTableFilePath) {
        File mccTableFileCustV2;
        if (hwCfgPolicyPath.equals(mccTableFilePath)) {
            try {
                File cfg = HwCfgFilePolicy.getCfgFile("carrier/network/xml/mccTable_V2.xml", 0);
                if (cfg == null) {
                    cfg = HwCfgFilePolicy.getCfgFile("global/mccTable_V2.xml", 0);
                    if (cfg == null) {
                        cfg = HwCfgFilePolicy.getCfgFile("xml/mccTable_V2.xml", 0);
                    }
                }
                if (cfg == null) {
                    return null;
                }
                mccTableFileCustV2 = cfg;
            } catch (NoClassDefFoundError e) {
                Rlog.w(LOG_TAG, "NoClassDefFoundError : HwCfgFilePolicy ");
                return null;
            } catch (NoSuchMethodError e2) {
                Log.e(LOG_TAG, Log.getStackTraceString(e2));
                Rlog.e(LOG_TAG, "[ERROR:NoSuchMethodError] This error may cause local language or timezone uncorrect when starting device with icccard.");
                return null;
            }
        } else {
            mccTableFileCustV2 = new File(mccTableFilePath, "mccTable_V2.xml");
        }
        return mccTableFileCustV2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0052, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0053, code lost:
        r11 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        android.telephony.Rlog.w(LOG_TAG, "Exception in mcctable parser " + r0);
        r0 = 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00bc, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00bd, code lost:
        r6 = r0;
        android.telephony.Rlog.w(LOG_TAG, "Exception in mcctable parser " + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0102, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0129, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x012a, code lost:
        r5 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:?, code lost:
        android.telephony.Rlog.w(LOG_TAG, "Exception in mcctable parser " + r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0142, code lost:
        if (r3 != null) goto L_0x0144;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0148, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0149, code lost:
        r6 = r0;
        r6 = LOG_TAG;
        r7 = new java.lang.StringBuilder();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0153, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0154, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0155, code lost:
        r5 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:?, code lost:
        android.telephony.Rlog.w(LOG_TAG, "Exception in mcctable parser " + r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x016d, code lost:
        if (r3 != null) goto L_0x016f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x0173, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0174, code lost:
        r6 = r0;
        r6 = LOG_TAG;
        r7 = new java.lang.StringBuilder();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x017e, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x0180, code lost:
        if (r3 != null) goto L_0x0182;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x0186, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x0187, code lost:
        r5 = r0;
        r5 = LOG_TAG;
        r6 = new java.lang.StringBuilder();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x0192, code lost:
        throw r0;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0129 A[ExcHandler: IOException (r0v4 'e' java.io.IOException A[CUSTOM_DECLARE]), PHI: r3 
      PHI: (r3v2 'fin' java.io.FileInputStream) = (r3v0 'fin' java.io.FileInputStream), (r3v5 'fin' java.io.FileInputStream), (r3v5 'fin' java.io.FileInputStream), (r3v5 'fin' java.io.FileInputStream), (r3v5 'fin' java.io.FileInputStream) binds: [B:4:0x0011, B:10:0x003e, B:13:0x0046, B:16:0x004d, B:20:0x0056] A[DONT_GENERATE, DONT_INLINE], Splitter:B:4:0x0011] */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0154 A[ExcHandler: XmlPullParserException (r0v1 'e' org.xmlpull.v1.XmlPullParserException A[CUSTOM_DECLARE]), PHI: r3 
      PHI: (r3v1 'fin' java.io.FileInputStream) = (r3v0 'fin' java.io.FileInputStream), (r3v5 'fin' java.io.FileInputStream), (r3v5 'fin' java.io.FileInputStream), (r3v5 'fin' java.io.FileInputStream), (r3v5 'fin' java.io.FileInputStream) binds: [B:4:0x0011, B:10:0x003e, B:13:0x0046, B:16:0x004d, B:20:0x0056] A[DONT_GENERATE, DONT_INLINE], Splitter:B:4:0x0011] */
    private static boolean loadCustMccTableV2(String mccTableFilePath) {
        String str;
        StringBuilder sb;
        String str2;
        StringBuilder sb2;
        File mccTableFileCustV2 = parseFile(mccTableFilePath);
        if (mccTableFileCustV2 == null) {
            return false;
        }
        FileInputStream fin = null;
        ArrayList<MccEntry> tmpTable = new ArrayList<>(HwFullNetworkConstants.EVENT_CHECK_STATE_BASE);
        try {
            fin = new FileInputStream(mccTableFileCustV2);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fin, "UTF-8");
            XmlUtils.beginDocument(parser, MCC_TABLE_V2_PROP_NODE_ROOT);
            while (true) {
                XmlUtils.nextElement(parser);
                boolean invalidLan = true;
                if (MCC_TABLE_V2_PROP_NODE_ENTRY.equalsIgnoreCase(parser.getName())) {
                    int mcc = Integer.parseInt(parser.getAttributeValue(null, "mcc"));
                    String smallestDigitString = parser.getAttributeValue(null, MCC_TABLE_V2_SMALLEST_DIGIT);
                    int smallestDigit = Integer.parseInt(smallestDigitString);
                    String language = parser.getAttributeValue(null, MCC_TABLE_V2_PROP_LANGUAGE);
                    if (language != null) {
                        if (language.trim().length() != 0) {
                            invalidLan = false;
                        }
                    }
                    if (invalidLan) {
                        language = "en";
                    }
                    String language2 = language;
                    String country = parser.getAttributeValue(null, MCC_TABLE_V2_PROP_COUNTRY);
                    if (country == null || country.trim().length() == 0) {
                        country = "us";
                    }
                    String str3 = smallestDigitString;
                    MccEntry mccEntry = new MccEntry(mcc, country, smallestDigit, language2, parser.getAttributeValue(null, MCC_TABLE_V2_PROP_TIME_ZONE), parser.getAttributeValue(null, MCC_TABLE_V2_PROP_SCRIPT));
                    tmpTable.add(mccEntry);
                } else {
                    try {
                        fin.close();
                        Collections.sort(tmpTable);
                        sTable = tmpTable;
                        Rlog.i(LOG_TAG, "cust file is successfully load into the table v2");
                        return true;
                    } catch (IOException e) {
                        e2 = e;
                        IOException iOException = e2;
                        str2 = LOG_TAG;
                        sb2 = new StringBuilder();
                    }
                }
            }
        } catch (XmlPullParserException e2) {
        } catch (IOException e3) {
        } catch (Exception e4) {
            Exception exc = e4;
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e5) {
                    e2 = e5;
                    IOException iOException2 = e2;
                    str = LOG_TAG;
                    sb = new StringBuilder();
                }
            }
            return false;
        }
        sb2.append("Exception in mcctable parser ");
        sb2.append(e2);
        Rlog.w(str2, sb2.toString());
        return false;
        sb.append("Exception in mcctable parser ");
        sb.append(e2);
        Rlog.w(str, sb.toString());
        return false;
    }

    public static void setDefaultTimezone(boolean value) {
        isDefaultTimezone = value;
    }

    public static void setDefaultTimezone(Context context) {
        String timezone = SystemProperties.get(TIMEZONE_PROPERTY);
        if (timezone == null || timezone.length() == 0) {
            String region = SystemProperties.get("ro.product.locale.region");
            if (region != null && region.equals("CN")) {
                ((AlarmManager) context.getSystemService("alarm")).setTimeZone("Asia/Shanghai");
                isDefaultTimezone = true;
                Rlog.d(LOG_TAG, "set default timezone to Asia/Shanghai");
            }
        }
    }

    public static boolean shouldSkipUpdateMccMnc(String mccmnc) {
        try {
            Rlog.d(LOG_TAG, "shouldSkipUpdateMccMnc getSimState(0) = " + TelephonyManager.getDefault().getSimState(0));
            Rlog.d(LOG_TAG, "shouldSkipUpdateMccMnc hasIccCard(0) = " + TelephonyManager.getDefault().hasIccCard(0));
            if (TelephonyManager.getDefault().hasIccCard(0)) {
                String simOperator = TelephonyManager.getDefault().getSimOperator(SubscriptionManager.getSubId(0)[0]);
                Rlog.d(LOG_TAG, "shouldSkipUpdateMccMnc mccmnc = " + mccmnc + " getSimOperator = " + simOperator);
                if (mccmnc != null && !mccmnc.equals(simOperator)) {
                    return true;
                }
            }
            if (HwVSimUtils.isVSimOn()) {
                Rlog.d(LOG_TAG, "shouldSkipUpdateMccMnc mccmnc = " + mccmnc);
                if (mccmnc != null) {
                    Rlog.d(LOG_TAG, "vsim enabled , Skip Update vsim MccMnc");
                    return true;
                }
            }
        } catch (IllegalStateException e) {
            Rlog.d(LOG_TAG, "Default Phones not init yet, shouldSkipUpdateMccMnc return false");
        }
        return false;
    }
}
