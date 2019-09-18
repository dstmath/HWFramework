package com.huawei.android.app;

import com.android.internal.app.LocaleHelper;
import com.huawei.android.app.LocaleStoreEx;
import java.util.Comparator;
import java.util.Locale;

public class LocaleHelperEx {

    public static final class LocaleInfoComparatorEx implements Comparator<LocaleStoreEx.LocaleInfo> {
        LocaleHelper.LocaleInfoComparator mComparator;

        public LocaleInfoComparatorEx(Locale sortLocale, boolean countryMode) {
            this.mComparator = new LocaleHelper.LocaleInfoComparator(sortLocale, countryMode);
        }

        public int compare(LocaleStoreEx.LocaleInfo lhs, LocaleStoreEx.LocaleInfo rhs) {
            return this.mComparator.compare(lhs.getInternalLocaleInfo(), rhs.getInternalLocaleInfo());
        }
    }

    public static String getDisplayCountry(Locale locale, Locale displayLocale) {
        return LocaleHelper.getDisplayCountry(locale, displayLocale);
    }

    public static String normalizeForSearch(String str, Locale locale) {
        return LocaleHelper.normalizeForSearch(str, locale);
    }

    public static String getDisplayName(Locale locale, Locale displayLocale, boolean sentenceCase) {
        return LocaleHelper.getDisplayName(locale, displayLocale, sentenceCase);
    }
}
