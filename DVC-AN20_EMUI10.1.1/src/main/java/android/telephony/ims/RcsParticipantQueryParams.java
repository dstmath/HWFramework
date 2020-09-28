package android.telephony.ims;

import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.InvalidParameterException;

public final class RcsParticipantQueryParams implements Parcelable {
    public static final Parcelable.Creator<RcsParticipantQueryParams> CREATOR = new Parcelable.Creator<RcsParticipantQueryParams>() {
        /* class android.telephony.ims.RcsParticipantQueryParams.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RcsParticipantQueryParams createFromParcel(Parcel in) {
            return new RcsParticipantQueryParams(in);
        }

        @Override // android.os.Parcelable.Creator
        public RcsParticipantQueryParams[] newArray(int size) {
            return new RcsParticipantQueryParams[size];
        }
    };
    public static final String PARTICIPANT_QUERY_PARAMETERS_KEY = "participant_query_parameters";
    public static final int SORT_BY_ALIAS = 1;
    public static final int SORT_BY_CANONICAL_ADDRESS = 2;
    public static final int SORT_BY_CREATION_ORDER = 0;
    private String mAliasLike;
    private String mCanonicalAddressLike;
    private boolean mIsAscending;
    private int mLimit;
    private int mSortingProperty;
    private int mThreadId;

    @Retention(RetentionPolicy.SOURCE)
    public @interface SortingProperty {
    }

    RcsParticipantQueryParams(int rcsThreadId, String aliasLike, String canonicalAddressLike, int sortingProperty, boolean isAscending, int limit) {
        this.mThreadId = rcsThreadId;
        this.mAliasLike = aliasLike;
        this.mCanonicalAddressLike = canonicalAddressLike;
        this.mSortingProperty = sortingProperty;
        this.mIsAscending = isAscending;
        this.mLimit = limit;
    }

    public int getThreadId() {
        return this.mThreadId;
    }

    public String getAliasLike() {
        return this.mAliasLike;
    }

    public String getCanonicalAddressLike() {
        return this.mCanonicalAddressLike;
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
        private String mAliasLike;
        private String mCanonicalAddressLike;
        private boolean mIsAscending;
        private int mLimit = 100;
        private int mSortingProperty;
        private int mThreadId;

        public Builder setThread(RcsThread rcsThread) {
            this.mThreadId = rcsThread.getThreadId();
            return this;
        }

        public Builder setAliasLike(String likeClause) {
            this.mAliasLike = likeClause;
            return this;
        }

        public Builder setCanonicalAddressLike(String likeClause) {
            this.mCanonicalAddressLike = likeClause;
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

        public RcsParticipantQueryParams build() {
            return new RcsParticipantQueryParams(this.mThreadId, this.mAliasLike, this.mCanonicalAddressLike, this.mSortingProperty, this.mIsAscending, this.mLimit);
        }
    }

    private RcsParticipantQueryParams(Parcel in) {
        this.mAliasLike = in.readString();
        this.mCanonicalAddressLike = in.readString();
        this.mSortingProperty = in.readInt();
        this.mIsAscending = in.readByte() != 1 ? false : true;
        this.mLimit = in.readInt();
        this.mThreadId = in.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mAliasLike);
        dest.writeString(this.mCanonicalAddressLike);
        dest.writeInt(this.mSortingProperty);
        dest.writeByte(this.mIsAscending ? (byte) 1 : 0);
        dest.writeInt(this.mLimit);
        dest.writeInt(this.mThreadId);
    }
}
