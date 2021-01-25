package com.huawei.android.app;

import android.content.Context;
import com.android.internal.app.LocaleStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public interface IHwLocaleHelperEx {
    ArrayList<String> getBlackLocales(Context context);

    int getCompareIntEx(LocaleStore.LocaleInfo localeInfo, LocaleStore.LocaleInfo localeInfo2, List<String> list);

    List<String> getRelatedLocalesEx();

    String replaceCountryName(Locale locale, Locale locale2, String str);

    String replaceDisplayName(Locale locale, Locale locale2, String str);
}
