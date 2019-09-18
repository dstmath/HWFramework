package android.view.accessibility;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Pools;
import com.android.internal.util.BitUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public final class AccessibilityEvent extends AccessibilityRecord implements Parcelable {
    public static final int CONTENT_CHANGE_TYPE_CONTENT_DESCRIPTION = 4;
    public static final int CONTENT_CHANGE_TYPE_PANE_APPEARED = 16;
    public static final int CONTENT_CHANGE_TYPE_PANE_DISAPPEARED = 32;
    public static final int CONTENT_CHANGE_TYPE_PANE_TITLE = 8;
    public static final int CONTENT_CHANGE_TYPE_SUBTREE = 1;
    public static final int CONTENT_CHANGE_TYPE_TEXT = 2;
    public static final int CONTENT_CHANGE_TYPE_UNDEFINED = 0;
    public static final Parcelable.Creator<AccessibilityEvent> CREATOR = new Parcelable.Creator<AccessibilityEvent>() {
        public AccessibilityEvent createFromParcel(Parcel parcel) {
            AccessibilityEvent event = AccessibilityEvent.obtain();
            event.initFromParcel(parcel);
            return event;
        }

        public AccessibilityEvent[] newArray(int size) {
            return new AccessibilityEvent[size];
        }
    };
    private static final boolean DEBUG = false;
    public static final boolean DEBUG_ORIGIN = false;
    public static final int INVALID_POSITION = -1;
    private static final int MAX_POOL_SIZE = 10;
    @Deprecated
    public static final int MAX_TEXT_LENGTH = 500;
    public static final int TYPES_ALL_MASK = -1;
    public static final int TYPE_ANNOUNCEMENT = 16384;
    public static final int TYPE_ASSIST_READING_CONTEXT = 16777216;
    public static final int TYPE_GESTURE_DETECTION_END = 524288;
    public static final int TYPE_GESTURE_DETECTION_START = 262144;
    public static final int TYPE_NOTIFICATION_STATE_CHANGED = 64;
    public static final int TYPE_TOUCH_EXPLORATION_GESTURE_END = 1024;
    public static final int TYPE_TOUCH_EXPLORATION_GESTURE_START = 512;
    public static final int TYPE_TOUCH_INTERACTION_END = 2097152;
    public static final int TYPE_TOUCH_INTERACTION_START = 1048576;
    public static final int TYPE_VIEW_ACCESSIBILITY_FOCUSED = 32768;
    public static final int TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED = 65536;
    public static final int TYPE_VIEW_CLICKED = 1;
    public static final int TYPE_VIEW_CONTEXT_CLICKED = 8388608;
    public static final int TYPE_VIEW_FOCUSED = 8;
    public static final int TYPE_VIEW_HOVER_ENTER = 128;
    public static final int TYPE_VIEW_HOVER_EXIT = 256;
    public static final int TYPE_VIEW_LONG_CLICKED = 2;
    public static final int TYPE_VIEW_SCROLLED = 4096;
    public static final int TYPE_VIEW_SELECTED = 4;
    public static final int TYPE_VIEW_TEXT_CHANGED = 16;
    public static final int TYPE_VIEW_TEXT_SELECTION_CHANGED = 8192;
    public static final int TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY = 131072;
    public static final int TYPE_WINDOWS_CHANGED = 4194304;
    public static final int TYPE_WINDOW_CONTENT_CHANGED = 2048;
    public static final int TYPE_WINDOW_STATE_CHANGED = 32;
    public static final int WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED = 128;
    public static final int WINDOWS_CHANGE_ACTIVE = 32;
    public static final int WINDOWS_CHANGE_ADDED = 1;
    public static final int WINDOWS_CHANGE_BOUNDS = 8;
    public static final int WINDOWS_CHANGE_CHILDREN = 512;
    public static final int WINDOWS_CHANGE_FOCUSED = 64;
    public static final int WINDOWS_CHANGE_LAYER = 16;
    public static final int WINDOWS_CHANGE_PARENT = 256;
    public static final int WINDOWS_CHANGE_PIP = 1024;
    public static final int WINDOWS_CHANGE_REMOVED = 2;
    public static final int WINDOWS_CHANGE_TITLE = 4;
    private static final Pools.SynchronizedPool<AccessibilityEvent> sPool = new Pools.SynchronizedPool<>(10);
    int mAction;
    int mContentChangeTypes;
    private long mEventTime;
    private int mEventType;
    int mMovementGranularity;
    private CharSequence mPackageName;
    private ArrayList<AccessibilityRecord> mRecords;
    int mWindowChangeTypes;
    public StackTraceElement[] originStackTrace = null;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ContentChangeTypes {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface EventType {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface WindowsChangeTypes {
    }

    private AccessibilityEvent() {
    }

    /* access modifiers changed from: package-private */
    public void init(AccessibilityEvent event) {
        super.init(event);
        this.mEventType = event.mEventType;
        this.mMovementGranularity = event.mMovementGranularity;
        this.mAction = event.mAction;
        this.mContentChangeTypes = event.mContentChangeTypes;
        this.mWindowChangeTypes = event.mWindowChangeTypes;
        this.mEventTime = event.mEventTime;
        this.mPackageName = event.mPackageName;
    }

    public void setSealed(boolean sealed) {
        super.setSealed(sealed);
        List<AccessibilityRecord> records = this.mRecords;
        if (records != null) {
            int recordCount = records.size();
            for (int i = 0; i < recordCount; i++) {
                records.get(i).setSealed(sealed);
            }
        }
    }

    public int getRecordCount() {
        if (this.mRecords == null) {
            return 0;
        }
        return this.mRecords.size();
    }

    public void appendRecord(AccessibilityRecord record) {
        enforceNotSealed();
        if (this.mRecords == null) {
            this.mRecords = new ArrayList<>();
        }
        this.mRecords.add(record);
    }

    public AccessibilityRecord getRecord(int index) {
        if (this.mRecords != null) {
            return this.mRecords.get(index);
        }
        throw new IndexOutOfBoundsException("Invalid index " + index + ", size is 0");
    }

    public int getEventType() {
        return this.mEventType;
    }

    public int getContentChangeTypes() {
        return this.mContentChangeTypes;
    }

    private static String contentChangeTypesToString(int types) {
        return BitUtils.flagsToString(types, $$Lambda$AccessibilityEvent$gjyLj65KEDUo5PJZiVYxPrd2Vug.INSTANCE);
    }

    /* access modifiers changed from: private */
    public static String singleContentChangeTypeToString(int type) {
        if (type == 4) {
            return "CONTENT_CHANGE_TYPE_CONTENT_DESCRIPTION";
        }
        if (type == 8) {
            return "CONTENT_CHANGE_TYPE_PANE_TITLE";
        }
        switch (type) {
            case 0:
                return "CONTENT_CHANGE_TYPE_UNDEFINED";
            case 1:
                return "CONTENT_CHANGE_TYPE_SUBTREE";
            case 2:
                return "CONTENT_CHANGE_TYPE_TEXT";
            default:
                return Integer.toHexString(type);
        }
    }

    public void setContentChangeTypes(int changeTypes) {
        enforceNotSealed();
        this.mContentChangeTypes = changeTypes;
    }

    public int getWindowChanges() {
        return this.mWindowChangeTypes;
    }

    public void setWindowChanges(int changes) {
        this.mWindowChangeTypes = changes;
    }

    private static String windowChangeTypesToString(int types) {
        return BitUtils.flagsToString(types, $$Lambda$AccessibilityEvent$c6ikd5OkCnJv2aVsheVXIxBvSTk.INSTANCE);
    }

    /* access modifiers changed from: private */
    public static String singleWindowChangeTypeToString(int type) {
        if (type == 4) {
            return "WINDOWS_CHANGE_TITLE";
        }
        if (type == 8) {
            return "WINDOWS_CHANGE_BOUNDS";
        }
        if (type == 16) {
            return "WINDOWS_CHANGE_LAYER";
        }
        if (type == 32) {
            return "WINDOWS_CHANGE_ACTIVE";
        }
        if (type == 64) {
            return "WINDOWS_CHANGE_FOCUSED";
        }
        if (type == 128) {
            return "WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED";
        }
        if (type == 256) {
            return "WINDOWS_CHANGE_PARENT";
        }
        if (type == 512) {
            return "WINDOWS_CHANGE_CHILDREN";
        }
        switch (type) {
            case 1:
                return "WINDOWS_CHANGE_ADDED";
            case 2:
                return "WINDOWS_CHANGE_REMOVED";
            default:
                return Integer.toHexString(type);
        }
    }

    public void setEventType(int eventType) {
        enforceNotSealed();
        this.mEventType = eventType;
    }

    public long getEventTime() {
        return this.mEventTime;
    }

    public void setEventTime(long eventTime) {
        enforceNotSealed();
        this.mEventTime = eventTime;
    }

    public CharSequence getPackageName() {
        return this.mPackageName;
    }

    public void setPackageName(CharSequence packageName) {
        enforceNotSealed();
        this.mPackageName = packageName;
    }

    public void setMovementGranularity(int granularity) {
        enforceNotSealed();
        this.mMovementGranularity = granularity;
    }

    public int getMovementGranularity() {
        return this.mMovementGranularity;
    }

    public void setAction(int action) {
        enforceNotSealed();
        this.mAction = action;
    }

    public int getAction() {
        return this.mAction;
    }

    public static AccessibilityEvent obtainWindowsChangedEvent(int windowId, int windowChangeTypes) {
        AccessibilityEvent event = obtain(4194304);
        event.setWindowId(windowId);
        event.setWindowChanges(windowChangeTypes);
        event.setImportantForAccessibility(true);
        return event;
    }

    public static AccessibilityEvent obtain(int eventType) {
        AccessibilityEvent event = obtain();
        event.setEventType(eventType);
        return event;
    }

    public static AccessibilityEvent obtain(AccessibilityEvent event) {
        AccessibilityEvent eventClone = obtain();
        eventClone.init(event);
        if (event.mRecords != null) {
            int recordCount = event.mRecords.size();
            eventClone.mRecords = new ArrayList<>(recordCount);
            for (int i = 0; i < recordCount; i++) {
                eventClone.mRecords.add(AccessibilityRecord.obtain(event.mRecords.get(i)));
            }
        }
        return eventClone;
    }

    public static AccessibilityEvent obtain() {
        AccessibilityEvent event = sPool.acquire();
        if (event == null) {
            return new AccessibilityEvent();
        }
        return event;
    }

    public void recycle() {
        clear();
        sPool.release(this);
    }

    /* access modifiers changed from: protected */
    public void clear() {
        super.clear();
        this.mEventType = 0;
        this.mMovementGranularity = 0;
        this.mAction = 0;
        this.mContentChangeTypes = 0;
        this.mWindowChangeTypes = 0;
        this.mPackageName = null;
        this.mEventTime = 0;
        if (this.mRecords != null) {
            while (!this.mRecords.isEmpty()) {
                this.mRecords.remove(0).recycle();
            }
        }
    }

    public void initFromParcel(Parcel parcel) {
        boolean z = true;
        if (parcel.readInt() != 1) {
            z = false;
        }
        this.mSealed = z;
        this.mEventType = parcel.readInt();
        this.mMovementGranularity = parcel.readInt();
        this.mAction = parcel.readInt();
        this.mContentChangeTypes = parcel.readInt();
        this.mWindowChangeTypes = parcel.readInt();
        this.mPackageName = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
        this.mEventTime = parcel.readLong();
        this.mConnectionId = parcel.readInt();
        readAccessibilityRecordFromParcel(this, parcel);
        int recordCount = parcel.readInt();
        if (recordCount > 0) {
            this.mRecords = new ArrayList<>(recordCount);
            for (int i = 0; i < recordCount; i++) {
                AccessibilityRecord record = AccessibilityRecord.obtain();
                readAccessibilityRecordFromParcel(record, parcel);
                record.mConnectionId = this.mConnectionId;
                this.mRecords.add(record);
            }
        }
    }

    private void readAccessibilityRecordFromParcel(AccessibilityRecord record, Parcel parcel) {
        record.mBooleanProperties = parcel.readInt();
        record.mCurrentItemIndex = parcel.readInt();
        record.mItemCount = parcel.readInt();
        record.mFromIndex = parcel.readInt();
        record.mToIndex = parcel.readInt();
        record.mScrollX = parcel.readInt();
        record.mScrollY = parcel.readInt();
        record.mScrollDeltaX = parcel.readInt();
        record.mScrollDeltaY = parcel.readInt();
        record.mMaxScrollX = parcel.readInt();
        record.mMaxScrollY = parcel.readInt();
        record.mAddedCount = parcel.readInt();
        record.mRemovedCount = parcel.readInt();
        record.mClassName = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
        record.mContentDescription = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
        record.mBeforeText = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
        record.mParcelableData = parcel.readParcelable(null);
        parcel.readList(record.mText, null);
        record.mSourceWindowId = parcel.readInt();
        record.mSourceNodeId = parcel.readLong();
        boolean z = true;
        if (parcel.readInt() != 1) {
            z = false;
        }
        record.mSealed = z;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(isSealed() ? 1 : 0);
        parcel.writeInt(this.mEventType);
        parcel.writeInt(this.mMovementGranularity);
        parcel.writeInt(this.mAction);
        parcel.writeInt(this.mContentChangeTypes);
        parcel.writeInt(this.mWindowChangeTypes);
        TextUtils.writeToParcel(this.mPackageName, parcel, 0);
        parcel.writeLong(this.mEventTime);
        parcel.writeInt(this.mConnectionId);
        writeAccessibilityRecordToParcel(this, parcel, flags);
        int recordCount = getRecordCount();
        parcel.writeInt(recordCount);
        for (int i = 0; i < recordCount; i++) {
            writeAccessibilityRecordToParcel(this.mRecords.get(i), parcel, flags);
        }
    }

    private void writeAccessibilityRecordToParcel(AccessibilityRecord record, Parcel parcel, int flags) {
        parcel.writeInt(record.mBooleanProperties);
        parcel.writeInt(record.mCurrentItemIndex);
        parcel.writeInt(record.mItemCount);
        parcel.writeInt(record.mFromIndex);
        parcel.writeInt(record.mToIndex);
        parcel.writeInt(record.mScrollX);
        parcel.writeInt(record.mScrollY);
        parcel.writeInt(record.mScrollDeltaX);
        parcel.writeInt(record.mScrollDeltaY);
        parcel.writeInt(record.mMaxScrollX);
        parcel.writeInt(record.mMaxScrollY);
        parcel.writeInt(record.mAddedCount);
        parcel.writeInt(record.mRemovedCount);
        TextUtils.writeToParcel(record.mClassName, parcel, flags);
        TextUtils.writeToParcel(record.mContentDescription, parcel, flags);
        TextUtils.writeToParcel(record.mBeforeText, parcel, flags);
        parcel.writeParcelable(record.mParcelableData, flags);
        parcel.writeList(record.mText);
        parcel.writeInt(record.mSourceWindowId);
        parcel.writeLong(record.mSourceNodeId);
        parcel.writeInt(record.mSealed ? 1 : 0);
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EventType: ");
        builder.append(eventTypeToString(this.mEventType));
        builder.append("; EventTime: ");
        builder.append(this.mEventTime);
        builder.append("; PackageName: ");
        builder.append(this.mPackageName);
        builder.append("; MovementGranularity: ");
        builder.append(this.mMovementGranularity);
        builder.append("; Action: ");
        builder.append(this.mAction);
        builder.append("; ContentChangeTypes: ");
        builder.append(contentChangeTypesToString(this.mContentChangeTypes));
        builder.append("; WindowChangeTypes: ");
        builder.append(windowChangeTypesToString(this.mWindowChangeTypes));
        super.appendTo(builder);
        builder.append("; recordCount: ");
        builder.append(getRecordCount());
        return builder.toString();
    }

    public static String eventTypeToString(int eventType) {
        if (eventType == -1) {
            return "TYPES_ALL_MASK";
        }
        StringBuilder builder = new StringBuilder();
        int eventType2 = eventType;
        int eventTypeCount = 0;
        while (eventType2 != 0) {
            int eventTypeFlag = 1 << Integer.numberOfTrailingZeros(eventType2);
            eventType2 &= ~eventTypeFlag;
            if (eventTypeCount > 0) {
                builder.append(", ");
            }
            builder.append(singleEventTypeToString(eventTypeFlag));
            eventTypeCount++;
        }
        if (eventTypeCount > 1) {
            builder.insert(0, '[');
            builder.append(']');
        }
        return builder.toString();
    }

    private static String singleEventTypeToString(int eventType) {
        switch (eventType) {
            case 1:
                return "TYPE_VIEW_CLICKED";
            case 2:
                return "TYPE_VIEW_LONG_CLICKED";
            case 4:
                return "TYPE_VIEW_SELECTED";
            case 8:
                return "TYPE_VIEW_FOCUSED";
            case 16:
                return "TYPE_VIEW_TEXT_CHANGED";
            case 32:
                return "TYPE_WINDOW_STATE_CHANGED";
            case 64:
                return "TYPE_NOTIFICATION_STATE_CHANGED";
            case 128:
                return "TYPE_VIEW_HOVER_ENTER";
            case 256:
                return "TYPE_VIEW_HOVER_EXIT";
            case 512:
                return "TYPE_TOUCH_EXPLORATION_GESTURE_START";
            case 1024:
                return "TYPE_TOUCH_EXPLORATION_GESTURE_END";
            case 2048:
                return "TYPE_WINDOW_CONTENT_CHANGED";
            case 4096:
                return "TYPE_VIEW_SCROLLED";
            case 8192:
                return "TYPE_VIEW_TEXT_SELECTION_CHANGED";
            case 16384:
                return "TYPE_ANNOUNCEMENT";
            case 32768:
                return "TYPE_VIEW_ACCESSIBILITY_FOCUSED";
            case 65536:
                return "TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED";
            case 131072:
                return "TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY";
            case 262144:
                return "TYPE_GESTURE_DETECTION_START";
            case 524288:
                return "TYPE_GESTURE_DETECTION_END";
            case 1048576:
                return "TYPE_TOUCH_INTERACTION_START";
            case 2097152:
                return "TYPE_TOUCH_INTERACTION_END";
            case 4194304:
                return "TYPE_WINDOWS_CHANGED";
            case 8388608:
                return "TYPE_VIEW_CONTEXT_CLICKED";
            case 16777216:
                return "TYPE_ASSIST_READING_CONTEXT";
            default:
                return Integer.toHexString(eventType);
        }
    }
}
