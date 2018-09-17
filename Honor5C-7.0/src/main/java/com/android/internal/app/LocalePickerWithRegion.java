package com.android.internal.app;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.PtmLog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import com.android.internal.R;
import com.android.internal.app.LocaleHelper.LocaleInfoComparator;
import com.android.internal.app.LocaleStore.LocaleInfo;
import com.huawei.hwperformance.HwPerformance;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class LocalePickerWithRegion extends ListFragment implements OnQueryTextListener {
    private static final String PARENT_FRAGMENT_NAME = "localeListEditor";
    private SuggestedLocaleAdapter mAdapter;
    private int mFirstVisiblePosition;
    private LocaleSelectedListener mListener;
    private Set<LocaleInfo> mLocaleList;
    private LocaleInfo mParentLocale;
    private CharSequence mPreviousSearch;
    private boolean mPreviousSearchHadFocus;
    private SearchView mSearchView;
    private int mTopDistance;
    private boolean mTranslatedOnly;

    public interface LocaleSelectedListener {
        void onLocaleSelected(LocaleInfo localeInfo);
    }

    public LocalePickerWithRegion() {
        this.mTranslatedOnly = false;
        this.mSearchView = null;
        this.mPreviousSearch = null;
        this.mPreviousSearchHadFocus = false;
        this.mFirstVisiblePosition = 0;
        this.mTopDistance = 0;
    }

    private static LocalePickerWithRegion createCountryPicker(Context context, LocaleSelectedListener listener, LocaleInfo parent, boolean translatedOnly) {
        LocalePickerWithRegion localePicker = new LocalePickerWithRegion();
        return localePicker.setListener(context, listener, parent, translatedOnly) ? localePicker : null;
    }

    public static LocalePickerWithRegion createLanguagePicker(Context context, LocaleSelectedListener listener, boolean translatedOnly) {
        LocalePickerWithRegion localePicker = new LocalePickerWithRegion();
        localePicker.setListener(context, listener, null, translatedOnly);
        return localePicker;
    }

    private boolean setListener(Context context, LocaleSelectedListener listener, LocaleInfo parent, boolean translatedOnly) {
        this.mParentLocale = parent;
        this.mListener = listener;
        this.mTranslatedOnly = translatedOnly;
        setRetainInstance(true);
        HashSet<String> langTagsToIgnore = new HashSet();
        if (!translatedOnly) {
            Collections.addAll(langTagsToIgnore, LocalePicker.getLocales().toLanguageTags().split(PtmLog.PAIRE_DELIMETER));
        }
        if (parent != null) {
            this.mLocaleList = LocaleStore.getLevelLocales(context, langTagsToIgnore, parent, translatedOnly);
            if (this.mLocaleList.size() <= 1) {
                if (listener != null && this.mLocaleList.size() == 1) {
                    listener.onLocaleSelected((LocaleInfo) this.mLocaleList.iterator().next());
                }
                return false;
            }
        }
        this.mLocaleList = LocaleStore.getLevelLocales(context, langTagsToIgnore, null, translatedOnly);
        return true;
    }

    private void returnToParentFrame() {
        getFragmentManager().popBackStack(PARENT_FRAGMENT_NAME, 1);
    }

    public void onCreate(Bundle savedInstanceState) {
        boolean countryMode = true;
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (this.mLocaleList == null) {
            returnToParentFrame();
            return;
        }
        if (this.mParentLocale == null) {
            countryMode = false;
        }
        Locale sortingLocale = countryMode ? this.mParentLocale.getLocale() : Locale.getDefault();
        this.mAdapter = new SuggestedLocaleAdapter(this.mLocaleList, countryMode);
        this.mAdapter.sort(new LocaleInfoComparator(sortingLocale, countryMode));
        setListAdapter(this.mAdapter);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.home /*16908332*/:
                getFragmentManager().popBackStack();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    public void onResume() {
        super.onResume();
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            if (this.mParentLocale != null) {
                getActivity().getActionBar().setTitle(this.mParentLocale.getFullNameNative());
            } else {
                getActivity().getActionBar().setTitle(R.string.language_selection_title);
            }
        }
        getListView().requestFocus();
    }

    public void onPause() {
        int i = 0;
        super.onPause();
        if (this.mSearchView != null) {
            this.mPreviousSearchHadFocus = this.mSearchView.hasFocus();
            this.mPreviousSearch = this.mSearchView.getQuery();
        } else {
            this.mPreviousSearchHadFocus = false;
            this.mPreviousSearch = null;
        }
        ListView list = getListView();
        View firstChild = list.getChildAt(0);
        this.mFirstVisiblePosition = list.getFirstVisiblePosition();
        if (firstChild != null) {
            i = firstChild.getTop() - list.getPaddingTop();
        }
        this.mTopDistance = i;
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        LocaleInfo locale = (LocaleInfo) getListAdapter().getItem(position);
        if (locale.getParent() != null) {
            if (this.mListener != null) {
                this.mListener.onLocaleSelected(locale);
            }
            returnToParentFrame();
            return;
        }
        LocalePickerWithRegion selector = createCountryPicker(getContext(), this.mListener, locale, this.mTranslatedOnly);
        if (selector != null) {
            getFragmentManager().beginTransaction().setTransition(HwPerformance.PERF_EVENT_PROBE).replace(getId(), selector).addToBackStack(null).commit();
        } else {
            returnToParentFrame();
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (this.mParentLocale == null) {
            inflater.inflate(R.menu.language_selection_list, menu);
            MenuItem searchMenuItem = menu.findItem(R.id.locale_search_menu);
            this.mSearchView = (SearchView) searchMenuItem.getActionView();
            this.mSearchView.setQueryHint(getText(R.string.search_language_hint));
            this.mSearchView.setOnQueryTextListener(this);
            if (TextUtils.isEmpty(this.mPreviousSearch)) {
                this.mSearchView.setQuery(null, false);
            } else {
                searchMenuItem.expandActionView();
                this.mSearchView.setIconified(false);
                this.mSearchView.setActivated(true);
                if (this.mPreviousSearchHadFocus) {
                    this.mSearchView.requestFocus();
                }
                this.mSearchView.setQuery(this.mPreviousSearch, true);
            }
            getListView().setSelectionFromTop(this.mFirstVisiblePosition, this.mTopDistance);
        }
    }

    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    public boolean onQueryTextChange(String newText) {
        if (this.mAdapter != null) {
            this.mAdapter.getFilter().filter(newText);
        }
        return false;
    }
}
