package android.net.wifi.aware;

import android.os.Parcel;
import android.os.Parcelable;

public final class ParcelablePeerHandle extends PeerHandle implements Parcelable {
    public static final Parcelable.Creator<ParcelablePeerHandle> CREATOR = new Parcelable.Creator<ParcelablePeerHandle>() {
        /* class android.net.wifi.aware.ParcelablePeerHandle.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ParcelablePeerHandle[] newArray(int size) {
            return new ParcelablePeerHandle[size];
        }

        @Override // android.os.Parcelable.Creator
        public ParcelablePeerHandle createFromParcel(Parcel in) {
            return new ParcelablePeerHandle(new PeerHandle(in.readInt()));
        }
    };

    public ParcelablePeerHandle(PeerHandle peerHandle) {
        super(peerHandle.peerId);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.peerId);
    }
}
