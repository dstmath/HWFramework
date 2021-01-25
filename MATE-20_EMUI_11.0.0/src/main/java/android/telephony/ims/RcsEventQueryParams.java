package android.telephony.ims;

import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.InvalidParameterException;

public final class RcsEventQueryParams implements Parcelable {
    public static final int ALL_EVENTS = -1;
    public static final int ALL_GROUP_THREAD_EVENTS = 0;
    public static final Parcelable.Creator<RcsEventQueryParams> CREATOR = new Parcelable.Creator<RcsEventQueryParams>() {
        /* class android.telephony.ims.RcsEventQueryParams.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RcsEventQueryParams createFromParcel(Parcel in) {
            return new RcsEventQueryParams(in);
        }

        @Override // android.os.Parcelable.Creator
        public RcsEventQueryParams[] newArray(int size) {
            return new RcsEventQueryParams[size];
        }
    };
    public static final String EVENT_QUERY_PARAMETERS_KEY = "event_query_parameters";
    public static final int GROUP_THREAD_ICON_CHANGED_EVENT = 8;
    public static final int GROUP_THREAD_NAME_CHANGED_EVENT = 16;
    public static final int GROUP_THREAD_PARTICIPANT_JOINED_EVENT = 2;
    public static final int GROUP_THREAD_PARTICIPANT_LEFT_EVENT = 4;
    public static final int PARTICIPANT_ALIAS_CHANGED_EVENT = 1;
    public static final int SORT_BY_CREATION_ORDER = 0;
    public static final int SORT_BY_TIMESTAMP = 1;
    private int mEventType;
    private boolean mIsAscending;
    private int mLimit;
    private int mSortingProperty;
    private int mThreadId;

    @Retention(RetentionPolicy.SOURCE)
    public @interface EventType {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SortingProperty {
    }

    RcsEventQueryParams(int eventType, int threadId, int sortingProperty, boolean isAscending, int limit) {
        this.mEventType = eventType;
        this.mSortingProperty = sortingProperty;
        this.mIsAscending = isAscending;
        this.mLimit = limit;
        this.mThreadId = threadId;
    }

    public int getEventType() {
        return this.mEventType;
    }

    public int getLimit() {
        return this.mLimit;
    }

    public int getSortingProperty() {
        return this.mSortingProperty;
    }

    public boolean getSortDirection() {
        return this.mIsAscending;
    }

    public int getThreadId() {
        return this.mThreadId;
    }

    public static class Builder {
        private int mEventType;
        private boolean mIsAscending;
        private int mLimit = 100;
        private int mSortingProperty;
        private int mThreadId;

        public Builder setResultLimit(int limit) throws InvalidParameterException {
            if (limit >= 0) {
                this.mLimit = limit;
                return this;
            }
            throw new InvalidParameterException("The query limit must be non-negative");
        }

        public Builder setEventType(int eventType) {
            this.mEventType = eventType;
            return this;
        }

        public Builder setSortProperty(int sortingProperty) {
            this.mSortingProperty = sortingProperty;
            return this;
        }

        public Builder setSortDirection(boolean isAscending) {
            this.mIsAscending = isAscending;
            return this;
        }

        public Builder setGroupThread(RcsGroupThread groupThread) {
            this.mThreadId = groupThread.getThreadId();
            return this;
        }

        public RcsEventQueryParams build() {
            return new RcsEventQueryParams(this.mEventType, this.mThreadId, this.mSortingProperty, this.mIsAscending, this.mLimit);
        }
    }

    private RcsEventQueryParams(Parcel in) {
        this.mEventType = in.readInt();
        this.mThreadId = in.readInt();
        this.mSortingProperty = in.readInt();
        this.mIsAscending = in.readBoolean();
        this.mLimit = in.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mEventType);
        dest.writeInt(this.mThreadId);
        dest.writeInt(this.mSortingProperty);
        dest.writeBoolean(this.mIsAscending);
        dest.writeInt(this.mLimit);
    }
}
