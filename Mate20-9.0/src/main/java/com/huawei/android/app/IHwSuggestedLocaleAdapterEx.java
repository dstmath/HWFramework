package com.huawei.android.app;

import android.view.View;
import com.android.internal.app.LocaleHelper;
import com.android.internal.app.LocaleStore;
import java.util.ArrayList;
import java.util.Set;

public interface IHwSuggestedLocaleAdapterEx {
    void changeAddedLanguagesPos(LocaleHelper.LocaleInfoComparator localeInfoComparator, ArrayList<LocaleStore.LocaleInfo> arrayList);

    ArrayList<LocaleStore.LocaleInfo> changePerformFiltering(CharSequence charSequence);

    int getSuggestionCount(CharSequence charSequence, int i);

    void init(Set<LocaleStore.LocaleInfo> set, ArrayList<LocaleStore.LocaleInfo> arrayList);

    boolean isAddedLanguages();

    int preGetCount(ArrayList<LocaleStore.LocaleInfo> arrayList);

    int preGetItemViewType(int i);

    Object rePlaceGetItem(int i, ArrayList<LocaleStore.LocaleInfo> arrayList);

    void setDefaultItemDivider(View view, int i, int i2);

    void setHeaderDivider(View view, int i);
}
