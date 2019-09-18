package android.widget;

import android.database.DataSetObserver;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Iterator;

public class HeaderViewListAdapter implements WrapperListAdapter, Filterable {
    static final ArrayList<ListView.FixedViewInfo> EMPTY_INFO_LIST = new ArrayList<>();
    private final ListAdapter mAdapter;
    boolean mAreAllFixedViewsSelectable;
    ArrayList<ListView.FixedViewInfo> mFooterViewInfos;
    ArrayList<ListView.FixedViewInfo> mHeaderViewInfos;
    private final boolean mIsFilterable;

    public HeaderViewListAdapter(ArrayList<ListView.FixedViewInfo> headerViewInfos, ArrayList<ListView.FixedViewInfo> footerViewInfos, ListAdapter adapter) {
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
        this.mAreAllFixedViewsSelectable = areAllListInfosSelectable(this.mHeaderViewInfos) && areAllListInfosSelectable(this.mFooterViewInfos);
    }

    public int getHeadersCount() {
        return this.mHeaderViewInfos.size();
    }

    public int getFootersCount() {
        return this.mFooterViewInfos.size();
    }

    public boolean isEmpty() {
        return this.mAdapter == null || this.mAdapter.isEmpty();
    }

    private boolean areAllListInfosSelectable(ArrayList<ListView.FixedViewInfo> infos) {
        if (infos != null) {
            Iterator<ListView.FixedViewInfo> it = infos.iterator();
            while (it.hasNext()) {
                if (!it.next().isSelectable) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean removeHeader(View v) {
        boolean z = false;
        for (int i = 0; i < this.mHeaderViewInfos.size(); i++) {
            if (this.mHeaderViewInfos.get(i).view == v) {
                this.mHeaderViewInfos.remove(i);
                if (areAllListInfosSelectable(this.mHeaderViewInfos) && areAllListInfosSelectable(this.mFooterViewInfos)) {
                    z = true;
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
            if (this.mFooterViewInfos.get(i).view == v) {
                this.mFooterViewInfos.remove(i);
                if (areAllListInfosSelectable(this.mHeaderViewInfos) && areAllListInfosSelectable(this.mFooterViewInfos)) {
                    z = true;
                }
                this.mAreAllFixedViewsSelectable = z;
                return true;
            }
        }
        return false;
    }

    public int getCount() {
        if (this.mAdapter != null) {
            return getFootersCount() + getHeadersCount() + this.mAdapter.getCount();
        }
        return getFootersCount() + getHeadersCount();
    }

    public boolean areAllItemsEnabled() {
        boolean z = true;
        if (this.mAdapter == null) {
            return true;
        }
        if (!this.mAreAllFixedViewsSelectable || !this.mAdapter.areAllItemsEnabled()) {
            z = false;
        }
        return z;
    }

    public boolean isEnabled(int position) {
        int numHeaders = getHeadersCount();
        if (position < numHeaders) {
            return this.mHeaderViewInfos.get(position).isSelectable;
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
            return this.mFooterViewInfos.get(adjPosition - adapterCount).isSelectable;
        }
        Log.e("HeaderViewListAdapter", "throwIndexOutOfBoundsException");
        return false;
    }

    public Object getItem(int position) {
        int numHeaders = getHeadersCount();
        if (position < numHeaders) {
            return this.mHeaderViewInfos.get(position).data;
        }
        int adjPosition = position - numHeaders;
        int adapterCount = 0;
        if (this.mAdapter != null) {
            adapterCount = this.mAdapter.getCount();
            if (adjPosition < adapterCount) {
                return this.mAdapter.getItem(adjPosition);
            }
        }
        return this.mFooterViewInfos.get(adjPosition - adapterCount).data;
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
            return this.mHeaderViewInfos.get(position).view;
        }
        int adjPosition = position - numHeaders;
        int adapterCount = 0;
        if (this.mAdapter != null) {
            adapterCount = this.mAdapter.getCount();
            if (adjPosition < adapterCount) {
                return this.mAdapter.getView(adjPosition, convertView, parent);
            }
        }
        return this.mFooterViewInfos.get(adjPosition - adapterCount).view;
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
