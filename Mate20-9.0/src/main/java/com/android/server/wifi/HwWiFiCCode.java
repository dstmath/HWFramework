package com.android.server.wifi;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwWiFiCCode {
    private static final String COUNTRY_CODE_DEFAULT = "HK";
    private static final int LENGTH_MIN = 0;
    private static final String MCC_TABLE_V2_PROP_COUNTRY = "country";
    private static final String MCC_TABLE_V2_PROP_LANGUAGE = "language";
    private static final String MCC_TABLE_V2_PROP_MCC = "mcc";
    private static final String MCC_TABLE_V2_PROP_NODE_ENTRY = "mcc-entry";
    private static final String MCC_TABLE_V2_PROP_NODE_ROOT = "mcc-table";
    private static final String MCC_TABLE_V2_PROP_TIME_ZONE = "time-zone";
    private static final String MCC_TABLE_V2_SMALLEST_DIGIT = "smallest-digit";
    private static final String NAME_COUNTRY = "COUNTRY_CODES";
    private static final String NAME_IND = "IND_CODES";
    private static final String NAME_LANG = "LANG_STRINGS";
    private static final String NAME_MCC = "MCC_CODES";
    private static final String NAME_TZ = "TZ_STRINGS";
    private static final String PROPERTY_GLOBAL_OPERATOR_NUMERIC = "ril.operator.numeric";
    private static final int SUBSCRIPTION_ID = 0;
    static final String TAG = "HwWiFiCCode";
    static ArrayList<MccEntry> sTable = new ArrayList<>(240);
    private Context mContext;

    static class MccEntry implements Comparable<MccEntry> {
        String mIso;
        int mMcc;

        MccEntry(int mnc, String iso, int smallestDigitsMCC) {
            this(mnc, iso, smallestDigitsMCC, null);
        }

        MccEntry(int mnc, String iso, int smallestDigitsMCC, String language) {
            this(mnc, iso, smallestDigitsMCC, language, null);
        }

        MccEntry(int mnc, String iso, int smallestDigitsMCC, String language, String timeZone) {
            this.mMcc = mnc;
            this.mIso = iso;
        }

        public int compareTo(MccEntry o) {
            if (o == null) {
                return -1;
            }
            return this.mMcc - o.mMcc;
        }

        public boolean equals(Object o) {
            boolean z = true;
            if (this == o) {
                return true;
            }
            if (o == null || !(o instanceof MccEntry)) {
                return false;
            }
            if (this.mMcc != ((MccEntry) o).mMcc) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return (37 * 17) + this.mMcc;
        }
    }

    public HwWiFiCCode(Context context) {
        if (context != null) {
            this.mContext = context;
            loadCustMccTableV2();
        }
    }

    static {
        sTable.add(new MccEntry(HwSelfCureUtils.RESET_LEVEL_LOW_2_RENEW_DHCP, "gr", 2));
        sTable.add(new MccEntry(HwSelfCureUtils.RESET_LEVEL_MIDDLE_REASSOC, "nl", 2));
        sTable.add(new MccEntry(HwSelfCureUtils.RESET_REJECTED_BY_STATIC_IP_ENABLED, "be", 2));
        sTable.add(new MccEntry(HwSelfCureUtils.RESET_LEVEL_DEAUTH_BSSID, "fr", 2));
        sTable.add(new MccEntry(212, "mc", 2));
        sTable.add(new MccEntry(213, "ad", 2));
        sTable.add(new MccEntry(214, "es", 2));
        sTable.add(new MccEntry(216, "hu", 2));
        sTable.add(new MccEntry(218, "ba", 2));
        sTable.add(new MccEntry(219, "hr", 2));
        sTable.add(new MccEntry(220, "rs", 2));
        sTable.add(new MccEntry(222, "it", 2));
        sTable.add(new MccEntry(225, "va", 2));
        sTable.add(new MccEntry(226, "ro", 2));
        sTable.add(new MccEntry(228, "ch", 2));
        sTable.add(new MccEntry(230, "cz", 2));
        sTable.add(new MccEntry(231, "sk", 2));
        sTable.add(new MccEntry(232, "at", 2));
        sTable.add(new MccEntry(234, "gb", 2));
        sTable.add(new MccEntry(235, "gb", 2));
        sTable.add(new MccEntry(238, "dk", 2));
        sTable.add(new MccEntry(240, "se", 2));
        sTable.add(new MccEntry(242, "no", 2));
        sTable.add(new MccEntry(244, "fi", 2));
        sTable.add(new MccEntry(246, "lt", 2));
        sTable.add(new MccEntry(247, "lv", 2));
        sTable.add(new MccEntry(248, "ee", 2));
        sTable.add(new MccEntry(250, "ru", 2));
        sTable.add(new MccEntry(255, "ua", 2));
        sTable.add(new MccEntry(257, "by", 2));
        sTable.add(new MccEntry(259, "md", 2));
        sTable.add(new MccEntry(260, "pl", 2));
        sTable.add(new MccEntry(262, "de", 2));
        sTable.add(new MccEntry(266, "gi", 2));
        sTable.add(new MccEntry(268, "pt", 2));
        sTable.add(new MccEntry(270, "lu", 2));
        sTable.add(new MccEntry(272, "ie", 2));
        sTable.add(new MccEntry(274, "is", 2));
        sTable.add(new MccEntry(276, "al", 2));
        sTable.add(new MccEntry(278, "mt", 2));
        sTable.add(new MccEntry(280, "cy", 2));
        sTable.add(new MccEntry(282, "ge", 2));
        sTable.add(new MccEntry(283, "am", 2));
        sTable.add(new MccEntry(284, "bg", 2));
        sTable.add(new MccEntry(286, "tr", 2));
        sTable.add(new MccEntry(288, "fo", 2));
        sTable.add(new MccEntry(289, "ge", 2));
        sTable.add(new MccEntry(290, "gl", 2));
        sTable.add(new MccEntry(292, "sm", 2));
        sTable.add(new MccEntry(293, "si", 2));
        sTable.add(new MccEntry(294, "mk", 2));
        sTable.add(new MccEntry(295, "li", 2));
        sTable.add(new MccEntry(297, "me", 2));
        sTable.add(new MccEntry(302, "ca", 3));
        sTable.add(new MccEntry(308, "pm", 2));
        sTable.add(new MccEntry(310, "us", 3));
        sTable.add(new MccEntry(311, "us", 3));
        sTable.add(new MccEntry(312, "us", 3));
        sTable.add(new MccEntry(313, "us", 3));
        sTable.add(new MccEntry(314, "us", 3));
        sTable.add(new MccEntry(315, "us", 3));
        sTable.add(new MccEntry(316, "us", 3));
        sTable.add(new MccEntry(330, "pr", 2));
        sTable.add(new MccEntry(332, "vi", 2));
        sTable.add(new MccEntry(334, "mx", 3));
        sTable.add(new MccEntry(338, "jm", 3));
        sTable.add(new MccEntry(340, "gp", 2));
        sTable.add(new MccEntry(342, "bb", 3));
        sTable.add(new MccEntry(344, "ag", 3));
        sTable.add(new MccEntry(346, "ky", 3));
        sTable.add(new MccEntry(348, "vg", 3));
        sTable.add(new MccEntry(350, "bm", 2));
        sTable.add(new MccEntry(352, "gd", 2));
        sTable.add(new MccEntry(354, "ms", 2));
        sTable.add(new MccEntry(356, "kn", 2));
        sTable.add(new MccEntry(358, "lc", 2));
        sTable.add(new MccEntry(360, "vc", 2));
        sTable.add(new MccEntry(362, "ai", 2));
        sTable.add(new MccEntry(363, "aw", 2));
        sTable.add(new MccEntry(364, "bs", 2));
        sTable.add(new MccEntry(365, "ai", 3));
        sTable.add(new MccEntry(366, "dm", 2));
        sTable.add(new MccEntry(368, "cu", 2));
        sTable.add(new MccEntry(370, "do", 2));
        sTable.add(new MccEntry(372, "ht", 2));
        sTable.add(new MccEntry(374, "tt", 2));
        sTable.add(new MccEntry(376, "tc", 2));
        sTable.add(new MccEntry(400, "az", 2));
        sTable.add(new MccEntry(401, "kz", 2));
        sTable.add(new MccEntry(402, "bt", 2));
        sTable.add(new MccEntry(404, "in", 2));
        sTable.add(new MccEntry(405, "in", 2));
        sTable.add(new MccEntry(406, "in", 2));
        sTable.add(new MccEntry(410, "pk", 2));
        sTable.add(new MccEntry(412, "af", 2));
        sTable.add(new MccEntry(413, "lk", 2));
        sTable.add(new MccEntry(414, "mm", 2));
        sTable.add(new MccEntry(415, "lb", 2));
        sTable.add(new MccEntry(416, "jo", 2));
        sTable.add(new MccEntry(417, "sy", 2));
        sTable.add(new MccEntry(418, "iq", 2));
        sTable.add(new MccEntry(419, "kw", 2));
        sTable.add(new MccEntry(420, "sa", 2));
        sTable.add(new MccEntry(421, "ye", 2));
        sTable.add(new MccEntry(422, "om", 2));
        sTable.add(new MccEntry(423, "ps", 2));
        sTable.add(new MccEntry(424, "ae", 2));
        sTable.add(new MccEntry(425, "il", 2));
        sTable.add(new MccEntry(426, "bh", 2));
        sTable.add(new MccEntry(427, "qa", 2));
        sTable.add(new MccEntry(428, "mn", 2));
        sTable.add(new MccEntry(429, "np", 2));
        sTable.add(new MccEntry(430, "ae", 2));
        sTable.add(new MccEntry(431, "ae", 2));
        sTable.add(new MccEntry(432, "ir", 2));
        sTable.add(new MccEntry(434, "uz", 2));
        sTable.add(new MccEntry(436, "tj", 2));
        sTable.add(new MccEntry(437, "kg", 2));
        sTable.add(new MccEntry(438, "tm", 2));
        sTable.add(new MccEntry(440, "jp", 2));
        sTable.add(new MccEntry(441, "jp", 2));
        sTable.add(new MccEntry(450, "kr", 2));
        sTable.add(new MccEntry(452, "vn", 2));
        sTable.add(new MccEntry(454, "hk", 2));
        sTable.add(new MccEntry(455, "mo", 2));
        sTable.add(new MccEntry(456, "kh", 2));
        sTable.add(new MccEntry(457, "la", 2));
        sTable.add(new MccEntry(460, "cn", 2));
        sTable.add(new MccEntry(461, "cn", 2));
        sTable.add(new MccEntry(466, "tw", 2));
        sTable.add(new MccEntry(467, "kp", 2));
        sTable.add(new MccEntry(470, "bd", 2));
        sTable.add(new MccEntry(472, "mv", 2));
        sTable.add(new MccEntry(502, "my", 2));
        sTable.add(new MccEntry(505, "au", 2));
        sTable.add(new MccEntry(510, "id", 2));
        sTable.add(new MccEntry(514, "tl", 2));
        sTable.add(new MccEntry(515, "ph", 2));
        sTable.add(new MccEntry(520, "th", 2));
        sTable.add(new MccEntry(525, "sg", 2));
        sTable.add(new MccEntry(528, "bn", 2));
        sTable.add(new MccEntry(530, "nz", 2));
        sTable.add(new MccEntry(534, "mp", 2));
        sTable.add(new MccEntry(535, "gu", 2));
        sTable.add(new MccEntry(536, "nr", 2));
        sTable.add(new MccEntry(537, "pg", 2));
        sTable.add(new MccEntry(539, "to", 2));
        sTable.add(new MccEntry(540, "sb", 2));
        sTable.add(new MccEntry(541, "vu", 2));
        sTable.add(new MccEntry(542, "fj", 2));
        sTable.add(new MccEntry(543, "wf", 2));
        sTable.add(new MccEntry(544, "as", 2));
        sTable.add(new MccEntry(545, "ki", 2));
        sTable.add(new MccEntry(546, "nc", 2));
        sTable.add(new MccEntry(547, "pf", 2));
        sTable.add(new MccEntry(548, "ck", 2));
        sTable.add(new MccEntry(549, "ws", 2));
        sTable.add(new MccEntry(550, "fm", 2));
        sTable.add(new MccEntry(551, "mh", 2));
        sTable.add(new MccEntry(552, "pw", 2));
        sTable.add(new MccEntry(553, "tv", 2));
        sTable.add(new MccEntry(555, "nu", 2));
        sTable.add(new MccEntry(602, "eg", 2));
        sTable.add(new MccEntry(603, "dz", 2));
        sTable.add(new MccEntry(604, "ma", 2));
        sTable.add(new MccEntry(605, "tn", 2));
        sTable.add(new MccEntry(606, "ly", 2));
        sTable.add(new MccEntry(607, "gm", 2));
        sTable.add(new MccEntry(608, "sn", 2));
        sTable.add(new MccEntry(609, "mr", 2));
        sTable.add(new MccEntry(610, "ml", 2));
        sTable.add(new MccEntry(611, "gn", 2));
        sTable.add(new MccEntry(612, "ci", 2));
        sTable.add(new MccEntry(613, "bf", 2));
        sTable.add(new MccEntry(614, "ne", 2));
        sTable.add(new MccEntry(615, "tg", 2));
        sTable.add(new MccEntry(616, "bj", 2));
        sTable.add(new MccEntry(617, "mu", 2));
        sTable.add(new MccEntry(618, "lr", 2));
        sTable.add(new MccEntry(619, "sl", 2));
        sTable.add(new MccEntry(620, "gh", 2));
        sTable.add(new MccEntry(621, "ng", 2));
        sTable.add(new MccEntry(622, "td", 2));
        sTable.add(new MccEntry(623, "cf", 2));
        sTable.add(new MccEntry(624, "cm", 2));
        sTable.add(new MccEntry(625, "cv", 2));
        sTable.add(new MccEntry(626, "st", 2));
        sTable.add(new MccEntry(627, "gq", 2));
        sTable.add(new MccEntry(628, "ga", 2));
        sTable.add(new MccEntry(629, "cg", 2));
        sTable.add(new MccEntry(630, "cg", 2));
        sTable.add(new MccEntry(631, "ao", 2));
        sTable.add(new MccEntry(632, "gw", 2));
        sTable.add(new MccEntry(633, "sc", 2));
        sTable.add(new MccEntry(634, "sd", 2));
        sTable.add(new MccEntry(635, "rw", 2));
        sTable.add(new MccEntry(636, "et", 2));
        sTable.add(new MccEntry(637, "so", 2));
        sTable.add(new MccEntry(638, "dj", 2));
        sTable.add(new MccEntry(639, "ke", 2));
        sTable.add(new MccEntry(640, "tz", 2));
        sTable.add(new MccEntry(641, "ug", 2));
        sTable.add(new MccEntry(642, "bi", 2));
        sTable.add(new MccEntry(643, "mz", 2));
        sTable.add(new MccEntry(645, "zm", 2));
        sTable.add(new MccEntry(646, "mg", 2));
        sTable.add(new MccEntry(647, "re", 2));
        sTable.add(new MccEntry(648, "zw", 2));
        sTable.add(new MccEntry(649, "na", 2));
        sTable.add(new MccEntry(650, "mw", 2));
        sTable.add(new MccEntry(651, "ls", 2));
        sTable.add(new MccEntry(652, "bw", 2));
        sTable.add(new MccEntry(653, "sz", 2));
        sTable.add(new MccEntry(654, "km", 2));
        sTable.add(new MccEntry(655, "za", 2));
        sTable.add(new MccEntry(657, "er", 2));
        sTable.add(new MccEntry(658, "sh", 2));
        sTable.add(new MccEntry(659, "ss", 2));
        sTable.add(new MccEntry(702, "bz", 2));
        sTable.add(new MccEntry(704, "gt", 2));
        sTable.add(new MccEntry(706, "sv", 2));
        sTable.add(new MccEntry(708, "hn", 3));
        sTable.add(new MccEntry(710, "ni", 2));
        sTable.add(new MccEntry(712, "cr", 2));
        sTable.add(new MccEntry(714, "pa", 2));
        sTable.add(new MccEntry(716, "pe", 2));
        sTable.add(new MccEntry(722, "ar", 3));
        sTable.add(new MccEntry(724, "br", 2));
        sTable.add(new MccEntry(730, "cl", 2));
        sTable.add(new MccEntry(732, "co", 3));
        sTable.add(new MccEntry(734, "ve", 2));
        sTable.add(new MccEntry(736, "bo", 2));
        sTable.add(new MccEntry(738, "gy", 2));
        sTable.add(new MccEntry(740, "ec", 2));
        sTable.add(new MccEntry(742, "gf", 2));
        sTable.add(new MccEntry(744, "py", 2));
        sTable.add(new MccEntry(746, "sr", 2));
        sTable.add(new MccEntry(748, "uy", 2));
        sTable.add(new MccEntry(750, "fk", 2));
        Collections.sort(sTable);
    }

    public String getActiveCountryCode() {
        if ("factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
            String countryCode = SystemProperties.get("persist.factory.wifi_country_code", "UNKNOWN");
            Log.d(TAG, "factory wifi country code: " + countryCode);
            if (!countryCode.equals("UNKNOWN") && countryCode.length() == 2) {
                String countryCode2 = countryCode.toUpperCase(Locale.ENGLISH);
                Log.d(TAG, "countryCode got from by factory");
                return countryCode2;
            }
        }
        String countryCode3 = getCountryCodeByMCC();
        if (countryCode3 == null || countryCode3.isEmpty()) {
            String countryCode4 = Settings.Global.getString(this.mContext.getContentResolver(), "wifi_country_code");
            if (countryCode4 == null || countryCode4.isEmpty()) {
                String countryCode5 = getCCodeByLocaleLanguage();
                if (countryCode5 == null || countryCode5.isEmpty()) {
                    Log.d(TAG, "countryCode got by DEFAULT");
                    return COUNTRY_CODE_DEFAULT;
                }
                String countryCode6 = countryCode5.toUpperCase(Locale.ENGLISH);
                Log.d(TAG, "countryCode got by LOCALE_LANGUAGE");
                return countryCode6;
            }
            String countryCode7 = countryCode4.toUpperCase(Locale.ENGLISH);
            Log.d(TAG, "countryCode got by RECORD");
            return countryCode7;
        }
        String countryCode8 = countryCode3.toUpperCase(Locale.ENGLISH);
        Log.d(TAG, "countryCode got from by MCC");
        return countryCode8;
    }

    private static MccEntry entryForMcc(int mcc) {
        int index = Collections.binarySearch(sTable, new MccEntry(mcc, null, 0));
        if (index < 0) {
            return null;
        }
        return sTable.get(index);
    }

    private static String countryCodeForMcc(int integerMcc) {
        MccEntry entry = entryForMcc(integerMcc);
        if (entry == null) {
            return "";
        }
        return entry.mIso;
    }

    private int getNetworkMCC() {
        String regPlmnMcc = "";
        String residentPlmn = SystemProperties.get(PROPERTY_GLOBAL_OPERATOR_NUMERIC, "");
        if (this.mContext != null) {
            String regPlmn1 = TelephonyManager.from(this.mContext).getNetworkOperator(0);
            if (regPlmn1 != null && regPlmn1.length() >= 3) {
                regPlmnMcc = regPlmn1.substring(0, 3);
            }
        }
        if (residentPlmn == null || residentPlmn.length() < 3) {
            return 0;
        }
        String residentPlmnMcc = residentPlmn.substring(0, 3);
        if (!"".equals(regPlmnMcc) && regPlmnMcc.length() == 3 && !regPlmnMcc.equals(residentPlmnMcc)) {
            residentPlmnMcc = regPlmnMcc;
        }
        try {
            return Integer.parseInt(residentPlmnMcc);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String getCountryCodeByMCC() {
        int currentMcc = getNetworkMCC();
        if (currentMcc != 0) {
            return countryCodeForMcc(currentMcc);
        }
        return null;
    }

    private String getCCodeByLocaleLanguage() {
        String clocale = Locale.getDefault().getCountry().toLowerCase(Locale.ENGLISH);
        if (clocale.isEmpty() || clocale.length() != 2) {
            return null;
        }
        return clocale;
    }

    private static boolean loadCustMccTableV2() {
        String str;
        StringBuilder sb;
        String str2;
        StringBuilder sb2;
        int smallestDigit;
        File mccTableFileCustV2 = HwCfgFilePolicy.getCfgFile("carrier/network/xml/mccTable_V2.xml", 0);
        if (mccTableFileCustV2 == null) {
            mccTableFileCustV2 = HwCfgFilePolicy.getCfgFile("global/mccTable_V2.xml", 0);
            if (mccTableFileCustV2 == null) {
                mccTableFileCustV2 = HwCfgFilePolicy.getCfgFile("xml/mccTable_V2.xml", 0);
            }
        }
        File mccTableFileCustV22 = mccTableFileCustV2;
        if (mccTableFileCustV22 == null) {
            Slog.w(TAG, "get file mccTableFileCustV2 failed!");
            return false;
        }
        FileInputStream fin = null;
        ArrayList<MccEntry> tmpTable = new ArrayList<>(300);
        try {
            fin = new FileInputStream(mccTableFileCustV22);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fin, "UTF-8");
            XmlUtils.beginDocument(parser, MCC_TABLE_V2_PROP_NODE_ROOT);
            while (true) {
                XmlUtils.nextElement(parser);
                if (MCC_TABLE_V2_PROP_NODE_ENTRY.equalsIgnoreCase(parser.getName())) {
                    try {
                        int mcc = Integer.parseInt(parser.getAttributeValue(null, MCC_TABLE_V2_PROP_MCC));
                        try {
                            smallestDigit = Integer.parseInt(parser.getAttributeValue(null, MCC_TABLE_V2_SMALLEST_DIGIT));
                        } catch (Exception e) {
                            Exception exc = e;
                            Slog.w(TAG, "Exception in mcctable parser " + e);
                            smallestDigit = 2;
                        }
                        String language = parser.getAttributeValue(null, MCC_TABLE_V2_PROP_LANGUAGE);
                        if (language == null || language.trim().length() == 0) {
                            language = "en";
                        }
                        String language2 = language;
                        String country = parser.getAttributeValue(null, MCC_TABLE_V2_PROP_COUNTRY);
                        if (country == null || country.trim().length() == 0) {
                            country = "us";
                        }
                        MccEntry mccEntry = new MccEntry(mcc, country, smallestDigit, language2, parser.getAttributeValue(null, MCC_TABLE_V2_PROP_TIME_ZONE));
                        tmpTable.add(mccEntry);
                    } catch (Exception e2) {
                        Exception exc2 = e2;
                        Slog.w(TAG, "Exception in mcctable parser " + e2);
                    }
                } else {
                    try {
                        fin.close();
                        Collections.sort(tmpTable);
                        sTable = tmpTable;
                        Slog.i(TAG, "cust file is successfully load into the table v2");
                        return true;
                    } catch (IOException e3) {
                        e2 = e3;
                        IOException iOException = e2;
                        str2 = TAG;
                        sb2 = new StringBuilder();
                    }
                }
            }
        } catch (XmlPullParserException e4) {
            Slog.w(TAG, "Exception in mcctable parser " + e);
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e5) {
                    e2 = e5;
                    IOException iOException2 = e2;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
            return false;
        } catch (IOException e6) {
            Slog.w(TAG, "Exception in mcctable parser " + e);
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e7) {
                    e2 = e7;
                    IOException iOException3 = e2;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
            return false;
        } catch (Throwable th) {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e8) {
                    e2 = e8;
                    IOException iOException4 = e2;
                    str2 = TAG;
                    sb2 = new StringBuilder();
                }
            }
            throw th;
        }
        sb.append("Exception in mcctable parser ");
        sb.append(e2);
        Slog.w(str, sb.toString());
        return false;
        sb2.append("Exception in mcctable parser ");
        sb2.append(e2);
        Slog.w(str2, sb2.toString());
        return false;
    }
}
