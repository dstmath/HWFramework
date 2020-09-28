package com.android.internal.app;

import android.content.Context;
import com.android.internal.app.LocalePicker;
import java.util.Locale;

public interface HwLocalePickerManager {
    LocalePicker.LocaleInfo[] addModifyLocaleInfos(Context context, LocalePicker.LocaleInfo[] localeInfoArr);

    boolean checkCustLanguages(Context context, String str);

    String getLanguageNameOnly(String str);

    Locale getLnumLocale(Locale locale);

    void initParams(Context context);
}
