package android.widget;

import android.database.DataSetObserver;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Collections;

class ExpandableListConnector extends BaseAdapter implements Filterable {
    private final DataSetObserver mDataSetObserver = new MyDataSetObserver();
    private ArrayList<GroupMetadata> mExpGroupMetadataList = new ArrayList<>();
    private ExpandableListAdapter mExpandableListAdapter;
    private int mMaxExpGroupCount = Integer.MAX_VALUE;
    private int mTotalExpChildrenCount;

    static class GroupMetadata implements Parcelable, Comparable<GroupMetadata> {
        public static final Parcelable.Creator<GroupMetadata> CREATOR = new Parcelable.Creator<GroupMetadata>() {
            public GroupMetadata createFromParcel(Parcel in) {
                return GroupMetadata.obtain(in.readInt(), in.readInt(), in.readInt(), in.readLong());
            }

            public GroupMetadata[] newArray(int size) {
                return new GroupMetadata[size];
            }
        };
        static final int REFRESH = -1;
        int flPos;
        long gId;
        int gPos;
        int lastChildFlPos;

        private GroupMetadata() {
        }

        static GroupMetadata obtain(int flPos2, int lastChildFlPos2, int gPos2, long gId2) {
            GroupMetadata gm = new GroupMetadata();
            gm.flPos = flPos2;
            gm.lastChildFlPos = lastChildFlPos2;
            gm.gPos = gPos2;
            gm.gId = gId2;
            return gm;
        }

