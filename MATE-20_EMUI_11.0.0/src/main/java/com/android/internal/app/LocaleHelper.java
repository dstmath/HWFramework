package com.android.internal.app;

import android.annotation.UnsupportedAppUsage;
import android.common.HwFrameworkFactory;
import android.icu.text.ListFormatter;
import android.icu.util.ULocale;
import android.os.LocaleList;
import android.text.TextUtils;
import com.android.internal.app.LocaleStore;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import libcore.icu.ICU;

public class LocaleHelper {
    public static String toSentenceCase(String str, Locale locale) {
        if (str.isEmpty()) {
            return str;
        }
        int firstCodePointLen = str.offsetByCodePoints(0, 1);
        return str.substring(0, firstCodePointLen).toUpperCase(locale) + str.substring(firstCodePointLen);
    }

    @UnsupportedAppUsage
    public static String normalizeForSearch(String str, Locale locale) {
        return str.toUpperCase();
    }

    private static boolean shouldUseDialectName(Locale locale) {
        String lang = locale.getLanguage();
        return "fa".equals(lang) || "zh".equals(lang);
    }

    @UnsupportedAppUsage
    public static String getDisplayName(Locale locale, Locale displayLocale, boolean sentenceCase) {
        String result;
        Locale noNumberLocale = HwFrameworkFactory.getHwLocaleStoreEx().removeSystemNumber(locale);
        ULocale displayULocale = ULocale.forLocale(displayLocale);
        if (shouldUseDialectName(noNumberLocale)) {
            result = ULocale.getDisplayNameWithDialect(noNumberLocale.toLanguageTag(), displayULocale);
        } else {
            result = ULocale.getDisplayName(noNumberLocale.toLanguageTag(), displayULocale);
        }
        String result2 = HwFrameworkFactory.getHwLocaleHelperEx().replaceDisplayName(noNumberLocale, displayLocale, result);
        if (result2 != null) {
            return sentenceCase ? toSentenceCase(result2, displayLocale) : result2;
        }
        return result2;
    }

    public static String getDisplayName(Locale locale, boolean sentenceCase) {
        return getDisplayName(locale, Locale.getDefault(), sentenceCase);
    }

    @UnsupportedAppUsage
    public static String getDisplayCountry(Locale locale, Locale displayLocale) {
        String country = ULocale.getDisplayCountry(locale.toLanguageTag(), ULocale.forLocale(displayLocale));
        locale.getUnicodeLocaleType("nu");
        return HwFrameworkFactory.getHwLocaleHelperEx().replaceCountryName(locale, displayLocale, country);
    }

    public static String getDisplayCountry(Locale locale) {
        return HwFrameworkFactory.getHwLocaleHelperEx().replaceCountryName(locale, Locale.getDefault(), ULocale.getDisplayCountry(locale.toLanguageTag(), ULocale.getDefault()));
    }

    public static String getDisplayLocaleList(LocaleList locales, Locale displayLocale, int maxLocales) {
        int localeCount;
        int localeCount2;
        Locale dispLocale = displayLocale == null ? Locale.getDefault() : displayLocale;
        boolean ellipsisNeeded = locales.size() > maxLocales;
        if (ellipsisNeeded) {
            localeCount = maxLocales;
            localeCount2 = maxLocales + 1;
        } else {
            localeCount2 = locales.size();
            localeCount = localeCount2;
        }
        String[] localeNames = new String[localeCount2];
        for (int i = 0; i < localeCount; i++) {
            localeNames[i] = getDisplayName(locales.get(i), dispLocale, false);
        }
        if (ellipsisNeeded) {
            localeNames[maxLocales] = TextUtils.getEllipsisString(TextUtils.TruncateAt.END);
        }
        return ListFormatter.getInstance(dispLocale).format(localeNames);
    }

    public static Locale addLikelySubtags(Locale locale) {
        return ICU.addLikelySubtags(locale);
    }

    public static final class LocaleInfoComparator implements Comparator<LocaleStore.LocaleInfo> {
        private static final String PREFIX_ARABIC = "ال";
        private final Collator mCollator;
        private final boolean mCountryMode;
        private List<String> relatedLocales = null;

        @UnsupportedAppUsage
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

        @UnsupportedAppUsage
        public int compare(LocaleStore.LocaleInfo lhs, LocaleStore.LocaleInfo rhs) {
            if (lhs.isSuggested() != rhs.isSuggested()) {
                return lhs.isSuggested() ? -1 : 1;
            }
            int compareInt = HwFrameworkFactory.getHwLocaleHelperEx(new LocaleStore()).getCompareIntEx(lhs, rhs, this.relatedLocales);
            if (compareInt != 0) {
                return compareInt;
            }
            return this.mCollator.compare(removePrefixForCompare(lhs.getLocale(), lhs.getLabel(this.mCountryMode)), removePrefixForCompare(rhs.getLocale(), rhs.getLabel(this.mCountryMode)));
        }
    }

    public static boolean shouldUseDialectNameEx(Locale locale) {
        return shouldUseDialectName(locale);
    }
}
