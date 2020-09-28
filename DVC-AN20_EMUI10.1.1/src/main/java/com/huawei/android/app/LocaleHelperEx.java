package com.huawei.android.app;

import android.common.HwFrameworkFactory;
import android.content.Context;
import com.android.internal.app.LocaleHelper;
import com.huawei.android.app.LocaleStoreEx;
import huawei.com.android.internal.app.HwLocaleHelperEx;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

public class LocaleHelperEx {
    public static String getDisplayCountry(Locale locale, Locale displayLocale) {
        return LocaleHelper.getDisplayCountry(locale, displayLocale);
    }

    public static String normalizeForSearch(String str, Locale locale) {
        return LocaleHelper.normalizeForSearch(str, locale);
    }

    public static String getDisplayName(Locale locale, Locale displayLocale, boolean sentenceCase) {
        return LocaleHelper.getDisplayName(locale, displayLocale, sentenceCase);
    }

    public static final class LocaleInfoComparatorEx implements Comparator<LocaleStoreEx.LocaleInfo> {
        LocaleHelper.LocaleInfoComparator mComparator;

        public LocaleInfoComparatorEx(Locale sortLocale, boolean countryMode) {
            this.mComparator = new LocaleHelper.LocaleInfoComparator(sortLocale, countryMode);
        }

        public int compare(LocaleStoreEx.LocaleInfo lhs, LocaleStoreEx.LocaleInfo rhs) {
            return this.mComparator.compare(lhs.getInternalLocaleInfo(), rhs.getInternalLocaleInfo());
        }
    }

    public static String replaceCountryName(Locale locale, Locale displayLocale, String display) {
        return HwFrameworkFactory.getHwLocaleHelperEx().replaceCountryName(locale, displayLocale, display);
    }

    public static ArrayList<String> getBlackRegions(Context context, Locale locale) {
        return HwLocaleHelperEx.getTabooBlackAllRegionsPart(context, locale);
    }

    public static ArrayList<Locale> getBlackLangs(Context context) {
        return HwLocaleHelperEx.getTabooBlackLangsPart(context);
    }

    public static ArrayList<String> getBlackLangs2String(Context context) {
        return HwLocaleHelperEx.getTabooBlackLangs2String(context);
    }

    public static ArrayList<String> getBlackCities(Context context) {
        return HwLocaleHelperEx.getBlackCities(context);
    }

    public static String getCityName(String city, Locale displayLocale) {
        return HwLocaleHelperEx.getCityName(city, displayLocale);
    }
}
