package android.icu.impl;

import android.icu.impl.LocaleDisplayNamesImpl.DataTable;
import android.icu.util.ULocale;

public class ICURegionDataTables extends ICUDataTables {
    public /* bridge */ /* synthetic */ DataTable get(ULocale locale) {
        return super.get(locale);
    }

    public ICURegionDataTables() {
        super(ICUResourceBundle.ICU_REGION_BASE_NAME);
    }
}
