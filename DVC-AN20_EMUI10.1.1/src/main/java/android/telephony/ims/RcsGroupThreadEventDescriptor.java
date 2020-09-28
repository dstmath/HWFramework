package android.telephony.ims;

import android.os.Parcel;

public abstract class RcsGroupThreadEventDescriptor extends RcsEventDescriptor {
    protected final int mOriginatingParticipantId;
    protected final int mRcsGroupThreadId;

    RcsGroupThreadEventDescriptor(long timestamp, int rcsGroupThreadId, int originatingParticipantId) {
        super(timestamp);
        this.mRcsGroupThreadId = rcsGroupThreadId;
        this.mOriginatingParticipantId = originatingParticipantId;
    }

    RcsGroupThreadEventDescriptor(Parcel in) {
        super(in);
        this.mRcsGroupThreadId = in.readInt();
        this.mOriginatingParticipantId = in.readInt();
    }

    @Override // android.os.Parcelable, android.telephony.ims.RcsEventDescriptor
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.mRcsGroupThreadId);
        dest.writeInt(this.mOriginatingParticipantId);
    }
}
