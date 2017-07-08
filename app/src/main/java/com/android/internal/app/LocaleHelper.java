package com.android.internal.app;

import android.icu.text.ListFormatter;
import android.icu.util.ULocale;
import android.os.LocaleList;
import android.os.SystemProperties;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.android.internal.app.LocaleStore.LocaleInfo;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import libcore.icu.ICU;

public class LocaleHelper {
    private static final boolean isDT = false;
    private static final boolean isMKD = false;

    public static final class LocaleInfoComparator implements Comparator<LocaleInfo> {
        private static final String PREFIX_ARABIC = "\u0627\u0644";
        private final Collator mCollator;
        private final boolean mCountryMode;

        public LocaleInfoComparator(Locale sortLocale, boolean countryMode) {
            this.mCollator = Collator.getInstance(sortLocale);
            this.mCountryMode = countryMode;
        }

        private String removePrefixForCompare(Locale locale, String str) {
            if ("ar".equals(locale.getLanguage()) && str.startsWith(PREFIX_ARABIC)) {
                return str.substring(PREFIX_ARABIC.length());
            }
            return str;
        }

        public int compare(LocaleInfo lhs, LocaleInfo rhs) {
            if (lhs.isSuggested() == rhs.isSuggested()) {
                return this.mCollator.compare(removePrefixForCompare(lhs.getLocale(), lhs.getLabel(this.mCountryMode)), removePrefixForCompare(rhs.getLocale(), rhs.getLabel(this.mCountryMode)));
            }
            return lhs.isSuggested() ? -1 : 1;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.app.LocaleHelper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.app.LocaleHelper.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.LocaleHelper.<clinit>():void");
    }

    public static String toSentenceCase(String str, Locale locale) {
        if (str.isEmpty()) {
            return str;
        }
        int firstCodePointLen = str.offsetByCodePoints(0, 1);
        return str.substring(0, firstCodePointLen).toUpperCase(locale) + str.substring(firstCodePointLen);
    }

    public static String normalizeForSearch(String str, Locale locale) {
        return str.toUpperCase();
    }

    private static boolean shouldUseDialectName(Locale locale) {
        String lang = locale.getLanguage();
        if ("fa".equals(lang) || "ro".equals(lang)) {
            return true;
        }
        return "zh".equals(lang);
    }

    public static String getDisplayName(Locale locale, Locale displayLocale, boolean sentenceCase) {
        String result;
        ULocale displayULocale = ULocale.forLocale(displayLocale);
        String[] specialCode1 = new String[]{"ar_XB", "en_XA", "zz_ZX", "zz"};
        String[] specialNames1 = new String[]{"[Bidirection test locale]", "[Pseudo locale]", "[DBID version]", "[DBID version]"};
        String[] specialCode2 = new String[]{"my_MM", "my_ZG"};
        String[] specialNames2 = new String[]{"\u1017\u1019\u102c (Unicode)", "\u1017\u1019\u102c (Zawgyi)"};
        String[] specialCode3 = new String[]{"mk_MK", "mk"};
        String[] specialNames3 = new String[]{"FYROM", "FYROM"};
        if (shouldUseDialectName(locale)) {
            result = ULocale.getDisplayNameWithDialect(locale.toLanguageTag(), displayULocale);
        } else {
            result = ULocale.getDisplayName(locale.toLanguageTag(), displayULocale);
        }
        result = getDisplayName(locale, specialCode1, specialNames1, result);
        if (locale.equals(displayLocale)) {
            result = getDisplayName(locale, specialCode2, specialNames2, result);
        }
        if (!(isMacedonianSIM() || (isDT && isMKD)) || (isDT && !isMKD)) {
            result = getDisplayName(locale, specialCode3, specialNames3, result);
        }
        return sentenceCase ? toSentenceCase(result, displayLocale) : result;
    }

    public static String getDisplayName(Locale locale, boolean sentenceCase) {
        return getDisplayName(locale, Locale.getDefault(), sentenceCase);
    }

    private static String getDisplayName(Locale locale, String[] specialLocaleCodes, String[] specialLocaleNames, String originalStr) {
        String code = locale.toString();
        for (int i = 0; i < specialLocaleCodes.length; i++) {
            if (specialLocaleCodes[i].equals(code)) {
                return specialLocaleNames[i];
            }
        }
        return originalStr;
    }

    private static boolean isMacedonianSIM() {
        ArrayList<String> mccList = new ArrayList();
        mccList.add("294");
        String simOperator = SystemProperties.get("persist.sys.mcc_match_fyrom");
        if (simOperator == null || simOperator.length() < 4) {
            return false;
        }
        if (simOperator.charAt(0) == PhoneNumberUtils.PAUSE) {
            simOperator = simOperator.substring(1);
        }
        if (mccList.contains(simOperator.substring(0, 3))) {
            return true;
        }
        return false;
    }

    public static String getDisplayCountry(Locale locale, Locale displayLocale) {
        String myanmarUCountryName = "Unicode";
        String myanmarZCountryName = "Zawgyi";
        if ("my".equals(displayLocale.getLanguage())) {
            String country = locale.getCountry();
            if ("MM".equals(country)) {
                return "Unicode";
            }
            if ("ZG".equals(country)) {
                return "Zawgyi";
            }
        }
        return ULocale.getDisplayCountry(locale.toLanguageTag(), ULocale.forLocale(displayLocale));
    }

    public static String getDisplayCountry(Locale locale) {
        return ULocale.getDisplayCountry(locale.toLanguageTag(), ULocale.getDefault());
    }

    public static String getDisplayLocaleList(LocaleList locales, Locale displayLocale, int maxLocales) {
        boolean ellipsisNeeded;
        int localeCount;
        int listCount;
        Locale dispLocale = displayLocale == null ? Locale.getDefault() : displayLocale;
        if (locales.size() > maxLocales) {
            ellipsisNeeded = true;
        } else {
            ellipsisNeeded = false;
        }
        if (ellipsisNeeded) {
            localeCount = maxLocales;
            listCount = maxLocales + 1;
        } else {
            localeCount = locales.size();
            listCount = localeCount;
        }
        String[] localeNames = new String[listCount];
        for (int i = 0; i < localeCount; i++) {
            localeNames[i] = getDisplayName(locales.get(i), dispLocale, false);
        }
        if (ellipsisNeeded) {
            localeNames[maxLocales] = TextUtils.ELLIPSIS_STRING;
        }
        return ListFormatter.getInstance(dispLocale).format(localeNames);
    }

    public static Locale addLikelySubtags(Locale locale) {
        return ICU.addLikelySubtags(locale);
    }
}
