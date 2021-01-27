package com.android.internal.app;

import com.android.internal.app.LocaleStore;
import java.util.ArrayList;

public interface IHwSuggestedLocaleAdapterInner {
    int getItemViewTypeEx(int i);

    String getLocaleInfoScript(LocaleStore.LocaleInfo localeInfo);

    ArrayList<LocaleStore.LocaleInfo> getLocaleOptions();

    ArrayList<LocaleStore.LocaleInfo> getOriginalLocaleOptions();

    int getSuggestionCount();

    boolean isCountryMode();

    boolean isShowHeaders();

    boolean isSuggestedLocale(LocaleStore.LocaleInfo localeInfo);

    void setCountryMode(boolean z);

    void setSuggestionCount(int i);
}
