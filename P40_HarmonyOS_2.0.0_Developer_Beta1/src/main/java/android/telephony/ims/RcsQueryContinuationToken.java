package android.telephony.ims;

import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class RcsQueryContinuationToken implements Parcelable {
    public static final Parcelable.Creator<RcsQueryContinuationToken> CREATOR = new Parcelable.Creator<RcsQueryContinuationToken>() {
        /* class android.telephony.ims.RcsQueryContinuationToken.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RcsQueryContinuationToken createFromParcel(Parcel in) {
            return new RcsQueryContinuationToken(in);
        }

        @Override // android.os.Parcelable.Creator
        public RcsQueryContinuationToken[] newArray(int size) {
            return new RcsQueryContinuationToken[size];
        }
    };
    public static final int EVENT_QUERY_CONTINUATION_TOKEN_TYPE = 0;
    public static final int MESSAGE_QUERY_CONTINUATION_TOKEN_TYPE = 1;
    public static final int PARTICIPANT_QUERY_CONTINUATION_TOKEN_TYPE = 2;
    public static final String QUERY_CONTINUATION_TOKEN = "query_continuation_token";
    public static final int THREAD_QUERY_CONTINUATION_TOKEN_TYPE = 3;
    private final int mLimit;
    private int mOffset;
    private int mQueryType;
    private final String mRawQuery;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ContinuationTokenType {
    }

    public RcsQueryContinuationToken(int queryType, String rawQuery, int limit, int offset) {
        this.mQueryType = queryType;
        this.mRawQuery = rawQuery;
        this.mLimit = limit;
        this.mOffset = offset;
    }

    public String getRawQuery() {
        return this.mRawQuery;
    }

    public int getOffset() {
        return this.mOffset;
    }

    public void incrementOffset() {
        this.mOffset += this.mLimit;
    }

    public int getQueryType() {
        return this.mQueryType;
    }

    private RcsQueryContinuationToken(Parcel in) {
        this.mQueryType = in.readInt();
        this.mRawQuery = in.readString();
        this.mLimit = in.readInt();
        this.mOffset = in.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mQueryType);
        dest.writeString(this.mRawQuery);
        dest.writeInt(this.mLimit);
        dest.writeInt(this.mOffset);
    }
}
