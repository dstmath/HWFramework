package com.android.internal.app;

import android.icu.text.ListFormatter;
import android.icu.util.ULocale;
import android.os.LocaleList;
import android.os.SystemProperties;
import android.text.TextUtils;
import com.android.internal.app.LocaleStore.LocaleInfo;
import huawei.cust.HwCustUtils;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import libcore.icu.ICU;

public class LocaleHelper {
    private static final boolean isDT = SystemProperties.get("ro.config.hw_opta", "0").equals("150");
    private static final boolean isMKD = SystemProperties.get("ro.config.hw_optb", "0").equals("807");
    private static HwCustLocaleHelper sHwCustLocaleHelper = ((HwCustLocaleHelper) HwCustUtils.createObj(HwCustLocaleHelper.class, new Object[0]));

    public static final class LocaleInfoComparator implements Comparator<LocaleInfo> {
        private static final String PREFIX_ARABIC = "ال";
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
        return !"fa".equals(lang) ? "zh".equals(lang) : true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00cb  */
    /* JADX WARNING: Missing block: B:15:0x00b9, code:
            if (((isDT ? isMKD : 0) ^ 1) == 0) goto L_0x00bb;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String getDisplayName(Locale locale, Locale displayLocale, boolean sentenceCase) {
        String result;
        ULocale displayULocale = ULocale.forLocale(displayLocale);
        String[] specialCode1 = new String[]{"ar_XB", "en_XA", "zz_ZX", "zz"};
        String[] specialNames1 = new String[]{"[Bidirection test locale]", "[Pseudo locale]", "[DBID version]", "[DBID version]"};
        String[] specialCode2 = new String[]{"my_MM", "my_ZG"};
        String[] specialNames2 = new String[]{"ဗမာ (Unicode)", "ဗမာ (Zawgyi)"};
        String[] specialCode3 = new String[]{"mk_MK", "mk"};
        String[] specialNames3 = new String[]{"FYROM", "FYROM"};
        String[] specialNames4 = new String[]{"Macedonian (Macedonia)", "Macedonian (Macedonia)"};
        if (shouldUseDialectName(locale)) {
            result = ULocale.getDisplayNameWithDialect(locale.toLanguageTag(), displayULocale);
        } else {
            result = ULocale.getDisplayName(locale.toLanguageTag(), displayULocale);
        }
        result = getDisplayName(locale, specialCode1, specialNames1, result);
        if (sHwCustLocaleHelper != null) {
            result = sHwCustLocaleHelper.customDisplayName(locale, displayLocale, result);
        }
        if (locale.equals(displayLocale)) {
            result = getDisplayName(locale, specialCode2, specialNames2, result);
        }
        if (isGreeceSIM()) {
        }
        if (!isDT || (isMKD ^ 1) == 0) {
            if (isDT && isMKD && displayLocale.toString().startsWith("en")) {
                result = getDisplayName(locale, specialCode3, specialNames4, result);
            }
            return sentenceCase ? toSentenceCase(result, displayLocale) : result;
        }
        result = getDisplayName(locale, specialCode3, specialNames3, result);
        if (sentenceCase) {
        }
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

    private static boolean isGreeceSIM() {
        ArrayList<String> mccList = new ArrayList();
        mccList.add("202");
        String simOperator = SystemProperties.get("persist.sys.mcc_match_fyrom");
        if (simOperator == null || simOperator.length() < 4) {
            return false;
        }
        if (simOperator.charAt(0) == ',') {
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
        String country = locale.getCountry();
        if ("MM".equals(country)) {
            return "Unicode";
        }
        if ("ZG".equals(country)) {
            return "Zawgyi";
        }
        return ULocale.getDisplayCountry(locale.toLanguageTag(), ULocale.forLocale(displayLocale));
    }

    public static String getDisplayCountry(Locale locale) {
        String myanmarUCountryName = "Unicode";
        String myanmarZCountryName = "Zawgyi";
        String country = locale.getCountry();
        if ("MM".equals(country)) {
            return "Unicode";
        }
        if ("ZG".equals(country)) {
            return "Zawgyi";
        }
        return ULocale.getDisplayCountry(locale.toLanguageTag(), ULocale.getDefault());
    }

    public static String getDisplayLocaleList(LocaleList locales, Locale displayLocale, int maxLocales) {
        int localeCount;
        int listCount;
        Locale dispLocale = displayLocale == null ? Locale.getDefault() : displayLocale;
        boolean ellipsisNeeded = locales.size() > maxLocales;
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
