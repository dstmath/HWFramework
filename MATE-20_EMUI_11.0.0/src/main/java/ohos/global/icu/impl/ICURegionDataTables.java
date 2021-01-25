package ohos.global.icu.impl;

import ohos.global.icu.impl.LocaleDisplayNamesImpl;
import ohos.global.icu.util.ULocale;

public class ICURegionDataTables extends LocaleDisplayNamesImpl.ICUDataTables {
    @Override // ohos.global.icu.impl.LocaleDisplayNamesImpl.ICUDataTables, ohos.global.icu.impl.LocaleDisplayNamesImpl.DataTables
    public /* bridge */ /* synthetic */ LocaleDisplayNamesImpl.DataTable get(ULocale uLocale, boolean z) {
        return super.get(uLocale, z);
    }

    public ICURegionDataTables() {
        super(ICUData.ICU_REGION_BASE_NAME);
    }
}
