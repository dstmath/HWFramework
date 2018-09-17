package android.icu.impl;

import android.icu.impl.LocaleDisplayNamesImpl.DataTable;
import android.icu.util.ULocale;

public class ICULangDataTables extends ICUDataTables {
    public /* bridge */ /* synthetic */ DataTable get(ULocale locale) {
        return super.get(locale);
    }

    public ICULangDataTables() {
        super(ICUResourceBundle.ICU_LANG_BASE_NAME);
    }
}
