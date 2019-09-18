package android.icu.impl;

import android.icu.impl.LocaleDisplayNamesImpl;
import android.icu.util.ULocale;

public class ICURegionDataTables extends LocaleDisplayNamesImpl.ICUDataTables {
    public /* bridge */ /* synthetic */ LocaleDisplayNamesImpl.DataTable get(ULocale uLocale, boolean z) {
        return super.get(uLocale, z);
    }

    public ICURegionDataTables() {
        super(ICUData.ICU_REGION_BASE_NAME);
    }
}
