package com.android.internal.app;

import android.content.Context;
import com.android.internal.app.LocaleStore;
import java.util.Set;

public interface IHwLocaleStoreInner {
    String getLangScriptKeyEx(LocaleStore.LocaleInfo localeInfo);

    LocaleStore.LocaleInfo getLanguageLocaleInfo(String str);

    Set<String> getSimCountriesEx(Context context);

    boolean getSuggestionTypeSim(LocaleStore.LocaleInfo localeInfo);

    boolean isSuggestedLocale(LocaleStore.LocaleInfo localeInfo);

    void setPseudo(LocaleStore.LocaleInfo localeInfo, boolean z);

    void setSuggestionTypeCfg(LocaleStore.LocaleInfo localeInfo);

    void setSuggestionTypeSim(LocaleStore.LocaleInfo localeInfo);
}
