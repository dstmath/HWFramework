package com.huawei.android.app;

import android.common.HwFrameworkFactory;
import android.content.Context;
import com.android.internal.app.LocaleStore;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class LocaleStoreEx {
    public static Set<LocaleInfo> getLevelLocales(Context context, Set<String> ignorables, LocaleInfo parent, boolean translatedOnly) {
        Set<LocaleStore.LocaleInfo> setOri;
        if (parent != null) {
            setOri = LocaleStore.getLevelLocales(context, ignorables, parent.getInternalLocaleInfo(), translatedOnly);
        } else {
            setOri = LocaleStore.getLevelLocales(context, ignorables, (LocaleStore.LocaleInfo) null, translatedOnly);
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

    public static LocaleInfo getLocaleInfo(String localeId) {
        return new LocaleInfo(LocaleStore.getLocaleInfo(Locale.forLanguageTag(localeId)));
    }

    public static void fillCache(Context context) {
        LocaleStore.fillCache(context);
    }

    public static class LocaleInfo {
        private LocaleStore.LocaleInfo mLocaleInfo;

        public LocaleInfo(LocaleStore.LocaleInfo localeInfo) {
            this.mLocaleInfo = localeInfo;
        }

        public LocaleStore.LocaleInfo getInternalLocaleInfo() {
            return this.mLocaleInfo;
        }

        public Locale getLocale() {
            LocaleStore.LocaleInfo localeInfo = this.mLocaleInfo;
            if (localeInfo == null) {
                return null;
            }
            return localeInfo.getLocale();
        }

        public Locale getParent() {
            LocaleStore.LocaleInfo localeInfo = this.mLocaleInfo;
            if (localeInfo == null) {
                return null;
            }
            return localeInfo.getParent();
        }

        public String getId() {
            LocaleStore.LocaleInfo localeInfo = this.mLocaleInfo;
            if (localeInfo == null) {
                return "";
            }
            return localeInfo.getId();
        }

        public boolean isSuggested() {
            return new LocaleStore().isSuggestedLocale(this.mLocaleInfo);
        }

        public String getFullNameNative() {
            LocaleStore.LocaleInfo localeInfo = this.mLocaleInfo;
            if (localeInfo != null) {
                return localeInfo.getFullNameNative();
            }
            return null;
        }

        public String getFullNameInUiLanguage() {
            LocaleStore.LocaleInfo localeInfo = this.mLocaleInfo;
            if (localeInfo != null) {
                return localeInfo.getFullNameInUiLanguage();
            }
            return null;
        }
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

    public static String[] getNumberSystems(Locale locale) {
        return HwFrameworkFactory.getHwLocaleStoreEx().getNumberSystems(locale);
    }

    public static boolean isSupportNativeNumber(Locale locale) {
        return HwFrameworkFactory.getHwLocaleStoreEx().isSupportNativeNumber(locale);
    }

    public static boolean updateSystemNumber(String name) {
        return HwFrameworkFactory.getHwLocaleStoreEx().updateSystemNumber(name);
    }
}
