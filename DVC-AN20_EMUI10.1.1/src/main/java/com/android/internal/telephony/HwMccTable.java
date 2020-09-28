package com.android.internal.telephony;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
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
    private static final String[] BRITIAN_ENGLISH = {"IE", "GB", "BN", "MY", "PG", "NR", "WS", "FJ", "VU", "TO", "MT", "GI"};
    private static final boolean HB_DEFAULT_LANGUAGE = SystemProperties.getBoolean("ro.config.hw.BH_defaultlanguage", false);
    private static final int HB_MCC = 218;
    private static final int HB_MNC_ERONET = 3;
    private static final int HB_MNC_MOBILE = 90;
    private static final int HB_MNC_TEL = 5;
    private static final String ICCID_IUCACELL_MEXICO_PREFIX = "8952050";
    private static final int LENGTH_MIN = 0;
    private static final String LOG_TAG = "HwMccTable";
    private static final String MCC_MNC_NOWAY_EX = "2400768";
    private static final String MCC_MNC_SWEDISH_EX = "24007";
    private static final int MCC_TABLE_SIZE = 300;
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
    private static String mIccId = null;
    static String mImsi = null;
    private static int mMnc;
    static ArrayList<MccEntry> sTable = new ArrayList<>();
    private static String systemFilePath = "/system/etc/";

    static {
        if (loadCustMccTableV2(hwCfgPolicyPath)) {
            Rlog.i(LOG_TAG, "loadCustMccTableV2 from hwCfgPolicyPath sucess");
        } else if (loadCustMccTableV2(custFilePath)) {
            Rlog.i(LOG_TAG, "loadCustMccTableV2 from cust sucess");
        } else if (loadCustMccTableV2(systemFilePath)) {
            Rlog.i(LOG_TAG, "loadCustMccTableV2 from system/etc sucess");
        } else {
            Rlog.i(LOG_TAG, "start loadCustMccTable sucess");
            loadCustMccTable();
        }
    }

    /* access modifiers changed from: package-private */
    public static class MccEntry implements Comparable<MccEntry> {
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
            if (!(o instanceof MccEntry) || this.mMcc - ((MccEntry) o).mMcc != 0) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return this.mMcc;
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
        String str;
        String str2 = mImsi;
        if (str2 == null || !str2.startsWith(MCC_MNC_SWEDISH_EX)) {
            if (mIccId == null || (str = mImsi) == null || !str.startsWith(PLMN_IUCACELL_MEXICO) || !mIccId.startsWith(ICCID_IUCACELL_MEXICO_PREFIX)) {
                return null;
            }
            Rlog.i(LOG_TAG, "locale set to es_us ,IUCACELL_MEXICO");
            return new Locale("es", "mx");
        } else if (mImsi.startsWith(MCC_MNC_NOWAY_EX)) {
            Rlog.i(LOG_TAG, "locale set to nb_no");
            return new Locale("nb", "no");
        } else {
            Rlog.i(LOG_TAG, "locale set to sv_se");
            return new Locale("sv", "se");
        }
    }

    public static Locale getSpecialLoacleConfig(Context context, int mcc) {
        Locale custLocale;
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
            if (mcc == HB_MCC && HB_DEFAULT_LANGUAGE) {
                int i = mMnc;
                if (i == 3 || i == HB_MNC_MOBILE) {
                    Rlog.i(LOG_TAG, "current mnc is " + mMnc + ", set language to hr and set country to hr");
                    return new Locale("hr", "ba");
                }
                Rlog.i(LOG_TAG, "current mcc 218 mnc is " + mMnc + ", set language and country to cust config");
            }
            HwCustMccTable hwCustMccTable = (HwCustMccTable) HwCustUtils.createObj(HwCustMccTable.class, new Object[0]);
            if (hwCustMccTable == null || (custLocale = hwCustMccTable.getCustSpecialLocaleConfig(mImsi)) == null) {
                return null;
            }
            return custLocale;
        }
        Rlog.i(LOG_TAG, "getSpecialLoacleConfig: skipping already persisted");
        return null;
    }

    private static boolean isHaveString(String[] strs, String s) {
        if (strs == null) {
            return false;
        }
        for (String str : strs) {
            if (str.indexOf(s) != -1) {
                return true;
            }
        }
        return false;
    }

    private static boolean isStartWithString(String[] strs, String s) {
        if (strs == null) {
            return false;
        }
        for (String str : strs) {
            if (str.startsWith(s)) {
                return true;
            }
        }
        return false;
    }

    public static String getBetterMatchLocale(Context context, String language, String country, String bestMatch) {
        String whiteStrings = Settings.System.getString(context.getContentResolver(), "white_languages");
        if (!(whiteStrings == null || isStartWithString(whiteStrings.split(","), language))) {
            return "en_US";
        }
        if ((language + "_" + country).equalsIgnoreCase(bestMatch)) {
            return bestMatch;
        }
        Locale[] availableLocale = Locale.getAvailableLocales();
        for (Locale loc : availableLocale) {
            if (loc.getLanguage().length() == 2 && language.equalsIgnoreCase(loc.getLanguage()) && ((loc.getCountry() != null && country.equalsIgnoreCase(loc.getCountry())) || (loc.getVariant() != null && country.equalsIgnoreCase(loc.getVariant())))) {
                return language + "_" + country;
            }
        }
        if (!"en".equalsIgnoreCase(language)) {
            return bestMatch;
        }
        if (isHaveString(BRITIAN_ENGLISH, country)) {
            return "en_GB";
        }
        return "en_US";
    }

    public static Locale getBetterMatchLocale(Context context, String language, String script, String country, Locale bestMatch) {
        String whiteStrings = Settings.System.getString(context.getContentResolver(), "white_languages");
        if (!(whiteStrings == null || isStartWithString(whiteStrings.split(","), language))) {
            return Locale.forLanguageTag("en_US".replace('_', '-'));
        }
        Locale custLocale = new Locale.Builder().setLanguage(language).setScript(script).setRegion(country).build();
        Rlog.i(LOG_TAG, "getBetterMatchLocale, custLocale: " + custLocale + ", bestMatch: " + bestMatch);
        if (bestMatch != null) {
            if (custLocale.toString().equalsIgnoreCase(bestMatch.toString())) {
                Rlog.i(LOG_TAG, "getBetterMatchLocale, got exact match with script!!");
                return bestMatch;
            }
            if ((language + "_" + country).equalsIgnoreCase(bestMatch.getLanguage() + "_" + bestMatch.getCountry())) {
                Rlog.i(LOG_TAG, "getBetterMatchLocale, got language and country match!!");
                return Locale.forLanguageTag((language + "_" + country).replace('_', '-'));
            }
        }
        Locale[] availablelocale = Locale.getAvailableLocales();
        for (Locale loc : availablelocale) {
            if (language.equalsIgnoreCase(loc.getLanguage()) && ((loc.getCountry() != null && country.equalsIgnoreCase(loc.getCountry())) || (loc.getVariant() != null && country.equalsIgnoreCase(loc.getVariant())))) {
                return Locale.forLanguageTag((language + "_" + country).replace('_', '-'));
            }
        }
        if (!"en".equalsIgnoreCase(language)) {
            return bestMatch;
        }
        if (isHaveString(BRITIAN_ENGLISH, country)) {
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
                        } catch (IOException e2) {
                            Rlog.w(LOG_TAG, "Exception in mcctable parser " + e2);
                            return null;
                        }
                    }
                } while (!(itemsMap == null || itemsMap.size() <= 0));
                try {
                    mccTableReader.close();
                    return null;
                } catch (IOException e22) {
                    Rlog.w(LOG_TAG, "Exception in mcctable parser " + e22);
                    return null;
                }
            } catch (XmlPullParserException e) {
                Rlog.w(LOG_TAG, "Exception in mcctable parser " + e);
                try {
                    mccTableReader.close();
                    return null;
                } catch (IOException e23) {
                    Rlog.w(LOG_TAG, "Exception in mcctable parser " + e23);
                    return null;
                }
            } catch (IOException e3) {
                Rlog.w(LOG_TAG, "Exception in mcctable parser " + e3);
                try {
                    mccTableReader.close();
                    return null;
                } catch (IOException e24) {
                    Rlog.w(LOG_TAG, "Exception in mcctable parser " + e24);
                    return null;
                }
            } catch (Throwable e25) {
                try {
                    mccTableReader.close();
                    throw e25;
                } catch (IOException e26) {
                    Rlog.w(LOG_TAG, "Exception in mcctable parser " + e26);
                    return null;
                }
            }
        } catch (FileNotFoundException e4) {
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
        short[] mccCodes = new short[mcc.length];
        for (int i = 0; i < mcc.length; i++) {
            try {
                mccCodes[i] = Short.parseShort(mcc[i]);
            } catch (NumberFormatException e) {
                Rlog.e(LOG_TAG, "NumberFormatException mccCodes.");
            }
        }
        int[] indCodes = new int[ind.length];
        for (int i2 = 0; i2 < ind.length; i2++) {
            try {
                indCodes[i2] = Integer.parseInt(ind[i2]);
            } catch (NumberFormatException e2) {
                Rlog.w(LOG_TAG, "NumberFormatException loadCustMccTable.");
            }
        }
        try {
            sTable = new ArrayList<>((int) MCC_TABLE_SIZE);
            int i3 = 0;
            while (i3 < mcc.length) {
                short s = mccCodes[i3];
                int indCode = indCodes[i3];
                try {
                    sTable.add(new MccEntry(s, country[(indCode >>> 16) & HwSubscriptionManager.SUB_INIT_STATE], (indCode >>> 9) & 3, lang[(indCode >>> 24) & HwSubscriptionManager.SUB_INIT_STATE], tz[(indCode >>> 4) & 31]));
                    i3++;
                    elementsMap = elementsMap;
                } catch (ArrayIndexOutOfBoundsException e3) {
                    Rlog.w(LOG_TAG, "parse cust mcc table failed, ArrayIndexOutOfBoundsException occured.");
                    return false;
                }
            }
            Collections.sort(sTable);
            Rlog.i(LOG_TAG, "cust file is successfully load into the table");
            return true;
        } catch (ArrayIndexOutOfBoundsException e4) {
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
        if (!hwCfgPolicyPath.equals(mccTableFilePath)) {
            return new File(mccTableFilePath, "mccTable_V2.xml");
        }
        try {
            File cfg = HwCfgFilePolicy.getCfgFile("carrier/network/xml/mccTable_V2.xml", 0);
            if (cfg == null && (cfg = HwCfgFilePolicy.getCfgFile("global/mccTable_V2.xml", 0)) == null) {
                cfg = HwCfgFilePolicy.getCfgFile("xml/mccTable_V2.xml", 0);
            }
            if (cfg != null) {
                return cfg;
            }
            return null;
        } catch (NoClassDefFoundError e) {
            Rlog.w(LOG_TAG, "NoClassDefFoundError : HwCfgFilePolicy ");
            return null;
        } catch (NoSuchMethodError e2) {
            Log.e(LOG_TAG, Log.getStackTraceString(e2));
            Rlog.e(LOG_TAG, "[ERROR:NoSuchMethodError] This error may cause local language or timezone uncorrect when starting device with icccard.");
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0098, code lost:
        if (0 != 0) goto L_0x009a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x009f, code lost:
        android.telephony.Rlog.w(com.android.internal.telephony.HwMccTable.LOG_TAG, "IOException in mcctable parser finally");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00a4, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00a6, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        android.telephony.Rlog.w(com.android.internal.telephony.HwMccTable.LOG_TAG, "IOException in mcctable parser ");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00af, code lost:
        if (r5 != null) goto L_0x00b1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:?, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00b6, code lost:
        android.telephony.Rlog.w(com.android.internal.telephony.HwMccTable.LOG_TAG, "IOException in mcctable parser finally");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00bb, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00bd, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00bf, code lost:
        android.telephony.Rlog.w(com.android.internal.telephony.HwMccTable.LOG_TAG, "XmlPullParserException in mcctable parser ");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00c6, code lost:
        if (r5 != null) goto L_0x00c8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:?, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00cd, code lost:
        android.telephony.Rlog.w(com.android.internal.telephony.HwMccTable.LOG_TAG, "IOException in mcctable parser finally");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00d2, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00d4, code lost:
        return false;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00a7 A[ExcHandler: IOException (e java.io.IOException), PHI: r5 
      PHI: (r5v2 'fin' java.io.FileInputStream) = (r5v0 'fin' java.io.FileInputStream), (r5v3 'fin' java.io.FileInputStream), (r5v3 'fin' java.io.FileInputStream) binds: [B:4:0x0015, B:10:0x0041, B:11:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:4:0x0015] */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00be A[ExcHandler: XmlPullParserException (e org.xmlpull.v1.XmlPullParserException), PHI: r5 
      PHI: (r5v1 'fin' java.io.FileInputStream) = (r5v0 'fin' java.io.FileInputStream), (r5v3 'fin' java.io.FileInputStream), (r5v3 'fin' java.io.FileInputStream) binds: [B:4:0x0015, B:10:0x0041, B:11:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:4:0x0015] */
    private static boolean loadCustMccTableV2(String mccTableFilePath) {
        File mccTableFileCustV2 = parseFile(mccTableFilePath);
        if (mccTableFileCustV2 == null) {
            return false;
        }
        FileInputStream fin = null;
        ArrayList<MccEntry> tmpTable = new ArrayList<>((int) MCC_TABLE_SIZE);
        try {
            fin = new FileInputStream(mccTableFileCustV2);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fin, "UTF-8");
            XmlUtils.beginDocument(parser, MCC_TABLE_V2_PROP_NODE_ROOT);
            while (true) {
                XmlUtils.nextElement(parser);
                if (MCC_TABLE_V2_PROP_NODE_ENTRY.equalsIgnoreCase(parser.getName())) {
                    int mcc = Integer.parseInt(parser.getAttributeValue(null, "mcc"));
                    tmpTable.add(new MccEntry(mcc, getCountry(parser), getSmallestDigit(parser), getLanguage(parser), parser.getAttributeValue(null, MCC_TABLE_V2_PROP_TIME_ZONE), parser.getAttributeValue(null, MCC_TABLE_V2_PROP_SCRIPT)));
                } else {
                    try {
                        fin.close();
                        Collections.sort(tmpTable);
                        sTable = tmpTable;
                        Rlog.i(LOG_TAG, "cust file is successfully load into the table v2");
                        return true;
                    } catch (IOException e) {
                        Rlog.w(LOG_TAG, "IOException in mcctable parser finally");
                        return false;
                    }
                }
            }
        } catch (NumberFormatException e2) {
            Rlog.w(LOG_TAG, "NumberFormatException parseInt at loadCustMccTableV2 ");
        } catch (Exception e3) {
            Rlog.w(LOG_TAG, "Exception in mcctable parser at loadCustMccTableV2");
        } catch (XmlPullParserException e4) {
        } catch (IOException e5) {
        } catch (Throwable e22) {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e6) {
                    Rlog.w(LOG_TAG, "IOException in mcctable parser finally");
                    return false;
                }
            }
            throw e22;
        }
    }

    private static int getSmallestDigit(XmlPullParser parser) {
        try {
            return Integer.parseInt(parser.getAttributeValue(null, MCC_TABLE_V2_SMALLEST_DIGIT));
        } catch (NumberFormatException e) {
            Rlog.w(LOG_TAG, "NumberFormatException1 parseInt at loadCustMccTableV2 ");
            return 2;
        } catch (Exception e2) {
            Rlog.w(LOG_TAG, "Exception in mcctable1 parser at loadCustMccTableV2");
            return 2;
        }
    }

    private static String getLanguage(XmlPullParser parser) {
        String language = parser.getAttributeValue(null, MCC_TABLE_V2_PROP_LANGUAGE);
        if (language == null || language.trim().length() == 0) {
            return "en";
        }
        return language;
    }

    private static String getCountry(XmlPullParser parser) {
        String country = parser.getAttributeValue(null, MCC_TABLE_V2_PROP_COUNTRY);
        if (country == null || country.trim().length() == 0) {
            return "us";
        }
        return country;
    }

    public static void setDefaultTimezone(boolean value) {
        isDefaultTimezone = value;
    }

    public static void setDefaultTimezone(Context context) {
        String region;
        String timezone = SystemProperties.get(TIMEZONE_PROPERTY);
        if ((timezone == null || timezone.length() == 0) && (region = SystemProperties.get("ro.product.locale.region")) != null && region.equals("CN")) {
            ((AlarmManager) context.getSystemService("alarm")).setTimeZone("Asia/Shanghai");
            isDefaultTimezone = true;
            Rlog.i(LOG_TAG, "set default timezone");
        }
    }

    public static boolean shouldSkipUpdateMccMnc(String mccmnc) {
        try {
            Rlog.i(LOG_TAG, "shouldSkipUpdateMccMnc getSimState(0) = " + TelephonyManager.getDefault().getSimState(0));
            Rlog.i(LOG_TAG, "shouldSkipUpdateMccMnc hasIccCard(0) = " + TelephonyManager.getDefault().hasIccCard(0));
            if (TelephonyManager.getDefault().hasIccCard(0)) {
                String simOperator = TelephonyManager.getDefault().getSimOperatorNumericForPhone(0);
                Rlog.i(LOG_TAG, "shouldSkipUpdateMccMnc mccmnc = " + mccmnc + " getSimOperator = " + simOperator);
                if (mccmnc != null && !mccmnc.equals(simOperator)) {
                    return true;
                }
            }
            if (HwTelephonyManager.getDefault().isPlatformSupportVsim() && HwVSimUtils.isVSimOn()) {
                Rlog.i(LOG_TAG, "shouldSkipUpdateMccMnc mccmnc = " + mccmnc);
                if (mccmnc != null) {
                    Rlog.i(LOG_TAG, "vsim enabled , Skip Update vsim MccMnc");
                    return true;
                }
            }
        } catch (IllegalStateException e) {
            Rlog.i(LOG_TAG, "Default Phones not init yet, shouldSkipUpdateMccMnc return false");
        }
        return false;
    }
}
