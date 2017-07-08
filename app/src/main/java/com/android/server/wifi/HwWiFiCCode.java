package com.android.server.wifi;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.Global;
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
    static ArrayList<MccEntry> sTable;
    private Context mContext;
    private Locale mLocale;

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
            return this.mMcc + 629;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.HwWiFiCCode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.HwWiFiCCode.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.HwWiFiCCode.<clinit>():void");
    }

    public HwWiFiCCode(Context context) {
        this.mLocale = Locale.getDefault();
        if (context != null) {
            this.mContext = context;
            loadCustMccTableV2();
        }
    }

    public String getActiveCountryCode() {
        String countryCode = getCountryCodeByMCC();
        if (countryCode == null || countryCode.isEmpty()) {
            countryCode = Global.getString(this.mContext.getContentResolver(), "wifi_country_code");
            if (countryCode == null || countryCode.isEmpty()) {
                countryCode = getCCodeByLocaleLanguage();
                if (countryCode == null || countryCode.isEmpty()) {
                    countryCode = COUNTRY_CODE_DEFAULT;
                    Log.d(TAG, "countryCode got by DEFAULT == " + countryCode);
                    return countryCode;
                }
                countryCode = countryCode.toUpperCase(this.mLocale);
                Log.d(TAG, "countryCode got by LOCALE_LANGUAGE == " + countryCode);
                return countryCode;
            }
            countryCode = countryCode.toUpperCase(this.mLocale);
            Log.d(TAG, "countryCode got by RECORD == " + countryCode);
            return countryCode;
        }
        countryCode = countryCode.toUpperCase(this.mLocale);
        Log.d(TAG, "countryCode got from by MCC == " + countryCode);
        return countryCode;
    }

    private static MccEntry entryForMcc(int mcc) {
        int index = Collections.binarySearch(sTable, new MccEntry(mcc, null, SUBSCRIPTION_ID));
        if (index < 0) {
            return null;
        }
        return (MccEntry) sTable.get(index);
    }

    private static String countryCodeForMcc(int integerMcc) {
        MccEntry entry = entryForMcc(integerMcc);
        if (entry == null) {
            return "";
        }
        return entry.mIso;
    }

    private int getNetworkMCC() {
        int integerMcc = SUBSCRIPTION_ID;
        String regPlmnMcc = "";
        String residentPlmn = SystemProperties.get(PROPERTY_GLOBAL_OPERATOR_NUMERIC, "");
        if (this.mContext != null) {
            String regPlmn1 = TelephonyManager.from(this.mContext).getNetworkOperator(SUBSCRIPTION_ID);
            if (regPlmn1 != null && regPlmn1.length() >= 3) {
                regPlmnMcc = regPlmn1.substring(SUBSCRIPTION_ID, 3);
            }
        }
        if (residentPlmn == null || residentPlmn.length() < 3) {
            return integerMcc;
        }
        String residentPlmnMcc = residentPlmn.substring(SUBSCRIPTION_ID, 3);
        if (!("".equals(regPlmnMcc) || regPlmnMcc.length() != 3 || regPlmnMcc.equals(residentPlmnMcc))) {
            residentPlmnMcc = regPlmnMcc;
        }
        try {
            return Integer.parseInt(residentPlmnMcc);
        } catch (NumberFormatException e) {
            return integerMcc;
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
        String clocale = Locale.getDefault().getCountry().toLowerCase(this.mLocale);
        if (clocale.isEmpty() || clocale.length() != 2) {
            return null;
        }
        return clocale;
    }

    private static boolean loadCustMccTableV2() {
        XmlPullParserException e;
        IOException e2;
        NullPointerException e3;
        Throwable th;
        File mccTableFileCustV2 = HwCfgFilePolicy.getCfgFile("xml/mccTable_V2.xml", SUBSCRIPTION_ID);
        FileInputStream fileInputStream = null;
        ArrayList<MccEntry> arrayList = new ArrayList(TCPIpqRtt.RTT_FINE_5);
        try {
            FileInputStream fin = new FileInputStream(mccTableFileCustV2);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(fin, "UTF-8");
                XmlUtils.beginDocument(parser, MCC_TABLE_V2_PROP_NODE_ROOT);
                while (true) {
                    XmlUtils.nextElement(parser);
                    if (!MCC_TABLE_V2_PROP_NODE_ENTRY.equalsIgnoreCase(parser.getName())) {
                        break;
                    }
                    try {
                        int mcc = Integer.parseInt(parser.getAttributeValue(null, MCC_TABLE_V2_PROP_MCC));
                        int smallestDigit = 2;
                        try {
                            smallestDigit = Integer.parseInt(parser.getAttributeValue(null, MCC_TABLE_V2_SMALLEST_DIGIT));
                        } catch (Exception e4) {
                            Slog.w(TAG, "Exception in mcctable parser " + e4);
                        }
                        String language = parser.getAttributeValue(null, MCC_TABLE_V2_PROP_LANGUAGE);
                        if (language == null || language.trim().length() == 0) {
                            language = "en";
                        }
                        String country = parser.getAttributeValue(null, MCC_TABLE_V2_PROP_COUNTRY);
                        if (country == null || country.trim().length() == 0) {
                            country = "us";
                        }
                        arrayList = arrayList;
                        arrayList.add(new MccEntry(mcc, country, smallestDigit, language, parser.getAttributeValue(null, MCC_TABLE_V2_PROP_TIME_ZONE)));
                    } catch (Exception e42) {
                        Slog.w(TAG, "Exception in mcctable parser " + e42);
                    }
                }
                if (fin != null) {
                    try {
                        fin.close();
                    } catch (IOException e22) {
                        Slog.w(TAG, "Exception in mcctable parser " + e22);
                        return false;
                    }
                }
                Collections.sort(arrayList);
                sTable = arrayList;
                Slog.i(TAG, "cust file is successfully load into the table v2");
                return true;
            } catch (XmlPullParserException e5) {
                e = e5;
                fileInputStream = fin;
            } catch (IOException e6) {
                e2 = e6;
                fileInputStream = fin;
            } catch (NullPointerException e7) {
                e3 = e7;
                fileInputStream = fin;
            } catch (Throwable th2) {
                th = th2;
                fileInputStream = fin;
            }
        } catch (XmlPullParserException e8) {
            e = e8;
            try {
                Slog.w(TAG, "Exception in mcctable parser " + e);
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e222) {
                        Slog.w(TAG, "Exception in mcctable parser " + e222);
                        return false;
                    }
                }
                return false;
            } catch (Throwable th3) {
                th = th3;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e2222) {
                        Slog.w(TAG, "Exception in mcctable parser " + e2222);
                        return false;
                    }
                }
                throw th;
            }
        } catch (IOException e9) {
            e2 = e9;
            Slog.w(TAG, "Exception in mcctable parser " + e2);
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e22222) {
                    Slog.w(TAG, "Exception in mcctable parser " + e22222);
                    return false;
                }
            }
            return false;
        } catch (NullPointerException e10) {
            e3 = e10;
            Slog.w(TAG, "Exception in mcctable parser " + e3);
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e222222) {
                    Slog.w(TAG, "Exception in mcctable parser " + e222222);
                    return false;
                }
            }
            return false;
        }
    }
}
