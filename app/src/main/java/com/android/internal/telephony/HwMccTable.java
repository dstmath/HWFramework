package com.android.internal.telephony;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.provider.SettingsEx.Systemex;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Locale.Builder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwMccTable {
    private static final String[] BritianEnglish = null;
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
    private static String SIM_LANGUAGE = null;
    protected static final String TIMEZONE_PROPERTY = "persist.sys.timezone";
    private static String custFilePath = null;
    private static String hwCfgPolicyPath = null;
    static boolean isDefaultTimezone = false;
    private static boolean mEnableSimLang = false;
    private static final boolean mHB_defaultlanguage = false;
    private static final int mHB_mcc = 218;
    private static final int mHB_mncEronet = 3;
    private static final int mHB_mncMobile = 90;
    private static final int mHB_mncTel = 5;
    private static String mIccId;
    static String mImsi;
    private static int mMnc;
    static ArrayList<MccEntry> sTable;
    private static String systemFilePath;

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
            boolean z = HwMccTable.mHB_defaultlanguage;
            if (!(o instanceof MccEntry)) {
                return HwMccTable.mHB_defaultlanguage;
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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwMccTable.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwMccTable.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwMccTable.<clinit>():void");
    }

    private static boolean loadCustMccTable() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.HwMccTable.loadCustMccTable():boolean
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.HwMccTable.loadCustMccTable():boolean
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.HwMccTable.loadCustMccTable():boolean");
    }

    private static MccEntry entryForMcc(int mcc) {
        int index = Collections.binarySearch(sTable, new MccEntry(mcc, null, LENGTH_MIN));
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
        if (mEnableSimLang && 1 == System.getInt(context.getContentResolver(), SIM_LANGUAGE, LENGTH_MIN)) {
            return true;
        }
        return mHB_defaultlanguage;
    }

    public static Locale getSpecialLoacleConfig(Context context, int mcc) {
        boolean alwaysPersist = mHB_defaultlanguage;
        if (Build.IS_DEBUGGABLE) {
            alwaysPersist = SystemProperties.getBoolean("persist.always.persist.locale", mHB_defaultlanguage);
        }
        if (!(!alwaysPersist ? SystemProperties.get("persist.sys.locale", "").isEmpty() : true)) {
            Rlog.d(LOG_TAG, "getSpecialLoacleConfig: skipping already persisted");
            return null;
        } else if (mImsi == null || !mImsi.startsWith(MCC_MNC_SWEDISH_EX)) {
            if (mIccId == null || mImsi == null || !mImsi.startsWith(PLMN_IUCACELL_MEXICO) || !mIccId.startsWith(ICCID_IUCACELL_MEXICO_PREFIX)) {
                if (mHB_mcc == mcc && mHB_defaultlanguage) {
                    if (mHB_mncEronet == mMnc || mHB_mncMobile == mMnc) {
                        Rlog.d(LOG_TAG, "current mnc is " + mMnc + ", set language to hr and set country to hr");
                        return new Locale("hr", "ba");
                    }
                    Rlog.i(LOG_TAG, "current mcc 218 mnc is " + mMnc + ", set language and country to cust config");
                }
                HwCustMccTable hwCustMccTable = (HwCustMccTable) HwCustUtils.createObj(HwCustMccTable.class, new Object[LENGTH_MIN]);
                if (hwCustMccTable != null) {
                    Locale custLocale = hwCustMccTable.getCustSpecialLocaleConfig(mImsi);
                    if (custLocale != null) {
                        return custLocale;
                    }
                }
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

    private static boolean isHaveString(String[] strs, String s) {
        if (strs == null) {
            return mHB_defaultlanguage;
        }
        for (int i = LENGTH_MIN; i < strs.length; i++) {
            if (strs[i].indexOf(s) != -1) {
                return true;
            }
        }
        return mHB_defaultlanguage;
    }

    private static boolean isStartWithString(String[] strs, String s) {
        if (strs == null) {
            return mHB_defaultlanguage;
        }
        for (int i = LENGTH_MIN; i < strs.length; i++) {
            if (strs[i].startsWith(s)) {
                return true;
            }
        }
        return mHB_defaultlanguage;
    }

    public static String getBetterMatchLocale(Context context, String language, String country, String bestMatch) {
        String white_strings = Systemex.getString(context.getContentResolver(), "white_languages");
        if (white_strings != null && !isStartWithString(white_strings.split(","), language)) {
            return "en_US";
        }
        if ((language + "_" + country).equalsIgnoreCase(bestMatch)) {
            return bestMatch;
        }
        Locale[] availablelocale = Locale.getAvailableLocales();
        int length = availablelocale.length;
        for (int i = LENGTH_MIN; i < length; i++) {
            Locale loc = availablelocale[i];
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
        String white_strings = Systemex.getString(context.getContentResolver(), "white_languages");
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
        Locale[] availablelocale = Locale.getAvailableLocales();
        int length = availablelocale.length;
        for (int i = LENGTH_MIN; i < length; i++) {
            Locale loc = availablelocale[i];
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

    public static void setImsi(String imsi) {
        mImsi = imsi;
    }

    public static void setIccId(String iccid) {
        mIccId = iccid;
    }

    public static void setMnc(int mnc) {
        mMnc = mnc;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean loadCustMccTableV2(String mccTableFilePath) {
        XmlPullParserException e;
        IOException e2;
        Throwable th;
        File file = new File("/data/cust", "xml/mccTable_V2.xml");
        if (hwCfgPolicyPath.equals(mccTableFilePath)) {
            try {
                File cfg = HwCfgFilePolicy.getCfgFile("xml/mccTable_V2.xml", LENGTH_MIN);
                if (cfg == null) {
                    return mHB_defaultlanguage;
                }
                File mccTableFileCustV2 = cfg;
            } catch (NoClassDefFoundError e3) {
                Rlog.w(LOG_TAG, "NoClassDefFoundError : HwCfgFilePolicy ");
                return mHB_defaultlanguage;
            }
        }
        file = new File(mccTableFilePath, "mccTable_V2.xml");
        FileInputStream fileInputStream = null;
        ArrayList<MccEntry> arrayList = new ArrayList(300);
        try {
            InputStream fileInputStream2 = new FileInputStream(mccTableFileCustV2);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(fileInputStream2, "UTF-8");
                XmlUtils.beginDocument(parser, MCC_TABLE_V2_PROP_NODE_ROOT);
                while (true) {
                    XmlUtils.nextElement(parser);
                    if (!MCC_TABLE_V2_PROP_NODE_ENTRY.equalsIgnoreCase(parser.getName())) {
                        break;
                    }
                    int mcc = Integer.parseInt(parser.getAttributeValue(null, MCC_TABLE_V2_PROP_MCC));
                    int smallestDigit = 2;
                    smallestDigit = Integer.parseInt(parser.getAttributeValue(null, MCC_TABLE_V2_SMALLEST_DIGIT));
                    String language = parser.getAttributeValue(null, MCC_TABLE_V2_PROP_LANGUAGE);
                    if (language == null || language.trim().length() == 0) {
                        language = "en";
                    }
                    String country = parser.getAttributeValue(null, MCC_TABLE_V2_PROP_COUNTRY);
                    if (country == null || country.trim().length() == 0) {
                        country = "us";
                    }
                    arrayList = arrayList;
                    arrayList.add(new MccEntry(mcc, country, smallestDigit, language, parser.getAttributeValue(null, MCC_TABLE_V2_PROP_TIME_ZONE), parser.getAttributeValue(null, MCC_TABLE_V2_PROP_SCRIPT)));
                }
                if (fileInputStream2 != null) {
                    try {
                        fileInputStream2.close();
                    } catch (IOException e22) {
                        Rlog.w(LOG_TAG, "Exception in mcctable parser " + e22);
                        return mHB_defaultlanguage;
                    }
                }
                Collections.sort(arrayList);
                sTable = arrayList;
                Rlog.i(LOG_TAG, "cust file is successfully load into the table v2");
                return true;
            } catch (Exception e4) {
                Rlog.w(LOG_TAG, "Exception in mcctable parser " + e4);
            } catch (XmlPullParserException e5) {
                e = e5;
                fileInputStream = fileInputStream2;
            } catch (IOException e6) {
                e2 = e6;
                fileInputStream = fileInputStream2;
            } catch (Throwable th2) {
                th = th2;
                InputStream fin = fileInputStream2;
            }
        } catch (XmlPullParserException e7) {
            e = e7;
            try {
                Rlog.w(LOG_TAG, "Exception in mcctable parser " + e);
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e222) {
                        Rlog.w(LOG_TAG, "Exception in mcctable parser " + e222);
                        return mHB_defaultlanguage;
                    }
                }
                return mHB_defaultlanguage;
            } catch (Throwable th3) {
                th = th3;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e2222) {
                        Rlog.w(LOG_TAG, "Exception in mcctable parser " + e2222);
                        return mHB_defaultlanguage;
                    }
                }
                throw th;
            }
        } catch (IOException e8) {
            e2 = e8;
            Rlog.w(LOG_TAG, "Exception in mcctable parser " + e2);
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e22222) {
                    Rlog.w(LOG_TAG, "Exception in mcctable parser " + e22222);
                    return mHB_defaultlanguage;
                }
            }
            return mHB_defaultlanguage;
        } catch (Exception e9) {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e222222) {
                    Rlog.w(LOG_TAG, "Exception in mcctable parser " + e222222);
                    return mHB_defaultlanguage;
                }
            }
            return mHB_defaultlanguage;
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
            Rlog.d(LOG_TAG, "shouldSkipUpdateMccMnc getSimState(0) = " + TelephonyManager.getDefault().getSimState(LENGTH_MIN));
            Rlog.d(LOG_TAG, "shouldSkipUpdateMccMnc hasIccCard(0) = " + TelephonyManager.getDefault().hasIccCard(LENGTH_MIN));
            if (TelephonyManager.getDefault().hasIccCard(LENGTH_MIN)) {
                String simOperator = TelephonyManager.getDefault().getSimOperator(SubscriptionManager.getSubId(LENGTH_MIN)[LENGTH_MIN]);
                Rlog.d(LOG_TAG, "shouldSkipUpdateMccMnc mccmnc = " + mccmnc + " getSimOperator = " + simOperator);
                if (!(mccmnc == null || mccmnc.equals(simOperator))) {
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
        return mHB_defaultlanguage;
    }
}
