package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.View.BaseSavedState;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListConnector.PositionMetadata;
import com.android.internal.R;
import java.util.ArrayList;

public class ExpandableListView extends ListView {
    public static final int CHILD_INDICATOR_INHERIT = -1;
    private static final int[] CHILD_LAST_STATE_SET = null;
    private static final int[] EMPTY_STATE_SET = null;
    private static final int[] GROUP_EMPTY_STATE_SET = null;
    private static final int[] GROUP_EXPANDED_EMPTY_STATE_SET = null;
    private static final int[] GROUP_EXPANDED_STATE_SET = null;
    private static final int[][] GROUP_STATE_SETS = null;
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
    private Drawable mChildDivider;
    private Drawable mChildIndicator;
    private int mChildIndicatorEnd;
    private int mChildIndicatorLeft;
    private int mChildIndicatorRight;
    private int mChildIndicatorStart;
    private ExpandableListConnector mConnector;
    private Drawable mGroupIndicator;
    private int mIndicatorEnd;
    private int mIndicatorLeft;
    private final Rect mIndicatorRect;
    private int mIndicatorRight;
    private int mIndicatorStart;
    private OnChildClickListener mOnChildClickListener;
    private OnGroupClickListener mOnGroupClickListener;
    private OnGroupCollapseListener mOnGroupCollapseListener;
    private OnGroupExpandListener mOnGroupExpandListener;

    public static class ExpandableListContextMenuInfo implements ContextMenuInfo {
        public long id;
        public long packedPosition;
        public View targetView;

