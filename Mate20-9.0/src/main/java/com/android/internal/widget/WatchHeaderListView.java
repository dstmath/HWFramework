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

    private static class WatchHeaderListAdapter extends HeaderViewListAdapter {
        private View mTopPanel;

        public WatchHeaderListAdapter(ArrayList<ListView.FixedViewInfo> headerViewInfos, ArrayList<ListView.FixedViewInfo> footerViewInfos, ListAdapter adapter) {
            super(headerViewInfos, footerViewInfos, adapter);
        }

        public void setTopPanel(View v) {
            this.mTopPanel = v;
        }

        private int getTopPanelCount() {
            return (this.mTopPanel == null || this.mTopPanel.getVisibility() == 8) ? 0 : 1;
        }

        public int getCount() {
            return super.getCount() + getTopPanelCount();
        }

        public boolean areAllItemsEnabled() {
            return getTopPanelCount() == 0 && super.areAllItemsEnabled();
        }

        public boolean isEnabled(int position) {
            int topPanelCount = getTopPanelCount();
            if (position < topPanelCount) {
                return false;
            }
            return super.isEnabled(position - topPanelCount);
        }

        public Object getItem(int position) {
            int topPanelCount = getTopPanelCount();
            if (position < topPanelCount) {
                return null;
            }
            return super.getItem(position - topPanelCount);
        }

        public long getItemId(int position) {
            int numHeaders = getHeadersCount() + getTopPanelCount();
            if (getWrappedAdapter() != null && position >= numHeaders) {
                int adjPosition = position - numHeaders;
                if (adjPosition < getWrappedAdapter().getCount()) {
                    return getWrappedAdapter().getItemId(adjPosition);
                }
            }
            return -1;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            int topPanelCount = getTopPanelCount();
            return position < topPanelCount ? this.mTopPanel : super.getView(position - topPanelCount, convertView, parent);
        }

        public int getItemViewType(int position) {
            int numHeaders = getHeadersCount() + getTopPanelCount();
            if (getWrappedAdapter() != null && position >= numHeaders) {
                int adjPosition = position - numHeaders;
                if (adjPosition < getWrappedAdapter().getCount()) {
                    return getWrappedAdapter().getItemViewType(adjPosition);
                }
            }
            return -2;
        }
    }

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
    public HeaderViewListAdapter wrapHeaderListAdapterInternal(ArrayList<ListView.FixedViewInfo> headerViewInfos, ArrayList<ListView.FixedViewInfo> footerViewInfos, ListAdapter adapter) {
        return new WatchHeaderListAdapter(headerViewInfos, footerViewInfos, adapter);
    }

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

    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        wrapAdapterIfNecessary();
    }

    /* access modifiers changed from: protected */
    public View findViewTraversal(int id) {
        View v = super.findViewTraversal(id);
        if (v != null || this.mTopPanel == null || this.mTopPanel.isRootNamespace()) {
            return v;
        }
        return this.mTopPanel.findViewById(id);
    }

    /* access modifiers changed from: protected */
    public View findViewWithTagTraversal(Object tag) {
        View v = super.findViewWithTagTraversal(tag);
        if (v != null || this.mTopPanel == null || this.mTopPanel.isRootNamespace()) {
            return v;
        }
        return this.mTopPanel.findViewWithTag(tag);
    }

    /* access modifiers changed from: protected */
    public <T extends View> T findViewByPredicateTraversal(Predicate<View> predicate, View childToSkip) {
        View v = super.findViewByPredicateTraversal(predicate, childToSkip);
        if (v != null || this.mTopPanel == null || this.mTopPanel == childToSkip || this.mTopPanel.isRootNamespace()) {
            return v;
        }
        return this.mTopPanel.findViewByPredicate(predicate);
    }

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
}
