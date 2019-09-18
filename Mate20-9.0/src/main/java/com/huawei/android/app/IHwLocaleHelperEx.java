package com.huawei.android.app;

import com.android.internal.app.LocaleStore;
import java.util.List;

public interface IHwLocaleHelperEx {
    int getCompareIntEx(LocaleStore.LocaleInfo localeInfo, LocaleStore.LocaleInfo localeInfo2, List<String> list);

    List<String> getRelatedLocalesEx();
}
