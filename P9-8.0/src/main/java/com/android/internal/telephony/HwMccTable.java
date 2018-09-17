package com.android.internal.telephony;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
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
import java.util.Locale.Builder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwMccTable {
    private static final String[] BritianEnglish = new String[]{"IE", "GB", "BN", "MY", "PG", "NR", "WS", "FJ", "VU", "TO", "MT", "GI"};
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
    static ArrayList<MccEntry> sTable = new ArrayList();
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

        MccEntry(int mnc, String iso, int smallestDigitsMCC, String language, String timeZone) {
            this(mnc, iso, smallestDigitsMCC, language, timeZone, null);
        }

        MccEntry(int mnc, String iso, int smallestDigitsMCC, String language, String timeZone, String script) {
            this.mMcc = mnc;
            this.mIso = iso;
            this.mSmallestDigitsMnc = smallestDigitsMCC;
            this.mLanguage = language;
            this.timeZone = timeZone;
            this.script = script;
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
        return (MccEntry) sTable.get(index);
    }

    public static String custTimeZoneForMcc(int mcc) {
        MccEntry entry = entryForMcc(mcc);
        if (entry == null || entry.mIso == null) {
            return null;
        }
        String timeZone = entry.timeZone;
        if (TextUtils.isEmpty(timeZone)) {
            return null;
        }
        return timeZone;
    }

    public static String custCountryCodeForMcc(int mcc) {
        MccEntry entry = entryForMcc(mcc);
        if (entry == null) {
            return null;
        }
        String iso = entry.mIso;
        if (TextUtils.isEmpty(iso)) {
            return null;
        }
        return iso;
    }

    public static String custLanguageForMcc(int mcc) {
        MccEntry entry = entryForMcc(mcc);
        if (entry == null) {
            return null;
        }
        String language = entry.mLanguage;
        if (TextUtils.isEmpty(language)) {
            return null;
        }
        return language;
    }

    public static int custSmallestDigitsMccForMnc(int mcc) {
        MccEntry entry = entryForMcc(mcc);
        if (entry == null) {
            return -1;
        }
        return entry.mSmallestDigitsMnc;
    }

    public static boolean useAutoSimLan(Context context) {
        if (mEnableSimLang && 1 == System.getInt(context.getContentResolver(), SIM_LANGUAGE, 0)) {
            return true;
        }
        return false;
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
        if (!alwaysPersist ? SystemProperties.get("persist.sys.locale", "").isEmpty() : true) {
            Locale result = getImsiSpecialConfig();
            if (result != null) {
                return result;
            }
            if (mHB_mcc == mcc && mHB_defaultlanguage) {
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
        String white_strings = System.getString(context.getContentResolver(), "white_languages");
        if (white_strings != null && !isStartWithString(white_strings.split(","), language)) {
            return "en_US";
        }
        if ((language + "_" + country).equalsIgnoreCase(bestMatch)) {
            return bestMatch;
        }
        for (Locale loc : Locale.getAvailableLocales()) {
            if (loc.getLanguage().length() == 2 && language.equalsIgnoreCase(loc.getLanguage()) && ((loc.getCountry() != null && country.equalsIgnoreCase(loc.getCountry())) || (loc.getVariant() != null && country.equalsIgnoreCase(loc.getVariant())))) {
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
        String white_strings = System.getString(context.getContentResolver(), "white_languages");
        if (white_strings != null && !isStartWithString(white_strings.split(","), language)) {
            return Locale.forLanguageTag("en_US".replace('_', '-'));
        }
        Locale custLocale = new Builder().setLanguage(language).setScript(script).setRegion(country).build();
        Rlog.d(LOG_TAG, "getBetterMatchLocale, custLocale: " + custLocale + ", bestMatch: " + bestMatch);
        if (bestMatch != null) {
            if (custLocale.toString().equalsIgnoreCase(bestMatch.toString())) {
                Rlog.d(LOG_TAG, "getBetterMatchLocale, got exact match with script!!");
                return bestMatch;
            } else if ((language + "_" + country).equalsIgnoreCase(bestMatch.getLanguage() + "_" + bestMatch.getCountry())) {
                Rlog.d(LOG_TAG, "getBetterMatchLocale, got language and country match!!");
                return Locale.forLanguageTag((language + "_" + country).replace('_', '-'));
            }
        }
        for (Locale loc : Locale.getAvailableLocales()) {
            if (language.equalsIgnoreCase(loc.getLanguage()) && ((loc.getCountry() != null && country.equalsIgnoreCase(loc.getCountry())) || (loc.getVariant() != null && country.equalsIgnoreCase(loc.getVariant())))) {
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
        if (TextUtils.isEmpty(script)) {
            return null;
        }
        return script;
    }

    private static HashMap<String, String[]> parseString(XmlPullParser parser, HashMap<String, String[]> map) throws XmlPullParserException {
        if (map == null || parser == null) {
            return null;
        }
        String attName = parser.getAttributeName(0);
        String strValue;
        String[] items;
        if (NAME_TZ.equals(attName)) {
            strValue = parser.getAttributeValue(null, NAME_TZ);
            if (strValue == null) {
                Rlog.e(LOG_TAG, "TimeZone get from cust file is null");
                return null;
            }
            items = strValue.split(",");
            if (items.length <= 0) {
                Rlog.e(LOG_TAG, "number of TimeZone get from cust file is less than zero");
                return null;
            }
            map.put(NAME_TZ, items);
        } else if (NAME_LANG.equals(attName)) {
            strValue = parser.getAttributeValue(null, NAME_LANG);
            if (strValue == null) {
                Rlog.e(LOG_TAG, "LanguageCode get from cust file is null");
                return null;
            }
            items = strValue.split(",");
            if (items.length <= 0) {
                Rlog.e(LOG_TAG, "number of LanguageCode get from cust file is less than zero");
                return null;
            }
            map.put(NAME_LANG, items);
        } else if (NAME_COUNTRY.equals(attName)) {
            strValue = parser.getAttributeValue(null, NAME_COUNTRY);
            if (strValue == null) {
                Rlog.e(LOG_TAG, "CountryCode get from cust file is null");
                return null;
            }
            items = strValue.split(",");
            if (items.length <= 0) {
                Rlog.e(LOG_TAG, "number of CountryCode get from cust file is less than zero");
                return null;
            }
            map.put(NAME_LANG, items);
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
            HashMap<String, String[]> itemsMap = new HashMap();
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(mccTableReader);
                XmlUtils.beginDocument(parser, "mccTableParse");
                while (true) {
                    boolean fail;
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
                    if (itemsMap == null || itemsMap.size() <= 0) {
                        fail = true;
                        continue;
                    } else {
                        fail = false;
                        continue;
                    }
                    if (fail) {
                        break;
                    }
                }
                return null;
            } catch (XmlPullParserException e) {
                Rlog.w(LOG_TAG, "Exception in mcctable parser " + e);
                return null;
            } catch (IOException e3) {
                Rlog.w(LOG_TAG, "Exception in mcctable parser " + e3);
                return null;
            } finally {
                try {
                    mccTableReader.close();
                } catch (IOException e22) {
                    Rlog.w(LOG_TAG, "Exception in mcctable parser " + e22);
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
        String[] tz = (String[]) elementsMap.get(NAME_TZ);
        String[] lang = (String[]) elementsMap.get(NAME_LANG);
        String[] country = (String[]) elementsMap.get(NAME_COUNTRY);
        String[] mcc = (String[]) elementsMap.get(NAME_MCC);
        String[] ind = (String[]) elementsMap.get(NAME_IND);
        boolean invalid = (tz == null || lang == null || country == null || mcc == null || ind == null || tz.length <= 0 || lang.length <= 0 || country.length <= 0 || mcc.length <= 0 || ind.length <= 0) ? true : mcc.length != ind.length;
        if (invalid) {
            Rlog.e(LOG_TAG, "some other exception occured");
            return false;
        }
        int i;
        short[] mcc_codes = new short[mcc.length];
        for (i = 0; i < mcc.length; i++) {
            mcc_codes[i] = Short.parseShort(mcc[i]);
        }
        int[] ind_codes = new int[ind.length];
        for (i = 0; i < ind.length; i++) {
            ind_codes[i] = Integer.parseInt(ind[i]);
        }
        try {
            sTable = new ArrayList(300);
            for (i = 0; i < mcc.length; i++) {
                int mccTemp = mcc_codes[i];
                int indCode = ind_codes[i];
                int smDigTemp = (indCode >>> 9) & 3;
                sTable.add(new MccEntry(mccTemp, country[(indCode >>> 16) & HwSubscriptionManager.SUB_INIT_STATE], smDigTemp, lang[(indCode >>> 24) & HwSubscriptionManager.SUB_INIT_STATE], tz[(indCode >>> 4) & 31]));
            }
            Collections.sort(sTable);
            Rlog.i(LOG_TAG, "cust file is successfully load into the table");
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
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
                File cfg = HwCfgFilePolicy.getCfgFile("global/mccTable_V2.xml", 0);
                if (cfg == null) {
                    cfg = HwCfgFilePolicy.getCfgFile("xml/mccTable_V2.xml", 0);
                }
                if (cfg == null) {
                    return null;
                }
                mccTableFileCustV2 = cfg;
            } catch (NoClassDefFoundError e) {
                Rlog.w(LOG_TAG, "NoClassDefFoundError : HwCfgFilePolicy ");
                return null;
            }
        }
        mccTableFileCustV2 = new File(mccTableFilePath, "mccTable_V2.xml");
        return mccTableFileCustV2;
    }

    /* JADX WARNING: Removed duplicated region for block: B:34:0x00c3 A:{Splitter: B:7:0x0019, ExcHandler: org.xmlpull.v1.XmlPullParserException (e org.xmlpull.v1.XmlPullParserException)} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x010b A:{Splitter: B:7:0x0019, ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x0222 A:{Splitter: B:7:0x0019, ExcHandler: all (th java.lang.Throwable)} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00c3 A:{Splitter: B:7:0x0019, ExcHandler: org.xmlpull.v1.XmlPullParserException (e org.xmlpull.v1.XmlPullParserException)} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x010b A:{Splitter: B:7:0x0019, ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x0222 A:{Splitter: B:7:0x0019, ExcHandler: all (th java.lang.Throwable)} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:34:0x00c3, code:
            r11 = e;
     */
    /* JADX WARNING: Missing block: B:35:0x00c4, code:
            r13 = r14;
     */
    /* JADX WARNING: Missing block: B:43:0x00ea, code:
            r10 = move-exception;
     */
    /* JADX WARNING: Missing block: B:45:?, code:
            android.telephony.Rlog.w(LOG_TAG, "Exception in mcctable parser " + r10);
     */
    /* JADX WARNING: Missing block: B:47:0x010b, code:
            r9 = e;
     */
    /* JADX WARNING: Missing block: B:48:0x010c, code:
            r13 = r14;
     */
    /* JADX WARNING: Missing block: B:60:0x0154, code:
            r13 = r14;
     */
    /* JADX WARNING: Missing block: B:92:0x0222, code:
            r2 = th;
     */
    /* JADX WARNING: Missing block: B:93:0x0223, code:
            r13 = r14;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean loadCustMccTableV2(String mccTableFilePath) {
        File mccTableFileCustV2 = parseFile(mccTableFilePath);
        if (mccTableFileCustV2 == null) {
            return false;
        }
        FileInputStream fin = null;
        ArrayList<MccEntry> arrayList = new ArrayList(300);
        try {
            FileInputStream fin2 = new FileInputStream(mccTableFileCustV2);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(fin2, "UTF-8");
                XmlUtils.beginDocument(parser, MCC_TABLE_V2_PROP_NODE_ROOT);
                while (true) {
                    XmlUtils.nextElement(parser);
                    if (MCC_TABLE_V2_PROP_NODE_ENTRY.equalsIgnoreCase(parser.getName())) {
                        int mcc = Integer.parseInt(parser.getAttributeValue(null, "mcc"));
                        int smallestDigit = 2;
                        smallestDigit = Integer.parseInt(parser.getAttributeValue(null, MCC_TABLE_V2_SMALLEST_DIGIT));
                        String language = parser.getAttributeValue(null, MCC_TABLE_V2_PROP_LANGUAGE);
                        boolean invalidLan = language == null || language.trim().length() == 0;
                        if (invalidLan) {
                            language = "en";
                        }
                        String country = parser.getAttributeValue(null, MCC_TABLE_V2_PROP_COUNTRY);
                        if (country == null || country.trim().length() == 0) {
                            country = "us";
                        }
                        arrayList.add(new MccEntry(mcc, country, smallestDigit, language, parser.getAttributeValue(null, MCC_TABLE_V2_PROP_TIME_ZONE), parser.getAttributeValue(null, MCC_TABLE_V2_PROP_SCRIPT)));
                    } else {
                        if (fin2 != null) {
                            try {
                                fin2.close();
                            } catch (IOException e2) {
                                Rlog.w(LOG_TAG, "Exception in mcctable parser " + e2);
                                return false;
                            }
                        }
                        Collections.sort(arrayList);
                        sTable = arrayList;
                        Rlog.i(LOG_TAG, "cust file is successfully load into the table v2");
                        return true;
                    }
                }
            } catch (Exception e) {
                Rlog.w(LOG_TAG, "Exception in mcctable parser " + e);
            } catch (XmlPullParserException e3) {
            } catch (IOException e4) {
            } catch (Throwable th) {
            }
        } catch (XmlPullParserException e5) {
            XmlPullParserException e6 = e5;
            try {
                Rlog.w(LOG_TAG, "Exception in mcctable parser " + e6);
                if (fin != null) {
                    try {
                        fin.close();
                    } catch (IOException e22) {
                        Rlog.w(LOG_TAG, "Exception in mcctable parser " + e22);
                        return false;
                    }
                }
                return false;
            } catch (Throwable th2) {
                Throwable th3 = th2;
                if (fin != null) {
                    try {
                        fin.close();
                    } catch (IOException e222) {
                        Rlog.w(LOG_TAG, "Exception in mcctable parser " + e222);
                        return false;
                    }
                }
                throw th3;
            }
        } catch (IOException e7) {
            IOException e8 = e7;
            Rlog.w(LOG_TAG, "Exception in mcctable parser " + e8);
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e2222) {
                    Rlog.w(LOG_TAG, "Exception in mcctable parser " + e2222);
                    return false;
                }
            }
            return false;
        } catch (Exception e9) {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e22222) {
                    Rlog.w(LOG_TAG, "Exception in mcctable parser " + e22222);
                    return false;
                }
            }
            return false;
        }
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
                if (!(mccmnc == null || (mccmnc.equals(simOperator) ^ 1) == 0)) {
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
