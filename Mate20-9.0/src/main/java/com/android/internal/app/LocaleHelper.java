package com.android.internal.app;

import android.common.HwFrameworkFactory;
import android.icu.text.ListFormatter;
import android.icu.util.ULocale;
import android.os.LocaleList;
import android.os.SystemProperties;
import android.text.TextUtils;
import com.android.internal.app.LocaleStore;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import libcore.icu.ICU;

public class LocaleHelper {
    private static final boolean IS_HIDE_COUNTRY_NAME = SystemProperties.getBoolean("ro.config.hw_hide_country_name", false);
    private static final boolean isCN = SystemProperties.get("ro.product.locale.region").equals("CN");
    private static final boolean isDT = SystemProperties.get("ro.config.hw_opta", "0").equals("150");
    private static final boolean isDomesticVersion = (isZH && isCN);
    private static final boolean isMKD = SystemProperties.get("ro.config.hw_optb", "0").equals("807");
    private static final boolean isZH = SystemProperties.get("ro.product.locale.language").equals("zh");

    public static final class LocaleInfoComparator implements Comparator<LocaleStore.LocaleInfo> {
        private static final String PREFIX_ARABIC = "ال";
        private final Collator mCollator;
        private final boolean mCountryMode;
        private List<String> relatedLocales = null;

        public LocaleInfoComparator(Locale sortLocale, boolean countryMode) {
            this.mCollator = Collator.getInstance(sortLocale);
            this.mCountryMode = countryMode;
            this.relatedLocales = HwFrameworkFactory.getHwLocaleHelperEx(new LocaleStore()).getRelatedLocalesEx();
        }

        private String removePrefixForCompare(Locale locale, String str) {
            if (!"ar".equals(locale.getLanguage()) || !str.startsWith(PREFIX_ARABIC)) {
                return str;
            }
            return str.substring(PREFIX_ARABIC.length());
        }

        public int compare(LocaleStore.LocaleInfo lhs, LocaleStore.LocaleInfo rhs) {
            if (lhs.isSuggested() == rhs.isSuggested()) {
                int compareInt = HwFrameworkFactory.getHwLocaleHelperEx(new LocaleStore()).getCompareIntEx(lhs, rhs, this.relatedLocales);
                if (compareInt != 0) {
                    return compareInt;
                }
                return this.mCollator.compare(removePrefixForCompare(lhs.getLocale(), lhs.getLabel(this.mCountryMode)), removePrefixForCompare(rhs.getLocale(), rhs.getLabel(this.mCountryMode)));
            }
            return lhs.isSuggested() != 0 ? -1 : 1;
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
        return "fa".equals(lang) || "zh".equals(lang);
    }

    public static String getDisplayName(Locale locale, Locale displayLocale, boolean sentenceCase) {
        String result;
        Locale systemLocale = Locale.getDefault();
        if ("my".equals(locale.getLanguage()) && "my".equals(displayLocale.getLanguage())) {
            if ("ZG".equals(systemLocale.getCountry())) {
                displayLocale = Locale.forLanguageTag("my-ZG");
            } else {
                displayLocale = Locale.forLanguageTag("my");
            }
        }
        ULocale displayULocale = ULocale.forLocale(displayLocale);
        String[] specialCode1 = {"ar_XB", "en_XA", "zz_ZX", "zz"};
        String[] specialNames1 = {"[Bidirection test locale]", "[Pseudo locale]", "[DBID version]", "[DBID version]"};
        String[] specialCode3 = {"mk_MK", "mk"};
        String[] specialNames3 = {"FYROM", "FYROM"};
        String[] specialNames4 = {"Macedonian (Macedonia)", "Macedonian (Macedonia)"};
        if (shouldUseDialectName(locale)) {
            result = ULocale.getDisplayNameWithDialect(locale.toLanguageTag(), displayULocale);
        } else {
            result = ULocale.getDisplayName(locale.toLanguageTag(), displayULocale);
        }
        String result2 = getDisplayName(locale, specialCode1, specialNames1, result);
        if ((isGreeceSIM() && (!isDT || !isMKD)) || (isDT && !isMKD)) {
            result2 = getDisplayName(locale, specialCode3, specialNames3, result2);
        } else if (displayLocale.toString().startsWith("en")) {
            result2 = getDisplayName(locale, specialCode3, specialNames4, result2);
        }
        if (IS_HIDE_COUNTRY_NAME) {
            result2 = result2.replace("(", "（").split("（")[0];
        }
        if (isDomesticVersion) {
            String newResult = HwFrameworkFactory.getHwLocaleHelperManagerEx().replaceTaiwan2TaiwanChina(locale, displayLocale, result2);
            if (newResult != null) {
                result2 = newResult;
            }
        }
        return sentenceCase ? toSentenceCase(result2, displayLocale) : result2;
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
        ArrayList<String> mccList = new ArrayList<>();
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
        Locale systemLocale = Locale.getDefault();
        if ("my".equals(locale.getLanguage()) && "my".equals(displayLocale.getLanguage())) {
            if ("ZG".equals(systemLocale.getCountry())) {
                displayLocale = Locale.forLanguageTag("my-ZG");
            } else {
                displayLocale = Locale.forLanguageTag("my");
            }
        }
        String languageTag = locale.toLanguageTag();
        ULocale uDisplayLocale = ULocale.forLocale(displayLocale);
        String country = ULocale.getDisplayCountry(languageTag, uDisplayLocale);
        String numberingSystem = locale.getUnicodeLocaleType("nu");
        if (isDomesticVersion) {
            String newCountry = HwFrameworkFactory.getHwLocaleHelperManagerEx().replaceTaiwan2TaiwanChina(locale, displayLocale, country);
            if (newCountry != null) {
                country = newCountry;
            }
        }
        if (numberingSystem == null) {
            return country;
        }
        return String.format("%s (%s)", new Object[]{country, ULocale.getDisplayKeywordValue(languageTag, "numbers", uDisplayLocale)});
    }

    public static String getDisplayCountry(Locale locale) {
        String country = ULocale.getDisplayCountry(locale.toLanguageTag(), ULocale.getDefault());
        if (isDomesticVersion) {
            String newCountry = HwFrameworkFactory.getHwLocaleHelperManagerEx().replaceTaiwan2TaiwanChina(locale, Locale.getDefault(), country);
            if (newCountry != null) {
                return newCountry;
            }
        }
        return country;
    }

    public static String getDisplayLocaleList(LocaleList locales, Locale displayLocale, int maxLocales) {
        int listCount;
        int localeCount;
        Locale dispLocale = displayLocale == null ? Locale.getDefault() : displayLocale;
        boolean ellipsisNeeded = locales.size() > maxLocales;
        if (ellipsisNeeded) {
            localeCount = maxLocales;
            listCount = maxLocales + 1;
        } else {
            listCount = locales.size();
            localeCount = listCount;
        }
        String[] localeNames = new String[listCount];
        for (int i = 0; i < localeCount; i++) {
            localeNames[i] = getDisplayName(locales.get(i), dispLocale, false);
        }
        if (ellipsisNeeded) {
            localeNames[maxLocales] = TextUtils.getEllipsisString(TextUtils.TruncateAt.END);
        }
        return ListFormatter.getInstance(dispLocale).format((Object[]) localeNames);
    }

    public static Locale addLikelySubtags(Locale locale) {
        return ICU.addLikelySubtags(locale);
    }
}
