package android.telephony.ims;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.ims.RcsTypeIdPair;
import java.util.ArrayList;
import java.util.List;

public final class RcsThreadQueryResultParcelable implements Parcelable {
    public static final Parcelable.Creator<RcsThreadQueryResultParcelable> CREATOR = new Parcelable.Creator<RcsThreadQueryResultParcelable>() {
        /* class android.telephony.ims.RcsThreadQueryResultParcelable.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RcsThreadQueryResultParcelable createFromParcel(Parcel in) {
            return new RcsThreadQueryResultParcelable(in);
        }

        @Override // android.os.Parcelable.Creator
        public RcsThreadQueryResultParcelable[] newArray(int size) {
            return new RcsThreadQueryResultParcelable[size];
        }
    };
    final RcsQueryContinuationToken mContinuationToken;
    final List<RcsTypeIdPair> mRcsThreadIds;

    public RcsThreadQueryResultParcelable(RcsQueryContinuationToken continuationToken, List<RcsTypeIdPair> rcsThreadIds) {
        this.mContinuationToken = continuationToken;
        this.mRcsThreadIds = rcsThreadIds;
    }

    private RcsThreadQueryResultParcelable(Parcel in) {
        this.mContinuationToken = (RcsQueryContinuationToken) in.readParcelable(RcsQueryContinuationToken.class.getClassLoader());
        this.mRcsThreadIds = new ArrayList();
        in.readList(this.mRcsThreadIds, RcsTypeIdPair.class.getClassLoader());
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mContinuationToken, flags);
        dest.writeList(this.mRcsThreadIds);
    }
}
