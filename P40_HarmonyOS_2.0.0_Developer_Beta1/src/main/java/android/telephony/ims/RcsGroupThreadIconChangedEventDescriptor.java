package android.telephony.ims;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.annotations.VisibleForTesting;

public class RcsGroupThreadIconChangedEventDescriptor extends RcsGroupThreadEventDescriptor {
    public static final Parcelable.Creator<RcsGroupThreadIconChangedEventDescriptor> CREATOR = new Parcelable.Creator<RcsGroupThreadIconChangedEventDescriptor>() {
        /* class android.telephony.ims.RcsGroupThreadIconChangedEventDescriptor.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public RcsGroupThreadIconChangedEventDescriptor createFromParcel(Parcel in) {
            return new RcsGroupThreadIconChangedEventDescriptor(in);
        }

        @Override // android.os.Parcelable.Creator
        public RcsGroupThreadIconChangedEventDescriptor[] newArray(int size) {
            return new RcsGroupThreadIconChangedEventDescriptor[size];
        }
    };
    private final Uri mNewIcon;

    public RcsGroupThreadIconChangedEventDescriptor(long timestamp, int rcsGroupThreadId, int originatingParticipantId, Uri newIcon) {
        super(timestamp, rcsGroupThreadId, originatingParticipantId);
        this.mNewIcon = newIcon;
    }

    @Override // android.telephony.ims.RcsEventDescriptor
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PROTECTED)
    public RcsGroupThreadIconChangedEvent createRcsEvent(RcsControllerCall rcsControllerCall) {
        return new RcsGroupThreadIconChangedEvent(this.mTimestamp, new RcsGroupThread(rcsControllerCall, this.mRcsGroupThreadId), new RcsParticipant(rcsControllerCall, this.mOriginatingParticipantId), this.mNewIcon);
    }

    protected RcsGroupThreadIconChangedEventDescriptor(Parcel in) {
        super(in);
        this.mNewIcon = (Uri) in.readParcelable(Uri.class.getClassLoader());
    }

    @Override // android.telephony.ims.RcsEventDescriptor, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.telephony.ims.RcsGroupThreadEventDescriptor, android.telephony.ims.RcsEventDescriptor, android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(this.mNewIcon, flags);
    }
}
