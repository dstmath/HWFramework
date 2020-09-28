package com.huawei.android.app;

import android.content.Context;
import com.android.internal.app.IHwLocaleStoreInner;
import com.android.internal.app.LocaleStore;
import java.util.Locale;
import java.util.Set;

public interface IHwLocaleStoreEx {
    String getDialectsName(Context context, String str);

    String getFullLanguageName(Context context, Locale locale, Locale locale2);

    Set<LocaleStore.LocaleInfo> getLanguageLocales(Context context, IHwLocaleStoreInner iHwLocaleStoreInner);

    Set<LocaleStore.LocaleInfo> getRegionLocales(Context context, Locale locale, IHwLocaleStoreInner iHwLocaleStoreInner);

    boolean isSupportRegion(Context context, Locale locale, String str);
}
