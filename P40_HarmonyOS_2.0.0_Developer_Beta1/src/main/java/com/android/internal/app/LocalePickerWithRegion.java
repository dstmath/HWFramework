package com.android.internal.app;

import android.app.ListFragment;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import com.android.hwext.internal.R;
import com.android.internal.app.LocaleHelper;
import com.android.internal.app.LocaleStore;
import com.huawei.android.app.IHwLocalePickerWithRegionEx;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class LocalePickerWithRegion extends ListFragment implements IHwLocalePickerWithRegionInner, SearchView.OnQueryTextListener {
    private static final String PARENT_FRAGMENT_NAME = "localeListEditor";
    private static final String TAG = "LocalePickerWithRegion";
    private boolean isShowAddedHeaders;
    private SuggestedLocaleAdapter mAdapter;
    private Context mContext;
    private int mFirstVisiblePosition = 0;
    IHwLocalePickerWithRegionEx mHwLPWEx;
    private LocaleSelectedListener mListener;
    private Set<LocaleStore.LocaleInfo> mLocaleList;
    private LocaleStore.LocaleInfo mParentLocale;
    private CharSequence mPreviousSearch = null;
    private boolean mPreviousSearchHadFocus = false;
    private SearchView mSearchView = null;
    private int mTopDistance = 0;
    private boolean mTranslatedOnly = false;

    public interface LocaleSelectedListener {
        void onLocaleSelected(LocaleStore.LocaleInfo localeInfo);
    }

    private static LocalePickerWithRegion createCountryPicker(Context context, LocaleSelectedListener listener, LocaleStore.LocaleInfo parent, boolean translatedOnly) {
        LocalePickerWithRegion localePicker = new LocalePickerWithRegion();
        if (localePicker.setListener(context, listener, parent, translatedOnly)) {
            return localePicker;
        }
        return null;
    }

    public static LocalePickerWithRegion createLanguagePicker(Context context, LocaleSelectedListener listener, boolean translatedOnly) {
        LocalePickerWithRegion localePicker = new LocalePickerWithRegion();
        localePicker.setListener(context, listener, null, translatedOnly);
        return localePicker;
    }

    private boolean setListener(Context context, LocaleSelectedListener listener, LocaleStore.LocaleInfo parent, boolean translatedOnly) {
        this.mParentLocale = parent;
        this.mListener = listener;
        this.mTranslatedOnly = translatedOnly;
        this.mContext = context;
        setRetainInstance(true);
        this.mHwLPWEx = HwFrameworkFactory.getHwLocalePickerWithRegionEx(this);
        StringBuilder sb = new StringBuilder();
        sb.append("setListener, mHwLPWEx is null =");
        sb.append(this.mHwLPWEx == null);
        Log.i(TAG, sb.toString());
        HashSet<String> langTagsToIgnore = new HashSet<>();
        if (!translatedOnly) {
            Collections.addAll(langTagsToIgnore, LocalePicker.getLocales().toLanguageTags().split(SmsManager.REGEX_PREFIX_DELIMITER));
        }
        if (parent != null) {
            this.mLocaleList = HwFrameworkFactory.getHwLocaleStoreEx().getRegionLocales(context, parent.getLocale(), new LocaleStore());
        } else {
            this.mLocaleList = HwFrameworkFactory.getHwLocaleStoreEx().getLanguageLocales(context, new LocaleStore());
        }
        return true;
    }

    private void returnToParentFrame() {
        getFragmentManager().popBackStack(PARENT_FRAGMENT_NAME, 1);
    }

    @Override // android.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean z = true;
        setHasOptionsMenu(true);
        if (this.mLocaleList == null) {
            returnToParentFrame();
            return;
        }
        boolean countryMode = this.mParentLocale != null;
        if (countryMode) {
            z = false;
        }
        this.isShowAddedHeaders = z;
        Locale sortingLocale = countryMode ? this.mParentLocale.getLocale() : Locale.getDefault();
        this.mAdapter = new SuggestedLocaleAdapter(this.mLocaleList, countryMode, this.mContext, this.isShowAddedHeaders);
        this.mAdapter.sort(new LocaleHelper.LocaleInfoComparator(sortingLocale, countryMode));
        setListAdapter(this.mAdapter);
    }

    @Override // android.app.ListFragment, android.app.Fragment
    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.mHwLPWEx.onViewCreated(getActivity(), view, savedInstanceState);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override // android.app.Fragment
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 16908332) {
            return super.onOptionsItemSelected(menuItem);
        }
        getFragmentManager().popBackStack();
        return true;
    }

    @Override // android.app.Fragment
    public void onResume() {
        super.onResume();
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            if (this.mParentLocale != null) {
                getActivity().getActionBar().setTitle(getText(R.string.language_region_selection_title));
            } else {
                getActivity().getActionBar().setTitle(com.android.internal.R.string.language_selection_title);
            }
        }
        getListView().requestFocus();
    }

    @Override // android.app.Fragment
    public void onPause() {
        super.onPause();
        SearchView searchView = this.mSearchView;
        int i = 0;
        if (searchView != null) {
            this.mPreviousSearchHadFocus = searchView.hasFocus();
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

    @Override // android.app.ListFragment
    public void onListItemClick(ListView l, View v, int position, long id) {
        this.mHwLPWEx.chooseLanguageOrRegion(this.mAdapter.getIsClickable(), this.mContext, (LocaleStore.LocaleInfo) getListAdapter().getItem(position), position, this.mParentLocale == null);
    }

    @Override // android.widget.SearchView.OnQueryTextListener
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override // android.widget.SearchView.OnQueryTextListener
    public boolean onQueryTextChange(String newText) {
        SuggestedLocaleAdapter suggestedLocaleAdapter = this.mAdapter;
        if (suggestedLocaleAdapter == null) {
            return false;
        }
        suggestedLocaleAdapter.getFilter().filter(newText);
        return false;
    }

    @Override // com.android.internal.app.IHwLocalePickerWithRegionInner
    public void createCountry(LocaleStore.LocaleInfo locale) {
        LocalePickerWithRegion selector = createCountryPicker(getContext(), this.mListener, locale, this.mTranslatedOnly);
        if (selector != null) {
            getFragmentManager().beginTransaction().setTransition(4097).replace(getId(), selector).addToBackStack(null).commit();
        }
    }

    @Override // com.android.internal.app.IHwLocalePickerWithRegionInner
    public void returnToParentFrameEx() {
        returnToParentFrame();
    }

    @Override // com.android.internal.app.IHwLocalePickerWithRegionInner
    public void onLocaleSelectedEx(LocaleStore.LocaleInfo locale) {
        LocaleSelectedListener localeSelectedListener = this.mListener;
        if (localeSelectedListener != null) {
            localeSelectedListener.onLocaleSelected(locale);
        }
    }

    public static LocalePickerWithRegion createCountryPickerEx(Context context, LocaleSelectedListener listener, LocaleStore.LocaleInfo parent, boolean translatedOnly) {
        return createCountryPicker(context, listener, parent, translatedOnly);
    }

    @Override // android.app.ListFragment, android.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.mHwLPWEx == null) {
            Log.w(TAG, "onCreateView,mHwLPWEx is null");
            this.mHwLPWEx = HwFrameworkFactory.getHwLocalePickerWithRegionEx(this);
        }
        return this.mHwLPWEx.onCreateView(inflater, container, savedInstanceState);
    }

    @Override // com.android.internal.app.IHwLocalePickerWithRegionInner
    public void setSearchView(SearchView searchView) {
        this.mSearchView = searchView;
    }

    @Override // com.android.internal.app.IHwLocalePickerWithRegionInner
    public void setEmptyView(FrameLayout listviewContainer, LinearLayout emptyRoot) {
        SuggestedLocaleAdapter suggestedLocaleAdapter = this.mAdapter;
        if (suggestedLocaleAdapter != null) {
            suggestedLocaleAdapter.setEmptyView(listviewContainer, emptyRoot);
        }
    }

    @Override // com.android.internal.app.IHwLocalePickerWithRegionInner
    public boolean getPreviousSearchHadFocus() {
        return this.mPreviousSearchHadFocus;
    }

    @Override // com.android.internal.app.IHwLocalePickerWithRegionInner
    public CharSequence getPreviousSearch() {
        return this.mPreviousSearch;
    }

    @Override // com.android.internal.app.IHwLocalePickerWithRegionInner
    public int getFirstVisiblePosition() {
        return this.mFirstVisiblePosition;
    }

    @Override // com.android.internal.app.IHwLocalePickerWithRegionInner
    public int getTopDistance() {
        return this.mTopDistance;
    }

    @Override // com.android.internal.app.IHwLocalePickerWithRegionInner
    public ListView getListViewEx() {
        return getListView();
    }

    @Override // com.android.internal.app.IHwLocalePickerWithRegionInner
    public CharSequence getTextEx(int resId) {
        return getText(resId);
    }

    @Override // com.android.internal.app.IHwLocalePickerWithRegionInner
    public boolean onQueryTextSubmitEx(String query) {
        return onQueryTextSubmit(query);
    }

    @Override // com.android.internal.app.IHwLocalePickerWithRegionInner
    public boolean onQueryTextChangeEx(String newText) {
        return onQueryTextChange(newText);
    }

    @Override // com.android.internal.app.IHwLocalePickerWithRegionInner
    public LocaleStore.LocaleInfo getParentLocaleEx() {
        return this.mParentLocale;
    }
}
