package com.huawei.android.app;

import android.content.Context;
import com.android.internal.app.LocaleStore;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class LocaleStoreEx {

    public static class LocaleInfo {
        private com.android.internal.app.LocaleStore.LocaleInfo mLocaleInfo;

        public LocaleInfo(com.android.internal.app.LocaleStore.LocaleInfo localeInfo) {
            this.mLocaleInfo = localeInfo;
        }

        public com.android.internal.app.LocaleStore.LocaleInfo getInternalLocaleInfo() {
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
        Set<com.android.internal.app.LocaleStore.LocaleInfo> setOri;
        if (parent != null) {
            setOri = LocaleStore.getLevelLocales(context, ignorables, parent.getInternalLocaleInfo(), translatedOnly);
        } else {
            setOri = LocaleStore.getLevelLocales(context, ignorables, null, translatedOnly);
        }
        if (setOri == null) {
            return new HashSet();
        }
        HashSet<LocaleInfo> retSet = new HashSet();
        for (com.android.internal.app.LocaleStore.LocaleInfo ori : setOri) {
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
}
