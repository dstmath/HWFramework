package android.telephony.ims;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.annotations.VisibleForTesting;

public class RcsGroupThreadParticipantJoinedEventDescriptor extends RcsGroupThreadEventDescriptor {
    public static final Parcelable.Creator<RcsGroupThreadParticipantJoinedEventDescriptor> CREATOR = new Parcelable.Creator<RcsGroupThreadParticipantJoinedEventDescriptor>() {
        /* class android.telephony.ims.RcsGroupThreadParticipantJoinedEventDescriptor.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RcsGroupThreadParticipantJoinedEventDescriptor createFromParcel(Parcel in) {
            return new RcsGroupThreadParticipantJoinedEventDescriptor(in);
        }

        @Override // android.os.Parcelable.Creator
        public RcsGroupThreadParticipantJoinedEventDescriptor[] newArray(int size) {
            return new RcsGroupThreadParticipantJoinedEventDescriptor[size];
        }
    };
    private final int mJoinedParticipantId;

    public RcsGroupThreadParticipantJoinedEventDescriptor(long timestamp, int rcsGroupThreadId, int originatingParticipantId, int joinedParticipantId) {
        super(timestamp, rcsGroupThreadId, originatingParticipantId);
        this.mJoinedParticipantId = joinedParticipantId;
    }

    @Override // android.telephony.ims.RcsEventDescriptor
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PROTECTED)
    public RcsGroupThreadParticipantJoinedEvent createRcsEvent(RcsControllerCall rcsControllerCall) {
        return new RcsGroupThreadParticipantJoinedEvent(this.mTimestamp, new RcsGroupThread(rcsControllerCall, this.mRcsGroupThreadId), new RcsParticipant(rcsControllerCall, this.mOriginatingParticipantId), new RcsParticipant(rcsControllerCall, this.mJoinedParticipantId));
    }

    protected RcsGroupThreadParticipantJoinedEventDescriptor(Parcel in) {
        super(in);
        this.mJoinedParticipantId = in.readInt();
    }

    @Override // android.os.Parcelable, android.telephony.ims.RcsEventDescriptor
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable, android.telephony.ims.RcsEventDescriptor, android.telephony.ims.RcsGroupThreadEventDescriptor
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.mJoinedParticipantId);
    }
}
