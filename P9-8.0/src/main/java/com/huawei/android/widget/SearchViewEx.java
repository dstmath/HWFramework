package com.huawei.android.widget;

import android.view.View.OnTouchListener;
import android.widget.SearchView;

public class SearchViewEx {
    public static void setQueryTextOnTouchListener(SearchView searchView, OnTouchListener onTouchListener) {
        if (searchView != null && searchView.getSearchSrcTextView() != null && onTouchListener != null) {
            searchView.getSearchSrcTextView().setOnTouchListener(onTouchListener);
        }
    }

    public static boolean isSearchViewFocused(SearchView searchView) {
        if (searchView == null || searchView.getSearchSrcTextView() == null) {
            return false;
        }
        return searchView.getSearchSrcTextView().isFocused();
    }
}
