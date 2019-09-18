package android.telecom;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class ConferenceParticipant implements Parcelable {
    public static final Parcelable.Creator<ConferenceParticipant> CREATOR = new Parcelable.Creator<ConferenceParticipant>() {
        public ConferenceParticipant createFromParcel(Parcel source) {
            ClassLoader classLoader = ParcelableCall.class.getClassLoader();
            return new ConferenceParticipant((Uri) source.readParcelable(classLoader), source.readString(), (Uri) source.readParcelable(classLoader), source.readInt());
        }

        public ConferenceParticipant[] newArray(int size) {
            return new ConferenceParticipant[size];
        }
    };
    private final String mDisplayName;
    private final Uri mEndpoint;
    private final Uri mHandle;
    private final int mState;

    public ConferenceParticipant(Uri handle, String displayName, Uri endpoint, int state) {
        this.mHandle = handle;
        this.mDisplayName = displayName;
        this.mEndpoint = endpoint;
        this.mState = state;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mHandle, 0);
        dest.writeString(this.mDisplayName);
        dest.writeParcelable(this.mEndpoint, 0);
        dest.writeInt(this.mState);
    }

    public String toString() {
        return "[ConferenceParticipant Handle: " + "XXX" + " DisplayName: " + Log.pii(this.mDisplayName) + " Endpoint: " + "XXX" + " State: " + Connection.stateToString(this.mState) + "]";
    }

    public Uri getHandle() {
        return this.mHandle;
    }

    public String getDisplayName() {
        return this.mDisplayName;
    }

    public Uri getEndpoint() {
        return this.mEndpoint;
    }

    public int getState() {
        return this.mState;
    }
}
