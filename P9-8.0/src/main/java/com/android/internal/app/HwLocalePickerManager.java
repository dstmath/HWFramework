package com.android.internal.app;

import android.content.Context;
import com.android.internal.app.LocalePicker.LocaleInfo;
import java.util.Locale;

public interface HwLocalePickerManager {
    LocaleInfo[] addModifyLocaleInfos(Context context, LocaleInfo[] localeInfoArr);

    boolean checkCustLanguages(Context context, String str);

    String getLanguageNameOnly(String str);

    Locale getLnumLocale(Locale locale);

    void initParams(Context context);
}
