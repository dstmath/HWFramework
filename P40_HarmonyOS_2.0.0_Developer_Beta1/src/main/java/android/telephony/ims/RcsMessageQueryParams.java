package android.telephony.ims;

import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.InvalidParameterException;

public final class RcsMessageQueryParams implements Parcelable {
    public static final Parcelable.Creator<RcsMessageQueryParams> CREATOR = new Parcelable.Creator<RcsMessageQueryParams>() {
        /* class android.telephony.ims.RcsMessageQueryParams.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RcsMessageQueryParams createFromParcel(Parcel in) {
            return new RcsMessageQueryParams(in);
        }

        @Override // android.os.Parcelable.Creator
        public RcsMessageQueryParams[] newArray(int size) {
            return new RcsMessageQueryParams[size];
        }
    };
    public static final int MESSAGES_WITHOUT_FILE_TRANSFERS = 8;
    public static final int MESSAGES_WITH_FILE_TRANSFERS = 4;
    public static final String MESSAGE_QUERY_PARAMETERS_KEY = "message_query_parameters";
    public static final int MESSAGE_TYPE_INCOMING = 1;
    public static final int MESSAGE_TYPE_OUTGOING = 2;
    public static final int SORT_BY_CREATION_ORDER = 0;
    public static final int SORT_BY_TIMESTAMP = 1;
    public static final int THREAD_ID_NOT_SET = -1;
    private int mFileTransferPresence;
    private boolean mIsAscending;
    private int mLimit;
    private String mMessageLike;
    private int mMessageType;
    private int mSortingProperty;
    private int mThreadId;

    @Retention(RetentionPolicy.SOURCE)
    public @interface SortingProperty {
    }

    RcsMessageQueryParams(int messageType, int fileTransferPresence, String messageLike, int threadId, int sortingProperty, boolean isAscending, int limit) {
        this.mMessageType = messageType;
        this.mFileTransferPresence = fileTransferPresence;
        this.mMessageLike = messageLike;
        this.mSortingProperty = sortingProperty;
        this.mIsAscending = isAscending;
        this.mLimit = limit;
        this.mThreadId = threadId;
    }

    public int getMessageType() {
        return this.mMessageType;
    }

    public int getFileTransferPresence() {
        return this.mFileTransferPresence;
    }

    public String getMessageLike() {
        return this.mMessageLike;
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
        private int mFileTransferPresence;
        private boolean mIsAscending;
        private int mLimit = 100;
        private String mMessageLike;
        private int mMessageType;
        private int mSortingProperty;
        private int mThreadId = -1;

        public Builder setResultLimit(int limit) throws InvalidParameterException {
            if (limit >= 0) {
                this.mLimit = limit;
                return this;
            }
            throw new InvalidParameterException("The query limit must be non-negative");
        }

        public Builder setMessageType(int messageType) {
            this.mMessageType = messageType;
            return this;
        }

        public Builder setFileTransferPresence(int fileTransferPresence) {
            this.mFileTransferPresence = fileTransferPresence;
            return this;
        }

        public Builder setMessageLike(String messageLike) {
            this.mMessageLike = messageLike;
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

        public Builder setThread(RcsThread thread) {
            if (thread == null) {
                this.mThreadId = -1;
            } else {
                this.mThreadId = thread.getThreadId();
            }
            return this;
        }

        public RcsMessageQueryParams build() {
            return new RcsMessageQueryParams(this.mMessageType, this.mFileTransferPresence, this.mMessageLike, this.mThreadId, this.mSortingProperty, this.mIsAscending, this.mLimit);
        }
    }

    private RcsMessageQueryParams(Parcel in) {
        this.mMessageType = in.readInt();
        this.mFileTransferPresence = in.readInt();
        this.mMessageLike = in.readString();
        this.mSortingProperty = in.readInt();
        this.mIsAscending = in.readBoolean();
        this.mLimit = in.readInt();
        this.mThreadId = in.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mMessageType);
        dest.writeInt(this.mFileTransferPresence);
        dest.writeString(this.mMessageLike);
        dest.writeInt(this.mSortingProperty);
        dest.writeBoolean(this.mIsAscending);
        dest.writeInt(this.mLimit);
        dest.writeInt(this.mThreadId);
    }
}
