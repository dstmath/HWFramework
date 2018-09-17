package android.widget;

import android.database.DataSetObserver;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView.FixedViewInfo;
import java.util.ArrayList;

public class HeaderViewListAdapter implements WrapperListAdapter, Filterable {
    static final ArrayList<FixedViewInfo> EMPTY_INFO_LIST = new ArrayList();
    private final ListAdapter mAdapter;
    boolean mAreAllFixedViewsSelectable;
    ArrayList<FixedViewInfo> mFooterViewInfos;
    ArrayList<FixedViewInfo> mHeaderViewInfos;
    private final boolean mIsFilterable;

    public HeaderViewListAdapter(ArrayList<FixedViewInfo> headerViewInfos, ArrayList<FixedViewInfo> footerViewInfos, ListAdapter adapter) {
        boolean areAllListInfosSelectable;
        this.mAdapter = adapter;
        this.mIsFilterable = adapter instanceof Filterable;
        if (headerViewInfos == null) {
            this.mHeaderViewInfos = EMPTY_INFO_LIST;
        } else {
            this.mHeaderViewInfos = headerViewInfos;
        }
        if (footerViewInfos == null) {
            this.mFooterViewInfos = EMPTY_INFO_LIST;
        } else {
            this.mFooterViewInfos = footerViewInfos;
        }
        if (areAllListInfosSelectable(this.mHeaderViewInfos)) {
            areAllListInfosSelectable = areAllListInfosSelectable(this.mFooterViewInfos);
        } else {
            areAllListInfosSelectable = false;
        }
        this.mAreAllFixedViewsSelectable = areAllListInfosSelectable;
    }

    public int getHeadersCount() {
        return this.mHeaderViewInfos.size();
    }

    public int getFootersCount() {
        return this.mFooterViewInfos.size();
    }

    public boolean isEmpty() {
        return this.mAdapter != null ? this.mAdapter.isEmpty() : true;
    }

    private boolean areAllListInfosSelectable(ArrayList<FixedViewInfo> infos) {
        if (infos != null) {
            for (FixedViewInfo info : infos) {
                if (!info.isSelectable) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean removeHeader(View v) {
        boolean z = false;
        for (int i = 0; i < this.mHeaderViewInfos.size(); i++) {
            if (((FixedViewInfo) this.mHeaderViewInfos.get(i)).view == v) {
                this.mHeaderViewInfos.remove(i);
                if (areAllListInfosSelectable(this.mHeaderViewInfos)) {
                    z = areAllListInfosSelectable(this.mFooterViewInfos);
                }
                this.mAreAllFixedViewsSelectable = z;
                return true;
            }
        }
        return false;
    }

    public boolean removeFooter(View v) {
        boolean z = false;
        for (int i = 0; i < this.mFooterViewInfos.size(); i++) {
            if (((FixedViewInfo) this.mFooterViewInfos.get(i)).view == v) {
                this.mFooterViewInfos.remove(i);
                if (areAllListInfosSelectable(this.mHeaderViewInfos)) {
                    z = areAllListInfosSelectable(this.mFooterViewInfos);
                }
                this.mAreAllFixedViewsSelectable = z;
                return true;
            }
        }
        return false;
    }

    public int getCount() {
        if (this.mAdapter != null) {
            return (getFootersCount() + getHeadersCount()) + this.mAdapter.getCount();
        }
        return getFootersCount() + getHeadersCount();
    }

    public boolean areAllItemsEnabled() {
        if (this.mAdapter == null) {
            return true;
        }
        return this.mAreAllFixedViewsSelectable ? this.mAdapter.areAllItemsEnabled() : false;
    }

    public boolean isEnabled(int position) {
        int numHeaders = getHeadersCount();
        if (position < numHeaders) {
            return ((FixedViewInfo) this.mHeaderViewInfos.get(position)).isSelectable;
        }
        int adjPosition = position - numHeaders;
        int adapterCount = 0;
        if (this.mAdapter != null) {
            adapterCount = this.mAdapter.getCount();
            if (adjPosition < adapterCount) {
                return this.mAdapter.isEnabled(adjPosition);
            }
        }
        if (adjPosition - adapterCount < getFootersCount()) {
            return ((FixedViewInfo) this.mFooterViewInfos.get(adjPosition - adapterCount)).isSelectable;
        }
        Log.e("HeaderViewListAdapter", "throwIndexOutOfBoundsException");
        return false;
    }

    public Object getItem(int position) {
        int numHeaders = getHeadersCount();
        if (position < numHeaders) {
            return ((FixedViewInfo) this.mHeaderViewInfos.get(position)).data;
        }
        int adjPosition = position - numHeaders;
        int adapterCount = 0;
        if (this.mAdapter != null) {
            adapterCount = this.mAdapter.getCount();
            if (adjPosition < adapterCount) {
                return this.mAdapter.getItem(adjPosition);
            }
        }
        return ((FixedViewInfo) this.mFooterViewInfos.get(adjPosition - adapterCount)).data;
    }

    public long getItemId(int position) {
        int numHeaders = getHeadersCount();
        if (this.mAdapter != null && position >= numHeaders) {
            int adjPosition = position - numHeaders;
            if (adjPosition < this.mAdapter.getCount()) {
                return this.mAdapter.getItemId(adjPosition);
            }
        }
        return -1;
    }

    public boolean hasStableIds() {
        if (this.mAdapter != null) {
            return this.mAdapter.hasStableIds();
        }
        return false;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        int numHeaders = getHeadersCount();
        if (position < numHeaders) {
            return ((FixedViewInfo) this.mHeaderViewInfos.get(position)).view;
        }
        int adjPosition = position - numHeaders;
        int adapterCount = 0;
        if (this.mAdapter != null) {
            adapterCount = this.mAdapter.getCount();
            if (adjPosition < adapterCount) {
                return this.mAdapter.getView(adjPosition, convertView, parent);
            }
        }
        return ((FixedViewInfo) this.mFooterViewInfos.get(adjPosition - adapterCount)).view;
    }

    public int getItemViewType(int position) {
        int numHeaders = getHeadersCount();
        if (this.mAdapter != null && position >= numHeaders) {
            int adjPosition = position - numHeaders;
            if (adjPosition < this.mAdapter.getCount()) {
                return this.mAdapter.getItemViewType(adjPosition);
            }
        }
        return -2;
    }

    public int getViewTypeCount() {
        if (this.mAdapter != null) {
            return this.mAdapter.getViewTypeCount();
        }
        return 1;
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        if (this.mAdapter != null) {
            this.mAdapter.registerDataSetObserver(observer);
        }
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (this.mAdapter != null) {
            this.mAdapter.unregisterDataSetObserver(observer);
        }
    }

    public Filter getFilter() {
        if (this.mIsFilterable) {
            return ((Filterable) this.mAdapter).getFilter();
        }
        return null;
    }

    public ListAdapter getWrappedAdapter() {
        return this.mAdapter;
    }
}
