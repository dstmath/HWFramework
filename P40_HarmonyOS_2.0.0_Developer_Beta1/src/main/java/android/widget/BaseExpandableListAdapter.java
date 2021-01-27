package android.widget;

import android.database.DataSetObservable;
import android.database.DataSetObserver;

public abstract class BaseExpandableListAdapter implements ExpandableListAdapter, HeterogeneousExpandableList {
    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    @Override // android.widget.ExpandableListAdapter
    public void registerDataSetObserver(DataSetObserver observer) {
        this.mDataSetObservable.registerObserver(observer);
    }

    @Override // android.widget.ExpandableListAdapter
    public void unregisterDataSetObserver(DataSetObserver observer) {
        this.mDataSetObservable.unregisterObserver(observer);
    }

    public void notifyDataSetInvalidated() {
        this.mDataSetObservable.notifyInvalidated();
    }

    public void notifyDataSetChanged() {
        this.mDataSetObservable.notifyChanged();
    }

    @Override // android.widget.ExpandableListAdapter
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override // android.widget.ExpandableListAdapter
    public void onGroupCollapsed(int groupPosition) {
    }

    @Override // android.widget.ExpandableListAdapter
    public void onGroupExpanded(int groupPosition) {
    }

    @Override // android.widget.ExpandableListAdapter
    public long getCombinedChildId(long groupId, long childId) {
        return ((2147483647L & groupId) << 32) | Long.MIN_VALUE | (-1 & childId);
    }

    @Override // android.widget.ExpandableListAdapter
    public long getCombinedGroupId(long groupId) {
        return (2147483647L & groupId) << 32;
    }

    @Override // android.widget.ExpandableListAdapter
    public boolean isEmpty() {
        return getGroupCount() == 0;
    }

    @Override // android.widget.HeterogeneousExpandableList
    public int getChildType(int groupPosition, int childPosition) {
        return 0;
    }

    @Override // android.widget.HeterogeneousExpandableList
    public int getChildTypeCount() {
        return 1;
    }

    @Override // android.widget.HeterogeneousExpandableList
    public int getGroupType(int groupPosition) {
        return 0;
    }

    @Override // android.widget.HeterogeneousExpandableList
    public int getGroupTypeCount() {
        return 1;
    }
}
