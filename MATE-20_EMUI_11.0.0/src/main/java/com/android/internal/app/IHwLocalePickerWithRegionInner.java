package com.android.internal.app;

import android.widget.FrameLayout;
import android.widget.LinearLayout;
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

    void onLocaleSelectedEx(LocaleStore.LocaleInfo localeInfo);

    boolean onQueryTextChangeEx(String str);

    boolean onQueryTextSubmitEx(String str);

    void returnToParentFrameEx();

    void setEmptyView(FrameLayout frameLayout, LinearLayout linearLayout);

    void setSearchView(SearchView searchView);
}
