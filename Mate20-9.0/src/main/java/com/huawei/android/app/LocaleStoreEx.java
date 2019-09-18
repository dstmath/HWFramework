package com.huawei.android.app;

import android.common.HwFrameworkFactory;
import android.content.Context;
import com.android.internal.app.LocaleStore;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class LocaleStoreEx {

    public static class LocaleInfo {
        private LocaleStore.LocaleInfo mLocaleInfo;

        public LocaleInfo(LocaleStore.LocaleInfo localeInfo) {
            this.mLocaleInfo = localeInfo;
        }

        public LocaleStore.LocaleInfo getInternalLocaleInfo() {
            return this.mLocaleInfo;
        }

        public Locale getLocale() {
            return this.mLocaleInfo.getLocale();
        }

        public Locale getParent() {
            return this.mLocaleInfo.getParent();
        }

        public String getId() {
            return this.mLocaleInfo.getId();
        }

        public String getFullNameNative() {
            return this.mLocaleInfo.getFullNameNative();
        }

        public String getFullNameInUiLanguage() {
            return this.mLocaleInfo.getFullNameInUiLanguage();
        }
    }

    public static Set<LocaleInfo> getLevelLocales(Context context, Set<String> ignorables, LocaleInfo parent, boolean translatedOnly) {
        Set<LocaleStore.LocaleInfo> setOri;
        if (parent != null) {
            setOri = LocaleStore.getLevelLocales(context, ignorables, parent.getInternalLocaleInfo(), translatedOnly);
        } else {
            setOri = LocaleStore.getLevelLocales(context, ignorables, null, translatedOnly);
        }
        if (setOri == null) {
            return new HashSet();
        }
        HashSet<LocaleInfo> retSet = new HashSet<>();
        for (LocaleStore.LocaleInfo ori : setOri) {
            retSet.add(new LocaleInfo(ori));
        }
        return retSet;
    }

    public static LocaleInfo getLocaleInfo(Locale locale) {
        return new LocaleInfo(LocaleStore.getLocaleInfo(locale));
    }

    public static void fillCache(Context context) {
        LocaleStore.fillCache(context);
    }

    public static LocaleInfo getLocaleInfo(String localeId) {
        return new LocaleInfo(LocaleStore.getLocaleInfo(Locale.forLanguageTag(localeId)));
    }

    public static String getFullLanguageName(Context context, Locale locale, Locale displayLocale) {
        return HwFrameworkFactory.getHwLocaleStoreEx().getFullLanguageName(context, locale, displayLocale);
    }

    public static Set<LocaleInfo> getLanguageLocales(Context context) {
        HashSet<LocaleInfo> result = new HashSet<>();
        for (LocaleStore.LocaleInfo li : HwFrameworkFactory.getHwLocaleStoreEx().getLanguageLocales(context, new LocaleStore())) {
            result.add(new LocaleInfo(li));
        }
        return result;
    }

    public static Set<LocaleInfo> getRegionLocales(Context context, Locale locale) {
        HashSet<LocaleInfo> result = new HashSet<>();
        for (LocaleStore.LocaleInfo li : HwFrameworkFactory.getHwLocaleStoreEx().getRegionLocales(context, locale, new LocaleStore())) {
            result.add(new LocaleInfo(li));
        }
        return result;
    }

    public static boolean isSupportRegion(Context context, Locale locale, String region) {
        return HwFrameworkFactory.getHwLocaleStoreEx().isSupportRegion(context, locale, region);
    }
}
