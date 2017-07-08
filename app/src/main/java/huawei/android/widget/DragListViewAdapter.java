package huawei.android.widget;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

public abstract class DragListViewAdapter extends BaseAdapter {
    private static final String TAG = "DragListViewAdapter";
    private int mId;
    private LayoutInflater mInflater;

    private static class MyViewHolder {
        private View customLayout;
        private LinearLayout customPanel;
        private ImageView dragBtn;

        private MyViewHolder() {
        }
    }

    public DragListViewAdapter() {
        this.mId = 0;
    }

    public void setContext(Activity context) {
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        int size = getCount();
        if (position >= size) {
            Log.d(TAG, "Invalid view position : " + position + ", Actual size is : " + size);
            return null;
        } else if (this.mInflater == null) {
            Log.d(TAG, "mInflater == null");
            return null;
        } else {
            MyViewHolder viewHolder;
            if (convertView == null) {
                convertView = this.mInflater.inflate(34013215, parent, false);
                viewHolder = new MyViewHolder();
                viewHolder.dragBtn = (ImageView) convertView.findViewById(34603105);
                viewHolder.customPanel = (LinearLayout) convertView.findViewById(34603106);
                if (this.mId == 0) {
                    Log.d(TAG, "mId == 0");
                    viewHolder.customLayout = null;
                } else {
                    viewHolder.customLayout = this.mInflater.inflate(this.mId, viewHolder.customPanel, false);
                    viewHolder.customPanel.addView(viewHolder.customLayout, new LayoutParams(-1, -1));
                }
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (MyViewHolder) convertView.getTag();
            }
            if (getFlagDragIconVisible()) {
                viewHolder.dragBtn.setVisibility(0);
            } else {
                viewHolder.dragBtn.setVisibility(8);
            }
            if (getFlagCanNotDrag(position)) {
                viewHolder.dragBtn.setAlpha(0.3f);
            } else {
                viewHolder.dragBtn.setAlpha(1.0f);
            }
            return convertView;
        }
    }

    public void addCustomId(int id) {
        if (id != 0) {
            this.mId = id;
        }
    }

    public View getCustomLayout(View convertView) {
        if (convertView != null && convertView.getTag() != null) {
            return ((MyViewHolder) convertView.getTag()).customLayout;
        }
        Log.d(TAG, "getCustomLayout failed, convertView incorrect");
        return null;
    }

    protected boolean getFlagCanNotDrag(int position) {
        return false;
    }

    protected boolean getFlagDragIconVisible() {
        return false;
    }
}
