package android.telephony.ims;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public final class RcsParticipantQueryResultParcelable implements Parcelable {
    public static final Parcelable.Creator<RcsParticipantQueryResultParcelable> CREATOR = new Parcelable.Creator<RcsParticipantQueryResultParcelable>() {
        /* class android.telephony.ims.RcsParticipantQueryResultParcelable.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RcsParticipantQueryResultParcelable createFromParcel(Parcel in) {
            return new RcsParticipantQueryResultParcelable(in);
        }

        @Override // android.os.Parcelable.Creator
        public RcsParticipantQueryResultParcelable[] newArray(int size) {
            return new RcsParticipantQueryResultParcelable[size];
        }
    };
    final RcsQueryContinuationToken mContinuationToken;
    final List<Integer> mParticipantIds;

    public RcsParticipantQueryResultParcelable(RcsQueryContinuationToken continuationToken, List<Integer> participantIds) {
        this.mContinuationToken = continuationToken;
        this.mParticipantIds = participantIds;
    }

    private RcsParticipantQueryResultParcelable(Parcel in) {
        this.mContinuationToken = (RcsQueryContinuationToken) in.readParcelable(RcsQueryContinuationToken.class.getClassLoader());
        this.mParticipantIds = new ArrayList();
        in.readList(this.mParticipantIds, Integer.class.getClassLoader());
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mContinuationToken, flags);
        dest.writeList(this.mParticipantIds);
    }
}
