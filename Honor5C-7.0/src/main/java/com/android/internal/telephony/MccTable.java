package com.android.internal.telephony;

import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.content.Context;
import android.content.res.Configuration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.app.LocalePicker;
import com.android.internal.telephony.imsphone.CallFailCause;
import com.android.internal.telephony.vsim.VSimUtilsInner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import libcore.icu.ICU;
import libcore.icu.TimeZoneNames;

public final class MccTable {
    private static final Map<Locale, Locale> FALLBACKS = null;
    static final String LOG_TAG = "MccTable";
    static ArrayList<MccEntry> sTable;

    static class MccEntry implements Comparable<MccEntry> {
        final String mIso;
        final int mMcc;
        final int mSmallestDigitsMnc;

        MccEntry(int mnc, String iso, int smallestDigitsMCC) {
            if (iso == null) {
                throw new NullPointerException();
            }
            this.mMcc = mnc;
            this.mIso = iso;
            this.mSmallestDigitsMnc = smallestDigitsMCC;
        }

        public int compareTo(MccEntry o) {
            return this.mMcc - o.mMcc;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.MccTable.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.MccTable.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.MccTable.<clinit>():void");
    }

    private static MccEntry entryForMcc(int mcc) {
        int index = Collections.binarySearch(sTable, new MccEntry(mcc, "", 0));
        if (index < 0) {
            return null;
        }
        return (MccEntry) sTable.get(index);
    }

    public static String defaultTimeZoneForMcc(int mcc) {
        String custTimeZone = HwTelephonyFactory.getHwPhoneManager().custTimeZoneForMcc(mcc);
        if (custTimeZone != null) {
            return custTimeZone;
        }
        MccEntry entry = entryForMcc(mcc);
        if (entry == null) {
            return null;
        }
        String[] tz = TimeZoneNames.forLocale(new Locale("", entry.mIso));
        if (tz.length == 0) {
            return null;
        }
        return tz[0];
    }

    public static String countryCodeForMcc(int mcc) {
        String custCode = HwTelephonyFactory.getHwPhoneManager().custCountryCodeForMcc(mcc);
        if (custCode != null) {
            return custCode;
        }
        MccEntry entry = entryForMcc(mcc);
        if (entry == null) {
            return "";
        }
        return entry.mIso;
    }

    public static String defaultLanguageForMcc(int mcc) {
        String custLanguage = HwTelephonyFactory.getHwPhoneManager().custLanguageForMcc(mcc);
        if (custLanguage != null) {
            return custLanguage;
        }
        MccEntry entry = entryForMcc(mcc);
        if (entry == null) {
            Slog.d(LOG_TAG, "defaultLanguageForMcc(" + mcc + "): no country for mcc");
            return null;
        }
        String likelyLanguage = ICU.addLikelySubtags(new Locale("und", entry.mIso)).getLanguage();
        Slog.d(LOG_TAG, "defaultLanguageForMcc(" + mcc + "): country " + entry.mIso + " uses " + likelyLanguage);
        return likelyLanguage;
    }

    public static int smallestDigitsMccForMnc(int mcc) {
        int custDigit = HwTelephonyFactory.getHwPhoneManager().custSmallestDigitsMccForMnc(mcc);
        if (custDigit > 0) {
            return custDigit;
        }
        MccEntry entry = entryForMcc(mcc);
        if (entry == null) {
            return 2;
        }
        return entry.mSmallestDigitsMnc;
    }

