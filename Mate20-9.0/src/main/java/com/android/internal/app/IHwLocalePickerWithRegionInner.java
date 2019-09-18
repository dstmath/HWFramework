package com.android.internal.app;

import android.widget.ListView;
import android.widget.SearchView;
import com.android.internal.app.LocaleStore;

public interface IHwLocalePickerWithRegionInner {
    void createCountry(LocaleStore.LocaleInfo localeInfo);

    int getFirstVisiblePosition();

    ListView getListViewEx();

    LocaleStore.LocaleInfo getParentLocaleEx();

    CharSequence getPreviousSearch();

    boolean getPreviousSearchHadFocus();

    CharSequence getTextEx(int i);

    int getTopDistance();

    void onLocaleSelectedEx(LocaleStore.LocaleInfo localeInfo, boolean z);

    boolean onQueryTextChangeEx(String str);

    boolean onQueryTextSubmitEx(String str);

    void returnToParentFrameEx();

    void setSearchView(SearchView searchView);
}
