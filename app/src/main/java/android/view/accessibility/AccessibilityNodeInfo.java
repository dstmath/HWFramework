package android.view.accessibility;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.LongArray;
import android.util.Pools.SynchronizedPool;
import android.view.View;
import com.android.internal.R;
import com.android.internal.telephony.RILConstants;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.pgmng.log.LogPower;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccessibilityNodeInfo implements Parcelable {
    public static final int ACTION_ACCESSIBILITY_FOCUS = 64;
    public static final String ACTION_ARGUMENT_COLUMN_INT = "android.view.accessibility.action.ARGUMENT_COLUMN_INT";
    public static final String ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN = "ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN";
    public static final String ACTION_ARGUMENT_HTML_ELEMENT_STRING = "ACTION_ARGUMENT_HTML_ELEMENT_STRING";
    public static final String ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT = "ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT";
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
    public static final int ACTIVE_WINDOW_ID = Integer.MAX_VALUE;
    public static final int ANY_WINDOW_ID = -2;
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
    private static final int BOOLEAN_PROPERTY_LONG_CLICKABLE = 64;
    private static final int BOOLEAN_PROPERTY_MULTI_LINE = 32768;
    private static final int BOOLEAN_PROPERTY_OPENS_POPUP = 8192;
    private static final int BOOLEAN_PROPERTY_PASSWORD = 256;
    private static final int BOOLEAN_PROPERTY_SCROLLABLE = 512;
    private static final int BOOLEAN_PROPERTY_SELECTED = 16;
    private static final int BOOLEAN_PROPERTY_VISIBLE_TO_USER = 2048;
    public static final Creator<AccessibilityNodeInfo> CREATOR = null;
    private static final boolean DEBUG = false;
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
    public static final long ROOT_NODE_ID = 0;
    public static final int UNDEFINED_CONNECTION_ID = -1;
    public static final int UNDEFINED_ITEM_ID = Integer.MAX_VALUE;
    public static final int UNDEFINED_SELECTION_INDEX = -1;
    private static final long VIRTUAL_DESCENDANT_ID_MASK = -4294967296L;
    private static final int VIRTUAL_DESCENDANT_ID_SHIFT = 32;
    private static final SynchronizedPool<AccessibilityNodeInfo> sPool = null;
    private ArrayList<AccessibilityAction> mActions;
    private int mBooleanProperties;
    private final Rect mBoundsInParent;
    private final Rect mBoundsInScreen;
    private LongArray mChildNodeIds;
    private CharSequence mClassName;
    private CollectionInfo mCollectionInfo;
    private CollectionItemInfo mCollectionItemInfo;
    private int mConnectionId;
    private CharSequence mContentDescription;
    private int mDrawingOrderInParent;
    private CharSequence mError;
    private Bundle mExtras;
    private int mInputType;
    private long mLabelForId;
    private long mLabeledById;
    private int mLiveRegion;
    private int mMaxTextLength;
    private int mMovementGranularities;
    private CharSequence mPackageName;
    private long mParentNodeId;
    private RangeInfo mRangeInfo;
    private boolean mSealed;
    private long mSourceNodeId;
    private CharSequence mText;
    private int mTextSelectionEnd;
    private int mTextSelectionStart;
    private long mTraversalAfter;
    private long mTraversalBefore;
    private String mViewIdResourceName;
    private int mWindowId;

    public static final class AccessibilityAction {
        public static final AccessibilityAction ACTION_ACCESSIBILITY_FOCUS = null;
        public static final AccessibilityAction ACTION_CLEAR_ACCESSIBILITY_FOCUS = null;
        public static final AccessibilityAction ACTION_CLEAR_FOCUS = null;
        public static final AccessibilityAction ACTION_CLEAR_SELECTION = null;
        public static final AccessibilityAction ACTION_CLICK = null;
        public static final AccessibilityAction ACTION_COLLAPSE = null;
        public static final AccessibilityAction ACTION_CONTEXT_CLICK = null;
        public static final AccessibilityAction ACTION_COPY = null;
        public static final AccessibilityAction ACTION_CUT = null;
        public static final AccessibilityAction ACTION_DISMISS = null;
        public static final AccessibilityAction ACTION_EXPAND = null;
        public static final AccessibilityAction ACTION_FOCUS = null;
        public static final AccessibilityAction ACTION_LONG_CLICK = null;
        public static final AccessibilityAction ACTION_NEXT_AT_MOVEMENT_GRANULARITY = null;
        public static final AccessibilityAction ACTION_NEXT_HTML_ELEMENT = null;
        public static final AccessibilityAction ACTION_PASTE = null;
        public static final AccessibilityAction ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY = null;
        public static final AccessibilityAction ACTION_PREVIOUS_HTML_ELEMENT = null;
        public static final AccessibilityAction ACTION_SCROLL_BACKWARD = null;
        public static final AccessibilityAction ACTION_SCROLL_DOWN = null;
        public static final AccessibilityAction ACTION_SCROLL_FORWARD = null;
        public static final AccessibilityAction ACTION_SCROLL_LEFT = null;
        public static final AccessibilityAction ACTION_SCROLL_RIGHT = null;
        public static final AccessibilityAction ACTION_SCROLL_TO_POSITION = null;
        public static final AccessibilityAction ACTION_SCROLL_UP = null;
        public static final AccessibilityAction ACTION_SELECT = null;
        public static final AccessibilityAction ACTION_SET_PROGRESS = null;
        public static final AccessibilityAction ACTION_SET_SELECTION = null;
        public static final AccessibilityAction ACTION_SET_TEXT = null;
        public static final AccessibilityAction ACTION_SHOW_ON_SCREEN = null;
        private static final ArraySet<AccessibilityAction> sStandardActions = null;
        private final int mActionId;
        private final CharSequence mLabel;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.<clinit>():void");
        }

        public AccessibilityAction(int actionId, CharSequence label) {
            if ((AccessibilityNodeInfo.ACTION_TYPE_MASK & actionId) != 0 || Integer.bitCount(actionId) == AccessibilityNodeInfo.MOVEMENT_GRANULARITY_CHARACTER) {
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
                return AccessibilityNodeInfo.DEBUG;
            }
            if (other == this) {
                return true;
            }
            if (getClass() != other.getClass()) {
                return AccessibilityNodeInfo.DEBUG;
            }
            if (this.mActionId != ((AccessibilityAction) other).mActionId) {
                z = AccessibilityNodeInfo.DEBUG;
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
        private static final SynchronizedPool<CollectionInfo> sPool = null;
        private int mColumnCount;
        private boolean mHierarchical;
        private int mRowCount;
        private int mSelectionMode;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.accessibility.AccessibilityNodeInfo.CollectionInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.accessibility.AccessibilityNodeInfo.CollectionInfo.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.view.accessibility.AccessibilityNodeInfo.CollectionInfo.<clinit>():void");
        }

        public static CollectionInfo obtain(CollectionInfo other) {
            return obtain(other.mRowCount, other.mColumnCount, other.mHierarchical, other.mSelectionMode);
        }

        public static CollectionInfo obtain(int rowCount, int columnCount, boolean hierarchical) {
            return obtain(rowCount, columnCount, hierarchical, SELECTION_MODE_NONE);
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
            this.mRowCount = SELECTION_MODE_NONE;
            this.mColumnCount = SELECTION_MODE_NONE;
            this.mHierarchical = AccessibilityNodeInfo.DEBUG;
            this.mSelectionMode = SELECTION_MODE_NONE;
        }
    }

    public static final class CollectionItemInfo {
        private static final int MAX_POOL_SIZE = 20;
        private static final SynchronizedPool<CollectionItemInfo> sPool = null;
        private int mColumnIndex;
        private int mColumnSpan;
        private boolean mHeading;
        private int mRowIndex;
        private int mRowSpan;
        private boolean mSelected;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.accessibility.AccessibilityNodeInfo.CollectionItemInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.accessibility.AccessibilityNodeInfo.CollectionItemInfo.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.view.accessibility.AccessibilityNodeInfo.CollectionItemInfo.<clinit>():void");
        }

        public static CollectionItemInfo obtain(CollectionItemInfo other) {
            return obtain(other.mRowIndex, other.mRowSpan, other.mColumnIndex, other.mColumnSpan, other.mHeading, other.mSelected);
        }

        public static CollectionItemInfo obtain(int rowIndex, int rowSpan, int columnIndex, int columnSpan, boolean heading) {
            return obtain(rowIndex, rowSpan, columnIndex, columnSpan, heading, AccessibilityNodeInfo.DEBUG);
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
            this.mHeading = AccessibilityNodeInfo.DEBUG;
            this.mSelected = AccessibilityNodeInfo.DEBUG;
        }
    }

    public static final class RangeInfo {
        private static final int MAX_POOL_SIZE = 10;
        public static final int RANGE_TYPE_FLOAT = 1;
        public static final int RANGE_TYPE_INT = 0;
        public static final int RANGE_TYPE_PERCENT = 2;
        private static final SynchronizedPool<RangeInfo> sPool = null;
        private float mCurrent;
        private float mMax;
        private float mMin;
        private int mType;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.accessibility.AccessibilityNodeInfo.RangeInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.accessibility.AccessibilityNodeInfo.RangeInfo.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.view.accessibility.AccessibilityNodeInfo.RangeInfo.<clinit>():void");
        }

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
            this.mType = RANGE_TYPE_INT;
            this.mMin = 0.0f;
            this.mMax = 0.0f;
            this.mCurrent = 0.0f;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.accessibility.AccessibilityNodeInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.accessibility.AccessibilityNodeInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.accessibility.AccessibilityNodeInfo.<clinit>():void");
    }

    private void addLegacyStandardActions(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.accessibility.AccessibilityNodeInfo.addLegacyStandardActions(int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.accessibility.AccessibilityNodeInfo.addLegacyStandardActions(int):void");
    }

    private void setBooleanProperty(int r1, boolean r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.accessibility.AccessibilityNodeInfo.setBooleanProperty(int, boolean):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.accessibility.AccessibilityNodeInfo.setBooleanProperty(int, boolean):void");
    }

    public static int getAccessibilityViewId(long accessibilityNodeId) {
        return (int) accessibilityNodeId;
    }

    public static int getVirtualDescendantId(long accessibilityNodeId) {
        return (int) ((VIRTUAL_DESCENDANT_ID_MASK & accessibilityNodeId) >> VIRTUAL_DESCENDANT_ID_SHIFT);
    }

    public static long makeNodeId(int accessibilityViewId, int virtualDescendantId) {
        if (virtualDescendantId == UNDEFINED_SELECTION_INDEX) {
            virtualDescendantId = UNDEFINED_ITEM_ID;
        }
        return (((long) virtualDescendantId) << VIRTUAL_DESCENDANT_ID_SHIFT) | ((long) accessibilityViewId);
    }

    private AccessibilityNodeInfo() {
        this.mWindowId = UNDEFINED_ITEM_ID;
        this.mSourceNodeId = ROOT_NODE_ID;
        this.mParentNodeId = ROOT_NODE_ID;
        this.mLabelForId = ROOT_NODE_ID;
        this.mLabeledById = ROOT_NODE_ID;
        this.mTraversalBefore = ROOT_NODE_ID;
        this.mTraversalAfter = ROOT_NODE_ID;
        this.mBoundsInParent = new Rect();
        this.mBoundsInScreen = new Rect();
        this.mMaxTextLength = UNDEFINED_SELECTION_INDEX;
        this.mTextSelectionStart = UNDEFINED_SELECTION_INDEX;
        this.mTextSelectionEnd = UNDEFINED_SELECTION_INDEX;
        this.mInputType = 0;
        this.mLiveRegion = 0;
        this.mConnectionId = UNDEFINED_SELECTION_INDEX;
    }

    public void setSource(View source) {
        setSource(source, UNDEFINED_ITEM_ID);
    }

    public void setSource(View root, int virtualDescendantId) {
        int accessibilityWindowId;
        int rootAccessibilityViewId;
        enforceNotSealed();
        if (root != null) {
            accessibilityWindowId = root.getAccessibilityWindowId();
        } else {
            accessibilityWindowId = UNDEFINED_ITEM_ID;
        }
        this.mWindowId = accessibilityWindowId;
        if (root != null) {
            rootAccessibilityViewId = root.getAccessibilityViewId();
        } else {
            rootAccessibilityViewId = UNDEFINED_ITEM_ID;
        }
        this.mSourceNodeId = makeNodeId(rootAccessibilityViewId, virtualDescendantId);
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

    public boolean refresh(boolean bypassCache) {
        enforceSealed();
        if (!canPerformRequestOverConnection(this.mSourceNodeId)) {
            return DEBUG;
        }
        AccessibilityNodeInfo refreshedInfo = AccessibilityInteractionClient.getInstance().findAccessibilityNodeInfoByAccessibilityId(this.mConnectionId, this.mWindowId, this.mSourceNodeId, bypassCache, 0);
        if (refreshedInfo == null) {
            return DEBUG;
        }
        init(refreshedInfo);
        refreshedInfo.recycle();
        return true;
    }

    public boolean refresh() {
        return refresh(true);
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
        return AccessibilityInteractionClient.getInstance().findAccessibilityNodeInfoByAccessibilityId(this.mConnectionId, this.mWindowId, this.mChildNodeIds.get(index), DEBUG, MOVEMENT_GRANULARITY_LINE);
    }

    public void addChild(View child) {
        addChildInternal(child, UNDEFINED_ITEM_ID, true);
    }

    public void addChildUnchecked(View child) {
        addChildInternal(child, UNDEFINED_ITEM_ID, DEBUG);
    }

    public boolean removeChild(View child) {
        return removeChild(child, UNDEFINED_ITEM_ID);
    }

    public void addChild(View root, int virtualDescendantId) {
        addChildInternal(root, virtualDescendantId, true);
    }

    private void addChildInternal(View root, int virtualDescendantId, boolean checked) {
        enforceNotSealed();
        if (this.mChildNodeIds == null) {
            this.mChildNodeIds = new LongArray();
        }
        long childNodeId = makeNodeId(root != null ? root.getAccessibilityViewId() : UNDEFINED_ITEM_ID, virtualDescendantId);
        if (!checked || this.mChildNodeIds.indexOf(childNodeId) < 0) {
            this.mChildNodeIds.add(childNodeId);
        }
    }

    public boolean removeChild(View root, int virtualDescendantId) {
        enforceNotSealed();
        LongArray childIds = this.mChildNodeIds;
        if (childIds == null) {
            return DEBUG;
        }
        int index = childIds.indexOf(makeNodeId(root != null ? root.getAccessibilityViewId() : UNDEFINED_ITEM_ID, virtualDescendantId));
        if (index < 0) {
            return DEBUG;
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
        for (int i = 0; i < actionSize; i += MOVEMENT_GRANULARITY_CHARACTER) {
            int actionId = ((AccessibilityAction) this.mActions.get(i)).getId();
            if (actionId <= LAST_LEGACY_STANDARD_ACTION) {
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
        if ((ACTION_TYPE_MASK & action) != 0) {
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
            return DEBUG;
        }
        return this.mActions.remove(action);
    }

    public AccessibilityNodeInfo getTraversalBefore() {
        enforceSealed();
        return getNodeForAccessibilityId(this.mTraversalBefore);
    }

    public void setTraversalBefore(View view) {
        setTraversalBefore(view, UNDEFINED_ITEM_ID);
    }

    public void setTraversalBefore(View root, int virtualDescendantId) {
        enforceNotSealed();
        this.mTraversalBefore = makeNodeId(root != null ? root.getAccessibilityViewId() : UNDEFINED_ITEM_ID, virtualDescendantId);
    }

    public AccessibilityNodeInfo getTraversalAfter() {
        enforceSealed();
        return getNodeForAccessibilityId(this.mTraversalAfter);
    }

    public void setTraversalAfter(View view) {
        setTraversalAfter(view, UNDEFINED_ITEM_ID);
    }

    public void setTraversalAfter(View root, int virtualDescendantId) {
        enforceNotSealed();
        this.mTraversalAfter = makeNodeId(root != null ? root.getAccessibilityViewId() : UNDEFINED_ITEM_ID, virtualDescendantId);
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
        return DEBUG;
    }

    public boolean performAction(int action, Bundle arguments) {
        enforceSealed();
        if (canPerformRequestOverConnection(this.mSourceNodeId)) {
            return AccessibilityInteractionClient.getInstance().performAccessibilityAction(this.mConnectionId, this.mWindowId, this.mSourceNodeId, action, arguments);
        }
        return DEBUG;
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
        setParent(parent, UNDEFINED_ITEM_ID);
    }

    public void setParent(View root, int virtualDescendantId) {
        enforceNotSealed();
        this.mParentNodeId = makeNodeId(root != null ? root.getAccessibilityViewId() : UNDEFINED_ITEM_ID, virtualDescendantId);
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
        return getBooleanProperty(MOVEMENT_GRANULARITY_CHARACTER);
    }

    public void setCheckable(boolean checkable) {
        setBooleanProperty(MOVEMENT_GRANULARITY_CHARACTER, checkable);
    }

    public boolean isChecked() {
        return getBooleanProperty(MOVEMENT_GRANULARITY_WORD);
    }

    public void setChecked(boolean checked) {
        setBooleanProperty(MOVEMENT_GRANULARITY_WORD, checked);
    }

    public boolean isFocusable() {
        return getBooleanProperty(MOVEMENT_GRANULARITY_LINE);
    }

    public void setFocusable(boolean focusable) {
        setBooleanProperty(MOVEMENT_GRANULARITY_LINE, focusable);
    }

    public boolean isFocused() {
        return getBooleanProperty(MOVEMENT_GRANULARITY_PARAGRAPH);
    }

    public void setFocused(boolean focused) {
        setBooleanProperty(MOVEMENT_GRANULARITY_PARAGRAPH, focused);
    }

    public boolean isVisibleToUser() {
        return getBooleanProperty(BOOLEAN_PROPERTY_VISIBLE_TO_USER);
    }

    public void setVisibleToUser(boolean visibleToUser) {
        setBooleanProperty(BOOLEAN_PROPERTY_VISIBLE_TO_USER, visibleToUser);
    }

    public boolean isAccessibilityFocused() {
        return getBooleanProperty(BOOLEAN_PROPERTY_ACCESSIBILITY_FOCUSED);
    }

    public void setAccessibilityFocused(boolean focused) {
        setBooleanProperty(BOOLEAN_PROPERTY_ACCESSIBILITY_FOCUSED, focused);
    }

    public boolean isSelected() {
        return getBooleanProperty(MOVEMENT_GRANULARITY_PAGE);
    }

    public void setSelected(boolean selected) {
        setBooleanProperty(MOVEMENT_GRANULARITY_PAGE, selected);
    }

    public boolean isClickable() {
        return getBooleanProperty(VIRTUAL_DESCENDANT_ID_SHIFT);
    }

    public void setClickable(boolean clickable) {
        setBooleanProperty(VIRTUAL_DESCENDANT_ID_SHIFT, clickable);
    }

    public boolean isLongClickable() {
        return getBooleanProperty(BOOLEAN_PROPERTY_LONG_CLICKABLE);
    }

    public void setLongClickable(boolean longClickable) {
        setBooleanProperty(BOOLEAN_PROPERTY_LONG_CLICKABLE, longClickable);
    }

    public boolean isEnabled() {
        return getBooleanProperty(BOOLEAN_PROPERTY_ENABLED);
    }

    public void setEnabled(boolean enabled) {
        setBooleanProperty(BOOLEAN_PROPERTY_ENABLED, enabled);
    }

    public boolean isPassword() {
        return getBooleanProperty(BOOLEAN_PROPERTY_PASSWORD);
    }

    public void setPassword(boolean password) {
        setBooleanProperty(BOOLEAN_PROPERTY_PASSWORD, password);
    }

    public boolean isScrollable() {
        return getBooleanProperty(BOOLEAN_PROPERTY_SCROLLABLE);
    }

    public void setScrollable(boolean scrollable) {
        setBooleanProperty(BOOLEAN_PROPERTY_SCROLLABLE, scrollable);
    }

    public boolean isEditable() {
        return getBooleanProperty(BOOLEAN_PROPERTY_EDITABLE);
    }

    public void setEditable(boolean editable) {
        setBooleanProperty(BOOLEAN_PROPERTY_EDITABLE, editable);
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
        return getBooleanProperty(BOOLEAN_PROPERTY_CONTENT_INVALID);
    }

    public void setContentInvalid(boolean contentInvalid) {
        setBooleanProperty(BOOLEAN_PROPERTY_CONTENT_INVALID, contentInvalid);
    }

    public boolean isContextClickable() {
        return getBooleanProperty(BOOLEAN_PROPERTY_CONTEXT_CLICKABLE);
    }

    public void setContextClickable(boolean contextClickable) {
        setBooleanProperty(BOOLEAN_PROPERTY_CONTEXT_CLICKABLE, contextClickable);
    }

    public int getLiveRegion() {
        return this.mLiveRegion;
    }

    public void setLiveRegion(int mode) {
        enforceNotSealed();
        this.mLiveRegion = mode;
    }

    public boolean isMultiLine() {
        return getBooleanProperty(BOOLEAN_PROPERTY_MULTI_LINE);
    }

    public void setMultiLine(boolean multiLine) {
        setBooleanProperty(BOOLEAN_PROPERTY_MULTI_LINE, multiLine);
    }

    public boolean canOpenPopup() {
        return getBooleanProperty(BOOLEAN_PROPERTY_OPENS_POPUP);
    }

    public void setCanOpenPopup(boolean opensPopup) {
        enforceNotSealed();
        setBooleanProperty(BOOLEAN_PROPERTY_OPENS_POPUP, opensPopup);
    }

    public boolean isDismissable() {
        return getBooleanProperty(BOOLEAN_PROPERTY_DISMISSABLE);
    }

    public void setDismissable(boolean dismissable) {
        setBooleanProperty(BOOLEAN_PROPERTY_DISMISSABLE, dismissable);
    }

    public boolean isImportantForAccessibility() {
        return getBooleanProperty(BOOLEAN_PROPERTY_IMPORTANCE);
    }

    public void setImportantForAccessibility(boolean important) {
        setBooleanProperty(BOOLEAN_PROPERTY_IMPORTANCE, important);
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
        return this.mText;
    }

    public void setText(CharSequence text) {
        enforceNotSealed();
        this.mText = text;
    }

    public void setError(CharSequence error) {
        enforceNotSealed();
        this.mError = error;
    }

    public CharSequence getError() {
        return this.mError;
    }

    public CharSequence getContentDescription() {
        return this.mContentDescription;
    }

    public void setContentDescription(CharSequence contentDescription) {
        enforceNotSealed();
        this.mContentDescription = contentDescription;
    }

    public void setLabelFor(View labeled) {
        setLabelFor(labeled, UNDEFINED_ITEM_ID);
    }

    public void setLabelFor(View root, int virtualDescendantId) {
        enforceNotSealed();
        this.mLabelForId = makeNodeId(root != null ? root.getAccessibilityViewId() : UNDEFINED_ITEM_ID, virtualDescendantId);
    }

    public AccessibilityNodeInfo getLabelFor() {
        enforceSealed();
        return getNodeForAccessibilityId(this.mLabelForId);
    }

    public void setLabeledBy(View label) {
        setLabeledBy(label, UNDEFINED_ITEM_ID);
    }

    public void setLabeledBy(View root, int virtualDescendantId) {
        enforceNotSealed();
        this.mLabeledById = makeNodeId(root != null ? root.getAccessibilityViewId() : UNDEFINED_ITEM_ID, virtualDescendantId);
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

    private boolean getBooleanProperty(int property) {
        return (this.mBooleanProperties & property) != 0 ? true : DEBUG;
    }

    public void setConnectionId(int connectionId) {
        enforceNotSealed();
        this.mConnectionId = connectionId;
    }

    public int describeContents() {
        return 0;
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
            case MOVEMENT_GRANULARITY_CHARACTER /*1*/:
            case MOVEMENT_GRANULARITY_WORD /*2*/:
            case StatisticalConstant.TYPE_TRIKEY_RIGHT_RECENT /*17*/:
            case StatisticalConstant.TYPE_MULTIWINDOW_BUBBLE_MENU_CLOSE /*33*/:
            case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
            case LogPower.END_CHG_ROTATION /*130*/:
            default:
                throw new IllegalArgumentException("Unknown direction: " + direction);
        }
    }

    private void enforceValidFocusType(int focusType) {
        switch (focusType) {
            case MOVEMENT_GRANULARITY_CHARACTER /*1*/:
            case MOVEMENT_GRANULARITY_WORD /*2*/:
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
    }

    public void writeToParcel(Parcel parcel, int flags) {
        int i;
        int i2;
        int i3 = MOVEMENT_GRANULARITY_CHARACTER;
        if (isSealed()) {
            i = MOVEMENT_GRANULARITY_CHARACTER;
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
            for (i2 = 0; i2 < childIdsSize; i2 += MOVEMENT_GRANULARITY_CHARACTER) {
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
        if (this.mActions == null || this.mActions.isEmpty()) {
            parcel.writeInt(0);
        } else {
            AccessibilityAction action;
            int actionCount = this.mActions.size();
            parcel.writeInt(actionCount);
            int defaultLegacyStandardActions = 0;
            for (i2 = 0; i2 < actionCount; i2 += MOVEMENT_GRANULARITY_CHARACTER) {
                action = (AccessibilityAction) this.mActions.get(i2);
                if (isDefaultLegacyStandardAction(action)) {
                    defaultLegacyStandardActions |= action.getId();
                }
            }
            parcel.writeInt(defaultLegacyStandardActions);
            for (i2 = 0; i2 < actionCount; i2 += MOVEMENT_GRANULARITY_CHARACTER) {
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
        parcel.writeCharSequence(this.mError);
        parcel.writeCharSequence(this.mContentDescription);
        parcel.writeString(this.mViewIdResourceName);
        parcel.writeInt(this.mTextSelectionStart);
        parcel.writeInt(this.mTextSelectionEnd);
        parcel.writeInt(this.mInputType);
        parcel.writeInt(this.mLiveRegion);
        parcel.writeInt(this.mDrawingOrderInParent);
        if (this.mExtras != null) {
            parcel.writeInt(MOVEMENT_GRANULARITY_CHARACTER);
            parcel.writeBundle(this.mExtras);
        } else {
            parcel.writeInt(0);
        }
        if (this.mRangeInfo != null) {
            parcel.writeInt(MOVEMENT_GRANULARITY_CHARACTER);
            parcel.writeInt(this.mRangeInfo.getType());
            parcel.writeFloat(this.mRangeInfo.getMin());
            parcel.writeFloat(this.mRangeInfo.getMax());
            parcel.writeFloat(this.mRangeInfo.getCurrent());
        } else {
            parcel.writeInt(0);
        }
        if (this.mCollectionInfo != null) {
            parcel.writeInt(MOVEMENT_GRANULARITY_CHARACTER);
            parcel.writeInt(this.mCollectionInfo.getRowCount());
            parcel.writeInt(this.mCollectionInfo.getColumnCount());
            if (this.mCollectionInfo.isHierarchical()) {
                i = MOVEMENT_GRANULARITY_CHARACTER;
            } else {
                i = 0;
            }
            parcel.writeInt(i);
            parcel.writeInt(this.mCollectionInfo.getSelectionMode());
        } else {
            parcel.writeInt(0);
        }
        if (this.mCollectionItemInfo != null) {
            parcel.writeInt(MOVEMENT_GRANULARITY_CHARACTER);
            parcel.writeInt(this.mCollectionItemInfo.getRowIndex());
            parcel.writeInt(this.mCollectionItemInfo.getRowSpan());
            parcel.writeInt(this.mCollectionItemInfo.getColumnIndex());
            parcel.writeInt(this.mCollectionItemInfo.getColumnSpan());
            if (this.mCollectionItemInfo.isHeading()) {
                i = MOVEMENT_GRANULARITY_CHARACTER;
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
        boolean sealed = parcel.readInt() == MOVEMENT_GRANULARITY_CHARACTER ? true : DEBUG;
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
            for (i = 0; i < childrenSize; i += MOVEMENT_GRANULARITY_CHARACTER) {
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
        int actionCount = parcel.readInt();
        if (actionCount > 0) {
            int legacyStandardActions = parcel.readInt();
            addLegacyStandardActions(legacyStandardActions);
            int nonLegacyActionCount = actionCount - Integer.bitCount(legacyStandardActions);
            for (i = 0; i < nonLegacyActionCount; i += MOVEMENT_GRANULARITY_CHARACTER) {
                addActionUnchecked(new AccessibilityAction(parcel.readInt(), parcel.readCharSequence()));
            }
        }
        this.mMaxTextLength = parcel.readInt();
        this.mMovementGranularities = parcel.readInt();
        this.mBooleanProperties = parcel.readInt();
        this.mPackageName = parcel.readCharSequence();
        this.mClassName = parcel.readCharSequence();
        this.mText = parcel.readCharSequence();
        this.mError = parcel.readCharSequence();
        this.mContentDescription = parcel.readCharSequence();
        this.mViewIdResourceName = parcel.readString();
        this.mTextSelectionStart = parcel.readInt();
        this.mTextSelectionEnd = parcel.readInt();
        this.mInputType = parcel.readInt();
        this.mLiveRegion = parcel.readInt();
        this.mDrawingOrderInParent = parcel.readInt();
        if (parcel.readInt() == MOVEMENT_GRANULARITY_CHARACTER) {
            this.mExtras = parcel.readBundle();
        } else {
            this.mExtras = null;
        }
        if (parcel.readInt() == MOVEMENT_GRANULARITY_CHARACTER) {
            this.mRangeInfo = RangeInfo.obtain(parcel.readInt(), parcel.readFloat(), parcel.readFloat(), parcel.readFloat());
        }
        if (parcel.readInt() == MOVEMENT_GRANULARITY_CHARACTER) {
            this.mCollectionInfo = CollectionInfo.obtain(parcel.readInt(), parcel.readInt(), parcel.readInt() == MOVEMENT_GRANULARITY_CHARACTER ? true : DEBUG, parcel.readInt());
        }
        if (parcel.readInt() == MOVEMENT_GRANULARITY_CHARACTER) {
            boolean z;
            int readInt = parcel.readInt();
            int readInt2 = parcel.readInt();
            int readInt3 = parcel.readInt();
            int readInt4 = parcel.readInt();
            boolean z2 = parcel.readInt() == MOVEMENT_GRANULARITY_CHARACTER ? true : DEBUG;
            if (parcel.readInt() == MOVEMENT_GRANULARITY_CHARACTER) {
                z = true;
            } else {
                z = DEBUG;
            }
            this.mCollectionItemInfo = CollectionItemInfo.obtain(readInt, readInt2, readInt3, readInt4, z2, z);
        }
        this.mSealed = sealed;
    }

    private void clear() {
        this.mSealed = DEBUG;
        this.mSourceNodeId = ROOT_NODE_ID;
        this.mParentNodeId = ROOT_NODE_ID;
        this.mLabelForId = ROOT_NODE_ID;
        this.mLabeledById = ROOT_NODE_ID;
        this.mTraversalBefore = ROOT_NODE_ID;
        this.mTraversalAfter = ROOT_NODE_ID;
        this.mWindowId = UNDEFINED_ITEM_ID;
        this.mConnectionId = UNDEFINED_SELECTION_INDEX;
        this.mMaxTextLength = UNDEFINED_SELECTION_INDEX;
        this.mMovementGranularities = 0;
        if (this.mChildNodeIds != null) {
            this.mChildNodeIds.clear();
        }
        this.mBoundsInParent.set(0, 0, 0, 0);
        this.mBoundsInScreen.set(0, 0, 0, 0);
        this.mBooleanProperties = 0;
        this.mDrawingOrderInParent = 0;
        this.mPackageName = null;
        this.mClassName = null;
        this.mText = null;
        this.mError = null;
        this.mContentDescription = null;
        this.mViewIdResourceName = null;
        if (this.mActions != null) {
            this.mActions.clear();
        }
        this.mTextSelectionStart = UNDEFINED_SELECTION_INDEX;
        this.mTextSelectionEnd = UNDEFINED_SELECTION_INDEX;
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
        if (action.getId() <= LAST_LEGACY_STANDARD_ACTION) {
            return TextUtils.isEmpty(action.getLabel());
        }
        return DEBUG;
    }

    private static AccessibilityAction getActionSingleton(int actionId) {
        int actions = AccessibilityAction.sStandardActions.size();
        for (int i = 0; i < actions; i += MOVEMENT_GRANULARITY_CHARACTER) {
            AccessibilityAction currentAction = (AccessibilityAction) AccessibilityAction.sStandardActions.valueAt(i);
            if (actionId == currentAction.getId()) {
                return currentAction;
            }
        }
        return null;
    }

    private static String getActionSymbolicName(int action) {
        switch (action) {
            case MOVEMENT_GRANULARITY_CHARACTER /*1*/:
                return "ACTION_FOCUS";
            case MOVEMENT_GRANULARITY_WORD /*2*/:
                return "ACTION_CLEAR_FOCUS";
            case MOVEMENT_GRANULARITY_LINE /*4*/:
                return "ACTION_SELECT";
            case MOVEMENT_GRANULARITY_PARAGRAPH /*8*/:
                return "ACTION_CLEAR_SELECTION";
            case MOVEMENT_GRANULARITY_PAGE /*16*/:
                return "ACTION_CLICK";
            case VIRTUAL_DESCENDANT_ID_SHIFT /*32*/:
                return "ACTION_LONG_CLICK";
            case BOOLEAN_PROPERTY_LONG_CLICKABLE /*64*/:
                return "ACTION_ACCESSIBILITY_FOCUS";
            case BOOLEAN_PROPERTY_ENABLED /*128*/:
                return "ACTION_CLEAR_ACCESSIBILITY_FOCUS";
            case BOOLEAN_PROPERTY_PASSWORD /*256*/:
                return "ACTION_NEXT_AT_MOVEMENT_GRANULARITY";
            case BOOLEAN_PROPERTY_SCROLLABLE /*512*/:
                return "ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY";
            case BOOLEAN_PROPERTY_ACCESSIBILITY_FOCUSED /*1024*/:
                return "ACTION_NEXT_HTML_ELEMENT";
            case BOOLEAN_PROPERTY_VISIBLE_TO_USER /*2048*/:
                return "ACTION_PREVIOUS_HTML_ELEMENT";
            case BOOLEAN_PROPERTY_EDITABLE /*4096*/:
                return "ACTION_SCROLL_FORWARD";
            case BOOLEAN_PROPERTY_OPENS_POPUP /*8192*/:
                return "ACTION_SCROLL_BACKWARD";
            case BOOLEAN_PROPERTY_DISMISSABLE /*16384*/:
                return "ACTION_COPY";
            case BOOLEAN_PROPERTY_MULTI_LINE /*32768*/:
                return "ACTION_PASTE";
            case BOOLEAN_PROPERTY_CONTENT_INVALID /*65536*/:
                return "ACTION_CUT";
            case BOOLEAN_PROPERTY_CONTEXT_CLICKABLE /*131072*/:
                return "ACTION_SET_SELECTION";
            case BOOLEAN_PROPERTY_IMPORTANCE /*262144*/:
                return "ACTION_EXPAND";
            case ACTION_COLLAPSE /*524288*/:
                return "ACTION_COLLAPSE";
            case ACTION_DISMISS /*1048576*/:
                return "ACTION_DISMISS";
            case LAST_LEGACY_STANDARD_ACTION /*2097152*/:
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
            case MOVEMENT_GRANULARITY_CHARACTER /*1*/:
                return "MOVEMENT_GRANULARITY_CHARACTER";
            case MOVEMENT_GRANULARITY_WORD /*2*/:
                return "MOVEMENT_GRANULARITY_WORD";
            case MOVEMENT_GRANULARITY_LINE /*4*/:
                return "MOVEMENT_GRANULARITY_LINE";
            case MOVEMENT_GRANULARITY_PARAGRAPH /*8*/:
                return "MOVEMENT_GRANULARITY_PARAGRAPH";
            case MOVEMENT_GRANULARITY_PAGE /*16*/:
                return "MOVEMENT_GRANULARITY_PAGE";
            default:
                throw new IllegalArgumentException("Unknown movement granularity: " + granularity);
        }
    }

    private boolean canPerformRequestOverConnection(long accessibilityNodeId) {
        if (this.mWindowId == UNDEFINED_ITEM_ID || getAccessibilityViewId(accessibilityNodeId) == UNDEFINED_ITEM_ID || this.mConnectionId == UNDEFINED_SELECTION_INDEX) {
            return DEBUG;
        }
        return true;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return DEBUG;
        }
        AccessibilityNodeInfo other = (AccessibilityNodeInfo) object;
        return (this.mSourceNodeId == other.mSourceNodeId && this.mWindowId == other.mWindowId) ? true : DEBUG;
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
        builder.append("; actions: ").append(this.mActions);
        return builder.toString();
    }

    private AccessibilityNodeInfo getNodeForAccessibilityId(long accessibilityId) {
        if (canPerformRequestOverConnection(accessibilityId)) {
            return AccessibilityInteractionClient.getInstance().findAccessibilityNodeInfoByAccessibilityId(this.mConnectionId, this.mWindowId, accessibilityId, DEBUG, 7);
        }
        return null;
    }
}
