package com.android.internal.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.function.Predicate;

public class WatchHeaderListView extends ListView {
    private View mTopPanel;

    public WatchHeaderListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WatchHeaderListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public WatchHeaderListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ListView
    public HeaderViewListAdapter wrapHeaderListAdapterInternal(ArrayList<ListView.FixedViewInfo> headerViewInfos, ArrayList<ListView.FixedViewInfo> footerViewInfos, ListAdapter adapter) {
        return new WatchHeaderListAdapter(headerViewInfos, footerViewInfos, adapter);
    }

    @Override // android.widget.AdapterView, android.view.ViewGroup, android.view.ViewManager
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (this.mTopPanel == null) {
            setTopPanel(child);
            return;
        }
        throw new IllegalStateException("WatchHeaderListView can host only one header");
    }

    public void setTopPanel(View v) {
        this.mTopPanel = v;
        wrapAdapterIfNecessary();
    }

    @Override // android.widget.ListView, android.widget.AbsListView
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        wrapAdapterIfNecessary();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ListView, android.view.ViewGroup, android.view.View
    public View findViewTraversal(int id) {
        View view;
        View v = super.findViewTraversal(id);
        if (v != null || (view = this.mTopPanel) == null || view.isRootNamespace()) {
            return v;
        }
        return this.mTopPanel.findViewById(id);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ListView, android.view.ViewGroup, android.view.View
    public View findViewWithTagTraversal(Object tag) {
        View view;
        View v = super.findViewWithTagTraversal(tag);
        if (v != null || (view = this.mTopPanel) == null || view.isRootNamespace()) {
            return v;
        }
        return this.mTopPanel.findViewWithTag(tag);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ListView, android.view.ViewGroup, android.view.View
    public <T extends View> T findViewByPredicateTraversal(Predicate<View> predicate, View childToSkip) {
        View view;
        T t = (T) super.findViewByPredicateTraversal(predicate, childToSkip);
        return (t != null || (view = this.mTopPanel) == null || view == childToSkip || view.isRootNamespace()) ? t : (T) this.mTopPanel.findViewByPredicate(predicate);
    }

    @Override // android.widget.ListView, android.widget.AbsListView
    public int getHeaderViewsCount() {
        if (this.mTopPanel == null) {
            return super.getHeaderViewsCount();
        }
        return super.getHeaderViewsCount() + (this.mTopPanel.getVisibility() == 8 ? 0 : 1);
    }

    private void wrapAdapterIfNecessary() {
        ListAdapter adapter = getAdapter();
        if (adapter != null && this.mTopPanel != null) {
            if (!(adapter instanceof WatchHeaderListAdapter)) {
                wrapHeaderListAdapterInternal();
            }
            ((WatchHeaderListAdapter) getAdapter()).setTopPanel(this.mTopPanel);
            dispatchDataSetObserverOnChangedInternal();
        }
    }

    /* access modifiers changed from: private */
    public static class WatchHeaderListAdapter extends HeaderViewListAdapter {
        private View mTopPanel;

        public WatchHeaderListAdapter(ArrayList<ListView.FixedViewInfo> headerViewInfos, ArrayList<ListView.FixedViewInfo> footerViewInfos, ListAdapter adapter) {
            super(headerViewInfos, footerViewInfos, adapter);
        }

        public void setTopPanel(View v) {
            this.mTopPanel = v;
        }

        private int getTopPanelCount() {
            View view = this.mTopPanel;
            return (view == null || view.getVisibility() == 8) ? 0 : 1;
        }

        @Override // android.widget.HeaderViewListAdapter, android.widget.Adapter
        public int getCount() {
            return super.getCount() + getTopPanelCount();
        }

        @Override // android.widget.HeaderViewListAdapter, android.widget.ListAdapter
        public boolean areAllItemsEnabled() {
            return getTopPanelCount() == 0 && super.areAllItemsEnabled();
        }

        @Override // android.widget.HeaderViewListAdapter, android.widget.ListAdapter
        public boolean isEnabled(int position) {
            int topPanelCount = getTopPanelCount();
            if (position < topPanelCount) {
                return false;
            }
            return super.isEnabled(position - topPanelCount);
        }

        @Override // android.widget.HeaderViewListAdapter, android.widget.Adapter
        public Object getItem(int position) {
            int topPanelCount = getTopPanelCount();
            if (position < topPanelCount) {
                return null;
            }
            return super.getItem(position - topPanelCount);
        }

        @Override // android.widget.HeaderViewListAdapter, android.widget.Adapter
        public long getItemId(int position) {
            int adjPosition;
            int numHeaders = getHeadersCount() + getTopPanelCount();
            if (getWrappedAdapter() == null || position < numHeaders || (adjPosition = position - numHeaders) >= getWrappedAdapter().getCount()) {
                return -1;
            }
            return getWrappedAdapter().getItemId(adjPosition);
        }

        @Override // android.widget.HeaderViewListAdapter, android.widget.Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            int topPanelCount = getTopPanelCount();
            return position < topPanelCount ? this.mTopPanel : super.getView(position - topPanelCount, convertView, parent);
        }

        @Override // android.widget.HeaderViewListAdapter, android.widget.Adapter
        public int getItemViewType(int position) {
            int adjPosition;
            int numHeaders = getHeadersCount() + getTopPanelCount();
            if (getWrappedAdapter() == null || position < numHeaders || (adjPosition = position - numHeaders) >= getWrappedAdapter().getCount()) {
                return -2;
            }
            return getWrappedAdapter().getItemViewType(adjPosition);
        }
    }
}
