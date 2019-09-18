package com.huawei.android.app;

import android.os.LocaleList;
import com.android.internal.app.LocalePicker;
import java.util.Locale;

public class LocalePickerEx {
    public static void updateLocale(Locale locale) {
        LocalePicker.updateLocale(locale);
    }

    public static LocaleList getLocales() {
        return LocalePicker.getLocales();
    }

    public static void updateLocales(LocaleList locales) {
        LocalePicker.updateLocales(locales);
    }
}
