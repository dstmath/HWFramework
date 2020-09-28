package android.widget;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListConnector;
import com.android.internal.R;
import java.util.ArrayList;

public class ExpandableListView extends ListView {
    public static final int CHILD_INDICATOR_INHERIT = -1;
    private static final int[] CHILD_LAST_STATE_SET = {16842918};
    private static final int[] EMPTY_STATE_SET = new int[0];
    private static final int[] GROUP_EMPTY_STATE_SET = {16842921};
    private static final int[] GROUP_EXPANDED_EMPTY_STATE_SET = {16842920, 16842921};
    private static final int[] GROUP_EXPANDED_STATE_SET = {16842920};
    @UnsupportedAppUsage
    private static final int[][] GROUP_STATE_SETS = {EMPTY_STATE_SET, GROUP_EXPANDED_STATE_SET, GROUP_EMPTY_STATE_SET, GROUP_EXPANDED_EMPTY_STATE_SET};
    private static final int INDICATOR_UNDEFINED = -2;
    private static final long PACKED_POSITION_INT_MASK_CHILD = -1;
    private static final long PACKED_POSITION_INT_MASK_GROUP = 2147483647L;
    private static final long PACKED_POSITION_MASK_CHILD = 4294967295L;
    private static final long PACKED_POSITION_MASK_GROUP = 9223372032559808512L;
    private static final long PACKED_POSITION_MASK_TYPE = Long.MIN_VALUE;
    private static final long PACKED_POSITION_SHIFT_GROUP = 32;
    private static final long PACKED_POSITION_SHIFT_TYPE = 63;
    public static final int PACKED_POSITION_TYPE_CHILD = 1;
    public static final int PACKED_POSITION_TYPE_GROUP = 0;
    public static final int PACKED_POSITION_TYPE_NULL = 2;
    public static final long PACKED_POSITION_VALUE_NULL = 4294967295L;
    private ExpandableListAdapter mAdapter;
    @UnsupportedAppUsage
    private Drawable mChildDivider;
    private Drawable mChildIndicator;
    private int mChildIndicatorEnd;
    private int mChildIndicatorLeft;
    private int mChildIndicatorRight;
    private int mChildIndicatorStart;
    @UnsupportedAppUsage
    private ExpandableListConnector mConnector;
    @UnsupportedAppUsage
    private Drawable mGroupIndicator;
    private int mIndicatorEnd;
    @UnsupportedAppUsage
    private int mIndicatorLeft;
    private final Rect mIndicatorRect;
    @UnsupportedAppUsage
    private int mIndicatorRight;
    private int mIndicatorStart;
    @UnsupportedAppUsage
    private OnChildClickListener mOnChildClickListener;
    @UnsupportedAppUsage
    private OnGroupClickListener mOnGroupClickListener;
    @UnsupportedAppUsage
    private OnGroupCollapseListener mOnGroupCollapseListener;
    @UnsupportedAppUsage
    private OnGroupExpandListener mOnGroupExpandListener;

