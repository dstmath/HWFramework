package com.huawei.android.app;

import com.android.internal.app.LocaleHelper;
import com.android.internal.app.LocaleHelper.LocaleInfoComparator;
import com.huawei.android.app.LocaleStoreEx.LocaleInfo;
import java.util.Comparator;
import java.util.Locale;

public class LocaleHelperEx {

    public static final class LocaleInfoComparatorEx implements Comparator<LocaleInfo> {
        LocaleInfoComparator mComparator;

        public LocaleInfoComparatorEx(Locale sortLocale, boolean countryMode) {
            this.mComparator = new LocaleInfoComparator(sortLocale, countryMode);
        }

        public int compare(LocaleInfo lhs, LocaleInfo rhs) {
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
