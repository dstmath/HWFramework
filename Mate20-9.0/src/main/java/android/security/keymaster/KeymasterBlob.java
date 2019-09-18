package android.security.keymaster;

import android.os.Parcel;
import android.os.Parcelable;

public class KeymasterBlob implements Parcelable {
    public static final Parcelable.Creator<KeymasterBlob> CREATOR = new Parcelable.Creator<KeymasterBlob>() {
        public KeymasterBlob createFromParcel(Parcel in) {
            return new KeymasterBlob(in);
        }

        public KeymasterBlob[] newArray(int length) {
            return new KeymasterBlob[length];
        }
    };
    public byte[] blob;

    public KeymasterBlob(byte[] blob2) {
        this.blob = blob2;
    }

    protected KeymasterBlob(Parcel in) {
        this.blob = in.createByteArray();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeByteArray(this.blob);
    }
}