    public interface OnChildClickListener {
        boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i2, long j);
    }

    public interface OnGroupClickListener {
        boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long j);
    }

    public interface OnGroupCollapseListener {
        void onGroupCollapse(int i);
    }

    public interface OnGroupExpandListener {
        void onGroupExpand(int i);
    }

    public ExpandableListView(Context context) {
        this(context, null);
    }

    public ExpandableListView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842863);
    }

    public ExpandableListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ExpandableListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Drawable drawable;
        this.mIndicatorRect = new Rect();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExpandableListView, defStyleAttr, defStyleRes);
        saveAttributeDataForStyleable(context, R.styleable.ExpandableListView, attrs, a, defStyleAttr, defStyleRes);
        this.mGroupIndicator = a.getDrawable(0);
        this.mChildIndicator = a.getDrawable(1);
        this.mIndicatorLeft = a.getDimensionPixelSize(2, 0);
        this.mIndicatorRight = a.getDimensionPixelSize(3, 0);
        if (this.mIndicatorRight == 0 && (drawable = this.mGroupIndicator) != null) {
            this.mIndicatorRight = this.mIndicatorLeft + drawable.getIntrinsicWidth();
        }
        this.mChildIndicatorLeft = a.getDimensionPixelSize(4, -1);
        this.mChildIndicatorRight = a.getDimensionPixelSize(5, -1);
        this.mChildDivider = a.getDrawable(6);
        if (!isRtlCompatibilityMode()) {
            this.mIndicatorStart = a.getDimensionPixelSize(7, -2);
            this.mIndicatorEnd = a.getDimensionPixelSize(8, -2);
            this.mChildIndicatorStart = a.getDimensionPixelSize(9, -1);
            this.mChildIndicatorEnd = a.getDimensionPixelSize(10, -1);
        }
        a.recycle();
    }

    private boolean isRtlCompatibilityMode() {
        return this.mContext.getApplicationInfo().targetSdkVersion < 17 || !hasRtlSupport();
    }

    private boolean hasRtlSupport() {
        return this.mContext.getApplicationInfo().hasRtlSupport();
    }

    @Override // android.widget.AbsListView, android.view.View
    public void onRtlPropertiesChanged(int layoutDirection) {
        resolveIndicator();
        resolveChildIndicator();
    }

    private void resolveIndicator() {
        Drawable drawable;
        if (isLayoutRtl()) {
            int i = this.mIndicatorStart;
            if (i >= 0) {
                this.mIndicatorRight = i;
            }
            int i2 = this.mIndicatorEnd;
            if (i2 >= 0) {
                this.mIndicatorLeft = i2;
            }
        } else {
            int i3 = this.mIndicatorStart;
            if (i3 >= 0) {
                this.mIndicatorLeft = i3;
            }
            int i4 = this.mIndicatorEnd;
            if (i4 >= 0) {
                this.mIndicatorRight = i4;
            }
        }
        if (this.mIndicatorRight == 0 && (drawable = this.mGroupIndicator) != null) {
            this.mIndicatorRight = this.mIndicatorLeft + drawable.getIntrinsicWidth();
        }
    }

    private void resolveChildIndicator() {
        if (isLayoutRtl()) {
            int i = this.mChildIndicatorStart;
            if (i >= -1) {
                this.mChildIndicatorRight = i;
            }
            int i2 = this.mChildIndicatorEnd;
            if (i2 >= -1) {
                this.mChildIndicatorLeft = i2;
                return;
            }
            return;
        }
        int i3 = this.mChildIndicatorStart;
        if (i3 >= -1) {
            this.mChildIndicatorLeft = i3;
        }
        int i4 = this.mChildIndicatorEnd;
        if (i4 >= -1) {
            this.mChildIndicatorRight = i4;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView, android.view.ViewGroup, android.view.View, android.widget.ListView
    public void dispatchDraw(Canvas canvas) {
        int myB;
        int lastChildFlPos;
        int headerViewsCount;
        super.dispatchDraw(canvas);
        if (this.mChildIndicator != null || this.mGroupIndicator != null) {
            int saveCount = 0;
            boolean clipToPadding = (this.mGroupFlags & 34) == 34;
            if (clipToPadding) {
                saveCount = canvas.save();
                int scrollX = this.mScrollX;
                int scrollY = this.mScrollY;
                canvas.clipRect(this.mPaddingLeft + scrollX, this.mPaddingTop + scrollY, ((this.mRight + scrollX) - this.mLeft) - this.mPaddingRight, ((this.mBottom + scrollY) - this.mTop) - this.mPaddingBottom);
            }
            int headerViewsCount2 = getHeaderViewsCount();
            int lastChildFlPos2 = ((this.mItemCount - getFooterViewsCount()) - headerViewsCount2) - 1;
            int myB2 = this.mBottom;
            int lastItemType = -4;
            Rect indicatorRect = this.mIndicatorRect;
            int childCount = getChildCount();
            int i = 0;
            int childFlPos = this.mFirstPosition - headerViewsCount2;
            while (true) {
                if (i >= childCount) {
                    break;
                }
                if (childFlPos < 0) {
                    headerViewsCount = headerViewsCount2;
                    lastChildFlPos = lastChildFlPos2;
                    myB = myB2;
                } else if (childFlPos > lastChildFlPos2) {
                    break;
                } else {
                    View item = getChildAt(i);
                    int t = item.getTop();
                    int b = item.getBottom();
                    if (b < 0) {
                        headerViewsCount = headerViewsCount2;
                        lastChildFlPos = lastChildFlPos2;
                        myB = myB2;
                    } else if (t > myB2) {
                        headerViewsCount = headerViewsCount2;
                        lastChildFlPos = lastChildFlPos2;
                        myB = myB2;
                    } else {
                        ExpandableListConnector.PositionMetadata pos = this.mConnector.getUnflattenedPos(childFlPos);
                        headerViewsCount = headerViewsCount2;
                        boolean isLayoutRtl = isLayoutRtl();
                        int width = getWidth();
                        lastChildFlPos = lastChildFlPos2;
                        if (pos.position.type != lastItemType) {
                            myB = myB2;
                            if (pos.position.type == 1) {
                                int i2 = this.mChildIndicatorLeft;
                                if (i2 == -1) {
                                    i2 = this.mIndicatorLeft;
                                }
                                indicatorRect.left = i2;
                                int i3 = this.mChildIndicatorRight;
                                if (i3 == -1) {
                                    i3 = this.mIndicatorRight;
                                }
                                indicatorRect.right = i3;
                            } else {
                                indicatorRect.left = this.mIndicatorLeft;
                                indicatorRect.right = this.mIndicatorRight;
                            }
                            adjustIndicatorLocation(indicatorRect, isLayoutRtl, this.mIndicatorStart, this.mGroupIndicator);
                            if (HwWidgetFactory.isHwTheme(this.mContext) ^ isLayoutRtl) {
                                int temp = indicatorRect.left;
                                indicatorRect.left = width - indicatorRect.right;
                                indicatorRect.right = width - temp;
                                indicatorRect.left -= this.mPaddingRight;
                                indicatorRect.right -= this.mPaddingRight;
                            } else {
                                indicatorRect.left += this.mPaddingLeft;
                                indicatorRect.right += this.mPaddingLeft;
                            }
                            lastItemType = pos.position.type;
                        } else {
                            myB = myB2;
                        }
                        if (indicatorRect.left != indicatorRect.right) {
                            if (this.mStackFromBottom) {
                                indicatorRect.top = t;
                                indicatorRect.bottom = b;
                            } else {
                                indicatorRect.top = t;
                                indicatorRect.bottom = b;
                            }
                            Drawable indicator = getIndicator(pos);
                            if (indicator != null) {
                                indicator.setBounds(indicatorRect);
                                indicator.draw(canvas);
                            }
                        }
                        pos.recycle();
                    }
                }
                i++;
                childFlPos++;
                headerViewsCount2 = headerViewsCount;
                lastChildFlPos2 = lastChildFlPos;
                myB2 = myB;
            }
            if (clipToPadding) {
                canvas.restoreToCount(saveCount);
            }
        }
    }

    private Drawable getIndicator(ExpandableListConnector.PositionMetadata pos) {
        Drawable indicator;
        int[] stateSet;
        char c = 2;
        if (pos.position.type == 2) {
            indicator = this.mGroupIndicator;
            if (indicator != null && indicator.isStateful()) {
                boolean isEmpty = pos.groupMetadata == null || pos.groupMetadata.lastChildFlPos == pos.groupMetadata.flPos;
                boolean isExpanded = pos.isExpanded();
                if (!isEmpty) {
                    c = 0;
                }
                indicator.setState(GROUP_STATE_SETS[c | isExpanded]);
            }
        } else {
            indicator = this.mChildIndicator;
            if (indicator != null && indicator.isStateful()) {
                if (pos.position.flatListPos == pos.groupMetadata.lastChildFlPos) {
                    stateSet = CHILD_LAST_STATE_SET;
                } else {
                    stateSet = EMPTY_STATE_SET;
                }
                indicator.setState(stateSet);
            }
        }
        return indicator;
    }

    public void setChildDivider(Drawable childDivider) {
        this.mChildDivider = childDivider;
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.ListView
    public void drawDivider(Canvas canvas, Rect bounds, int childIndex) {
        int flatListPosition = this.mFirstPosition + childIndex;
        if (flatListPosition >= 0) {
            ExpandableListConnector.PositionMetadata pos = this.mConnector.getUnflattenedPos(getFlatPositionForConnector(flatListPosition));
            if (pos.position.type == 1 || (pos.isExpanded() && pos.groupMetadata.lastChildFlPos != pos.groupMetadata.flPos)) {
                Drawable divider = isGroupDivider(pos) ? this.mDivider : this.mChildDivider;
                divider.setBounds(bounds);
                divider.draw(canvas);
                pos.recycle();
                return;
            }
            pos.recycle();
        }
        super.drawDivider(canvas, bounds, flatListPosition);
    }

    @Override // android.widget.AbsListView, android.widget.ListView
    public void setAdapter(ListAdapter adapter) {
        throw new RuntimeException("For ExpandableListView, use setAdapter(ExpandableListAdapter) instead of setAdapter(ListAdapter)");
    }

    @Override // android.widget.ListView, android.widget.ListView, android.widget.AdapterView
    public ListAdapter getAdapter() {
        return super.getAdapter();
    }

    @Override // android.widget.AdapterView
    public void setOnItemClickListener(AdapterView.OnItemClickListener l) {
        super.setOnItemClickListener(l);
    }

    public void setAdapter(ExpandableListAdapter adapter) {
        this.mAdapter = adapter;
        if (adapter != null) {
            this.mConnector = new ExpandableListConnector(adapter);
        } else {
            this.mConnector = null;
        }
        super.setAdapter((ListAdapter) this.mConnector);
    }

    public ExpandableListAdapter getExpandableListAdapter() {
        return this.mAdapter;
    }

    private boolean isHeaderOrFooterPosition(int position) {
        return position < getHeaderViewsCount() || position >= this.mItemCount - getFooterViewsCount();
    }

    private int getFlatPositionForConnector(int flatListPosition) {
        return flatListPosition - getHeaderViewsCount();
    }

    private int getAbsoluteFlatPosition(int flatListPosition) {
        return getHeaderViewsCount() + flatListPosition;
    }

    @Override // android.widget.AbsListView, android.widget.AdapterView
    public boolean performItemClick(View v, int position, long id) {
        if (isHeaderOrFooterPosition(position)) {
            return super.performItemClick(v, position, id);
        }
        return handleItemClick(v, getFlatPositionForConnector(position), id);
    }

    /* access modifiers changed from: package-private */
    public boolean handleItemClick(View v, int position, long id) {
        boolean returnValue;
        ExpandableListConnector.PositionMetadata posMetadata = this.mConnector.getUnflattenedPos(position);
        long id2 = getChildOrGroupId(posMetadata.position);
        if (posMetadata.position.type == 2) {
            OnGroupClickListener onGroupClickListener = this.mOnGroupClickListener;
            if (onGroupClickListener == null || !onGroupClickListener.onGroupClick(this, v, posMetadata.position.groupPos, id2)) {
                if (posMetadata.isExpanded()) {
                    this.mConnector.collapseGroup(posMetadata);
                    playSoundEffect(0);
                    OnGroupCollapseListener onGroupCollapseListener = this.mOnGroupCollapseListener;
                    if (onGroupCollapseListener != null) {
                        onGroupCollapseListener.onGroupCollapse(posMetadata.position.groupPos);
                    }
                } else {
                    this.mConnector.expandGroup(posMetadata);
                    playSoundEffect(0);
                    OnGroupExpandListener onGroupExpandListener = this.mOnGroupExpandListener;
                    if (onGroupExpandListener != null) {
                        onGroupExpandListener.onGroupExpand(posMetadata.position.groupPos);
                    }
                    int groupPos = posMetadata.position.groupPos;
                    int shiftedGroupPosition = getHeaderViewsCount() + posMetadata.position.flatListPos;
                    smoothScrollToPosition(this.mAdapter.getChildrenCount(groupPos) + shiftedGroupPosition, shiftedGroupPosition);
                }
                returnValue = true;
            } else {
                posMetadata.recycle();
                return true;
            }
        } else if (this.mOnChildClickListener != null) {
            playSoundEffect(0);
            return this.mOnChildClickListener.onChildClick(this, v, posMetadata.position.groupPos, posMetadata.position.childPos, id2);
        } else {
            returnValue = false;
        }
        posMetadata.recycle();
        return returnValue;
    }

    public boolean expandGroup(int groupPos) {
        return expandGroup(groupPos, false);
    }

    public boolean expandGroup(int groupPos, boolean animate) {
        ExpandableListPosition elGroupPos = ExpandableListPosition.obtain(2, groupPos, -1, -1);
        ExpandableListConnector.PositionMetadata pm = this.mConnector.getFlattenedPos(elGroupPos);
        elGroupPos.recycle();
        boolean retValue = this.mConnector.expandGroup(pm);
        OnGroupExpandListener onGroupExpandListener = this.mOnGroupExpandListener;
        if (onGroupExpandListener != null) {
            onGroupExpandListener.onGroupExpand(groupPos);
        }
        if (animate) {
            int shiftedGroupPosition = getHeaderViewsCount() + pm.position.flatListPos;
            smoothScrollToPosition(this.mAdapter.getChildrenCount(groupPos) + shiftedGroupPosition, shiftedGroupPosition);
        }
        pm.recycle();
        return retValue;
    }

    public boolean collapseGroup(int groupPos) {
        boolean retValue = this.mConnector.collapseGroup(groupPos);
        OnGroupCollapseListener onGroupCollapseListener = this.mOnGroupCollapseListener;
        if (onGroupCollapseListener != null) {
            onGroupCollapseListener.onGroupCollapse(groupPos);
        }
        return retValue;
    }

    public void setOnGroupCollapseListener(OnGroupCollapseListener onGroupCollapseListener) {
        this.mOnGroupCollapseListener = onGroupCollapseListener;
    }

    public void setOnGroupExpandListener(OnGroupExpandListener onGroupExpandListener) {
        this.mOnGroupExpandListener = onGroupExpandListener;
    }

    public void setOnGroupClickListener(OnGroupClickListener onGroupClickListener) {
        this.mOnGroupClickListener = onGroupClickListener;
    }

    public void setOnChildClickListener(OnChildClickListener onChildClickListener) {
        this.mOnChildClickListener = onChildClickListener;
    }

    public long getExpandableListPosition(int flatListPosition) {
        if (isHeaderOrFooterPosition(flatListPosition)) {
            return 4294967295L;
        }
        ExpandableListConnector.PositionMetadata pm = this.mConnector.getUnflattenedPos(getFlatPositionForConnector(flatListPosition));
        long packedPos = pm.position.getPackedPosition();
        pm.recycle();
        return packedPos;
    }

    public int getFlatListPosition(long packedPosition) {
        ExpandableListPosition elPackedPos = ExpandableListPosition.obtainPosition(packedPosition);
        ExpandableListConnector.PositionMetadata pm = this.mConnector.getFlattenedPos(elPackedPos);
        elPackedPos.recycle();
        int flatListPosition = pm.position.flatListPos;
        pm.recycle();
        return getAbsoluteFlatPosition(flatListPosition);
    }

    public long getSelectedPosition() {
        return getExpandableListPosition(getSelectedItemPosition());
    }

    public long getSelectedId() {
        long packedPos = getSelectedPosition();
        if (packedPos == 4294967295L) {
            return -1;
        }
        int groupPos = getPackedPositionGroup(packedPos);
        if (getPackedPositionType(packedPos) == 0) {
            return this.mAdapter.getGroupId(groupPos);
        }
        return this.mAdapter.getChildId(groupPos, getPackedPositionChild(packedPos));
    }

    public void setSelectedGroup(int groupPosition) {
        ExpandableListPosition elGroupPos = ExpandableListPosition.obtainGroupPosition(groupPosition);
        ExpandableListConnector.PositionMetadata pm = this.mConnector.getFlattenedPos(elGroupPos);
        elGroupPos.recycle();
        super.setSelection(getAbsoluteFlatPosition(pm.position.flatListPos));
        pm.recycle();
    }

    public boolean setSelectedChild(int groupPosition, int childPosition, boolean shouldExpandGroup) {
        ExpandableListPosition elChildPos = ExpandableListPosition.obtainChildPosition(groupPosition, childPosition);
        ExpandableListConnector.PositionMetadata flatChildPos = this.mConnector.getFlattenedPos(elChildPos);
        if (flatChildPos == null) {
            if (!shouldExpandGroup) {
                return false;
            }
            expandGroup(groupPosition);
            flatChildPos = this.mConnector.getFlattenedPos(elChildPos);
            if (flatChildPos == null) {
                throw new IllegalStateException("Could not find child");
            }
        }
        super.setSelection(getAbsoluteFlatPosition(flatChildPos.position.flatListPos));
        elChildPos.recycle();
        flatChildPos.recycle();
        return true;
    }

    public boolean isGroupExpanded(int groupPosition) {
        return this.mConnector.isGroupExpanded(groupPosition);
    }

    public static int getPackedPositionType(long packedPosition) {
        if (packedPosition == 4294967295L) {
            return 2;
        }
        if ((packedPosition & Long.MIN_VALUE) == Long.MIN_VALUE) {
            return 1;
        }
        return 0;
    }

    public static int getPackedPositionGroup(long packedPosition) {
        if (packedPosition == 4294967295L) {
            return -1;
        }
        return (int) ((PACKED_POSITION_MASK_GROUP & packedPosition) >> 32);
    }

    public static int getPackedPositionChild(long packedPosition) {
        if (packedPosition != 4294967295L && (packedPosition & Long.MIN_VALUE) == Long.MIN_VALUE) {
            return (int) (4294967295L & packedPosition);
        }
        return -1;
    }

    public static long getPackedPositionForChild(int groupPosition, int childPosition) {
        return ((((long) groupPosition) & PACKED_POSITION_INT_MASK_GROUP) << 32) | Long.MIN_VALUE | (((long) childPosition) & -1);
    }

    public static long getPackedPositionForGroup(int groupPosition) {
        return (((long) groupPosition) & PACKED_POSITION_INT_MASK_GROUP) << 32;
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.AbsListView
    public ContextMenu.ContextMenuInfo createContextMenuInfo(View view, int flatListPosition, long id) {
        if (isHeaderOrFooterPosition(flatListPosition)) {
            return new AdapterView.AdapterContextMenuInfo(view, flatListPosition, id);
        }
        ExpandableListConnector.PositionMetadata pm = this.mConnector.getUnflattenedPos(getFlatPositionForConnector(flatListPosition));
        ExpandableListPosition pos = pm.position;
        long id2 = getChildOrGroupId(pos);
        long packedPosition = pos.getPackedPosition();
        pm.recycle();
        return new ExpandableListContextMenuInfo(view, packedPosition, id2);
    }

    private long getChildOrGroupId(ExpandableListPosition position) {
        if (position.type == 1) {
            return this.mAdapter.getChildId(position.groupPos, position.childPos);
        }
        return this.mAdapter.getGroupId(position.groupPos);
    }

    public void setChildIndicator(Drawable childIndicator) {
        this.mChildIndicator = childIndicator;
    }

    public void setChildIndicatorBounds(int left, int right) {
        this.mChildIndicatorLeft = left;
        this.mChildIndicatorRight = right;
        resolveChildIndicator();
    }

    public void setChildIndicatorBoundsRelative(int start, int end) {
        this.mChildIndicatorStart = start;
        this.mChildIndicatorEnd = end;
        resolveChildIndicator();
    }

    public void setGroupIndicator(Drawable groupIndicator) {
        Drawable drawable;
        this.mGroupIndicator = groupIndicator;
        if (this.mIndicatorRight == 0 && (drawable = this.mGroupIndicator) != null) {
            this.mIndicatorRight = this.mIndicatorLeft + drawable.getIntrinsicWidth();
        }
    }

    public void setIndicatorBounds(int left, int right) {
        this.mIndicatorLeft = left;
        this.mIndicatorRight = right;
        resolveIndicator();
    }

    public void setIndicatorBoundsRelative(int start, int end) {
        this.mIndicatorStart = start;
        this.mIndicatorEnd = end;
        resolveIndicator();
    }

    public static class ExpandableListContextMenuInfo implements ContextMenu.ContextMenuInfo {
        public long id;
        public long packedPosition;
        public View targetView;

        public ExpandableListContextMenuInfo(View targetView2, long packedPosition2, long id2) {
            this.targetView = targetView2;
            this.packedPosition = packedPosition2;
            this.id = id2;
        }
    }

    /* access modifiers changed from: package-private */
    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            /* class android.widget.ExpandableListView.SavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        ArrayList<ExpandableListConnector.GroupMetadata> expandedGroupMetadataList;

        SavedState(Parcelable superState, ArrayList<ExpandableListConnector.GroupMetadata> expandedGroupMetadataList2) {
            super(superState);
            this.expandedGroupMetadataList = expandedGroupMetadataList2;
        }

        private SavedState(Parcel in) {
            super(in);
            this.expandedGroupMetadataList = new ArrayList<>();
            in.readList(this.expandedGroupMetadataList, ExpandableListConnector.class.getClassLoader());
        }

        @Override // android.view.View.BaseSavedState, android.os.Parcelable, android.view.AbsSavedState
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeList(this.expandedGroupMetadataList);
        }
    }

    @Override // android.widget.AbsListView, android.view.View
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        ExpandableListConnector expandableListConnector = this.mConnector;
        return new SavedState(superState, expandableListConnector != null ? expandableListConnector.getExpandedGroupMetadataList() : null);
    }

    @Override // android.widget.AbsListView, android.view.View
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        if (this.mConnector != null && ss.expandedGroupMetadataList != null) {
            this.mConnector.setExpandedGroupMetadataList(ss.expandedGroupMetadataList);
        }
    }

    @Override // android.widget.AbsListView, android.view.ViewGroup, android.view.View, android.widget.ListView, android.widget.AdapterView
    public CharSequence getAccessibilityClassName() {
        return ExpandableListView.class.getName();
    }

    private boolean isGroupDivider(ExpandableListConnector.PositionMetadata pos) {
        return pos.isExpanded() && pos.position.type == 2;
    }

    /* access modifiers changed from: protected */
    public void adjustIndicatorLocation(Rect indicatorRect, boolean isLayoutRtl, int indicatorStart, Drawable groupIndicator) {
    }
}
