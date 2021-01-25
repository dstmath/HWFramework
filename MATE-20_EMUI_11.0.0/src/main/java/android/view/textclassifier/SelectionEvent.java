package android.view.textclassifier;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;
import java.util.Objects;

public final class SelectionEvent implements Parcelable {
    public static final int ACTION_ABANDON = 107;
    public static final int ACTION_COPY = 101;
    public static final int ACTION_CUT = 103;
    public static final int ACTION_DRAG = 106;
    public static final int ACTION_OTHER = 108;
    public static final int ACTION_OVERTYPE = 100;
    public static final int ACTION_PASTE = 102;
    public static final int ACTION_RESET = 201;
    public static final int ACTION_SELECT_ALL = 200;
    public static final int ACTION_SHARE = 104;
    public static final int ACTION_SMART_SHARE = 105;
    public static final Parcelable.Creator<SelectionEvent> CREATOR = new Parcelable.Creator<SelectionEvent>() {
        /* class android.view.textclassifier.SelectionEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SelectionEvent createFromParcel(Parcel in) {
            return new SelectionEvent(in);
        }

        @Override // android.os.Parcelable.Creator
        public SelectionEvent[] newArray(int size) {
            return new SelectionEvent[size];
        }
    };
    public static final int EVENT_AUTO_SELECTION = 5;
    public static final int EVENT_SELECTION_MODIFIED = 2;
    public static final int EVENT_SELECTION_STARTED = 1;
    public static final int EVENT_SMART_SELECTION_MULTI = 4;
    public static final int EVENT_SMART_SELECTION_SINGLE = 3;
    public static final int INVOCATION_LINK = 2;
    public static final int INVOCATION_MANUAL = 1;
    public static final int INVOCATION_UNKNOWN = 0;
    static final String NO_SIGNATURE = "";
    private final int mAbsoluteEnd;
    private final int mAbsoluteStart;
    private long mDurationSincePreviousEvent;
    private long mDurationSinceSessionStart;
    private int mEnd;
    private final String mEntityType;
    private int mEventIndex;
    private long mEventTime;
    private int mEventType;
    private int mInvocationMethod;
    private String mPackageName;
    private String mResultId;
    private TextClassificationSessionId mSessionId;
    private int mSmartEnd;
    private int mSmartStart;
    private int mStart;
    private int mUserId;
    private String mWidgetType;
    private String mWidgetVersion;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ActionType {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface EventType {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface InvocationMethod {
    }

    SelectionEvent(int start, int end, int eventType, String entityType, int invocationMethod, String resultId) {
        this.mPackageName = "";
        this.mWidgetType = "unknown";
        this.mUserId = -10000;
        Preconditions.checkArgument(end >= start, "end cannot be less than start");
        this.mAbsoluteStart = start;
        this.mAbsoluteEnd = end;
        this.mEventType = eventType;
        this.mEntityType = (String) Preconditions.checkNotNull(entityType);
        this.mResultId = resultId;
        this.mInvocationMethod = invocationMethod;
    }

    private SelectionEvent(Parcel in) {
        this.mPackageName = "";
        this.mWidgetType = "unknown";
        this.mUserId = -10000;
        this.mAbsoluteStart = in.readInt();
        this.mAbsoluteEnd = in.readInt();
        this.mEventType = in.readInt();
        this.mEntityType = in.readString();
        TextClassificationSessionId textClassificationSessionId = null;
        this.mWidgetVersion = in.readInt() > 0 ? in.readString() : null;
        this.mPackageName = in.readString();
        this.mWidgetType = in.readString();
        this.mInvocationMethod = in.readInt();
        this.mResultId = in.readString();
        this.mEventTime = in.readLong();
        this.mDurationSinceSessionStart = in.readLong();
        this.mDurationSincePreviousEvent = in.readLong();
        this.mEventIndex = in.readInt();
        this.mSessionId = in.readInt() > 0 ? TextClassificationSessionId.CREATOR.createFromParcel(in) : textClassificationSessionId;
        this.mStart = in.readInt();
        this.mEnd = in.readInt();
        this.mSmartStart = in.readInt();
        this.mSmartEnd = in.readInt();
        this.mUserId = in.readInt();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mAbsoluteStart);
        dest.writeInt(this.mAbsoluteEnd);
        dest.writeInt(this.mEventType);
        dest.writeString(this.mEntityType);
        int i = 1;
        dest.writeInt(this.mWidgetVersion != null ? 1 : 0);
        String str = this.mWidgetVersion;
        if (str != null) {
            dest.writeString(str);
        }
        dest.writeString(this.mPackageName);
        dest.writeString(this.mWidgetType);
        dest.writeInt(this.mInvocationMethod);
        dest.writeString(this.mResultId);
        dest.writeLong(this.mEventTime);
        dest.writeLong(this.mDurationSinceSessionStart);
        dest.writeLong(this.mDurationSincePreviousEvent);
        dest.writeInt(this.mEventIndex);
        if (this.mSessionId == null) {
            i = 0;
        }
        dest.writeInt(i);
        TextClassificationSessionId textClassificationSessionId = this.mSessionId;
        if (textClassificationSessionId != null) {
            textClassificationSessionId.writeToParcel(dest, flags);
        }
        dest.writeInt(this.mStart);
        dest.writeInt(this.mEnd);
        dest.writeInt(this.mSmartStart);
        dest.writeInt(this.mSmartEnd);
        dest.writeInt(this.mUserId);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public static SelectionEvent createSelectionStartedEvent(int invocationMethod, int start) {
        return new SelectionEvent(start, start + 1, 1, "", invocationMethod, "");
    }

    public static SelectionEvent createSelectionModifiedEvent(int start, int end) {
        Preconditions.checkArgument(end >= start, "end cannot be less than start");
        return new SelectionEvent(start, end, 2, "", 0, "");
    }

    public static SelectionEvent createSelectionModifiedEvent(int start, int end, TextClassification classification) {
        String entityType;
        Preconditions.checkArgument(end >= start, "end cannot be less than start");
        Preconditions.checkNotNull(classification);
        if (classification.getEntityCount() > 0) {
            entityType = classification.getEntity(0);
        } else {
            entityType = "";
        }
        return new SelectionEvent(start, end, 2, entityType, 0, classification.getId());
    }

    public static SelectionEvent createSelectionModifiedEvent(int start, int end, TextSelection selection) {
        String entityType;
        Preconditions.checkArgument(end >= start, "end cannot be less than start");
        Preconditions.checkNotNull(selection);
        if (selection.getEntityCount() > 0) {
            entityType = selection.getEntity(0);
        } else {
            entityType = "";
        }
        return new SelectionEvent(start, end, 5, entityType, 0, selection.getId());
    }

    public static SelectionEvent createSelectionActionEvent(int start, int end, int actionType) {
        Preconditions.checkArgument(end >= start, "end cannot be less than start");
        checkActionType(actionType);
        return new SelectionEvent(start, end, actionType, "", 0, "");
    }

    public static SelectionEvent createSelectionActionEvent(int start, int end, int actionType, TextClassification classification) {
        String entityType;
        Preconditions.checkArgument(end >= start, "end cannot be less than start");
        Preconditions.checkNotNull(classification);
        checkActionType(actionType);
        if (classification.getEntityCount() > 0) {
            entityType = classification.getEntity(0);
        } else {
            entityType = "";
        }
        return new SelectionEvent(start, end, actionType, entityType, 0, classification.getId());
    }

    private static void checkActionType(int eventType) throws IllegalArgumentException {
        if (eventType != 200 && eventType != 201) {
            switch (eventType) {
                case 100:
                case 101:
                case 102:
                case 103:
                case 104:
                case 105:
                case 106:
                case 107:
                case 108:
                    return;
                default:
                    throw new IllegalArgumentException(String.format(Locale.US, "%d is not an eventType", Integer.valueOf(eventType)));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int getAbsoluteStart() {
        return this.mAbsoluteStart;
    }

    /* access modifiers changed from: package-private */
    public int getAbsoluteEnd() {
        return this.mAbsoluteEnd;
    }

    public int getEventType() {
        return this.mEventType;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public void setEventType(int eventType) {
        this.mEventType = eventType;
    }

    public String getEntityType() {
        return this.mEntityType;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    /* access modifiers changed from: package-private */
    public void setUserId(int userId) {
        this.mUserId = userId;
    }

    public int getUserId() {
        return this.mUserId;
    }

    public String getWidgetType() {
        return this.mWidgetType;
    }

    public String getWidgetVersion() {
        return this.mWidgetVersion;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public void setTextClassificationSessionContext(TextClassificationContext context) {
        this.mPackageName = context.getPackageName();
        this.mWidgetType = context.getWidgetType();
        this.mWidgetVersion = context.getWidgetVersion();
        this.mUserId = context.getUserId();
    }

    public int getInvocationMethod() {
        return this.mInvocationMethod;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public void setInvocationMethod(int invocationMethod) {
        this.mInvocationMethod = invocationMethod;
    }

    public String getResultId() {
        return this.mResultId;
    }

    /* access modifiers changed from: package-private */
    public SelectionEvent setResultId(String resultId) {
        this.mResultId = resultId;
        return this;
    }

    public long getEventTime() {
        return this.mEventTime;
    }

    /* access modifiers changed from: package-private */
    public SelectionEvent setEventTime(long timeMs) {
        this.mEventTime = timeMs;
        return this;
    }

    public long getDurationSinceSessionStart() {
        return this.mDurationSinceSessionStart;
    }

    /* access modifiers changed from: package-private */
    public SelectionEvent setDurationSinceSessionStart(long durationMs) {
        this.mDurationSinceSessionStart = durationMs;
        return this;
    }

    public long getDurationSincePreviousEvent() {
        return this.mDurationSincePreviousEvent;
    }

    /* access modifiers changed from: package-private */
    public SelectionEvent setDurationSincePreviousEvent(long durationMs) {
        this.mDurationSincePreviousEvent = durationMs;
        return this;
    }

    public int getEventIndex() {
        return this.mEventIndex;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public SelectionEvent setEventIndex(int index) {
        this.mEventIndex = index;
        return this;
    }

    public TextClassificationSessionId getSessionId() {
        return this.mSessionId;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public SelectionEvent setSessionId(TextClassificationSessionId id) {
        this.mSessionId = id;
        return this;
    }

    public int getStart() {
        return this.mStart;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public SelectionEvent setStart(int start) {
        this.mStart = start;
        return this;
    }

    public int getEnd() {
        return this.mEnd;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public SelectionEvent setEnd(int end) {
        this.mEnd = end;
        return this;
    }

    public int getSmartStart() {
        return this.mSmartStart;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public SelectionEvent setSmartStart(int start) {
        this.mSmartStart = start;
        return this;
    }

    public int getSmartEnd() {
        return this.mSmartEnd;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public SelectionEvent setSmartEnd(int end) {
        this.mSmartEnd = end;
        return this;
    }

    /* access modifiers changed from: package-private */
    public boolean isTerminal() {
        return isTerminal(this.mEventType);
    }

    public static boolean isTerminal(int eventType) {
        switch (eventType) {
            case 100:
            case 101:
            case 102:
            case 103:
            case 104:
            case 105:
            case 106:
            case 107:
            case 108:
                return true;
            default:
                return false;
        }
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mAbsoluteStart), Integer.valueOf(this.mAbsoluteEnd), Integer.valueOf(this.mEventType), this.mEntityType, this.mWidgetVersion, this.mPackageName, Integer.valueOf(this.mUserId), this.mWidgetType, Integer.valueOf(this.mInvocationMethod), this.mResultId, Long.valueOf(this.mEventTime), Long.valueOf(this.mDurationSinceSessionStart), Long.valueOf(this.mDurationSincePreviousEvent), Integer.valueOf(this.mEventIndex), this.mSessionId, Integer.valueOf(this.mStart), Integer.valueOf(this.mEnd), Integer.valueOf(this.mSmartStart), Integer.valueOf(this.mSmartEnd));
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SelectionEvent)) {
            return false;
        }
        SelectionEvent other = (SelectionEvent) obj;
        if (this.mAbsoluteStart == other.mAbsoluteStart && this.mAbsoluteEnd == other.mAbsoluteEnd && this.mEventType == other.mEventType && Objects.equals(this.mEntityType, other.mEntityType) && Objects.equals(this.mWidgetVersion, other.mWidgetVersion) && Objects.equals(this.mPackageName, other.mPackageName) && this.mUserId == other.mUserId && Objects.equals(this.mWidgetType, other.mWidgetType) && this.mInvocationMethod == other.mInvocationMethod && Objects.equals(this.mResultId, other.mResultId) && this.mEventTime == other.mEventTime && this.mDurationSinceSessionStart == other.mDurationSinceSessionStart && this.mDurationSincePreviousEvent == other.mDurationSincePreviousEvent && this.mEventIndex == other.mEventIndex && Objects.equals(this.mSessionId, other.mSessionId) && this.mStart == other.mStart && this.mEnd == other.mEnd && this.mSmartStart == other.mSmartStart && this.mSmartEnd == other.mSmartEnd) {
            return true;
        }
        return false;
    }

    public String toString() {
        return String.format(Locale.US, "SelectionEvent {absoluteStart=%d, absoluteEnd=%d, eventType=%d, entityType=%s, widgetVersion=%s, packageName=%s, widgetType=%s, invocationMethod=%s, userId=%d, resultId=%s, eventTime=%d, durationSinceSessionStart=%d, durationSincePreviousEvent=%d, eventIndex=%d,sessionId=%s, start=%d, end=%d, smartStart=%d, smartEnd=%d}", Integer.valueOf(this.mAbsoluteStart), Integer.valueOf(this.mAbsoluteEnd), Integer.valueOf(this.mEventType), this.mEntityType, this.mWidgetVersion, this.mPackageName, this.mWidgetType, Integer.valueOf(this.mInvocationMethod), Integer.valueOf(this.mUserId), this.mResultId, Long.valueOf(this.mEventTime), Long.valueOf(this.mDurationSinceSessionStart), Long.valueOf(this.mDurationSincePreviousEvent), Integer.valueOf(this.mEventIndex), this.mSessionId, Integer.valueOf(this.mStart), Integer.valueOf(this.mEnd), Integer.valueOf(this.mSmartStart), Integer.valueOf(this.mSmartEnd));
    }
}
