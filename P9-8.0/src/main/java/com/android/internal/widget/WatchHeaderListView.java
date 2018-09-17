package com.android.internal.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ListView.FixedViewInfo;
import java.util.ArrayList;
import java.util.function.Predicate;

public class WatchHeaderListView extends ListView {
    private View mTopPanel;

    private static class WatchHeaderListAdapter extends HeaderViewListAdapter {
        private View mTopPanel;

        public WatchHeaderListAdapter(ArrayList<FixedViewInfo> headerViewInfos, ArrayList<FixedViewInfo> footerViewInfos, ListAdapter adapter) {
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
            return getTopPanelCount() == 0 ? super.areAllItemsEnabled() : false;
        }

        public boolean isEnabled(int position) {
            int topPanelCount = getTopPanelCount();
            return position < topPanelCount ? false : super.isEnabled(position - topPanelCount);
        }

        public Object getItem(int position) {
            int topPanelCount = getTopPanelCount();
            return position < topPanelCount ? null : super.getItem(position - topPanelCount);
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

    protected HeaderViewListAdapter wrapHeaderListAdapterInternal(ArrayList<FixedViewInfo> headerViewInfos, ArrayList<FixedViewInfo> footerViewInfos, ListAdapter adapter) {
        return new WatchHeaderListAdapter(headerViewInfos, footerViewInfos, adapter);
    }

    public void addView(View child, LayoutParams params) {
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

    protected View findViewTraversal(int id) {
        View v = super.findViewTraversal(id);
        if (v != null || this.mTopPanel == null || (this.mTopPanel.isRootNamespace() ^ 1) == 0) {
            return v;
        }
        return this.mTopPanel.findViewById(id);
    }

    protected View findViewWithTagTraversal(Object tag) {
        View v = super.findViewWithTagTraversal(tag);
        if (v != null || this.mTopPanel == null || (this.mTopPanel.isRootNamespace() ^ 1) == 0) {
            return v;
        }
        return this.mTopPanel.findViewWithTag(tag);
    }

    protected <T extends View> T findViewByPredicateTraversal(Predicate<View> predicate, View childToSkip) {
        View v = super.findViewByPredicateTraversal(predicate, childToSkip);
        if (v != null || this.mTopPanel == null || this.mTopPanel == childToSkip || (this.mTopPanel.isRootNamespace() ^ 1) == 0) {
            return v;
        }
        return this.mTopPanel.findViewByPredicate(predicate);
    }

    public int getHeaderViewsCount() {
        if (this.mTopPanel == null) {
            return super.getHeaderViewsCount();
        }
        return (this.mTopPanel.getVisibility() == 8 ? 0 : 1) + super.getHeaderViewsCount();
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