        public int compareTo(GroupMetadata another) {
            if (another != null) {
                return this.gPos - another.gPos;
            }
            throw new IllegalArgumentException();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.flPos);
            dest.writeInt(this.lastChildFlPos);
            dest.writeInt(this.gPos);
            dest.writeLong(this.gId);
        }
    }

    protected class MyDataSetObserver extends DataSetObserver {
        protected MyDataSetObserver() {
        }

        public void onChanged() {
            ExpandableListConnector.this.refreshExpGroupMetadataList(true, true);
            ExpandableListConnector.this.notifyDataSetChanged();
        }

        public void onInvalidated() {
            ExpandableListConnector.this.refreshExpGroupMetadataList(true, true);
            ExpandableListConnector.this.notifyDataSetInvalidated();
        }
    }

    public static class PositionMetadata {
        private static final int MAX_POOL_SIZE = 5;
        private static ArrayList<PositionMetadata> sPool = new ArrayList<>(5);
        public int groupInsertIndex;
        public GroupMetadata groupMetadata;
        public ExpandableListPosition position;

        private void resetState() {
            if (this.position != null) {
                this.position.recycle();
                this.position = null;
            }
            this.groupMetadata = null;
            this.groupInsertIndex = 0;
        }

        private PositionMetadata() {
        }

        static PositionMetadata obtain(int flatListPos, int type, int groupPos, int childPos, GroupMetadata groupMetadata2, int groupInsertIndex2) {
            PositionMetadata pm = getRecycledOrCreate();
            pm.position = ExpandableListPosition.obtain(type, groupPos, childPos, flatListPos);
            pm.groupMetadata = groupMetadata2;
            pm.groupInsertIndex = groupInsertIndex2;
            return pm;
        }

        private static PositionMetadata getRecycledOrCreate() {
            synchronized (sPool) {
                if (sPool.size() > 0) {
                    PositionMetadata pm = sPool.remove(0);
                    pm.resetState();
                    return pm;
                }
                PositionMetadata positionMetadata = new PositionMetadata();
                return positionMetadata;
            }
        }

        public void recycle() {
            resetState();
            synchronized (sPool) {
                if (sPool.size() < 5) {
                    sPool.add(this);
                }
            }
        }

        public boolean isExpanded() {
            return this.groupMetadata != null;
        }
    }

    public ExpandableListConnector(ExpandableListAdapter expandableListAdapter) {
        setExpandableListAdapter(expandableListAdapter);
    }

    public void setExpandableListAdapter(ExpandableListAdapter expandableListAdapter) {
        if (this.mExpandableListAdapter != null) {
            this.mExpandableListAdapter.unregisterDataSetObserver(this.mDataSetObserver);
        }
        this.mExpandableListAdapter = expandableListAdapter;
        expandableListAdapter.registerDataSetObserver(this.mDataSetObserver);
    }

    /* access modifiers changed from: package-private */
    public PositionMetadata getUnflattenedPos(int flPos) {
        int insertPosition;
        int groupPos;
        int i = flPos;
        ArrayList<GroupMetadata> egml = this.mExpGroupMetadataList;
        int numExpGroups = egml.size();
        int leftExpGroupIndex = 0;
        int rightExpGroupIndex = numExpGroups - 1;
        int midExpGroupIndex = 0;
        if (numExpGroups == 0) {
            return PositionMetadata.obtain(i, 2, i, -1, null, 0);
        }
        while (leftExpGroupIndex <= rightExpGroupIndex) {
            midExpGroupIndex = ((rightExpGroupIndex - leftExpGroupIndex) / 2) + leftExpGroupIndex;
            GroupMetadata midExpGm = egml.get(midExpGroupIndex);
            if (i > midExpGm.lastChildFlPos) {
                leftExpGroupIndex = midExpGroupIndex + 1;
            } else if (i < midExpGm.flPos) {
                rightExpGroupIndex = midExpGroupIndex - 1;
            } else if (i == midExpGm.flPos) {
                return PositionMetadata.obtain(i, 2, midExpGm.gPos, -1, midExpGm, midExpGroupIndex);
            } else if (i <= midExpGm.lastChildFlPos) {
                return PositionMetadata.obtain(i, 1, midExpGm.gPos, i - (midExpGm.flPos + 1), midExpGm, midExpGroupIndex);
            }
        }
        if (leftExpGroupIndex > midExpGroupIndex) {
            GroupMetadata leftExpGm = egml.get(leftExpGroupIndex - 1);
            insertPosition = leftExpGroupIndex;
            groupPos = (i - leftExpGm.lastChildFlPos) + leftExpGm.gPos;
        } else if (rightExpGroupIndex < midExpGroupIndex) {
            rightExpGroupIndex++;
            GroupMetadata rightExpGm = egml.get(rightExpGroupIndex);
            insertPosition = rightExpGroupIndex;
            groupPos = rightExpGm.gPos - (rightExpGm.flPos - i);
        } else {
            throw new RuntimeException("Unknown state");
        }
        return PositionMetadata.obtain(i, 2, groupPos, -1, null, insertPosition);
    }

    /* access modifiers changed from: package-private */
    public PositionMetadata getFlattenedPos(ExpandableListPosition pos) {
        ExpandableListPosition expandableListPosition = pos;
        ArrayList<GroupMetadata> egml = this.mExpGroupMetadataList;
        int numExpGroups = egml.size();
        int leftExpGroupIndex = 0;
        int rightExpGroupIndex = numExpGroups - 1;
        int midExpGroupIndex = 0;
        if (numExpGroups == 0) {
            return PositionMetadata.obtain(expandableListPosition.groupPos, expandableListPosition.type, expandableListPosition.groupPos, expandableListPosition.childPos, null, 0);
        }
        while (leftExpGroupIndex <= rightExpGroupIndex) {
            midExpGroupIndex = ((rightExpGroupIndex - leftExpGroupIndex) / 2) + leftExpGroupIndex;
            GroupMetadata midExpGm = egml.get(midExpGroupIndex);
            if (expandableListPosition.groupPos > midExpGm.gPos) {
                leftExpGroupIndex = midExpGroupIndex + 1;
            } else if (expandableListPosition.groupPos < midExpGm.gPos) {
                rightExpGroupIndex = midExpGroupIndex - 1;
            } else if (expandableListPosition.groupPos == midExpGm.gPos) {
                if (expandableListPosition.type == 2) {
                    return PositionMetadata.obtain(midExpGm.flPos, expandableListPosition.type, expandableListPosition.groupPos, expandableListPosition.childPos, midExpGm, midExpGroupIndex);
                }
                if (expandableListPosition.type == 1) {
                    return PositionMetadata.obtain(midExpGm.flPos + expandableListPosition.childPos + 1, expandableListPosition.type, expandableListPosition.groupPos, expandableListPosition.childPos, midExpGm, midExpGroupIndex);
                }
                return null;
            }
        }
        if (expandableListPosition.type != 2) {
            return null;
        }
        if (leftExpGroupIndex > midExpGroupIndex) {
            GroupMetadata leftExpGm = egml.get(leftExpGroupIndex - 1);
            return PositionMetadata.obtain(leftExpGm.lastChildFlPos + (expandableListPosition.groupPos - leftExpGm.gPos), expandableListPosition.type, expandableListPosition.groupPos, expandableListPosition.childPos, null, leftExpGroupIndex);
        } else if (rightExpGroupIndex >= midExpGroupIndex) {
            return null;
        } else {
            int rightExpGroupIndex2 = rightExpGroupIndex + 1;
            GroupMetadata rightExpGm = egml.get(rightExpGroupIndex2);
            return PositionMetadata.obtain(rightExpGm.flPos - (rightExpGm.gPos - expandableListPosition.groupPos), expandableListPosition.type, expandableListPosition.groupPos, expandableListPosition.childPos, null, rightExpGroupIndex2);
        }
    }

    public boolean areAllItemsEnabled() {
        return this.mExpandableListAdapter.areAllItemsEnabled();
    }

    public boolean isEnabled(int flatListPos) {
        PositionMetadata metadata = getUnflattenedPos(flatListPos);
        ExpandableListPosition pos = metadata.position;
        boolean retValue = true;
        if (pos.type == 1) {
            retValue = this.mExpandableListAdapter.isChildSelectable(pos.groupPos, pos.childPos);
        }
        boolean retValue2 = retValue;
        metadata.recycle();
        return retValue2;
    }

    public int getCount() {
        return this.mExpandableListAdapter.getGroupCount() + this.mTotalExpChildrenCount;
    }

    public Object getItem(int flatListPos) {
        Object retValue;
        PositionMetadata posMetadata = getUnflattenedPos(flatListPos);
        if (posMetadata.position.type == 2) {
            retValue = this.mExpandableListAdapter.getGroup(posMetadata.position.groupPos);
        } else if (posMetadata.position.type == 1) {
            retValue = this.mExpandableListAdapter.getChild(posMetadata.position.groupPos, posMetadata.position.childPos);
        } else {
            throw new RuntimeException("Flat list position is of unknown type");
        }
        posMetadata.recycle();
        return retValue;
    }

    public long getItemId(int flatListPos) {
        long retValue;
        PositionMetadata posMetadata = getUnflattenedPos(flatListPos);
        long groupId = this.mExpandableListAdapter.getGroupId(posMetadata.position.groupPos);
        if (posMetadata.position.type == 2) {
            retValue = this.mExpandableListAdapter.getCombinedGroupId(groupId);
        } else if (posMetadata.position.type == 1) {
            retValue = this.mExpandableListAdapter.getCombinedChildId(groupId, this.mExpandableListAdapter.getChildId(posMetadata.position.groupPos, posMetadata.position.childPos));
        } else {
            throw new RuntimeException("Flat list position is of unknown type");
        }
        posMetadata.recycle();
        return retValue;
    }

    public View getView(int flatListPos, View convertView, ViewGroup parent) {
        View retValue;
        PositionMetadata posMetadata = getUnflattenedPos(flatListPos);
        if (posMetadata.position.type == 2) {
            retValue = this.mExpandableListAdapter.getGroupView(posMetadata.position.groupPos, posMetadata.isExpanded(), convertView, parent);
        } else {
            boolean isLastChild = true;
            if (posMetadata.position.type == 1) {
                if (posMetadata.groupMetadata.lastChildFlPos != flatListPos) {
                    isLastChild = false;
                }
                retValue = this.mExpandableListAdapter.getChildView(posMetadata.position.groupPos, posMetadata.position.childPos, isLastChild, convertView, parent);
            } else {
                throw new RuntimeException("Flat list position is of unknown type");
            }
        }
        posMetadata.recycle();
        return retValue;
    }

    public int getItemViewType(int flatListPos) {
        int retValue;
        int childType;
        PositionMetadata metadata = getUnflattenedPos(flatListPos);
        ExpandableListPosition pos = metadata.position;
        if (this.mExpandableListAdapter instanceof HeterogeneousExpandableList) {
            HeterogeneousExpandableList adapter = (HeterogeneousExpandableList) this.mExpandableListAdapter;
            if (pos.type == 2) {
                childType = adapter.getGroupType(pos.groupPos);
            } else {
                childType = adapter.getChildType(pos.groupPos, pos.childPos) + adapter.getGroupTypeCount();
            }
            retValue = childType;
        } else if (pos.type == 2) {
            retValue = 0;
        } else {
            retValue = 1;
        }
        metadata.recycle();
        return retValue;
    }

    public int getViewTypeCount() {
        if (!(this.mExpandableListAdapter instanceof HeterogeneousExpandableList)) {
            return 2;
        }
        HeterogeneousExpandableList adapter = (HeterogeneousExpandableList) this.mExpandableListAdapter;
        return adapter.getGroupTypeCount() + adapter.getChildTypeCount();
    }

    public boolean hasStableIds() {
        return this.mExpandableListAdapter.hasStableIds();
    }

    /* access modifiers changed from: private */
    public void refreshExpGroupMetadataList(boolean forceChildrenCountRefresh, boolean syncGroupPositions) {
        ArrayList<GroupMetadata> egml = this.mExpGroupMetadataList;
        int egmlSize = egml.size();
        int curFlPos = 0;
        this.mTotalExpChildrenCount = 0;
        if (syncGroupPositions) {
            boolean positionsChanged = false;
            for (int i = egmlSize - 1; i >= 0; i--) {
                GroupMetadata curGm = egml.get(i);
                int newGPos = findGroupPosition(curGm.gId, curGm.gPos);
                if (newGPos != curGm.gPos) {
                    if (newGPos == -1) {
                        egml.remove(i);
                        egmlSize--;
                    }
                    curGm.gPos = newGPos;
                    if (!positionsChanged) {
                        positionsChanged = true;
                    }
                }
            }
            if (positionsChanged) {
                Collections.sort(egml);
            }
        }
        int lastGPos = 0;
        for (int i2 = 0; i2 < egmlSize; i2++) {
            GroupMetadata curGm2 = egml.get(i2);
            int gChildrenCount = (curGm2.lastChildFlPos == -1 || forceChildrenCountRefresh) ? this.mExpandableListAdapter.getChildrenCount(curGm2.gPos) : curGm2.lastChildFlPos - curGm2.flPos;
            this.mTotalExpChildrenCount += gChildrenCount;
            int curFlPos2 = curFlPos + (curGm2.gPos - lastGPos);
            lastGPos = curGm2.gPos;
            curGm2.flPos = curFlPos2;
            curFlPos = curFlPos2 + gChildrenCount;
            curGm2.lastChildFlPos = curFlPos;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean collapseGroup(int groupPos) {
        ExpandableListPosition elGroupPos = ExpandableListPosition.obtain(2, groupPos, -1, -1);
        PositionMetadata pm = getFlattenedPos(elGroupPos);
        elGroupPos.recycle();
        if (pm == null) {
            return false;
        }
        boolean retValue = collapseGroup(pm);
        pm.recycle();
        return retValue;
    }

    /* access modifiers changed from: package-private */
    public boolean collapseGroup(PositionMetadata posMetadata) {
        if (posMetadata.groupMetadata == null) {
            return false;
        }
        this.mExpGroupMetadataList.remove(posMetadata.groupMetadata);
        refreshExpGroupMetadataList(false, false);
        notifyDataSetChanged();
        this.mExpandableListAdapter.onGroupCollapsed(posMetadata.groupMetadata.gPos);
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean expandGroup(int groupPos) {
        ExpandableListPosition elGroupPos = ExpandableListPosition.obtain(2, groupPos, -1, -1);
        PositionMetadata pm = getFlattenedPos(elGroupPos);
        elGroupPos.recycle();
        boolean retValue = expandGroup(pm);
        pm.recycle();
        return retValue;
    }

    /* access modifiers changed from: package-private */
    public boolean expandGroup(PositionMetadata posMetadata) {
        if (posMetadata.position.groupPos < 0) {
            throw new RuntimeException("Need group");
        } else if (this.mMaxExpGroupCount == 0 || posMetadata.groupMetadata != null) {
            return false;
        } else {
            if (this.mExpGroupMetadataList.size() >= this.mMaxExpGroupCount) {
                GroupMetadata collapsedGm = this.mExpGroupMetadataList.get(0);
                int collapsedIndex = this.mExpGroupMetadataList.indexOf(collapsedGm);
                collapseGroup(collapsedGm.gPos);
                if (posMetadata.groupInsertIndex > collapsedIndex) {
                    posMetadata.groupInsertIndex--;
                }
            }
            GroupMetadata expandedGm = GroupMetadata.obtain(-1, -1, posMetadata.position.groupPos, this.mExpandableListAdapter.getGroupId(posMetadata.position.groupPos));
            this.mExpGroupMetadataList.add(posMetadata.groupInsertIndex, expandedGm);
            refreshExpGroupMetadataList(false, false);
            notifyDataSetChanged();
            this.mExpandableListAdapter.onGroupExpanded(expandedGm.gPos);
            return true;
        }
    }

    public boolean isGroupExpanded(int groupPosition) {
        for (int i = this.mExpGroupMetadataList.size() - 1; i >= 0; i--) {
            if (this.mExpGroupMetadataList.get(i).gPos == groupPosition) {
                return true;
            }
        }
        return false;
    }

    public void setMaxExpGroupCount(int maxExpGroupCount) {
        this.mMaxExpGroupCount = maxExpGroupCount;
    }

    /* access modifiers changed from: package-private */
    public ExpandableListAdapter getAdapter() {
        return this.mExpandableListAdapter;
    }

    public Filter getFilter() {
        ExpandableListAdapter adapter = getAdapter();
        if (adapter instanceof Filterable) {
            return ((Filterable) adapter).getFilter();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public ArrayList<GroupMetadata> getExpandedGroupMetadataList() {
        return this.mExpGroupMetadataList;
    }

    /* access modifiers changed from: package-private */
    public void setExpandedGroupMetadataList(ArrayList<GroupMetadata> expandedGroupMetadataList) {
        if (expandedGroupMetadataList != null && this.mExpandableListAdapter != null) {
            int numGroups = this.mExpandableListAdapter.getGroupCount();
            int i = expandedGroupMetadataList.size() - 1;
            while (i >= 0) {
                if (expandedGroupMetadataList.get(i).gPos < numGroups) {
                    i--;
                } else {
                    return;
                }
            }
            this.mExpGroupMetadataList = expandedGroupMetadataList;
            refreshExpGroupMetadataList(true, false);
        }
    }

    public boolean isEmpty() {
        ExpandableListAdapter adapter = getAdapter();
        if (adapter != null) {
            return adapter.isEmpty();
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public int findGroupPosition(long groupIdToMatch, int seedGroupPosition) {
        int count = this.mExpandableListAdapter.getGroupCount();
        if (count == 0 || groupIdToMatch == Long.MIN_VALUE) {
            return -1;
        }
        int seedGroupPosition2 = Math.min(count - 1, Math.max(0, seedGroupPosition));
        long endTime = SystemClock.uptimeMillis() + 100;
        int first = seedGroupPosition2;
        int last = seedGroupPosition2;
        boolean next = false;
        ExpandableListAdapter adapter = getAdapter();
        if (adapter == null) {
            return -1;
        }
        while (SystemClock.uptimeMillis() <= endTime) {
            if (adapter.getGroupId(seedGroupPosition2) != groupIdToMatch) {
                boolean hitFirst = true;
                boolean hitLast = last == count + -1;
                if (first != 0) {
                    hitFirst = false;
                }
                if (hitLast && hitFirst) {
                    break;
                } else if (hitFirst || (next && !hitLast)) {
                    last++;
                    seedGroupPosition2 = last;
                    next = false;
                } else if (hitLast || (!next && !hitFirst)) {
                    first--;
                    seedGroupPosition2 = first;
                    next = true;
                }
            } else {
                return seedGroupPosition2;
            }
        }
        return -1;
    }
}
