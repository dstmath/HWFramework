package android.widget;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;

public interface ExpandableListAdapter {
    boolean areAllItemsEnabled();

    Object getChild(int i, int i2);

    long getChildId(int i, int i2);

    View getChildView(int i, int i2, boolean z, View view, ViewGroup viewGroup);

    int getChildrenCount(int i);

    long getCombinedChildId(long j, long j2);

    long getCombinedGroupId(long j);

    Object getGroup(int i);

    int getGroupCount();

    long getGroupId(int i);

    View getGroupView(int i, boolean z, View view, ViewGroup viewGroup);

    boolean hasStableIds();

    boolean isChildSelectable(int i, int i2);

    boolean isEmpty();

    void onGroupCollapsed(int i);

    void onGroupExpanded(int i);

    void registerDataSetObserver(DataSetObserver dataSetObserver);

    void unregisterDataSetObserver(DataSetObserver dataSetObserver);
}
