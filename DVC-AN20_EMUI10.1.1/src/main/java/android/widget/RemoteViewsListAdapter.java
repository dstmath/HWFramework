package android.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Iterator;

public class RemoteViewsListAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<RemoteViews> mRemoteViewsList;
    private int mViewTypeCount;
    private ArrayList<Integer> mViewTypes = new ArrayList<>();

    public RemoteViewsListAdapter(Context context, ArrayList<RemoteViews> remoteViews, int viewTypeCount) {
        this.mContext = context;
        this.mRemoteViewsList = remoteViews;
        this.mViewTypeCount = viewTypeCount;
        init();
    }

    public void setViewsList(ArrayList<RemoteViews> remoteViews) {
        this.mRemoteViewsList = remoteViews;
        init();
        notifyDataSetChanged();
    }

    private void init() {
        if (this.mRemoteViewsList != null) {
            this.mViewTypes.clear();
            Iterator<RemoteViews> it = this.mRemoteViewsList.iterator();
            while (it.hasNext()) {
                RemoteViews rv = it.next();
                if (!this.mViewTypes.contains(Integer.valueOf(rv.getLayoutId()))) {
                    this.mViewTypes.add(Integer.valueOf(rv.getLayoutId()));
                }
            }
            int size = this.mViewTypes.size();
            int i = this.mViewTypeCount;
            if (size > i || i < 1) {
                throw new RuntimeException("Invalid view type count -- view type count must be >= 1and must be as large as the total number of distinct view types");
            }
        }
    }

    @Override // android.widget.Adapter
    public int getCount() {
        ArrayList<RemoteViews> arrayList = this.mRemoteViewsList;
        if (arrayList != null) {
            return arrayList.size();
        }
        return 0;
    }

    @Override // android.widget.Adapter
    public Object getItem(int position) {
        return null;
    }

    @Override // android.widget.Adapter
    public long getItemId(int position) {
        return (long) position;
    }

    @Override // android.widget.Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position >= getCount()) {
            return null;
        }
        RemoteViews rv = this.mRemoteViewsList.get(position);
        rv.addFlags(2);
        if (convertView == null || convertView.getId() != rv.getLayoutId()) {
            return rv.apply(this.mContext, parent);
        }
        rv.reapply(this.mContext, convertView);
        return convertView;
    }

    @Override // android.widget.Adapter, android.widget.BaseAdapter
    public int getItemViewType(int position) {
        if (position >= getCount()) {
            return 0;
        }
        return this.mViewTypes.indexOf(Integer.valueOf(this.mRemoteViewsList.get(position).getLayoutId()));
    }

    @Override // android.widget.Adapter, android.widget.BaseAdapter
    public int getViewTypeCount() {
        return this.mViewTypeCount;
    }

    @Override // android.widget.Adapter, android.widget.BaseAdapter
    public boolean hasStableIds() {
        return false;
    }
}
