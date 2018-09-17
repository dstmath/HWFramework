package android.view.accessibility;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AccessibilityClickableSpan;
import android.text.style.AccessibilityURLSpan;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.ArraySet;
import android.util.LongArray;
import android.util.Pools.SynchronizedPool;
import android.view.View;
import com.android.internal.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AccessibilityNodeInfo implements Parcelable {
    public static final int ACTION_ACCESSIBILITY_FOCUS = 64;
    public static final String ACTION_ARGUMENT_ACCESSIBLE_CLICKABLE_SPAN = "android.view.accessibility.action.ACTION_ARGUMENT_ACCESSIBLE_CLICKABLE_SPAN";
    public static final String ACTION_ARGUMENT_COLUMN_INT = "android.view.accessibility.action.ARGUMENT_COLUMN_INT";
    public static final String ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN = "ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN";
    public static final String ACTION_ARGUMENT_HTML_ELEMENT_STRING = "ACTION_ARGUMENT_HTML_ELEMENT_STRING";
    public static final String ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT = "ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT";
    public static final String ACTION_ARGUMENT_MOVE_WINDOW_X = "ACTION_ARGUMENT_MOVE_WINDOW_X";
    public static final String ACTION_ARGUMENT_MOVE_WINDOW_Y = "ACTION_ARGUMENT_MOVE_WINDOW_Y";
    public static final String ACTION_ARGUMENT_PROGRESS_VALUE = "android.view.accessibility.action.ARGUMENT_PROGRESS_VALUE";
    public static final String ACTION_ARGUMENT_ROW_INT = "android.view.accessibility.action.ARGUMENT_ROW_INT";
    public static final String ACTION_ARGUMENT_SELECTION_END_INT = "ACTION_ARGUMENT_SELECTION_END_INT";
    public static final String ACTION_ARGUMENT_SELECTION_START_INT = "ACTION_ARGUMENT_SELECTION_START_INT";
    public static final String ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE = "ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE";
    public static final int ACTION_CLEAR_ACCESSIBILITY_FOCUS = 128;
    public static final int ACTION_CLEAR_FOCUS = 2;
    public static final int ACTION_CLEAR_SELECTION = 8;
    public static final int ACTION_CLICK = 16;
    public static final int ACTION_COLLAPSE = 524288;
    public static final int ACTION_COPY = 16384;
    public static final int ACTION_CUT = 65536;
    public static final int ACTION_DISMISS = 1048576;
    public static final int ACTION_EXPAND = 262144;
    public static final int ACTION_FOCUS = 1;
    public static final int ACTION_LONG_CLICK = 32;
    public static final int ACTION_NEXT_AT_MOVEMENT_GRANULARITY = 256;
    public static final int ACTION_NEXT_HTML_ELEMENT = 1024;
    public static final int ACTION_PASTE = 32768;
    public static final int ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY = 512;
    public static final int ACTION_PREVIOUS_HTML_ELEMENT = 2048;
    public static final int ACTION_SCROLL_BACKWARD = 8192;
    public static final int ACTION_SCROLL_FORWARD = 4096;
    public static final int ACTION_SELECT = 4;
    public static final int ACTION_SET_SELECTION = 131072;
    public static final int ACTION_SET_TEXT = 2097152;
    private static final int ACTION_TYPE_MASK = -16777216;
    private static final int BOOLEAN_PROPERTY_ACCESSIBILITY_FOCUSED = 1024;
    private static final int BOOLEAN_PROPERTY_CHECKABLE = 1;
    private static final int BOOLEAN_PROPERTY_CHECKED = 2;
    private static final int BOOLEAN_PROPERTY_CLICKABLE = 32;
    private static final int BOOLEAN_PROPERTY_CONTENT_INVALID = 65536;
    private static final int BOOLEAN_PROPERTY_CONTEXT_CLICKABLE = 131072;
    private static final int BOOLEAN_PROPERTY_DISMISSABLE = 16384;
    private static final int BOOLEAN_PROPERTY_EDITABLE = 4096;
    private static final int BOOLEAN_PROPERTY_ENABLED = 128;
    private static final int BOOLEAN_PROPERTY_FOCUSABLE = 4;
    private static final int BOOLEAN_PROPERTY_FOCUSED = 8;
    private static final int BOOLEAN_PROPERTY_IMPORTANCE = 262144;
    private static final int BOOLEAN_PROPERTY_IS_SHOWING_HINT = 1048576;
    private static final int BOOLEAN_PROPERTY_LONG_CLICKABLE = 64;
    private static final int BOOLEAN_PROPERTY_MULTI_LINE = 32768;
    private static final int BOOLEAN_PROPERTY_OPENS_POPUP = 8192;
    private static final int BOOLEAN_PROPERTY_PASSWORD = 256;
    private static final int BOOLEAN_PROPERTY_SCROLLABLE = 512;
    private static final int BOOLEAN_PROPERTY_SELECTED = 16;
    private static final int BOOLEAN_PROPERTY_VISIBLE_TO_USER = 2048;
    public static final Creator<AccessibilityNodeInfo> CREATOR = new Creator<AccessibilityNodeInfo>() {
        public AccessibilityNodeInfo createFromParcel(Parcel parcel) {
            AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain();
            info.initFromParcel(parcel);
            return info;
        }

        public AccessibilityNodeInfo[] newArray(int size) {
            return new AccessibilityNodeInfo[size];
        }
    };
    private static final boolean DEBUG = false;
    public static final String EXTRA_DATA_REQUESTED_KEY = "android.view.accessibility.AccessibilityNodeInfo.extra_data_requested";
    public static final String EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_LENGTH = "android.view.accessibility.extra.DATA_TEXT_CHARACTER_LOCATION_ARG_LENGTH";
    public static final String EXTRA_DATA_TEXT_CHARACTER_LOCATION_ARG_START_INDEX = "android.view.accessibility.extra.DATA_TEXT_CHARACTER_LOCATION_ARG_START_INDEX";
    public static final String EXTRA_DATA_TEXT_CHARACTER_LOCATION_KEY = "android.view.accessibility.extra.DATA_TEXT_CHARACTER_LOCATION_KEY";
    public static final int FLAG_INCLUDE_NOT_IMPORTANT_VIEWS = 8;
    public static final int FLAG_PREFETCH_DESCENDANTS = 4;
    public static final int FLAG_PREFETCH_PREDECESSORS = 1;
    public static final int FLAG_PREFETCH_SIBLINGS = 2;
    public static final int FLAG_REPORT_VIEW_IDS = 16;
    public static final int FOCUS_ACCESSIBILITY = 2;
    public static final int FOCUS_INPUT = 1;
    private static final int LAST_LEGACY_STANDARD_ACTION = 2097152;
    private static final int MAX_POOL_SIZE = 50;
    public static final int MOVEMENT_GRANULARITY_CHARACTER = 1;
    public static final int MOVEMENT_GRANULARITY_LINE = 4;
    public static final int MOVEMENT_GRANULARITY_PAGE = 16;
    public static final int MOVEMENT_GRANULARITY_PARAGRAPH = 8;
    public static final int MOVEMENT_GRANULARITY_WORD = 2;
    public static final int ROOT_ITEM_ID = 2147483646;
    public static final long ROOT_NODE_ID = makeNodeId(2147483646, -1);
    public static final int UNDEFINED_CONNECTION_ID = -1;
    public static final int UNDEFINED_ITEM_ID = Integer.MAX_VALUE;
    public static final long UNDEFINED_NODE_ID = makeNodeId(Integer.MAX_VALUE, Integer.MAX_VALUE);
    public static final int UNDEFINED_SELECTION_INDEX = -1;
    private static final long VIRTUAL_DESCENDANT_ID_MASK = -4294967296L;
    private static final int VIRTUAL_DESCENDANT_ID_SHIFT = 32;
    private static AtomicInteger sNumInstancesInUse;
    private static final SynchronizedPool<AccessibilityNodeInfo> sPool = new SynchronizedPool(50);
    private ArrayList<AccessibilityAction> mActions;
    private int mBooleanProperties;
    private final Rect mBoundsInParent = new Rect();
    private final Rect mBoundsInScreen = new Rect();
    private LongArray mChildNodeIds;
    private CharSequence mClassName;
    private CollectionInfo mCollectionInfo;
    private CollectionItemInfo mCollectionItemInfo;
    private int mConnectionId = -1;
    private CharSequence mContentDescription;
    private int mDrawingOrderInParent;
    private CharSequence mError;
    private ArrayList<String> mExtraDataKeys;
    private Bundle mExtras;
    private CharSequence mHintText;
    private int mInputType = 0;
    private long mLabelForId = UNDEFINED_NODE_ID;
    private long mLabeledById = UNDEFINED_NODE_ID;
    private int mLiveRegion = 0;
    private int mMaxTextLength = -1;
    private int mMovementGranularities;
    private CharSequence mOriginalText;
    private CharSequence mPackageName;
    private long mParentNodeId = UNDEFINED_NODE_ID;
    private RangeInfo mRangeInfo;
    private boolean mSealed;
    private long mSourceNodeId = UNDEFINED_NODE_ID;
    private CharSequence mText;
    private int mTextSelectionEnd = -1;
    private int mTextSelectionStart = -1;
    private long mTraversalAfter = UNDEFINED_NODE_ID;
    private long mTraversalBefore = UNDEFINED_NODE_ID;
    private String mViewIdResourceName;
    private int mWindowId = -1;

    public static final class AccessibilityAction {
        public static final AccessibilityAction ACTION_ACCESSIBILITY_FOCUS = new AccessibilityAction(64, null);
        public static final AccessibilityAction ACTION_CLEAR_ACCESSIBILITY_FOCUS = new AccessibilityAction(128, null);
        public static final AccessibilityAction ACTION_CLEAR_FOCUS = new AccessibilityAction(2, null);
        public static final AccessibilityAction ACTION_CLEAR_SELECTION = new AccessibilityAction(8, null);
        public static final AccessibilityAction ACTION_CLICK = new AccessibilityAction(16, null);
        public static final AccessibilityAction ACTION_COLLAPSE = new AccessibilityAction(524288, null);
        public static final AccessibilityAction ACTION_CONTEXT_CLICK = new AccessibilityAction(R.id.accessibilityActionContextClick, null);
        public static final AccessibilityAction ACTION_COPY = new AccessibilityAction(16384, null);
        public static final AccessibilityAction ACTION_CUT = new AccessibilityAction(65536, null);
        public static final AccessibilityAction ACTION_DISMISS = new AccessibilityAction(1048576, null);
        public static final AccessibilityAction ACTION_EXPAND = new AccessibilityAction(262144, null);
        public static final AccessibilityAction ACTION_FOCUS = new AccessibilityAction(1, null);
        public static final AccessibilityAction ACTION_LONG_CLICK = new AccessibilityAction(32, null);
        public static final AccessibilityAction ACTION_MOVE_WINDOW = new AccessibilityAction(R.id.accessibilityActionMoveWindow, null);
        public static final AccessibilityAction ACTION_NEXT_AT_MOVEMENT_GRANULARITY = new AccessibilityAction(256, null);
        public static final AccessibilityAction ACTION_NEXT_HTML_ELEMENT = new AccessibilityAction(1024, null);
        public static final AccessibilityAction ACTION_PASTE = new AccessibilityAction(32768, null);
        public static final AccessibilityAction ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY = new AccessibilityAction(512, null);
        public static final AccessibilityAction ACTION_PREVIOUS_HTML_ELEMENT = new AccessibilityAction(2048, null);
        public static final AccessibilityAction ACTION_SCROLL_BACKWARD = new AccessibilityAction(8192, null);
        public static final AccessibilityAction ACTION_SCROLL_DOWN = new AccessibilityAction(R.id.accessibilityActionScrollDown, null);
        public static final AccessibilityAction ACTION_SCROLL_FORWARD = new AccessibilityAction(4096, null);
        public static final AccessibilityAction ACTION_SCROLL_LEFT = new AccessibilityAction(R.id.accessibilityActionScrollLeft, null);
        public static final AccessibilityAction ACTION_SCROLL_RIGHT = new AccessibilityAction(R.id.accessibilityActionScrollRight, null);
        public static final AccessibilityAction ACTION_SCROLL_TO_POSITION = new AccessibilityAction(R.id.accessibilityActionScrollToPosition, null);
        public static final AccessibilityAction ACTION_SCROLL_UP = new AccessibilityAction(R.id.accessibilityActionScrollUp, null);
        public static final AccessibilityAction ACTION_SELECT = new AccessibilityAction(4, null);
        public static final AccessibilityAction ACTION_SET_PROGRESS = new AccessibilityAction(R.id.accessibilityActionSetProgress, null);
        public static final AccessibilityAction ACTION_SET_SELECTION = new AccessibilityAction(131072, null);
        public static final AccessibilityAction ACTION_SET_TEXT = new AccessibilityAction(2097152, null);
        public static final AccessibilityAction ACTION_SHOW_ON_SCREEN = new AccessibilityAction(R.id.accessibilityActionShowOnScreen, null);
        private static final ArraySet<AccessibilityAction> sStandardActions = new ArraySet();
        private final int mActionId;
        private final CharSequence mLabel;

        static {
            sStandardActions.add(ACTION_FOCUS);
            sStandardActions.add(ACTION_CLEAR_FOCUS);
            sStandardActions.add(ACTION_SELECT);
            sStandardActions.add(ACTION_CLEAR_SELECTION);
            sStandardActions.add(ACTION_CLICK);
            sStandardActions.add(ACTION_LONG_CLICK);
            sStandardActions.add(ACTION_ACCESSIBILITY_FOCUS);
            sStandardActions.add(ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            sStandardActions.add(ACTION_NEXT_AT_MOVEMENT_GRANULARITY);
            sStandardActions.add(ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY);
            sStandardActions.add(ACTION_NEXT_HTML_ELEMENT);
            sStandardActions.add(ACTION_PREVIOUS_HTML_ELEMENT);
            sStandardActions.add(ACTION_SCROLL_FORWARD);
            sStandardActions.add(ACTION_SCROLL_BACKWARD);
            sStandardActions.add(ACTION_COPY);
            sStandardActions.add(ACTION_PASTE);
            sStandardActions.add(ACTION_CUT);
            sStandardActions.add(ACTION_SET_SELECTION);
            sStandardActions.add(ACTION_EXPAND);
            sStandardActions.add(ACTION_COLLAPSE);
            sStandardActions.add(ACTION_DISMISS);
            sStandardActions.add(ACTION_SET_TEXT);
            sStandardActions.add(ACTION_SHOW_ON_SCREEN);
            sStandardActions.add(ACTION_SCROLL_TO_POSITION);
            sStandardActions.add(ACTION_SCROLL_UP);
            sStandardActions.add(ACTION_SCROLL_LEFT);
            sStandardActions.add(ACTION_SCROLL_DOWN);
            sStandardActions.add(ACTION_SCROLL_RIGHT);
            sStandardActions.add(ACTION_SET_PROGRESS);
            sStandardActions.add(ACTION_CONTEXT_CLICK);
        }

        public AccessibilityAction(int actionId, CharSequence label) {
            if ((-16777216 & actionId) != 0 || Integer.bitCount(actionId) == 1) {
                this.mActionId = actionId;
                this.mLabel = label;
                return;
            }
            throw new IllegalArgumentException("Invalid standard action id");
        }

        public int getId() {
            return this.mActionId;
        }

        public CharSequence getLabel() {
            return this.mLabel;
        }

        public int hashCode() {
            return this.mActionId;
        }

        public boolean equals(Object other) {
            boolean z = true;
            if (other == null) {
                return false;
            }
            if (other == this) {
                return true;
            }
            if (getClass() != other.getClass()) {
                return false;
            }
            if (this.mActionId != ((AccessibilityAction) other).mActionId) {
                z = false;
            }
            return z;
        }

        public String toString() {
            return "AccessibilityAction: " + AccessibilityNodeInfo.getActionSymbolicName(this.mActionId) + " - " + this.mLabel;
        }
    }

    public static final class CollectionInfo {
        private static final int MAX_POOL_SIZE = 20;
        public static final int SELECTION_MODE_MULTIPLE = 2;
        public static final int SELECTION_MODE_NONE = 0;
        public static final int SELECTION_MODE_SINGLE = 1;
        private static final SynchronizedPool<CollectionInfo> sPool = new SynchronizedPool(20);
        private int mColumnCount;
        private boolean mHierarchical;
        private int mRowCount;
        private int mSelectionMode;

        public static CollectionInfo obtain(CollectionInfo other) {
            return obtain(other.mRowCount, other.mColumnCount, other.mHierarchical, other.mSelectionMode);
        }

        public static CollectionInfo obtain(int rowCount, int columnCount, boolean hierarchical) {
            return obtain(rowCount, columnCount, hierarchical, 0);
        }

        public static CollectionInfo obtain(int rowCount, int columnCount, boolean hierarchical, int selectionMode) {
            CollectionInfo info = (CollectionInfo) sPool.acquire();
            if (info == null) {
                return new CollectionInfo(rowCount, columnCount, hierarchical, selectionMode);
            }
            info.mRowCount = rowCount;
            info.mColumnCount = columnCount;
            info.mHierarchical = hierarchical;
            info.mSelectionMode = selectionMode;
            return info;
        }

        private CollectionInfo(int rowCount, int columnCount, boolean hierarchical, int selectionMode) {
            this.mRowCount = rowCount;
            this.mColumnCount = columnCount;
            this.mHierarchical = hierarchical;
            this.mSelectionMode = selectionMode;
        }

        public int getRowCount() {
            return this.mRowCount;
        }

        public int getColumnCount() {
            return this.mColumnCount;
        }

        public boolean isHierarchical() {
            return this.mHierarchical;
        }

        public int getSelectionMode() {
            return this.mSelectionMode;
        }

        void recycle() {
            clear();
            sPool.release(this);
        }

        private void clear() {
            this.mRowCount = 0;
            this.mColumnCount = 0;
            this.mHierarchical = false;
            this.mSelectionMode = 0;
        }
    }

    public static final class CollectionItemInfo {
        private static final int MAX_POOL_SIZE = 20;
        private static final SynchronizedPool<CollectionItemInfo> sPool = new SynchronizedPool(20);
        private int mColumnIndex;
        private int mColumnSpan;
        private boolean mHeading;
        private int mRowIndex;
        private int mRowSpan;
        private boolean mSelected;

        public static CollectionItemInfo obtain(CollectionItemInfo other) {
            return obtain(other.mRowIndex, other.mRowSpan, other.mColumnIndex, other.mColumnSpan, other.mHeading, other.mSelected);
        }

        public static CollectionItemInfo obtain(int rowIndex, int rowSpan, int columnIndex, int columnSpan, boolean heading) {
            return obtain(rowIndex, rowSpan, columnIndex, columnSpan, heading, false);
        }

        public static CollectionItemInfo obtain(int rowIndex, int rowSpan, int columnIndex, int columnSpan, boolean heading, boolean selected) {
            CollectionItemInfo info = (CollectionItemInfo) sPool.acquire();
            if (info == null) {
                return new CollectionItemInfo(rowIndex, rowSpan, columnIndex, columnSpan, heading, selected);
            }
            info.mRowIndex = rowIndex;
            info.mRowSpan = rowSpan;
            info.mColumnIndex = columnIndex;
            info.mColumnSpan = columnSpan;
            info.mHeading = heading;
            info.mSelected = selected;
            return info;
        }

        private CollectionItemInfo(int rowIndex, int rowSpan, int columnIndex, int columnSpan, boolean heading, boolean selected) {
            this.mRowIndex = rowIndex;
            this.mRowSpan = rowSpan;
            this.mColumnIndex = columnIndex;
            this.mColumnSpan = columnSpan;
            this.mHeading = heading;
            this.mSelected = selected;
        }

        public int getColumnIndex() {
            return this.mColumnIndex;
        }

        public int getRowIndex() {
            return this.mRowIndex;
        }

        public int getColumnSpan() {
            return this.mColumnSpan;
        }

        public int getRowSpan() {
            return this.mRowSpan;
        }

        public boolean isHeading() {
            return this.mHeading;
        }

        public boolean isSelected() {
            return this.mSelected;
        }

        void recycle() {
            clear();
            sPool.release(this);
        }

        private void clear() {
            this.mColumnIndex = 0;
            this.mColumnSpan = 0;
            this.mRowIndex = 0;
            this.mRowSpan = 0;
            this.mHeading = false;
            this.mSelected = false;
        }
    }

    public static final class RangeInfo {
        private static final int MAX_POOL_SIZE = 10;
        public static final int RANGE_TYPE_FLOAT = 1;
        public static final int RANGE_TYPE_INT = 0;
        public static final int RANGE_TYPE_PERCENT = 2;
        private static final SynchronizedPool<RangeInfo> sPool = new SynchronizedPool(10);
        private float mCurrent;
        private float mMax;
        private float mMin;
        private int mType;

        public static RangeInfo obtain(RangeInfo other) {
            return obtain(other.mType, other.mMin, other.mMax, other.mCurrent);
        }

        public static RangeInfo obtain(int type, float min, float max, float current) {
            RangeInfo info = (RangeInfo) sPool.acquire();
            if (info == null) {
                return new RangeInfo(type, min, max, current);
            }
            info.mType = type;
            info.mMin = min;
            info.mMax = max;
            info.mCurrent = current;
            return info;
        }

        private RangeInfo(int type, float min, float max, float current) {
            this.mType = type;
            this.mMin = min;
            this.mMax = max;
            this.mCurrent = current;
        }

        public int getType() {
            return this.mType;
        }

        public float getMin() {
            return this.mMin;
        }

        public float getMax() {
            return this.mMax;
        }

        public float getCurrent() {
            return this.mCurrent;
        }

        void recycle() {
            clear();
            sPool.release(this);
        }

        private void clear() {
            this.mType = 0;
            this.mMin = 0.0f;
            this.mMax = 0.0f;
            this.mCurrent = 0.0f;
        }
    }

    public static int getAccessibilityViewId(long accessibilityNodeId) {
        return (int) accessibilityNodeId;
    }

    public static int getVirtualDescendantId(long accessibilityNodeId) {
        return (int) ((VIRTUAL_DESCENDANT_ID_MASK & accessibilityNodeId) >> 32);
    }

    public static long makeNodeId(int accessibilityViewId, int virtualDescendantId) {
        return (((long) virtualDescendantId) << 32) | ((long) accessibilityViewId);
    }

    private AccessibilityNodeInfo() {
    }

    public void setSource(View source) {
        setSource(source, -1);
    }

    public void setSource(View root, int virtualDescendantId) {
        enforceNotSealed();
        this.mWindowId = root != null ? root.getAccessibilityWindowId() : Integer.MAX_VALUE;
        this.mSourceNodeId = makeNodeId(root != null ? root.getAccessibilityViewId() : Integer.MAX_VALUE, virtualDescendantId);
    }

    public AccessibilityNodeInfo findFocus(int focus) {
        enforceSealed();
        enforceValidFocusType(focus);
        if (canPerformRequestOverConnection(this.mSourceNodeId)) {
            return AccessibilityInteractionClient.getInstance().findFocus(this.mConnectionId, this.mWindowId, this.mSourceNodeId, focus);
        }
        return null;
    }

    public AccessibilityNodeInfo focusSearch(int direction) {
        enforceSealed();
        enforceValidFocusDirection(direction);
        if (canPerformRequestOverConnection(this.mSourceNodeId)) {
            return AccessibilityInteractionClient.getInstance().focusSearch(this.mConnectionId, this.mWindowId, this.mSourceNodeId, direction);
        }
        return null;
    }

    public int getWindowId() {
        return this.mWindowId;
    }

    public boolean refresh(Bundle arguments, boolean bypassCache) {
        enforceSealed();
        if (!canPerformRequestOverConnection(this.mSourceNodeId)) {
            return false;
        }
        AccessibilityNodeInfo refreshedInfo = AccessibilityInteractionClient.getInstance().findAccessibilityNodeInfoByAccessibilityId(this.mConnectionId, this.mWindowId, this.mSourceNodeId, bypassCache, 0, arguments);
        if (refreshedInfo == null) {
            return false;
        }
        init(refreshedInfo);
        refreshedInfo.recycle();
        return true;
    }

    public boolean refresh() {
        return refresh(null, true);
    }

    public boolean refreshWithExtraData(String extraDataKey, Bundle args) {
        args.putString(EXTRA_DATA_REQUESTED_KEY, extraDataKey);
        return refresh(args, true);
    }

    public LongArray getChildNodeIds() {
        return this.mChildNodeIds;
    }

    public long getChildId(int index) {
        if (this.mChildNodeIds != null) {
            return this.mChildNodeIds.get(index);
        }
        throw new IndexOutOfBoundsException();
    }

    public int getChildCount() {
        return this.mChildNodeIds == null ? 0 : this.mChildNodeIds.size();
    }

    public AccessibilityNodeInfo getChild(int index) {
        enforceSealed();
        if (this.mChildNodeIds == null || !canPerformRequestOverConnection(this.mSourceNodeId)) {
            return null;
        }
        return AccessibilityInteractionClient.getInstance().findAccessibilityNodeInfoByAccessibilityId(this.mConnectionId, this.mWindowId, this.mChildNodeIds.get(index), false, 4, null);
    }

    public void addChild(View child) {
        addChildInternal(child, -1, true);
    }

    public void addChildUnchecked(View child) {
        addChildInternal(child, -1, false);
    }

    public boolean removeChild(View child) {
        return removeChild(child, -1);
    }

    public void addChild(View root, int virtualDescendantId) {
        addChildInternal(root, virtualDescendantId, true);
    }

    private void addChildInternal(View root, int virtualDescendantId, boolean checked) {
        enforceNotSealed();
        if (this.mChildNodeIds == null) {
            this.mChildNodeIds = new LongArray();
        }
        long childNodeId = makeNodeId(root != null ? root.getAccessibilityViewId() : Integer.MAX_VALUE, virtualDescendantId);
        if (!checked || this.mChildNodeIds.indexOf(childNodeId) < 0) {
            this.mChildNodeIds.add(childNodeId);
        }
    }

    public boolean removeChild(View root, int virtualDescendantId) {
        enforceNotSealed();
        LongArray childIds = this.mChildNodeIds;
        if (childIds == null) {
            return false;
        }
        int index = childIds.indexOf(makeNodeId(root != null ? root.getAccessibilityViewId() : Integer.MAX_VALUE, virtualDescendantId));
        if (index < 0) {
            return false;
        }
        childIds.remove(index);
        return true;
    }

    public List<AccessibilityAction> getActionList() {
        if (this.mActions == null) {
            return Collections.emptyList();
        }
        return this.mActions;
    }

    @Deprecated
    public int getActions() {
        int returnValue = 0;
        if (this.mActions == null) {
            return 0;
        }
        int actionSize = this.mActions.size();
        for (int i = 0; i < actionSize; i++) {
            int actionId = ((AccessibilityAction) this.mActions.get(i)).getId();
            if (actionId <= 2097152) {
                returnValue |= actionId;
            }
        }
        return returnValue;
    }

    public void addAction(AccessibilityAction action) {
        enforceNotSealed();
        addActionUnchecked(action);
    }

    private void addActionUnchecked(AccessibilityAction action) {
        if (action != null) {
            if (this.mActions == null) {
                this.mActions = new ArrayList();
            }
            this.mActions.remove(action);
            this.mActions.add(action);
        }
    }

    @Deprecated
    public void addAction(int action) {
        enforceNotSealed();
        if ((-16777216 & action) != 0) {
            throw new IllegalArgumentException("Action is not a combination of the standard actions: " + action);
        }
        addLegacyStandardActions(action);
    }

    @Deprecated
    public void removeAction(int action) {
        enforceNotSealed();
        removeAction(getActionSingleton(action));
    }

    public boolean removeAction(AccessibilityAction action) {
        enforceNotSealed();
        if (this.mActions == null || action == null) {
            return false;
        }
        return this.mActions.remove(action);
    }

    public void removeAllActions() {
        if (this.mActions != null) {
            this.mActions.clear();
        }
    }

    public AccessibilityNodeInfo getTraversalBefore() {
        enforceSealed();
        return getNodeForAccessibilityId(this.mTraversalBefore);
    }

    public void setTraversalBefore(View view) {
        setTraversalBefore(view, -1);
    }

    public void setTraversalBefore(View root, int virtualDescendantId) {
        enforceNotSealed();
        this.mTraversalBefore = makeNodeId(root != null ? root.getAccessibilityViewId() : Integer.MAX_VALUE, virtualDescendantId);
    }

    public AccessibilityNodeInfo getTraversalAfter() {
        enforceSealed();
        return getNodeForAccessibilityId(this.mTraversalAfter);
    }

    public void setTraversalAfter(View view) {
        setTraversalAfter(view, -1);
    }

    public void setTraversalAfter(View root, int virtualDescendantId) {
        enforceNotSealed();
        this.mTraversalAfter = makeNodeId(root != null ? root.getAccessibilityViewId() : Integer.MAX_VALUE, virtualDescendantId);
    }

    public List<String> getAvailableExtraData() {
        if (this.mExtraDataKeys != null) {
            return Collections.unmodifiableList(this.mExtraDataKeys);
        }
        return Collections.EMPTY_LIST;
    }

    public void setAvailableExtraData(List<String> extraDataKeys) {
        enforceNotSealed();
        this.mExtraDataKeys = new ArrayList(extraDataKeys);
    }

    public void setMaxTextLength(int max) {
        enforceNotSealed();
        this.mMaxTextLength = max;
    }

    public int getMaxTextLength() {
        return this.mMaxTextLength;
    }

    public void setMovementGranularities(int granularities) {
        enforceNotSealed();
        this.mMovementGranularities = granularities;
    }

    public int getMovementGranularities() {
        return this.mMovementGranularities;
    }

    public boolean performAction(int action) {
        enforceSealed();
        if (canPerformRequestOverConnection(this.mSourceNodeId)) {
            return AccessibilityInteractionClient.getInstance().performAccessibilityAction(this.mConnectionId, this.mWindowId, this.mSourceNodeId, action, null);
        }
        return false;
    }

    public boolean performAction(int action, Bundle arguments) {
        enforceSealed();
        if (canPerformRequestOverConnection(this.mSourceNodeId)) {
            return AccessibilityInteractionClient.getInstance().performAccessibilityAction(this.mConnectionId, this.mWindowId, this.mSourceNodeId, action, arguments);
        }
        return false;
    }

    public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText(String text) {
        enforceSealed();
        if (canPerformRequestOverConnection(this.mSourceNodeId)) {
            return AccessibilityInteractionClient.getInstance().findAccessibilityNodeInfosByText(this.mConnectionId, this.mWindowId, this.mSourceNodeId, text);
        }
        return Collections.emptyList();
    }

    public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByViewId(String viewId) {
        enforceSealed();
        if (canPerformRequestOverConnection(this.mSourceNodeId)) {
            return AccessibilityInteractionClient.getInstance().findAccessibilityNodeInfosByViewId(this.mConnectionId, this.mWindowId, this.mSourceNodeId, viewId);
        }
        return Collections.emptyList();
    }

    public AccessibilityWindowInfo getWindow() {
        enforceSealed();
        if (canPerformRequestOverConnection(this.mSourceNodeId)) {
            return AccessibilityInteractionClient.getInstance().getWindow(this.mConnectionId, this.mWindowId);
        }
        return null;
    }

    public AccessibilityNodeInfo getParent() {
        enforceSealed();
        return getNodeForAccessibilityId(this.mParentNodeId);
    }

    public long getParentNodeId() {
        return this.mParentNodeId;
    }

    public void setParent(View parent) {
        setParent(parent, -1);
    }

    public void setParent(View root, int virtualDescendantId) {
        enforceNotSealed();
        this.mParentNodeId = makeNodeId(root != null ? root.getAccessibilityViewId() : Integer.MAX_VALUE, virtualDescendantId);
    }

    public void getBoundsInParent(Rect outBounds) {
        outBounds.set(this.mBoundsInParent.left, this.mBoundsInParent.top, this.mBoundsInParent.right, this.mBoundsInParent.bottom);
    }

    public void setBoundsInParent(Rect bounds) {
        enforceNotSealed();
        this.mBoundsInParent.set(bounds.left, bounds.top, bounds.right, bounds.bottom);
    }

    public void getBoundsInScreen(Rect outBounds) {
        outBounds.set(this.mBoundsInScreen.left, this.mBoundsInScreen.top, this.mBoundsInScreen.right, this.mBoundsInScreen.bottom);
    }

    public Rect getBoundsInScreen() {
        return this.mBoundsInScreen;
    }

    public void setBoundsInScreen(Rect bounds) {
        enforceNotSealed();
        this.mBoundsInScreen.set(bounds.left, bounds.top, bounds.right, bounds.bottom);
    }

    public boolean isCheckable() {
        return getBooleanProperty(1);
    }

    public void setCheckable(boolean checkable) {
        setBooleanProperty(1, checkable);
    }

    public boolean isChecked() {
        return getBooleanProperty(2);
    }

    public void setChecked(boolean checked) {
        setBooleanProperty(2, checked);
    }

    public boolean isFocusable() {
        return getBooleanProperty(4);
    }

    public void setFocusable(boolean focusable) {
        setBooleanProperty(4, focusable);
    }

    public boolean isFocused() {
        return getBooleanProperty(8);
    }

    public void setFocused(boolean focused) {
        setBooleanProperty(8, focused);
    }

    public boolean isVisibleToUser() {
        return getBooleanProperty(2048);
    }

    public void setVisibleToUser(boolean visibleToUser) {
        setBooleanProperty(2048, visibleToUser);
    }

    public boolean isAccessibilityFocused() {
        return getBooleanProperty(1024);
    }

    public void setAccessibilityFocused(boolean focused) {
        setBooleanProperty(1024, focused);
    }

    public boolean isSelected() {
        return getBooleanProperty(16);
    }

    public void setSelected(boolean selected) {
        setBooleanProperty(16, selected);
    }

    public boolean isClickable() {
        return getBooleanProperty(32);
    }

    public void setClickable(boolean clickable) {
        setBooleanProperty(32, clickable);
    }

    public boolean isLongClickable() {
        return getBooleanProperty(64);
    }

    public void setLongClickable(boolean longClickable) {
        setBooleanProperty(64, longClickable);
    }

    public boolean isEnabled() {
        return getBooleanProperty(128);
    }

    public void setEnabled(boolean enabled) {
        setBooleanProperty(128, enabled);
    }

    public boolean isPassword() {
        return getBooleanProperty(256);
    }

    public void setPassword(boolean password) {
        setBooleanProperty(256, password);
    }

    public boolean isScrollable() {
        return getBooleanProperty(512);
    }

    public void setScrollable(boolean scrollable) {
        setBooleanProperty(512, scrollable);
    }

    public boolean isEditable() {
        return getBooleanProperty(4096);
    }

    public void setEditable(boolean editable) {
        setBooleanProperty(4096, editable);
    }

    public int getDrawingOrder() {
        return this.mDrawingOrderInParent;
    }

    public void setDrawingOrder(int drawingOrderInParent) {
        enforceNotSealed();
        this.mDrawingOrderInParent = drawingOrderInParent;
    }

    public CollectionInfo getCollectionInfo() {
        return this.mCollectionInfo;
    }

    public void setCollectionInfo(CollectionInfo collectionInfo) {
        enforceNotSealed();
        this.mCollectionInfo = collectionInfo;
    }

    public CollectionItemInfo getCollectionItemInfo() {
        return this.mCollectionItemInfo;
    }

    public void setCollectionItemInfo(CollectionItemInfo collectionItemInfo) {
        enforceNotSealed();
        this.mCollectionItemInfo = collectionItemInfo;
    }

    public RangeInfo getRangeInfo() {
        return this.mRangeInfo;
    }

    public void setRangeInfo(RangeInfo rangeInfo) {
        enforceNotSealed();
        this.mRangeInfo = rangeInfo;
    }

    public boolean isContentInvalid() {
        return getBooleanProperty(65536);
    }

    public void setContentInvalid(boolean contentInvalid) {
        setBooleanProperty(65536, contentInvalid);
    }

    public boolean isContextClickable() {
        return getBooleanProperty(131072);
    }

    public void setContextClickable(boolean contextClickable) {
        setBooleanProperty(131072, contextClickable);
    }

    public int getLiveRegion() {
        return this.mLiveRegion;
    }

    public void setLiveRegion(int mode) {
        enforceNotSealed();
        this.mLiveRegion = mode;
    }

    public boolean isMultiLine() {
        return getBooleanProperty(32768);
    }

    public void setMultiLine(boolean multiLine) {
        setBooleanProperty(32768, multiLine);
    }

    public boolean canOpenPopup() {
        return getBooleanProperty(8192);
    }

    public void setCanOpenPopup(boolean opensPopup) {
        enforceNotSealed();
        setBooleanProperty(8192, opensPopup);
    }

    public boolean isDismissable() {
        return getBooleanProperty(16384);
    }

    public void setDismissable(boolean dismissable) {
        setBooleanProperty(16384, dismissable);
    }

    public boolean isImportantForAccessibility() {
        return getBooleanProperty(262144);
    }

    public void setImportantForAccessibility(boolean important) {
        setBooleanProperty(262144, important);
    }

    public boolean isShowingHintText() {
        return getBooleanProperty(1048576);
    }

    public void setShowingHintText(boolean showingHintText) {
        setBooleanProperty(1048576, showingHintText);
    }

    public CharSequence getPackageName() {
        return this.mPackageName;
    }

    public void setPackageName(CharSequence packageName) {
        enforceNotSealed();
        this.mPackageName = packageName;
    }

    public CharSequence getClassName() {
        return this.mClassName;
    }

    public void setClassName(CharSequence className) {
        enforceNotSealed();
        this.mClassName = className;
    }

    public CharSequence getText() {
        if (this.mText instanceof Spanned) {
            Spanned spanned = this.mText;
            AccessibilityClickableSpan[] clickableSpans = (AccessibilityClickableSpan[]) spanned.getSpans(0, this.mText.length(), AccessibilityClickableSpan.class);
            for (AccessibilityClickableSpan copyConnectionDataFrom : clickableSpans) {
                copyConnectionDataFrom.copyConnectionDataFrom(this);
            }
            AccessibilityURLSpan[] urlSpans = (AccessibilityURLSpan[]) spanned.getSpans(0, this.mText.length(), AccessibilityURLSpan.class);
            for (AccessibilityURLSpan copyConnectionDataFrom2 : urlSpans) {
                copyConnectionDataFrom2.copyConnectionDataFrom(this);
            }
        }
        return this.mText;
    }

    public CharSequence getOriginalText() {
        return this.mOriginalText;
    }

    public void setText(CharSequence text) {
        enforceNotSealed();
        this.mOriginalText = text;
        if (text instanceof Spanned) {
            ClickableSpan[] spans = (ClickableSpan[]) ((Spanned) text).getSpans(0, text.length(), ClickableSpan.class);
            if (spans.length > 0) {
                Spannable spannable = new SpannableStringBuilder(text);
                for (ClickableSpan span : spans) {
                    if ((span instanceof AccessibilityClickableSpan) || (span instanceof AccessibilityURLSpan)) {
                        break;
                    }
                    ClickableSpan replacementSpan;
                    int spanToReplaceStart = spannable.getSpanStart(span);
                    int spanToReplaceEnd = spannable.getSpanEnd(span);
                    int spanToReplaceFlags = spannable.getSpanFlags(span);
                    spannable.removeSpan(span);
                    if (span instanceof URLSpan) {
                        replacementSpan = new AccessibilityURLSpan((URLSpan) span);
                    } else {
                        replacementSpan = new AccessibilityClickableSpan(span.getId());
                    }
                    spannable.setSpan(replacementSpan, spanToReplaceStart, spanToReplaceEnd, spanToReplaceFlags);
                }
                this.mText = spannable;
                return;
            }
        }
        this.mText = text == null ? null : text.subSequence(0, text.length());
    }

    public CharSequence getHintText() {
        return this.mHintText;
    }

    public void setHintText(CharSequence hintText) {
        CharSequence charSequence = null;
        enforceNotSealed();
        if (hintText != null) {
            charSequence = hintText.subSequence(0, hintText.length());
        }
        this.mHintText = charSequence;
    }

    public void setError(CharSequence error) {
        CharSequence charSequence = null;
        enforceNotSealed();
        if (error != null) {
            charSequence = error.subSequence(0, error.length());
        }
        this.mError = charSequence;
    }

    public CharSequence getError() {
        return this.mError;
    }

    public CharSequence getContentDescription() {
        return this.mContentDescription;
    }

    public void setContentDescription(CharSequence contentDescription) {
        CharSequence charSequence = null;
        enforceNotSealed();
        if (contentDescription != null) {
            charSequence = contentDescription.subSequence(0, contentDescription.length());
        }
        this.mContentDescription = charSequence;
    }

    public void setLabelFor(View labeled) {
        setLabelFor(labeled, -1);
    }

    public void setLabelFor(View root, int virtualDescendantId) {
        enforceNotSealed();
        this.mLabelForId = makeNodeId(root != null ? root.getAccessibilityViewId() : Integer.MAX_VALUE, virtualDescendantId);
    }

    public AccessibilityNodeInfo getLabelFor() {
        enforceSealed();
        return getNodeForAccessibilityId(this.mLabelForId);
    }

    public void setLabeledBy(View label) {
        setLabeledBy(label, -1);
    }

    public void setLabeledBy(View root, int virtualDescendantId) {
        enforceNotSealed();
        this.mLabeledById = makeNodeId(root != null ? root.getAccessibilityViewId() : Integer.MAX_VALUE, virtualDescendantId);
    }

    public AccessibilityNodeInfo getLabeledBy() {
        enforceSealed();
        return getNodeForAccessibilityId(this.mLabeledById);
    }

    public void setViewIdResourceName(String viewIdResName) {
        enforceNotSealed();
        this.mViewIdResourceName = viewIdResName;
    }

    public String getViewIdResourceName() {
        return this.mViewIdResourceName;
    }

    public int getTextSelectionStart() {
        return this.mTextSelectionStart;
    }

    public int getTextSelectionEnd() {
        return this.mTextSelectionEnd;
    }

    public void setTextSelection(int start, int end) {
        enforceNotSealed();
        this.mTextSelectionStart = start;
        this.mTextSelectionEnd = end;
    }

    public int getInputType() {
        return this.mInputType;
    }

    public void setInputType(int inputType) {
        enforceNotSealed();
        this.mInputType = inputType;
    }

    public Bundle getExtras() {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        return this.mExtras;
    }

    public boolean hasExtras() {
        return this.mExtras != null;
    }

    private boolean getBooleanProperty(int property) {
        return (this.mBooleanProperties & property) != 0;
    }

    private void setBooleanProperty(int property, boolean value) {
        enforceNotSealed();
        if (value) {
            this.mBooleanProperties |= property;
        } else {
            this.mBooleanProperties &= ~property;
        }
    }

    public void setConnectionId(int connectionId) {
        enforceNotSealed();
        this.mConnectionId = connectionId;
    }

    public int getConnectionId() {
        return this.mConnectionId;
    }

    public int describeContents() {
        return 0;
    }

    public void setSourceNodeId(long sourceId, int windowId) {
        enforceNotSealed();
        this.mSourceNodeId = sourceId;
        this.mWindowId = windowId;
    }

    public long getSourceNodeId() {
        return this.mSourceNodeId;
    }

    public void setSealed(boolean sealed) {
        this.mSealed = sealed;
    }

    public boolean isSealed() {
        return this.mSealed;
    }

    protected void enforceSealed() {
        if (!isSealed()) {
            throw new IllegalStateException("Cannot perform this action on a not sealed instance.");
        }
    }

    private void enforceValidFocusDirection(int direction) {
        switch (direction) {
            case 1:
            case 2:
            case 17:
            case 33:
            case 66:
            case 130:
                return;
            default:
                throw new IllegalArgumentException("Unknown direction: " + direction);
        }
    }

    private void enforceValidFocusType(int focusType) {
        switch (focusType) {
            case 1:
            case 2:
                return;
            default:
                throw new IllegalArgumentException("Unknown focus type: " + focusType);
        }
    }

    protected void enforceNotSealed() {
        if (isSealed()) {
            throw new IllegalStateException("Cannot perform this action on a sealed instance.");
        }
    }

    public static AccessibilityNodeInfo obtain(View source) {
        AccessibilityNodeInfo info = obtain();
        info.setSource(source);
        return info;
    }

    public static AccessibilityNodeInfo obtain(View root, int virtualDescendantId) {
        AccessibilityNodeInfo info = obtain();
        info.setSource(root, virtualDescendantId);
        return info;
    }

    public static AccessibilityNodeInfo obtain() {
        AccessibilityNodeInfo info = (AccessibilityNodeInfo) sPool.acquire();
        if (sNumInstancesInUse != null) {
            sNumInstancesInUse.incrementAndGet();
        }
        return info != null ? info : new AccessibilityNodeInfo();
    }

    public static AccessibilityNodeInfo obtain(AccessibilityNodeInfo info) {
        AccessibilityNodeInfo infoClone = obtain();
        infoClone.init(info);
        return infoClone;
    }

    public void recycle() {
        clear();
        sPool.release(this);
        if (sNumInstancesInUse != null) {
            sNumInstancesInUse.decrementAndGet();
        }
    }

    public static void setNumInstancesInUseCounter(AtomicInteger counter) {
        sNumInstancesInUse = counter;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        int i;
        int i2;
        int i3 = 1;
        if (isSealed()) {
            i = 1;
        } else {
            i = 0;
        }
        parcel.writeInt(i);
        parcel.writeLong(this.mSourceNodeId);
        parcel.writeInt(this.mWindowId);
        parcel.writeLong(this.mParentNodeId);
        parcel.writeLong(this.mLabelForId);
        parcel.writeLong(this.mLabeledById);
        parcel.writeLong(this.mTraversalBefore);
        parcel.writeLong(this.mTraversalAfter);
        parcel.writeInt(this.mConnectionId);
        LongArray childIds = this.mChildNodeIds;
        if (childIds == null) {
            parcel.writeInt(0);
        } else {
            int childIdsSize = childIds.size();
            parcel.writeInt(childIdsSize);
            for (i2 = 0; i2 < childIdsSize; i2++) {
                parcel.writeLong(childIds.get(i2));
            }
        }
        parcel.writeInt(this.mBoundsInParent.top);
        parcel.writeInt(this.mBoundsInParent.bottom);
        parcel.writeInt(this.mBoundsInParent.left);
        parcel.writeInt(this.mBoundsInParent.right);
        parcel.writeInt(this.mBoundsInScreen.top);
        parcel.writeInt(this.mBoundsInScreen.bottom);
        parcel.writeInt(this.mBoundsInScreen.left);
        parcel.writeInt(this.mBoundsInScreen.right);
        if (this.mActions == null || (this.mActions.isEmpty() ^ 1) == 0) {
            parcel.writeInt(0);
            parcel.writeInt(0);
        } else {
            AccessibilityAction action;
            int actionCount = this.mActions.size();
            int nonLegacyActionCount = 0;
            int defaultLegacyStandardActions = 0;
            for (i2 = 0; i2 < actionCount; i2++) {
                action = (AccessibilityAction) this.mActions.get(i2);
                if (isDefaultLegacyStandardAction(action)) {
                    defaultLegacyStandardActions |= action.getId();
                } else {
                    nonLegacyActionCount++;
                }
            }
            parcel.writeInt(defaultLegacyStandardActions);
            parcel.writeInt(nonLegacyActionCount);
            for (i2 = 0; i2 < actionCount; i2++) {
                action = (AccessibilityAction) this.mActions.get(i2);
                if (!isDefaultLegacyStandardAction(action)) {
                    parcel.writeInt(action.getId());
                    parcel.writeCharSequence(action.getLabel());
                }
            }
        }
        parcel.writeInt(this.mMaxTextLength);
        parcel.writeInt(this.mMovementGranularities);
        parcel.writeInt(this.mBooleanProperties);
        parcel.writeCharSequence(this.mPackageName);
        parcel.writeCharSequence(this.mClassName);
        parcel.writeCharSequence(this.mText);
        parcel.writeCharSequence(this.mHintText);
        parcel.writeCharSequence(this.mError);
        parcel.writeCharSequence(this.mContentDescription);
        parcel.writeString(this.mViewIdResourceName);
        parcel.writeInt(this.mTextSelectionStart);
        parcel.writeInt(this.mTextSelectionEnd);
        parcel.writeInt(this.mInputType);
        parcel.writeInt(this.mLiveRegion);
        parcel.writeInt(this.mDrawingOrderInParent);
        if (this.mExtraDataKeys != null) {
            parcel.writeInt(1);
            parcel.writeStringList(this.mExtraDataKeys);
        } else {
            parcel.writeInt(0);
        }
        if (this.mExtras != null) {
            parcel.writeInt(1);
            parcel.writeBundle(this.mExtras);
        } else {
            parcel.writeInt(0);
        }
        if (this.mRangeInfo != null) {
            parcel.writeInt(1);
            parcel.writeInt(this.mRangeInfo.getType());
            parcel.writeFloat(this.mRangeInfo.getMin());
            parcel.writeFloat(this.mRangeInfo.getMax());
            parcel.writeFloat(this.mRangeInfo.getCurrent());
        } else {
            parcel.writeInt(0);
        }
        if (this.mCollectionInfo != null) {
            parcel.writeInt(1);
            parcel.writeInt(this.mCollectionInfo.getRowCount());
            parcel.writeInt(this.mCollectionInfo.getColumnCount());
            if (this.mCollectionInfo.isHierarchical()) {
                i = 1;
            } else {
                i = 0;
            }
            parcel.writeInt(i);
            parcel.writeInt(this.mCollectionInfo.getSelectionMode());
        } else {
            parcel.writeInt(0);
        }
        if (this.mCollectionItemInfo != null) {
            parcel.writeInt(1);
            parcel.writeInt(this.mCollectionItemInfo.getRowIndex());
            parcel.writeInt(this.mCollectionItemInfo.getRowSpan());
            parcel.writeInt(this.mCollectionItemInfo.getColumnIndex());
            parcel.writeInt(this.mCollectionItemInfo.getColumnSpan());
            if (this.mCollectionItemInfo.isHeading()) {
                i = 1;
            } else {
                i = 0;
            }
            parcel.writeInt(i);
            if (!this.mCollectionItemInfo.isSelected()) {
                i3 = 0;
            }
            parcel.writeInt(i3);
        } else {
            parcel.writeInt(0);
        }
        recycle();
    }

    private void init(AccessibilityNodeInfo other) {
        RangeInfo obtain;
        CollectionInfo obtain2;
        CollectionItemInfo collectionItemInfo = null;
        this.mSealed = other.mSealed;
        this.mSourceNodeId = other.mSourceNodeId;
        this.mParentNodeId = other.mParentNodeId;
        this.mLabelForId = other.mLabelForId;
        this.mLabeledById = other.mLabeledById;
        this.mTraversalBefore = other.mTraversalBefore;
        this.mTraversalAfter = other.mTraversalAfter;
        this.mWindowId = other.mWindowId;
        this.mConnectionId = other.mConnectionId;
        this.mBoundsInParent.set(other.mBoundsInParent);
        this.mBoundsInScreen.set(other.mBoundsInScreen);
        this.mPackageName = other.mPackageName;
        this.mClassName = other.mClassName;
        this.mText = other.mText;
        this.mHintText = other.mHintText;
        this.mError = other.mError;
        this.mContentDescription = other.mContentDescription;
        this.mViewIdResourceName = other.mViewIdResourceName;
        ArrayList<AccessibilityAction> otherActions = other.mActions;
        if (otherActions != null && otherActions.size() > 0) {
            if (this.mActions == null) {
                this.mActions = new ArrayList(otherActions);
            } else {
                this.mActions.clear();
                this.mActions.addAll(other.mActions);
            }
        }
        this.mBooleanProperties = other.mBooleanProperties;
        this.mMaxTextLength = other.mMaxTextLength;
        this.mMovementGranularities = other.mMovementGranularities;
        LongArray otherChildNodeIds = other.mChildNodeIds;
        if (otherChildNodeIds != null && otherChildNodeIds.size() > 0) {
            if (this.mChildNodeIds == null) {
                this.mChildNodeIds = otherChildNodeIds.clone();
            } else {
                this.mChildNodeIds.clear();
                this.mChildNodeIds.addAll(otherChildNodeIds);
            }
        }
        this.mTextSelectionStart = other.mTextSelectionStart;
        this.mTextSelectionEnd = other.mTextSelectionEnd;
        this.mInputType = other.mInputType;
        this.mLiveRegion = other.mLiveRegion;
        this.mDrawingOrderInParent = other.mDrawingOrderInParent;
        this.mExtraDataKeys = other.mExtraDataKeys;
        if (other.mExtras != null) {
            this.mExtras = new Bundle(other.mExtras);
        } else {
            this.mExtras = null;
        }
        if (other.mRangeInfo != null) {
            obtain = RangeInfo.obtain(other.mRangeInfo);
        } else {
            obtain = null;
        }
        this.mRangeInfo = obtain;
        if (other.mCollectionInfo != null) {
            obtain2 = CollectionInfo.obtain(other.mCollectionInfo);
        } else {
            obtain2 = null;
        }
        this.mCollectionInfo = obtain2;
        if (other.mCollectionItemInfo != null) {
            collectionItemInfo = CollectionItemInfo.obtain(other.mCollectionItemInfo);
        }
        this.mCollectionItemInfo = collectionItemInfo;
    }

    private void initFromParcel(Parcel parcel) {
        int i;
        boolean sealed = parcel.readInt() == 1;
        this.mSourceNodeId = parcel.readLong();
        this.mWindowId = parcel.readInt();
        this.mParentNodeId = parcel.readLong();
        this.mLabelForId = parcel.readLong();
        this.mLabeledById = parcel.readLong();
        this.mTraversalBefore = parcel.readLong();
        this.mTraversalAfter = parcel.readLong();
        this.mConnectionId = parcel.readInt();
        int childrenSize = parcel.readInt();
        if (childrenSize <= 0) {
            this.mChildNodeIds = null;
        } else {
            this.mChildNodeIds = new LongArray(childrenSize);
            for (i = 0; i < childrenSize; i++) {
                this.mChildNodeIds.add(parcel.readLong());
            }
        }
        this.mBoundsInParent.top = parcel.readInt();
        this.mBoundsInParent.bottom = parcel.readInt();
        this.mBoundsInParent.left = parcel.readInt();
        this.mBoundsInParent.right = parcel.readInt();
        this.mBoundsInScreen.top = parcel.readInt();
        this.mBoundsInScreen.bottom = parcel.readInt();
        this.mBoundsInScreen.left = parcel.readInt();
        this.mBoundsInScreen.right = parcel.readInt();
        addLegacyStandardActions(parcel.readInt());
        int nonLegacyActionCount = parcel.readInt();
        for (i = 0; i < nonLegacyActionCount; i++) {
            addActionUnchecked(new AccessibilityAction(parcel.readInt(), parcel.readCharSequence()));
        }
        this.mMaxTextLength = parcel.readInt();
        this.mMovementGranularities = parcel.readInt();
        this.mBooleanProperties = parcel.readInt();
        this.mPackageName = parcel.readCharSequence();
        this.mClassName = parcel.readCharSequence();
        this.mText = parcel.readCharSequence();
        this.mHintText = parcel.readCharSequence();
        this.mError = parcel.readCharSequence();
        this.mContentDescription = parcel.readCharSequence();
        this.mViewIdResourceName = parcel.readString();
        this.mTextSelectionStart = parcel.readInt();
        this.mTextSelectionEnd = parcel.readInt();
        this.mInputType = parcel.readInt();
        this.mLiveRegion = parcel.readInt();
        this.mDrawingOrderInParent = parcel.readInt();
        if (parcel.readInt() == 1) {
            this.mExtraDataKeys = parcel.createStringArrayList();
        } else {
            this.mExtraDataKeys = null;
        }
        if (parcel.readInt() == 1) {
            this.mExtras = parcel.readBundle();
        } else {
            this.mExtras = null;
        }
        if (parcel.readInt() == 1) {
            this.mRangeInfo = RangeInfo.obtain(parcel.readInt(), parcel.readFloat(), parcel.readFloat(), parcel.readFloat());
        }
        if (parcel.readInt() == 1) {
            this.mCollectionInfo = CollectionInfo.obtain(parcel.readInt(), parcel.readInt(), parcel.readInt() == 1, parcel.readInt());
        }
        if (parcel.readInt() == 1) {
            boolean z;
            int readInt = parcel.readInt();
            int readInt2 = parcel.readInt();
            int readInt3 = parcel.readInt();
            int readInt4 = parcel.readInt();
            boolean z2 = parcel.readInt() == 1;
            if (parcel.readInt() == 1) {
                z = true;
            } else {
                z = false;
            }
            this.mCollectionItemInfo = CollectionItemInfo.obtain(readInt, readInt2, readInt3, readInt4, z2, z);
        }
        this.mSealed = sealed;
    }

    private void clear() {
        this.mSealed = false;
        this.mSourceNodeId = UNDEFINED_NODE_ID;
        this.mParentNodeId = UNDEFINED_NODE_ID;
        this.mLabelForId = UNDEFINED_NODE_ID;
        this.mLabeledById = UNDEFINED_NODE_ID;
        this.mTraversalBefore = UNDEFINED_NODE_ID;
        this.mTraversalAfter = UNDEFINED_NODE_ID;
        this.mWindowId = -1;
        this.mConnectionId = -1;
        this.mMaxTextLength = -1;
        this.mMovementGranularities = 0;
        if (this.mChildNodeIds != null) {
            this.mChildNodeIds.clear();
        }
        this.mBoundsInParent.set(0, 0, 0, 0);
        this.mBoundsInScreen.set(0, 0, 0, 0);
        this.mBooleanProperties = 0;
        this.mDrawingOrderInParent = 0;
        this.mExtraDataKeys = null;
        this.mPackageName = null;
        this.mClassName = null;
        this.mText = null;
        this.mHintText = null;
        this.mError = null;
        this.mContentDescription = null;
        this.mViewIdResourceName = null;
        removeAllActions();
        this.mTextSelectionStart = -1;
        this.mTextSelectionEnd = -1;
        this.mInputType = 0;
        this.mLiveRegion = 0;
        this.mExtras = null;
        if (this.mRangeInfo != null) {
            this.mRangeInfo.recycle();
            this.mRangeInfo = null;
        }
        if (this.mCollectionInfo != null) {
            this.mCollectionInfo.recycle();
            this.mCollectionInfo = null;
        }
        if (this.mCollectionItemInfo != null) {
            this.mCollectionItemInfo.recycle();
            this.mCollectionItemInfo = null;
        }
    }

    private static boolean isDefaultLegacyStandardAction(AccessibilityAction action) {
        if (action.getId() <= 2097152) {
            return TextUtils.isEmpty(action.getLabel());
        }
        return false;
    }

    private static AccessibilityAction getActionSingleton(int actionId) {
        int actions = AccessibilityAction.sStandardActions.size();
        for (int i = 0; i < actions; i++) {
            AccessibilityAction currentAction = (AccessibilityAction) AccessibilityAction.sStandardActions.valueAt(i);
            if (actionId == currentAction.getId()) {
                return currentAction;
            }
        }
        return null;
    }

    private void addLegacyStandardActions(int actionMask) {
        int remainingIds = actionMask;
        while (remainingIds > 0) {
            int id = 1 << Integer.numberOfTrailingZeros(remainingIds);
            remainingIds &= ~id;
            addAction(getActionSingleton(id));
        }
    }

    private static String getActionSymbolicName(int action) {
        switch (action) {
            case 1:
                return "ACTION_FOCUS";
            case 2:
                return "ACTION_CLEAR_FOCUS";
            case 4:
                return "ACTION_SELECT";
            case 8:
                return "ACTION_CLEAR_SELECTION";
            case 16:
                return "ACTION_CLICK";
            case 32:
                return "ACTION_LONG_CLICK";
            case 64:
                return "ACTION_ACCESSIBILITY_FOCUS";
            case 128:
                return "ACTION_CLEAR_ACCESSIBILITY_FOCUS";
            case 256:
                return "ACTION_NEXT_AT_MOVEMENT_GRANULARITY";
            case 512:
                return "ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY";
            case 1024:
                return "ACTION_NEXT_HTML_ELEMENT";
            case 2048:
                return "ACTION_PREVIOUS_HTML_ELEMENT";
            case 4096:
                return "ACTION_SCROLL_FORWARD";
            case 8192:
                return "ACTION_SCROLL_BACKWARD";
            case 16384:
                return "ACTION_COPY";
            case 32768:
                return "ACTION_PASTE";
            case 65536:
                return "ACTION_CUT";
            case 131072:
                return "ACTION_SET_SELECTION";
            case 262144:
                return "ACTION_EXPAND";
            case 524288:
                return "ACTION_COLLAPSE";
            case 1048576:
                return "ACTION_DISMISS";
            case 2097152:
                return "ACTION_SET_TEXT";
            case R.id.accessibilityActionShowOnScreen /*16908342*/:
                return "ACTION_SHOW_ON_SCREEN";
            case R.id.accessibilityActionScrollToPosition /*16908343*/:
                return "ACTION_SCROLL_TO_POSITION";
            case R.id.accessibilityActionScrollUp /*16908344*/:
                return "ACTION_SCROLL_UP";
            case R.id.accessibilityActionScrollLeft /*16908345*/:
                return "ACTION_SCROLL_LEFT";
            case R.id.accessibilityActionScrollDown /*16908346*/:
                return "ACTION_SCROLL_DOWN";
            case R.id.accessibilityActionScrollRight /*16908347*/:
                return "ACTION_SCROLL_RIGHT";
            case R.id.accessibilityActionContextClick /*16908348*/:
                return "ACTION_CONTEXT_CLICK";
            case R.id.accessibilityActionSetProgress /*16908349*/:
                return "ACTION_SET_PROGRESS";
            default:
                return "ACTION_UNKNOWN";
        }
    }

    private static String getMovementGranularitySymbolicName(int granularity) {
        switch (granularity) {
            case 1:
                return "MOVEMENT_GRANULARITY_CHARACTER";
            case 2:
                return "MOVEMENT_GRANULARITY_WORD";
            case 4:
                return "MOVEMENT_GRANULARITY_LINE";
            case 8:
                return "MOVEMENT_GRANULARITY_PARAGRAPH";
            case 16:
                return "MOVEMENT_GRANULARITY_PAGE";
            default:
                throw new IllegalArgumentException("Unknown movement granularity: " + granularity);
        }
    }

    private boolean canPerformRequestOverConnection(long accessibilityNodeId) {
        if (this.mWindowId == -1 || getAccessibilityViewId(accessibilityNodeId) == Integer.MAX_VALUE || this.mConnectionId == -1) {
            return false;
        }
        return true;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        AccessibilityNodeInfo other = (AccessibilityNodeInfo) object;
        return this.mSourceNodeId == other.mSourceNodeId && this.mWindowId == other.mWindowId;
    }

    public int hashCode() {
        return ((((getAccessibilityViewId(this.mSourceNodeId) + 31) * 31) + getVirtualDescendantId(this.mSourceNodeId)) * 31) + this.mWindowId;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append("; boundsInParent: ").append(this.mBoundsInParent);
        builder.append("; boundsInScreen: ").append(this.mBoundsInScreen);
        builder.append("; packageName: ").append(this.mPackageName);
        builder.append("; className: ").append(this.mClassName);
        builder.append("; text: ").append(this.mText);
        builder.append("; error: ").append(this.mError);
        builder.append("; maxTextLength: ").append(this.mMaxTextLength);
        builder.append("; contentDescription: ").append(this.mContentDescription);
        builder.append("; viewIdResName: ").append(this.mViewIdResourceName);
        builder.append("; checkable: ").append(isCheckable());
        builder.append("; checked: ").append(isChecked());
        builder.append("; focusable: ").append(isFocusable());
        builder.append("; focused: ").append(isFocused());
        builder.append("; selected: ").append(isSelected());
        builder.append("; clickable: ").append(isClickable());
        builder.append("; longClickable: ").append(isLongClickable());
        builder.append("; contextClickable: ").append(isContextClickable());
        builder.append("; enabled: ").append(isEnabled());
        builder.append("; password: ").append(isPassword());
        builder.append("; scrollable: ").append(isScrollable());
        builder.append("; importantForAccessibility: ").append(isImportantForAccessibility());
        builder.append("; actions: ").append(this.mActions);
        return builder.toString();
    }

    private AccessibilityNodeInfo getNodeForAccessibilityId(long accessibilityId) {
        if (canPerformRequestOverConnection(accessibilityId)) {
            return AccessibilityInteractionClient.getInstance().findAccessibilityNodeInfoByAccessibilityId(this.mConnectionId, this.mWindowId, accessibilityId, false, 7, null);
        }
        return null;
    }
}
