package ohos.global.icu.impl;

import ohos.global.icu.impl.LocaleDisplayNamesImpl;
import ohos.global.icu.util.ULocale;

public class ICULangDataTables extends LocaleDisplayNamesImpl.ICUDataTables {
    @Override // ohos.global.icu.impl.LocaleDisplayNamesImpl.ICUDataTables, ohos.global.icu.impl.LocaleDisplayNamesImpl.DataTables
    public /* bridge */ /* synthetic */ LocaleDisplayNamesImpl.DataTable get(ULocale uLocale, boolean z) {
        return super.get(uLocale, z);
    }

    public ICULangDataTables() {
        super(ICUData.ICU_LANG_BASE_NAME);
    }
}
