package android.nfc;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;

public final class BeamShareData implements Parcelable {
    public static final Parcelable.Creator<BeamShareData> CREATOR = new Parcelable.Creator<BeamShareData>() {
        /* class android.nfc.BeamShareData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BeamShareData createFromParcel(Parcel source) {
            Uri[] uris = null;
            NdefMessage msg = (NdefMessage) source.readParcelable(NdefMessage.class.getClassLoader());
            int numUris = source.readInt();
            if (numUris > 0) {
                uris = new Uri[numUris];
                source.readTypedArray(uris, Uri.CREATOR);
            }
            return new BeamShareData(msg, uris, (UserHandle) source.readParcelable(UserHandle.class.getClassLoader()), source.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public BeamShareData[] newArray(int size) {
            return new BeamShareData[size];
        }
    };
    public final int flags;
    public final NdefMessage ndefMessage;
    public final Uri[] uris;
    public final UserHandle userHandle;

    public BeamShareData(NdefMessage msg, Uri[] uris2, UserHandle userHandle2, int flags2) {
        this.ndefMessage = msg;
        this.uris = uris2;
        this.userHandle = userHandle2;
        this.flags = flags2;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags2) {
        Uri[] uriArr = this.uris;
        int urisLength = uriArr != null ? uriArr.length : 0;
        dest.writeParcelable(this.ndefMessage, 0);
        dest.writeInt(urisLength);
        if (urisLength > 0) {
            dest.writeTypedArray(this.uris, 0);
        }
        dest.writeParcelable(this.userHandle, 0);
        dest.writeInt(this.flags);
    }
}