    public static void updateMccMncConfiguration(Context context, String mccmnc, boolean fromServiceState) {
        Slog.d(LOG_TAG, "updateMccMncConfiguration mccmnc='" + mccmnc + "' fromServiceState=" + fromServiceState);
        if (Build.IS_DEBUGGABLE) {
            String overrideMcc = SystemProperties.get("persist.sys.override_mcc");
            if (!TextUtils.isEmpty(overrideMcc)) {
                mccmnc = overrideMcc;
                Slog.d(LOG_TAG, "updateMccMncConfiguration overriding mccmnc='" + overrideMcc + "'");
            }
        }
        if (!TextUtils.isEmpty(mccmnc)) {
            Slog.d(LOG_TAG, "updateMccMncConfiguration defaultMccMnc=" + TelephonyManager.getDefault().getSimOperatorNumeric());
            if (!HwTelephonyFactory.getHwPhoneManager().shouldSkipUpdateMccMnc(mccmnc) || fromServiceState) {
                if (VSimUtilsInner.isVSimOn()) {
                    String vsimOperator = SystemProperties.get("gsm.operator.numeric.vsim");
                    Slog.d(LOG_TAG, "updateMccMncConfiguration mccmnc = " + mccmnc + " getVSimNetworkOperator = " + vsimOperator);
                    if (mccmnc.equals(vsimOperator)) {
                        Slog.d(LOG_TAG, "vsim enabled , Skip Update vsim MccMnc");
                        return;
                    }
                }
                try {
                    int mcc = Integer.parseInt(mccmnc.substring(0, 3));
                    int mnc = Integer.parseInt(mccmnc.substring(3));
                    HwTelephonyFactory.getHwPhoneManager().setMccTableMnc(mnc);
                    Slog.d(LOG_TAG, "updateMccMncConfiguration: mcc=" + mcc + ", mnc=" + mnc);
                    Locale locale = null;
                    if (mcc != 0) {
                        setTimezoneFromMccIfNeeded(context, mcc);
                        locale = getLocaleFromMcc(context, mcc, null);
                    }
                    if (fromServiceState) {
                        setWifiCountryCodeFromMcc(context, mcc);
                    } else {
                        try {
                            Configuration config = new Configuration();
                            boolean updateConfig = false;
                            if (mcc != 0) {
                                config.mcc = mcc;
                                if (mnc == 0) {
                                    mnc = CallFailCause.ERROR_UNSPECIFIED;
                                }
                                config.mnc = mnc;
                                updateConfig = true;
                            }
                            if (locale != null) {
                                config.setLocale(locale);
                                config.userSetLocale = true;
                                updateConfig = true;
                            }
                            if (updateConfig) {
                                Slog.d(LOG_TAG, "updateMccMncConfiguration updateConfig config=" + config);
                                ActivityManagerNative.getDefault().updateConfiguration(config);
                            } else {
                                Slog.d(LOG_TAG, "updateMccMncConfiguration nothing to update");
                            }
                        } catch (RemoteException e) {
                            Slog.e(LOG_TAG, "Can't update configuration", e);
                        }
                    }
                } catch (NumberFormatException e2) {
                    Slog.e(LOG_TAG, "Error parsing IMSI: " + mccmnc);
                    return;
                }
            }
            Slog.d(LOG_TAG, "shouldSkipUpdateMccMnc !");
        } else if (fromServiceState) {
            setWifiCountryCodeFromMcc(context, 0);
        }
    }

    private static Locale chooseBestFallback(Locale target, List<Locale> candidates) {
        if (candidates.isEmpty()) {
            return null;
        }
        Locale fallback = target;
        do {
            fallback = (Locale) FALLBACKS.get(fallback);
            if (fallback == null) {
                return (Locale) candidates.get(0);
            }
        } while (!candidates.contains(fallback));
        return fallback;
    }

    private static Locale getLocaleForLanguageCountry(Context context, String language, String country) {
        return getLocaleForLanguageCountry(context, language, country, 0);
    }