        public ExpandableListContextMenuInfo(View targetView, long packedPosition, long id) {
            this.targetView = targetView;
            this.packedPosition = packedPosition;
            this.id = id;
        }
    }

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

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = null;
        ArrayList<GroupMetadata> expandedGroupMetadataList;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.ExpandableListView.SavedState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.ExpandableListView.SavedState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListView.SavedState.<clinit>():void");
        }

        SavedState(Parcelable superState, ArrayList<GroupMetadata> expandedGroupMetadataList) {
            super(superState);
            this.expandedGroupMetadataList = expandedGroupMetadataList;
        }

        private SavedState(Parcel in) {
            super(in);
            this.expandedGroupMetadataList = new ArrayList();
            in.readList(this.expandedGroupMetadataList, ExpandableListConnector.class.getClassLoader());
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeList(this.expandedGroupMetadataList);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.ExpandableListView.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.ExpandableListView.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.widget.ExpandableListView.<clinit>():void");
    }

    public ExpandableListView(Context context) {
        this(context, null);
    }

    public ExpandableListView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.expandableListViewStyle);
    }

    public ExpandableListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, PACKED_POSITION_TYPE_GROUP);
    }

    public ExpandableListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mIndicatorRect = new Rect();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExpandableListView, defStyleAttr, defStyleRes);
        this.mGroupIndicator = a.getDrawable(PACKED_POSITION_TYPE_GROUP);
        this.mChildIndicator = a.getDrawable(PACKED_POSITION_TYPE_CHILD);
        this.mIndicatorLeft = a.getDimensionPixelSize(PACKED_POSITION_TYPE_NULL, PACKED_POSITION_TYPE_GROUP);
        this.mIndicatorRight = a.getDimensionPixelSize(3, PACKED_POSITION_TYPE_GROUP);
        if (this.mIndicatorRight == 0 && this.mGroupIndicator != null) {
            this.mIndicatorRight = this.mIndicatorLeft + this.mGroupIndicator.getIntrinsicWidth();
        }
        this.mChildIndicatorLeft = a.getDimensionPixelSize(4, CHILD_INDICATOR_INHERIT);
        this.mChildIndicatorRight = a.getDimensionPixelSize(5, CHILD_INDICATOR_INHERIT);
        this.mChildDivider = a.getDrawable(6);
        if (!isRtlCompatibilityMode()) {
            this.mIndicatorStart = a.getDimensionPixelSize(7, INDICATOR_UNDEFINED);
            this.mIndicatorEnd = a.getDimensionPixelSize(8, INDICATOR_UNDEFINED);
            this.mChildIndicatorStart = a.getDimensionPixelSize(9, CHILD_INDICATOR_INHERIT);
            this.mChildIndicatorEnd = a.getDimensionPixelSize(10, CHILD_INDICATOR_INHERIT);
        }
        a.recycle();
        setIgnoreScrollMultiSelectStub();
    }

    private boolean isRtlCompatibilityMode() {
        if (this.mContext.getApplicationInfo().targetSdkVersion < 17 || !hasRtlSupport()) {
            return true;
        }
        return false;
    }

    private boolean hasRtlSupport() {
        return this.mContext.getApplicationInfo().hasRtlSupport();
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        resolveIndicator();
        resolveChildIndicator();
    }

    private void resolveIndicator() {
        if (isRtlLocale()) {
            if (this.mIndicatorStart >= 0) {
                this.mIndicatorRight = this.mIndicatorStart;
            }
            if (this.mIndicatorEnd >= 0) {
                this.mIndicatorLeft = this.mIndicatorEnd;
            }
        } else {
            if (this.mIndicatorStart >= 0) {
                this.mIndicatorLeft = this.mIndicatorStart;
            }
            if (this.mIndicatorEnd >= 0) {
                this.mIndicatorRight = this.mIndicatorEnd;
            }
        }
        if (this.mIndicatorRight == 0 && this.mGroupIndicator != null) {
            this.mIndicatorRight = this.mIndicatorLeft + this.mGroupIndicator.getIntrinsicWidth();
        }
    }

    private void resolveChildIndicator() {
        if (isRtlLocale()) {
            if (this.mChildIndicatorStart >= CHILD_INDICATOR_INHERIT) {
                this.mChildIndicatorRight = this.mChildIndicatorStart;
            }
            if (this.mChildIndicatorEnd >= CHILD_INDICATOR_INHERIT) {
                this.mChildIndicatorLeft = this.mChildIndicatorEnd;
                return;
            }
            return;
        }
        if (this.mChildIndicatorStart >= CHILD_INDICATOR_INHERIT) {
            this.mChildIndicatorLeft = this.mChildIndicatorStart;
        }
        if (this.mChildIndicatorEnd >= CHILD_INDICATOR_INHERIT) {
            this.mChildIndicatorRight = this.mChildIndicatorEnd;
        }
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (this.mChildIndicator != null || this.mGroupIndicator != null) {
            int saveCount = PACKED_POSITION_TYPE_GROUP;
            boolean clipToPadding = (this.mGroupFlags & 34) == 34;
            if (clipToPadding) {
                saveCount = canvas.save();
                int scrollX = this.mScrollX;
                int scrollY = this.mScrollY;
                canvas.clipRect(this.mPaddingLeft + scrollX, this.mPaddingTop + scrollY, ((this.mRight + scrollX) - this.mLeft) - this.mPaddingRight, ((this.mBottom + scrollY) - this.mTop) - this.mPaddingBottom);
            }
            int headerViewsCount = getHeaderViewsCount();
            int lastChildFlPos = ((this.mItemCount - getFooterViewsCount()) - headerViewsCount) + CHILD_INDICATOR_INHERIT;
            int myB = this.mBottom;
            int lastItemType = -4;
            Rect indicatorRect = this.mIndicatorRect;
            int childCount = getChildCount();
            int i = PACKED_POSITION_TYPE_GROUP;
            int childFlPos = this.mFirstPosition - headerViewsCount;
            while (i < childCount) {
                if (childFlPos >= 0) {
                    if (childFlPos > lastChildFlPos) {
                        break;
                    }
                    View item = getChildAt(i);
                    int t = item.getTop();
                    int b = item.getBottom();
                    if (b >= 0 && t <= myB) {
                        PositionMetadata pos = this.mConnector.getUnflattenedPos(childFlPos);
                        boolean isLayoutRtl = isRtlLocale();
                        int width = getWidth();
                        int i2 = pos.position.type;
                        if (r0 != lastItemType) {
                            i2 = pos.position.type;
                            if (r0 == PACKED_POSITION_TYPE_CHILD) {
                                i2 = this.mChildIndicatorLeft;
                                indicatorRect.left = r0 == CHILD_INDICATOR_INHERIT ? this.mIndicatorLeft : this.mChildIndicatorLeft;
                                i2 = this.mChildIndicatorRight;
                                indicatorRect.right = r0 == CHILD_INDICATOR_INHERIT ? this.mIndicatorRight : this.mChildIndicatorRight;
                            } else {
                                indicatorRect.left = this.mIndicatorLeft;
                                indicatorRect.right = this.mIndicatorRight;
                            }
                            if (!(!isLayoutRtl || this.mIndicatorStart == 0 || this.mGroupIndicator == null)) {
                                indicatorRect.left = this.mIndicatorStart;
                                indicatorRect.right = this.mIndicatorStart + this.mGroupIndicator.getIntrinsicWidth();
                            }
                            if ((HwWidgetFactory.isHwTheme(this.mContext) ^ isLayoutRtl) != 0) {
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
                i += PACKED_POSITION_TYPE_CHILD;
                childFlPos += PACKED_POSITION_TYPE_CHILD;
            }
            if (clipToPadding) {
                canvas.restoreToCount(saveCount);
            }
        }
    }

    private Drawable getIndicator(PositionMetadata pos) {
        Drawable indicator;
        int i = PACKED_POSITION_TYPE_NULL;
        if (pos.position.type == PACKED_POSITION_TYPE_NULL) {
            indicator = this.mGroupIndicator;
            if (indicator != null && indicator.isStateful()) {
                int i2;
                boolean isEmpty = pos.groupMetadata != null ? pos.groupMetadata.lastChildFlPos == pos.groupMetadata.flPos : true;
                if (pos.isExpanded()) {
                    i2 = PACKED_POSITION_TYPE_CHILD;
                } else {
                    i2 = PACKED_POSITION_TYPE_GROUP;
                }
                if (!isEmpty) {
                    i = PACKED_POSITION_TYPE_GROUP;
                }
                indicator.setState(GROUP_STATE_SETS[i2 | i]);
            }
        } else {
            indicator = this.mChildIndicator;
            if (indicator != null && indicator.isStateful()) {
                int[] stateSet;
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

    void drawDivider(Canvas canvas, Rect bounds, int childIndex) {
        int flatListPosition = childIndex + this.mFirstPosition;
        PositionMetadata pos = this.mConnector.getUnflattenedPos(flatListPosition);
        if (pos.position.type == PACKED_POSITION_TYPE_NULL && pos.isExpanded()) {
            Drawable divider = this.mDivider;
            divider.setBounds(bounds);
            divider.draw(canvas);
            pos.recycle();
            return;
        }
        if (flatListPosition >= 0) {
            pos = this.mConnector.getUnflattenedPos(getFlatPositionForConnector(flatListPosition));
            if (pos.position.type == PACKED_POSITION_TYPE_CHILD || (pos.isExpanded() && pos.groupMetadata.lastChildFlPos != pos.groupMetadata.flPos)) {
                divider = this.mChildDivider;
                divider.setBounds(bounds);
                divider.draw(canvas);
                pos.recycle();
                return;
            }
        }
        pos.recycle();
        super.drawDivider(canvas, bounds, flatListPosition);
    }

    public void setAdapter(ListAdapter adapter) {
        throw new RuntimeException("For ExpandableListView, use setAdapter(ExpandableListAdapter) instead of setAdapter(ListAdapter)");
    }

    public ListAdapter getAdapter() {
        return super.getAdapter();
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        super.setOnItemClickListener(l);
    }

    public void setAdapter(ExpandableListAdapter adapter) {
        this.mAdapter = adapter;
        if (adapter != null) {
            this.mConnector = new ExpandableListConnector(adapter);
        } else {
            this.mConnector = null;
        }
        super.setAdapter(this.mConnector);
    }

    public ExpandableListAdapter getExpandableListAdapter() {
        return this.mAdapter;
    }

    private boolean isHeaderOrFooterPosition(int position) {
        int footerViewsStart = this.mItemCount - getFooterViewsCount();
        if (position < getHeaderViewsCount() || position >= footerViewsStart) {
            return true;
        }
        return false;
    }

    private int getFlatPositionForConnector(int flatListPosition) {
        return flatListPosition - getHeaderViewsCount();
    }

    private int getAbsoluteFlatPosition(int flatListPosition) {
        return getHeaderViewsCount() + flatListPosition;
    }

    public boolean performItemClick(View v, int position, long id) {
        if (isHeaderOrFooterPosition(position)) {
            return super.performItemClick(v, position, id);
        }
        return handleItemClick(v, getFlatPositionForConnector(position), id);
    }

    boolean handleItemClick(View v, int position, long id) {
        boolean returnValue;
        PositionMetadata posMetadata = this.mConnector.getUnflattenedPos(position);
        id = getChildOrGroupId(posMetadata.position);
        if (posMetadata.position.type == PACKED_POSITION_TYPE_NULL) {
            if (this.mOnGroupClickListener != null) {
                if (this.mOnGroupClickListener.onGroupClick(this, v, posMetadata.position.groupPos, id)) {
                    posMetadata.recycle();
                    return true;
                }
            }
            if (posMetadata.isExpanded()) {
                this.mConnector.collapseGroup(posMetadata);
                playSoundEffect(PACKED_POSITION_TYPE_GROUP);
                if (this.mOnGroupCollapseListener != null) {
                    this.mOnGroupCollapseListener.onGroupCollapse(posMetadata.position.groupPos);
                }
            } else {
                this.mConnector.expandGroup(posMetadata);
                playSoundEffect(PACKED_POSITION_TYPE_GROUP);
                if (this.mOnGroupExpandListener != null) {
                    this.mOnGroupExpandListener.onGroupExpand(posMetadata.position.groupPos);
                }
                int shiftedGroupPosition = posMetadata.position.flatListPos + getHeaderViewsCount();
                smoothScrollToPosition(this.mAdapter.getChildrenCount(posMetadata.position.groupPos) + shiftedGroupPosition, shiftedGroupPosition);
            }
            returnValue = true;
        } else if (this.mOnChildClickListener != null) {
            playSoundEffect(PACKED_POSITION_TYPE_GROUP);
            return this.mOnChildClickListener.onChildClick(this, v, posMetadata.position.groupPos, posMetadata.position.childPos, id);
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
        ExpandableListPosition elGroupPos = ExpandableListPosition.obtain(PACKED_POSITION_TYPE_NULL, groupPos, CHILD_INDICATOR_INHERIT, CHILD_INDICATOR_INHERIT);
        PositionMetadata pm = this.mConnector.getFlattenedPos(elGroupPos);
        elGroupPos.recycle();
        boolean retValue = this.mConnector.expandGroup(pm);
        if (this.mOnGroupExpandListener != null) {
            this.mOnGroupExpandListener.onGroupExpand(groupPos);
        }
        if (animate) {
            int shiftedGroupPosition = pm.position.flatListPos + getHeaderViewsCount();
            smoothScrollToPosition(this.mAdapter.getChildrenCount(groupPos) + shiftedGroupPosition, shiftedGroupPosition);
        }
        pm.recycle();
        return retValue;
    }

    public boolean collapseGroup(int groupPos) {
        boolean retValue = this.mConnector.collapseGroup(groupPos);
        if (this.mOnGroupCollapseListener != null) {
            this.mOnGroupCollapseListener.onGroupCollapse(groupPos);
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
            return PACKED_POSITION_VALUE_NULL;
        }
        PositionMetadata pm = this.mConnector.getUnflattenedPos(getFlatPositionForConnector(flatListPosition));
        long packedPos = pm.position.getPackedPosition();
        pm.recycle();
        return packedPos;
    }

    public int getFlatListPosition(long packedPosition) {
        ExpandableListPosition elPackedPos = ExpandableListPosition.obtainPosition(packedPosition);
        PositionMetadata pm = this.mConnector.getFlattenedPos(elPackedPos);
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
        if (packedPos == PACKED_POSITION_VALUE_NULL) {
            return PACKED_POSITION_INT_MASK_CHILD;
        }
        int groupPos = getPackedPositionGroup(packedPos);
        if (getPackedPositionType(packedPos) == 0) {
            return this.mAdapter.getGroupId(groupPos);
        }
        return this.mAdapter.getChildId(groupPos, getPackedPositionChild(packedPos));
    }

    public void setSelectedGroup(int groupPosition) {
        ExpandableListPosition elGroupPos = ExpandableListPosition.obtainGroupPosition(groupPosition);
        PositionMetadata pm = this.mConnector.getFlattenedPos(elGroupPos);
        elGroupPos.recycle();
        super.setSelection(getAbsoluteFlatPosition(pm.position.flatListPos));
        pm.recycle();
    }

    public boolean setSelectedChild(int groupPosition, int childPosition, boolean shouldExpandGroup) {
        ExpandableListPosition elChildPos = ExpandableListPosition.obtainChildPosition(groupPosition, childPosition);
        PositionMetadata flatChildPos = this.mConnector.getFlattenedPos(elChildPos);
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
        if (packedPosition == PACKED_POSITION_VALUE_NULL) {
            return PACKED_POSITION_TYPE_NULL;
        }
        int i;
        if ((packedPosition & PACKED_POSITION_MASK_TYPE) == PACKED_POSITION_MASK_TYPE) {
            i = PACKED_POSITION_TYPE_CHILD;
        } else {
            i = PACKED_POSITION_TYPE_GROUP;
        }
        return i;
    }

    public static int getPackedPositionGroup(long packedPosition) {
        if (packedPosition == PACKED_POSITION_VALUE_NULL) {
            return CHILD_INDICATOR_INHERIT;
        }
        return (int) ((PACKED_POSITION_MASK_GROUP & packedPosition) >> 32);
    }

    public static int getPackedPositionChild(long packedPosition) {
        if (packedPosition != PACKED_POSITION_VALUE_NULL && (packedPosition & PACKED_POSITION_MASK_TYPE) == PACKED_POSITION_MASK_TYPE) {
            return (int) (packedPosition & PACKED_POSITION_VALUE_NULL);
        }
        return CHILD_INDICATOR_INHERIT;
    }

    public static long getPackedPositionForChild(int groupPosition, int childPosition) {
        return (((((long) groupPosition) & PACKED_POSITION_INT_MASK_GROUP) << 32) | PACKED_POSITION_MASK_TYPE) | (((long) childPosition) & PACKED_POSITION_INT_MASK_CHILD);
    }

    public static long getPackedPositionForGroup(int groupPosition) {
        return (((long) groupPosition) & PACKED_POSITION_INT_MASK_GROUP) << 32;
    }

    ContextMenuInfo createContextMenuInfo(View view, int flatListPosition, long id) {
        if (isHeaderOrFooterPosition(flatListPosition)) {
            return new AdapterContextMenuInfo(view, flatListPosition, id);
        }
        PositionMetadata pm = this.mConnector.getUnflattenedPos(getFlatPositionForConnector(flatListPosition));
        ExpandableListPosition pos = pm.position;
        id = getChildOrGroupId(pos);
        long packedPosition = pos.getPackedPosition();
        pm.recycle();
        return new ExpandableListContextMenuInfo(view, packedPosition, id);
    }

    private long getChildOrGroupId(ExpandableListPosition position) {
        if (position.type == PACKED_POSITION_TYPE_CHILD) {
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
        this.mGroupIndicator = groupIndicator;
        if (this.mIndicatorRight == 0 && this.mGroupIndicator != null) {
            this.mIndicatorRight = this.mIndicatorLeft + this.mGroupIndicator.getIntrinsicWidth();
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

    public Parcelable onSaveInstanceState() {
        ArrayList arrayList = null;
        Parcelable superState = super.onSaveInstanceState();
        if (this.mConnector != null) {
            arrayList = this.mConnector.getExpandedGroupMetadataList();
        }
        return new SavedState(superState, arrayList);
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState ss = (SavedState) state;
            super.onRestoreInstanceState(ss.getSuperState());
            if (!(this.mConnector == null || ss.expandedGroupMetadataList == null)) {
                this.mConnector.setExpandedGroupMetadataList(ss.expandedGroupMetadataList);
            }
            return;
        }
        super.onRestoreInstanceState(state);
    }

    public CharSequence getAccessibilityClassName() {
        return ExpandableListView.class.getName();
    }
}
