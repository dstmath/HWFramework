package android.telephony.ims;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.annotations.VisibleForTesting;

public class RcsGroupThreadNameChangedEventDescriptor extends RcsGroupThreadEventDescriptor {
    public static final Parcelable.Creator<RcsGroupThreadNameChangedEventDescriptor> CREATOR = new Parcelable.Creator<RcsGroupThreadNameChangedEventDescriptor>() {
        /* class android.telephony.ims.RcsGroupThreadNameChangedEventDescriptor.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RcsGroupThreadNameChangedEventDescriptor createFromParcel(Parcel in) {
            return new RcsGroupThreadNameChangedEventDescriptor(in);
        }

        @Override // android.os.Parcelable.Creator
        public RcsGroupThreadNameChangedEventDescriptor[] newArray(int size) {
            return new RcsGroupThreadNameChangedEventDescriptor[size];
        }
    };
    private final String mNewName;

    public RcsGroupThreadNameChangedEventDescriptor(long timestamp, int rcsGroupThreadId, int originatingParticipantId, String newName) {
        super(timestamp, rcsGroupThreadId, originatingParticipantId);
        this.mNewName = newName;
    }

    @Override // android.telephony.ims.RcsEventDescriptor
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PROTECTED)
    public RcsGroupThreadNameChangedEvent createRcsEvent(RcsControllerCall rcsControllerCall) {
        return new RcsGroupThreadNameChangedEvent(this.mTimestamp, new RcsGroupThread(rcsControllerCall, this.mRcsGroupThreadId), new RcsParticipant(rcsControllerCall, this.mOriginatingParticipantId), this.mNewName);
    }

    protected RcsGroupThreadNameChangedEventDescriptor(Parcel in) {
        super(in);
        this.mNewName = in.readString();
    }

    @Override // android.os.Parcelable, android.telephony.ims.RcsEventDescriptor
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable, android.telephony.ims.RcsEventDescriptor, android.telephony.ims.RcsGroupThreadEventDescriptor
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.mNewName);
    }
}
