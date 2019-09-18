package android.view.accessibility;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.SettingsStringUtil;
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
import android.util.Pools;
import android.view.View;
import com.android.internal.util.BitUtils;
import com.android.internal.util.CollectionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
    private static final int BOOLEAN_PROPERTY_IS_HEADING = 2097152;
    private static final int BOOLEAN_PROPERTY_IS_SHOWING_HINT = 1048576;
    private static final int BOOLEAN_PROPERTY_LONG_CLICKABLE = 64;
    private static final int BOOLEAN_PROPERTY_MULTI_LINE = 32768;
    private static final int BOOLEAN_PROPERTY_OPENS_POPUP = 8192;
    private static final int BOOLEAN_PROPERTY_PASSWORD = 256;
    private static final int BOOLEAN_PROPERTY_SCREEN_READER_FOCUSABLE = 524288;
    private static final int BOOLEAN_PROPERTY_SCROLLABLE = 512;
    private static final int BOOLEAN_PROPERTY_SELECTED = 16;
    private static final int BOOLEAN_PROPERTY_VISIBLE_TO_USER = 2048;
    public static final Parcelable.Creator<AccessibilityNodeInfo> CREATOR = new Parcelable.Creator<AccessibilityNodeInfo>() {
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
    private static final AccessibilityNodeInfo DEFAULT = new AccessibilityNodeInfo();
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
    public static final int LAST_LEGACY_STANDARD_ACTION = 2097152;
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
    private static final Pools.SynchronizedPool<AccessibilityNodeInfo> sPool = new Pools.SynchronizedPool<>(50);
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
    private CharSequence mPaneTitle;
    private long mParentNodeId = UNDEFINED_NODE_ID;
    private RangeInfo mRangeInfo;
    private boolean mSealed;
    private long mSourceNodeId = UNDEFINED_NODE_ID;
    private CharSequence mText;
    private int mTextSelectionEnd = -1;
    private int mTextSelectionStart = -1;
    private CharSequence mTooltipText;
    private long mTraversalAfter = UNDEFINED_NODE_ID;
    private long mTraversalBefore = UNDEFINED_NODE_ID;
    private String mViewIdResourceName;
    private int mWindowId = -1;

    public static final class AccessibilityAction {
        public static final AccessibilityAction ACTION_ACCESSIBILITY_FOCUS = new AccessibilityAction(64);
        public static final AccessibilityAction ACTION_CLEAR_ACCESSIBILITY_FOCUS = new AccessibilityAction(128);
        public static final AccessibilityAction ACTION_CLEAR_FOCUS = new AccessibilityAction(2);
        public static final AccessibilityAction ACTION_CLEAR_SELECTION = new AccessibilityAction(8);
        public static final AccessibilityAction ACTION_CLICK = new AccessibilityAction(16);
        public static final AccessibilityAction ACTION_COLLAPSE = new AccessibilityAction(524288);
        public static final AccessibilityAction ACTION_CONTEXT_CLICK = new AccessibilityAction(16908348);
        public static final AccessibilityAction ACTION_COPY = new AccessibilityAction(16384);
        public static final AccessibilityAction ACTION_CUT = new AccessibilityAction(65536);
        public static final AccessibilityAction ACTION_DISMISS = new AccessibilityAction(1048576);
        public static final AccessibilityAction ACTION_EXPAND = new AccessibilityAction(262144);
        public static final AccessibilityAction ACTION_FOCUS = new AccessibilityAction(1);
        public static final AccessibilityAction ACTION_HIDE_TOOLTIP = new AccessibilityAction(16908357);
        public static final AccessibilityAction ACTION_LONG_CLICK = new AccessibilityAction(32);
        public static final AccessibilityAction ACTION_MOVE_WINDOW = new AccessibilityAction(16908354);
        public static final AccessibilityAction ACTION_NEXT_AT_MOVEMENT_GRANULARITY = new AccessibilityAction(256);
        public static final AccessibilityAction ACTION_NEXT_HTML_ELEMENT = new AccessibilityAction(1024);
        public static final AccessibilityAction ACTION_PASTE = new AccessibilityAction(32768);
        public static final AccessibilityAction ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY = new AccessibilityAction(512);
        public static final AccessibilityAction ACTION_PREVIOUS_HTML_ELEMENT = new AccessibilityAction(2048);
        public static final AccessibilityAction ACTION_SCROLL_BACKWARD = new AccessibilityAction(8192);
        public static final AccessibilityAction ACTION_SCROLL_DOWN = new AccessibilityAction(16908346);
        public static final AccessibilityAction ACTION_SCROLL_FORWARD = new AccessibilityAction(4096);
        public static final AccessibilityAction ACTION_SCROLL_LEFT = new AccessibilityAction(16908345);
        public static final AccessibilityAction ACTION_SCROLL_RIGHT = new AccessibilityAction(16908347);
        public static final AccessibilityAction ACTION_SCROLL_TO_POSITION = new AccessibilityAction(16908343);
        public static final AccessibilityAction ACTION_SCROLL_UP = new AccessibilityAction(16908344);
        public static final AccessibilityAction ACTION_SELECT = new AccessibilityAction(4);
        public static final AccessibilityAction ACTION_SET_PROGRESS = new AccessibilityAction(16908349);
        public static final AccessibilityAction ACTION_SET_SELECTION = new AccessibilityAction(131072);
        public static final AccessibilityAction ACTION_SET_TEXT = new AccessibilityAction(2097152);
        public static final AccessibilityAction ACTION_SHOW_ON_SCREEN = new AccessibilityAction(16908342);
        public static final AccessibilityAction ACTION_SHOW_TOOLTIP = new AccessibilityAction(16908356);
        public static final ArraySet<AccessibilityAction> sStandardActions = new ArraySet<>();
        private final int mActionId;
        private final CharSequence mLabel;
        public long mSerializationFlag;

        public AccessibilityAction(int actionId, CharSequence label) {
            this.mSerializationFlag = -1;
            if ((-16777216 & actionId) != 0 || Integer.bitCount(actionId) == 1) {
                this.mActionId = actionId;
                this.mLabel = label;
                return;
            }
            throw new IllegalArgumentException("Invalid standard action id");
        }

        private AccessibilityAction(int standardActionId) {
            this(standardActionId, null);
            this.mSerializationFlag = BitUtils.bitAt(sStandardActions.size());
            sStandardActions.add(this);
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
            boolean z = false;
            if (other == null) {
                return false;
            }
            if (other == this) {
                return true;
            }
            if (getClass() != other.getClass()) {
                return false;
            }
            if (this.mActionId == ((AccessibilityAction) other).mActionId) {
                z = true;
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
        private static final Pools.SynchronizedPool<CollectionInfo> sPool = new Pools.SynchronizedPool<>(20);
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
            CollectionInfo info = sPool.acquire();
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

        /* access modifiers changed from: package-private */
        public void recycle() {
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
        private static final Pools.SynchronizedPool<CollectionItemInfo> sPool = new Pools.SynchronizedPool<>(20);
        private int mColumnIndex;
        private int mColumnSpan;
        /* access modifiers changed from: private */
        public boolean mHeading;
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
            CollectionItemInfo info = sPool.acquire();
            if (info == null) {
                CollectionItemInfo collectionItemInfo = new CollectionItemInfo(rowIndex, rowSpan, columnIndex, columnSpan, heading, selected);
                return collectionItemInfo;
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

        /* access modifiers changed from: package-private */
        public void recycle() {
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
        private static final Pools.SynchronizedPool<RangeInfo> sPool = new Pools.SynchronizedPool<>(10);
        private float mCurrent;
        private float mMax;
        private float mMin;
        private int mType;

        public static RangeInfo obtain(RangeInfo other) {
            return obtain(other.mType, other.mMin, other.mMax, other.mCurrent);
        }

        public static RangeInfo obtain(int type, float min, float max, float current) {
            RangeInfo info = sPool.acquire();
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

        /* access modifiers changed from: package-private */
        public void recycle() {
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
        int rootAccessibilityViewId = Integer.MAX_VALUE;
        this.mWindowId = root != null ? root.getAccessibilityWindowId() : Integer.MAX_VALUE;
        if (root != null) {
            rootAccessibilityViewId = root.getAccessibilityViewId();
        }
        this.mSourceNodeId = makeNodeId(rootAccessibilityViewId, virtualDescendantId);
    }

    public AccessibilityNodeInfo findFocus(int focus) {
        enforceSealed();
        enforceValidFocusType(focus);
        if (!canPerformRequestOverConnection(this.mSourceNodeId)) {
            return null;
        }
        return AccessibilityInteractionClient.getInstance().findFocus(this.mConnectionId, this.mWindowId, this.mSourceNodeId, focus);
    }

    public AccessibilityNodeInfo focusSearch(int direction) {
        enforceSealed();
        enforceValidFocusDirection(direction);
        if (!canPerformRequestOverConnection(this.mSourceNodeId)) {
            return null;
        }
        return AccessibilityInteractionClient.getInstance().focusSearch(this.mConnectionId, this.mWindowId, this.mSourceNodeId, direction);
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
        enforceSealed();
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
        if (this.mChildNodeIds == null) {
            return 0;
        }
        return this.mChildNodeIds.size();
    }

    public AccessibilityNodeInfo getChild(int index) {
        enforceSealed();
        if (this.mChildNodeIds == null || !canPerformRequestOverConnection(this.mSourceNodeId)) {
            return null;
        }
        long childId = this.mChildNodeIds.get(index);
        return AccessibilityInteractionClient.getInstance().findAccessibilityNodeInfoByAccessibilityId(this.mConnectionId, this.mWindowId, childId, false, 4, null);
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
        return CollectionUtils.emptyIfNull(this.mActions);
    }

    @Deprecated
    public int getActions() {
        int returnValue = 0;
        if (this.mActions == null) {
            return 0;
        }
        int actionSize = this.mActions.size();
        for (int i = 0; i < actionSize; i++) {
            int actionId = this.mActions.get(i).getId();
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
                this.mActions = new ArrayList<>();
            }
            this.mActions.remove(action);
            this.mActions.add(action);
        }
    }

    @Deprecated
    public void addAction(int action) {
        enforceNotSealed();
        if ((-16777216 & action) == 0) {
            addStandardActions((long) action);
            return;
        }
        throw new IllegalArgumentException("Action is not a combination of the standard actions: " + action);
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
        this.mExtraDataKeys = new ArrayList<>(extraDataKeys);
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
        if (!canPerformRequestOverConnection(this.mSourceNodeId)) {
            return false;
        }
        return AccessibilityInteractionClient.getInstance().performAccessibilityAction(this.mConnectionId, this.mWindowId, this.mSourceNodeId, action, null);
    }

    public boolean performAction(int action, Bundle arguments) {
        enforceSealed();
        if (!canPerformRequestOverConnection(this.mSourceNodeId)) {
            return false;
        }
        return AccessibilityInteractionClient.getInstance().performAccessibilityAction(this.mConnectionId, this.mWindowId, this.mSourceNodeId, action, arguments);
    }

    public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText(String text) {
        enforceSealed();
        if (!canPerformRequestOverConnection(this.mSourceNodeId)) {
            return Collections.emptyList();
        }
        return AccessibilityInteractionClient.getInstance().findAccessibilityNodeInfosByText(this.mConnectionId, this.mWindowId, this.mSourceNodeId, text);
    }

    public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByViewId(String viewId) {
        enforceSealed();
        if (!canPerformRequestOverConnection(this.mSourceNodeId)) {
            return Collections.emptyList();
        }
        return AccessibilityInteractionClient.getInstance().findAccessibilityNodeInfosByViewId(this.mConnectionId, this.mWindowId, this.mSourceNodeId, viewId);
    }

    public AccessibilityWindowInfo getWindow() {
        enforceSealed();
        if (!canPerformRequestOverConnection(this.mSourceNodeId)) {
            return null;
        }
        return AccessibilityInteractionClient.getInstance().getWindow(this.mConnectionId, this.mWindowId);
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

    public void setPaneTitle(CharSequence paneTitle) {
        enforceNotSealed();
        this.mPaneTitle = paneTitle == null ? null : paneTitle.subSequence(0, paneTitle.length());
    }

    public CharSequence getPaneTitle() {
        return this.mPaneTitle;
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

    public boolean isScreenReaderFocusable() {
        return getBooleanProperty(524288);
    }

    public void setScreenReaderFocusable(boolean screenReaderFocusable) {
        setBooleanProperty(524288, screenReaderFocusable);
    }

    public boolean isShowingHintText() {
        return getBooleanProperty(1048576);
    }

    public void setShowingHintText(boolean showingHintText) {
        setBooleanProperty(1048576, showingHintText);
    }

    public boolean isHeading() {
        boolean z = true;
        if (getBooleanProperty(2097152)) {
            return true;
        }
        CollectionItemInfo itemInfo = getCollectionItemInfo();
        if (itemInfo == null || !itemInfo.mHeading) {
            z = false;
        }
        return z;
    }

    public void setHeading(boolean isHeading) {
        setBooleanProperty(2097152, isHeading);
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
            Spanned spanned = (Spanned) this.mText;
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
        ClickableSpan replacementSpan;
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
        enforceNotSealed();
        this.mHintText = hintText == null ? null : hintText.subSequence(0, hintText.length());
    }

    public void setError(CharSequence error) {
        enforceNotSealed();
        this.mError = error == null ? null : error.subSequence(0, error.length());
    }

    public CharSequence getError() {
        return this.mError;
    }

    public CharSequence getContentDescription() {
        return this.mContentDescription;
    }

    public void setContentDescription(CharSequence contentDescription) {
        CharSequence charSequence;
        enforceNotSealed();
        if (contentDescription == null) {
            charSequence = null;
        } else {
            charSequence = contentDescription.subSequence(0, contentDescription.length());
        }
        this.mContentDescription = charSequence;
    }

    public CharSequence getTooltipText() {
        return this.mTooltipText;
    }

    public void setTooltipText(CharSequence tooltipText) {
        CharSequence charSequence;
        enforceNotSealed();
        if (tooltipText == null) {
            charSequence = null;
        } else {
            charSequence = tooltipText.subSequence(0, tooltipText.length());
        }
        this.mTooltipText = charSequence;
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

    /* access modifiers changed from: protected */
    public void enforceSealed() {
        if (!isSealed()) {
            throw new IllegalStateException("Cannot perform this action on a not sealed instance.");
        }
    }

    private void enforceValidFocusDirection(int direction) {
        if (direction != 17 && direction != 33 && direction != 66 && direction != 130) {
            switch (direction) {
                case 1:
                case 2:
                    return;
                default:
                    throw new IllegalArgumentException("Unknown direction: " + direction);
            }
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

    /* access modifiers changed from: protected */
    public void enforceNotSealed() {
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
        AccessibilityNodeInfo info = sPool.acquire();
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
        writeToParcelNoRecycle(parcel, flags);
        recycle();
    }

    public void writeToParcelNoRecycle(Parcel parcel, int flags) {
        long nonDefaultFields = 0;
        if (isSealed() != DEFAULT.isSealed()) {
            nonDefaultFields = 0 | BitUtils.bitAt(0);
        }
        int fieldIndex = 0 + 1;
        if (this.mSourceNodeId != DEFAULT.mSourceNodeId) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex);
        }
        int fieldIndex2 = fieldIndex + 1;
        if (this.mWindowId != DEFAULT.mWindowId) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex2);
        }
        int fieldIndex3 = fieldIndex2 + 1;
        if (this.mParentNodeId != DEFAULT.mParentNodeId) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex3);
        }
        int fieldIndex4 = fieldIndex3 + 1;
        if (this.mLabelForId != DEFAULT.mLabelForId) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex4);
        }
        int fieldIndex5 = fieldIndex4 + 1;
        if (this.mLabeledById != DEFAULT.mLabeledById) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex5);
        }
        int fieldIndex6 = fieldIndex5 + 1;
        if (this.mTraversalBefore != DEFAULT.mTraversalBefore) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex6);
        }
        int fieldIndex7 = fieldIndex6 + 1;
        if (this.mTraversalAfter != DEFAULT.mTraversalAfter) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex7);
        }
        int fieldIndex8 = fieldIndex7 + 1;
        if (this.mConnectionId != DEFAULT.mConnectionId) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex8);
        }
        int fieldIndex9 = fieldIndex8 + 1;
        if (!LongArray.elementsEqual(this.mChildNodeIds, DEFAULT.mChildNodeIds)) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex9);
        }
        int fieldIndex10 = fieldIndex9 + 1;
        if (!Objects.equals(this.mBoundsInParent, DEFAULT.mBoundsInParent)) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex10);
        }
        int fieldIndex11 = fieldIndex10 + 1;
        if (!Objects.equals(this.mBoundsInScreen, DEFAULT.mBoundsInScreen)) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex11);
        }
        int fieldIndex12 = fieldIndex11 + 1;
        if (!Objects.equals(this.mActions, DEFAULT.mActions)) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex12);
        }
        int fieldIndex13 = fieldIndex12 + 1;
        if (this.mMaxTextLength != DEFAULT.mMaxTextLength) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex13);
        }
        int fieldIndex14 = fieldIndex13 + 1;
        if (this.mMovementGranularities != DEFAULT.mMovementGranularities) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex14);
        }
        int fieldIndex15 = fieldIndex14 + 1;
        if (this.mBooleanProperties != DEFAULT.mBooleanProperties) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex15);
        }
        int fieldIndex16 = fieldIndex15 + 1;
        if (!Objects.equals(this.mPackageName, DEFAULT.mPackageName)) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex16);
        }
        int fieldIndex17 = fieldIndex16 + 1;
        if (!Objects.equals(this.mClassName, DEFAULT.mClassName)) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex17);
        }
        int fieldIndex18 = fieldIndex17 + 1;
        if (!Objects.equals(this.mText, DEFAULT.mText)) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex18);
        }
        int fieldIndex19 = fieldIndex18 + 1;
        if (!Objects.equals(this.mHintText, DEFAULT.mHintText)) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex19);
        }
        int fieldIndex20 = fieldIndex19 + 1;
        if (!Objects.equals(this.mError, DEFAULT.mError)) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex20);
        }
        int fieldIndex21 = fieldIndex20 + 1;
        if (!Objects.equals(this.mContentDescription, DEFAULT.mContentDescription)) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex21);
        }
        int fieldIndex22 = fieldIndex21 + 1;
        if (!Objects.equals(this.mPaneTitle, DEFAULT.mPaneTitle)) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex22);
        }
        int fieldIndex23 = fieldIndex22 + 1;
        if (!Objects.equals(this.mTooltipText, DEFAULT.mTooltipText)) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex23);
        }
        int fieldIndex24 = fieldIndex23 + 1;
        if (!Objects.equals(this.mViewIdResourceName, DEFAULT.mViewIdResourceName)) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex24);
        }
        int fieldIndex25 = fieldIndex24 + 1;
        if (this.mTextSelectionStart != DEFAULT.mTextSelectionStart) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex25);
        }
        int fieldIndex26 = fieldIndex25 + 1;
        if (this.mTextSelectionEnd != DEFAULT.mTextSelectionEnd) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex26);
        }
        int fieldIndex27 = fieldIndex26 + 1;
        if (this.mInputType != DEFAULT.mInputType) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex27);
        }
        int fieldIndex28 = fieldIndex27 + 1;
        if (this.mLiveRegion != DEFAULT.mLiveRegion) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex28);
        }
        int fieldIndex29 = fieldIndex28 + 1;
        if (this.mDrawingOrderInParent != DEFAULT.mDrawingOrderInParent) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex29);
        }
        int fieldIndex30 = fieldIndex29 + 1;
        if (!Objects.equals(this.mExtraDataKeys, DEFAULT.mExtraDataKeys)) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex30);
        }
        int fieldIndex31 = fieldIndex30 + 1;
        if (!Objects.equals(this.mExtras, DEFAULT.mExtras)) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex31);
        }
        int fieldIndex32 = fieldIndex31 + 1;
        if (!Objects.equals(this.mRangeInfo, DEFAULT.mRangeInfo)) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex32);
        }
        int fieldIndex33 = fieldIndex32 + 1;
        if (!Objects.equals(this.mCollectionInfo, DEFAULT.mCollectionInfo)) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex33);
        }
        int fieldIndex34 = fieldIndex33 + 1;
        if (!Objects.equals(this.mCollectionItemInfo, DEFAULT.mCollectionItemInfo)) {
            nonDefaultFields |= BitUtils.bitAt(fieldIndex34);
        }
        int i = fieldIndex34;
        parcel.writeLong(nonDefaultFields);
        int fieldIndex35 = 0 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, 0) != 0) {
            parcel.writeInt(isSealed() ? 1 : 0);
        }
        int fieldIndex36 = fieldIndex35 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex35) != 0) {
            parcel.writeLong(this.mSourceNodeId);
        }
        int fieldIndex37 = fieldIndex36 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex36) != 0) {
            parcel.writeInt(this.mWindowId);
        }
        int fieldIndex38 = fieldIndex37 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex37) != 0) {
            parcel.writeLong(this.mParentNodeId);
        }
        int fieldIndex39 = fieldIndex38 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex38) != 0) {
            parcel.writeLong(this.mLabelForId);
        }
        int fieldIndex40 = fieldIndex39 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex39) != 0) {
            parcel.writeLong(this.mLabeledById);
        }
        int fieldIndex41 = fieldIndex40 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex40) != 0) {
            parcel.writeLong(this.mTraversalBefore);
        }
        int fieldIndex42 = fieldIndex41 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex41) != 0) {
            parcel.writeLong(this.mTraversalAfter);
        }
        int fieldIndex43 = fieldIndex42 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex42) != 0) {
            parcel.writeInt(this.mConnectionId);
        }
        int fieldIndex44 = fieldIndex43 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex43) != 0) {
            LongArray childIds = this.mChildNodeIds;
            if (childIds == null) {
                parcel.writeInt(0);
            } else {
                int childIdsSize = childIds.size();
                parcel.writeInt(childIdsSize);
                for (int i2 = 0; i2 < childIdsSize; i2++) {
                    parcel.writeLong(childIds.get(i2));
                }
            }
        }
        int fieldIndex45 = fieldIndex44 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex44) != 0) {
            parcel.writeInt(this.mBoundsInParent.top);
            parcel.writeInt(this.mBoundsInParent.bottom);
            parcel.writeInt(this.mBoundsInParent.left);
            parcel.writeInt(this.mBoundsInParent.right);
        }
        int fieldIndex46 = fieldIndex45 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex45) != 0) {
            parcel.writeInt(this.mBoundsInScreen.top);
            parcel.writeInt(this.mBoundsInScreen.bottom);
            parcel.writeInt(this.mBoundsInScreen.left);
            parcel.writeInt(this.mBoundsInScreen.right);
        }
        int fieldIndex47 = fieldIndex46 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex46) != 0) {
            if (this.mActions == null || this.mActions.isEmpty()) {
                parcel.writeLong(0);
                parcel.writeInt(0);
            } else {
                int actionCount = this.mActions.size();
                long defaultStandardActions = 0;
                int nonStandardActionCount = 0;
                for (int i3 = 0; i3 < actionCount; i3++) {
                    AccessibilityAction action = this.mActions.get(i3);
                    if (isDefaultStandardAction(action)) {
                        defaultStandardActions |= action.mSerializationFlag;
                    } else {
                        nonStandardActionCount++;
                    }
                }
                parcel.writeLong(defaultStandardActions);
                parcel.writeInt(nonStandardActionCount);
                for (int i4 = 0; i4 < actionCount; i4++) {
                    AccessibilityAction action2 = this.mActions.get(i4);
                    if (!isDefaultStandardAction(action2)) {
                        parcel.writeInt(action2.getId());
                        parcel.writeCharSequence(action2.getLabel());
                    }
                }
            }
        }
        int fieldIndex48 = fieldIndex47 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex47) != 0) {
            parcel.writeInt(this.mMaxTextLength);
        }
        int fieldIndex49 = fieldIndex48 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex48) != 0) {
            parcel.writeInt(this.mMovementGranularities);
        }
        int fieldIndex50 = fieldIndex49 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex49) != 0) {
            parcel.writeInt(this.mBooleanProperties);
        }
        int fieldIndex51 = fieldIndex50 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex50) != 0) {
            parcel.writeCharSequence(this.mPackageName);
        }
        int fieldIndex52 = fieldIndex51 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex51) != 0) {
            parcel.writeCharSequence(this.mClassName);
        }
        int fieldIndex53 = fieldIndex52 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex52) != 0) {
            parcel.writeCharSequence(this.mText);
        }
        int fieldIndex54 = fieldIndex53 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex53) != 0) {
            parcel.writeCharSequence(this.mHintText);
        }
        int fieldIndex55 = fieldIndex54 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex54) != 0) {
            parcel.writeCharSequence(this.mError);
        }
        int fieldIndex56 = fieldIndex55 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex55) != 0) {
            parcel.writeCharSequence(this.mContentDescription);
        }
        int fieldIndex57 = fieldIndex56 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex56) != 0) {
            parcel.writeCharSequence(this.mPaneTitle);
        }
        int fieldIndex58 = fieldIndex57 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex57) != 0) {
            parcel.writeCharSequence(this.mTooltipText);
        }
        int fieldIndex59 = fieldIndex58 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex58) != 0) {
            parcel.writeString(this.mViewIdResourceName);
        }
        int fieldIndex60 = fieldIndex59 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex59) != 0) {
            parcel.writeInt(this.mTextSelectionStart);
        }
        int fieldIndex61 = fieldIndex60 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex60) != 0) {
            parcel.writeInt(this.mTextSelectionEnd);
        }
        int fieldIndex62 = fieldIndex61 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex61) != 0) {
            parcel.writeInt(this.mInputType);
        }
        int fieldIndex63 = fieldIndex62 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex62) != 0) {
            parcel.writeInt(this.mLiveRegion);
        }
        int fieldIndex64 = fieldIndex63 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex63) != 0) {
            parcel.writeInt(this.mDrawingOrderInParent);
        }
        int fieldIndex65 = fieldIndex64 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex64) != 0) {
            parcel.writeStringList(this.mExtraDataKeys);
        }
        int fieldIndex66 = fieldIndex65 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex65) != 0) {
            parcel.writeBundle(this.mExtras);
        }
        int fieldIndex67 = fieldIndex66 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex66) != 0) {
            parcel.writeInt(this.mRangeInfo.getType());
            parcel.writeFloat(this.mRangeInfo.getMin());
            parcel.writeFloat(this.mRangeInfo.getMax());
            parcel.writeFloat(this.mRangeInfo.getCurrent());
        }
        int fieldIndex68 = fieldIndex67 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex67) != 0) {
            parcel.writeInt(this.mCollectionInfo.getRowCount());
            parcel.writeInt(this.mCollectionInfo.getColumnCount());
            parcel.writeInt(this.mCollectionInfo.isHierarchical() ? 1 : 0);
            parcel.writeInt(this.mCollectionInfo.getSelectionMode());
        }
        int i5 = fieldIndex68 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex68) != 0) {
            parcel.writeInt(this.mCollectionItemInfo.getRowIndex());
            parcel.writeInt(this.mCollectionItemInfo.getRowSpan());
            parcel.writeInt(this.mCollectionItemInfo.getColumnIndex());
            parcel.writeInt(this.mCollectionItemInfo.getColumnSpan());
            parcel.writeInt(this.mCollectionItemInfo.isHeading() ? 1 : 0);
            parcel.writeInt(this.mCollectionItemInfo.isSelected() ? 1 : 0);
        }
    }

    private void init(AccessibilityNodeInfo other) {
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
        this.mOriginalText = other.mOriginalText;
        this.mHintText = other.mHintText;
        this.mError = other.mError;
        this.mContentDescription = other.mContentDescription;
        this.mPaneTitle = other.mPaneTitle;
        this.mTooltipText = other.mTooltipText;
        this.mViewIdResourceName = other.mViewIdResourceName;
        if (this.mActions != null) {
            this.mActions.clear();
        }
        ArrayList<AccessibilityAction> otherActions = other.mActions;
        if (otherActions != null && otherActions.size() > 0) {
            if (this.mActions == null) {
                this.mActions = new ArrayList<>(otherActions);
            } else {
                this.mActions.addAll(other.mActions);
            }
        }
        this.mBooleanProperties = other.mBooleanProperties;
        this.mMaxTextLength = other.mMaxTextLength;
        this.mMovementGranularities = other.mMovementGranularities;
        if (this.mChildNodeIds != null) {
            this.mChildNodeIds.clear();
        }
        LongArray otherChildNodeIds = other.mChildNodeIds;
        if (otherChildNodeIds != null && otherChildNodeIds.size() > 0) {
            if (this.mChildNodeIds == null) {
                this.mChildNodeIds = otherChildNodeIds.clone();
            } else {
                this.mChildNodeIds.addAll(otherChildNodeIds);
            }
        }
        this.mTextSelectionStart = other.mTextSelectionStart;
        this.mTextSelectionEnd = other.mTextSelectionEnd;
        this.mInputType = other.mInputType;
        this.mLiveRegion = other.mLiveRegion;
        this.mDrawingOrderInParent = other.mDrawingOrderInParent;
        this.mExtraDataKeys = other.mExtraDataKeys;
        CollectionItemInfo collectionItemInfo = null;
        this.mExtras = other.mExtras != null ? new Bundle(other.mExtras) : null;
        if (this.mRangeInfo != null) {
            this.mRangeInfo.recycle();
        }
        this.mRangeInfo = other.mRangeInfo != null ? RangeInfo.obtain(other.mRangeInfo) : null;
        if (this.mCollectionInfo != null) {
            this.mCollectionInfo.recycle();
        }
        this.mCollectionInfo = other.mCollectionInfo != null ? CollectionInfo.obtain(other.mCollectionInfo) : null;
        if (this.mCollectionItemInfo != null) {
            this.mCollectionItemInfo.recycle();
        }
        if (other.mCollectionItemInfo != null) {
            collectionItemInfo = CollectionItemInfo.obtain(other.mCollectionItemInfo);
        }
        this.mCollectionItemInfo = collectionItemInfo;
    }

    /* access modifiers changed from: private */
    public void initFromParcel(Parcel parcel) {
        ArrayList<String> arrayList;
        Bundle bundle;
        RangeInfo rangeInfo;
        CollectionInfo collectionInfo;
        long nonDefaultFields = parcel.readLong();
        int fieldIndex = 0 + 1;
        boolean sealed = BitUtils.isBitSet(nonDefaultFields, 0) != 0 ? parcel.readInt() == 1 : DEFAULT.mSealed;
        int fieldIndex2 = fieldIndex + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex) != 0) {
            this.mSourceNodeId = parcel.readLong();
        }
        int fieldIndex3 = fieldIndex2 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex2) != 0) {
            this.mWindowId = parcel.readInt();
        }
        int fieldIndex4 = fieldIndex3 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex3) != 0) {
            this.mParentNodeId = parcel.readLong();
        }
        int fieldIndex5 = fieldIndex4 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex4) != 0) {
            this.mLabelForId = parcel.readLong();
        }
        int fieldIndex6 = fieldIndex5 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex5) != 0) {
            this.mLabeledById = parcel.readLong();
        }
        int fieldIndex7 = fieldIndex6 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex6) != 0) {
            this.mTraversalBefore = parcel.readLong();
        }
        int fieldIndex8 = fieldIndex7 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex7) != 0) {
            this.mTraversalAfter = parcel.readLong();
        }
        int fieldIndex9 = fieldIndex8 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex8) != 0) {
            this.mConnectionId = parcel.readInt();
        }
        int fieldIndex10 = fieldIndex9 + 1;
        CollectionItemInfo collectionItemInfo = null;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex9) != 0) {
            int childrenSize = parcel.readInt();
            if (childrenSize <= 0) {
                this.mChildNodeIds = null;
            } else {
                this.mChildNodeIds = new LongArray(childrenSize);
                for (int i = 0; i < childrenSize; i++) {
                    this.mChildNodeIds.add(parcel.readLong());
                }
            }
        }
        int childrenSize2 = fieldIndex10 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex10) != 0) {
            this.mBoundsInParent.top = parcel.readInt();
            this.mBoundsInParent.bottom = parcel.readInt();
            this.mBoundsInParent.left = parcel.readInt();
            this.mBoundsInParent.right = parcel.readInt();
        }
        int fieldIndex11 = childrenSize2 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, childrenSize2) != 0) {
            this.mBoundsInScreen.top = parcel.readInt();
            this.mBoundsInScreen.bottom = parcel.readInt();
            this.mBoundsInScreen.left = parcel.readInt();
            this.mBoundsInScreen.right = parcel.readInt();
        }
        int fieldIndex12 = fieldIndex11 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex11) != 0) {
            addStandardActions(parcel.readLong());
            int nonStandardActionCount = parcel.readInt();
            for (int i2 = 0; i2 < nonStandardActionCount; i2++) {
                addActionUnchecked(new AccessibilityAction(parcel.readInt(), parcel.readCharSequence()));
            }
        }
        int fieldIndex13 = fieldIndex12 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex12) != 0) {
            this.mMaxTextLength = parcel.readInt();
        }
        int fieldIndex14 = fieldIndex13 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex13) != 0) {
            this.mMovementGranularities = parcel.readInt();
        }
        int fieldIndex15 = fieldIndex14 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex14) != 0) {
            this.mBooleanProperties = parcel.readInt();
        }
        int fieldIndex16 = fieldIndex15 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex15) != 0) {
            this.mPackageName = parcel.readCharSequence();
        }
        int fieldIndex17 = fieldIndex16 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex16) != 0) {
            this.mClassName = parcel.readCharSequence();
        }
        int fieldIndex18 = fieldIndex17 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex17) != 0) {
            this.mText = parcel.readCharSequence();
        }
        int fieldIndex19 = fieldIndex18 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex18) != 0) {
            this.mHintText = parcel.readCharSequence();
        }
        int fieldIndex20 = fieldIndex19 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex19) != 0) {
            this.mError = parcel.readCharSequence();
        }
        int fieldIndex21 = fieldIndex20 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex20) != 0) {
            this.mContentDescription = parcel.readCharSequence();
        }
        int fieldIndex22 = fieldIndex21 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex21) != 0) {
            this.mPaneTitle = parcel.readCharSequence();
        }
        int fieldIndex23 = fieldIndex22 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex22) != 0) {
            this.mTooltipText = parcel.readCharSequence();
        }
        int fieldIndex24 = fieldIndex23 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex23) != 0) {
            this.mViewIdResourceName = parcel.readString();
        }
        int fieldIndex25 = fieldIndex24 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex24) != 0) {
            this.mTextSelectionStart = parcel.readInt();
        }
        int fieldIndex26 = fieldIndex25 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex25) != 0) {
            this.mTextSelectionEnd = parcel.readInt();
        }
        int fieldIndex27 = fieldIndex26 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex26) != 0) {
            this.mInputType = parcel.readInt();
        }
        int fieldIndex28 = fieldIndex27 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex27) != 0) {
            this.mLiveRegion = parcel.readInt();
        }
        int fieldIndex29 = fieldIndex28 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex28) != 0) {
            this.mDrawingOrderInParent = parcel.readInt();
        }
        int fieldIndex30 = fieldIndex29 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex29) != 0) {
            arrayList = parcel.createStringArrayList();
        } else {
            arrayList = null;
        }
        this.mExtraDataKeys = arrayList;
        int fieldIndex31 = fieldIndex30 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex30) != 0) {
            bundle = parcel.readBundle();
        } else {
            bundle = null;
        }
        this.mExtras = bundle;
        if (this.mRangeInfo != null) {
            this.mRangeInfo.recycle();
        }
        int fieldIndex32 = fieldIndex31 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex31) != 0) {
            rangeInfo = RangeInfo.obtain(parcel.readInt(), parcel.readFloat(), parcel.readFloat(), parcel.readFloat());
        } else {
            rangeInfo = null;
        }
        this.mRangeInfo = rangeInfo;
        if (this.mCollectionInfo != null) {
            this.mCollectionInfo.recycle();
        }
        int fieldIndex33 = fieldIndex32 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex32) != 0) {
            collectionInfo = CollectionInfo.obtain(parcel.readInt(), parcel.readInt(), parcel.readInt() == 1, parcel.readInt());
        } else {
            collectionInfo = null;
        }
        this.mCollectionInfo = collectionInfo;
        if (this.mCollectionItemInfo != null) {
            this.mCollectionItemInfo.recycle();
        }
        int i3 = fieldIndex33 + 1;
        if (BitUtils.isBitSet(nonDefaultFields, fieldIndex33) != 0) {
            collectionItemInfo = CollectionItemInfo.obtain(parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt() == 1, parcel.readInt() == 1);
        }
        this.mCollectionItemInfo = collectionItemInfo;
        this.mSealed = sealed;
    }

    private void clear() {
        init(DEFAULT);
    }

    private static boolean isDefaultStandardAction(AccessibilityAction action) {
        return action.mSerializationFlag != -1 && TextUtils.isEmpty(action.getLabel());
    }

    private static AccessibilityAction getActionSingleton(int actionId) {
        int actions = AccessibilityAction.sStandardActions.size();
        for (int i = 0; i < actions; i++) {
            AccessibilityAction currentAction = AccessibilityAction.sStandardActions.valueAt(i);
            if (actionId == currentAction.getId()) {
                return currentAction;
            }
        }
        return null;
    }

    private static AccessibilityAction getActionSingletonBySerializationFlag(long flag) {
        int actions = AccessibilityAction.sStandardActions.size();
        for (int i = 0; i < actions; i++) {
            AccessibilityAction currentAction = AccessibilityAction.sStandardActions.valueAt(i);
            if (flag == currentAction.mSerializationFlag) {
                return currentAction;
            }
        }
        return null;
    }

    private void addStandardActions(long serializationIdMask) {
        long remainingIds = serializationIdMask;
        while (remainingIds > 0) {
            long id = 1 << Long.numberOfTrailingZeros(remainingIds);
            remainingIds &= ~id;
            addAction(getActionSingletonBySerializationFlag(id));
        }
    }

    /* access modifiers changed from: private */
    public static String getActionSymbolicName(int action) {
        switch (action) {
            case 1:
                return "ACTION_FOCUS";
            case 2:
                return "ACTION_CLEAR_FOCUS";
            default:
                switch (action) {
                    case 16908342:
                        return "ACTION_SHOW_ON_SCREEN";
                    case 16908343:
                        return "ACTION_SCROLL_TO_POSITION";
                    case 16908344:
                        return "ACTION_SCROLL_UP";
                    case 16908345:
                        return "ACTION_SCROLL_LEFT";
                    case 16908346:
                        return "ACTION_SCROLL_DOWN";
                    case 16908347:
                        return "ACTION_SCROLL_RIGHT";
                    case 16908348:
                        return "ACTION_CONTEXT_CLICK";
                    case 16908349:
                        return "ACTION_SET_PROGRESS";
                    default:
                        switch (action) {
                            case 16908356:
                                return "ACTION_SHOW_TOOLTIP";
                            case 16908357:
                                return "ACTION_HIDE_TOOLTIP";
                            default:
                                switch (action) {
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
                                    default:
                                        return "ACTION_UNKNOWN";
                                }
                        }
                }
        }
    }

    private static String getMovementGranularitySymbolicName(int granularity) {
        if (granularity == 4) {
            return "MOVEMENT_GRANULARITY_LINE";
        }
        if (granularity == 8) {
            return "MOVEMENT_GRANULARITY_PARAGRAPH";
        }
        if (granularity == 16) {
            return "MOVEMENT_GRANULARITY_PAGE";
        }
        switch (granularity) {
            case 1:
                return "MOVEMENT_GRANULARITY_CHARACTER";
            case 2:
                return "MOVEMENT_GRANULARITY_WORD";
            default:
                throw new IllegalArgumentException("Unknown movement granularity: " + granularity);
        }
    }

    private boolean canPerformRequestOverConnection(long accessibilityNodeId) {
        return (this.mWindowId == -1 || getAccessibilityViewId(accessibilityNodeId) == Integer.MAX_VALUE || this.mConnectionId == -1) ? false : true;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        AccessibilityNodeInfo other = (AccessibilityNodeInfo) object;
        if (this.mSourceNodeId == other.mSourceNodeId && this.mWindowId == other.mWindowId) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (31 * ((31 * ((31 * 1) + getAccessibilityViewId(this.mSourceNodeId))) + getVirtualDescendantId(this.mSourceNodeId))) + this.mWindowId;
    }

    public String toString() {
        return super.toString() + "; boundsInParent: " + this.mBoundsInParent + "; boundsInScreen: " + this.mBoundsInScreen + "; packageName: " + this.mPackageName + "; className: " + this.mClassName + "; text: " + this.mText + "; error: " + this.mError + "; maxTextLength: " + this.mMaxTextLength + "; contentDescription: " + this.mContentDescription + "; tooltipText: " + this.mTooltipText + "; viewIdResName: " + this.mViewIdResourceName + "; checkable: " + isCheckable() + "; checked: " + isChecked() + "; focusable: " + isFocusable() + "; focused: " + isFocused() + "; selected: " + isSelected() + "; clickable: " + isClickable() + "; longClickable: " + isLongClickable() + "; contextClickable: " + isContextClickable() + "; enabled: " + isEnabled() + "; password: " + isPassword() + "; scrollable: " + isScrollable() + "; importantForAccessibility: " + isImportantForAccessibility() + "; visible: " + isVisibleToUser() + "; actions: " + this.mActions;
    }

    private AccessibilityNodeInfo getNodeForAccessibilityId(long accessibilityId) {
        if (!canPerformRequestOverConnection(accessibilityId)) {
            return null;
        }
        return AccessibilityInteractionClient.getInstance().findAccessibilityNodeInfoByAccessibilityId(this.mConnectionId, this.mWindowId, accessibilityId, false, 7, null);
    }

    public static String idToString(long accessibilityId) {
        int accessibilityViewId = getAccessibilityViewId(accessibilityId);
        int virtualDescendantId = getVirtualDescendantId(accessibilityId);
        if (virtualDescendantId == -1) {
            return idItemToString(accessibilityViewId);
        }
        return idItemToString(accessibilityViewId) + SettingsStringUtil.DELIMITER + idItemToString(virtualDescendantId);
    }

    private static String idItemToString(int item) {
        if (item == -1) {
            return "HOST";
        }
        switch (item) {
            case 2147483646:
                return "ROOT";
            case Integer.MAX_VALUE:
                return "UNDEFINED";
            default:
                return "" + item;
        }
    }
}
