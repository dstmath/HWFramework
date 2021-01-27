package android.telephony.ims;

import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RcsThreadQueryParams implements Parcelable {
    public static final Parcelable.Creator<RcsThreadQueryParams> CREATOR = new Parcelable.Creator<RcsThreadQueryParams>() {
        /* class android.telephony.ims.RcsThreadQueryParams.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RcsThreadQueryParams createFromParcel(Parcel in) {
            return new RcsThreadQueryParams(in);
        }

        @Override // android.os.Parcelable.Creator
        public RcsThreadQueryParams[] newArray(int size) {
            return new RcsThreadQueryParams[size];
        }
    };
    public static final int SORT_BY_CREATION_ORDER = 0;
    public static final int SORT_BY_TIMESTAMP = 1;
    public static final String THREAD_QUERY_PARAMETERS_KEY = "thread_query_parameters";
    public static final int THREAD_TYPE_1_TO_1 = 2;
    public static final int THREAD_TYPE_GROUP = 1;
    private final boolean mIsAscending;
    private final int mLimit;
    private final List<Integer> mRcsParticipantIds;
    private final int mSortingProperty;
    private final int mThreadType;

    @Retention(RetentionPolicy.SOURCE)
    public @interface SortingProperty {
    }

    RcsThreadQueryParams(int threadType, Set<RcsParticipant> participants, int limit, int sortingProperty, boolean isAscending) {
        this.mThreadType = threadType;
        this.mRcsParticipantIds = convertParticipantSetToIdList(participants);
        this.mLimit = limit;
        this.mSortingProperty = sortingProperty;
        this.mIsAscending = isAscending;
    }

    private static List<Integer> convertParticipantSetToIdList(Set<RcsParticipant> participants) {
        List<Integer> ids = new ArrayList<>(participants.size());
        for (RcsParticipant participant : participants) {
            ids.add(Integer.valueOf(participant.getId()));
        }
        return ids;
    }

    public List<Integer> getRcsParticipantsIds() {
        return Collections.unmodifiableList(this.mRcsParticipantIds);
    }

    public int getThreadType() {
        return this.mThreadType;
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

    public static class Builder {
        private boolean mIsAscending;
        private int mLimit = 100;
        private Set<RcsParticipant> mParticipants = new HashSet();
        private int mSortingProperty;
        private int mThreadType;

        public Builder setThreadType(int threadType) {
            this.mThreadType = threadType;
            return this;
        }

        public Builder setParticipant(RcsParticipant participant) {
            this.mParticipants.add(participant);
            return this;
        }

        public Builder setParticipants(List<RcsParticipant> participants) {
            this.mParticipants.addAll(participants);
            return this;
        }

        public Builder setResultLimit(int limit) throws InvalidParameterException {
            if (limit >= 0) {
                this.mLimit = limit;
                return this;
            }
            throw new InvalidParameterException("The query limit must be non-negative");
        }

        public Builder setSortProperty(int sortingProperty) {
            this.mSortingProperty = sortingProperty;
            return this;
        }

        public Builder setSortDirection(boolean isAscending) {
            this.mIsAscending = isAscending;
            return this;
        }

        public RcsThreadQueryParams build() {
            return new RcsThreadQueryParams(this.mThreadType, this.mParticipants, this.mLimit, this.mSortingProperty, this.mIsAscending);
        }
    }

    private RcsThreadQueryParams(Parcel in) {
        this.mThreadType = in.readInt();
        this.mRcsParticipantIds = new ArrayList();
        in.readList(this.mRcsParticipantIds, Integer.class.getClassLoader());
        this.mLimit = in.readInt();
        this.mSortingProperty = in.readInt();
        this.mIsAscending = in.readByte() != 1 ? false : true;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mThreadType);
        dest.writeList(this.mRcsParticipantIds);
        dest.writeInt(this.mLimit);
        dest.writeInt(this.mSortingProperty);
        dest.writeByte(this.mIsAscending ? (byte) 1 : 0);
    }
}
