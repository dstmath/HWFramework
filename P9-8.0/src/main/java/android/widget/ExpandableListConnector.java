package android.widget;

import android.database.DataSetObserver;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Collections;

class ExpandableListConnector extends BaseAdapter implements Filterable {
    private final DataSetObserver mDataSetObserver = new MyDataSetObserver();
    private ArrayList<GroupMetadata> mExpGroupMetadataList = new ArrayList();
    private ExpandableListAdapter mExpandableListAdapter;
    private int mMaxExpGroupCount = Integer.MAX_VALUE;
    private int mTotalExpChildrenCount;

    static class GroupMetadata implements Parcelable, Comparable<GroupMetadata> {
        public static final Creator<GroupMetadata> CREATOR = new Creator<GroupMetadata>() {
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

        static GroupMetadata obtain(int flPos, int lastChildFlPos, int gPos, long gId) {
            GroupMetadata gm = new GroupMetadata();
            gm.flPos = flPos;
            gm.lastChildFlPos = lastChildFlPos;
            gm.gPos = gPos;
            gm.gId = gId;
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
        private static ArrayList<PositionMetadata> sPool = new ArrayList(5);
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

        static PositionMetadata obtain(int flatListPos, int type, int groupPos, int childPos, GroupMetadata groupMetadata, int groupInsertIndex) {
            PositionMetadata pm = getRecycledOrCreate();
            pm.position = ExpandableListPosition.obtain(type, groupPos, childPos, flatListPos);
            pm.groupMetadata = groupMetadata;
            pm.groupInsertIndex = groupInsertIndex;
            return pm;
        }

        private static PositionMetadata getRecycledOrCreate() {
            synchronized (sPool) {
                if (sPool.size() > 0) {
                    PositionMetadata pm = (PositionMetadata) sPool.remove(0);
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

    PositionMetadata getUnflattenedPos(int flPos) {
        ArrayList<GroupMetadata> egml = this.mExpGroupMetadataList;
        int numExpGroups = egml.size();
        int leftExpGroupIndex = 0;
        int rightExpGroupIndex = numExpGroups - 1;
        int midExpGroupIndex = 0;
        if (numExpGroups == 0) {
            return PositionMetadata.obtain(flPos, 2, flPos, -1, null, 0);
        }
        int insertPosition;
        int groupPos;
        while (leftExpGroupIndex <= rightExpGroupIndex) {
            midExpGroupIndex = ((rightExpGroupIndex - leftExpGroupIndex) / 2) + leftExpGroupIndex;
            GroupMetadata midExpGm = (GroupMetadata) egml.get(midExpGroupIndex);
            if (flPos > midExpGm.lastChildFlPos) {
                leftExpGroupIndex = midExpGroupIndex + 1;
            } else if (flPos < midExpGm.flPos) {
                rightExpGroupIndex = midExpGroupIndex - 1;
            } else if (flPos == midExpGm.flPos) {
                return PositionMetadata.obtain(flPos, 2, midExpGm.gPos, -1, midExpGm, midExpGroupIndex);
            } else if (flPos <= midExpGm.lastChildFlPos) {
                return PositionMetadata.obtain(flPos, 1, midExpGm.gPos, flPos - (midExpGm.flPos + 1), midExpGm, midExpGroupIndex);
            }
        }
        if (leftExpGroupIndex > midExpGroupIndex) {
            GroupMetadata leftExpGm = (GroupMetadata) egml.get(leftExpGroupIndex - 1);
            insertPosition = leftExpGroupIndex;
            groupPos = (flPos - leftExpGm.lastChildFlPos) + leftExpGm.gPos;
        } else if (rightExpGroupIndex < midExpGroupIndex) {
            rightExpGroupIndex++;
            GroupMetadata rightExpGm = (GroupMetadata) egml.get(rightExpGroupIndex);
            insertPosition = rightExpGroupIndex;
            groupPos = rightExpGm.gPos - (rightExpGm.flPos - flPos);
        } else {
            throw new RuntimeException("Unknown state");
        }
        return PositionMetadata.obtain(flPos, 2, groupPos, -1, null, insertPosition);
    }

    PositionMetadata getFlattenedPos(ExpandableListPosition pos) {
        ArrayList<GroupMetadata> egml = this.mExpGroupMetadataList;
        int numExpGroups = egml.size();
        int leftExpGroupIndex = 0;
        int rightExpGroupIndex = numExpGroups - 1;
        int midExpGroupIndex = 0;
        if (numExpGroups == 0) {
            return PositionMetadata.obtain(pos.groupPos, pos.type, pos.groupPos, pos.childPos, null, 0);
        }
        while (leftExpGroupIndex <= rightExpGroupIndex) {
            midExpGroupIndex = ((rightExpGroupIndex - leftExpGroupIndex) / 2) + leftExpGroupIndex;
            GroupMetadata midExpGm = (GroupMetadata) egml.get(midExpGroupIndex);
            if (pos.groupPos > midExpGm.gPos) {
                leftExpGroupIndex = midExpGroupIndex + 1;
            } else if (pos.groupPos < midExpGm.gPos) {
                rightExpGroupIndex = midExpGroupIndex - 1;
            } else if (pos.groupPos == midExpGm.gPos) {
                if (pos.type == 2) {
                    return PositionMetadata.obtain(midExpGm.flPos, pos.type, pos.groupPos, pos.childPos, midExpGm, midExpGroupIndex);
                }
                if (pos.type == 1) {
                    return PositionMetadata.obtain((midExpGm.flPos + pos.childPos) + 1, pos.type, pos.groupPos, pos.childPos, midExpGm, midExpGroupIndex);
                }
                return null;
            }
        }
        if (pos.type != 2) {
            return null;
        }
        if (leftExpGroupIndex > midExpGroupIndex) {
            GroupMetadata leftExpGm = (GroupMetadata) egml.get(leftExpGroupIndex - 1);
            return PositionMetadata.obtain(leftExpGm.lastChildFlPos + (pos.groupPos - leftExpGm.gPos), pos.type, pos.groupPos, pos.childPos, null, leftExpGroupIndex);
        } else if (rightExpGroupIndex >= midExpGroupIndex) {
            return null;
        } else {
            rightExpGroupIndex++;
            GroupMetadata rightExpGm = (GroupMetadata) egml.get(rightExpGroupIndex);
            return PositionMetadata.obtain(rightExpGm.flPos - (rightExpGm.gPos - pos.groupPos), pos.type, pos.groupPos, pos.childPos, null, rightExpGroupIndex);
        }
    }

    public boolean areAllItemsEnabled() {
        return this.mExpandableListAdapter.areAllItemsEnabled();
    }

    public boolean isEnabled(int flatListPos) {
        boolean retValue;
        PositionMetadata metadata = getUnflattenedPos(flatListPos);
        ExpandableListPosition pos = metadata.position;
        if (pos.type == 1) {
            retValue = this.mExpandableListAdapter.isChildSelectable(pos.groupPos, pos.childPos);
        } else {
            retValue = true;
        }
        metadata.recycle();
        return retValue;
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
        } else if (posMetadata.position.type == 1) {
            retValue = this.mExpandableListAdapter.getChildView(posMetadata.position.groupPos, posMetadata.position.childPos, posMetadata.groupMetadata.lastChildFlPos == flatListPos, convertView, parent);
        } else {
            throw new RuntimeException("Flat list position is of unknown type");
        }
        posMetadata.recycle();
        return retValue;
    }

    public int getItemViewType(int flatListPos) {
        int retValue;
        PositionMetadata metadata = getUnflattenedPos(flatListPos);
        ExpandableListPosition pos = metadata.position;
        if (this.mExpandableListAdapter instanceof HeterogeneousExpandableList) {
            HeterogeneousExpandableList adapter = this.mExpandableListAdapter;
            if (pos.type == 2) {
                retValue = adapter.getGroupType(pos.groupPos);
            } else {
                retValue = adapter.getGroupTypeCount() + adapter.getChildType(pos.groupPos, pos.childPos);
            }
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
        HeterogeneousExpandableList adapter = this.mExpandableListAdapter;
        return adapter.getGroupTypeCount() + adapter.getChildTypeCount();
    }

    public boolean hasStableIds() {
        return this.mExpandableListAdapter.hasStableIds();
    }

    private void refreshExpGroupMetadataList(boolean forceChildrenCountRefresh, boolean syncGroupPositions) {
        int i;
        GroupMetadata curGm;
        ArrayList<GroupMetadata> egml = this.mExpGroupMetadataList;
        int egmlSize = egml.size();
        int curFlPos = 0;
        this.mTotalExpChildrenCount = 0;
        if (syncGroupPositions) {
            boolean positionsChanged = false;
            for (i = egmlSize - 1; i >= 0; i--) {
                curGm = (GroupMetadata) egml.get(i);
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
        for (i = 0; i < egmlSize; i++) {
            int gChildrenCount;
            curGm = (GroupMetadata) egml.get(i);
            if (curGm.lastChildFlPos == -1 || forceChildrenCountRefresh) {
                gChildrenCount = this.mExpandableListAdapter.getChildrenCount(curGm.gPos);
            } else {
                gChildrenCount = curGm.lastChildFlPos - curGm.flPos;
            }
            this.mTotalExpChildrenCount += gChildrenCount;
            curFlPos += curGm.gPos - lastGPos;
            lastGPos = curGm.gPos;
            curGm.flPos = curFlPos;
            curFlPos += gChildrenCount;
            curGm.lastChildFlPos = curFlPos;
        }
    }

    boolean collapseGroup(int groupPos) {
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

    boolean collapseGroup(PositionMetadata posMetadata) {
        if (posMetadata.groupMetadata == null) {
            return false;
        }
        this.mExpGroupMetadataList.remove(posMetadata.groupMetadata);
        refreshExpGroupMetadataList(false, false);
        notifyDataSetChanged();
        this.mExpandableListAdapter.onGroupCollapsed(posMetadata.groupMetadata.gPos);
        return true;
    }

    boolean expandGroup(int groupPos) {
        ExpandableListPosition elGroupPos = ExpandableListPosition.obtain(2, groupPos, -1, -1);
        PositionMetadata pm = getFlattenedPos(elGroupPos);
        elGroupPos.recycle();
        boolean retValue = expandGroup(pm);
        pm.recycle();
        return retValue;
    }

    boolean expandGroup(PositionMetadata posMetadata) {
        if (posMetadata.position.groupPos < 0) {
            throw new RuntimeException("Need group");
        } else if (this.mMaxExpGroupCount == 0 || posMetadata.groupMetadata != null) {
            return false;
        } else {
            if (this.mExpGroupMetadataList.size() >= this.mMaxExpGroupCount) {
                GroupMetadata collapsedGm = (GroupMetadata) this.mExpGroupMetadataList.get(0);
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
            if (((GroupMetadata) this.mExpGroupMetadataList.get(i)).gPos == groupPosition) {
                return true;
            }
        }
        return false;
    }

    public void setMaxExpGroupCount(int maxExpGroupCount) {
        this.mMaxExpGroupCount = maxExpGroupCount;
    }

    ExpandableListAdapter getAdapter() {
        return this.mExpandableListAdapter;
    }

    public Filter getFilter() {
        ExpandableListAdapter adapter = getAdapter();
        if (adapter instanceof Filterable) {
            return ((Filterable) adapter).getFilter();
        }
        return null;
    }

    ArrayList<GroupMetadata> getExpandedGroupMetadataList() {
        return this.mExpGroupMetadataList;
    }

    void setExpandedGroupMetadataList(ArrayList<GroupMetadata> expandedGroupMetadataList) {
        if (expandedGroupMetadataList != null && this.mExpandableListAdapter != null) {
            int numGroups = this.mExpandableListAdapter.getGroupCount();
            int i = expandedGroupMetadataList.size() - 1;
            while (i >= 0) {
                if (((GroupMetadata) expandedGroupMetadataList.get(i)).gPos < numGroups) {
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
        return adapter != null ? adapter.isEmpty() : true;
    }

    int findGroupPosition(long groupIdToMatch, int seedGroupPosition) {
        int count = this.mExpandableListAdapter.getGroupCount();
        if (count == 0) {
            return -1;
        }
        if (groupIdToMatch == Long.MIN_VALUE) {
            return -1;
        }
        seedGroupPosition = Math.min(count - 1, Math.max(0, seedGroupPosition));
        long endTime = SystemClock.uptimeMillis() + 100;
        int first = seedGroupPosition;
        int last = seedGroupPosition;
        boolean next = false;
        ExpandableListAdapter adapter = getAdapter();
        if (adapter == null) {
            return -1;
        }
        while (SystemClock.uptimeMillis() <= endTime) {
            if (adapter.getGroupId(seedGroupPosition) != groupIdToMatch) {
                boolean hitLast = last == count + -1;
                boolean hitFirst = first == 0;
                if (hitLast && hitFirst) {
                    break;
                } else if (hitFirst || (next && (hitLast ^ 1) != 0)) {
                    last++;
                    seedGroupPosition = last;
                    next = false;
                } else if (hitLast || !(next || (hitFirst ^ 1) == 0)) {
                    first--;
                    seedGroupPosition = first;
                    next = true;
                }
            } else {
                return seedGroupPosition;
            }
        }
        return -1;
    }
}