    private static Locale getLocaleForLanguageCountry(Context context, String language, String country, int mcc) {
        if (language == null) {
            Slog.d(LOG_TAG, "getLocaleForLanguageCountry: skipping no language");
            return null;
        }
        language = new Locale(language).getLanguage();
        if (country == null) {
            country = "";
        }
        if (SystemProperties.get("persist.sys.locale", "").isEmpty()) {
            Locale locale = new Locale(language, country);
            try {
                String[] localeArray = LocalePicker.getSupportedLocales(context);
                Arrays.sort(localeArray);
                List<String> locales = new ArrayList(Arrays.asList(localeArray));
                String custScript = HwTelephonyFactory.getHwPhoneManager().custScriptForMcc(mcc);
                Slog.d(LOG_TAG, "getLocaleForLanguageCountry: custScript= " + custScript);
                locales.remove("ar-XB");
                locales.remove("en-XA");
                Locale firstMatch = null;
                for (String locale2 : locales) {
                    Locale l = Locale.forLanguageTag(locale2.replace('_', '-'));
                    if (!(l == null || "und".equals(l.getLanguage()) || l.getLanguage().isEmpty() || l.getCountry().isEmpty() || !l.getLanguage().equals(locale.getLanguage()))) {
                        if (l.getCountry().equals(locale.getCountry())) {
                            if (custScript != null && custScript.equalsIgnoreCase(l.getScript())) {
                                Slog.d(LOG_TAG, "getLocaleForLanguageCountry: got perfect match: " + l.toLanguageTag());
                                firstMatch = l;
                                break;
                            }
                            Slog.d(LOG_TAG, "getLocaleForLanguageCountry: got language and country match: " + l.toLanguageTag());
                            firstMatch = l;
                        } else if (firstMatch == null) {
                            firstMatch = l;
                        }
                    }
                }
                if (firstMatch != null) {
                    Locale betterMatch = HwTelephonyFactory.getHwPhoneManager().getBetterMatchLocale(context, language, custScript, country, firstMatch);
                    if (betterMatch != null) {
                        firstMatch = betterMatch;
                    }
                }
                if (firstMatch != null) {
                    Slog.d(LOG_TAG, "getLocaleForLanguageCountry: got a final match: " + firstMatch.toLanguageTag());
                    return firstMatch;
                }
                Slog.d(LOG_TAG, "getLocaleForLanguageCountry: no locales for language " + language);
                return null;
            } catch (Exception e) {
                Slog.d(LOG_TAG, "getLocaleForLanguageCountry: exception", e);
            }
        } else {
            Slog.d(LOG_TAG, "getLocaleForLanguageCountry: skipping already persisted");
            return null;
        }
    }

    private static void setTimezoneFromMccIfNeeded(Context context, int mcc) {
        String timezone = SystemProperties.get("persist.sys.timezone");
        if (!(timezone == null || timezone.length() == 0)) {
            if (!HwTelephonyFactory.getHwPhoneManager().isDefaultTimezone()) {
                return;
            }
        }
        String zoneId = defaultTimeZoneForMcc(mcc);
        if (zoneId != null && zoneId.length() > 0) {
            ((AlarmManager) context.getSystemService("alarm")).setTimeZone(zoneId);
            HwTelephonyFactory.getHwPhoneManager().changedDefaultTimezone();
            Slog.d(LOG_TAG, "timezone set to " + zoneId);
        }
    }

    public static Locale getLocaleFromMcc(Context context, int mcc, String simLanguage) {
        String language = simLanguage == null ? defaultLanguageForMcc(mcc) : simLanguage;
        String country = countryCodeForMcc(mcc);
        Locale custLocale = HwTelephonyFactory.getHwPhoneManager().getSpecialLoacleConfig(context, mcc);
        if (custLocale != null) {
            return custLocale;
        }
        Slog.d(LOG_TAG, "getLocaleFromMcc(" + language + ", " + country + ", " + mcc);
        Locale locale = getLocaleForLanguageCountry(context, language, country, mcc);
        if (locale != null || simLanguage == null) {
            return locale;
        }
        Slog.d(LOG_TAG, "[retry ] getLocaleFromMcc(" + defaultLanguageForMcc(mcc) + ", " + country + ", " + mcc);
        return getLocaleForLanguageCountry(context, null, country);
    }

    private static void setWifiCountryCodeFromMcc(Context context, int mcc) {
        String country = countryCodeForMcc(mcc);
        Slog.d(LOG_TAG, "WIFI_COUNTRY_CODE set to " + country);
        ((WifiManager) context.getSystemService("wifi")).setCountryCode(country, true);
    }
}
