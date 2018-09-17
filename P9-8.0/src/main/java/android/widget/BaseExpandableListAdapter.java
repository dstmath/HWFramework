package android.widget;

import android.database.DataSetObservable;
import android.database.DataSetObserver;

public abstract class BaseExpandableListAdapter implements ExpandableListAdapter, HeterogeneousExpandableList {
    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    public void registerDataSetObserver(DataSetObserver observer) {
        this.mDataSetObservable.registerObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        this.mDataSetObservable.unregisterObserver(observer);
    }

    public void notifyDataSetInvalidated() {
        this.mDataSetObservable.notifyInvalidated();
    }

    public void notifyDataSetChanged() {
        this.mDataSetObservable.notifyChanged();
    }

    public boolean areAllItemsEnabled() {
        return true;
    }

    public void onGroupCollapsed(int groupPosition) {
    }

    public void onGroupExpanded(int groupPosition) {
    }

    public long getCombinedChildId(long groupId, long childId) {
        return (((2147483647L & groupId) << 32) | Long.MIN_VALUE) | (-1 & childId);
    }

    public long getCombinedGroupId(long groupId) {
        return (2147483647L & groupId) << 32;
    }

    public boolean isEmpty() {
        return getGroupCount() == 0;
    }

    public int getChildType(int groupPosition, int childPosition) {
        return 0;
    }

    public int getChildTypeCount() {
        return 1;
    }

    public int getGroupType(int groupPosition) {
        return 0;
    }

    public int getGroupTypeCount() {
        return 1;
    }
}
