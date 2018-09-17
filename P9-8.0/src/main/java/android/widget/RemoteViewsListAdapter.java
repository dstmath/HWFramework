package android.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;

public class RemoteViewsListAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<RemoteViews> mRemoteViewsList;
    private int mViewTypeCount;
    private ArrayList<Integer> mViewTypes = new ArrayList();

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
            for (RemoteViews rv : this.mRemoteViewsList) {
                if (!this.mViewTypes.contains(Integer.valueOf(rv.getLayoutId()))) {
                    this.mViewTypes.add(Integer.valueOf(rv.getLayoutId()));
                }
            }
            if (this.mViewTypes.size() > this.mViewTypeCount || this.mViewTypeCount < 1) {
                throw new RuntimeException("Invalid view type count -- view type count must be >= 1and must be as large as the total number of distinct view types");
            }
        }
    }

    public int getCount() {
        if (this.mRemoteViewsList != null) {
            return this.mRemoteViewsList.size();
        }
        return 0;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (position >= getCount()) {
            return null;
        }
        View v;
        RemoteViews rv = (RemoteViews) this.mRemoteViewsList.get(position);
        rv.setIsWidgetCollectionChild(true);
        if (convertView == null || rv == null || convertView.getId() != rv.getLayoutId()) {
            v = rv.apply(this.mContext, parent);
        } else {
            v = convertView;
            rv.reapply(this.mContext, convertView);
        }
        return v;
    }

    public int getItemViewType(int position) {
        if (position >= getCount()) {
            return 0;
        }
        return this.mViewTypes.indexOf(Integer.valueOf(((RemoteViews) this.mRemoteViewsList.get(position)).getLayoutId()));
    }

    public int getViewTypeCount() {
        return this.mViewTypeCount;
    }

    public boolean hasStableIds() {
        return false;
    }
}
